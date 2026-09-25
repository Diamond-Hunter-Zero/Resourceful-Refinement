---
title: Forge Mould
category: Machine
status: Implemented
introduced: v0.1
recipe_type: resourceful_refinement:mechanical_forge_mould
related:
  - "[[Coating]]"
  - "[[Casting Depot]]"
  - "[[Fluid Processing Recipes]]"
  - "[[Coating Variants]]"
tags:
  - machine
  - kinetic
  - coating
---

The Forge Mould (Mechanical Forge Mould) is a rotation-powered mechanical block that turns a fluid input and/or an item input into an item output on the working surface beneath it, behaving like a Spout combined with a Mechanical Press. It also hosts the mod's [[Coating]] recipes, and unlocks a distinct "casting" recipe mode when placed above a [[Casting Depot]].

**ID:** `mechanical_forge_mould`

## Gameplay Role

The Forge Mould is the casting/press step of the refinement loop: it takes molten/processed fluids (optionally combined with a mould or ingredient item held in the machine) and stamps out finished items onto a belt or depot below. With a [[Casting Depot]] underneath it, it runs fluid-only "casting" recipes that deposit their output directly onto the depot, and with a valid tool sitting on that depot it instead applies a [[Coating]] to the tool.

## Construction & Placement

The Forge Mould is a single block. Like a Mechanical Press, it must be placed with a **one-block gap above a working surface** in order to craft — the machine occupies one block, the block directly below it must be air, and the working surface (belt, depot, or [[Casting Depot]]) sits two blocks below the machine.

It allows pipe connection on its top face for fluid input and an item interface connection on its front face for item input. It accepts rotational input from shafts on its left or right sides, and acts as a shaft transferring rotation along that axis.

> [!note] Implementation
> The workspace is read at `worldPosition.below(2)` and the intervening block at `below(1)` must be air. The rotation axis is `FACING.getClockWise().getAxis()` — i.e. the horizontal axis perpendicular to the facing direction — so shafts connect on the left/right faces (`hasShaftTowards` matches that axis).

## Inputs & Outputs

The Forge Mould is a horizontally-rotatable block (furnace-like). It always accepts fluid input from the **top** face and item input through its **front** face. Rotational input is provided to the left or right face by a shaft.

- **Input tank:** 2000 mB (`TANK_CAPACITY`).
- **Input inventory:** 1 slot (mould / ingredient item; optional depending on recipe).
- **Output:** the produced item is inserted into the item handler of the block two positions below (belt, depot, or Casting Depot). There is no fluid output.

A Create filter slot is positioned on the back face. Right-clicking a Forge Mould with an empty hand extracts any item held in the machine's input inventory.

## Operation

The Forge Mould behaves like a Mechanical Press. It cycles through four states: **IDLE → EXTENDING → IMPACTING → RETRACTING**. Once the arm extends fully (the "impact period"), it waits the recipe's duration before spawning the product, consuming inputs, and retracting.

Unlike the Press or Spout, the Forge Mould works on **empty surfaces** for standard recipes: it will not operate if an item already sits on the surface below, and a standard recipe will fail if an item is inserted underneath during the impact period. If an item intercepts the forge mid-impact, it immediately moves to the retract phase and spawns black smoke particles. (Coating recipes invert this: they *require* a valid item on the depot.)

Increasing the RPM increases the speed of the extension, retraction, and impact periods.

Some recipes require the Forge Mould to be placed above a [[Casting Depot]]. These "casting" recipes are distinct from ones without the casting flag, regardless of inputs/outputs.

> [!note] Implementation
> Casting is **not a separate recipe type** — there is no `casting_mould` recipe type. It is the same `resourceful_refinement:mechanical_forge_mould` recipe type with an optional `"casting": true` field. When true, `MechanicalForgeMouldRecipe.matches` additionally requires `input.hasCastingDepot()`, which is set when the block at `below(2)` is a `CastingDepotBlock`. On each cycle the BE inspects the workspace: if the depot holds an item it first tries a `coating` recipe, otherwise it tries a standard/casting forge recipe (and rejects standard recipes when the surface is occupied). `getProcessingSpeed()` (clamped `abs(speed)/16`, 1–512) drives the impact timer; the BE aborts and retracts if the gap is obstructed, the workspace disappears, or a running casting recipe loses its depot.

### Coating mode

When a valid tool (a `DiggerItem`, `SwordItem`, or `TridentItem`) sits on a [[Casting Depot]] below the mould, the Forge Mould runs a [[Coating]] recipe (`resourceful_refinement:coating`) instead of a casting recipe. This consumes fluid (and optionally an item ingredient), and writes a `coating_data` component onto the tool rather than producing a new item. A coating recipe is rejected if the tool already carries a different coating type, or the same type already at full integrity.

## Recipes

Recipe type: `resourceful_refinement:mechanical_forge_mould`. Recipes are serialisable as JSON for the mod and datapacks.

A Forge Mould recipe takes 1 fluid input and an optional item input, and produces 1 item. Add `"casting": true` to require a [[Casting Depot]] below.

```json
{
    "type": "resourceful_refinement:mechanical_forge_mould",
    "casting": true,
    "processing_time": 200,
    "ingredients": [
        {
            "type": "neoforge:single",
            "fluid": "minecraft:lava",
            "amount": 50
        },
        {
            "item": "minecraft:cobblestone",
            "amount": 2
        }
    ],
    "results": [
        {
            "item": "minecraft:magma_block",
            "count": 5
        }
    ]
}
```

Item ingredients can carry an optional per-item **consumption chance**: an ingredient is only consumed on the roll (moulds, for example, usually survive processing). See the implementation note below.

> [!note] Implementation
> `MechanicalForgeMouldRecipe` extends Create's `StandardProcessingRecipe` (1 fluid in, up to 64 item in, 1 item out, no fluid out) and uses a **custom `Serializer`** that layers a `"casting"` boolean and a list of `ChancedIngredient` onto Create's `ProcessingRecipeParams` codec. `ChancedIngredient(Item, consumptionChance)` pairs an input item with the probability it is consumed (`consumption_chance` in JSON, default `1.0`) — it governs **input consumption**, not weighted output. `getConsumptionChance(item)` is rolled in `process()` to decide whether to extract the ingredient. Constants `INGOT_MOULD_BREAK_CHANCE = 0.25` and `SHAFT_MOULD_BREAK_CHANCE = 0.01` exist on the BE for mould-breakage tuning. See [[Fluid Processing Recipes]] for the concrete casting table (e.g. `ingot_mould` + 250 mB molten metal → 1 ingot).

## Rendering

The Forge Mould uses three conceptual model classes:

- **`ForgeMouldCasingModel`** — the block's main casing model.
- **`ForgeMouldPressModel`** — the base of the forge's extendable arm. Its base is level with the surface of the belt/depot/worktop when crafting, and level with the bottom of the forge block when retracted.
- **`ForgeMouldTubeModel`** — the repeatable connecting segment of the extendable arm, stacked from the top of the press until the top-most tube is at least 4 pixels above the bottom of the forge block.

The forge animates like a Mechanical Press: idle and retracted inside the casing when stationary; on processing, the press and its tubes lower (proportional to processing speed), pause on the working surface, then retract once the item spawns. On impact it spawns white smoke particles; an aborted/obstructed cycle spawns black/grey smoke.

![[Forge Mould Render.png|253]]

> [!note] Implementation
> Model classes carry a `Model` suffix in code (`ForgeMouldCasingModel`, `ForgeMouldPressModel`, `ForgeMouldTubeModel`). Rendering is handled by `ForgeMouldRenderer` with layers in `ForgeMouldLayers`; the block item uses `ForgeMouldItemRenderer`. `getRenderShape` is `INVISIBLE`. The render bounding box is expanded 2 blocks downward to cover the extended arm. The BE exposes `TriggerFalseAnimation()` / `IsRunningFalseAnimation()` hooks used by Ponder scenes. Impact spawns `CAMPFIRE_COSY_SMOKE`, a successful process spawns `CLOUD`, and aborts spawn `SMOKE`.

## Implementation

- **Package:** `content/forge_mould/`
- **Block:** `MechanicalForgeMouldBlock` (`KineticBlock`, `IBE<MechanicalForgeMouldBlockEntity>`) — id `resourceful_refinement:mechanical_forge_mould`.
- **Block entity:** `MechanicalForgeMouldBlockEntity` (`KineticBlockEntity`) — `ModBlockEntities.MECHANICAL_FORGE_MOULD_BE`. Holds `inputTank` (2000 mB), a 1-slot `inputInv`, and the `RunningState` machine.
- **Recipes:**
  - `MechanicalForgeMouldRecipe` (extends `StandardProcessingRecipe`) with custom `Serializer`, plus `ChancedIngredient`. Type/serializer `resourceful_refinement:mechanical_forge_mould`.
  - `CoatingRecipe` (implements `Recipe`) with its own `Serializer` — type `resourceful_refinement:coating`. Also lives in this package. Writes the `coating_data` component (see [[Coating]]).
  - Shared input record `MechanicalForgeMouldRecipeInput(item, fluid, hasCastingDepot, depotItem)`.
  - JEI category `MechanicalForgeMouldRecipeCategory`.
- **Filtering:** Create `FilteringBehaviour` (`ForgeMouldValueBox`, back face), `.forRecipes()`.
- **Stress:** `ModStressValues.FORGE_STRESS = 8` su per RPM (impact registered on `ModBlocks.MECHANICAL_FORGE_MOULD`).
- **Rendering:** `ForgeMouldRenderer`, `ForgeMouldLayers`, `ForgeMouldCasingModel`, `ForgeMouldPressModel`, `ForgeMouldTubeModel`, `ForgeMouldItemRenderer`.

## Related

- [[Coating]]
- [[Casting Depot]]
- [[Coating Variants]]
- [[Fluid Processing Recipes]]
