---
title: Crystal Fissure Bud Tints
category: Framework
status: Implemented
introduced: v0.4
recipe_type: n/a
related:
  - "[[Crystal Fissure Bud]]"
  - "[[Drill Pylon]]"
  - "[[Chorus Crystal]]"
tags:
  - rendering
  - datapack
  - block
  - reference
---

The [[Crystal Fissure Bud]] is rendered with a **whole-block tint that animates through a colour array over client game-time**. Which colours it cycles depends on the **source item stored in the bud**, and the colour profiles are **data-driven via a synced datapack registry**, so modpack makers can add their own without touching code. A hardcoded fallback guarantees every bud always has a profile.

This page is the reference for the system: how it renders, the datapack format, and the lookup rules.

## How It Renders

A static JSON-model block tint is baked into the chunk mesh and only re-evaluates on block/neighbour updates — so it **cannot animate**. To get a per-frame colour cycle:

- The block's `getRenderShape` is **`ENTITYBLOCK_ANIMATED`**, which keeps the static model out of the chunk mesh (no double-render).
- `CrystalFissureBudRenderer` (a `BlockEntityRenderer`, registered in `registerRenderers`) draws the baked model every frame via `ModelBlockRenderer.renderModel(...)`, multiplying it by the current cycle colour.
- Every face of the model (`block/chorus/crystal_fissure_bud.json`) carries **`tintindex: 0`** — `renderModel` only tints tint-indexed quads, so without this the colour would do nothing. The texture (`fissure_crystal_bud_tintable`) is greyscale so the tint reads cleanly.
- The **item form** (inventory/hand) is tinted by an `ItemColor` handler that also reads game-time, so a loose bud stack animates too — using the *default* profile, since a loose stack has no stored source item.

> [!note] Implementation
> `CrystalFissureBudRenderer` draws into `RenderType.translucent()` **explicitly**. It does *not* use `ItemBlockRenderTypes.getRenderType(state, …)` — that returns the block's chunk-render-layer map value, which defaults to `SOLID` (opaque) unless `setRenderLayer` was called, and the model's JSON `render_type` only affects chunk rendering (skipped under `ENTITYBLOCK_ANIMATED`). So the BER must pick the blending render type itself for the tintable texture's alpha to show. The BE already stores and syncs its `sourceItem`, so the renderer just reads `be.getSourceItem()`.

## The Colour Cycle

`FissureTintProfile.colorAt(timeTicks)` lerps between consecutive colours and loops:

- `timeTicks` = `level.getGameTime() + partialTick` (client game-time; pauses with the game).
- Each segment lasts `seconds_per_color` (× 20 ticks); the interpolation is eased with a smoothstep.
- 0 colours → white; 1 colour → static (no animation).

## Datapack Format

Profiles live in the **synced datapack registry** `resourceful_refinement:fissure_bud_tints`. Because the registry is namespaced by the mod, its folder is namespaced too (NeoForge convention):

```
data/<yourpack>/resourceful_refinement/fissure_bud_tints/<name>.json
```

Each file is one profile:

```json
{
  "colors": ["#f0f0f0", "#c7cfd8", "#9aa4b2", "#c7cfd8"],
  "seconds_per_color": 3.5,
  "source_items": ["minecraft:raw_iron", "minecraft:iron_ingot"],
  "fallback": false
}
```

| Field | Type | Default | Meaning |
|---|---|---|---|
| `colors` | array of hex strings (`"#RRGGBB"` / `"0xRRGGBB"` / `"RRGGBB"`) or ints | **required** | The colours to cycle through, in order. Alpha is ignored. |
| `seconds_per_color` | float | `3.0` | Seconds spent lerping from one colour to the next. |
| `source_items` | array of item ids | `[]` | Items this profile applies to when stored in a bud. |
| `fallback` | boolean | `false` | Marks this profile as the default for any stored item without its own profile. |

A profile with no `source_items` and `fallback: true` is the datapack default. One source item appearing in multiple profiles resolves to the last loaded (undefined order — don't rely on it).

### Shipped defaults

`resourceful_refinement` ships, under `data/resourceful_refinement/resourceful_refinement/fissure_bud_tints/`: `default.json` (the `fallback` purple/pink cycle), plus `gold.json` (`gold_ingot`/`raw_gold`), `iron.json` (`iron_ingot`), `diamond.json`, `copper.json` as per-item examples.

> [!note] Implementation
> The Crystal Fissure Bud BE defaults its stored source to `minecraft:raw_iron`. `raw_iron` is deliberately **not** mapped by any shipped profile, so a freshly placed / unconfigured bud uses the purple/pink fallback — the crystal's signature look — rather than the grey `iron_ingot` cycle. Map `raw_iron` in a datapack if you want default buds to take an ore-specific colour.

## Lookup & Precedence

For a bud storing item *X* (`FissureTintProfiles.get(X, registryAccess)`), highest priority first:

1. A registry profile whose `source_items` contains *X*.
2. The registry's `fallback` profile (if any datapack provides one — the shipped `default.json` does).
3. The hardcoded `FissureTintProfiles.FALLBACK` (purple/pink) — used only if the registry is unavailable or defines no fallback.

The item→profile map is cached and rebuilt whenever the backing registry instance changes (datapack `/reload`, world change). Because the registry is **synced** (a network codec was supplied at `DataPackRegistryEvent.NewRegistry`), server-defined profiles reach every client for rendering; no manual packets.

## Files

- `content/drill_pylon/tint/FissureTintProfile.java` — record + codec (`colors`, `seconds_per_color`, `source_items`, `fallback`) + `colorAt`.
- `content/drill_pylon/tint/FissureTintProfiles.java` — `REGISTRY_KEY`, hardcoded `FALLBACK`, cached item→profile lookup.
- `content/drill_pylon/CrystalFissureBudRenderer.java` — the BER.
- `ResourcefulRefinementMain` — registers the datapack registry (`registerDatapackRegistries`), the BER, and the item colour.
- `CrystalFissureBudBlock` — `RenderShape.ENTITYBLOCK_ANIMATED`.
- `models/block/chorus/crystal_fissure_bud.json` — all faces `tintindex: 0`.

## Open Tuning / Polish Surface

- **Timing/palettes** — per-profile `seconds_per_color` and `colors`; add datapack files for more items.
- **Per-state glow** — currently lit by block light; a time-based brightness pulse could be layered into `colorAt` if desired.
- **Translucency** — the BER draws into `RenderType.translucent()`, so the look depends on the `fissure_crystal_bud_tintable` texture's alpha. For a hard-edged opaque crystal, switch the BER to `RenderType.cutout()` instead.

## Related

- [[Crystal Fissure Bud]] · [[Drill Pylon]] · [[Chorus Crystal]]
