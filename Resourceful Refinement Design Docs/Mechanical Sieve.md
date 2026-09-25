---
title: Mechanical Sieve
category: Machine
status: Implemented
introduced: v0.1
recipe_type: resourceful_refinement:mechanical_fluid_sieve
related:
  - "[[Fluid Refinery]]"
  - "[[Fluid Processing Recipes]]"
  - "[[Forge Mould]]"
tags:
  - machine
  - multiblock
  - kinetic
---

The Mechanical Sieve is a rotation-powered block that processes an input fluid and produces an output fluid and/or a chance-based item by-product. Sieves can be stacked vertically into a single multiblock column that shares one inventory and processes faster and with more by-product rolls the taller it grows.

**ID:** `mechanical_sieve`

## Gameplay Role

The Mechanical Sieve sits in the refinement loop as the machine that separates a processed fluid into a purer/alloyed output fluid plus solid by-products (e.g. filtering molten ores into a refined fluid while dropping stone or crystal by-products). Stacking sieves lets a player trade vertical space and kinetic input for throughput and better by-product yield. It pairs naturally with the [[Fluid Refinery]] upstream and feeds cast/forge steps downstream (see [[Forge Mould]] and [[Fluid Processing Recipes]]).

## Construction & Placement

The Mechanical Sieve is a single block that can be stacked vertically to form a **sieve stack**. It allows pipe connection on its top and bottom faces for fluid I/O, and an item interface connection on its front face for by-product extraction.

### Multiblocking

When multiple Mechanical Sieves are placed vertically on top of each other, they form a sieve stack. Sieves check for stack construction whenever one is placed, or a vertically neighbouring sieve is placed/removed. Sieve stacks form from the bottom up, with the **bottom block acting as the controller**.

Sieves in a stack act as a single multiblock structure, with only the controller (bottom block) using its inventory. When a sieve is absorbed into a stack, any fluids in its own tanks are cleared and any stored items are dropped into the world. The stack accepts input fluid from the top of the column and pushes outputs through its bottom face (fluid) and bottom-front face (items).

Any sieve in the stack can receive rotational input to power the whole stack, and sieves transfer rotation between each other and to adjacent blocks, so the column behaves as a single run of cogs.

When part of a stack, each sieve's casing model switches to a bottom / middle / top variant according to its position, and every block is rotated to face the same direction as the controller.

> [!note] Implementation
> Stack height is capped at **4** (`MAX_STACK_HEIGHT = 4`), hardcoded rather than configurable. A physical column taller than 4 is split into consecutive segments of up to 4 sieves, each with its own controller at the bottom of the segment (`updateConnectivity` → `applyStackSegment`). Each block tracks `stackSize`, `stackIndex` and a `controllerPos`; the controller is the block whose `controllerPos` equals its own position. Rotation and connectivity are recomputed on place / remove / neighbour change.
> 
> TODO: Expose this as a config parameter, caped between 2 and 8 maybe?

## Inputs & Outputs

The Mechanical Sieve is a horizontally-rotatable block (furnace-like). It always accepts fluid input from the **top** of the stack and pushes output fluid through its **bottom** face. Item by-products can only be extracted from the block's **front** face; items cannot be inserted into the block. Rotational input is provided from the side faces (not the front).

- **Input tank:** 4000 mB (`TANK_CAPACITY`).
- **Output tank:** 4000 mB.
- **Output inventory:** 1 slot, for the by-product item.

Right-clicking a Mechanical Sieve with an empty hand extracts any items held in the controller's output inventory back into the player.

> [!note] Implementation
> The block is a Create `ICogWheel` whose rotation axis is **vertical** (`getRotationAxis` → `Axis.Y`), so the column meshes with adjacent cogs on its horizontal side faces and can accept a vertical shaft on its top/bottom faces (`hasShaftTowards` is true for the Y axis). Both tanks call `syncData()` on change so the client renders fluid contents. The empty-hand extraction reads from `controller.outputInv`.

## Operation

The sieve requires rotational input to process (it does nothing at zero speed). Processing time is driven by the recipe's duration scaled by stack size, and by the current RPM.

A sieve stack behaves like a single sieve, with two exceptions:

- **Duration** is increased by **25% for each additional sieve** in the stack (`duration × (1 + 0.25 × (stackSize − 1))`).
- The **by-product roll repeats once per sieve** in the stack, but stops as soon as a roll succeeds. (A recipe with a 10% chance to drop 5 stone, run in a stack of 4, rolls up to 4 times and produces 5 stone the first time a roll succeeds.)

A recipe may not commence if the output tank lacks room for the next fluid production. If the output item slot has insufficient room for a by-product, the recipe still progresses and the excess items are discarded.

> [!note] Implementation
> Recipe matching runs on the controller but uses the **top block's** filter (`getTopBlock().getFiltering()`). The controller ticks the `timer` down by `getProcessingSpeed()` (clamped `abs(speed)/16`, 1–512) each tick, then calls `process()`. `canProcess()` guards on the fluid ingredient and output-tank space. `hasIncompatibleOutput()` detects a stuck sieve holding an output that no matching recipe can produce and surfaces a "Blockage" goggle warning.

## Recipes

Recipe type: `resourceful_refinement:mechanical_fluid_sieve`. Recipes are serialisable as JSON for the mod and datapacks.

A Mechanical Fluid Sieve recipe takes 1 fluid input and produces an optional 1 fluid output plus a percentile chance of 1 item by-product. It requires rotational input and may specify a `processing_time` (duration). A recipe will not begin if the output tank has insufficient room for the produced fluid.

```json
{
    "type": "resourceful_refinement:mechanical_fluid_sieve",
    "processing_time": 200,
    "ingredients": [
        {
            "type": "neoforge:single",
            "fluid": "minecraft:lava",
            "amount": 250
        }
    ],
    "results": [
        {
            "id": "resourceful_refinement:molten_crimsite",
            "amount": 500
        },
        {
            "chance": 0.25,
            "item": "minecraft:stone",
            "amount": 2
        }
    ]
}
```

> [!note] Implementation
> `MechanicalSieveRecipe` extends Create's `StandardProcessingRecipe` and is registered with Create's `StandardProcessingRecipe.Serializer` (`new StandardProcessingRecipe.Serializer<>(MechanicalSieveRecipe::new)`). It caps at 0 item inputs, 1 item output, 1 fluid input, 1 fluid output, and allows an explicit duration. Matching tests the single fluid ingredient against the input tank; `matchesFilter` checks the recipe's fluid and item outputs against the top block's Create filter.

## Rendering

The Mechanical Sieve uses two conceptual model classes:

- **`MechanicalSieveCasingModel`** — the block's main casing model. When part of a stack, the casing switches to position-specific variants (`MechanicalSieveCasingBottomModel`, `MechanicalSieveCasingMiddleModel`, `MechanicalSieveCasingTopModel`).
- **`MechanicalSieveCogModel`** — the rotational components. Three cog instances are stacked vertically, 2 pixels up from the base of the block. The middle cog is animated as the connecting cog in the rotational network; the other two rotate at half speed and in the opposite direction to the middle cog.

Fluid processing spawns radial `FLUID_PARTICLE` effects at the top of the stack, skipping a ~90° arc toward the front face so particles don't jump out of the item-output side.

> [!note] Implementation
> Rendering is handled by `MechanicalSieveRenderer` with layer registration in `MechanicalSieveLayers`; the block item uses `MechanicalSieveItem` / `MechanicalSieveItemRenderer`. `getRenderShape` is `INVISIBLE` (fully BER-driven). The Create filter value box is positioned on the **top face** of the top block of the stack (near the front edge), and is only shown on the top block (`stackIndex == stackSize − 1`).

## Implementation

- **Package:** `content/sieve/`
- **Block:** `MechanicalFluidSieveBlock` (`KineticBlock`, `ICogWheel`, `IBE<MechanicalFluidSieveBlockEntity>`) — id `resourceful_refinement:mechanical_sieve`.
- **Block entity:** `MechanicalFluidSieveBlockEntity` (`KineticBlockEntity`) — `ModBlockEntities.MECHANICAL_SIEVE_BE`. Holds `inputTank`/`outputTank` (4000 mB each), a 1-slot `outputInv`, and multiblock fields `stackSize` / `stackIndex` / `controllerPos`.
- **Recipe:** `MechanicalSieveRecipe` (extends `StandardProcessingRecipe`), input `MechanicalSieveRecipeInput`, JEI category `MechanicalSieveRecipeCategory`. Type/serializer `resourceful_refinement:mechanical_fluid_sieve` in `ModRecipeTypes`.
- **Filtering:** Create `FilteringBehaviour` (`SieveFilterValueBox`), `.forRecipes()`.
- **Stress:** `ModStressValues.SIEVE_STRESS = 4` su per RPM (impact registered on `ModBlocks.MECHANICAL_SIEVE`).
- **Rendering:** `MechanicalSieveRenderer`, `MechanicalSieveLayers`, casing models (base/bottom/middle/top), `MechanicalSieveCogModel`, `MechanicalSieveItem`, `MechanicalSieveItemRenderer`.

## Related

- [[Fluid Refinery]]
- [[Fluid Processing Recipes]]
- [[Forge Mould]]
