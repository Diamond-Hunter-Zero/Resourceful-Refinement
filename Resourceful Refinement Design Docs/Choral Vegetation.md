---
title: Choral Vegetation
category: Worldgen
status: Implemented
introduced: v0.4
recipe_type: n/a
related:
  - "[[Choral Clusters]]"
  - "[[Choral Clusters Worldgen]]"
  - "[[Chorus Crystal]]"
  - "[[Choral End Stone]]"
tags:
  - worldgen
  - feature
  - datapack
  - reference
---

The decorative scatter life of the [[Choral Clusters]] biome: the amethyst-cluster-style **`chorus_crystal_growth`** and the grass-like **`choral_sprouts`**. Both are ordinary **biome placed-features** in the `VEGETAL_DECORATION` step (unlike the structural terrain/crystal, which is built in the [[Choral Clusters Worldgen|structure]] at `raw_generation`), so they decorate terrain and crystal that already exist. All parameters are datapack-tweakable.

## chorus_crystal_growth

A 6-way rotatable [[Chorus Crystal]]-family decorative cluster (an `AmethystClusterBlock`, so it orients to the face it grows from). It uses one small **custom feature** `resourceful_refinement:attached_growth`, wired up **twice** so terrain and crystal behave differently:

- **Surface** (`chorus_crystal_growth_surface`): attaches only to the **top** of `#resourceful_refinement:choral_terrain` (end-stone / choral-end-stone), i.e. `attach_directions: ["down"]` → always faces **up**, grass-like. Never on terrain cliff faces.
- **Crystal** (`chorus_crystal_growth_crystal`): attaches to any exposed face of `chorus_crystal` (default `attach_directions` = all six) → up, down, or sideways on crystal formations.

How the feature works: for each of `tries` attempts it picks a random air cell in a box around the origin, checks the `attach_directions` for a neighbour in the `attach_to` set, and places the growth with `facing` pointing **away** from that neighbour (support below → faces up; crystal to the east → faces east; ceiling above → faces down). A `canSurvive` check is applied first, so growths only sit on sturdy faces.

**Config fields** (in the configured-feature JSON):

| Field | Surface value | Crystal value | Meaning |
|---|---|---|---|
| `growth` | `chorus_crystal_growth` | `chorus_crystal_growth` | Block state to place (`facing` overridden per face). |
| `attach_to` | `#…:choral_terrain` | `[…:chorus_crystal]` | Block list/tag the growth may attach to. |
| `attach_directions` | `["down"]` | *(omitted → all 6)* | Allowed support directions (growth→support). `down` = grow on top only. |
| `tries` | 64 | 64 | Scatter attempts per placement. |
| `xz_spread` | 7 | 7 | Horizontal radius of the scatter box. |
| `y_spread` | 6 | 20 | Vertical radius (small for the flat surface; large to reach up tall pillars). |

**Placed features**: both use `in_square` + `heightmap MOTION_BLOCKING` + `biome`, with `count 6` (surface) / `count 4` (crystal) origins per chunk. Overall density ≈ `count × tries × hit-rate`; raise `count` for denser coverage. (Crystal-face coverage is also bounded by how much [[Chorus Crystal Pillars|crystal]] exists — sparse under the `MODEST` pillar preset.)

> [!warning] Heightmap must be live, not `_WG`
> These features **must** anchor on `MOTION_BLOCKING` (or `WORLD_SURFACE`), **not** `WORLD_SURFACE_WG`. The choral mountains are built by the [[Choral Clusters Worldgen|structure]] during the `FEATURES` chunk status (at `raw_generation`), which only updates the non-`_WG` heightmaps. `WORLD_SURFACE_WG` is frozen at the earlier noise/surface step, i.e. the *original island surface before the mountains were piled on* — anchoring there buries every placement inside solid rock, so almost nothing generates. This bit both these features on first pass.

## choral_sprouts

A non-rotatable grass-like block (a plain `Block` copied from `short_grass`: no collision, XZ offset, replaceable). Scattered on terrain only.

- **Configured feature** `worldgen/configured_feature/choral_sprouts.json`: vanilla `minecraft:simple_block` placing `choral_sprouts`.
- **Placed feature** `worldgen/placed_feature/choral_sprouts.json`: `count 24`, `in_square`, `heightmap MOTION_BLOCKING`, then a `block_predicate_filter` requiring the target to be `replaceable` and the block below to be in `#resourceful_refinement:choral_terrain` (`end_stone`, `choral_end_stone`), then `biome`.
- `count` is the density knob. The predicate keeps sprouts off crystal and off anything that isn't choral terrain.

> [!note] Implementation
> `choral_sprouts` is a plain `Block`, so it has no `canSurvive` soil requirement — the `block_predicate_filter` is what restricts it to choral terrain. `chorus_crystal_growth` *does* have a cluster `canSurvive`, enforced in the custom feature. Both features live in the biome's `VEGETAL_DECORATION` list alongside `sparse_chorus_plant`; none is referenced by any other biome, so there's no feature-ordering cycle.

## Tuning Cheat-Sheet

- **More/less surface growth, crystal growth, or sprouts** → `count` in the respective placed feature (`chorus_crystal_growth_surface` / `chorus_crystal_growth_crystal` / `choral_sprouts`).
- **Denser clumps per spot** → `tries` (and `xz_spread` / `y_spread` for reach) in the configured feature.
- **Change which faces growth takes** → `attach_directions` in a configured feature (e.g. add `"up"` to let surface growth also hang under terrain overhangs).
- **What surface growth / sprouts grow on** → edit `#choral_terrain`.
- **What crystal growth clings to** → the `attach_to` list in `chorus_crystal_growth_crystal.json` (add more crystal block ids).
- A `/reload` applies configured/placed-feature and tag edits to newly generated chunks.

## Files

- `worldgen/choral/feature/AttachedGrowthFeature.java`, `AttachedGrowthConfiguration.java` — the custom feature + config/codec.
- `registry/ModFeatures.java` — registers `attached_growth` on `Registries.FEATURE`.
- `worldgen/configured_feature/` + `worldgen/placed_feature/` — `chorus_crystal_growth_surface.json`, `chorus_crystal_growth_crystal.json`, `choral_sprouts.json`.
- `tags/block/choral_terrain.json`.
- `worldgen/biome/choral_clusters.json` — the three features in `VEGETAL_DECORATION`.
- Blockstate `chorus_crystal_growth.json` — 6-way `FACING` with the 3 random models (see note below).

> [!note] Implementation
> `chorus_crystal_growth`'s blockstate randomises between three models per placement and rotates them for all six `FACING` values (vanilla amethyst-cluster rotations). The custom feature sets `FACING`; the blockstate supplies the matching rotated model.

## Related

- [[Choral Clusters]] · [[Choral Clusters Worldgen]] · [[Chorus Crystal]] · [[Choral End Stone]]
