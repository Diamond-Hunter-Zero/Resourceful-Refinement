from __future__ import annotations

import json
from collections import defaultdict
from pathlib import Path
from typing import Any

from .models import (
    IngredientSpec,
    MACHINE_LABELS,
    MOD_NAMESPACE,
    OutputSpec,
    ParsedRecipe,
    PROCESSING_RECIPE_TYPES,
    ResourceRef,
    format_resource,
)


class RecipeIndex:
    def __init__(self, recipe_root: Path):
        self.recipe_root = recipe_root.resolve()
        self.recipes: list[ParsedRecipe] = []
        self.by_output: dict[str, list[ParsedRecipe]] = defaultdict(list)
        self.fluid_ids: set[str] = set()
        self.item_ids: set[str] = set()

    @classmethod
    def load(cls, recipe_root: Path) -> "RecipeIndex":
        index = cls(recipe_root)
        index._scan()
        index._reconcile_output_kinds()
        index._classify_ids()
        index._index_outputs()
        return index

    def _scan(self) -> None:
        if not self.recipe_root.is_dir():
            raise FileNotFoundError(f"Recipe directory not found: {self.recipe_root}")

        for path in sorted(self.recipe_root.rglob("*.json")):
            try:
                data = json.loads(path.read_text(encoding="utf-8"))
            except json.JSONDecodeError as exc:
                raise ValueError(f"Invalid JSON in {path}: {exc}") from exc

            recipe_type = data.get("type")
            if recipe_type not in PROCESSING_RECIPE_TYPES:
                continue

            recipe = self._parse_recipe(path, data)
            if recipe.outputs:
                self.recipes.append(recipe)

    def _reconcile_output_kinds(self) -> None:
        """Use collected ingredient ids to fix ambiguous result entries (amount vs count)."""
        known_fluids: set[str] = set()
        known_items: set[str] = set()
        for recipe in self.recipes:
            for ing in recipe.ingredients:
                if ing.kind == "fluid":
                    known_fluids.add(ing.id)
                elif ing.kind == "item" and not ing.is_tag:
                    known_items.add(ing.id)

        for recipe in self.recipes:
            for idx, out in enumerate(recipe.outputs):
                if out.id in known_fluids and out.id not in known_items:
                    recipe.outputs[idx].kind = "fluid"
                elif out.id in known_items and out.id not in known_fluids:
                    recipe.outputs[idx].kind = "item"

    def _classify_ids(self) -> None:
        for recipe in self.recipes:
            for ing in recipe.ingredients:
                if ing.kind == "fluid":
                    self.fluid_ids.add(ing.id)
                elif ing.kind == "item" and not ing.is_tag:
                    self.item_ids.add(ing.id)
            for out in recipe.outputs:
                if out.kind == "fluid":
                    self.fluid_ids.add(out.id)
                elif out.kind == "item":
                    self.item_ids.add(out.id)

    def _index_outputs(self) -> None:
        for recipe in self.recipes:
            for output in recipe.primary_outputs():
                self.by_output[output.key()].append(recipe)

    def _parse_recipe(self, path: Path, data: dict[str, Any]) -> ParsedRecipe:
        recipe_type = data["type"]
        rel_path = str(path.relative_to(self.recipe_root))
        recipe_id = path.stem

        duration = int(
            data.get("processing_time")
            or data.get("processingTime")
            or data.get("duration")
            or 0
        )

        recipe = ParsedRecipe(
            recipe_id=recipe_id,
            path=rel_path.replace("\\", "/"),
            type=recipe_type,
            machine=MACHINE_LABELS.get(recipe_type, recipe_type),
            duration_ticks=duration,
            heat_requirement=data.get("heat_requirement"),
            casting=bool(data.get("casting", False)),
            source_block=data.get("source_block"),
            source_fluid=data.get("source_fluid"),
            coating_name=data.get("coating"),
            raw=data,
        )

        if recipe_type == "resourceful_refinement:coating":
            self._parse_coating(recipe, data)
        elif recipe_type == "create:mechanical_crafting":
            self._parse_mechanical_crafting(recipe, data)
        else:
            self._parse_standard(recipe, data)

        return recipe

    def _parse_coating(self, recipe: ParsedRecipe, data: dict[str, Any]) -> None:
        fluid = data.get("fluid", {})
        item = data.get("ingredient", {})
        coating = data.get("coating", "Unknown")

        if fluid.get("fluid"):
            recipe.ingredients.append(
                IngredientSpec(
                    kind="fluid",
                    id=fluid["fluid"],
                    amount=float(fluid.get("amount", 0)),
                    display=format_resource("fluid", fluid["fluid"], float(fluid.get("amount", 0))),
                )
            )
        if item.get("item"):
            recipe.ingredients.append(
                IngredientSpec(
                    kind="item",
                    id=item["item"],
                    amount=1.0,
                    display=format_resource("item", item["item"], 1),
                )
            )
        recipe.outputs.append(
            OutputSpec(
                kind="coating",
                id=coating,
                amount=1.0,
                is_primary=True,
                display=f"Apply {coating} coating to tool",
            )
        )

    def _parse_mechanical_crafting(self, recipe: ParsedRecipe, data: dict[str, Any]) -> None:
        key = data.get("key", {})
        pattern = data.get("pattern", [])
        counts: dict[str, int] = defaultdict(int)
        for row in pattern:
            for symbol in row.replace(" ", ""):
                counts[symbol] += 1

        for symbol, count in sorted(counts.items()):
            entry = key.get(symbol)
            if not entry:
                continue
            ing = self._parse_item_ingredient(entry, float(count))
            if ing:
                recipe.ingredients.append(ing)

        result = data.get("result", {})
        result_id = result.get("id") or result.get("item")
        if result_id:
            recipe.outputs.append(
                OutputSpec(
                    kind="item",
                    id=result_id,
                    amount=float(result.get("count", result.get("amount", 1))),
                    is_primary=True,
                    display=format_resource("item", result_id, float(result.get("count", 1))),
                )
            )

    def _parse_standard(self, recipe: ParsedRecipe, data: dict[str, Any]) -> None:
        for entry in data.get("ingredients", []):
            ing = self._parse_ingredient(entry)
            if ing:
                recipe.ingredients.append(ing)

        results = data.get("results", [])
        if not results:
            return

        primary = results[0]
        for entry in results:
            out = self._parse_output(entry, is_primary=entry is primary)
            if out:
                recipe.outputs.append(out)

    def _parse_ingredient(self, entry: dict[str, Any]) -> IngredientSpec | None:
        if entry.get("type") == "neoforge:single" and entry.get("fluid"):
            amount = float(entry.get("amount", 0))
            fluid_id = entry["fluid"]
            return IngredientSpec(
                kind="fluid",
                id=fluid_id,
                amount=amount,
                display=format_resource("fluid", fluid_id, amount),
            )

        if entry.get("item"):
            return IngredientSpec(
                kind="item",
                id=entry["item"],
                amount=1.0,
                display=format_resource("item", entry["item"], 1),
            )

        if entry.get("tag"):
            tag = entry["tag"]
            return IngredientSpec(
                kind="item",
                id=f"#{tag}",
                amount=1.0,
                display=f"[tag: {tag}]",
                is_tag=True,
            )

        return None

    def _parse_item_ingredient(self, entry: dict[str, Any], amount: float) -> IngredientSpec | None:
        if entry.get("item"):
            return IngredientSpec(
                kind="item",
                id=entry["item"],
                amount=amount,
                display=format_resource("item", entry["item"], amount),
            )
        if entry.get("tag"):
            tag = entry["tag"]
            return IngredientSpec(
                kind="item",
                id=f"#{tag}",
                amount=amount,
                display=f"[tag: {tag}] × {amount:g}",
                is_tag=True,
            )
        return None

    def _parse_output(self, entry: dict[str, Any], is_primary: bool) -> OutputSpec | None:
        resource_id = entry.get("id") or entry.get("item")
        if not resource_id:
            return None

        chance = float(entry.get("chance", 1.0))
        if "amount" in entry and "count" not in entry:
            amount = float(entry["amount"])
            kind = "fluid"
        else:
            amount = float(entry.get("count", entry.get("amount", 1)))
            kind = "item"

        return OutputSpec(
            kind=kind,
            id=resource_id,
            amount=amount,
            chance=chance,
            is_primary=is_primary,
            display=format_resource(kind, resource_id, amount, chance),
        )

    def infer_kind(self, resource_id: str) -> str:
        rid = normalise_id(resource_id)
        if rid in self.fluid_ids:
            return "fluid"
        if rid in self.item_ids:
            return "item"
        # Heuristic: mod fluids are usually under mod namespace and not ending with common item suffixes
        if rid.startswith(f"{MOD_NAMESPACE}:") and any(
            token in rid for token in ("molten_", "purified_", "catalysed_", "liquid_", "paint", "glue", "carborax", "substrate", "alloy")
        ):
            return "fluid"
        return "item"

    def producers_for(self, resource: ResourceRef) -> list[ParsedRecipe]:
        return list(self.by_output.get(resource.key(), []))

    def stats(self) -> dict[str, int]:
        by_type: dict[str, int] = defaultdict(int)
        for recipe in self.recipes:
            by_type[recipe.type] += 1
        return {
            "total_recipes": len(self.recipes),
            "fluid_ids": len(self.fluid_ids),
            "item_ids": len(self.item_ids),
            **{f"type_{k.replace(':', '_')}": v for k, v in by_type.items()},
        }


def normalise_id(raw: str, default_namespace: str = MOD_NAMESPACE) -> str:
    raw = raw.strip()
    if ":" in raw:
        return raw
    return f"{default_namespace}:{raw}"
