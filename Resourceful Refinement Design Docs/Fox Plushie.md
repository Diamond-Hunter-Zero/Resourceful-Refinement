---
title: Fox Plushie
category: Decorative
status: Implemented
introduced: v0.2
recipe_type: n/a
related:
  - "[[Sports Ball]]"
  - "[[Codebase Overview]]"
  - "[[Primary Design Doc]]"
tags:
  - decorative
  - block
---

The Fox Plushie is a small decorative block — "an adorably mass-marketable vulpine companion for your engineering endeavours". It places at any of sixteen rotations, has four colour variants that cycle when it is right-clicked with shears, and is drawn by a custom block-entity renderer rather than a normal block model. It is the mod's shipped decorative stuffed toy; the Primary Design Doc frames it as one of the outputs of the planned Stuffing Sleave system.

## Gameplay Role

Pure decoration. The Fox Plushie exists to dress up a base — a collectible-style ornament with a few colourways — and carries no mechanical function. It sits alongside the [[Sports Ball]] as one of the mod's soft, "stuffed" novelty items.

## Acquisition

- **Crafting (shipped):** a shaped recipe (`recipe/shaped_crafting/plushie_shaped_crafting.json`) — four `minecraft:wool` (any colour) in the corners/sides, a `minecraft:copper_ingot` top-centre, and a `create:cogwheel` in the middle, yielding one `resourceful_refinement:fox_plushie`.
- Also available directly in the mod's creative tab.

> [!note] Implementation
> The Primary Design Doc's intended route is **Stuffing → Stuffing Sleave → Plushie**: a Plushie-variant Stuffing Sleave placed on a pipe and filled with the *stuffing* fluid would transform into the plushie. That Stuffing system is not implemented yet; the plushie currently ships with the shaped copper/wool/cogwheel recipe above. Keep the Stuffing Sleave route as design intent (see [[Primary Design Doc]]).

## Usage

- **Placement:** placed with a 16-way rotation (`ROTATION_16`), derived from the player's facing at placement, so it can be aimed in fine increments like a banner or skull.
- **Variant cycling:** right-click the placed plushie with `minecraft:shears` to advance its colour variant (`(variant + 1) % 4`), with a sheep-shear sound for feedback. The variant persists across saves.
- **Collision:** a slim 5×13.5×5 box centred in the block, so it reads as a small standing toy rather than a full cube.

## State & Data

- **Variant:** an `int` (0–3) stored on the block entity and saved as the `Variant` NBT tag; synced to clients via a block-entity update packet so the colour change is seen immediately.
- **Rotation:** the `ROTATION_16` blockstate property (0–15).

## Behaviour

The plushie has no tick logic — it is a static decorative block entity whose only state is its colour variant and placement rotation. Right-click-with-shears is its sole interaction.

## Implementation

- **Package:** `content/plushie/`
- **Block:** `PlushieBlock extends Block implements EntityBlock`; state `ROTATION` (`ROTATION_16`); render shape `ENTITYBLOCK_ANIMATED`; slim `getShape` box; `useItemOn` handles the shears variant-cycle.
- **Block entity:** `PlushieBlockEntity` — stores `variant` (NBT `Variant`), provides an update tag/packet for client sync.
- **Item:** `PlushieItem extends BlockItem` — installs `PlushieItemRenderer` as its custom BEWLR and appends the flavour tooltip.
- **Renderer / model:** `PlushieRenderer`, `PlushieModel`, `PlushieItemRenderer`.
- **Registry IDs:** block `ModBlocks.PLUSHIE` (`fox_plushie`), item `ModItems.PLUSHIE_ITEM` (`fox_plushie`), block entity `ModBlockEntities.PLUSHIE_BE` (`fox_plushie`).

## Related

- [[Sports Ball]] — the other "stuffed" novelty, and the other planned Stuffing Sleave output.
- [[Codebase Overview]] — where this feature sits in the registry/content map.
- [[Primary Design Doc]] — Stuffing & Stuffing Sleaves design intent.
