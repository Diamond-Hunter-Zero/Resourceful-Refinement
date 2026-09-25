---
title: Geyser Block
category: Machine
status: Implemented
introduced: v0.1
recipe_type: n/a
related:
  - "[[Fracking Pump]]"
  - "[[Nether Geyser Worldgen]]"
  - "[[Fracking Source Blocks]]"
tags:
  - machine
  - worldgen
  - fluid
  - decorative
---

The Geyser Block naturally generates inside geyser structures on the overworld's surface and within caves (and in the Nether — see [[Nether Geyser Worldgen]]). Geysers periodically spawn fluid source blocks above them, as well as jets of particles that launch entities into the air. Each geyser is associated with a stored fluid, which both determines what it erupts and which [[Fracking Pump]] recipes it enables.

**ID:** *geyser_block*

## Gameplay Role

Geysers are the natural anchor for the fracking loop: they generate as part of worldgen structures, carry a stored fluid (molten ores, lava, etc.), and are the block a [[Fracking Pump]] must be built on top of. Left alone, a geyser is a hazard-and-flavour block that erupts fluid and launches players; harnessed with a pump, it becomes a renewable fluid source. Worldgen placement is covered by [[Nether Geyser Worldgen]].

## Construction & Placement

Geysers are not crafted in survival; they generate as part of geyser worldgen structures. They appear as stone blocks with transparent cracks exposing an animated fluid cube within. Geysers do not drop when broken, are highly explosion resistant, and take a while to mine.

- **Setting the fluid (Creative):** right-clicking a geyser with a filled bucket sets its associated fluid. This is stored in the block entity's data. By default, geysers are associated with lava.
- **Copying (Creative):** Ctrl + middle-mouse (pick block) on a geyser should preserve its NBT data (i.e. its associated fluid).

> [!note] Implementation
> `GeyserBlock` copies `Blocks.STONE`'s properties with `strength(50.0f, 1200.0f)` (long mining time, high blast resistance), `requiresCorrectToolForDrops()`, `pushReaction(BLOCK)`, and `noOcclusion()`. Its generated loot table has no pools, so it drops nothing when broken (matching the "does not drop" intent). The bucket interaction is gated to `player.isCreative()`; it reads the fluid from the held stack and calls `setAssociatedFluid`. `Fluids.EMPTY` is rejected.

## Inputs & Outputs

A geyser has no item or fluid I/O of its own — it *spawns* fluid source blocks in the world above itself, and it exposes its associated fluid to a [[Fracking Pump]] sitting on top of it (the pump reads the geyser BE's `getAssociatedFluid()` to select a recipe).

## Operation

**Blasts.** Geysers frequently exhibit "Blasts" on a random, configurable basis. When a geyser erupts a blast, it spawns a plume of smoke/cloud/gust particles and launches any entities on top of the block into the air.

**Eruptions.** Geysers also occasionally spawn a fluid source block at the position directly above them, on a random cooldown. The type of fluid placed is the geyser's associated fluid. When a new fluid block is placed, the geyser emits a puff of smoke particles and plays a sound.

> [!note] Implementation
> `GeyserBlockEntity` runs a server-side ticker (`GeyserBlock.getTicker` returns null on the client). Two independent timers drive it:
> - **Blast** (`blastTimer`, initial `1000 + random(2000)` ticks, then re-armed to `(15..45) * 20` ticks): sets a 25-tick `activeBlastTimer` stream of cloud/dust/gust particles and looping steam sound, and applies a `+1.3` upward delta to entities in the AABB above. Player velocity is synced via `ClientboundSetEntityMotionPacket` so the client authorises the launch.
> - **Eruption** (`eruptionTimer`, initial `6000 + random(12000)` ticks, then re-armed to `(5..15) * 20 * 60` ticks — roughly 5–15 minutes): if the block above is air/replaceable, places `associatedFluid.defaultFluidState().createLegacyBlock()` there with a bucket-empty sound and a smoke/gust particle burst.
>
> The associated fluid persists as the `Fluid` NBT string and syncs to the client for rendering.

## Rendering

Geysers use a `BlockEntityRenderer` to draw an outer cutout stone texture (a standard solid block model, no custom shapes) and an interior cube of animated fluid. The interior fluid cube is almost flush with the outer block's bounds and uses the animated fluid texture of the associated fluid. By default, geysers are associated with lava. The orientation of the outer stone casing is randomised.

> [!note] Implementation
> `GeyserRenderer` seeds a `RandomSource` from the block position and rotates the casing by whole quarter-turns on all three axes (`random.nextInt(4) * PI/2`), giving a stable, position-deterministic random orientation. The block item uses `GeyserItemRenderer` (via `GeyserItem.initializeClient`).

## Implementation

- **Block:** `content/geyser/GeyserBlock` (`Block` + `EntityBlock`), registered as `ModBlocks.GEYSER` (`geyser_block`).
- **Block entity:** `GeyserBlockEntity` — stores `associatedFluid` (default `Fluids.LAVA`), `blastTimer`, `eruptionTimer`; registered as `ModBlockEntities.GEYSER_BE`.
- **Item:** `GeyserItem` (`BlockItem`) with custom `GeyserItemRenderer`.
- **Renderer:** `GeyserRenderer` (BER), registered in `ClientModEvents`.
- **Consumed by:** `FrackingPumpOutletBlockEntity.resolveGeyserFluid` reads `getAssociatedFluid()` to gate fracking recipes.
- **Worldgen:** placement, biomes and structures documented in [[Nether Geyser Worldgen]].

## Related

- [[Fracking Pump]]
- [[Nether Geyser Worldgen]]
- [[Fracking Source Blocks]]
