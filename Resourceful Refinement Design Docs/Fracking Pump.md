---
title: Fracking Pump
category: Machine
status: Implemented
introduced: v0.1
recipe_type: resourceful_refinement:fracking_pump
related:
  - "[[Geyser Block]]"
  - "[[Fracking Source Blocks]]"
  - "[[Nether Geyser Worldgen]]"
tags:
  - machine
  - multiblock
  - kinetic
  - fluid
  - worldgen
---

The Fracking Pump is a multiblock structure assembled on top of a [[Geyser Block]]. While assembled and provided sufficient RPM, the pump passively generates a fluid according to the block it sits on, in exchange for consuming another (config-specific) fluid pumped into it. The Fracking Pump stores its output in an internal tank, or pushes it to connected pipes. The height of the pump determines both the rate of production and the minimum RPM required to run.

**ID:** *fracking_pump_outlet*

## Gameplay Role

The Fracking Pump turns a naturally-generated [[Geyser Block]] into a renewable, factory-scale fluid tap. It is the endgame way to farm the molten ore fluids and lava that geysers store: place the pump on a geyser, feed it the cheap input fluid the recipe calls for (usually water), supply rotation, and it drips out the matching output fluid forever. Taller pumps trade a higher RPM requirement for a faster production rate, so it scales with the player's kinetic budget.

## Construction & Placement

The Fracking Pump is a multiblock that uses a *fracking_pump_outlet* as its controller block. To assemble one:

1. Place a *fracking_pump_outlet* on top of a geyser block.
2. Stack 2 brass casing blocks (*brass_casing*) directly on top of the outlet.
3. Stack metal girders (*metal_girder*) vertically on top of the casings to form the girder pole.
4. Cap the top of the girder pole with an andesite alloy block (*andesite_alloy_block*).
5. Build a ring (3×3 with a centre hole) of industrial iron blocks (*industrial_iron_block*) around the top half of the girder pole. This is the "Counterweight".

The height of the girder pole and its surrounding ring is dynamically scalable, but must satisfy two conditions:

- The girder pole must be exactly twice the height of the industrial iron ring.
- The top-most layer of industrial iron must be level with the top block in the girder pole.

This arrangement is possible up to a ring 4 blocks high (a pole length of 8).

Right-clicking an unassembled outlet attempts to create an assembled Fracking Pump. On success, all constituent blocks (except the outlet) are removed, and the centre column (girder pole + andesite cap) is replaced with invisible proxy blocks. Destroying the outlet or any proxy block of an assembled pump disassembles the multiblock and returns its constituent blocks to the world. If a placement space has since been occupied by another block, that block instead drops as an item.

> [!note] Implementation
> Assembly is validated in `FrackingPumpOutletBlockEntity.tryAssemble()`. The girder pole must be an **even** height (so the ring can be exactly half of it) and is capped at **8** blocks. `h_pole` and `h_ring` are stored on the controller; `h_ring = h_pole / 2`. Assembly gives player feedback via chat/action-bar messages listing the exact failure reason (missing casing, pole too tall, odd pole height, missing cap, incomplete ring).

## Inputs & Outputs

The *fracking_pump_outlet* is a horizontally rotatable block (`FACING`, horizontal). It has:

- A fluid **output** on its front face and a fluid **input** on its back face.
- A **half-shaft** on its side face, accepting rotational input from adjacent kinetic blocks.

The outlet will not accept fluid input or produce fluid output while unassembled.

Both tanks hold **4000 mB** (`TANK_CAPACITY`). Input fluid is drained from `inputTank`; produced fluid accumulates in `outputTank` and is pulled out through pipes on the output face.

> [!note] Implementation
> Geometry is authored with `Direction.SOUTH` as the identity orientation. `FrackingPumpOutletBlock.isFluidInputSide`/`isFluidOutputSide` map world sides to model-local **north** (input) and **south** (output). The `Capabilities.FluidHandler.BLOCK` capability is registered per-side in `ResourcefulRefinementMain.registerCapabilities`: the input side exposes `inputTank`, the output side exposes `outputTank`, and neither is exposed while the pump is unassembled. Rotation axis is `facing.getClockWise().getAxis()`, and `hasShaftTowards` returns true along that axis.

## Operation

While the assembled pump has the required input fluid and is spun **above a threshold RPM**, it continuously produces the output fluid. The height of the Counterweight affects both production rate and RPM threshold:

- **Production rate:** for each ring layer above the first, the rate of production/consumption increases by 25%.
- **RPM threshold:** increases with each additional ring layer.

An RPM input above the threshold does not increase production rate.

> [!note] Implementation
> `getRequiredRPM() = 64 + (h_ring - 1) * 64` — base 64 RPM, +64 per additional ring layer. The older design note said "+80" per layer; the shipping value is **+64**. Production rate multiplier is `1.0 + (h_ring - 1) * 0.25` (the 25% figure holds). Effective duration per cycle is `baseDuration / rate` (minimum 1 tick), where `baseDuration` comes from the recipe's `processing_time` (defaulting to 100 ticks if unset). Each cycle simulates a fill first and waits for output-tank space before draining input and producing output. Stress: `ModStressValues.FRACKING_STRESS = 16 su` (impact), registered against `FRACKING_PUMP_OUTLET`. When running, the pump plays anvil + lava-extinguish sounds and emits smoke particles on a fixed animation cycle. A Create goggle tooltip (`IHaveGoggleInformation`) reports required/current RPM, stress, and intake/outtake rates in mB/s.

## Recipes

The Fracking Pump uses a JSON-defined config lookup of all the fluids it can produce. Each recipe entry consists of:

- A **source block** that the pump must sit on (this must be unique per source).
- An **input fluid** ID with amount.
- An **output fluid** ID with amount.

*(e.g. magma_block, 1000 water in, 666 lava out — illustrative of the intended shape.)*

### Geyser source blocks

In addition to the source-block requirement, fracking recipes targeting a geyser source block may also specify a fluid ID. Only geysers storing the matching fluid then permit that recipe. See [[Fracking Source Blocks]] for the full recipe table, and [[Geyser Block]] for how a geyser's stored fluid is set.

**Recipe type:** `resourceful_refinement:fracking_pump`

```json
{
  "type": "resourceful_refinement:fracking_pump",
  "source_block": "resourceful_refinement:geyser_block",
  "source_fluid": "resourceful_refinement:molten_asurine",
  "processing_time": 20,
  "ingredients": [
    { "type": "neoforge:single", "fluid": "minecraft:water", "amount": 200 }
  ],
  "results": [
    { "id": "resourceful_refinement:molten_asurine", "amount": 8 }
  ]
}
```

> [!note] Implementation
> `FrackingPumpRecipe extends StandardProcessingRecipe<FrackingPumpRecipeInput>`. `source_block` defaults to `resourceful_refinement:geyser_block`; `source_fluid` is optional and, when present, restricts matching to geysers storing that fluid (`requiresGeyserFluid()`). Recipes allow 0 item inputs, up to 1 fluid input and 1 fluid output, and can specify a duration. In practice every shipped recipe uses `geyser_block` + a `source_fluid`, so the geyser's stored fluid — not a distinct source block — selects the recipe.

## Rendering

The assembled Fracking Pump is composed of several models:

- *FrackingPumpOutletModel* — the block model for the *fracking pump outlet* in its disassembled state.
- *FrackingPumpBaseModel* — the base of the pump, 1×3 blocks high, rendered in place of the outlet block and copper/brass casings.
- *FrackingPumpShaftModel* — a vertically tileable model rendered in place for each block in the girder pole.
- *FrackingPumpTopModel* — the top part of the pump shaft, rendered at the andesite alloy block's position.
- *FrackingPumpCounterweightModel* — a vertically tileable model representing the counterweight assembly. Multiple layers are stacked to imitate the original counterweight. The assembly moves as one object: while rotational input is below the required threshold, it moves to the top of the shaft and sits there; while input RPM meets the threshold, it repeats a cycle in which it rapidly drops down, then slowly rises back to the top of the shaft.

![[Fracking Pylon Master Render 1.png]]

> [!note] Implementation
> Rendered by `FrackingPumpRenderer` (a `BlockEntityRenderer` registered in `ClientModEvents`). Layer definitions are registered via `FrackingPumpLayers` (OUTLET, BASE, SHAFT, TOP, COUNTERWEIGHT). The dropped/placed item uses `FrackingPumpOutletItemRenderer`. Drop-cycle constants live on the block entity (`DROP_NORMALISED_DURATION`, `DROP_CYCLE_DURATION`, `DROP_PAUSE_NORMALISED_DURATION`), and `getRenderBoundingBox` expands to enclose the whole assembled pump.

## Implementation

- **Controller block/BE:** `content/fracking_pump/FrackingPumpOutletBlock` (`KineticBlock`, `IBE`), `FrackingPumpOutletBlockEntity` (`KineticBlockEntity`, `IBE`, `IHaveGoggleInformation`). Holds `inputTank`, `outputTank`, `assembled`, `h_pole`, `h_ring`.
- **Proxy block/BE:** `FrackingPumpProxyBlock`, `FrackingPumpProxyBlockEntity` (`SmartBlockEntity`). Proxies store their controller position and delegate behaviours/capabilities to the controller; they never duplicate state.
- **Item:** `FrackingPumpOutletItem`, rendered by `FrackingPumpOutletItemRenderer`.
- **Recipe:** `content/fracking_pump/recipe/FrackingPumpRecipe` (+ `FrackingPumpRecipeInput`, `FrackingPumpRecipeCategory` for JEI). Recipe type `resourceful_refinement:fracking_pump` (`ModRecipeTypes.FRACKING_PUMP_TYPE`).
- **Registry IDs:** `ModBlocks.FRACKING_PUMP_OUTLET`, `ModBlocks.FRACKING_PUMP_PROXY`, `ModBlockEntities.FRACKING_PUMP_OUTLET_BE`.
- **Stress:** `ModStressValues.FRACKING_STRESS = 16` (impact).
- **Capabilities:** `Capabilities.FluidHandler.BLOCK`, registered per-side in `ResourcefulRefinementMain.registerCapabilities`.
- **Recipe data:** `data/resourceful_refinement/recipe/fracking/*.json`. Crafting recipe: `recipe/shaped_crafting/fracking_pump_outlet_shaped_crafting.json`.

## Related

- [[Geyser Block]]
- [[Fracking Source Blocks]]
- [[Nether Geyser Worldgen]]
