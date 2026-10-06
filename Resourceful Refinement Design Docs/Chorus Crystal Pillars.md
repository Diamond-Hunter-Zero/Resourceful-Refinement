---
title: Chorus Crystal Pillars
category: Worldgen
status: Implemented
introduced: v0.4
recipe_type: n/a
related:
  - "[[Choral Clusters]]"
  - "[[Choral Clusters Worldgen]]"
  - "[[Chorus Crystal]]"
tags:
  - worldgen
  - structure
  - technical
  - tuning
---

The **Chorus Crystal Pillars** are the glowing crystalline spikes that decorate the [[Choral Clusters]] mountains — the vertical, slanted and pointed [[Chorus Crystal]] formations visible across the biome. They are placed procedurally by `ChorusCrystalPillarGenerator`, driven by a swappable **preset** that controls density, size and shape. This page covers both the content-tuning surface (presets) and the technical placement algorithm.

Java: `worldgen/choral/ChorusCrystalPillarGenerator.java`, `ChorusCrystalPillarPreset.java`, and the preset constants in `ChoralClusterGenerationSettings.java`.

## How It Runs

The generator is invoked **per chunk** from `ChoralClusterMountainPiece.postProcess`, after the [[Choral End Stone|mountain terrain]] for that chunk is laid down (see [[Choral Clusters Worldgen]]). Each invocation:

1. Rolls a pillar **count** from the active preset (`minCount … maxCount`).
2. For each pillar, picks a random column in the chunk (local X/Z 0–15) and reads the `WORLD_SURFACE` heightmap for its Y.
3. **Rejects** the site if `y < 48` or `y > maxBuildHeight − 8`, or if the block directly below the surface is not an anchor (`end_stone`, `choral_end_stone`, or `chorus_crystal`).
4. Rolls the pillar **type** by weight and places it.

> [!note] Implementation
> Pillars are clipped to the **current chunk**: `placeHexSlice` skips any x/z outside 0–15. Slanted and long pillars that would cross a chunk border are cut off at the seam rather than continuing into the neighbour. This is the main visual artefact to be aware of when tuning longer/flatter presets. Making pillars span chunks would require writing into neighbouring chunks (not currently done).

## Pillar Types

The roll is weighted across three shapes (`verticalWeight : slantedWeight : pointedWeight`, normalised by `totalWeight()`):

- **Vertical** — a straight column pointing up `(0, 1, 0)`, untapered.
- **Slanted** — a column leaning off vertical by `minSlantDegrees … maxSlantDegrees` at a random yaw, untapered.
- **Pointed** — a slanted column (randomly pointing up *or* down) that **tapers to a point** over its length (`taperStrength`).

### Shape construction

`placePillar` walks from `−rootDepth` to `ceil(length)` steps along the direction normal, placing a cross-section "slice" at each step:

- **radius** = `minRadius … maxRadius`; **length** = `minLength … maxLength`.
- **Monumental roll:** with probability `monumentalChance × 0.12`, radius ×1.45 and length ×1.55 (rare oversized spires).
- **rootDepth** extends the pillar *backwards* into the terrain so it reads as rooted rather than floating.
- **Taper** (pointed only): slice radius = `radius × max(0.15, 1 − progress^1.25 × taperStrength)`.
- `placeHexSlice` stamps a hexagon-ish disc (`dx + dz·0.62 ≤ radius·1.18` and `max(dx,dz) ≤ radius`, with a vertical band of `max(0.75, radius·0.55)`), replacing air / end stone / cave air / [[Choral End Stone]] / [[Chorus Crystal]].

The hex cross-section (rather than a circle) is what gives the spires their faceted, crystalline silhouette.

## Presets (content tuning)

All presets are `ChorusCrystalPillarPreset` records in `ChoralClusterGenerationSettings`. Swap the active one via:

```java
public static final ChorusCrystalPillarPreset ACTIVE_PRESET = MODEST;
```

> [!note] Implementation
> `ACTIVE_PRESET` is currently **`MODEST`** — the sparsest, nearly-all-vertical preset (0–1 pillars per chunk). The other four presets are defined and ready but **not live**; the dense/dramatic fields seen in the exploration screenshots correspond to the heavier presets. Changing the active preset is a one-line edit; there is no in-game/config switch yet (the class comment flags these as iteration constants to be mirrored into `ServerConfig` once the style settles).

Record fields, in order: `name, minCount, maxCount, minRadius, maxRadius, minLength, maxLength, verticalWeight, slantedWeight, pointedWeight, minSlantDegrees, maxSlantDegrees, taperStrength, rootDepth, monumentalChance`.

| Preset | Count/chunk | Radius | Length | V : S : P weights | Slant° | Taper | Root | Monumental |
|---|---|---|---|---|---|---|---|---|
| **MODEST** *(active)* | 0–1 | 1.0–4.0 | 4–15 | 0 : 0.1 : 0.9 | 12–40 | 0.52 | 4 | 0.15 |
| BALANCED | 5–10 | 1.0–3.25 | 10–42 | 0.44 : 0.36 : 0.20 | 8–38 | 0.52 | 4 | 0.42 |
| DENSE_SPIRES | 9–16 | 0.8–2.4 | 14–54 | 0.68 : 0.18 : 0.14 | 5–24 | 0.72 | 5 | 0.56 |
| WIDE_ASTEROIDS | 4–8 | 2.0–5.0 | 8–34 | 0.52 : 0.30 : 0.18 | 6–28 | 0.35 | 3 | 0.34 |
| CHAOTIC_SHARDS | 7–13 | 1.0–3.6 | 16–66 | 0.22 : 0.52 : 0.26 | 18–58 | 0.86 | 6 | 0.70 |

Reading the presets:
- **MODEST** — note the `0` vertical weight: with only slant(0.1)+pointed(0.9) weights, nearly every pillar is a *pointed* spike, mostly short. Sparse scattering.
- **DENSE_SPIRES** — many thin, tall, mostly-vertical columns — a crystal "forest".
- **WIDE_ASTEROIDS** — fewer, fatter, shorter chunks — bulky crystal masses.
- **CHAOTIC_SHARDS** — the wild one: long, steeply-slanted, heavily-tapered shards at up to 58° off vertical with a high monumental rate.

## Open Tuning / Polish Surface

- **Active preset** — pick/author the shipping look; consider exposing it (and count/size) through `ServerConfig`.
- **Chunk clipping** — longest-standing artefact for long/slanted presets (see note above).
- **Anchor/height gates** — `y < 48` floor and the anchor-block check determine where spires can root; loosening them changes coverage on mound flanks vs peaks.
- **Monumental multiplier** — the `× 0.12` factor makes the preset's `monumentalChance` much rarer than its face value; adjust if "hero" spires should be more common.

## Related

- [[Choral Clusters]] · [[Choral Clusters Worldgen]] · [[Chorus Crystal]] · [[Choral End Stone]]
