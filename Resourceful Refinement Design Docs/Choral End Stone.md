---
title: Choral End Stone
category: Worldgen
status: Implemented
introduced: v0.4
recipe_type: n/a
related:
  - "[[Choral Clusters]]"
  - "[[Choral Clusters Worldgen]]"
  - "[[Chorus Crystal]]"
tags:
  - block
  - worldgen
---

**Choral End Stone** is the terrain block the [[Choral Clusters]] mountains are built from — the modded End-stone variant that the `choral_cluster_mountain` structure piles up into mounds before the [[Chorus Crystal Pillars]] are seeded on top.

**ID:** `choral_end_stone` (block + item)

## Role

It is a near-pure reskin/clone of vanilla End stone used as a **surface skin**: the [[Choral Clusters Worldgen|mountain generator]] builds the mound body out of vanilla `end_stone` and paints Choral End Stone only onto the **exposed top surface layer**, in naturalistic noise-driven patches (`ChoralClusterNoise.isSurfacePatch`). The anchor/replace checks in both the terrain build and the [[Chorus Crystal Pillars|pillar generator]] treat `choral_end_stone` as valid ground to build on and over.

> [!note] Implementation
> Surface patch coverage/shape is tuned by `SURFACE_PATCH_NOISE_SCALE`, `SURFACE_PATCH_DETAIL_SCALE`, `SURFACE_PATCH_THRESHOLD` and `SURFACE_PATCH_SEED` in `ChoralClusterGenerationSettings`; `SURFACE_LAYER_DEPTH` (default 1) sets how many exposed blocks a patch paints down. `SURFACE_PATCH_SEED` is now a **salt XORed with the world seed**, so the blotch layout is deterministic per seed (and continuous within a world), not identical across all worlds.

## Block

Registered as `ModBlocks.CHORAL_END_STONE` from `BlockBehaviour.Properties.ofFullCopy(Blocks.END_STONE)` with an explicit `strength(3.0f, 9.0f)` and `requiresCorrectToolForDrops()` — i.e. the same hardness/blast resistance as vanilla End stone (the `strength` call just restates the inherited values). It is a plain `Block` (no block entity, no custom class), with a simple block item.

## Model & Tint

- **Model:** a Blockbench `cutout` model with its own texture `block/choral_end_stone` (a grass-like top). The **up face plus the four slightly-inset side overlay faces carry `tintindex: 0`**; the bottom face is untinted. This gives the block a tinted "grassy" cap and fringe over a plain base.
- **Tint:** a **world-position purple/pink noise palette** — `ChoralEndStoneColors.blockTint` (block) / `itemTint` (item), tuned in `ChoralEndStoneColorSettings`. A two-octave value noise (`ChoralClusterNoise.valueNoise`, scales `NOISE_SCALE` / `DETAIL_NOISE_SCALE`) selects a phase along a looping `COLOR_KEYS` gradient of purples and pinks. The `NOISE_SEED` salt is **XORed with a per-world seed** (`ChoralClientSeed` — the biome-zoom seed, available client-side), so the blotch palette layout is seed-driven and deterministic per world. Item stacks have no world position/seed, so they use the fixed `ITEM_PHASE`.

> [!note] Implementation
> The tint was **changed from the biome grass-colour map** (`BiomeColors.getAverageGrassColor`) to this self-contained palette. The grass-average approach blended the choral biome's pink `grass_color` with the **colour-map green of neighbouring vanilla End biomes**, producing an unwanted purple↔green gradient along the (noise-fragmented) biome borders. The noise palette has no biome dependency, so it never pulls toward that green. Non-zero tint indices return `-1` (no tint). See [[Chorus Crystal]] for the sibling crystal tint system this mirrors.

## Acquisition & Products

- **Source:** mined from [[Choral Clusters]] mountain terrain.
- Drops itself; no recipes consume or produce it in this merge.

## Open Tuning / Polish Surface

- **Palette** — `COLOR_KEYS` in `ChoralEndStoneColorSettings` (add/reorder keys), plus `NOISE_SCALE` / `DETAIL_NOISE_SCALE` for blotch size, `ITEM_PHASE` for the inventory colour, and `TINT_INTENSITY` / `BRIGHTNESS_MULTIPLIER`.
- Could gain variants (e.g. a crystal-infused or emissive edge block) if the biome's surface palette is expanded.

## Related

- [[Choral Clusters]] · [[Choral Clusters Worldgen]] · [[Chorus Crystal]] · [[Chorus Crystal Pillars]]
