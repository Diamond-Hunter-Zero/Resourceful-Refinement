---
title: Paint Fluids
category: Fluid
status: Implemented
introduced: v0.2
recipe_type: resourceful_refinement:fluid_refinery
related:
  - "[[Paint Recipes]]"
  - "[[Gel Splatter]]"
  - "[[Hosegun]]"
  - "[[Fluid Properties]]"
  - "[[Paint Nozzle]]"
tags:
  - fluid
  - paint
  - gel
---

Paint fluids are a fluid group representing fluid forms of each of Minecraft's 16 coloured dyes. Each
vanilla dye has a corresponding paint fluid, which matches its tint colour and name. Paint fluids are
crafted in the [[Fluid Refinery]] (see [[Paint Recipes]]), and can be used to dye things with the
[[Hosegun]].

**ID:** `{colour}_paint` (e.g. `red_paint`, `light_blue_paint`)

## Group & Properties

Paint fluids belong to the `PAINT` [[Fluid Properties|fluid group]]: light level 0, density 900,
viscosity 1000, drop rate 2, temperature 300, rendered slightly **translucent** (tint alpha `0xFA`).
They share the `smooth_liquid_still` / `smooth_liquid_flow` textures, tinted per fluid by the
registered colour. Their liquid blocks register under the `paint/<name>` path, and their buckets use a
shared `paint_fluid_bucket` model (a `paint_bucket` underlay plus a tinted `paint_bucket_content`
overlay) rather than a bespoke texture per colour.

## Members

All 16 vanilla dye colours, each mapped to the matching `DyeColor`:

| Fluid | Colour | Fluid | Colour |
| --- | --- | --- | --- |
| `white_paint` | White | `cyan_paint` | Cyan |
| `orange_paint` | Orange | `purple_paint` | Purple |
| `magenta_paint` | Magenta | `blue_paint` | Blue |
| `light_blue_paint` | Light Blue | `brown_paint` | Brown |
| `yellow_paint` | Yellow | `green_paint` | Green |
| `lime_paint` | Lime | `red_paint` | Red |
| `pink_paint` | Pink | `black_paint` | Black |
| `gray_paint` | Grey | `light_gray_paint` | Light Grey |

## Sources & Uses

- **Made in** the [[Fluid Refinery]] from a vanilla dye + water + a little liquid glue — see
  [[Paint Recipes]].
- **Consumed by** the [[Hosegun]] (fired as gel-blobs) and pumped/placed via the [[Paint Nozzle]].

## Behaviour

Paint fluids resolve to the **`PAINT` gel type** (via the `makes_paint_gel` fluid tag). Fired from the
[[Hosegun]] as a gel-blob, a paint blob behaves in one of two ways depending on whether the hosegun
fluid is *gloopy*:

- **Non-gloopy (dye mode):** the blob acts as an instant dyer.
  - **On a block:** it dyes colourable blocks within a large (3-block) radius of the impact to the
    paint's `DyeColor` (`dyeBlocks`).
  - **On an entity:** it dyes dyeable entities — sheep wool, and tamed wolf/cat collars — to the
    paint's colour (`PaintGelCollarHelper.tryDyeEntity`).
- **Gloopy:** the blob instead lays down a coloured [[Gel Splatter]] like any other gel fluid,
  rather than instantly dyeing.

Each paint fluid is mapped to its `DyeColor` at class-load in `GelBlobEntity` (the `PAINT_FLUID_COLORS`
map), so the impact colour always matches the fluid. Paint gel-blobs are rendered as the `paint_blob`
item and produce a large impact splash. Paint is one of two gel types (with `CLEANSE`) that use the
large impact radius.

## Implementation

- **Registration:** the 16 `FluidEntry`s in `registry/ModFluids` (the "Paint Fluids (v0.2)" block),
  all `FluidGroup.PAINT`. Blocks register under `paint/<name>`; buckets share
  `FluidEntry.PAINT_FLUID_BUCKET_MODEL` (`item/paint_fluid_bucket`). `FluidEntry.usesPaintBucketUnderlay()`
  returns true for the group; `data/ModItemModelProvider` special-cases `FluidGroup.PAINT` for the model.
- **Gel type:** `content/gel_splatter/GelType.PAINT` (`"paint"`, tag `makes_paint_gel`);
  radius from `content/gel_splatter/GelImpactConstants` (`PAINT` → large).
- **Impact logic:** `content/hosegun/GelBlobEntity` (`PAINT_FLUID_COLORS`, `onHitBlock`/`onHitEntity`
  `PAINT` cases) and `content/hosegun/PaintGelCollarHelper` (entity dyeing).
- **Colour mapping:** paint fluid `ResourceLocation` → `DyeColor` in the static block of `GelBlobEntity`.

## Related

- [[Paint Recipes]]
- [[Gel Splatter]]
- [[Hosegun]]
- [[Paint Nozzle]]
- [[Fluid Properties]]
