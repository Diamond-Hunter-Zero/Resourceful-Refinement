---
title: Resonance Crystal
category: Worldgen
status: Partial
introduced: v0.4
recipe_type: n/a
related:
  - "[[GLARE Emitter Dish]]"
  - "[[GLARE Networks]]"
tags:
  - worldgen
  - block
  - glare
---

The Resonance Crystal is the mineral that powers a [[GLARE Emitter Dish]]. A dish placed directly above a Resonance Crystal is fed by it, satisfying the crystal requirement for Lux emission. It comes in a natural variant and an unstable artificial variant, both modelled as amethyst-copy blocks.

**ID:** *resonance_crystal* (natural), *artificial_resonance_crystal* (artificial)

## Gameplay Role

The Resonance Crystal gates the [[GLARE Emitter Dish]]: no crystal beneath the dish means no Lux, regardless of RPM. It is therefore the consumable/placement resource that anchors every GLARE power source. The natural crystal is the intended world-sourced form; the artificial variant is a crafted/synthetic stand-in that behaves the same for powering a dish but is volatile in the End.

## Where It Generates

> [!note] Implementation
> There is currently **no worldgen** that places the natural Resonance Crystal, and **no crafting recipe** for the artificial variant. Acquisition is unimplemented — both blocks are obtainable only via creative/`/give` for now. The design intent is for the natural crystal to be world-generated and the artificial crystal to be craftable, but neither path exists in code yet.

## Structure / Feature

Both variants are single blocks copied from amethyst's block properties. There is no multiblock structure; the crystal simply needs to sit directly below a [[GLARE Emitter Dish]] to power it.

## Products

- Powers a [[GLARE Emitter Dish]] placed directly above it (checked in the dish's `refreshEmitterState`, which accepts either the natural or artificial crystal below).

## Behaviour

- **Natural (`resonance_crystal`):** `artificial = false`. Inert; simply powers a dish above it.
- **Artificial (`artificial_resonance_crystal`):** `artificial = true`. On placement (`onPlace`) in the **End** dimension, it **explodes** with power `6.0` (`Level.ExplosionInteraction.BLOCK`) — placing it in the End destroys it and its surroundings. Outside the End it behaves like the natural crystal.

## Implementation

- **Block:** `ResonanceCrystalBlock` (extends vanilla `Block`), constructed with an `artificial` boolean flag.
- **Registry IDs:** blocks `resourceful_refinement:resonance_crystal` and `resourceful_refinement:artificial_resonance_crystal` (`ModBlocks.RESONANCE_CRYSTAL`, `ModBlocks.ARTIFICIAL_RESONANCE_CRYSTAL`); simple block items registered under the same paths in `ModItems`.
- **Properties:** amethyst-copy block properties.
- **End behaviour:** `onPlace` triggers `server.explode(..., 6.0F, BLOCK)` when `artificial` and `dimension() == Level.END`.
- **Dish coupling:** `GlareEmitterDishBlockEntity.refreshEmitterState` sets `hasCrystal` if the block below is either crystal variant.
- **Not implemented:** worldgen feature/placement for the natural crystal; crafting recipe for the artificial crystal.

## Related

- [[GLARE Emitter Dish]]
- [[GLARE Networks]]
