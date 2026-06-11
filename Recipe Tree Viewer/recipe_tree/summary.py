from __future__ import annotations

from collections import defaultdict
from typing import Any

from .models import format_resource


def summarize_tree(tree: dict[str, Any] | None, shared_nodes: dict[str, Any] | None = None) -> dict[str, Any]:
    """Aggregate scaled leaf inputs and byproduct / excess outputs for a built tree."""
    if not tree:
        return {"inputs": [], "outputs": [], "target": None}

    shared_nodes = shared_nodes or {}
    input_totals: dict[tuple[str, str, str], float] = defaultdict(float)
    output_totals: dict[tuple[str, str, str], dict[str, Any]] = {}
    visited_shared: set[str] = set()

    def add_input(kind: str, resource_id: str, amount: float, category: str) -> None:
        if amount <= 0:
            return
        input_totals[(kind, resource_id, category)] += amount

    def add_output(kind: str, resource_id: str, amount: float, category: str, chance: float = 1.0) -> None:
        if amount <= 0:
            return
        key = (kind, resource_id, category)
        if key not in output_totals:
            output_totals[key] = {"kind": kind, "id": resource_id, "amount": 0.0, "category": category, "chance": chance}
        output_totals[key]["amount"] += amount

    def walk(node: dict[str, Any]) -> None:
        node_type = node.get("node_type")
        resource = node.get("resource", {})

        if node_type == "shared_ref":
            ref_key = node.get("ref_key")
            if not ref_key or ref_key in visited_shared:
                return
            visited_shared.add(ref_key)
            shared = shared_nodes.get(ref_key)
            if shared:
                walk(shared)
            return

        if node_type in ("external", "provided"):
            add_input(resource.get("kind", "item"), resource["id"], float(resource.get("amount", 0)), node_type)
        elif node_type == "world_source":
            add_input("block", resource.get("id", "unknown"), float(resource.get("amount", 1)), "world_source")
        elif node_type == "recipe":
            for bp in node.get("byproducts", []):
                add_output(
                    bp.get("kind", "item"),
                    bp["id"],
                    float(bp.get("amount", 0)),
                    "byproduct",
                    float(bp.get("chance", 1.0)),
                )
            excess = float(node.get("operational_excess", 0))
            if excess > 0:
                add_output(resource.get("kind", "item"), resource["id"], excess, "operational_excess")

        for child in node.get("inputs", []):
            walk(child)

    walk(tree)

    inputs = _format_entries(input_totals)
    outputs = _format_output_entries(output_totals)

    target = tree.get("resource")
    return {
        "target": target,
        "inputs": inputs,
        "outputs": outputs,
    }


def _format_entries(totals: dict[tuple[str, str, str], float]) -> list[dict[str, Any]]:
    entries: list[dict[str, Any]] = []
    for (kind, resource_id, category), amount in sorted(totals.items(), key=lambda item: (item[0][2], item[0][1])):
        rounded = round(amount, 3)
        entries.append(
            {
                "kind": kind,
                "id": resource_id,
                "amount": rounded,
                "category": category,
                "display": format_resource(kind, resource_id, rounded),
            }
        )
    return entries


def _format_output_entries(totals: dict[tuple[str, str, str], dict[str, Any]]) -> list[dict[str, Any]]:
    entries: list[dict[str, Any]] = []
    for (_kind, _id, _cat), data in sorted(totals.items(), key=lambda item: (item[0][2], item[0][1])):
        amount = round(float(data["amount"]), 3)
        chance = float(data.get("chance", 1.0))
        entries.append(
            {
                "kind": data["kind"],
                "id": data["id"],
                "amount": amount,
                "category": data["category"],
                "chance": chance if data["category"] == "byproduct" else 1.0,
                "display": format_resource(data["kind"], data["id"], amount, chance if data["category"] == "byproduct" else 1.0),
            }
        )
    return entries
