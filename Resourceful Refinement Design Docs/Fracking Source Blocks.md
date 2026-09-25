---
title: Fracking Source Blocks
category: Machine
status: Implemented
introduced: v0.1
recipe_type: resourceful_refinement:fracking_pump
related:
  - "[[Fracking Pump]]"
  - "[[Geyser Block]]"
  - "[[Nether Geyser Worldgen]]"
tags:
  - machine
  - fluid
  - recipe
---

"Fracking source blocks" are the blocks a [[Fracking Pump]] can be assembled on top of, together with the input→output fluid conversions each one enables. In the shipping build every source is a [[Geyser Block]], and it is the geyser's *stored fluid* — not a distinct block type — that selects which conversion runs. This page holds the fracking recipe table; see [[Fracking Pump]] for the machine and [[Geyser Block]] for how a geyser's fluid is set.

## Gameplay Role

Each geyser carries a fluid (lava or a molten ore). Sit a pump on it, feed the required input fluid, and the pump passively converts input into the geyser's fluid. This is the renewable tap for molten ore fluids and lava, gating a molten-ore fluid economy behind finding and harnessing geysers.

## Recipes

### Design intent (original table)

| **Source Block Fluid** | **Duration (t)** | **Input Fluid** | **Input Amount (mB)** | Output Fluid    | **Output Amount (mB)** |
| ---------------------- | ---------------- | --------------- | --------------------- | --------------- | ---------------------- |
| lava                   | 20               | molten_crimsite | 10                    | lava            | 100                    |
| lava                   | 20               | molten_veridium | 10                    | lava            | 100                    |
| molten_crimsite        | 20               | water           | 50                    | molten_crimsite | 25                     |
| molten_veridium        | 20               | water           | 50                    | molten_veridium | 25                     |
| molten_ochrum          | 20               | water           | 50                    | molten_ochrum   | 25                     |
| molten_asurine         | 20               | water           | 50                    | molten_asurine  | 25                     |
| molten_scorchia        | 20               | lava            | 100                   | molten_scorchia | 50                     |

### Currently shipped recipes

Every shipped recipe uses `source_block = resourceful_refinement:geyser_block` and distinguishes itself by `source_fluid` (the geyser's stored fluid). Duration is 20 ticks throughout.

| Geyser stored fluid (`source_fluid`) | Input fluid | Input (mB) | Output fluid | Output (mB) | Recipe file |
| --- | --- | --- | --- | --- | --- |
| `minecraft:lava` | water | 750 | `minecraft:lava` | 8 | `lava_geyser_to_lava` |
| `molten_asurine` | water | 200 | `molten_asurine` | 8 | `asurine_to_molten_asurine` |
| `molten_crimsite` | water | 200 | `molten_crimsite` | 8 | `crimsite_to_molten_crimsite` |
| `molten_veridium` | water | 200 | `molten_veridium` | 8 | `veridium_to_molten_veridium` |
| `molten_ochrum` | water | 200 | `molten_ochrum` | 8 | `ochrum_to_molten_ochrum` |
| `molten_scorchia` | lava | 200 | `molten_scorchia` | 12 | `lava_scorchia_to_molten_scorchia` |
| `molten_scorchia` | water | 125 | `molten_scorchia` | 20 | `water_scorchia_to_molten_scorchia` |
| `unrefined_carborax` | water | 200 | `unrefined_carborax` | 8 | `carborax_to_molten_carborax` |

> [!note] Implementation
> The shipped recipes diverge from the original design table in three ways:
> 1. **Source is the geyser's fluid, not a distinct block.** All recipes use `source_block: resourceful_refinement:geyser_block` plus a `source_fluid`; `FrackingPumpRecipe.matches` checks the geyser BE's stored fluid against `source_fluid`. There are no non-geyser source blocks in the data.
> 2. **No lava-from-molten-ore recipes.** The design table's `lava` rows (consuming molten_crimsite/molten_veridium to output lava) do not exist. Instead a lava geyser consumes water (750 mB) to output lava, and molten-ore geysers consume water to output their own molten ore.
> 3. **Amounts differ.** Input/output amounts are higher/rebalanced versus the design table (e.g. water inputs of 125–750 mB, molten-ore outputs of 8–20 mB), though the 20-tick duration is preserved. `molten_scorchia` has two recipes (a cheaper water route and a lava route), and `unrefined_carborax` is a shipped source the design table never listed.
>
> Recipe files live in `data/resourceful_refinement/recipe/fracking/`.

## Implementation

- **Recipe type:** `resourceful_refinement:fracking_pump` (`ModRecipeTypes.FRACKING_PUMP_TYPE`).
- **Recipe class:** `content/fracking_pump/recipe/FrackingPumpRecipe` (+ `FrackingPumpRecipeInput`, `FrackingPumpRecipeCategory`).
- **Selection:** `FrackingPumpOutletBlockEntity.resolveGeyserFluid` reads the geyser BE below the outlet; `FrackingPumpRecipe.requiresGeyserFluid()`/`getSourceFluid()` gate the match.
- **Data:** `data/resourceful_refinement/recipe/fracking/*.json`.

## Related

- [[Fracking Pump]]
- [[Geyser Block]]
- [[Nether Geyser Worldgen]]
