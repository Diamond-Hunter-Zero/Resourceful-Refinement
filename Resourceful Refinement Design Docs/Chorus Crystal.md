---
title: Chorus Crystal
category: Worldgen
status: Implemented
introduced: v0.4
recipe_type: n/a
related:
  - "[[Choral Clusters]]"
  - "[[Chorus Crystal Pillars]]"
  - "[[Choral End Stone]]"
tags:
  - block
  - worldgen
  - rendering
---

The **Chorus Crystal** is the glowing, translucent crystalline block that makes up the [[Chorus Crystal Pillars|spikes and pillars]] of the [[Choral Clusters]] biome. Its defining feature is a **world-position colour gradient** that bands the crystal through shifting hues — and swaps palette by dimension — so a single untinted texture reads as a living, iridescent mineral.

**ID:** `chorus_crystal` (block + item)

## Block

Registered as `ModBlocks.CHORUS_CRYSTAL` from `BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_BLOCK)` with:

- `strength(2.0f, 6.0f)`, `sound(SoundType.AMETHYST)`, `requiresCorrectToolForDrops()`.
- `lightLevel = 10` — the crystals glow.
- `noOcclusion()`, and `isRedstoneConductor`, `isSuffocating`, `isViewBlocking` all forced **false** (translucent, non-solid-feeling).
- Class `ChorusCrystalBlock extends Block` overrides `skipRendering` so a face **touching another Chorus Crystal is culled**. Large masses only render their outer shell, which is essential given how much crystal the [[Chorus Crystal Pillars|pillar generator]] places.

### Model & render

- Blockstate: single default variant → `block/chorus_crystal`.
- Model: a full cube (`block/block` parent) with `render_type: translucent`, every face `tintindex: 0`, texture `block/chorus_crystal_block` (a 16×16 near-white texture meant to be tinted).
- Render layer forced to `RenderType.translucent()` in `ClientModEvents` (alongside the block's own model `render_type`).

## World-Position Colour System

The tint is **not** a block state — it is computed from world coordinates at render time, giving smooth colour bands across whole formations. Logic in `ChorusCrystalColors`, tuning in `ChorusCrystalColorSettings`.

### Gradient phase

```
phaseBlocks = x·X_AXIS_SLOPE + y + z·Z_AXIS_SLOPE + PHASE_OFFSET
colour      = gradient( phaseBlocks / PERIOD_BLOCKS )
```

Defaults: `PERIOD_BLOCKS = 20` (bands repeat every ~20 vertical blocks), `X_AXIS_SLOPE = 0.36`, `Z_AXIS_SLOPE = −0.22` (bands slant diagonally), `PHASE_OFFSET = 0`. The fractional phase indexes a looping list of `ColorKey(position, color)` stops, smoothstep-interpolated.

In addition to `PHASE_OFFSET`, the block tint adds a **per-world phase offset** (`ChoralClientSeed.seedPhase(level)`, derived from the biome-zoom seed) so the colour banding is seed-driven and deterministic per world rather than identical everywhere. Item stacks (no world/seed) use the fixed `ITEM_PHASE`.

After sampling, `TINT_INTENSITY = 0.95` blends from white toward the keyed colour and `BRIGHTNESS_MULTIPLIER = 1.18` lifts the result for a bright crystalline glow. Any `tintIndex != 0` returns opaque white (no tint).

### Per-dimension palettes

`colorAtPhase` picks the palette from the block's dimension:

| Context | Palette | Character |
|---|---|---|
| **The End** | `COLOR_KEYS` | Magenta → pink → violet → blue (the biome's signature look in the screenshots). |
| **Nether** | `NETHER_COLOR_KEYS` | Orange → deep red → amber. |
| **Overworld / other** | `RAINBOW_COLOR_KEYS` | Full teal→blue→purple→red→orange→yellow→green rainbow. |
| **Item / inventory** | `COLOR_KEYS` at fixed `ITEM_PHASE = 0.18` | A representative End-palette colour (no world position to sample). |

So the same block placed in different dimensions looks materially different — an End-native crystal reads purple/pink, but a player who hauls it to the Nether or Overworld gets the warm or rainbow palettes.

> [!note] Implementation
> `getLevelSafely` works around **Sodium/Embeddium**: their `LevelSlice` is not a `Level`, so the tinter reflectively locates the `Level`-typed field inside the slice (cached after first lookup) to recover the dimension. If it can't (null level), it falls back to the End `COLOR_KEYS` palette. The block tint is registered via `RegisterColorHandlersEvent.Block` and the item tint via `RegisterColorHandlersEvent.Item` in `ClientModEvents`.

## Acquisition & Products

- **Source:** mined from [[Chorus Crystal Pillars]] in the [[Choral Clusters]] biome (`requiresCorrectToolForDrops`). No crafting recipe or processing chain ships in this merge.
- **Intended downstream:** the [[Primary Design Doc]] frames Chorus Crystal as the raw material behind **Liquid Chorus**, which fuels the [[Remote Entanglement]] transporter — that processing is not part of this feature yet.

> [!note] Implementation
> This merge adds the block, its tinting and its worldgen only. There is **no** Chorus Crystal → fluid/item processing recipe, and the block currently drops itself. The extraction-node relationship (Chorus Crystal Fissures feeding a [[Drill Pylon]]) lives on the separate [[Crystal Fissure Bud]] block and is **not yet generated** inside the Choral Clusters — see [[Choral Clusters]].

## Open Tuning / Polish Surface

- `ChorusCrystalColorSettings` — band period/slope, tint intensity/brightness, and the three palettes (add/reorder `ColorKey`s freely).
- Texture `chorus_crystal_block.png` — currently a flat near-white tile; any internal detail multiplies against the gradient.
- No block-entity, so the colour is purely positional; an animated/time-based shimmer would need a different hook.

## Related

- [[Choral Clusters]] · [[Chorus Crystal Pillars]] · [[Choral Clusters Worldgen]] · [[Choral End Stone]]
- [[Crystal Fissure Bud]] · [[Remote Entanglement]] · [[Primary Design Doc]]
