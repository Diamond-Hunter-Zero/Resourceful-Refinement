---
title: Prism Cell
category: Item
status: Planned
introduced: v0.4
recipe_type: n/a
related:
  - "[[GLARE Networks]]"
  - "[[Resource Resonator]]"
tags:
  - feature
---

The Relay Wrench is the link editor for [[GLARE Networks|GLARE networks]]. It is a two-click tool for creating links between GLARE nodes and, with a crouch-click, for bulk-unlinking a node. It complements the placement-target workflow carried by GLARE node block-items themselves.

**ID:** *low_grade_prism_cell*, *mid_grade_prism_cell*, *high_grade_prism_cell*, *creative_prism_cell*

## Acquisition

Crafted via Resourceful Refinement machines and Create/Vanilla crafting. Each tier of cell has a distinct recipe and set of ingredients.
Recipes to be determined later.

## Usage

Prism Cells function as both a typical 'crafting-ingredient' item, and as a form of exchangeable battery for certain blocks and items. Prism Cells come in three different tiers, each progressively harder to obtain, and dependent upon obtaining the previous tier.

Prism Cells carry an amount of 'charge', which can be consumed by some blocks and items to power functionality.

Prism Cell begins with full charge when crafted, and have no expected way to replenish charge (although API is to be provided). Player therefore have to build a reliable supply and logistic chain of cells.

## State & Data

- **`prismCellBatteryComponent`**: A Data Component which stores the tier index of the cell (0 for Creative, 1-3 for remaining tiers), the maximum amount of charge it can hold, and it's current remaining charge. All prism cells carry this component and prepopulate it on creation (pre-filled with charge).
- There are four variants of the prism cell item, each with a distinct texture and data component preset, but shared JSON item model
- All items with a `prismCellBatteryComponent` are interchangeable with the interfaces below

## Behaviour

- On creation, a prism cell is initialised with a `prismCellBatteryComponent` (values determined by a tier preset)

- Prism cell data can be surmised by a `PrismCell` class, which shares the following behaviour. Blocks and items can define `PrismCell` members in order to store data representations of their current prism cell state.
	- A `DrainCellCharge(int amount, bool destroyOnEmpty)` which reduces the stored amount of charge, and optionally destroys the instance on consumption. Returns the amount actually drained.
	- A `FillCellCharge(int amount)` which adds stored charge, up to the maximum. Returns the amount actually filled.
	- Accessors for the cell's charge, max charge, and tier
	- This class may need to integrate with, or be part of the `prismCellBatteryComponent`
	
- Blocks may choose to implement an `IPrismCellBlockConsumer` interface, which shares the following behaviour:
	- Right-clicking a `IPrismCellBlockConsumer` which has no stored `PrismCell` while holding an item with a `prismCellBatteryComponent` removes that item (or reduces the stack by 1) and populates the stored `PrismCell` from the data component's data.
	- Crouch-Right-clicking a `IPrismCellBlockConsumer` with a stored `PrismCell` while empty-handed removes the prism cell data (setting it to null) and instantiates a new prism cell item in the player's inventory with a `prismCellBatteryComponent` populated from the `PrismCell` data.
	- Blocks will typically consume an amount of charge to perform particular functions. If a consumption entirely empties the current  `PrismCell`, it will typically destroy the `PrismCell` instance (set to null).
	
- Items may choose to implement an `IPrismCellItemConsumer` interface, which shares the following behaviour:
	- Right-clicking on a `IPrismCellItemConsumer` which has no stored `PrismCell` while in an inventory and the cursor is holding an item with a `prismCellBatteryComponent` removes that item (or reduces the stack by 1) from the cursor and populates the stored `PrismCell` from the data component's data. (See Item Bundle behaviour)
	- Right-clicking on an available empty while in an inventory and the cursor is holding a `IPrismCellItemConsumer` with a stored `PrismCell` removes the prism cell data (setting it to null) and instantiates a new prism cell item in the target slot with a `prismCellBatteryComponent` populated from the `PrismCell` data. (See Item Bundle behaviour)
	- Items will typically consume an amount of charge to perform particular functions. If a consumption entirely empties the current  `PrismCell`, it will typically destroy the `PrismCell` instance (set to null).


## Implementation

- **Item:** TODO
- **Client renderer:** TODO
- **Block-item:** TODO
- **Data components:** TODO
- **Registry ID:** TODO

## Related

TODO