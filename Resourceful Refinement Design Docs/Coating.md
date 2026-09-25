---
title: Coating
category: Framework
status: Implemented
introduced: v0.2
recipe_type: resourceful_refinement:coating
related:
  - "[[Coating Variants]]"
  - "[[Forge Mould]]"
tags:
  - framework
  - tool
  - data-component
---

Coating is a data-component system in which a fluid and an item ingredient are applied onto a tool item, giving it a secondary durability bar and a special temporary effect. Coating is performed by the Mechanical [[Forge Mould]] with a [[Casting Depot]] beneath it holding the tool, and is a recipe type (`resourceful_refinement:coating`) distinct from `mechanical_forge_mould` recipes. It is not a block — a coated tool simply carries the `coating_data` component.

## Concepts

A coated tool has a secondary durability bar, called its 'Coating Integrity'. While a tool has any remaining coating integrity:

- Any damage that would be applied to the tool is instead applied to the coating integrity.
- The user of the tool benefits from a 'coating effect' while holding it. This might be a status effect, an attribute modifier on the tool itself, or a logic method which runs when the tool is used.
- The coating integrity is rendered as a second durability bar above the first. Its colour corresponds to the coating type defined in the recipe.

Once a tool's coating is depleted, the coating and its effects are entirely removed from the tool, and the tool returns to regular behaviour.

New coatings cannot be applied to a tool if it already has a coating, unless the coating type matches the type produced by the current recipe. In that case, the coating integrity is simply reset to its full amount.

### Application

To coat an item, the user places any tool onto a [[Casting Depot]] below a Mechanical [[Forge Mould]]. The forge is then provided with an amount of fluid (specified by the recipe) and a secondary ingredient item. If a valid recipe is found, the forge performs its processing animation and 'coats' the tool. In code the coatable tools are `DiggerItem`, `SwordItem`, and `TridentItem`.

## Key Types

- **`CoatingType`** (enum, `content/coating/CoatingType.java`) — defines each coating variant with a display name, associated colour, maximum durability, and a short effect description. Serialised via `StringRepresentable` `CODEC`. See [[Coating Variants]] for the full list.
- **`CoatingData`** (record, `content/coating/CoatingData.java`) — `(CoatingType type, int integrity)`. Provides a `CODEC` (persistent) and a `STREAM_CODEC` (network). Stored on the tool as the `coating_data` data component.
- **`CoatingItemDecorator`** (`content/coating/CoatingItemDecorator.java`) — an `IItemDecorator` that draws the secondary durability bar (coating integrity) above the vanilla durability bar, tinted with the coating type's colour. Returns `false` so vanilla decorators still run; the bar shifts up two pixels when the tool itself is also damaged.
- **`CoatingRecipe`** (`content/forge_mould/recipe/CoatingRecipe.java`) — the recipe implementation (see Recipes below).
- **`CoatingRecipeCategory`** (`content/coating/CoatingRecipeCategory.java`) — JEI category for displaying coating recipes.

## How Features Use It

- **[[Forge Mould]] / [[Casting Depot]]** — the coating process itself: the Casting Depot holds the tool while the Mechanical Forge Mould supplies fluid and ingredient and runs the recipe.
- **Tools/weapons/armour** — any coatable tool item carries the `coating_data` component once coated; effects apply while the tool is held/used.
- **JEI** — coating recipes surface through `CoatingRecipeCategory`.

## Recipes

Recipe type id: `resourceful_refinement:coating` (serializer `resourceful_refinement:coating`).

A coating recipe defines:

- A **fluid input** (`fluid`, a `SizedFluidIngredient`).
- An **item input** (`ingredient`, a non-empty `Ingredient`).
- The **coating type** produced (`coating`, a `CoatingType` enum value).
- A **duration** (`duration`, optional, default `100` ticks).
- Optional **chanced ingredients** — entries within the `ingredients` array that carry a per-item consumption chance (`ChancedIngredient`); fluid entries in that array are safely skipped by the serializer.

`assemble` copies the depot tool, sets its count to 1, and applies `new CoatingData(coatingType, coatingType.getMaxDurability())` (full integrity). `matches` requires a Casting Depot, a matching fluid and item, a coatable tool on the depot, and either no existing coating or a same-type coating that is not already at full integrity.

Each `CoatingType` in turn defines a name, an associated colour, and a maximum durability — see [[Coating Variants]].

## Implementation

- Package: `content/coating/` (`CoatingType`, `CoatingData`, `CoatingItemDecorator`, `CoatingRecipeCategory`); recipe under `content/forge_mould/recipe/CoatingRecipe.java`.
- Data component: `coating_data`, registered in `registry/ModDataComponents` (`COATING_DATA`, persistent `CODEC` + networked `STREAM_CODEC`).
- Recipe registration: `registry/ModRecipeTypes` — `COATING_TYPE`, `COATING_SERIALIZER`, `COATING_TYPE_INFO`.
- Effects: `registry/ModToolEvents` — subscribes to attribute, player-tick, block-break, and attack events to apply per-type behaviour (armour, knockback resistance, Haste II, Slow Fall, Conduction lightning, Gloopy gel splatter).
- Damage redirection: `mixin/ItemStackMixin` intercepts the vanilla `DAMAGE` component set. While a coating is present, incoming damage is subtracted from coating integrity; if integrity stays above zero the base damage is cancelled, otherwise the `coating_data` component is removed and the damage proceeds normally to the tool.

> [!note] Implementation
> `CoatingType` in code defines an additional **Gloopy** coating and a longer Obsidianite durability (160, not 128) than earlier design notes; `LUMINITE` and `OBSIDIAN` are commented out. The `LIQUIDLUCK` ("Raises Fortune level") effect has no handler in `ModToolEvents` yet. See [[Coating Variants]] for the authoritative per-variant table and status.

## Related

- [[Coating Variants]]
- [[Forge Mould]]
- [[Casting Depot]]
