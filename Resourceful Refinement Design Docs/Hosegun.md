---
title: Hosegun
category: Tool
status: Implemented
introduced: v0.2
recipe_type: n/a
related:
  - "[[Gel Splatter]]"
  - "[[Paint Nozzle]]"
  - "[[Fluid Refill Station]]"
  - "[[Paint Fluids]]"
tags:
  - tool
  - gel
  - fluid
---

The Hosegun is a fluid-powered tool with an internal tank that fires a continuous spray of gel-blob projectile entities. Each gel-blob carries a `FluidStack`, and executes a different function or effect when it impacts a block or entity according to the [[Gel Splatter|gel type]] mapped to that fluid. It is the primary way players place [[Gel Splatter]] blocks, dye blocks and mobs with [[Paint Fluids]], and clean gels back up again.

**ID:** `hosegun`

> [!note] Implementation
> The design doc historically listed the item id as "*housegun*" (*whoops*). The registered item is `hosegun` (see `ModItems.HOSEGUN`); *housegun* is a typo and should not be used.

## Acquisition

The Hosegun is a crafted tool item that stacks to 1. It ships empty and must be filled with a fluid before it can fire (see Usage).

## Usage

The Hosegun is a ranged tool that fires continuously while the player holds down use (right-click), driven through the vanilla bow-style use animation and a 72000-tick use duration for uninterrupted firing. To fire gel-blobs it must first be filled with a fluid using Create's Spout; it can be drained again with Create's Item Drain. Firing stops automatically the moment the tank drops below the ammo cost of the contained fluid.

Projectiles are emitted every 2 ticks while held. They travel fast and far but are inaccurate, producing a slight random spray (inaccuracy `4.5`). Horizontal speed lerps up from `0.8` to `1.6` over the first 20 ticks of a sustained spray (scaled by the global gel-blob velocity factor `1.15`), so the spray "spins up" from when firing begins.

Gel-blobs are destroyed when they impact entities. Unless their gel type dictates otherwise, they do not deal damage to the entity struck.

- **Shift + right-click on a [[Fluid Refill Station]]** binds/unbinds the Hosegun to that station's tracking ID (see State & Data and [[Gel Tracking]]).
- **Right-click with a Glue Pot in the other hand** converts the Hosegun into its *gloopy* variant (see Behaviour).

> [!note] Implementation
> The internal tank capacity is `HosegunItem.CAPACITY = 2000` mb in code. Older design text described a 1000 mb tank; the intended "small internal reservoir" role is unchanged, but the current implementation holds 2000 mb.

## State & Data

All Hosegun state lives on the item stack as data components:

| Component | ID | Type | Role |
|---|---|---|---|
| Stored fluid | `hosegun_fluid` | `SimpleFluidContent` | The tank contents, read/written through the inner `HosegunFluidHandler` (a `FluidHandlerItemStack`). |
| Tracking ID | `hosegun_tracking_id` | `String` | Gel-tracking network ID; sanitised via `FluidRefillStationBlockEntity.sanitiseTrackingId`. Bound gel-blobs tag their splatters with this ID. |
| Gloopy flag | `hosegun_gloopy` | `Boolean` | Marks the gloopy variant. |

The item name and tooltip surface the contained fluid, tank amount, gloopy state, and the bound tracking ID (or an "unbound" line when none is set).

## Behaviour

Each gel-blob carries the fluid used to instantiate it. The fluid's gel type (resolved by `GelPropertiesManager`) determines the projectile's on-hit behaviour and whether it creates a [[Gel Splatter]]. Ammo cost per shot is **not** a flat rate — it varies by gel type:

| Gel type | Ammo cost per blob (mb) |
|---|---|
| Standard gels | 5 |
| Molten | 20 |
| Frozen | 20 |
| Potion | 125 |
| Concrete | 125 |

> [!note] Implementation
> Costs are the `GEL_AMMO_COST` / `MOLTEN_GEL_AMMO_COST` / `FROZEN_AMMO_COST` / `POTION_AMMO_COST` / `CONCRETE_AMMO_COST` constants in `GelPropertiesManager`. Older design text stated a flat 5 mb per projectile; that is now only the base cost for ordinary gels.

Most fluids create a gel splatter where the blob lands. Special gel types behave differently on impact:

- **Cleanse** (water): removes any gel splatters within a 3-block radius of the impact, letting players clean up. Water blobs do not create splatters.
- **Potion**: applies the carried potion. Regular potions apply drink effects on a struck entity; splash potions splash over `ThrownPotion.SPLASH_RANGE`; lingering potions spawn an area-effect cloud. Handled by `PotionGelImpactHandler` via `CreatePotionFluidHelper`.
- **Paint**: dyes valid colourable blocks within a 3-block radius (wool, concrete, concrete_powder, terracotta, glazed_terracotta, stained_glass) to the mapped colour, and dyes valid entities on impact (sheep wool, tamed wolf and cat collars, via `PaintGelCollarHelper` and collar mixin invokers).
- **Molten**: deals molten-gel damage to entities the blob strikes (`ModDamageTypes.moltenGel`).
- **Bouncy**: applies knockback to entities on impact.
- **Frozen / Concrete**: place terrain (ice / powder snow / snow / deepslate for frozen; light grey concrete for concrete) around the impact rather than a splatter. See [[Gel Splatter]].

**Create Item Drains:** when a gel-blob impacts a Create Item Drain, instead of creating a splatter it attempts to fill the drain with its fluid. If the drain already holds an incompatible fluid the blob is discarded without placing anything.

**Gloopy variant:** applying a Glue Pot marks the Hosegun gloopy. A gloopy Hosegun suppresses paint dyeing behaviour on its blobs (paint blobs still place splatters but do not re-colour blocks/entities). The variant can be reverted with the datapack mixing recipe `resourceful_refinement:mixing/ungloop_hosegun` in a Create Basin, which clears the gloopy flag while preserving the stored fluid and tracking ID (`HosegunGloopyRecipes`).

## Implementation

- **Item:** `content/hosegun/HosegunItem` — inner `HosegunFluidHandler` (extends `FluidHandlerItemStack`, capacity 2000 mb, backed by the `hosegun_fluid` component). Firing lives in `onUseTick`.
- **Projectile:** `GelBlobEntity` (`ModEntities.GEL_BLOB`, id `gel_blob`; a `ThrowableItemProjectile` whose visible item is `paint_blob`) with `GelBlobEntityRenderer`. Carries synced fluid data, tracking ID and gloopy flag; contains the block/entity impact logic (splatter placement BFS, cleanse, paint, potion, frozen, concrete, drain fill).
- **Firing/aim helpers:** `HosegunShooting` (muzzle position), `HosegunParticleEffects` (spray velocity + particle streams), `HosegunArmPoses` (first/third-person arm posing), `HosegunClientEvents`.
- **Rendering:** `HosegunItemRenderer` + `HosegunModel` (custom BEWLR item model); `HosegunFluidColors` for fluid tinting.
- **Gloopy:** `HosegunGloopy` (component helper) + `HosegunGloopyRecipes` (Basin mixing capture/restore).
- **Tracking:** `HosegunTracking` (component helper); splatters are registered through `GelTrackingService` — see [[Gel Tracking]].
- **Paint/potion helpers:** `PaintGelCollarHelper`, `PotionGelImpactHandler`, `CreatePotionFluidHelper`.
- **Data components:** `hosegun_fluid`, `hosegun_tracking_id`, `hosegun_gloopy` (`ModDataComponents`).

## Related

- [[Gel Splatter]]
- [[Paint Nozzle]]
- [[Fluid Refill Station]]
- [[Paint Fluids]]
- [[Gel Tracking]]
