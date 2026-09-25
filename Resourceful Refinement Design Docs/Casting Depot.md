---
title: Casting Depot
category: Machine
status: Implemented
introduced: v0.1
recipe_type: n/a
related:
  - "[[Forge Mould]]"
  - "[[Coating]]"
tags:
  - machine
  - decorative
---

The Casting Depot is a Depot variant that acts as the dedicated working surface for the [[Forge Mould]]. Placed directly beneath a Forge Mould, it unlocks that machine's `"casting": true` recipes (fluid-only forge recipes whose item output is deposited onto the depot) and, when it holds a valid tool, its [[Coating]] recipes. It behaves like a normal Create Depot for item storage and belt/arm interaction, but is recognised by the Forge Mould as the trigger for casting/coating modes.

**ID:** `casting_depot`

## Gameplay Role

The Casting Depot exists to gate the Forge Mould's advanced modes. A generic surface (a belt or plain depot) lets the Forge Mould stamp standard item recipes; swapping in a Casting Depot as the surface tells the machine it may run fluid-only **casting** recipes and, with a tool resting on the depot, **coating** recipes that write a [[Coating]] onto that tool. It slots directly under the [[Forge Mould]] two blocks below the machine, with the intervening block left as air.

## Construction & Placement

The Casting Depot is a single block that extends Create's Depot. It is horizontally orientable (a `FACING` property is added on placement, facing the player) and otherwise places and behaves like a standard Depot — items can be dropped, deposited by mechanical arms, or carried onto it by belts.

To function as a casting surface it must be placed **two blocks directly below a [[Forge Mould]]**, with the block between them left empty, exactly like the Mechanical Press working-surface layout.

> [!note] Implementation
> `CastingDepotBlock extends DepotBlock` and adds `HorizontalDirectionalBlock.FACING` on top of the properties inherited from `DepotBlock` (such as `WATERLOGGED`). `getStateForPlacement` orients it toward the player. `getRenderShape` is `INVISIBLE` because the block is drawn by a custom block-entity renderer.

## Inputs & Outputs

As a Depot, the Casting Depot exposes a single-item surface via Create's `DepotBehaviour` item handler. In the casting/coating loop:

- **Casting recipes:** the depot must be **empty**; the Forge Mould inserts the recipe's item output onto the depot.
- **Coating recipes:** the depot must **hold a valid tool** (a `DiggerItem`, `SwordItem`, or `TridentItem`); the Forge Mould applies a coating to that tool in place.

The depot itself does not hold fluid or have kinetic connections — the fluid and rotational input belong to the [[Forge Mould]] above it.

> [!note] Implementation
> `CastingDepotBlockEntity extends DepotBlockEntity`. It exposes `GetDepotBehaviour()` and `getItemHandler()` (the `DepotBehaviour.itemHandler`). The Forge Mould reads/writes this handler through `Capabilities.ItemHandler.BLOCK` at `worldPosition.below(2)` and detects the depot via `instanceof CastingDepotBlock`, which sets `hasCastingDepot` on the recipe input.

## Operation

The Casting Depot has no processing logic of its own — all timing, kinetic requirements, and recipe resolution live in the [[Forge Mould]]. The depot's role is purely to be present (for casting recipes) and, optionally, to hold the tool being processed (for coating recipes). See [[Forge Mould]] for the full extend/impact/retract cycle and abort conditions (a running casting recipe aborts if its Casting Depot is removed).

## Rendering

The Casting Depot renders through a custom block-entity renderer using a bespoke model and texture, then delegates the floating-item-on-depot rendering to Create's standard `DepotRenderer`.

> [!note] Implementation
> `CastingDepotRenderer` (a `SafeBlockEntityRenderer`) bakes `CastingDepotModel` from `CastingDepotLayers.CASTING_DEPOT`, draws it with the `textures/block/casting_depot.png` texture rotated to the block's facing, then calls `DepotRenderer.renderItemsOf(...)` for items on the surface. The block item uses a custom `CastingDepotItemRenderer` (wired through `CastingDepotItem`'s `IClientItemExtensions`). View distance is 128.

## Implementation

- **Package:** `content/casting_depot/`
- **Block:** `CastingDepotBlock` (extends Create `DepotBlock`) — id `resourceful_refinement:casting_depot`. Adds `HorizontalDirectionalBlock.FACING`.
- **Block entity:** `CastingDepotBlockEntity` (extends `DepotBlockEntity`) — `ModBlockEntities.CASTING_DEPOT_BE`. Surfaces the Create `DepotBehaviour` item handler.
- **Item:** `CastingDepotItem` (`BlockItem`) with a custom in-hand/inventory renderer.
- **Rendering:** `rendering/CastingDepotRenderer`, `rendering/CastingDepotModel`, `rendering/CastingDepotLayers`, `rendering/CastingDepotItemRenderer`.
- **Consumed by:** the [[Forge Mould]], via the `mechanical_forge_mould` (`"casting": true`) and `coating` recipe types. The Casting Depot has no recipe type of its own.

## Related

- [[Forge Mould]]
- [[Coating]]
