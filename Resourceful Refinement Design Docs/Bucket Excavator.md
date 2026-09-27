---
title: Bucket Excavator
category: Machine
status: Implemented
introduced: v0.4
recipe_type: resourceful_refinement:excavation
related:
  - "[[Mineral Deposit]]"
  - "[[Drill Pylon]]"
  - "[[Geyser Block]]"
  - "[[Primary Design Doc]]"
tags:
  - machine
  - kinetic
  - worldgen
---

The Bucket Excavator is a single kinetic block whose protruding bucket-wheel passively generates resources when placed over mineral deposits, or slowly mines blocks when switched to 'destruction' mode.

**ID:** *bucket_excavator*

## Gameplay Role

The Bucket Excavator is the surface-extraction machine of the v0.4 tier: parked over [[Mineral Deposit|Mineral Deposits]] and driven by kinetic input, it passively produces simple resources (stone variants, dyes, and other recipe outputs), or acts as a wide area-miner in destruction mode. It pairs with the [[Drill Pylon]], which handles deeper crystal extraction.

## Construction & Placement

The Bucket Excavator is a single block entity, which can be horizontally rotated (`FACING` = `HORIZONTAL_FACING`, set from the player's placement direction).

Extending beyond the front face of the block (or "mount") is a large polyhedral bucket-wheel, which measures 3 blocks tall, 3 blocks deep, and 1 block wide. The wheel is positioned in front of the mount, its axis of rotation parallel to the mount's shaft axis and at the same y-position, such that the entire visible bounding volume of the bucket excavator appears to be 3x4x1.

![[bucket_wheel_excavator_block_test_render.png|388]]

Bucket Excavators occupy an 'excavation region' encapsulating their wheel. If the excavation-regions of two or more Bucket Excavators overlap, they won't function or produce resources. Bucket Excavators can be tiled laterally.

![[bucket_wheel_excavator_volumes.png|498]]

> [!note] Implementation
> The excavation region is **5 tall × 5 deep × 1 wide** (`EXCAVATION_REGION_HEIGHT = 5`, `EXCAVATION_REGION_DEPTH = 5`, `EXCAVATION_REGION_WIDTH = 1`), projected out from the mount's front face by `RegionExtents.GetFaceExtendedRegion`. The older "5x4x1 excavation volume" figure in the design text pre-dates this; treat the code constants as authoritative.

## Inputs & Outputs

The mount's back face exposes an item-output interface, and its left/right side faces act as a shaft transferring kinetic input. The rotation axis is the horizontal axis perpendicular to `FACING` (`getRotationAxis` returns `FACING.getClockWise().getAxis()`), so shafts connect on the two side faces.

The Bucket Excavator mount has an internal inventory of up to 4 slots (`outputInv`, an `ItemStackHandler` of `INVENTORY_SLOT_COUNT = 4`). Outputs from the excavation wheel are inserted into this inventory, not dropped on the ground. Intended behaviour is that any outputs which cannot fit are dropped as item entities below the wheel.

> [!note] Implementation
> Extraction inserts results with `ItemHandlerHelper.insertItemStacked(outputInv, …)` and **discards** any overflow — the "drop excess below the wheel" fallback is not yet wired up.

## Operation

When provided with kinetic input, the bucket-wheel rotates, indefinitely producing resources, or mining blocks within its excavation region.

The Bucket Excavator has two modes; **Excavation** (`EXTRACTION`) and **Destruction** (`DESTRUCTION`). The user toggles between them with a Create `ScrollOptionBehaviour` on the top face of the mount (value-box slot at the top-centre of the block).

The excavator only ticks its work loop while its region is clear of other excavators (`isExcavatorClear`) **and** its absolute speed is at or above `MIN_SPEED_THRESHOLD = 128` RPM. The duration of a processing cycle is fixed, regardless of input speed. While powered above the threshold, the excavator repeatedly processes cycles and emits working particles (surface dust over each non-air block, plus an arc of block particles thrown by the wheel).

**If in 'Excavation' Mode:** the cycle runs for `PROCESSING_CYCLE_DURATION = 300` ticks. At the end of each cycle, it checks every block overlapping its excavation region and, for each non-air block, looks up a matching `excavation` recipe and produces its rolled results into the output inventory.

**If in 'Destruction' Mode:** the cycle runs for `DESTRUCTION_CYCLE_DURATION = 60` ticks. Throughout the cycle, the wheel incrementally shows block-breaking progress across every block in the region, then destroys them when the cycle completes (`level.destroyBlock(pos, true)`, dropping their loot). It skips air, blocks in the `resourceful_refinement:excavator_indestructible` tag, and unbreakable blocks (`getDestroySpeed < 0`).

> [!note] Implementation
> The design specifies that redstone input to any face freezes the wheel and pauses processing. The current `BucketExcavatorBlockEntity` has **no redstone gate** — only the speed threshold and overlap-clear flag gate operation. Destruction mode also does not specially exempt Mineral Deposits or Geysers beyond the indestructible tag / unbreakable checks, so protecting them relies on tagging.

## Recipes

The Bucket Excavator defines the `resourceful_refinement:excavation` recipe type (`ExcavationRecipe`, extending Create's `StandardProcessingRecipe`). It expresses a extraction relationship between blocks in the region and item outputs. A recipe consists of:

- `mined_block` — the block that must be inside the excavation region for this recipe to match. Optional; defaults to `resourceful_refinement:mineral_deposit`.
- `mineral_deposit_type` — **optional** block id. When present, the Mineral Deposit's stored block must equal this for the recipe to match; when absent, only `mined_block` is used.
- `results` — the item output(s) (up to 4) produced by a cycle, each with an optional `chance`.
- `processing_time` and `ingredients` — inherited from `ProcessingRecipeParams`.

```json
{
  "type": "resourceful_refinement:excavation",
  "mined_block": "minecraft:stone",
  "ingredients": [],
  "results": [
    { "id": "minecraft:cobblestone", "amount": 2 },
    { "id": "minecraft:flint", "amount": 1 }
  ]
}
```

> [!note] Implementation
> - The old design name *bucketExcavationRecipe* is, in code, the registered type **`resourceful_refinement:excavation`**.
> - The design's source-block id *mineral_deposit_node* is actually **`resourceful_refinement:mineral_deposit`** (the recipe's default `mined_block`).
> - `ExcavationRecipe.matches` supports the optional `mineral_deposit_type` clause, but at runtime the block entity builds its recipe input as `new ExcavationRecipeInput(targetState.getBlock(), null)` — the mineral type is **never fed in**, so any recipe that specifies `mineral_deposit_type` cannot currently match. This aligns with [[Mineral Deposit]] presently being a plain block with no stored resource type.

## Rendering

The Bucket Excavator uses a protruding block entity renderer (`BucketExcavatorRenderer` + `BucketExcavatorModel`) for the bucket-wheel, which extends beyond the mount's own block (visible bounds ~3x4x1). The base block's own render shape is `INVISIBLE`; the wheel rotates with kinetic input.

## Implementation

- **Package:** `content/bucket_excavator/`
- **Block:** `BucketExcavatorBlock` (extends `KineticBlock`, implements `IBE<BucketExcavatorBlockEntity>`), id `bucket_excavator`.
- **Block entity:** `BucketExcavatorBlockEntity` (extends `KineticBlockEntity`).
- **Recipe:** `recipe/ExcavationRecipe` + `recipe/ExcavationRecipeInput`; type/serializer registered in `ModRecipeTypes` as `excavation`.
- **Overlap tracking:** `ExcavatorRegionSavedData` (a per-level `SavedData` keyed by `DimensionalNodePos`) plus `SpatialExcavatorIndex`. Regions are (re)registered on `setPlacedBy`, `updateAfterWrenched`, and `onLoad`, and removed on `onRemove`; overlapping excavators have their `isExcavatorClear` flag cleared.
- **Rendering:** `BucketExcavatorRenderer`, `BucketExcavatorModel`.
- **Tag:** `resourceful_refinement:excavator_indestructible` (blocks destruction mode won't break).
- **Stress:** `ModStressValues.BUCKET_EXCAVATOR_STRESS = 16` (SU per RPM).
- **Tests:** `ExcavatorGameTests`.
- Constants: `MIN_SPEED_THRESHOLD = 128`, `PROCESSING_CYCLE_DURATION = 300`, `DESTRUCTION_CYCLE_DURATION = 60`, `INVENTORY_SLOT_COUNT = 4`.

## Related

- [[Mineral Deposit]]
- [[Drill Pylon]]
- [[Geyser Block]]
- [[Primary Design Doc]]
