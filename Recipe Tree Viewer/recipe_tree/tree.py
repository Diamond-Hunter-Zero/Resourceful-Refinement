from __future__ import annotations

import math
from collections import defaultdict
from typing import Any

from .loader import RecipeIndex, normalise_id
from .summary import summarize_tree
from .models import IngredientSpec, OutputSpec, ParsedRecipe, ResourceRef, TreeNode, format_resource


def build_trees(
    index: RecipeIndex,
    target_id: str,
    amount: float,
    kind: str | None = None,
    max_depth: int = 32,
    provided_inputs: list[str] | None = None,
    recipe_selections: dict[str, str] | None = None,
    simplify: bool = False,
) -> dict[str, Any]:
    resource_id = normalise_id(target_id)
    resolved_kind = kind or index.infer_kind(resource_id)
    target = ResourceRef(resolved_kind, resource_id)
    provided = parse_provided_inputs(provided_inputs or [])
    selections = dict(recipe_selections or {})
    applied: dict[str, str] = {}

    producers = index.producers_for(target)
    if not producers:
        return {
            "target": {"kind": target.kind, "id": target.id, "amount": amount},
            "provided_inputs": sorted(provided),
            "recipe_selections": applied,
            "simplify": simplify,
            "paths_found": 0,
            "paths": [],
            "tree": None,
            "shared_nodes": {},
            "message": f"No mod processing recipe produces {target.kind}:{target.id}",
        }

    demands: dict[str, float] = defaultdict(float)
    shared_cache: dict[str, TreeNode] = {}

    if simplify:
        _accumulate_demands(
            index=index,
            target=target,
            needed_amount=amount,
            path="root",
            selections=selections,
            applied=applied,
            ancestors=set(),
            depth=0,
            max_depth=max_depth,
            provided=provided,
            demands=demands,
        )

    tree = _expand_for_resource(
        index=index,
        target=target,
        needed_amount=amount,
        path="root",
        selections=selections,
        applied=applied,
        ancestors=set(),
        depth=0,
        max_depth=max_depth,
        provided=provided,
        simplify=simplify,
        demands=demands,
        shared_cache=shared_cache,
        allow_shared=False,
    )

    shared_nodes = {key: node.to_dict() for key, node in shared_cache.items()}
    tree_dict = tree.to_dict() if tree else None
    summary = summarize_tree(tree_dict, shared_nodes)

    return {
        "target": {"kind": target.kind, "id": target.id, "amount": amount},
        "provided_inputs": sorted(provided),
        "recipe_selections": applied,
        "simplify": simplify,
        "paths_found": 1 if tree else 0,
        "paths": [tree_dict] if tree_dict else [],
        "tree": tree_dict,
        "shared_nodes": shared_nodes,
        "summary": summary,
        "index_stats": index.stats(),
    }


def shared_selection_path(resource: ResourceRef) -> str:
    return f"shared/{resource.kind}/{resource.id.replace(':', '/')}"


def parse_provided_inputs(raw_ids: list[str]) -> set[str]:
    provided: set[str] = set()
    for raw in raw_ids:
        for part in raw.split(","):
            part = part.strip()
            if part:
                provided.add(normalise_id(part))
    return provided


def parse_recipe_selections(raw_entries: list[str]) -> dict[str, str]:
    """Parse `path:recipe_id` selection entries from CLI/API."""
    selections: dict[str, str] = {}
    for raw in raw_entries:
        for part in raw.split(","):
            part = part.strip()
            if not part or ":" not in part:
                continue
            path, recipe_id = part.split(":", 1)
            path = path.strip()
            recipe_id = recipe_id.strip()
            if path and recipe_id:
                selections[path] = recipe_id
    return selections


def is_provided_input(resource: ResourceRef, provided: set[str]) -> bool:
    return resource.id in provided


def _scale_step(needed_amount: float, recipe_yield: float) -> tuple[float, int, float]:
    if recipe_yield <= 0:
        return 0.0, 0, 0.0

    scale_factor = needed_amount / recipe_yield
    operational_batches = max(1, math.ceil(scale_factor)) if needed_amount > 0 else 0
    operational_output = operational_batches * recipe_yield
    operational_excess = max(0.0, operational_output - needed_amount)
    return scale_factor, operational_batches, operational_excess


def _find_primary_output(recipe: ParsedRecipe, target: ResourceRef) -> OutputSpec | None:
    for output in recipe.outputs:
        if output.kind == target.kind and output.id == target.id:
            return output
    primaries = recipe.primary_outputs()
    return primaries[0] if primaries else None


def _recipe_alternative(recipe: ParsedRecipe, target: ResourceRef, needed_amount: float) -> dict[str, Any] | None:
    primary = _find_primary_output(recipe, target)
    if not primary or primary.amount <= 0:
        return None

    scale_factor, _, operational_excess = _scale_step(needed_amount, primary.amount)
    return {
        "recipe_id": recipe.recipe_id,
        "machine": recipe.machine,
        "duration_ticks": recipe.duration_ticks,
        "proportional_seconds": round(scale_factor * recipe.duration_ticks / 20, 2),
        "scale_factor": round(scale_factor, 6),
        "operational_excess": round(operational_excess, 3),
    }


def _build_alternatives(
    producers: list[ParsedRecipe],
    target: ResourceRef,
    needed_amount: float,
) -> list[dict[str, Any]]:
    alternatives: list[dict[str, Any]] = []
    for recipe in producers:
        alt = _recipe_alternative(recipe, target, needed_amount)
        if alt:
            alternatives.append(alt)
    alternatives.sort(key=lambda a: a["proportional_seconds"])
    return alternatives


def _pick_recipe(
    producers: list[ParsedRecipe],
    target: ResourceRef,
    needed_amount: float,
    path: str,
    selections: dict[str, str],
    applied: dict[str, str],
) -> ParsedRecipe | None:
    valid = [r for r in producers if _find_primary_output(r, target)]
    if not valid:
        return None

    chosen_id = selections.get(path)
    if chosen_id:
        for recipe in valid:
            if recipe.recipe_id == chosen_id:
                applied[path] = recipe.recipe_id
                return recipe

    best = min(
        valid,
        key=lambda r: _recipe_alternative(r, target, needed_amount)["proportional_seconds"],  # type: ignore[index]
    )
    applied[path] = best.recipe_id
    return best


def _shared_ref_node(target: ResourceRef, branch_amount: float, total_amount: float) -> TreeNode:
    return TreeNode(
        resource=target,
        needed_amount=branch_amount,
        node_type="shared_ref",
        ref_key=target.key(),
        shared_total_amount=total_amount,
        note=f"Shared production ({total_amount:g} total for all consumers)",
    )


def _accumulate_demands(
    index: RecipeIndex,
    target: ResourceRef,
    needed_amount: float,
    path: str,
    selections: dict[str, str],
    applied: dict[str, str],
    ancestors: set[str],
    depth: int,
    max_depth: int,
    provided: set[str],
    demands: dict[str, float],
) -> None:
    if is_provided_input(target, provided):
        return

    if target.key() in ancestors:
        return

    producers = index.producers_for(target)
    if not producers:
        return

    demands[target.key()] += needed_amount

    if depth >= max_depth:
        return

    selection_path = shared_selection_path(target) if path != "root" else path
    recipe = _pick_recipe(producers, target, needed_amount, selection_path, selections, applied)
    if not recipe:
        return

    primary = _find_primary_output(recipe, target)
    if not primary or primary.amount <= 0:
        return

    scale_factor = needed_amount / primary.amount
    next_ancestors = set(ancestors)
    next_ancestors.add(target.key())

    if recipe.type == "resourceful_refinement:fracking_pump" and recipe.source_block:
        pass

    for i, ingredient in enumerate(recipe.ingredients):
        if ingredient.is_tag:
            continue
        _accumulate_demands(
            index=index,
            target=ResourceRef(ingredient.kind, ingredient.id),
            needed_amount=ingredient.amount * scale_factor,
            path=f"{path}.c{i}",
            selections=selections,
            applied=applied,
            ancestors=next_ancestors,
            depth=depth + 1,
            max_depth=max_depth,
            provided=provided,
            demands=demands,
        )


def _ensure_shared_node(
    index: RecipeIndex,
    target: ResourceRef,
    selections: dict[str, str],
    applied: dict[str, str],
    ancestors: set[str],
    depth: int,
    max_depth: int,
    provided: set[str],
    demands: dict[str, float],
    shared_cache: dict[str, TreeNode],
) -> TreeNode | None:
    key = target.key()
    if key in shared_cache:
        return shared_cache[key]

    total_amount = demands.get(key, 0.0)
    if total_amount <= 0:
        return None

    shared_path = shared_selection_path(target)
    node = _expand_for_resource(
        index=index,
        target=target,
        needed_amount=total_amount,
        path=shared_path,
        selections=selections,
        applied=applied,
        ancestors=ancestors,
        depth=depth,
        max_depth=max_depth,
        provided=provided,
        simplify=True,
        demands=demands,
        shared_cache=shared_cache,
        allow_shared=False,
    )
    if node:
        shared_cache[key] = node
    return node


def _expand_for_resource(
    index: RecipeIndex,
    target: ResourceRef,
    needed_amount: float,
    path: str,
    selections: dict[str, str],
    applied: dict[str, str],
    ancestors: set[str],
    depth: int,
    max_depth: int,
    provided: set[str],
    simplify: bool = False,
    demands: dict[str, float] | None = None,
    shared_cache: dict[str, TreeNode] | None = None,
    allow_shared: bool = True,
) -> TreeNode | None:
    if is_provided_input(target, provided):
        return TreeNode(
            resource=target,
            needed_amount=needed_amount,
            node_type="provided",
            note="Provided input — assumed unlimited supply",
        )

    if target.key() in ancestors:
        return TreeNode(
            resource=target,
            needed_amount=needed_amount,
            node_type="cycle",
            note="Cyclical dependency detected",
        )

    producers = index.producers_for(target)
    if not producers:
        return TreeNode(
            resource=target,
            needed_amount=needed_amount,
            node_type="external",
            note="No mod processing recipe — treat as raw/external input",
        )

    if simplify and allow_shared and path != "root" and demands is not None and shared_cache is not None:
        key = target.key()
        if key not in shared_cache:
            _ensure_shared_node(
                index=index,
                target=target,
                selections=selections,
                applied=applied,
                ancestors=ancestors,
                depth=depth,
                max_depth=max_depth,
                provided=provided,
                demands=demands,
                shared_cache=shared_cache,
            )
        if key in shared_cache:
            return _shared_ref_node(target, needed_amount, demands[key])

    alternatives = _build_alternatives(producers, target, needed_amount)
    recipe = _pick_recipe(producers, target, needed_amount, path, selections, applied)
    if not recipe:
        return TreeNode(
            resource=target,
            needed_amount=needed_amount,
            node_type="unresolved",
            note="Could not resolve production path",
        )

    return _expand_recipe(
        index=index,
        recipe=recipe,
        target=target,
        needed_amount=needed_amount,
        path=path,
        alternatives=alternatives,
        selections=selections,
        applied=applied,
        ancestors=ancestors,
        depth=depth,
        max_depth=max_depth,
        provided=provided,
        simplify=simplify,
        demands=demands,
        shared_cache=shared_cache,
    )


def _expand_recipe(
    index: RecipeIndex,
    recipe: ParsedRecipe,
    target: ResourceRef,
    needed_amount: float,
    path: str,
    alternatives: list[dict[str, Any]],
    selections: dict[str, str],
    applied: dict[str, str],
    ancestors: set[str],
    depth: int,
    max_depth: int,
    provided: set[str],
    simplify: bool = False,
    demands: dict[str, float] | None = None,
    shared_cache: dict[str, TreeNode] | None = None,
) -> TreeNode | None:
    primary = _find_primary_output(recipe, target)
    if not primary or primary.amount <= 0:
        return None

    scale_factor, operational_batches, operational_excess = _scale_step(needed_amount, primary.amount)

    node = TreeNode(
        resource=target,
        needed_amount=needed_amount,
        node_type="recipe",
        recipe=recipe,
        selection_path=path,
        selected_recipe_id=recipe.recipe_id,
        alternatives=alternatives,
        scale_factor=scale_factor,
        recipe_yield=primary.amount,
        operational_batches=operational_batches,
        operational_excess=operational_excess,
        total_duration_ticks=scale_factor * recipe.duration_ticks,
    )

    byproducts = []
    for output in recipe.outputs:
        if output is primary:
            continue
        scaled_amount = output.amount * scale_factor * output.chance
        byproducts.append(
            OutputSpec(
                kind=output.kind,
                id=output.id,
                amount=round(scaled_amount, 3),
                chance=output.chance,
                display=format_resource(output.kind, output.id, scaled_amount, output.chance),
            )
        )
    node.byproducts = byproducts

    if depth >= max_depth:
        node.note = "Maximum recursion depth reached"
        return node

    chain_key = target.key()
    if chain_key in ancestors:
        node.node_type = "cycle"
        node.note = "Cyclical dependency detected; expansion stopped"
        node.inputs = []
        return node

    next_ancestors = set(ancestors)
    next_ancestors.add(chain_key)

    if recipe.type == "resourceful_refinement:fracking_pump" and recipe.source_block:
        note = f"Geyser / source block: {recipe.source_block}"
        if recipe.source_fluid:
            note += f" (requires stored fluid: {recipe.source_fluid})"
        node.inputs.append(
            TreeNode(
                resource=ResourceRef("block", recipe.source_block),
                needed_amount=1,
                node_type="world_source",
                note=note,
            )
        )

    for i, ingredient in enumerate(recipe.ingredients):
        if ingredient.is_tag:
            node.inputs.append(
                TreeNode(
                    resource=ResourceRef("item", ingredient.id),
                    needed_amount=ingredient.amount * scale_factor,
                    node_type="external",
                    note="Tag ingredient — not expanded (no tag resolution)",
                )
            )
            continue

        required = ingredient.amount * scale_factor
        child = _expand_for_resource(
            index=index,
            target=ResourceRef(ingredient.kind, ingredient.id),
            needed_amount=required,
            path=f"{path}.c{i}",
            selections=selections,
            applied=applied,
            ancestors=next_ancestors,
            depth=depth + 1,
            max_depth=max_depth,
            provided=provided,
            simplify=simplify,
            demands=demands,
            shared_cache=shared_cache,
            allow_shared=True,
        )
        if child:
            node.inputs.append(child)

    return node
