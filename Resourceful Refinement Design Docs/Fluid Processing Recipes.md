---
model: ollama@devstral-small-2:latest
---

#### **Basic Mineral Forging**

| **Machine** | **Duration** | Heat | Item 1 Input       | Item 2 Input | Fluid 1 Input            | Fluid 2 Input     | Item Output       | Fluid Output          |
| ----------- | ------------ | ---- | ------------------ | ------------ | ------------------------ | ----------------- | ----------------- | --------------------- |
| Mixer       | 100          |      | raw_iron           |              | 1B lava                  |                   |                   | 300mb molten_crimsite |
| Mixer       | 100          |      | crimsite           |              | 1B lava                  |                   |                   | 100mb molten_crimsite |
| Mixer       | 100          |      | raw_gold           |              | 1B lava                  |                   |                   | 300mb molten_ochrum   |
| Mixer       | 100          |      | ochrum             |              | 1B lava                  |                   |                   | 50mb molten_ochrum    |
| Mixer       | 100          |      | raw_copper         |              | 1B lava                  |                   |                   | 300mb molten_veridium |
| Mixer       | 100          |      | veridium           |              | 1B lava                  |                   |                   | 200mb molten_veridium |
| Mixer       | 100          |      | raw_zinc           |              | 1B lava                  |                   |                   | 300mb molten_asurine  |
| Mixer       | 100          |      | asurine            |              | 1B lava                  |                   |                   | 75mb molten_asurine   |
| Mixer       | 100          |      | schorcia           |              | 1B lava                  |                   |                   | 250mb molten_scorchia |
|             |              |      |                    |              |                          |                   |                   |                       |
| Mixer       | 200          |      | 16 ferrous_crystal |              | 1B molten_crimsite       | 1B molten_asurine |                   | 250mb durasteel_alloy |
|             |              |      |                    |              |                          |                   |                   |                       |
| Forge Mould | 80           |      | ingot_mould        |              | 250mb molten_crimsite    |                   | 1 iron_ingot      |                       |
| Forge Mould | 80           |      | ingot_mould        |              | 250mb molten_ochrum      |                   | 1 gold_ingot      |                       |
| Forge Mould | 80           |      | ingot_mould        |              | 250mb molten_veridium    |                   | 1 copper_ingot    |                       |
| Forge Mould | 80           |      | ingot_mould        |              | 250mb molten_asurine     |                   | 1 zinc_ingot      |                       |
|             |              |      |                    |              |                          |                   |                   |                       |
| Forge Mould | 120          |      | ingot_mould        |              | 250mb durasteel_alloy    |                   | 1 durasteel_ingot |                       |
| Forge Mould | 160          |      | ingot_mould        |              | 250mb molten_brass_blend |                   | 1 brass_ingot     |                       |
|             |              |      |                    |              |                          |                   |                   |                       |

#### **Sieving**

| **Machine** | **Duration** | Heat | Item 1 Input | Item 2 Input | Fluid 1 Input           | Fluid 2 Input | Item Output            | Fluid Output            |
| ----------- | ------------ | ---- | ------------ | ------------ | ----------------------- | ------------- | ---------------------- | ----------------------- |
| Sieve       | 60           |      |              |              | 50mb water              |               | 8% prismarine_crystals |                         |
| Sieve       | 60           |      |              |              | 50mb molten_crimsite    |               | 8% ferrous_crystal     | 30mb molten_crimsite    |
| Sieve       | 60           |      |              |              | 50mb molten_veridium    |               | 8% ferrous_crystal     | 30mb molten_veridium    |
| Sieve       | 60           |      |              |              | 50mb molten_ochrum      |               | 12% ferrous_crystal    | 30mb molten_ochrum      |
| Sieve       | 60           |      |              |              | 50mb molten_asurine     |               | 12% ferrous_crystal    | 30mb molten_asurine     |
| Sieve       | 60           |      |              |              | 50mb lava               |               | 15% blackstone         | 20mb lava               |
|             |              |      |              |              |                         |               |                        |                         |
| Sieve       | 60           |      |              |              | 50mb catalysed_iron     |               | 6% crushed_raw_iron    | 25mb molten_crimsite    |
| Sieve       | 60           |      |              |              | 50mb purified_iron      |               | 20% ferrous_crystal    | 25mb catalysed_iron     |
| Sieve       | 60           |      |              |              | 50mb catalysed_copper   |               | 6% crushed_raw_copper  | 25mb molten_veridium    |
| Sieve       | 60           |      |              |              | 50mb purified_copper    |               | 20% ferrous_crystal    | 25mb catalysed_copper   |
| Sieve       | 60           |      |              |              | 50mb catalysed_gold     |               | 6% crushed_raw_gold    | 25mb molten_ochrum      |
| Sieve       | 60           |      |              |              | 50mb purified_gold      |               | 30% ferrous_crystal    | 25mb catalysed_gold     |
| Sieve       | 60           |      |              |              | 50mb catalysed_zinc     |               | 6% crushed_raw_zinc    | 25mb molten_asurine     |
| Sieve       | 60           |      |              |              | 50mb purified_zinc      |               | 30% ferrous_crystal    | 25mb catalysed_zinc     |
|             |              |      |              |              |                         |               |                        |                         |
| Sieve       | 60           |      |              |              | 50mb durasteel_alloy    |               | 40% zinc_nugget        | 40mb molten_crimsite    |
| Sieve       | 60           |      |              |              | 50mb purified_durasteel |               | 60% zinc_nugget        | 40mb purified_durasteel |
| Sieve       | 60           |      |              |              | 50mb molten_scorchia    |               | 0.1% ancient_debris    | 35mb molten_scorchia    |
| Sieve       | 60           |      |              |              | 50mb catalysed_carborax |               | 8% coal                | 35mb catalysed_carborax |

#### **Casting Forging**
| **Machine**   | **Duration** | Heat | Item 1 Input      | Item 2 Input | Fluid 1 Input               | Fluid 2 Input | Item Output       | Fluid Output |
| ------------- | ------------ | ---- | ----------------- | ------------ | --------------------------- | ------------- | ----------------- | ------------ |
| Forge Casting | 160          |      |                   |              | 125mb molten_andesite_blend |               | 1 andesite_alloy  |              |
| Forge Casting | 60           |      | shaft_mould       |              | 15mb molten_andesite_blend  |               | 1 shaft           |              |
| Forge Casting | 160          |      |                   |              | 250mb molten_brass_blend    |               | 1 brass_ingot     |              |
| Forge Casting | 160          |      |                   |              | 1B molten_netherite_blend   |               | 1 netherite_ingot |              |
| Forge Casting | 80           |      |                   |              | 250mb molten_brass_blend    |               | 1 brass_ingot     |              |
| Forge Casting | 80           |      |                   |              | 250mb catalysed_redstone    |               | 4 redstone_dust   |              |
| Forge Casting | 80           |      |                   |              | 250mb catalysed_sparkdust   |               | 4 glowstone_dust  |              |
| Forge Casting | 80           |      | charcoal          |              | 150mb catalysed_sparkdust   |               | 1 gunpowder       |              |
|               |              |      |                   |              |                             |               |                   |              |
| Forge Casting | 200          |      | 4 ferrous_crystal |              | 1B molten_scorchia          |               | 1 magma_block     |              |
| Forge Casting | 120          |      | 1 sand            |              | 1B molten_scorchia          |               | 1 soul_sand       |              |
| Forge Casting | 120          |      | 1 gravel          |              | 1B molten_scorchia          |               | 1 soul_soil       |              |
| Forge Casting | 120          |      | 1 quartz          |              | 500mb molten_scorchia       |               | 4 ferrous_crystal |              |
| Forge Casting | 120          |      |                   |              | 500mb milk                  |               | 2 bone_meal       |              |
| Forge Casting | 120          |      |                   |              | 125mb honey                 |               | 1 wax             |              |
| Forge Casting | 80           |      | 1 amethyst_shard  |              | 500mb molten_scorchia       |               | 4 ferrous_crystal |              |

#### **Refinery Catalysation**
| **Machine** | **Duration** | Heat        | Item 1 Input | Item 2 Input      | Fluid 1 Input          | Fluid 2 Input          | Item Output | Fluid Output              |
| ----------- | ------------ | ----------- | ------------ | ----------------- | ---------------------- | ---------------------- | ----------- | ------------------------- |
| Refinery    | 40           | Heated      |              |                   | 50mb molten_crimsite   | 50mb silica_substrate  |             | 75mb catalysed_iron       |
| Refinery    | 40           | Heated      |              | 1 flux_dust       | 50mb molten_crimsite   | 35mb silica_substrate  |             | 75mb catalysed_redstone   |
| Refinery    | 200          | Superheated |              | 1 ferrous_crystal | 500mb catalysed_iron   | 75mb purified_gold     |             | 750mb purified_iron       |
|             |              |             |              |                   |                        |                        |             |                           |
| Refinery    | 40           | Heated      |              |                   | 50mb molten_veridium   | 50mb silica_substrate  |             | 75mb catalysed_copper     |
| Refinery    | 200          | Superheated |              | 1 ferrous_crystal | 500mb catalysed_copper | 75mb purified_zinc     |             | 750mb purified_copper     |
|             |              |             |              |                   |                        |                        |             |                           |
| Refinery    | 40           | Heated      |              |                   | 50mb molten_ochrum     | 50mb silica_substrate  |             | 75mb catalysed_gold       |
| Refinery    | 40           | Heated      |              | 1 flux_dust       | 35mb molten_veridium   | 15mb molten_ochrum     |             | 200mb catalysed_sparkdust |
| Refinery    | 200          | Superheated |              | 1 ferrous_crystal | 500mb catalysed_gold   | 75mb purified_copper   |             | 750mb purified_gold       |
|             |              |             |              |                   |                        |                        |             |                           |
| Refinery    | 40           | Heated      |              |                   | 50mb molten_asurine    | 50mb silica_substrate  |             | 75mb catalysed_zinc       |
| Refinery    | 200          | Superheated |              | 1 ferrous_crystal | 500mb catalysed_zinc   | 75mb purified_iron     |             | 750mb purified_zinc       |
|             |              |             |              |                   |                        |                        |             |                           |
| Refinery    | 200          | Superheated |              | 2 charcoal        | 500mb catalysed_iron   | 500mb silica_substrate |             | 125mb purified_iron       |
| Refinery    | 200          | Superheated |              | 2 charcoal        | 500mb catalysed_copper | 500mb silica_substrate |             | 125mb purified_copper     |
| Refinery    | 200          | Superheated |              | 2 charcoal        | 500mb catalysed_gold   | 500mb silica_substrate |             | 125mb purified_gold       |
| Refinery    | 200          | Superheated |              | 2 charcoal        | 500mb catalysed_zinc   | 500mb silica_substrate |             | 125mb purified_zinc       |
|             |              |             |              |                   |                        |                        |             |                           |
iron -> gold -> copper -> zinc -> iron

#### **Advanced Casting**
| **Machine**   | **Duration** | Heat | Item 1 Input      | Item 2 Input | Fluid 1 Input            | Fluid 2 Input | Item Output       | Fluid Output |
| ------------- | ------------ | ---- | ----------------- | ------------ | ------------------------ | ------------- | ----------------- | ------------ |
| Forge Casting | 80           |      |                   |              | 200mb catalysed_iron     |               | 1 iron_ingot      |              |
| Forge Casting | 80           |      |                   |              | 200mb catalysed_copper   |               | 1 copper_ingot    |              |
| Forge Casting | 80           |      |                   |              | 200mb catalysed_gold     |               | 1 gold_ingot      |              |
| Forge Casting | 80           |      |                   |              | 200mb catalysed_zinc     |               | 1 zinc_ingot      |              |
|               |              |      |                   |              |                          |               |                   |              |
| Forge Casting | 120          |      |                   |              | 150mb purified_iron      |               | 1 iron_ingot      |              |
| Forge Casting | 120          |      |                   |              | 150mb purified_copper    |               | 1 copper_ingot    |              |
| Forge Casting | 120          |      |                   |              | 150mb purified_gold      |               | 1 gold_ingot      |              |
| Forge Casting | 120          |      |                   |              | 150mb purified_zinc      |               | 1 zinc_ingot      |              |
|               |              |      |                   |              |                          |               |                   |              |
| Forge Casting | 80           |      |                   |              | 250mb molten_crimsite    |               | 1 iron_ingot      |              |
| Forge Casting | 80           |      |                   |              | 250mb molten_veridium    |               | 1 copper_ingot    |              |
| Forge Casting | 80           |      |                   |              | 250mb molten_ochrum      |               | 1 gold_ingot      |              |
| Forge Casting | 80           |      |                   |              | 250mb molten_asurine     |               | 1 zinc_ingot      |              |
|               |              |      |                   |              |                          |               |                   |              |
| Forge Casting | 160          |      |                   |              | 250mb purified_durasteel |               | 1 durasteel_sheet |              |
| Forge Casting | 200          |      | 1 blaze_cake_base |              | 300mb molten_scorchia    |               | 2 blaze_cake      |              |

#### **Miscellaneous Refining**
| **Machine** | **Duration** | Heat        | Item 1 Input  | Item 2 Input     | Fluid 1 Input         | Fluid 2 Input            | Item Output | Fluid Output                 |
| ----------- | ------------ | ----------- | ------------- | ---------------- | --------------------- | ------------------------ | ----------- | ---------------------------- |
| Refinery    | 60           | Heated      | 1 flint       | 1 cobblestone    | 250mb water           |                          |             | 125mb silica_substrate       |
| Refinery    | 60           | Heated      | 1 andesite    |                  | 50mb molten_crimsite  | 50mb silica_substrate    |             | 400mb molten_andesite_blend  |
| Refinery    | 60           | Heated      | 1 andesite    |                  | 50mb molten_asurine   | 50mb silica_substrate    |             | 500mb molten_andesite_blend  |
| Refinery    | 80           | Heated      |               |                  | 50mb molten_veridium  | 50mb molten_asurine      |             | 125mb molten_brass_blend     |
| Refinery    | 120          | Heated      | 1 iton_nugget |                  | 100mb purified_copper | 100mb purified_zinc      |             | 325mb molten_brass_blend     |
|             |              |             |               |                  |                       |                          |             |                              |
| Refinery    | 120          | Heated      | 1 charcoal    |                  | 100mb catalysed_iron  | 100mb catalysed_zinc     |             | 125mb durasteel_alloy        |
| Refinery    | 120          | Superheated | 1 charcoal    | 2 blackstone     | 100mb durasteel_alloy | 100mb catalysed_redstone |             | 125mb purified_durasteel     |
| Refinery    | 120          | Superheated | 1 gold_ingot  | 1 ancient_debris | 2B molten_scorchia    | 250mb durasteel_alloy    |             | 500mb molten_netherite_blend |
|             |              |             |               |                  |                       |                          |             |                              |

#### **Carborax Refining**
| **Machine** | **Duration** | Heat | Item 1 Input | Item 2 Input | Fluid 1 Input            | Fluid 2 Input            | Item Output | Fluid Output               |
| ----------- | ------------ | ---- | ------------ | ------------ | ------------------------ | ------------------------ | ----------- | -------------------------- |
| Refinery    | 120          |      |              |              | 250mb molten_carborax    | 25mb catalysed_sparkdust |             | 350mb catalysed_carborax   |
| Refinery    | 120          |      | 1 charcoal   |              | 250mb catalysed_carborax | 25mb molten_scorchia     |             | 400mb overcharged_carborax |
|             |              |      |              |              |                          |                          |             |                            |

