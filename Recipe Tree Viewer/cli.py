from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

from recipe_tree import RecipeIndex, build_trees, parse_recipe_selections
from recipe_tree.models import format_resource


DEFAULT_RECIPE_ROOT = (
    Path(__file__).resolve().parent.parent
    / "src"
    / "main"
    / "resources"
    / "data"
    / "resourceful_refinement"
    / "recipe"
)


def render_text(node: dict, indent: int = 0) -> list[str]:
    lines: list[str] = []
    pad = "  " * indent
    resource = node["resource"]
    kind = resource["kind"]
    amount = resource["amount"]
    rid = resource["id"]
    label = format_resource(kind, rid, amount)

    if node["node_type"] == "recipe" and node.get("recipe"):
        recipe = node["recipe"]
        scale_pct = node.get("scale_factor", 1) * 100
        meta = [
            recipe["machine"],
            f"scale {scale_pct:.2f}% of one craft ({node.get('recipe_yield', '?')} base yield)",
            f"{node['total_duration_seconds']}s proportional",
        ]
        if node.get("operational_batches"):
            excess = node.get("operational_excess", 0)
            meta.append(f"{node['operational_batches']} whole batch(es) -> {excess:g} excess primary")
        if recipe.get("heat_requirement") and recipe["heat_requirement"] != "none":
            meta.append(f"heat: {recipe['heat_requirement']}")
        if recipe.get("casting"):
            meta.append("requires Casting Depot")
        lines.append(f"{pad}-> {label}")
        lines.append(f"{pad}  via {recipe['id']} ({', '.join(meta)})")
        if node.get("alternatives") and len(node["alternatives"]) > 1:
            alt_ids = ", ".join(a["recipe_id"] for a in node["alternatives"])
            lines.append(f"{pad}  alternatives: {alt_ids}")
        for bp in node.get("byproducts", []):
            lines.append(f"{pad}  byproduct: {bp['display']}")
    elif node["node_type"] == "external":
        lines.append(f"{pad}* {label} [external/raw]")
    elif node["node_type"] == "provided":
        lines.append(f"{pad}+ {label} [provided input]")
    elif node["node_type"] == "shared_ref":
        total = node.get("shared_total_amount", node["resource"]["amount"])
        lines.append(f"{pad}~ {label} [shared ref -> {total:g} total production]")
    elif node["node_type"] == "world_source":
        lines.append(f"{pad}* {node.get('note', label)}")
    elif node["node_type"] == "cycle":
        lines.append(f"{pad}! {label} [cycle stopped]")
    else:
        lines.append(f"{pad}? {label} [{node['node_type']}]")

    if node.get("note") and node["node_type"] == "recipe":
        lines.append(f"{pad}  note: {node['note']}")

    for child in node.get("inputs", []):
        lines.extend(render_text(child, indent + 2))

    return lines


def render_summary(summary: dict) -> list[str]:
    lines: list[str] = []
    target = summary.get("target")
    if target:
        lines.append(f"Target product: {format_resource(target['kind'], target['id'], target['amount'])}")

    lines.append("")
    lines.append("Cumulative inputs (scaled):")
    inputs = summary.get("inputs", [])
    if not inputs:
        lines.append("  (none)")
    for entry in inputs:
        label = {
            "external": "raw/external",
            "provided": "provided",
            "world_source": "world source",
        }.get(entry["category"], entry["category"])
        lines.append(f"  - {entry['display']} [{label}]")

    lines.append("")
    lines.append("Cumulative outputs (scaled byproducts & batch excess):")
    outputs = summary.get("outputs", [])
    if not outputs:
        lines.append("  (none)")
    for entry in outputs:
        label = "byproduct" if entry["category"] == "byproduct" else "batch excess"
        lines.append(f"  - {entry['display']} [{label}]")

    return lines


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(
        description="Build dependency trees for Resourceful Refinement processing recipes.",
    )
    parser.add_argument("target", help="Item or fluid id, e.g. resourceful_refinement:purified_iron")
    parser.add_argument("amount", type=float, nargs="?", default=1.0, help="Target amount (items or mB)")
    parser.add_argument("--kind", choices=["item", "fluid", "coating"], help="Force resource kind")
    parser.add_argument("--recipe-root", type=Path, default=DEFAULT_RECIPE_ROOT, help="Path to recipe JSON folder")
    parser.add_argument("--json", action="store_true", help="Emit JSON instead of text")
    parser.add_argument("--max-depth", type=int, default=32)
    parser.add_argument(
        "--provided",
        action="append",
        default=[],
        metavar="ID",
        help="Item/fluid id assumed in unlimited supply (repeatable, or comma-separated)",
    )
    parser.add_argument(
        "--select",
        action="append",
        default=[],
        metavar="PATH:RECIPE",
        help="Recipe choice at a tree path, e.g. root.c2:lava_sieving (repeatable)",
    )
    parser.add_argument(
        "--simplify",
        action="store_true",
        help="Merge identical producible ingredients into shared production nodes",
    )
    args = parser.parse_args(argv)

    provided: list[str] = []
    for entry in args.provided:
        provided.extend(part.strip() for part in entry.split(",") if part.strip())

    recipe_selections = parse_recipe_selections(args.select)

    if not args.recipe_root.is_dir():
        print(f"Recipe root not found: {args.recipe_root}", file=sys.stderr)
        return 1

    index = RecipeIndex.load(args.recipe_root)
    result = build_trees(
        index,
        args.target,
        args.amount,
        kind=args.kind,
        max_depth=args.max_depth,
        provided_inputs=provided,
        recipe_selections=recipe_selections,
        simplify=args.simplify,
    )

    if args.json:
        print(json.dumps(result, indent=2))
        return 0

    print(f"Target: {result['target']['kind']}:{result['target']['id']} x {result['target']['amount']}")
    print(f"Indexed {index.stats()['total_recipes']} processing recipes")
    if result.get("provided_inputs"):
        print(f"Provided inputs: {', '.join(result['provided_inputs'])}")
    if result.get("simplify"):
        print("Simplify mode: enabled")
    print()

    if not result["paths"]:
        print(result.get("message", "No paths found."))
        return 1

    print(f"Total proportional time: {result['paths'][0]['total_duration_seconds']}s")
    print()

    for i, path in enumerate(result["paths"], start=1):
        if result["paths_found"] == 1:
            print("=== Recipe tree ===")
        else:
            print(f"=== Path {i} / {result['paths_found']} ===")
        for line in render_text(path):
            print(line)
        if result.get("summary"):
            print()
            for line in render_summary(result["summary"]):
                print(line)
        if result.get("shared_nodes"):
            print()
            print("=== Shared production nodes ===")
            for key, shared in result["shared_nodes"].items():
                print(f"[{key}]")
                for line in render_text(shared, indent=1):
                    print(line)
        print()

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
