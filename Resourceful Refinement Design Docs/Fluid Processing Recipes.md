---
title: Fluid Processing Recipes
category: Recipe
status: Implemented
introduced: v0.1
recipe_type: resourceful_refinement:fluid_refinery
related:
  - "[[Fluid Refinery]]"
  - "[[Fluid Properties]]"
  - "[[Forge Mould]]"
  - "[[Casting Depot]]"
  - "[[Mechanical Sieve]]"
  - "[[Distillery]]"
  - "[[Fracking Pump]]"
  - "[[Paint Recipes]]"
tags:
  - recipe
  - fluid
  - progression
---

Ship-accurate reference for **every fluid-related recipe in the project**, generated from the recipe
JSON under `data/resourceful_refinement/recipe/`. Each row is one shipped recipe file. This page
replaces the earlier design-intent tables; it reflects the live datapack, so treat it as the source of
truth for amounts, heat and timing.

> [!note] Conventions
> - Item and fluid ids are shown **without** the `resourceful_refinement:` and `minecraft:` prefixes;
>   `create:` (and other mod) prefixes are kept so external dependencies stay visible. `#` marks a tag.
> - Fluid quantities are in **mb**; item counts as `Nx`. `—` means the column is empty for that recipe.
> - **Time** is `processing_time` in **ticks** (20 ticks = 1 second). **Heat** is the recipe's
>   `heat_requirement` (`none`/`cooled`/`chilled`/`heated`/`superheated`); `—` means no requirement.
> - Sieve/refinery item outputs marked `(N%)` are chanced by-products.

## Recipe Types at a Glance

| System                                      | Recipe type                                     | Machine                             | Recipes here |
| ------------------------------------------- | ----------------------------------------------- | ----------------------------------- | ------------ |
| Melting                                     | `create:mixing`                                 | Create Mechanical Mixer             | 16           |
| Catalysis / purification / alloying / paint | `resourceful_refinement:fluid_refinery`         | [[Fluid Refinery]]                  | 43           |
| Fluid sieving                               | `resourceful_refinement:mechanical_fluid_sieve` | [[Mechanical Sieve]]                | 19           |
| Moulding & casting                          | `resourceful_refinement:mechanical_forge_mould` | [[Forge Mould]] / [[Casting Depot]] | 41           |
| Distilling                                  | `resourceful_refinement:distillery`             | [[Distillery]]                      | 14           |
| Geyser extraction                           | `resourceful_refinement:fracking_pump`          | [[Fracking Pump]]                   | 8            |
| Tool coating                                | `resourceful_refinement:coating`                | [[Forge Mould]] + Casting Depot     | 7            |
| Drink tapping                               | `resourceful_refinement:brewers_tap`            | [[Brewer's Tap]]                    | 6            |
| Mob milking (fluid)                         | `resourceful_refinement:milking_station`        | [[Milking Station]]                 | 3            |

Recipe format samples for each type are in the respective feature pages; a Fluid Refinery example:

```json
{
  "type": "resourceful_refinement:fluid_refinery",
  "heat_requirement": "heated",
  "processing_time": 40,
  "ingredients": [
    { "item": "resourceful_refinement:flux_dust" },
    { "type": "neoforge:single", "fluid": "resourceful_refinement:molten_crimsite", "amount": 50 },
    { "type": "neoforge:single", "fluid": "resourceful_refinement:silica_substrate", "amount": 35 }
  ],
  "results": [ { "id": "resourceful_refinement:catalysed_redstone", "amount": 75 } ]
}
```

## Melting — `create:mixing`

Create's Mechanical Mixer melts raw ores and Create's cast stones into molten minerals, and mixes a few
mod materials. (Recipes live under `recipe/mixing/`.)

### Ores & cast stone → molten minerals

| Recipe | Time | Item input | Fluid input | Output |
| --- | --- | --- | --- | --- |
| `raw_iron_to_crimsite` | 100 | raw_iron | 1000mb lava | 300mb molten_crimsite |
| `crimsite_to_crimsite` | 100 | create:crimsite | 1000mb lava | 100mb molten_crimsite |
| `raw_copper_to_veridium` | 100 | raw_copper | 1000mb lava | 300mb molten_veridium |
| `veridium_to_veridium` | 100 | create:veridium | 1000mb lava | 200mb molten_veridium |
| `raw_gold_to_ochrum` | 100 | raw_gold | 1000mb lava | 300mb molten_ochrum |
| `ochrum_to_ochrum` | 100 | create:ochrum | 1000mb lava | 50mb molten_ochrum |
| `raw_zinc_to_asurine` | 100 | create:raw_zinc | 1000mb lava | 300mb molten_asurine |
| `asurine_to_asurine` | 100 | create:asurine | 1000mb lava | 75mb molten_asurine |
| `scorchia_to_scorchia` | 100 | create:scorchia | 1000mb lava | 250mb molten_scorchia |

### Materials & fluids via mixing

| Recipe | Heat | Time | Item input | Fluid input | Output |
| --- | --- | --- | --- | --- | --- |
| `durasteel_alloy` | — | 200 | 16x ferrous_crystal | 1000mb molten_crimsite; 1000mb molten_asurine | 250mb durasteel_alloy |
| `durasteel_ingot_mixing` | — | 120 | ingot_mould | 500mb durasteel_alloy | 1x durasteel_ingot |
| `flux_dust_mixing` | — | 100 | redstone | 75mb silica_substrate | 6x flux_dust; 4x flux_dust (50%) |
| `graphite_mixing` | superheated | 360 | coal | 100mb catalysed_sparkpowder | 2x graphite |
| `polymer_residue_mixing` | heated | 300 | — | 500mb unrefined_carborax; 500mb water | 4x polymer_residue |
| `compacted_biomatter_mixing` | — | 80 | string | 500mb organic_slush | 2x compacted_biomatter |
| `ungloop_hosegun` | — | 100 | gloopy hosegun | 1000mb water | hosegun (cleans a gloopy [[Hosegun]]) |


## Fluid Refinery — `fluid_refinery`

Two-fluid + item processing in the [[Fluid Refinery]]. Always outputs a fluid. Recipes under
`recipe/fluid_refinery/` (with `alloys/` and `paint/` subfolders).

### Catalysation

| Recipe | Heat | Time | Item input | Fluid input | Fluid output |
| --- | --- | --- | --- | --- | --- |
| `catalysed_iron` | heated | 40 | — | 50mb molten_crimsite; 50mb silica_substrate | 70mb catalysed_iron |
| `catalysed_copper` | heated | 40 | — | 50mb molten_veridium; 50mb silica_substrate | 70mb catalysed_copper |
| `catalysed_gold` | heated | 40 | — | 50mb molten_ochrum; 50mb silica_substrate | 70mb catalysed_gold |
| `catalysed_zinc` | heated | 40 | — | 50mb molten_asurine; 50mb silica_substrate | 70mb catalysed_zinc |
| `catalysed_redstone` | heated | 40 | flux_dust | 50mb molten_crimsite; 35mb silica_substrate | 75mb catalysed_redstone |
| `catalysed_sparkpowder` | heated | 60 | flux_dust | 35mb molten_veridium; 15mb molten_ochrum | 200mb catalysed_sparkpowder |

### Purification

The primary route cycles the four metals (iron→gold→copper→zinc→iron) using a purified partner fluid;
each has a charcoal-based `_alt` route through silica substrate.

| Recipe | Heat | Time | Item input | Fluid input | Fluid output |
| --- | --- | --- | --- | --- | --- |
| `purified_iron` | superheated | 200 | ferrous_crystal | 500mb catalysed_iron; 75mb purified_gold | 640mb purified_iron |
| `purified_gold` | superheated | 200 | ferrous_crystal | 500mb catalysed_gold; 75mb purified_copper | 640mb purified_gold |
| `purified_copper` | superheated | 200 | ferrous_crystal | 500mb catalysed_copper; 75mb purified_zinc | 640mb purified_copper |
| `purified_zinc` | superheated | 200 | ferrous_crystal | 500mb catalysed_zinc; 75mb purified_iron | 640mb purified_zinc |
| `purified_iron_alt` | superheated | 200 | 2x charcoal | 500mb catalysed_iron; 500mb silica_substrate | 125mb purified_iron |
| `purified_gold_alt` | superheated | 200 | 2x charcoal | 500mb catalysed_gold; 500mb silica_substrate | 125mb purified_gold |
| `purified_copper_alt` | superheated | 200 | 2x charcoal | 500mb catalysed_copper; 500mb silica_substrate | 125mb purified_copper |
| `purified_zinc_alt` | superheated | 200 | 2x charcoal | 500mb catalysed_zinc; 500mb silica_substrate | 125mb purified_zinc |
| `alloys/purified_durasteel_refining` | superheated | 120 | charcoal; 2x blackstone | 100mb durasteel_alloy; 100mb catalysed_redstone | 125mb purified_durasteel |

### Alloying

| Recipe | Heat | Time | Item input | Fluid input | Fluid output |
| --- | --- | --- | --- | --- | --- |
| `alloys/silica_substrate_refining` | heated | 60 | flint; cobblestone | 250mb water | 125mb silica_substrate |
| `alloys/molten_andesite_blend_1` | heated | 60 | andesite | 50mb molten_crimsite; 50mb silica_substrate | 400mb molten_andesite_blend |
| `alloys/molten_andesite_blend_2` | heated | 60 | andesite | 50mb molten_asurine; 50mb silica_substrate | 500mb molten_andesite_blend |
| `alloys/molten_brass_blend_1` | heated | 80 | — | 50mb molten_veridium; 50mb molten_asurine | 125mb molten_brass_blend |
| `alloys/molten_brass_blend_2` | heated | 120 | iron_nugget | 100mb purified_copper; 100mb purified_zinc | 325mb molten_brass_blend |
| `alloys/durasteel_alloy_refining` | heated | 120 | charcoal | 100mb catalysed_iron; 100mb catalysed_zinc | 125mb durasteel_alloy |
| `alloys/molten_netherite_blend` | superheated | 120 | gold_ingot; ancient_debris | 2000mb molten_scorchia; 250mb durasteel_alloy | 500mb molten_netherite_blend |

### Carborax fuels

| Recipe | Heat | Time | Item input | Fluid input | Fluid output |
| --- | --- | --- | --- | --- | --- |
| `cheap_catalysed_carborax_refinery` | heated | 200 | polymer_residue | 200mb unrefined_carborax; 10mb catalysed_sparkpowder | 250mb catalysed_carborax |
| `graphite_catalysed_carborax_refinery` | chilled | 400 | graphite | 400mb unrefined_carborax; 75mb polymer_sludge | 750mb catalysed_carborax |

> [!note] Carborax Processing
> Overcharged carborax is made in the [[Distillery]] (`overcharged_carborax_distilling`), not the
> refinery. Unrefined carborax comes from a geyser via the [[Fracking Pump]] or `polymer_residue_mixing`.

### Utility fluids

| Recipe | Heat | Time | Item input | Fluid input | Fluid output |
| --- | --- | --- | --- | --- | --- |
| `alloys/liquid_glue_from_slime_ball` | none | 100 | slime_ball | 500mb water | 250mb liquid_glue |
| `alloys/liquid_glue_from_honey` | none | 100 | — | 500mb water; 250mb create:honey | 250mb liquid_glue |
| `coolant_refinery` | cooled | 800 | polymer_residue; packed_ice | 200mb polymer_sludge | 1000mb coolant |

### Paints

All 16 dyes share one recipe shape: dye + 500mb water + 20mb liquid_glue → 250mb paint, `none`, 320
ticks. Files under `fluid_refinery/paint/`. See [[Paint Recipes]] for the full colour list and downstream
gel/dye use.

| Recipe (per colour) | Heat | Time | Item input | Fluid input | Fluid output |
| --- | --- | --- | --- | --- | --- |
| `paint/<colour>_paint_from_dye` | none | 320 | `<colour>`_dye | 500mb water; 20mb liquid_glue | 250mb `<colour>`_paint |

Colours: white, orange, magenta, light_blue, yellow, lime, pink, gray, light_gray, cyan, purple, blue,
brown, green, red, black.

## Mechanical Fluid Sieve — `mechanical_fluid_sieve`

Fluid in the top, refined fluid out the bottom, chanced item by-product out the front. Recipes under
`recipe/mechanical_fluid_sieve/`. All are 60 ticks except `concrete_sieving` (10).

| Recipe | Time | Fluid input | Fluid output | Item by-product (chance) |
| --- | --- | --- | --- | --- |
| `water_to_prismarine` | 60 | 50mb water | — | prismarine_crystals (8%) |
| `lava_sieving` | 60 | 50mb lava | 20mb lava | blackstone (15%) |
| `molten_crimsite_sieving` | 60 | 50mb molten_crimsite | 25mb molten_crimsite | ferrous_crystal (8%) |
| `molten_veridium_sieving` | 60 | 50mb molten_veridium | 25mb molten_veridium | ferrous_crystal (8%) |
| `molten_ochrum_sieving` | 60 | 50mb molten_ochrum | 25mb molten_ochrum | ferrous_crystal (12%) |
| `molten_asurine_sieving` | 60 | 50mb molten_asurine | 25mb molten_asurine | ferrous_crystal (12%) |
| `molten_scorchia_sieving` | 60 | 50mb molten_scorchia | 15mb molten_scorchia | ancient_debris (0.1%) |
| `catalysed_iron_sieving` | 60 | 50mb catalysed_iron | 15mb molten_crimsite | create:crushed_raw_iron (6%) |
| `catalysed_copper_sieving` | 60 | 50mb catalysed_copper | 15mb molten_veridium | create:crushed_raw_copper (6%) |
| `catalysed_gold_sieving` | 60 | 50mb catalysed_gold | 15mb molten_ochrum | create:crushed_raw_gold (6%) |
| `catalysed_zinc_sieving` | 60 | 50mb catalysed_zinc | 15mb molten_asurine | create:crushed_raw_zinc (6%) |
| `catalysed_carborax_sieving` | 60 | 50mb catalysed_carborax | 35mb catalysed_carborax | coal (6%) |
| `purified_iron_sieving` | 60 | 50mb purified_iron | 20mb catalysed_iron | ferrous_crystal (20%) |
| `purified_copper_sieving` | 60 | 50mb purified_copper | 20mb catalysed_copper | ferrous_crystal (20%) |
| `purified_gold_sieving` | 60 | 50mb purified_gold | 20mb catalysed_gold | ferrous_crystal (30%) |
| `purified_zinc_sieving` | 60 | 50mb purified_zinc | 20mb catalysed_zinc | ferrous_crystal (30%) |
| `durasteel_alloy_sieving` | 60 | 50mb durasteel_alloy | 20mb molten_crimsite | create:zinc_nugget (45%) |
| `purified_durasteel_sieving` | 60 | 50mb purified_durasteel | 40mb purified_durasteel | create:zinc_nugget (60%) |
| `concrete_sieving` | 10 | 20mb liquid_concrete | 150mb poured_cement | flint (15%) |

## Mechanical Forge Mould — `mechanical_forge_mould`

Fluid (and optional item/mould) → item. **Mould mode** ejects the output onto a belt/depot below;
**casting mode** (`"casting": true`) requires a [[Casting Depot]] directly below. Same recipe type for
both — there is no separate `casting_mould` type. Recipes under `recipe/mechanical_forge_mould/`.

### Mould mode (`casting: false`)

| Recipe | Time | Item input | Fluid input | Item output |
| --- | --- | --- | --- | --- |
| `molten_crimsite_to_iron` | 80 | ingot_mould | 250mb molten_crimsite | iron_ingot |
| `molten_veridium_to_copper` | 80 | ingot_mould | 250mb molten_veridium | copper_ingot |
| `molten_ochrum_to_gold` | 80 | ingot_mould | 250mb molten_ochrum | gold_ingot |
| `molten_asurine_to_zinc` | 80 | ingot_mould | 250mb molten_asurine | create:zinc_ingot |
| `molten_brass_blend_to_ingot` | 160 | ingot_mould | 250mb molten_brass_blend | create:brass_ingot |
| `durasteel_alloy_to_ingot` | 120 | ingot_mould | 250mb durasteel_alloy | durasteel_ingot |
| `lava_to_magma` | 200 | blackstone | 50mb lava | magma_block |
| `bonemeal_forging` | 100 | — | 250mb milk | 6x compacted_biomatter |
| `sugar_forging` | 60 | — | 250mb liquid_syrup | 3x sugar |
| `liquid_glue_to_glue_pot` | 60 | stick | 250mb liquid_glue | glue_pot |
| `lead_from_liquid_glue` | 40 | string | 175mb liquid_glue | lead |
| `super_glue_from_liquid_glue` | 60 | create:iron_sheet | 400mb liquid_glue | create:super_glue |
| `sticky_piston_from_liquid_glue` | 60 | piston | 175mb liquid_glue | sticky_piston |
| `sticky_mechanical_piston_from_liquid_glue` | 60 | create:mechanical_piston | 175mb liquid_glue | create:sticky_mechanical_piston |

### Casting mode (`casting: true`)

| Recipe | Time | Item input | Fluid input | Item output |
| --- | --- | --- | --- | --- |
| `molten_crimsite_to_iron_casting` | 80 | — | 250mb molten_crimsite | iron_ingot |
| `molten_veridium_to_copper_casting` | 80 | — | 250mb molten_veridium | copper_ingot |
| `molten_ochrum_to_gold_casting` | 80 | — | 250mb molten_ochrum | gold_ingot |
| `molten_asurine_to_zinc_casting` | 80 | — | 250mb molten_asurine | create:zinc_ingot |
| `catalysed_iron_to_ingot` | 80 | — | 200mb catalysed_iron | iron_ingot |
| `catalysed_copper_to_ingot` | 80 | — | 200mb catalysed_copper | copper_ingot |
| `catalysed_gold_to_ingot` | 80 | — | 200mb catalysed_gold | gold_ingot |
| `catalysed_zinc_to_ingot` | 80 | — | 200mb catalysed_zinc | create:zinc_ingot |
| `purified_iron_to_ingot` | 120 | — | 150mb purified_iron | iron_ingot |
| `purified_copper_to_ingot` | 120 | — | 150mb purified_copper | copper_ingot |
| `purified_gold_to_ingot` | 120 | — | 150mb purified_gold | gold_ingot |
| `purified_zinc_to_ingot` | 120 | — | 150mb purified_zinc | create:zinc_ingot |
| `purified_durasteel_to_sheet` | 160 | — | 250mb purified_durasteel | durasteel_sheet |
| `andesite_blend_casting` | 160 | — | 125mb molten_andesite_blend | create:andesite_alloy |
| `brass_blend_casting` | 160 | — | 250mb molten_brass_blend | create:brass_ingot |
| `netherite_blend_casting` | 160 | — | 1000mb molten_netherite_blend | netherite_ingot |
| `shaft_casting` | 100 | shaft_mould | 60mb molten_andesite_blend | 4x create:shaft |
| `catalysed_redstone_casting` | 80 | — | 250mb catalysed_redstone | 4x redstone |
| `catalysed_sparkpowder_casting` | 80 | — | 250mb catalysed_sparkpowder | 4x glowstone_dust |
| `gunpowder_casting` | 80 | charcoal | 150mb catalysed_sparkpowder | gunpowder |
| `blaze_cake_casting` | 200 | create:blaze_cake_base | 300mb molten_scorchia | 2x create:blaze_cake |
| `soul_sand_casting` | 120 | sand | 1000mb molten_scorchia | soul_sand |
| `soul_soil_casting` | 120 | gravel | 1000mb molten_scorchia | soul_soil |
| `quartz_to_ferrous` | 120 | quartz | 500mb molten_scorchia | 4x ferrous_crystal |
| `amethyst_to_ferrous` | 80 | amethyst_shard | 500mb molten_scorchia | 2x ferrous_crystal |
| `honey_to_wax` | 120 | — | 125mb create:honey | honeycomb |
| `milk_to_bone_meal` | 120 | — | 500mb milk | 2x bone_meal |

## Distillery — `distillery`

Vertically stacked heat-and-time distillation. Each recipe fixes a tower **height** and a heat source
beneath. Item + fluid in the bottom, fluid out the top, only on completion. Recipes under
`recipe/distillery/`. See [[Distillery]] for heat/height rules.

| Recipe | Height | Heat | Time | Item input | Fluid input | Fluid output |
| --- | --- | --- | --- | --- | --- | --- |
| `crop_organic_mush_distilling` | 2 | passive | 2400 | 24x #c:crops | 3000mb water | 750mb organic_slush |
| `seed_organic_mush_distilling` | 2 | passive | 2400 | 28x #c:seeds | 3000mb water | 750mb organic_slush |
| `hot_chocolate_distilling` | 2 | heated | 4800 | 4x ice | 1000mb create:chocolate | 1000mb hot_chocolate |
| `beets_syrup_distilling` | 4 | heated | 4800 | 12x beetroot | 5000mb water | 1000mb liquid_syrup |
| `berries_syrup_distilling` | 4 | heated | 4800 | 18x #c:foods/berry | 5000mb water | 1000mb liquid_syrup |
| `honey_syrup_distilling` | 4 | heated | 4800 | 4x sugar | 1000mb create:honey | 1000mb liquid_syrup |
| `sugar_syrup_distilling` | 4 | heated | 6000 | 20x sugar | 6000mb water | 1000mb liquid_syrup |
| `wheat_mead_distilling` | 3 | heated | 8400 | hay_block | 5000mb water | 500mb mead |
| `apple_spirits_distilling` | 3 | heated | 8400 | 4x apple | 250mb liquid_syrup | 500mb liquid_spirits |
| `potato_spirits_distilling` | 3 | heated | 8400 | 28x potato | 250mb liquid_syrup | 500mb liquid_spirits |
| `liquid_glue_distilling` | 3 | chilled | 6000 | compacted_biomatter | 250mb polymer_sludge | 1000mb liquid_glue |
| `polymer_sludge_distilling` | 4 | heated | 10800 | 2x polymer_residue | 250mb organic_slush | 750mb polymer_sludge |
| `carborax_diesel_distilling` | 4 | heated | 15000 | 6x compacted_biomatter | 2000mb unrefined_carborax | 3500mb carborax_diesel |
| `overcharged_carborax_distilling` | 6 | chilled | 8400 | graphene_mesh | 3000mb catalysed_carborax | 4000mb overcharged_carborax |

## Fracking Pump — `fracking_pump`

Passive fluid extraction over a [[Geyser Block]]. `source_block` is always `geyser_block`; the geyser's
stored `source_fluid` selects the recipe. All 20 ticks. Recipes under `recipe/fracking/`.

| Recipe | Geyser fluid | Time | Fluid input | Fluid output |
| --- | --- | --- | --- | --- |
| `crimsite_to_molten_crimsite` | molten_crimsite | 20 | 200mb water | 8mb molten_crimsite |
| `veridium_to_molten_veridium` | molten_veridium | 20 | 200mb water | 8mb molten_veridium |
| `ochrum_to_molten_ochrum` | molten_ochrum | 20 | 200mb water | 8mb molten_ochrum |
| `asurine_to_molten_asurine` | molten_asurine | 20 | 200mb water | 8mb molten_asurine |
| `water_scorchia_to_molten_scorchia` | molten_scorchia | 20 | 125mb water | 20mb molten_scorchia |
| `lava_scorchia_to_molten_scorchia` | molten_scorchia | 20 | 200mb lava | 12mb molten_scorchia |
| `carborax_to_molten_carborax` | unrefined_carborax | 20 | 200mb water | 8mb unrefined_carborax |
| `lava_geyser_to_lava` | lava | 20 | 750mb water | 8mb lava |

## Tool Coating — `coating`

Applied through the [[Forge Mould]] with a Casting Depot holding the target tool. Consumes a fluid and
an ingredient to write the `coating_data` component. `duration` is coating integrity. Recipes under
`recipe/coating/`. See [[Coating]] / [[Coating Variants]].

| Recipe | Coating | Integrity | Applied to (ingredient) | Fluid cost |
| --- | --- | --- | --- | --- |
| `obsidianite_coating` | Obsidianite | 200 | create:sturdy_sheet | 1000mb molten_scorchia |
| `quicksilver_coating` | Quicksilver | 100 | glowstone_dust | 1000mb molten_asurine |
| `durasteel_coating` | Durasteel | 100 | durasteel_sheet | 1000mb durasteel_alloy |
| `liquid_luck_coating` | Liquid Luck | 100 | lapis_block | 1000mb catalysed_sparkpowder |
| `uplift_coating` | Uplift | 100 | chorus_fruit | 1000mb catalysed_sparkpowder |
| `conduction_coating` | Conduction | 100 | create:brass_ingot | 1000mb catalysed_sparkpowder |
| `gloopy_coating` | Gloopy | 200 | create:zinc_ingot | 1000mb liquid_glue |

## Drink Tapping — `brewers_tap`

The [[Brewer's Tap]] fills a `drinks_glass` from a fluid, tagging the drink with flavour components.
Recipes under `recipe/brewers_tap/`.

| Recipe | Time | Item input | Fluid input | Drink output | Flavour tags |
| --- | --- | --- | --- | --- | --- |
| `milkshake_tapping` | 60 | drinks_glass | 500mb milk | milkshake | cosmic, fruit, sweet |
| `hot_chocolate_tapping` | 60 | drinks_glass | 250mb hot_chocolate | hot_chocolate_mug | cosmic, sweet, chilled, spice |
| `mead_tapping` | 60 | drinks_glass | 500mb mead | mead_tankard | cosmic, fruit, spice, veg |
| `spirits_tapping` | 60 | drinks_glass | 500mb liquid_spirits | spirits_bottle | cosmic, fruit, spice, veg |
| `energy_drink_tapping` | 80 | drinks_glass | 750mb liquid_syrup | energy_drink | cosmic, fruit, sweet, chilled |
| `tea_tapping` | 60 | drinks_glass | 250mb create:tea | create:builders_tea | cosmic, sweet, chilled, spice |

## Milking Station — `milking_station` (fluid outputs)

The Milking Station's fluid outputs. (Item-only outputs — blaze powder, gunpowder, ink, etc. — are on
the [[Milking Station]] page.) Recipes under `recipe/milking_station/`.

| Recipe | Entity | Time | Fluid output |
| --- | --- | --- | --- |
| `cow_milk` | cow | 1800 | 250mb milk |
| `goat_milk` | goat | 1800 | 250mb milk |
| `player_milk` | player | 400 | 25mb milk |

## Related

- [[Fluid Refinery]] · [[Fluid Properties]] · [[Forge Mould]] · [[Casting Depot]]
- [[Mechanical Sieve]] · [[Distillery]] · [[Fracking Pump]] · [[Paint Recipes]]
- [[Codebase Overview]] for the recipe-type registry (`ModRecipeTypes`) and datapack layout.
