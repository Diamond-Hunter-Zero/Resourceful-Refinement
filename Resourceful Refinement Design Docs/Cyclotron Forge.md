---
title: Cyclotron Forge
category: Machine
status: Implemented
introduced: v0.4
recipe_type: resourceful_refinement:cyclotron_forge
related:
  - "[[GLARE Networks]]"
  - "[[GLARE  Lux Transceiver]]"
  - "[[Fluid Processing Recipes]]"
  - "[[Drill Pylon]]"
tags:
  - machine
  - glare
  - multiblock
  - kinetic
  - fluid
---

The Cyclotron Forge is a high-end multiblock processing machine which combines Create kinetic input with [[GLARE Networks|GLARE]] Lux to produce both items and fluids at once. It is a horizontal, linear assembly of 3×3 slices — an output cap, a run of coil segments, and an input cap — whose length must exactly match the recipe being run. Its recipes frequently pair a desirable product with a by-product that must be disposed of.

## Gameplay Role

The Cyclotron Forge is a late-game processing multiblock that ties Create's stress economy into the wider GLARE Lux power economy. Recipes yield an item and a fluid together, and typically one output is considered an undesirable by-product that must be dealt with. Its variable length and its per-tick Lux curve make it a demanding, tuned machine rather than a fire-and-forget one: it requires a minimum RPM, a live Lux allocation sampled over the crafting cycle, and an online GLARE network link before it will run.

## Construction & Placement

The Cyclotron is a multi-block assembly built from a series of horizontally placed slices along `facing.getOpposite()` (the coil run extends backwards from the controller). Every cap and coil slice measures **3 blocks wide by 3 blocks high**, and the assembly has a total length of `(2 + N)` blocks deep, where `N` is the number of coil segments.

- **Output cap** — a 3×3 ring of `brass_casing` surrounding the `cyclotron_controller` at its centre.
- **Coil segments** — 3×3 rings of `heavy_plate_shielding` surrounding an air-block centre. There may be **1 to 14** coil slices.
- **Input cap** — a 3×3 ring of `brass_casing`, with two `item_vault` blocks in the top corners, two `fluid_tank` blocks in the bottom corners, and a kinetic shaft input at its centre.

![[2026-07-01_13.56.11.png|393]]

Right-clicking a `cyclotron_controller` attempts to assemble the structure. If any block in an assembled cyclotron is moved or removed, the structure disassembles and the original blocks are reinstantiated.

> [!note] Implementation
> The total assembly length is capped at **`MAX_TOTAL_LENGTH = 16`** blocks (`CyclotronControllerBlockEntity`). With the two caps fixed, that leaves a coil length of `MIN_COIL_LENGTH = 1` to `MAX_COIL_LENGTH = MAX_TOTAL_LENGTH - 2 = 14`. `heavy_plate_shielding` is the dedicated structural coil block and has no separate design page; it exists only as the coil-ring material.

## Inputs & Outputs

Both caps expose interfaces for items, fluids and kinetic rotation:

- **Item inputs (2):** front faces of the input cap's two **top corner** blocks (`item_vault`), routed through the `ITEM_INPUT_0` / `ITEM_INPUT_1` proxies.
- **Fluid inputs (2):** front faces of the input cap's two **bottom corner** blocks (`fluid_tank`), routed through `FLUID_INPUT_0` / `FLUID_INPUT_1`. Each input tank holds **8000 mB** (`INPUT_TANK_CAPACITY`).
- **Kinetic input:** the centre front face of the input cap (`KINETIC_INPUT`, backed by the `cyclotron_kinetic_proxy`).
- **Item output:** four output slots (`ITEM_OUTPUT_SLOTS = 4`), exposed on the middle back face of the output cap's left column (`ITEM_OUTPUT`).
- **Fluid output:** exposed on the middle back face of the output cap's right column (`FLUID_OUTPUT`); the output tank holds **8000 mB** (`OUTPUT_TANK_CAPACITY`).
- **Lux sockets:** the side faces of the middle-edge blocks of **both caps** act as GLARE receiver sockets for Lux input while assembled, connecting to an adjacent [[GLARE  Lux Transceiver]].

## Operation

When the cyclotron has the correct item and fluid inputs for a recipe, meets the minimum RPM, and is connected to an online Lux network, it begins a processing cycle whose duration is set by the recipe (`processing_time`).

- **Exact coil length.** Every recipe specifies a `coil_length` the assembly must match **exactly**. If a cyclotron has all the required inputs but the wrong coil length, recipe progress stays at **0** and a warning is shown in its goggle tooltips.
- **Minimum RPM.** A recipe requires a `min_rpm`. Kinetic input above this speed does not shorten the processing duration; it only needs to meet the threshold.
- **Lux curve.** A recipe carries a `lux_curve` that determines how much Lux is allocated to the cyclotron as it progresses over the crafting cycle. The curve is sampled per tick and normalised across the cycle duration (`GlareLuxCalculator.sampleVariableLux`) — see [[GLARE Networks]] for how the variable Lux curve is normalised.
- **Interruptions.** If RPM or Lux stops (or the Lux network overloads), the cyclotron **pauses** its current progress. If input amounts change so the recipe is no longer valid, the cycle and attempted recipe are discarded and the machine waits for a new valid recipe. If there is not enough room in the output tanks/inventory for all of a recipe's guaranteed products, the cyclotron pauses until conditions change.
- **Completion.** On a successful cycle the inputs are consumed and the outputs are added to the output tank and inventory slots.

## Recipes

Recipe type id: **`resourceful_refinement:cyclotron_forge`**. Recipes are `ProcessingRecipe`-derived (`StandardProcessingRecipe`) and accept up to **2 item inputs** and **2 fluid inputs**, producing up to **4 item outputs** and up to **2 fluid outputs**.

JSON keys:

- `processing_time` — cycle duration in ticks.
- `coil_length` — the exact coil length the assembly must have.
- `min_rpm` — minimum kinetic speed (optional, defaults to 0).
- `lux_curve` — an integer array sampled per tick to allocate Lux over the cycle (optional; if all zero the recipe requires no Lux).
- `ingredients` — fluid inputs, as `neoforge:single` fluid ingredients (`{ "fluid": ..., "amount": ... }`).
- `sized_ingredients` — item inputs, as `{ "item": ..., "count": ... }`.
- `results` — mixed item and fluid outputs. Items use `{ "id": ..., "count": ..., "chance"? }`; fluids use `{ "id": ..., "amount": ... }`.

```json
{
  "type": "resourceful_refinement:cyclotron_forge",
  "processing_time": 200,
  "coil_length": 3,
  "min_rpm": 64,
  "lux_curve": [4, 6, 8, 6, 4, 2],
  "ingredients": [
    { "type": "neoforge:single", "fluid": "minecraft:water", "amount": 250 }
  ],
  "sized_ingredients": [
    { "item": "minecraft:charcoal", "count": 2 }
  ],
  "results": [
    { "id": "resourceful_refinement:graphite", "count": 1 },
    { "id": "resourceful_refinement:coolant", "amount": 250 }
  ]
}
```

See [[Fluid Processing Recipes]] for the full shipped recipe table.

## Rendering

The assembled multiblock is drawn by `CyclotronForgeRenderer`, a block-entity renderer on the controller, using three baked model parts: `CyclotronFrontModel` (input cap), `CyclotronBackModel` (output cap) and `CyclotronCoilModel` (repeated per coil slice along the length).

## Implementation

Package `content/cyclotron_forge/`:

- **`CyclotronControllerBlock` / `CyclotronControllerBlockEntity`** — the controller. The block entity is a `SmartBlockEntity` implementing `IGlareNode`, `IGlareReceiver`, `GlareNetworkSnapshotProvider` and `IHaveGoggleInformation`. Holds assembly state, the input tanks (`inputTankA` / `inputTankB`), the output tank (`outputTank`), the output inventory (`outputInv`), Lux allocation and the recipe-selection logic. Constants: `MAX_TOTAL_LENGTH = 16`, `MIN_COIL_LENGTH = 1`, `MAX_COIL_LENGTH = 14`, `INPUT_TANK_CAPACITY = OUTPUT_TANK_CAPACITY = 8000`, `ITEM_OUTPUT_SLOTS = 4`.
- **`CyclotronProxyBlock` / `CyclotronProxyBlockEntity`** — non-kinetic proxy blocks that delegate capabilities and logic to the controller (item/fluid I/O, structure, Lux sockets).
- **`CyclotronKineticProxyBlock` / `CyclotronKineticProxyBlockEntity`** — the kinetic proxy that carries the shaft input at the input-cap centre and the machine's stress impact.
- **`CyclotronProxyRole`** — enum tagging each proxy's job: `STRUCTURE`, `ITEM_INPUT_0`, `ITEM_INPUT_1`, `FLUID_INPUT_0`, `FLUID_INPUT_1`, `ITEM_OUTPUT`, `FLUID_OUTPUT`, `KINETIC_INPUT`, `LUX_SOCKET`.
- **`recipe/CyclotronForgeRecipe`** (+ `CyclotronForgeRecipeInput`, `CyclotronForgeRecipeCategory`) — the recipe, its input record and its JEI category. `CyclotronForgeRecipe.Serializer` provides the map/stream codecs; `CyclotronProcessingRecipeParams` splits `ingredients`/`results` into fluid vs item lists via `Either` codecs.
- **`CyclotronForgeRenderer`**, `CyclotronFrontModel`, `CyclotronBackModel`, `CyclotronCoilModel` — segmented rendering.

Registry IDs (`registry/ModBlocks`): blocks `cyclotron_controller`, `cyclotron_proxy`, `cyclotron_kinetic_proxy`, plus the structural `heavy_plate_shielding`. Recipe registration in `registry/ModRecipeTypes` (`cyclotron_forge` type + serializer, `CYCLOTRON_FORGE_TYPE_INFO`). Stress impact `CYCLOTRON_STRESS = 32` (`registry/ModStressValues`, applied to `cyclotron_kinetic_proxy`). Lux integration via `content/glare` (`GlareLuxCalculator`, `LuxTransceiverBlockEntity`).

## Related

- [[GLARE Networks]]
- [[GLARE  Lux Transceiver]]
- [[Fluid Processing Recipes]]
- [[Drill Pylon]]
