---
title: Advanced Pump
category: Machine
status: Implemented
introduced: v0.3
recipe_type: n/a
related:
  - "[[Codebase Overview]]"
  - "[[Fluid Refinery]]"
  - "[[Primary Design Doc]]"
tags:
  - machine
  - kinetic
  - fluid
---

The Advanced Pump is an upgraded Create mechanical pump that moves fluids across **twice** the config distance limit of a standard pump, letting a single pump drive a much larger pipe network. It is a fully directional block: it pulls fluid in through its back (south) face and pushes it out through its front (north) face, needs a cog connection on one of its side faces for kinetic input, and reverses its flow direction when it receives a redstone signal so it can double as an inline valve or directional switch. Viewed through an [[Engineer's Goggles]] it reports its live passthrough rate in mB/s.

## Gameplay Role

Standard Create pumps are limited by the pipe-length config value, which caps how far one pump can propagate pressure. The Advanced Pump exists for late-game builds where that limit becomes a bottleneck: it doubles the reach, so long-distance fluid transport (refinery-to-storage runs, cross-base carborax lines) needs fewer relay pumps. The redstone-reverse behaviour also makes it a directional switch, so a single pump can serve both fill and drain roles under redstone control.

## Construction & Placement

- Single block. It extends Create's `PumpBlock`, inheriting the pump's `FACING` property, so it is fully directional and placed the same way as a vanilla mechanical pump (the front face points along the pipe it drives).
- Kinetic input is a **cog on a side face** — like a normal pump, it takes rotation on the axis perpendicular to its facing, not through the pump body itself.
- The block adds a `POWERED` boolean state used purely for the redstone-reverse behaviour and the visual cog direction.
- Custom support shapes (`getBlockSupportShape`) let pipes/attachments seat correctly on the faces around the pump body depending on its facing axis.

## Inputs & Outputs

- **Fluid in:** back / south face (the face opposite `FACING`).
- **Fluid out:** front / north face (`FACING`).
- **Kinetic in:** any side face via a cogwheel connection.
- Flow direction is decided by `isPullingOnSide(front)`: unpowered it pulls on the back and pushes out the front; when redstone-powered the two swap, reversing the whole network's flow.

## Operation

- **Range:** the pump's search distance is `FluidPropagator.getPumpRange() * 2` (`maxDistance` in `distributePressureTo`), i.e. exactly double a standard pump's config range. The goggle tooltip reports this as `Range: <pumpRange * 2> blocks`.
- **Kinetic requirement:** it only distributes pressure while `getSpeed() != 0`; with no rotation it idles and drops its measured throughput to zero.
- **Redstone reverse:** a neighbour signal sets `POWERED`, flips `redstonePowered`, and calls `updatePressureChange()` to re-propagate the network in the opposite direction. Redstone power is re-evaluated on placement, neighbour changes, and every server tick.
- **Throughput metering:** the pump measures how much fluid actually crosses the endpoints on its path each tick. `AdvancedPumpThroughputTracker` maps each pump to the set of `BlockFace` endpoints it feeds; whenever a transfer is recorded against one of those faces the pump accumulates `measuredThroughputMbPerTick`, which decays back to zero when flow stops. The value is synced to the client on a short cooldown (5-tick) so the goggle readout stays responsive without spamming packets.
- **Goggle tooltip:** shows `Throughput: <mB/t> (<mB/s>)`, `Range: <blocks>`, and `Direction: Normal | Reversed`.

## Rendering

`AdvancedPumpRenderer` extends Create's `KineticBlockEntityRenderer` and draws the rotating cog partial model (`ModPartialModels.ADVANCED_PUMP_COG`, `block/advanced_pump/advanced_pump_cog`) spinning about the facing axis. When the pump is redstone-reversed the renderer flips the visual facing (`getVisualState`) so the cog visibly turns the other way, matching the reversed flow. View distance is extended to 128.

## Implementation

- **Package:** `content/advanced_pump/`
- **Block:** `AdvancedPumpBlock extends com.simibubi.create.content.fluids.pump.PumpBlock`; adds `POWERED` boolean state; overrides `newBlockEntity`, `getStateForPlacement`, `neighborChanged`, `onPlace`, `getBlockSupportShape`, and the `getBlockEntityClass`/`getBlockEntityType` hooks so Create's pump code resolves the custom BE.
- **Block entity:** `AdvancedPumpBlockEntity extends PumpBlockEntity implements IHaveGoggleInformation`; overrides `isPullingOnSide` (redstone reverse), `distributePressureTo` (doubled-range BFS pressure propagation) and `updatePressureChange`; persists `RedstonePowered` and `MeasuredThroughputMbPerTick`.
- **Throughput tracker:** `AdvancedPumpThroughputTracker` — static map of pump → endpoint `BlockFace`s; `recordTransfer` credits matching pumps.
- **Renderer:** `AdvancedPumpRenderer`; partial model `ModPartialModels.ADVANCED_PUMP_COG`.
- **Registry IDs:** block `ModBlocks.ADVANCED_PUMP` (`advanced_pump`), item `ModItems.ADVANCED_PUMP_ITEM` (simple block item, `advanced_pump`), block entity `ModBlockEntities.ADVANCED_PUMP_BE` (`advanced_pump`).
- **Stress:** impact `ModStressValues.ADVANCED_PUMP_STRESS = 8` SU (registered on `ModBlocks.ADVANCED_PUMP`).
- **Crafting:** shaped recipe (`recipe/shaped_crafting/advanced_pump_shaped_crafting.json`) — a `create:mechanical_pump` centred, flanked by `minecraft:redstone` and `resourceful_refinement:polymer_residue`, with `minecraft:dried_kelp` top and bottom.

> [!note] Implementation
> The range multiplier is hard-wired as `FluidPropagator.getPumpRange() * 2` in `distributePressureTo` and in the goggle readout — it doubles whatever the (Create) pump-range config is set to, rather than exposing its own config key. The "south input / north front output" description is the default-facing convention; in play the input/output faces are simply the back and front of the block as placed, and they swap under redstone power.
> 
> TODO: Expose this pump-multiplier factor as a config value

## Related

- [[Codebase Overview]] — where this feature sits in the registry/content map.
- [[Fluid Refinery]] — a common source of the long fluid runs the Advanced Pump is built for.
- [[Primary Design Doc]] — v0.3 Advanced Pump design intent.
