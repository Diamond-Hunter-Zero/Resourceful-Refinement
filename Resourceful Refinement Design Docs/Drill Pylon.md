---
title: Drill Pylon
category: Machine
status: Implemented
introduced: v0.4
recipe_type: resourceful_refinement:drill_pylon
related:
  - "[[Crystal Fissure Bud]]"
  - "[[GLARE Networks]]"
  - "[[GLARE  Lux Transceiver]]"
  - "[[Bucket Excavator]]"
  - "[[Cyclotron Forge]]"
tags:
  - machine
  - multiblock
  - kinetic
  - glare
  - worldgen
---

The Drill Pylon is a multiblock structure built over a [[Crystal Fissure Bud]] that extracts resources from it. It uses kinetic input to passively produce resources over time, and can be pushed into an amplified mode by drawing Lux from a [[GLARE Networks|GLARE network]].

**Controller ID:** *drill_pylon_head*
**Proxy IDs:** *drill_pylon_proxy*, *drill_pylon_kinetic_proxy*
**Resource node:** *crystal_fissure_bud*

## Gameplay Role

The Drill Pylon is the deep-extraction counterpart to the [[Bucket Excavator]]: where the excavator works surface [[Mineral Deposit|Mineral Deposits]], the pylon is built over [[Crystal Fissure Bud]] blocks (the crystal-tier resource node) and taps them for higher-value outputs. Its efficiency can be pushed further by feeding it Lux from a [[GLARE Networks|GLARE network]] to run its amplified ("turbo") mode.

## Construction & Placement

To build a Drill Pylon, players first place a **Drill Pylon Head** directly on top of a [[Crystal Fissure Bud]] block. Above the head they stack **3 layers (y = 1–3)**, each a 3×3 ring of `brass_casing` with a `gearbox` in the centre. Then, on each of the four corners of a 5×5 square centred on the head, they stack `metal_girder` blocks **from y = 0 to y = 3**. The total structure height is **5 blocks** (the Fissure Bud at y = 0, the head at y = 0's position, rings at y = 1–3).

When the head is right-clicked, it attempts to assemble a Drill Pylon structure. The 'front' is taken to be the horizontal direction towards the activating player (`player.getDirection().getOpposite()`). Assembly requires a valid structure and a Crystal Fissure Bud directly below the head.

> [!note] Implementation
> `validateStructure` checks: for `y` in 1–3, a `gearbox` at the column centre and `brass_casing` filling the rest of that 3×3 ring; and for `y` in 0–3, a `metal_girder` at each of the four `(±2, y, ±2)` corners. The front must be horizontal. On success `convertStructureToProxies` swaps the built blocks for proxy block entities (storing their original states), and disassembly restores them. Assembly registers the head as a GLARE node and refreshes any adjacent Lux Transceiver.

If any block in an assembled Drill Pylon is removed or moved, the structure disassembles and reinstates all original blocks. Disassembly also removes any GLARE links and node registration.

Crystal Fissure Bud blocks (like [[Mineral Deposit|Mineral Deposits]] and [[Geyser Block|Geysers]]) internally store an `Item` reference (default `minecraft:raw_iron`). This stored item selects the active `DrillPylonRecipe` for any pylon assembled above the bud; a creative player can right-click the bud with an item to set it.

## Inputs & Outputs

An assembled Drill Pylon exposes interfaces through its proxies, whose roles are assigned by `DrillPylonProxyRole`:

- **OUTPUT** — the front-centre proxy of the lowest ring (y = 1) exposes an item-output interface on the front face.
- **KINETIC** — the left/right-centre proxies of the lowest ring expose kinetic shafts (as `drill_pylon_kinetic_proxy`, axis = `front.getClockWise().getAxis()`) that power the drill.
- **LUX_SOCKET** — the back-centre proxy of the 2nd ring (y = 2) exposes a Lux Socket on the rear face, where a [[GLARE  Lux Transceiver]] can attach.
- **STRUCTURE** — all other proxies (including the corner girders and the central column) are inert structural proxies.

A Drill Pylon has internal storage for 4 item slots (`outputInv`).

## Operation

The Drill Pylon requires a minimum speed of `MIN_RPM = 64`. Above that threshold the drill functions; further speed increases do not improve efficiency. Speed is read from the two kinetic proxies (`getProxySpeed` takes the max of the left/right kinetic proxy speeds).

The active recipe is chosen each tick from the Crystal Fissure Bud's stored item (`resolveSourceItem`). If there is no matching recipe, progress and Lux allocation reset and the drill idles.

While a recipe is active, the drill samples the recipe's **Lux curve** via `GlareLuxCalculator.sampleVariableLux(curve, progress, duration)` and requests that much Lux from the network each tick (`setAllocatedLux`). It only advances progress when `getProxySpeed() >= MIN_RPM` **and** its Lux requirement is satisfied. When progress reaches the recipe duration, it rolls the results and inserts them into the output inventory (deferring if they don't fit), then resets.

**Regular vs Amplified mode:** with only kinetic input the drill runs in regular mode. When a recipe specifies a non-zero Lux curve (`requiresLux()`), the drill needs an **online** [[GLARE Networks|GLARE network]] link — a bound `networkId`, `GlareOperationStatus.ONLINE`, and at least one live link via the Lux Socket (from a [[GLARE  Lux Transceiver]]) — to progress. If the network over-allocates and shuts down, or Lux otherwise drops out, operation pauses until requirements are met again.

**Standalone (unassembled) mode:** an un-assembled Drill Pylon Head behaves as an independent mechanical drill. Every `STANDALONE_DRILL_INTERVAL = 20` ticks, while `|speed| >= MIN_RPM`, it mines the 3×3 face directly in front of it (skipping air and unbreakable blocks), like a Create Drill widened to 3×3.

## Recipes

Each Drill Pylon runs the `resourceful_refinement:drill_pylon` recipe (`DrillPylonRecipe`) selected by the item stored in the Crystal Fissure Bud beneath it. A recipe defines the cycle duration, the rolled results, and an optional Lux curve dictating per-tick Lux consumption across the cycle in amplified mode.

Recipe JSON keys: `source_item`, `processing_time`, `ingredients`, `results` (each may carry a `chance`), and `lux_curve` (an int array sampled across the cycle; all-zero / absent means the recipe needs no Lux).

```json
{
  "type": "resourceful_refinement:drill_pylon",
  "source_item": "minecraft:raw_iron",
  "processing_time": 300,
  "ingredients": [],
  "results": [
    { "id": "minecraft:raw_iron", "count": 2 },
    { "id": "minecraft:raw_copper", "count": 1, "chance": 0.25 }
  ],
  "lux_curve": [0, 2, 4, 6, 4, 2]
}
```

## Rendering

The Drill Pylon Head uses `DrillPylonRenderer` (a `BlockEntityRenderer`) for its entity-model visuals. While part of an assembled pylon, the head renders the whole multiblock and reports an expanded render bounding box (`worldPosition ± 2` laterally, up to +4 in y).

## Implementation

- **Package:** `content/drill_pylon/`
- **Controller:** `DrillPylonHeadBlock` / `DrillPylonHeadBlockEntity` (extends `KineticBlockEntity`; implements `IGlareNode`, `IGlareReceiver`, `GlareNetworkSnapshotProvider`, `IHaveGoggleInformation`), id `drill_pylon_head`.
- **Proxies:** `DrillPylonProxyBlock` / `DrillPylonProxyBlockEntity` (id `drill_pylon_proxy`) and `DrillPylonKineticProxyBlock` / `DrillPylonKineticProxyBlockEntity` (id `drill_pylon_kinetic_proxy`, a `RotatedPillarKineticBlock`). Proxy roles: `DrillPylonProxyRole { STRUCTURE, OUTPUT, KINETIC, LUX_SOCKET }`.
- **Resource node:** `CrystalFissureBudBlock` / `CrystalFissureBudBlockEntity` (id `crystal_fissure_bud`) — see [[Crystal Fissure Bud]].
- **Recipe:** `recipe/DrillPylonRecipe` (extends `StandardProcessingRecipe`) + `recipe/DrillPylonRecipeInput` + `recipe/DrillPylonRecipeCategory` (JEI). Type/serializer registered in `ModRecipeTypes` as `drill_pylon`.
- **GLARE integration:** `getMaxGlareLinks() = 1`, `allowsManualGlareLinks() = false`; Lux endpoint is the rear-centre y=2 proxy. Lux sampling via `GlareLuxCalculator`; network state via `GlareService`.
- **Rendering:** `DrillPylonRenderer`.
- **Stress:** `ModStressValues.DRILL_PYLON_STRESS = 16` (applied to both `drill_pylon_head` and `drill_pylon_kinetic_proxy`).
- Constants: `MIN_RPM = 64`, `INVENTORY_SLOT_COUNT = 4`, `STANDALONE_DRILL_INTERVAL = 20`.

## Related

- [[Crystal Fissure Bud]]
- [[GLARE Networks]]
- [[GLARE  Lux Transceiver]]
- [[Bucket Excavator]]
- [[Cyclotron Forge]]
- [[Primary Design Doc]]
