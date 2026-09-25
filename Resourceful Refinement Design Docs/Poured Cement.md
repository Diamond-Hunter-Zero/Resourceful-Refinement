---
title: Poured Cement
category: Fluid
status: Implemented
introduced: v0.2
recipe_type: n/a
related:
  - "[[Fluid Properties]]"
  - "[[Fracking Pump]]"
  - "[[Mechanical Sieve]]"
  - "[[Gel Splatter]]"
tags:
  - fluid
  - concrete
  - worldgen
  - building
---

Poured Cement is the settling stage of the mod's **concrete** fluids: a liquid that solidifies into a
solid block. **Liquid Concrete** is the pumpable liquid form; when it is poured out of a Create
open-ended pipe (or sieved) it becomes **Poured Cement**, a custom liquid block that random-ticks
through a short setting sequence and finally turns into vanilla Light Grey Concrete. This gives an
automatable, Create-native way to cast concrete in place — pipe liquid concrete over a surface and let
it set.

## Group & Properties

Both fluids belong to the `CONCRETE` [[Fluid Properties|fluid group]]: light level 0, density 3000,
viscosity 3000, drop rate 1, temperature 300, fully opaque (tint alpha `0xFF`). The group reuses the
`molten_still` / `molten_flow` textures, tinted per fluid.

## Members

- **Liquid Concrete** (`liquid_concrete`, colour `0x948d83`) — the pumpable liquid. It has no crafting
  recipe; it is a source fluid obtained/pumped in-world (e.g. via the [[Fracking Pump]] / hose pulley)
  and moved through pipes.
- **Poured Cement** (`poured_cement`, colour `0xb4b5a7`) — the settling block, registered with the
  custom `PouredCementBlock` class. Its bucket exists but the block has **no loot table**
  (`ofFullCopy(Blocks.LAVA).noLootTable()`), like the other liquid blocks.

## Sources & Uses

- **Liquid Concrete** is pumped and piped. Pouring it from an open-ended pipe places Poured Cement
  (see Behaviour). It can also be sieved.
- **Sieving:** the [[Mechanical Sieve]] recipe `concrete_sieving` converts **20mb `liquid_concrete` →
  150mb `poured_cement`** (+15% chance of a flint), processing time 10 — a fast way to bulk-convert
  liquid concrete into the settling fluid.
- **Poured Cement** sets into **Light Grey Concrete**, consumable as an ordinary building block once set.

## Behaviour

### Pouring from a pipe

Create's open-ended pipe is patched so that filling it with `liquid_concrete` places Poured Cement at
the pipe outlet instead of spilling a liquid:

- The pipe accumulates liquid concrete internally until it holds a full source's worth
  (`LIQUID_CONCRETE_PER_SOURCE_MB = 125mb`).
- Once ≥125mb has accumulated, it attempts to place a Poured Cement **source block** at the outlet
  position (`PouredCementPlacement.tryPlaceAtPipeOutlet`), consuming the 125mb.
- Placement only succeeds into a replaceable, non-source-fluid space; otherwise the fill is rejected
  and nothing is consumed. Mixing a different fluid into a partially-filled pipe is rejected.

### Setting sequence

`PouredCementBlock` is a `LiquidBlock` with an extra `set_stage` blockstate property (`0..1`) and is
randomly ticking. On each random tick of a **source** block (`LEVEL == 0`):

1. It skips setting if it is stacked directly above another Poured Cement source (so columns settle
   from the bottom up), or if the block below is not a sturdy top face (needs solid support).
2. Otherwise it advances `set_stage` by one; when `set_stage` reaches 1 and ticks again, it replaces
   itself with **`minecraft:light_gray_concrete`**.

So a poured layer visibly "cures" over two random-tick stages before becoming solid Light Grey
Concrete, and only cures where it has settled on solid ground.

### Related gel behaviour

There is also a `CONCRETE` [[Gel Splatter|gel type]] (`makes_concrete_gel`), so concrete fluids fired
from the [[Hosegun]] have gel-side behaviour distinct from this pipe/sieve placement path.

## Implementation

- **Fluid group:** `content/fluids/base/FluidGroup.CONCRETE` (light 0, density 3000, viscosity 3000,
  dropRate 1, temperature 300; `molten_still`/`molten_flow` textures).
- **Registration:** `registry/ModFluids.LIQUID_CONCRETE` and `ModFluids.POURED_CEMENT`; the latter is
  registered with `PouredCementBlock.class` so `FluidEntry` builds its block as a `PouredCementBlock`
  rather than a plain `LiquidBlock`.
- **Block:** `content/fluids/PouredCementBlock` — adds `SET_STAGE` (`IntegerProperty`, 0–1), overrides
  `isRandomlyTicking`/`randomTick` for the two-stage cure into `Blocks.LIGHT_GRAY_CONCRETE`.
- **Placement helper:** `content/fluids/PouredCementPlacement` — `LIQUID_CONCRETE_PER_SOURCE_MB = 125`,
  `shouldPlacePouredCement`, `placementAmountFor`, `fluidToPlace`, `tryPlaceAtPipeOutlet`.
- **Pipe patch:** `mixin/OpenEndedPipeFluidHandlerMixin` injects into Create's
  `OpenEndedPipe$OpenEndFluidHandler.fill` to accumulate liquid concrete and place cement.
- **Sieve recipe:** `data/resourceful_refinement/recipe/mechanical_fluid_sieve/concrete_sieving.json`.
- **Ponder:** `ponders/FluidPonders.liquidConcreteScene` ("Building with Liquid Concrete") demonstrates
  pumping liquid concrete and its conversion to Light Grey Concrete; the liquid_concrete bucket is
  registered in `ModPonders` tags alongside the hose pulley.
- **Gel type:** `content/gel_splatter/GelType.CONCRETE` (tag `makes_concrete_gel`).

## Related

- [[Fluid Properties]]
- [[Fracking Pump]]
- [[Mechanical Sieve]]
- [[Gel Splatter]]
- [[Hosegun]]
