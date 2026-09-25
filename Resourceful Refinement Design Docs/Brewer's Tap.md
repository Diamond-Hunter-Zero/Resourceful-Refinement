---
title: Brewer's Tap
category: Machine
status: Implemented
introduced: v0.3
recipe_type: resourceful_refinement:brewers_tap
related:
  - "[[Fluid Properties]]"
  - "[[Distillery]]"
  - "[[Fuel Tank]]"
tags:
  - machine
  - fluid
  - kinetic
  - food
---

The Brewer's Tap is a horizontally rotatable block which turns fluids into drinks. Mounted on the side of a fluid-holding block, it pours a stored fluid onto a work item below to produce drinks such as milkshakes, hot chocolate, energy drinks, mead, and spirits — optionally imbuing them with a 'Flavour' that grants a short status effect when consumed.

**ID:** `brewers_tap`

## Gameplay Role

The Brewer's Tap is the drink-crafting machine: it consumes a fluid (from an attached tank) plus a work item on a depot or belt below and spouts a finished drink. Its Flavour subsystem lets a secondary ingredient add a temporary status effect to the output, giving a light food/buff progression on top of the fluid systems.

## Construction & Placement

A Brewer's Tap can be placed on the side of any [[Distillery]], [[Fuel Tank]], or Fluid Tank. It is horizontally directional (`FACING` set opposite the player's look direction on placement). Like the [[Paint Nozzle]], it has two states via the `valve_open` blockstate property: a `valve_open` state and a default `valve_closed` (i.e. `valve_open = false`) state, toggled by right-clicking with an empty hand (a wooden-trapdoor open/close sound plays on toggle).

## Inputs & Outputs

- **Attached tank (fluid input):** the fluid contents of the block the tap is mounted on. If attached to a [[Distillery]], the tap only checks against the controller's **output** tank.
- **Flavour item slot:** a single-slot input inventory (`flavourInv`). Right-click the tap while holding an item to place it into the slot, or swap the held item for the currently stored stack; crouch-right-click with an empty hand removes the stored stack.
- **Work item (item input):** items resting on a depot or belt directly below the tap.
- **Output:** the finished drink is spawned onto the depot/belt below, spout-style.

## Operation

When opened, a Brewer's Tap checks for any items on a depot or belt below it, plus the fluid contents of its attached tank. If both the item below and the tank fluid are valid inputs for a `BrewersTapRecipe`, the tap begins a processing cycle: it pours particle effects down onto the work item for a short duration, then consumes the work item and the specified fluid and spawns the product below, like a spout.

A Brewer's Tap re-checks that it still has valid criteria for a recipe at the start and end of a processing cycle, and looks for new matching recipes whenever: a processing cycle finishes, its flavour item changes, the fluid contents/amount of its attached tank changes, the presence of an item on a belt or depot below changes, or the tap is opened. It also supports processing items travelling along a belt beneath it (`handleBeltProcessing`), holding the belt item at the tap during a cycle.

### Flavouring

Flavours are an additional subsystem linked to the Brewer's Tap. They are enum types (`FlavourType`), stored on the `flavour` data component placed on items produced by `BrewersTapRecipe`s.

`BrewersTapRecipe`s can specify a list of string "flavour tags" (`flavour_tags`). When an itemstack matching one of those tags is inside the tap's flavour slot while it processes a work item, that stack's count is reduced by 1, and the 'Flavour' data component matching the tag is applied to the output item.

A Flavour data-component applies a short status effect when the item is consumed as food, according to its tag. The design also grants +2 additional saturation points on consumption. The flavour tags and their effects:

| **Tag**         | **Constant** | **Display** | **Colour**  | **Effect**              |
| --------------- | ------------ | ----------- | ----------- | ----------------------- |
| fruit_flavour   | `FRUIT`      | Fruity      | `#F05C5C`   | Regeneration I – 5s     |
| sweet_flavour   | `SWEET`      | Sugary      | `#F5C84C`   | Speed I – 15s           |
| veg_flavour     | `VEG`        | Vegetal     | `#6FBD55`   | Jump Boost II – 15s     |
| spice_flavour   | `YEAST`      | Spicy       | `#D6B77B`   | Resistance I – 20s      |
| chilled_flavour | `CHILLED`    | Chilled     | `#67CFE8`   | Fire Resistance I – 10s |
| cosmic_flavour  | `COSMIC`     | Cosmic      | `#A66BFF`   | Absorption II – 20s     |
| unknown_flavour | `UNKNOWN`    | Unknown     | `#252A35`   | None (fallback)         |

## Recipes

Recipe type id: `resourceful_refinement:brewers_tap` (serializer `resourceful_refinement:brewers_tap`).

`BrewersTapRecipe` extends Create's `StandardProcessingRecipe`, so it uses the standard processing-recipe JSON (one item ingredient, one fluid ingredient, one item result, a specifiable duration), with an added optional `flavour_tags` string list. Max one item input, one fluid input, one item output, no fluid output.

*Example recipe:*

| **Input Fluid**      | Input Item             | **Flavour Item** | Time | **Output Item**                                                                       |
| -------------------- | ---------------------- | ---------------- | ---- | ------------------------------------------------------------------------------------- |
| minecraft:milk 250mb | minecraft:glass_bottle | minecraft:apple  | 40t  | resourceful_resources:milk_glass (with Flavour component listing "fruit_flavour" tag) |

## Rendering

`BrewersTapRenderer` (BER) handles the tap's visual state and the pour/particle effects during processing.

## Implementation

- Package: `content/brewers_tap/`.
- **Block:** `BrewersTapBlock` — `FACING` + `valve_open` blockstate properties; right-click toggles the valve, manages the flavour slot, and drives interaction.
- **Block entity:** `BrewersTapBlockEntity` — single-slot `flavourInv`, processing timer/duration (`processingDuration`, default fallback used when a recipe omits one), depot and belt processing (`DepotBehaviour`, `handleBeltProcessing`), recipe re-evaluation on state changes.
- **Recipe:** `content/brewers_tap/recipe/` — `BrewersTapRecipe` (+ `Serializer`), `BrewersTapRecipeInput`, `BrewersTapRecipeCategory` (JEI).
- **Flavours:** `FlavourType` enum — tag name, display name, colour, and effect factory; `CODEC` + `STREAM_CODEC`; `ALL_FLAVOURS_ITEM_TAG` (`all_flavour_tags`) plus per-flavour item tags; `fromTagName` / `unsafeFromTagName` lookups falling back to `UNKNOWN`.
- **Data component:** `flavour`, registered in `registry/ModDataComponents` (`FLAVOUR`).
- **Effect application:** `registry/ModToolEvents.onItemConsumed` reads the `flavour` component on `LivingEntityUseItemEvent.Finish` and adds the flavour's effect (server-side).
- **Drink items:** `DrinkItem` (drink use animation/sound) in `registry/ModItems` — `milkshake`, `hot_chocolate_mug`, `energy_drink`, `mead_tankard`, `spirits_bottle` (each with its own `FoodProperties`), plus the empty `drinks_glass`.
- **Recipe registration:** `registry/ModRecipeTypes` — `BREWERS_TAP_TYPE`, `BREWERS_TAP_SERIALIZER`, `BREWERS_TAP_TYPE_INFO`.

> [!note] Implementation
> `FlavourType` in code adds an `UNKNOWN` fallback flavour (grey, no effect) used when a tag cannot be resolved. The status effects match the design table exactly. The +2 saturation-on-consume detail is design intent; effect application in `ModToolEvents.onItemConsumed` adds only the mob effect, so any saturation bonus would come from each drink's `FoodProperties` rather than the flavour component itself.

## Related

- [[Fluid Properties]]
- [[Distillery]]
- [[Fuel Tank]]
- [[Paint Nozzle]]
