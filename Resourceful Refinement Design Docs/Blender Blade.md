---
title: Blender Blade
category: Machine
status: Implemented
introduced: v0.1
recipe_type: n/a
related:
  - "[[Fluid Refinery]]"
tags:
  - machine
  - kinetic
  - multiblock
---

The **Blender Blade** (`resourceful_refinement:blender_blade`) is a Create-connected kinetic block:
a shaft-mounted rotor with two opposing flat arms. It is both a structural and mechanical component
inside the [[Fluid Refinery]] multiblock — filling the centre column of the middle and top layers —
and a block that can be placed standalone. When it receives rotational input and spins above a
minimum RPM, it **pushes entities** caught in the swept blade volume in the **tangential direction
of rotation** and **deals contact damage** on a fixed interval.

## Gameplay Role

Inside the refinery the blade is the kinetic sink that turns raw shaft rotation into processing:
the central cells become kinetic proxies driving the [[Fluid Refinery]]'s crafting. Placed on its
own it is a hazard/utility block — a spinning rotor that flings and injures entities that stray into
its reach, useful for mob-grinding or knockback contraptions.

## Construction & Placement

- Placed like any Create shaft block, orienting to a chosen **axis** (X, Y or Z).
- In the refinery it occupies the **centre cell of each middle layer** and the **centre of the top
  layer**, wrapped by the glass shell. `RefineryStructureHelper.isBlenderBlade()` checks for
  `blender_blade` at those positions during assembly; on assembly those cells are replaced with
  `refinery_kinetic_proxy` blocks that carry rotation through to the controller.
- Standalone blades placed at those positions in an un-assembled blueprint still tick as
  `BlenderBladeBlockEntity` and push/damage on their own when powered.

### Geometry & orientation

- **Shaft axis:** the `AXIS` block property (`Direction.Axis` — X, Y or Z), inherited from Create's
  `RotatedPillarKineticBlock`. This is the rotation axis.
- **Blade arms:** two opposing arms lie in a plane **perpendicular to the shaft**. Each arm reaches
  roughly **1.5 blocks** from the shaft centre (≈ **3 blocks** tip-to-tip along the span axis).
- **Arm profile:** each arm is a wide, thin rectangle — *span* (length) radial from the shaft,
  *width* (breadth) parallel to the shaft, *thickness* (plane normal) thin; the plane normal
  corresponds to the block's logical *front* (the blade disc faces "forward" along this normal).

Axis mapping, for a shaft along **Y** (vertical, typical in the refinery column):

| Dimension | Extent |
|---|---|
| Arm span | ±1.5 blocks along **X** |
| Plane thickness | ±0.5 blocks along **Z** |
| Width along shaft | ±0.55 blocks along **Y** from block centre |

For shaft **X**: span along **Y**, thickness along **Z**, width along **X**. For shaft **Z**: span
along **Y**, thickness along **X**, width along **Z**.

> [!note] Implementation
> The **collision voxel** for the placed block is a narrow shaft core only — `Block.box(5,0,5,11,16,11)`
> for a Y axis (a **6×6 px** column on the faces), with matching X/Z variants. Entity interaction
> uses a *separate, larger* sweep AABB on the block entity, so the gameplay hitbox is intentionally
> bigger than the placed collision box. The plane half-thickness in code is **0.5** (an earlier
> draft of this doc listed 0.65).

## Inputs & Outputs

- **Kinetic:** connects to Create's rotation network on shaft faces whose axis matches `AXIS`
  (`hasShaftTowards` / `getRotationAxis`). It is a pass-through rotor with no internal inventory or
  tanks.
- **Stress:** *not* registered in `ModStressValues` — the blade carries no stress impact entry as of
  the current build (in the assembled refinery the kinetic proxy carries the refinery's stress; see
  [[Fluid Refinery]]).

## Operation

Entity effects are implemented in `BlenderBladeBlockEntity.tick()` (server only), and run whenever
`|getSpeed()|` is at least `MIN_EFFECT_SPEED`.

### Push (tangential)

- Entities inside the blade sweep receive added **velocity** each tick, directed **tangent** to the
  circle of rotation in the plane perpendicular to the shaft.
- Direction follows Create's rotation sign — positive vs negative speed flips the handedness.
- Strength scales with `|speed|` and radial distance (stronger toward the arm tips).
- **Players:** motion is synced with `ClientboundSetEntityMotionPacket`.
- **Skipped:** spectators, creative players, dead entities. Non-item entities have their push halved.

### Contact damage

- **Living** entities in the blade volume take `CONTACT_DAMAGE` generic damage every
  `DAMAGE_INTERVAL` ticks while spinning above `MIN_EFFECT_SPEED`.
- Same inclusion rules as push (no creative/spectator harm).

### Effect thresholds & tuning (code)

| Constant | Value | Meaning |
|---|---|---|
| `MIN_EFFECT_SPEED` | `1` | Minimum \|speed\| before push/damage apply |
| `REFERENCE_SPEED` | `8` | Speed at which push reaches nominal strength |
| `ARM_LENGTH` | `1.5` | Arm reach from shaft centre (blocks) |
| `PLANE_HALF_THICKNESS` | `0.5` | Half-thickness of the blade plane (blocks) |
| `BASE_PUSH` | `0.05` | Base tangential impulse scale per tick at reference speed |
| `CONTACT_DAMAGE` | `0.5` | Damage per damage tick |
| `DAMAGE_INTERVAL` | `15` | Ticks between damage applications |

> [!note] Implementation
> These values were reconciled against `BlenderBladeBlockEntity`. An earlier draft of this doc
> listed `MIN_EFFECT_SPEED = 8`, `REFERENCE_SPEED = 64`, `PLANE_HALF_THICKNESS = 0.65`,
> `BASE_PUSH = 0.12`, `CONTACT_DAMAGE = 2.0` and `DAMAGE_INTERVAL = 10`; the current code uses the
> table above (activation from `|speed| ≥ 1`, full push strength referenced at `|speed| = 8`). Also
> note the narrow-phase `isEntityInBladeVolume` check is currently **commented out** in `tick()`, so
> push/damage are applied to every eligible entity inside the broad sweep AABB rather than only the
> thin rotating blade slab.

### Server rotation angle

The placed **block does not rotate** in-world; only the BER animates. Server hit detection uses
`BlenderBladeBlockEntity.computeRotationAngleRadians()`, which mirrors Create's client
`KineticBlockEntityRenderer.getAngleForBe` (same `time × speed × 3/10` degrees formula) but uses
`level.getGameTime()` instead of `AnimationTickHolder.getRenderTime`. Entity offsets are transformed
with `worldOffsetToBladeFrame()` (inverse rotation around the shaft axis) before the static blade
slab test in `isOffsetInBladeFrame()` (which uses a shaft half-width of 0.55).

Helpers usable from tests or other systems: `computeRotationAngleRadians`, `getBladeSweepAABB`,
`worldOffsetToBladeFrame`, `isEntityInBladeVolume`, `isOffsetInBladeFrame`, `computeTangentPush`.

## Rendering

- **Render shape:** `ENTITYBLOCK_ANIMATED` — the blades are drawn by a BER, not a static block model
  (the block model JSON is a particle-only placeholder).
- **Client model:** `RefineryBlenderModel` / `BlenderBladeRenderer` (animated BER). `getSpeed()`
  sign and magnitude drive client rotation via `getAngleForBe`.
- **Item display:** `BlenderBladeItem` plus `BlenderBladeItemRenderer` (a `builtin/entity` /
  BEWLR item model) render the blade in hand and in the GUI.

## Crafting & acquisition

- **ID:** `resourceful_refinement:blender_blade` (block, item and block entity all share the name).
- **Survival recipe:** shaped crafting —
  `data/resourceful_refinement/recipe/shaped_crafting/blender_blade_shaped_crafting.json`.
- **Ponder:** `RefineryPonders` demonstrates refinery construction, including blender placement.

## Implementation

Package: `com.resourceful_refinement.content.refinery` (rendering under `...refinery.rendering`).

| Purpose | Path |
|---|---|
| Block | `content/refinery/BlenderBladeBlock` (`RotatedPillarKineticBlock`, `IBE`) |
| Block entity | `content/refinery/BlenderBladeBlockEntity` (`KineticBlockEntity`) |
| Item | `content/refinery/BlenderBladeItem` |
| BER | `content/refinery/rendering/BlenderBladeRenderer` |
| Item renderer | `content/refinery/rendering/BlenderBladeItemRenderer` |
| Model layer | `content/refinery/rendering/RefineryBlenderModel`, `RefineryLayers.BLENDER` |
| Structure check | `content/refinery/RefineryStructureHelper` (`isBlenderBlade`) |

- Registered in `ModBlocks.BLENDER_BLADE`, `ModItems.BLENDER_BLADE`, `ModBlockEntities.BLENDER_BLADE`.
- Damage is dealt via `level.damageSources().generic()`.

## Role in the Fluid Refinery

See [[Fluid Refinery]] and `RefineryStructureHelper`:

- **Placement:** centre cell of each **middle** layer and centre of the **top** layer (glass shell
  around it).
- **Validation:** `isBlenderBlade()` checks for `blender_blade` at those positions during assembly.
- **Kinetics in the refinery:** middle/top centre cells become `refinery_kinetic_proxy` when
  assembled, and rotation is driven through those kinetic proxies linked to the Access Port network.
  Standalone placed blades still push/damage on their own when powered.

## Design history notes

- **Primary Design Doc:** described as shaft-like dual blades that rotate and damage entities on
  intersection.
- **Implementation plan (Phase 2):** originally a placeholder cube; later integrated with Create
  kinetics and a BER. Entity push was added to match physical blender behaviour.
- **Collision vs sweep:** block collision remains a small shaft; the gameplay hitbox for entities is
  intentionally larger than the placed collision box.

## Possible follow-ups

- Register a stress impact if the blade should load the kinetic network meaningfully.
- Custom damage type / advancement for "caught in the blender".

## Related

- [[Fluid Refinery]]
