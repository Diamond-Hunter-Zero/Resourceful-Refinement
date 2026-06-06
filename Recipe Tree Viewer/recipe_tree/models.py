from __future__ import annotations

from dataclasses import dataclass, field
from typing import Any


MOD_NAMESPACE = "resourceful_refinement"

PROCESSING_RECIPE_TYPES = {
    "resourceful_refinement:fracking_pump",
    "resourceful_refinement:fluid_refinery",
    "resourceful_refinement:mechanical_fluid_sieve",
    "resourceful_refinement:mechanical_forge_mould",
    "resourceful_refinement:coating",
    "create:mixing",
    "create:mechanical_crafting",
}

MACHINE_LABELS = {
    "resourceful_refinement:fracking_pump": "Fracking Pump",
    "resourceful_refinement:fluid_refinery": "Fluid Refinery",
    "resourceful_refinement:mechanical_fluid_sieve": "Mechanical Fluid Sieve",
    "resourceful_refinement:mechanical_forge_mould": "Mechanical Forge Mould",
    "resourceful_refinement:coating": "Mechanical Forge Mould (Coating)",
    "create:mixing": "Mixing Basin",
    "create:mechanical_crafting": "Mechanical Crafter",
}


@dataclass(frozen=True)
class ResourceRef:
    kind: str  # "item" | "fluid" | "block" | "coating"
    id: str

    def key(self) -> str:
        return f"{self.kind}:{self.id}"


@dataclass
class IngredientSpec:
    kind: str
    id: str
    amount: float
    display: str
    is_tag: bool = False


@dataclass
class OutputSpec:
    kind: str
    id: str
    amount: float
    chance: float = 1.0
    is_primary: bool = False
    display: str = ""

    def key(self) -> str:
        return f"{self.kind}:{self.id}"


@dataclass
class ParsedRecipe:
    recipe_id: str
    path: str
    type: str
    machine: str
    duration_ticks: int
    heat_requirement: str | None = None
    casting: bool = False
    source_block: str | None = None
    source_fluid: str | None = None
    coating_name: str | None = None
    ingredients: list[IngredientSpec] = field(default_factory=list)
    outputs: list[OutputSpec] = field(default_factory=list)
    raw: dict[str, Any] = field(default_factory=dict)

    def primary_outputs(self) -> list[OutputSpec]:
        primaries = [o for o in self.outputs if o.is_primary]
        return primaries if primaries else self.outputs


@dataclass
class TreeNode:
    resource: ResourceRef
    needed_amount: float
    node_type: str  # "recipe" | "external" | "provided" | "world_source" | "cycle" | "unresolved" | "shared_ref"
    recipe: ParsedRecipe | None = None
    scale_factor: float = 1.0
    recipe_yield: float = 0.0
    operational_batches: int = 0
    operational_excess: float = 0.0
    total_duration_ticks: float = 0.0
    selection_path: str = ""
    selected_recipe_id: str = ""
    alternatives: list[dict[str, Any]] = field(default_factory=list)
    ref_key: str = ""
    shared_total_amount: float = 0.0
    note: str = ""
    inputs: list[TreeNode] = field(default_factory=list)
    byproducts: list[OutputSpec] = field(default_factory=list)

    def to_dict(self) -> dict[str, Any]:
        payload: dict[str, Any] = {
            "resource": {
                "kind": self.resource.kind,
                "id": self.resource.id,
                "amount": round(self.needed_amount, 3),
            },
            "node_type": self.node_type,
            "scale_factor": round(self.scale_factor, 6),
            "recipe_yield": round(self.recipe_yield, 3),
            "operational_batches": self.operational_batches,
            "operational_excess": round(self.operational_excess, 3),
            "total_duration_ticks": round(self.total_duration_ticks, 3),
            "total_duration_seconds": round(self.total_duration_ticks / 20, 2),
        }
        if self.note:
            payload["note"] = self.note
        if self.ref_key:
            payload["ref_key"] = self.ref_key
        if self.shared_total_amount > 0:
            payload["shared_total_amount"] = round(self.shared_total_amount, 3)
        if self.selection_path:
            payload["selection_path"] = self.selection_path
        if self.selected_recipe_id:
            payload["selected_recipe_id"] = self.selected_recipe_id
        if self.alternatives:
            payload["alternatives"] = self.alternatives
        if self.recipe:
            payload["recipe"] = {
                "id": self.recipe.recipe_id,
                "path": self.recipe.path,
                "type": self.recipe.type,
                "machine": self.recipe.machine,
                "duration_ticks": self.recipe.duration_ticks,
                "heat_requirement": self.recipe.heat_requirement,
                "casting": self.recipe.casting,
                "source_block": self.recipe.source_block,
                "source_fluid": self.recipe.source_fluid,
                "coating_name": self.recipe.coating_name,
            }
        if self.byproducts:
            payload["byproducts"] = [
                {
                    "kind": b.kind,
                    "id": b.id,
                    "amount": b.amount,
                    "chance": b.chance,
                    "display": b.display or format_resource(b.kind, b.id, b.amount, b.chance),
                }
                for b in self.byproducts
            ]
        if self.node_type != "shared_ref":
            payload["inputs"] = [child.to_dict() for child in self.inputs]
        return payload


def normalise_id(raw: str, default_namespace: str = MOD_NAMESPACE) -> str:
    raw = raw.strip()
    if ":" in raw:
        return raw
    return f"{default_namespace}:{raw}"


def format_resource(kind: str, resource_id: str, amount: float, chance: float = 1.0) -> str:
    short = resource_id.split(":")[-1].replace("_", " ")
    if kind == "fluid":
        base = f"{amount:g} mB {short}"
    elif kind == "block":
        return f"World source: {resource_id}"
    elif kind == "coating":
        return f"Coating: {resource_id}"
    else:
        base = f"{amount:g}x {short}"
    if 0 < chance < 1:
        return f"{base} ({chance * 100:g}% chance byproduct)"
    return base
