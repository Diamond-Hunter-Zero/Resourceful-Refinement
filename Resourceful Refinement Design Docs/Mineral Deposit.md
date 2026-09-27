---
title: Mineral Deposit
category: Worldgen
status: Partial
introduced: v0.4
recipe_type: resourceful_refinement:excavation
related:
  - "[[Bucket Excavator]]"
  - "[[Geyser Block]]"
  - "[[Primary Design Doc]]"
tags:
  - worldgen
  - block
---

The Mineral Deposit is the surface resource block that a [[Bucket Excavator]] extracts from. It is the surface-tier counterpart to the [[Crystal Fissure Bud]] (which the [[Drill Pylon]] taps) and the fluid-tier [[Geyser Block]].

**ID:** *mineral_deposit* (block + item)

## Where It Generates

The design intent is for Mineral Deposits to generate as surface deposits across the Overworld (and the End), each storing a particular resource type that determines what a Bucket Excavator parked over it produces.

> [!note] Implementation
> There is **no natural worldgen for Mineral Deposits yet**. `mineral_deposit` is registered but not placed by any feature, so it is currently obtainable only in creative or by manual placement. The dimensional / biome / rarity design above is not yet realised.

## Structure / Feature

A Mineral Deposit is a single block sitting on or near the surface. In the intended design it behaves like the block equivalent of the [[Geyser Block]]: it stores a reference to a resource/block type, uses that block's appearance, and can be set by a creative player. A [[Bucket Excavator]] whose excavation region overlaps a Mineral Deposit produces resources according to matching `excavation` recipes.

> [!note] Implementation
> `mineral_deposit` is currently a **plain `Block`** (`strength 2f`, stone sound) — not a block entity. It does **not** store a per-block resource type, and there is no creative "right-click to set stored block" behaviour (unlike the [[Geyser Block]] and [[Crystal Fissure Bud]], which are block entities). Consequently the [[Bucket Excavator]] matches `excavation` recipes only on the deposit's block id (via the recipe's `mined_block`), and the recipe's optional `mineral_deposit_type` clause has no runtime source to match against.

## Products

Via `excavation` recipes, a Mineral Deposit yields whatever items those recipes define when mined by a Bucket Excavator's wheel. It is also the default `mined_block` for the `resourceful_refinement:excavation` recipe type (see [[Bucket Excavator]] for the recipe format).

## Implementation

- **Registry:** `ModBlocks.MINERAL_DEPOSIT` — `BLOCKS.register("mineral_deposit", () -> new Block(Properties.of().strength(2f).sound(SoundType.STONE)))`.
- **Consumed by:** `resourceful_refinement:excavation` recipes (`ExcavationRecipe`), read by `BucketExcavatorBlockEntity` — see [[Bucket Excavator]].
- **Status:** Partial — block exists; worldgen generation and stored-resource-type behaviour are design targets for a later v0.4 pass.

## Related

- [[Bucket Excavator]]
- [[Crystal Fissure Bud]]
- [[Geyser Block]]
- [[Primary Design Doc]]
