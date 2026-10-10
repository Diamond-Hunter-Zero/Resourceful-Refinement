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

Prism Cells are a late-game crafting resource and power-source for specialised items and blocks. They carry a charge which blocks and items can consume, and can be interchanged with each other.

**ID:** *low_grade_prism_cell*, *mid_grade_prism_cell*, *high_grade_prism_cell*, *creative_prism_cell*

## Acquisition

Crafted via Resourceful Refinement machines and Create/Vanilla crafting. Each tier of cell has a distinct recipe and set of ingredients.
Recipes to be determined later.

## Usage

Prism Cells function as both a typical 'crafting-ingredient' item, and as a form of exchangeable disposable battery for certain blocks and items. Prism Cells come in three different tiers, each progressively harder to obtain, and dependent upon obtaining the previous tier.

Prism Cells carry an amount of 'charge', which can be consumed by some blocks and items to power functionality.

Prism Cell begins with full charge when crafted, and have no expected way to replenish charge (although API is to be provided). Player therefore have to build a reliable supply and logistic chain of cells.

The 4 prism cell items are distinct IDs (and therefore are *not* interchangeable in terms of being used in recipes, ID matching, etc...). The Creative Prism Cell is a special case item, similar to Create's Creative motor, tanks, etc..., and does not drain or fill with charge - It always has maximum charge.

## State & Data

- **`prismCellBatteryComponent`**: A Data Component which stores the tier index of the cell (0 for Creative, 1-3 for remaining tiers), the maximum amount of charge it can hold, and it's current remaining charge. All prism cells carry this component and prepopulate it on creation (pre-filled with charge).
- There are four variants of the prism cell item, each with a distinct texture and data component preset, but shared JSON item model
- All items with a `prismCellBatteryComponent` are interchangeable with the interfaces below


| **ID**                | **Tier** | **Max Charge** |
| --------------------- | -------- | -------------- |
| creativeprism_cell    | 0        | 256000         |
| low_grade_prism_cell  | 1        | 4000           |
| mid_grade_prism_cell  | 2        | 16000          |
| high_grade_prism_cell | 3        | 64000          |


## Behaviour

- On creation, a prism cell is initialised with a `prismCellBatteryComponent` (values determined by a tier preset)

- Crafting recipes using prism cells should require a full charged cell to work

- Prism cell data can be surmised by a `PrismCell` class, which shares the following behaviour. Blocks and items can define `PrismCell` members in order to store data representations of their current prism cell state.
	- A `DrainCellCharge(int amount, bool destroyOnEmpty)` which reduces the stored amount of charge, and optionally destroys the instance on consumption. Returns the amount actually drained.
	- A `FillCellCharge(int amount)` which adds stored charge, up to the maximum. Returns the amount actually filled.
	- Accessors for the cell's charge, max charge, and tier
	- This class may need to integrate with, or be part of the `prismCellBatteryComponent`
	
- Blocks may choose to implement an `IPrismCellBlockConsumer` interface, which shares the following behaviour:
	- Right-clicking a `IPrismCellBlockConsumer` which has no stored `PrismCell` while holding an item with a `prismCellBatteryComponent` removes that item (or reduces the stack by 1) and populates the stored `PrismCell` from the data component's data.
	- Crouch-Right-clicking a `IPrismCellBlockConsumer` with a stored `PrismCell` while empty-handed removes the prism cell data (setting it to null) and instantiates a new prism cell item in the player's inventory with a `prismCellBatteryComponent` populated from the `PrismCell` data.
	- Blocks will typically consume an amount of charge to perform particular functions. If a consumption entirely empties the current  `PrismCell`, it will typically destroy the `PrismCell` instance (set to null).
	- The stored  `PrismCell` data needs to be preserved if the block is broken and dropped as an item, and restored on placement.
	
- Items may choose to implement an `IPrismCellItemConsumer` interface, which shares the following behaviour:
	- Right-clicking on a `IPrismCellItemConsumer` which has no stored `PrismCell` while in an inventory and the cursor is holding an item with a `prismCellBatteryComponent` removes that item (or reduces the stack by 1) from the cursor and populates the stored `PrismCell` from the data component's data. (See Item Bundle behaviour)
	- Right-clicking on an available empty while in an inventory and the cursor is holding a `IPrismCellItemConsumer` with a stored `PrismCell` removes the prism cell data (setting it to null) and instantiates a new prism cell item in the target slot with a `prismCellBatteryComponent` populated from the `PrismCell` data. (See Item Bundle behaviour)
	- Items will typically consume an amount of charge to perform particular functions. If a consumption entirely empties the current  `PrismCell`, it will typically destroy the `PrismCell` instance (set to null).


## Implementation

Implemented in v0.4 under the new `content/prism_cell/` package.

- **Items:** `PrismCellItem` (`low_grade_prism_cell`, `mid_grade_prism_cell`, `high_grade_prism_cell`,
  `creative_prism_cell`), registered in `ModItems`. Each carries a preset `prism_cell_battery` default component
  (full charge on creation) and shows a charge tooltip + durability-style charge bar. All four share the
  `item/prism_cell` geometry model, differing only by texture (per-grade child models under
  `models/item/`). Note the creative item id is `creative_prism_cell` while its texture file is
  `creative_grade_prism_cell.png`.
- **Data component:** `PrismCellBatteryComponent` (record: `tier`, `max_charge`, `charge`) registered as
  `prism_cell_battery` in `ModDataComponents`.
- **Runtime state:** `PrismCell` — a mutable holder blocks/items keep while a cell is installed, with
  `drainCellCharge(amount, destroyOnEmpty)`, `fillCellCharge(amount)`, charge/max/tier accessors, and
  NBT + item-stack conversion. `PrismCellTier` holds the four grade presets.
- **Block interface:** `IPrismCellBlockConsumer` — default `tryInsertCell` / `tryExtractCell` helpers plus
  `getPrismCell`/`setPrismCell`/`onPrismCellChanged`. The `IPrismCellItemConsumer` item variant from the design
  above is **not yet implemented** (no item consumer exists yet).

### Resource Resonator integration

The [[Resource Resonator]] is the first `IPrismCellBlockConsumer`:

- Right-click with a Prism Cell installs it; crouch-right-click empty-handed removes it. The installed cell is
  persisted on the block entity, preserved into the dropped block item on break (via `getDrops` +
  `prism_cell_battery` component) and restored on placement (`ResourceResonatorBlockItem`).
- Deploying a **Ping Beacon** consumes `ping_charge_cost` (default **1000**) from the installed cell; with no
  cell or insufficient charge the ping fails (GUI shows the reason). A drain that empties a non-creative cell
  destroys it.
- The installed grade sets the **scan radius** (server config): base `8` (no cell) / low `16` / mid `32` /
  high (and creative) `48` chunks. Scanning still works with no cell at the base radius.
- The radar GUI shows an installed-cell readout (grade + charge bar) and blocks pinging when the cell can't fire.

## Related

- [[Resource Resonator]] — first block to consume Prism Cells
- [[Codebase Overview]]