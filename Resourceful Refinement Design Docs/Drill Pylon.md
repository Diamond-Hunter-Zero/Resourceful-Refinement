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

Layers below are numbered from the [[Crystal Fissure Bud]]'s layer (y = 0):

- **Head (y = 1):** place the **Drill Pylon Head** directly on top of the Crystal Fissure Bud. Apart from the head and the corner legs, this layer is left open; it is the space the drill bit extends into.
- **Legs (y = 0–4):** on each of the four corners of a 5×5 square centred on the bud, stack `metal_girder` blocks starting level with the bud (5 tall).
- **Body (y = 2–4):** 3 layers above the head, each a 3×3 ring of `brass_casing` with a `gearbox` in the centre.

The pylon is **5 blocks** tall (y = 0–4), plus a [[GLARE  Lux Transceiver]] on top if Lux is used.

When the head is right-clicked, it attempts to assemble a Drill Pylon structure. The 'front' is taken to be the horizontal direction towards the activating player (`player.getDirection().getOpposite()`). Assembly requires a valid structure and a Crystal Fissure Bud directly below the head.

> [!note] Implementation
> Layout heights are constants on `DrillPylonHeadBlockEntity`, as offsets **from the head** (one less than the layer numbers above): `LEG_BOTTOM_Y = -1`, `LEG_TOP_Y = 3`, `BODY_BOTTOM_Y = 1`, `BODY_TOP_Y = 3`, `OUTPUT_Y = 1`, `KINETIC_Y = 2`, `LUX_SOCKET_Y = 3`.
> `validateStructure` checks: for head offsets 1–3, a `gearbox` at the column centre and `brass_casing` filling the rest of that 3×3 ring; and for offsets −1–3, a `metal_girder` at each of the four `(±2, y, ±2)` corners. The rest of the head's layer is not validated. The front must be horizontal. On success `convertStructureToProxies` swaps the built blocks for proxy block entities (storing their original states), and disassembly restores them. Assembly registers the head as a GLARE node and refreshes any adjacent Lux Transceiver.

If any block in an assembled Drill Pylon is removed or moved, the structure disassembles and reinstates all original blocks. Disassembly also removes any GLARE links and node registration.

Crystal Fissure Bud blocks (like [[Mineral Deposit|Mineral Deposits]] and [[Geyser Block|Geysers]]) internally store an `Item` reference (default `minecraft:raw_iron`). This stored item selects the active `DrillPylonRecipe` for any pylon assembled above the bud; a creative player can right-click the bud with an item to set it.

## Inputs & Outputs

An assembled Drill Pylon exposes interfaces through its proxies, whose roles are assigned by `DrillPylonProxyRole`:

- **OUTPUT** — the front-centre proxy of the lowest body ring (y = 2) exposes an item-output interface on the front face.
- **KINETIC** — the left/right-centre proxies of the middle body ring (y = 3) expose kinetic shafts (as `drill_pylon_kinetic_proxy`, axis = `front.getClockWise().getAxis()`) that power the drill. The centre gearbox of that ring also becomes a kinetic proxy.
- **LUX_SOCKET** — the top-centre proxy (the y = 4 gearbox position) exposes a Lux Socket on its **top** face, where a [[GLARE  Lux Transceiver]] can be placed facing down onto it.
- **STRUCTURE** — all other proxies (including the corner legs and the rest of the central column) are inert structural proxies.

A Drill Pylon has internal storage for 4 item slots (`outputInv`).

## Operation

The Drill Pylon requires a minimum speed of `MIN_RPM = 64`. Above that threshold the drill functions; further speed increases do not improve efficiency. Speed is read from the two kinetic proxies (`getProxySpeed` takes the max of the left/right kinetic proxy speeds).

The active recipe is chosen each tick from the Crystal Fissure Bud's stored item (`resolveSourceItem`). If there is no matching recipe, progress and Lux allocation reset and the drill idles.

While a recipe is active, the drill samples the recipe's **Lux curve** via `GlareLuxCalculator.sampleVariableLux(curve, progress, duration)` and requests that much Lux from the network each tick (`setAllocatedLux`). It only advances progress when `getProxySpeed() >= MIN_RPM` **and** its Lux requirement is satisfied. When progress reaches the recipe duration, it rolls the results and inserts them into the output inventory (deferring if they don't fit), then resets.

**Regular vs Amplified mode:** with only kinetic input the drill runs in regular mode. When a recipe specifies a non-zero Lux curve (`requiresLux()`), the drill needs an **online** [[GLARE Networks|GLARE network]] link — a bound `networkId`, `GlareOperationStatus.ONLINE`, and at least one live link via the Lux Socket (from a [[GLARE  Lux Transceiver]]) — to progress. If the network over-allocates and shuts down, or Lux otherwise drops out, operation pauses until requirements are met again.

**Standalone (unassembled) mode:** an un-assembled Drill Pylon Head behaves as an independent mechanical drill. Every `STANDALONE_DRILL_INTERVAL = 20` ticks, while `|speed| >= MIN_RPM`, it mines the 3×3 face directly in front of it (skipping air and unbreakable blocks), like a Create Drill widened to 3×3. Between breaks, each breakable block in the face shows the block-breaking crack overlay, advancing through all 10 stages over the interval. Each of the 9 targets has its own breaker id, taken from Create's `BlockBreakingKineticBlockEntity.NEXT_BREAKER_ID`. The overlay is cleared when speed drops, when the head assembles, or when the block entity is invalidated.

### Movement & Contraptions

- **Standalone heads are movable.** The head keeps `PushReaction.NORMAL`, so Create contraptions (pistons, bearings, gantries, minecart contraptions) can carry it. On a contraption it uses `DrillPylonHeadMovementBehaviour`, which extends Create's `DrillMovementBehaviour`:
  - It breaks the 3×3 face at the same speed and with the same stalling as a Create Drill.
  - The centre block drives the breaking progress. When it breaks, the other 8 blocks in the face (in world space, accounting for contraption rotation) break with it, and their drops go to the contraption's storage.
  - If the centre is empty or unbreakable, the first breakable block in the face drives the break instead.
  - On a contraption the head draws through its own renderer rather than Create's drill model.
- **Assembled heads cannot be moved.** A Create `MovementAllowedCheck` returns `FAIL` for any head whose block entity is assembled, so a contraption that includes one fails to assemble, just as it does with any other unmovable block. Vanilla pistons never push block entities, and every proxy block uses `PushReaction.BLOCK`, so nothing else can pull the multiblock apart.
- **Fallback:** if an assembled head is still removed by any other route (broken, wrenched, or replaced by another mod), `onRemove` disassembles the structure and restores the original blocks.

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

The Drill Pylon Head uses `DrillPylonRenderer` (a `BlockEntityRenderer`) to draw `DrillPylonModel`, a `HierarchicalModel` (texture `textures/block/drill_pylon.png`) of the whole multiblock: four angled legs, the octagonal drill body, and a three-tier drill bit hanging into the head's layer (y = 1). The model's ground plane sits on the bud's layer (y = 0) and is rotated to the head's `FACING`. While assembled, the head reports an expanded render bounding box (`worldPosition ± 2` laterally, from one below the head to four above it, covering y = 0–4). 

An assembled pylon also renders a half shaft on the outer face of each kinetic input proxy (left and right of the front, at `KINETIC_Y`). Each shaft is turned at that proxy's kinetic angle (`getAngleForBe`) and lit by the block outside that face.

### Standalone head

An unassembled head draws `DrillPylonHeadModel` (layer `drill_pylon_head:main`, same texture), through `DrillPylonHeadRendering`:

- **Orientation:** the model is authored facing north and is turned about the block centre to match any of the six `FACING` states.
- **Drill bit:** the `drill_bit` bone spins about the facing axis at the head's kinetic angle (`getAngleForBe`), so it matches the speed and direction of the shaft driving it.
- **Half shaft:** a half-shaft partial on the back face (`facing.getOpposite()`), drawn with `ShaftUtilities.RenderKineticShaft` and the same angle.
- **On contraptions:** `DrillPylonHeadMovementBehaviour` turns off block-entity rendering and draws the same model and half shaft through `DrillPylonHeadRendering.renderInContraption`. As with Create's drill, the bit spins at the contraption's animation speed while the contraption is stalled or moving away from the head's back face, and holds still otherwise. Lighting is sampled from the real world at the head's current world position (found through the contraption's world matrix), because the contraption's own render world treats the head as enclosed in solid blocks.

### Animation

The drill bit has three keyframe animations defined in `DrillPylonModel`:

| Animation | Length | Purpose |
| --- | --- | --- |
| `drill_extend` | 2.25 s | Start-up: the bit lowers out of the body while spinning up. |
| `drill_spin_loop` | 0.75 s, looping | Operating: one full revolution per loop. |
| `drill_retract` | 2.25 s | Wind-down: the bit spins down and lifts back into the body. |

The animation follows a server-synced `drilling` flag on the head: it is true while the pylon is advancing recipe progress, and false when it has no recipe, too little speed, no Lux, or is blocked by a full output. The renderer runs a client-side state machine (`DrillAnimationPhase`):

`RETRACTED` → (drilling) → `EXTENDING` → `RUNNING` → (not drilling) → `RETRACTING` → `RETRACTED`

- Start-up and wind-down always play to completion. If drilling stops during start-up, the drill goes straight into wind-down when start-up finishes.
- When drilling stops, the spin loop finishes its current revolution before wind-down begins.
- The bit's end rotation from start-up (−540°) is carried as a yaw offset, so the bit doesn't snap between animations.
- If a pylon is already drilling when it first renders (for example on chunk load), it starts in `RUNNING` and skips start-up.

While in `RUNNING`, the head also sprays debris particles every client tick (`DRILL_PARTICLES_PER_TICK = 8`). They use the texture of the block being drilled (the Crystal Fissure Bud below the head) and fly outward and upward from the bit's tip.

## Implementation

- **Package:** `content/drill_pylon/`
- **Controller:** `DrillPylonHeadBlock` / `DrillPylonHeadBlockEntity` (extends `KineticBlockEntity`; implements `IGlareNode`, `IGlareReceiver`, `GlareNetworkSnapshotProvider`, `IHaveGoggleInformation`), id `drill_pylon_head`.
- **Proxies:** `DrillPylonProxyBlock` / `DrillPylonProxyBlockEntity` (id `drill_pylon_proxy`) and `DrillPylonKineticProxyBlock` / `DrillPylonKineticProxyBlockEntity` (id `drill_pylon_kinetic_proxy`, a `RotatedPillarKineticBlock`). Proxy roles: `DrillPylonProxyRole { STRUCTURE, OUTPUT, KINETIC, LUX_SOCKET }`.
- **Resource node:** `CrystalFissureBudBlock` / `CrystalFissureBudBlockEntity` (id `crystal_fissure_bud`) — see [[Crystal Fissure Bud]].
- **Recipe:** `recipe/DrillPylonRecipe` (extends `StandardProcessingRecipe`) + `recipe/DrillPylonRecipeInput` + `recipe/DrillPylonRecipeCategory` (JEI). Type/serializer registered in `ModRecipeTypes` as `drill_pylon`.
- **GLARE integration:** `getMaxGlareLinks() = 1`, `allowsManualGlareLinks() = false`; Lux endpoint is the top-centre y=4 proxy (socket face `UP`; the transceiver is looked up at `socket.above()`). Lux sampling via `GlareLuxCalculator`; network state via `GlareService`.
- **Contraptions:** `DrillPylonHeadMovementBehaviour` and the assembled-head `MovementAllowedCheck`, both registered in `ResourcefulRefinementMain.commonSetup`.
- **Rendering:** `DrillPylonRenderer` + `DrillPylonModel` (layer `drill_pylon:main`); animation phases in `DrillAnimationPhase`.
- **Stress:** two server config values in the `Kinetics` section, registered in `ModStressValues`:
  - `drill_pylon_head_stress` (default 8) — the impact of the `drill_pylon_head` block, which drives it when used as a standalone drill. The goggle tooltip for an unassembled head uses this value.
  - `drill_pylon_stress` (default 16) — the total impact of an assembled pylon. It has three `drill_pylon_kinetic_proxy` blocks (left input, centre gearbox, right input) on one shaft line, all in the same network whether one or both inputs are connected. The centre proxy only links the two inputs and applies no stress: `DrillPylonKineticProxyBlockEntity.calculateStressApplied()` returns 0 when the proxy sits on the head's column (`dx == 0 && dz == 0`). The two inputs are each registered with `drill_pylon_stress / 2`, so the total matches the config value exactly (the default 16 gives 8 + 8).
- Constants: `MIN_RPM = 64`, `INVENTORY_SLOT_COUNT = 4`, `STANDALONE_DRILL_INTERVAL = 20`.

## Related

- [[Crystal Fissure Bud]]
- [[GLARE Networks]]
- [[GLARE  Lux Transceiver]]
- [[Bucket Excavator]]
- [[Cyclotron Forge]]
- [[Primary Design Doc]]
