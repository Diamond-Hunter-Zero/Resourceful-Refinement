---
title: Crystal Fissure Bud
category: Worldgen
status: Implemented
introduced: v0.4
recipe_type: resourceful_refinement:drill_pylon
related:
  - "[[Drill Pylon]]"
  - "[[Mineral Deposit]]"
  - "[[Geyser Block]]"
  - "[[Primary Design Doc]]"
tags:
  - block
  - worldgen
  - glare
---

The Crystal Fissure Bud is the crystal-tier resource node that a [[Drill Pylon]] is built over and taps. It is an amethyst-like block whose stored item selects the pylon's active recipe.

**ID:** *crystal_fissure_bud* (block + block entity)

## Where It Generates

The design intent is for Crystal Fissure Buds to generate naturally as crystal nodes for deep extraction, mirroring the surface [[Mineral Deposit]] and the fluid-tier [[Geyser Block]].

> [!note] Implementation
> There is **no natural worldgen for Crystal Fissure Buds yet** — the block is obtainable only via creative or manual placement. The generation design is a later target.

## Structure / Feature

A single amethyst-like block (registered from a full copy of `minecraft:amethyst_block`) placed on the ground. A [[Drill Pylon]] Head is placed directly on top of it, and the assembled pylon extracts from it.

Like the [[Mineral Deposit|Mineral Deposits]] and [[Geyser Block|Geysers]] pattern, the bud's block entity stores an `Item` reference (default `minecraft:raw_iron`). This stored item selects which `DrillPylonRecipe` a pylon assembled above it runs (`DrillPylonHeadBlockEntity.resolveSourceItem` reads the bud below). A creative player can right-click the bud with any item to set its stored source (`useItemOn`, gated on `player.isCreative()`), which shows an action-bar confirmation and syncs to clients.

## Products

Indirectly, whatever the matching `resourceful_refinement:drill_pylon` recipe yields — the bud itself produces nothing; it parameterises the [[Drill Pylon]] above it. See [[Drill Pylon]] for the recipe format and Lux behaviour.

## Implementation

- **Block:** `CrystalFissureBudBlock` (extends `BaseEntityBlock`, `RenderShape.MODEL`), id `crystal_fissure_bud`; registered via `ModBlocks.CRYSTAL_FISSURE_BUD` from `Properties.ofFullCopy(Blocks.AMETHYST_BLOCK)`.
- **Block entity:** `CrystalFissureBudBlockEntity` (`ModBlockEntities.CRYSTAL_FISSURE_BUD_BE`) — stores `Item sourceItem` (default `RAW_IRON`), persisted as `SourceItem`, synced via block-entity update packet.
- **Creative interaction:** right-click with an item sets `sourceItem` (creative only).
- **Consumed by:** [[Drill Pylon]] — `DrillPylonHeadBlockEntity` resolves the bud's `getSourceItem()` to pick its recipe.
- **Status:** Implemented (block + BE + creative source-setting); natural worldgen is a later target.

## Related

- [[Drill Pylon]]
- [[Mineral Deposit]]
- [[Geyser Block]]
- [[Primary Design Doc]]
