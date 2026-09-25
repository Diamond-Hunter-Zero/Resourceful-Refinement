---
title: Fluid Properties
category: Fluid
status: Implemented
introduced: v0.1
recipe_type: n/a
related:
  - "[[Codebase Overview]]"
  - "[[Paint Fluids]]"
  - "[[Fluid Processing Recipes]]"
  - "[[Fluid Refinery]]"
  - "[[Poured Cement]]"
tags:
  - fluid
  - framework
---

Many of the fluids added by this mod fall into distinct **fluid groups**, which share similar
properties and assets, and represent similar stages in progression. Every fluid is registered through
a single `FluidEntry` that is tagged with one `FluidGroup`; the group supplies the fluid type's
density, viscosity, temperature, light level, drop rate and still/flowing textures, so all members of
a group look and behave alike. Each group and its members are described below.

## Group & Properties

The table below lists the properties actually applied per group in code (`FluidGroup.java`). All fluids
are built on `GeneralizedFlowingFluid` / `GeneralizedFluidType`, share Lava's block behaviour
(`ofFullCopy(Blocks.LAVA)`, no loot table) and cannot be extinguished (`canExtinguish(false)`).

| Group | Light | Density | Viscosity | Drop rate | Temperature | Still / flow texture base | Tint alpha |
| --- | --- | --- | --- | --- | --- | --- | --- |
| RAW | 10 | 3000 | 3000 | 2 | 1300 | `raw_still` / `raw_flow` | translucent (`0xFA`) |
| CATALYSED | 8 | 2000 | 1500 | 2 | 800 | `molten_still` / `molten_flow` | translucent (`0xFA`) |
| ALLOYED | 8 | 2000 | 2500 | 2 | 1000 | `smooth_liquid_still` / `smooth_liquid_flow` | opaque (`0xFF`) |
| PURIFIED | 8 | 2000 | 4000 | 1 | 1200 | `template_fluid_still` / `template_fluid_flow` | opaque (`0xFF`) |
| CARBORAX | 0 | 900 | 1000 | 1 | 300 | `smooth_liquid_still` / `smooth_liquid_flow` | translucent (`0xFA`) |
| PAINT | 0 | 900 | 1000 | 2 | 300 | `smooth_liquid_still` / `smooth_liquid_flow` | translucent (`0xFA`) |
| CONCRETE | 0 | 3000 | 3000 | 1 | 300 | `molten_still` / `molten_flow` | opaque (`0xFF`) |
| DRINK | 0 | 900 | 1000 | 1 | 300 | `smooth_liquid_still` / `smooth_liquid_flow` | opaque (`0xFF`) |

> [!note] Implementation
> The original design listed a per-group **flow distance (`getAmount()`)** of RAW 5, CATALYSED 6,
> ALLOYED 7, PURIFIED 6 and CARBORAX 8. There is no explicit flow-distance field on `FluidGroup`;
> instead each group carries a **drop rate** (`RAW/CATALYSED/ALLOYED/PAINT = 2`, `PURIFIED/CARBORAX/
> CONCRETE/DRINK = 1`), which `GeneralizedFlowingFluid` uses to control how quickly a flowing column
> loses level. Treat the flow-distance numbers as design intent and the drop rate as the shipped
> behaviour. Density, viscosity and light level in the table above all match the original design values.

## Members

### Raw Molten Minerals (`RAW`)
Raw minerals in molten form, obtained from geysers or smelting/mixing raw ore.

- Molten Crimsite (`molten_crimsite`)
- Molten Ochrum (`molten_ochrum`)
- Molten Veridium (`molten_veridium`)
- Molten Scorchia (`molten_scorchia`)
- Molten Asurine (`molten_asurine`)

### Catalysed Fluids (`CATALYSED`)
Fluids which have undergone some form of prior processing, typically the first refinery step.

- Catalysed Iron (`catalysed_iron`)
- Catalysed Gold (`catalysed_gold`)
- Catalysed Copper (`catalysed_copper`)
- Catalysed Zinc (`catalysed_zinc`)
- Catalysed Redstone (`catalysed_redstone`)
- Catalysed Sparkpowder (`catalysed_sparkpowder`) — cast into glowstone/gunpowder; the redstone-line
  analogue for the glow/spark chain. *(Added since v0.1; earlier docs called this "catalysed_sparkdust".)*

> [!note] Implementation
> Older docs listed **Catalysed Carborax** as a member of this group. In code it belongs to the
> `CARBORAX` group, not `CATALYSED` — see below.

### Alloyed Fluids (`ALLOYED`)
Fluids comprised of multiple ingredients, often used as inputs into other recipes.

- Silica Substrate (`silica_substrate`)
- Molten Andesite Blend (`molten_andesite_blend`)
- Molten Brass Blend (`molten_brass_blend`)
- Molten Netherite Blend (`molten_netherite_blend`)
- Durasteel Alloy (`durasteel_alloy`)
- Liquid Glue (`liquid_glue`) — sticky intermediate; feeds paint recipes and Forge Mould glue
  products. *(Added since v0.1.)*
- Coolant (`coolant`) — cold utility fluid used by the chilling/cooling systems. *(Added since v0.1.)*
- Organic Slush (`organic_slush`) — biomass intermediate distilled into polymer sludge / compacted
  biomatter. *(Added since v0.1.)*
- Polymer Sludge (`polymer_sludge`) — refined organic feedstock for coolant and carborax refining.
  *(Added since v0.1.)*

### Purified Fluids (`PURIFIED`)
Fluids which represent the end of their processing trees.

- Purified Iron (`purified_iron`)
- Purified Gold (`purified_gold`)
- Purified Copper (`purified_copper`)
- Purified Zinc (`purified_zinc`)
- Purified Durasteel (`purified_durasteel`)

### Carborax Fluids (`CARBORAX`)
Fluids which have a burn time, like lava, and can be used as fuel sources.

- Unrefined Carborax (`unrefined_carborax`)
- Catalysed Carborax (`catalysed_carborax`)
- Overcharged Carborax (`overcharged_carborax`)
- Carborax Diesel (`carborax_diesel`)

### Concrete Fluids (`CONCRETE`)
Heavy, dark liquids that solidify into a solid block when they settle on a sturdy surface. See
[[Poured Cement]] for the full solidification behaviour.

- Liquid Concrete (`liquid_concrete`) — the pumpable liquid form; pouring it from an open-ended pipe
  places Poured Cement.
- Poured Cement (`poured_cement`) — the settling stage that random-ticks into vanilla Light Grey
  Concrete. Uses the custom `PouredCementBlock` liquid block.

### Drink Fluids (`DRINK`)
Consumable/flavour fluids produced mainly by the [[Distillery]].

- Hot Chocolate (`hot_chocolate`)
- Liquid Syrup (`liquid_syrup`)
- Mead (`mead`)
- Liquid Spirits (`liquid_spirits`)

### Paint Fluids (`PAINT`)
Fluid forms of Minecraft's 16 vanilla dye colours, used with the [[Hosegun]] to dye blocks and
entities. Documented in full on [[Paint Fluids]] and [[Paint Recipes]]. The 16 members are:
`white_paint`, `orange_paint`, `magenta_paint`, `light_blue_paint`, `yellow_paint`, `lime_paint`,
`pink_paint`, `gray_paint`, `light_gray_paint`, `cyan_paint`, `purple_paint`, `blue_paint`,
`brown_paint`, `green_paint`, `red_paint`, `black_paint`.

## Sources & Uses

- **Raw** fluids come from geysers, the [[Fracking Pump]] and mixing raw ore with lava; they feed the
  refinery catalysation chain.
- **Catalysed / Alloyed / Purified** fluids are produced and consumed across the [[Fluid Refinery]],
  [[Forge Mould]] / [[Casting Depot]] and [[Mechanical Sieve]] — see [[Fluid Processing Recipes]].
- **Carborax** fluids act as fuels (burn time like lava) and drive the [[Combustion Chamber]].
- **Concrete** fluids are pumped and poured to build. **Drink** fluids come from the [[Distillery]].
- **Paint** fluids are made in the refinery and fired from the [[Hosegun]].

## Behaviour

Most groups behave as ordinary (non-extinguishable) liquids differing only in the tuned
density/viscosity/light/temperature above. Group-specific behaviour:

- **CARBORAX** members carry a burn time and can be consumed as fuel.
- **CONCRETE** members solidify — see [[Poured Cement]].
- **PAINT** members map to a `DyeColor` and dye blocks/entities via the [[Hosegun]] — see [[Paint Fluids]].
- RAW, CATALYSED, CARBORAX and PAINT fluids are rendered slightly **translucent** (alpha `0xFA`);
  all other groups are fully opaque (alpha `0xFF`).

## Implementation

- **Enum:** `content/fluids/base/FluidGroup` — carries `lightLevel`, `density`, `viscosity`,
  `dropRate`, `temperature`, `stillTextureID`, `flowTextureID`.
- **Registration:** `registry/ModFluids` declares every fluid via `register(name, colour, group[, blockClass])`,
  building a `FluidEntry` per fluid. `registry/FluidEntry` registers the fluid type, source + flowing
  fluids (`GeneralizedFlowingFluid.Source` / `.Flowing`), the liquid block, and the bucket together.
  `registry/ModFluidTypes` builds the `GeneralizedFluidType` with the group's properties.
- **Colour/alpha:** the tint colour is `(alpha) | colour`, where alpha is `0xFA000000` for
  RAW/CATALYSED/CARBORAX/PAINT and `0xFF000000` otherwise.
- **Block path:** PAINT fluids register their liquid block under `paint/<name>`; all others under
  `<name>`. `POURED_CEMENT` passes `PouredCementBlock.class` so its block uses the custom class.
- **Textures:** `assets/resourceful_refinement/block/fluids/<stillTextureID|flowTextureID>`.

## Related

- [[Codebase Overview]]
- [[Fluid Processing Recipes]]
- [[Paint Fluids]]
- [[Paint Recipes]]
- [[Poured Cement]]
- [[Fluid Refinery]]
- [[Distillery]]
- [[Combustion Chamber]]
