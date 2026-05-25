# Blender Blade

## Overview

The **Blender Blade** (`resourceful_refinement:blender_blade`) is a Create-connected kinetic block: a shaft-mounted rotor with two opposing flat arms. It is used as a structural and mechanical component inside the [[Fluid Refinery]] multiblock (centre column of middle and top layers) and can also be placed standalone.

When the blade receives rotational input and spins above a minimum RPM, it **pushes entities** caught in the swept blade volume in the **tangential direction of rotation** and **deals contact damage** on a fixed interval.

---

## Identity & registry

| Kind | ID / class |
|------|------------|
| Block | `blender_blade` → `BlenderBladeBlock` |
| Block entity | `blender_blade` → `BlenderBladeBlockEntity` |
| Item | `blender_blade` → `BlenderBladeItem` (custom BEWLR in hand/GUI) |
| Recipe | Shaped crafting — `data/.../recipe/shaped_crafting/blender_blade_shaped_crafting.json` |

Package: `com.resourceful_refinement.content.refinery` (rendering under `...refinery.rendering`).

---

## Geometry & orientation

### Visual / collision model

- **Shaft axis:** `AXIS` block property (`Direction.Axis` — X, Y, or Z), inherited from Create’s `RotatedPillarKineticBlock`. This is the rotation axis.
- **Blade arms:** Two opposing arms lie in a **plane perpendicular to the shaft**. Each arm is roughly **1.5 blocks** from the shaft centre (≈ **3 blocks** tip-to-tip along the span axis).
- **Arm profile:** Each arm is a wide, thin rectangle:
  - **Span** (length): radial from the shaft, perpendicular to the axis.
  - **Width** (breadth): parallel to the shaft (along the axis).
  - **Thickness** (plane normal): thin; the plane normal corresponds to the block’s logical **front** (the blade disc faces “forward” along this normal).

### Axis mapping (implementation)

For a shaft along **Y** (vertical, typical in the refinery column):

| Dimension | Extent |
|-----------|--------|
| Arm span | ±1.5 blocks along **X** |
| Plane thickness | ±0.65 blocks along **Z** |
| Width along shaft | ±0.55 blocks along **Y** from block centre |

For shaft **X**: span along **Y**, thickness along **Z**, width along **X**.  
For shaft **Z**: span along **Y**, thickness along **X**, width along **Z**.

Client model: `RefineryBlenderModel` / `BlenderBladeRenderer` (animated BER). Collision voxel for the block is a **narrow shaft core** only (`BlenderBladeBlock` shapes 6×6 px on faces); entity interaction uses a **separate, larger sweep AABB** on the block entity.

---

## Kinetics & rendering

- Extends Create **`KineticBlockEntity`** — connects to the rotation network on shaft faces matching `AXIS`.
- **`getSpeed()`** sign and magnitude drive both client rotation (`BlenderBladeRenderer`, `getAngleForBe`) and server entity effects.
- **Render shape:** `ENTITYBLOCK_ANIMATED` — blades drawn by BER, not static block model JSON (block model is particle-only placeholder).
- **Item display:** `BlenderBladeItem` + `BlenderBladeItemRenderer` (`builtin/entity` item model).
- **Stress:** Not registered in `ModStressValues` (no stress impact entry as of current build).

### Effect thresholds

| Constant | Value | Meaning |
|----------|-------|---------|
| `MIN_EFFECT_SPEED` | `8` | Minimum \|speed\| before push/damage apply |
| `REFERENCE_SPEED` | `64` | Speed at which push reaches nominal strength |

### Server rotation angle

The placed **block does not rotate** in-world; only the BER animates. Server hit detection uses `BlenderBladeBlockEntity.computeRotationAngleRadians()`, which mirrors Create’s client `KineticBlockEntityRenderer.getAngleForBe` (same `time × speed × 3/10` degrees formula) but uses `level.getGameTime()` instead of `AnimationTickHolder.getRenderTime`.

Entity offsets are transformed with `worldOffsetToBladeFrame()` (inverse rotation around the shaft axis) before the static blade slab test in `isOffsetInBladeFrame()`.

---

## Entity interaction

Implemented in `BlenderBladeBlockEntity.tick()` (server only).

### Push (tangential)

- Entities inside the blade volume receive an added **velocity** each tick, directed **tangent** to the circle of rotation in the plane ⊥ shaft.
- Direction follows Create rotation sign: positive speed vs negative speed flips push handedness.
- Strength scales with \|speed\| (clamped) and is **stronger toward arm tips** (radial distance from shaft centre).
- **Players:** motion synced with `ClientboundSetEntityMotionPacket`.
- **Skipped:** spectators, creative players, dead entities.

### Contact damage

- **Living** entities in the blade volume take **2** (`CONTACT_DAMAGE`) generic damage every **10** ticks (`DAMAGE_INTERVAL`) while spinning above `MIN_EFFECT_SPEED`.
- Same inclusion rules as push (no creative/spectator harm).

### Tuning constants (code)

| Constant | Value |
|----------|-------|
| `ARM_LENGTH` | `1.5` |
| `PLANE_HALF_THICKNESS` | `0.65` |
| `BASE_PUSH` | `0.12` |
| `CONTACT_DAMAGE` | `2.0` |
| `DAMAGE_INTERVAL` | `10` |

Helpers (usable from tests or other systems): `computeRotationAngleRadians`, `getBladeSweepAABB`, `worldOffsetToBladeFrame`, `isEntityInBladeVolume`, `isOffsetInBladeFrame`, `computeTangentPush`.

---

## Role in the Fluid Refinery

See [[Fluid Refinery]] and `RefineryStructureHelper`:

- **Placement:** Centre cell of each **middle** layer and centre of the **top** layer (glass shell around it).
- **Validation:** `isBlenderBlade()` checks for `blender_blade` at those positions during assembly.
- **Kinetics in refinery:** Middle/top centre cells become `refinery_kinetic_proxy` when assembled; standalone blades at those positions in the blueprint still tick as `BlenderBladeBlockEntity` when not replaced — in the assembled structure, rotation is driven through kinetic proxies linked to the access port network. Standalone placed blades still push/damage on their own when powered.

---

## Crafting & acquisition

- Survival recipe: shaped crafting (see `blender_blade_shaped_crafting.json`).
- Ponder: `RefineryPonders` demonstrates refinery construction including blender placement.

---

## Related files

| Purpose | Path |
|---------|------|
| Block | `content/refinery/BlenderBladeBlock.java` |
| Block entity | `content/refinery/BlenderBladeBlockEntity.java` |
| Item | `content/refinery/BlenderBladeItem.java` |
| BER | `content/refinery/rendering/BlenderBladeRenderer.java` |
| Item renderer | `content/refinery/rendering/BlenderBladeItemRenderer.java` |
| Model layer | `content/refinery/rendering/RefineryBlenderModel.java`, `RefineryLayers.BLENDER` |
| Structure check | `content/refinery/RefineryStructureHelper.java` |

---

## Design history notes

- **Primary Design Doc:** Described as shaft-like dual blades that rotate and damage entities on intersection.
- **Implementation plan (Phase 2):** Originally a placeholder cube; later integrated with Create kinetics and BER. Entity push added to match physical blender behaviour.
- **Collision vs sweep:** Block collision remains a small shaft; gameplay hitbox for entities is intentionally larger than the placed collision box.

---

## Possible follow-ups

- Register stress impact if the blade should load the kinetic network meaningfully.
- Directional `FACING` property if “front” of the blade plane must align with refinery front when axis is vertical.
- Custom damage type / advancement for “caught in the blender”.
- Widen sweep along shaft for multi-block-tall blade stacks if refinery height &gt; 1 in future layouts.
