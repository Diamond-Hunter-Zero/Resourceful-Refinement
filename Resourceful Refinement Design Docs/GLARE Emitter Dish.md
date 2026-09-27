---
title: GLARE Emitter Dish
category: Machine
status: Implemented
introduced: v0.4
recipe_type: n/a
related:
  - "[[GLARE Networks]]"
  - "[[Resonance Crystal]]"
  - "[[GLARE Relay]]"
  - "[[Relay Wrench]]"
tags:
  - machine
  - glare
  - kinetic
  - network
---

The GLARE Emitter Dish is the primary Lux source for a [[GLARE Networks|GLARE network]]. It is a kinetic block that converts rotational power into network Lux, provided it is fed by a [[Resonance Crystal]] and spun fast enough. Each emitter contributes a fixed block of Lux capacity, tinted by a colour charge, to whichever network it belongs to.

**ID:** *glare_emitter_dish*

## Gameplay Role

The Emitter Dish is where a GLARE network gets its power. Without at least one enabled emitter a network has zero Lux capacity and any receiver drawing power will overload it. The dish ties GLARE progression back into Create kinetics — you need a shaft, sufficient RPM, and a [[Resonance Crystal]] beneath it before it produces anything, so it sits downstream of a working kinetic setup.

## Construction & Placement

The Emitter Dish is a horizontal kinetic block (`HorizontalKineticBlock`) — it faces one of the four horizontal directions and its rotation axis is the axis of that facing. It accepts a shaft on the faces along its rotation axis. A [[Resonance Crystal]] (natural or artificial) must be placed **directly below** the dish for it to emit.

## Inputs & Outputs

- **Kinetic input:** shaft connection on the rotation axis (the horizontal facing axis). Stress impact 8.
- **GLARE link:** a single link (`MAX_LINK_COUNT = 1`). The dish connects to exactly one other node; chain further reach through a [[GLARE Relay]].
- **Output:** `BASE_LUX = 8` Lux contributed to the network, carrying the dish's colour charge.

## Operation

The dish emits only when **all** of the following hold (`isGlareEmitterEnabled`):

- a [[Resonance Crystal]] (natural or artificial) sits directly below it;
- absolute rotation speed is at least `REQUIRED_RPM = 32` RPM;
- the block entity is **not** over-stressed;
- there is **no** redstone signal at the block (any neighbour signal disables it).

When any of these change (speed change, neighbour change, redstone) the emitter re-evaluates its state; it also refreshes on a 20-tick cadence. A redstone signal is therefore an off-switch. When enabled it produces `BASE_LUX` (8); when disabled it produces 0 toward the network.

The colour charge is set by right-clicking the dish with a **stained glass block** — the dish adopts that glass's `DyeColor` (default **white**). Right-clicking with an empty hand opens the GLARE power terminal (`GlarePowerTerminalOpener`). Right-clicking with a [[Relay Wrench]] edits links; right-clicking with a held GLARE node block-item records the dish as a placement target.

## Rendering

Standard block model (`RenderShape.MODEL`), full-cube collision/selection shape. Goggle tooltip (`addToGoggleTooltip`) reports link count, current vs maximum Lux output, required RPM, crystal presence, redstone signal state, and — when networked — network Lux allocation, online/overloaded status, and per-colour charge summary.

## Implementation

- **Block:** `GlareEmitterDishBlock` (extends `com.simibubi.create.content.kinetics.base.HorizontalKineticBlock`, implements `IBE<GlareEmitterDishBlockEntity>`).
- **Block entity:** `GlareEmitterDishBlockEntity` (extends `KineticBlockEntity`, implements `IGlareNode`, `IGlareEmitter`, `GlareNetworkSnapshotProvider`).
- **Registry IDs:** block/item `resourceful_refinement:glare_emitter_dish`; block entity type `ModBlockEntities.GLARE_EMITTER_DISH_BE`.
- **Constants:** `BASE_LUX = 8`, `REQUIRED_RPM = 32.0F`, `MAX_LINK_COUNT = 1`.
- **Stress:** impact 8, from `ModStressValues.GLARE_EMITTER_STRESS` (registered in `ModStressValues`).
- **Colour:** stored as a `DyeColor`, defaulting to `DyeColor.WHITE`, persisted under the `Colour` NBT key; exposed as the network colour charge via `getLuxColourCharge`.
- **Interfaces:** `IGlareEmitter.getProducedLux()` returns `BASE_LUX`; `isGlareEmitterEnabled()` gates emission on crystal, RPM, stress, and redstone.
- Node lifecycle is driven through `GlareService` (`onNodeLoaded`, `updateNodeState`, `onNodeRemoved`).

## Related

- [[GLARE Networks]]
- [[Resonance Crystal]]
- [[GLARE Relay]]
- [[Relay Wrench]]
