---
title: Distillery
category: Machine
status: Implemented
introduced: v0.3
recipe_type: resourceful_refinement:distillery
related:
  - "[[ExtendedHeatCondition]]"
  - "[[Radiator]]"
  - "[[Primary Design Doc]]"
tags:
  - machine
  - multiblock
  - heat
  - fluid
---

The Distillery is a vertically stackable tower that refines an item + fluid input into a fluid output using heat and time. Distillery blocks placed on top of one another form a single tower whose height is part of the recipe: a recipe specifies exactly how tall the tower must be, and the tower needs a heat source of the right [[ExtendedHeatCondition]] beneath it. Once the conditions are met the tower runs a long fermentation, and inputs are consumed and outputs produced only when that fermentation finishes.

## Gameplay Role

Distilleries are the mod's slow, batch-style fluid-refinement machines: where the [[Radiator]] and [[Combustion Chamber]] handle heat and power, the Distillery turns raw ingredients (crops, biomatter, carborax) plus a fluid into refined fluids — syrups, spirits, mead, diesel, glue, sludge and so on. The long fermentation time and the exact height + heat requirements make each recipe a small build-and-wait puzzle rather than an instant conversion.

## Construction & Placement

- Each block is a `HorizontalDirectionalBlock` (`FACING`). Stacking them vertically forms a tower; connectivity is recomputed on placement, removal and neighbour changes.
- A tower segment is between 2 and 8 blocks tall (`MIN_STACK_HEIGHT = 2`, `MAX_STACK_HEIGHT = 8`). A column taller than 8 is split into consecutive segments, each an independent tower with its own controller.
- The **controller** is the bottom block of a segment (`stackIndex == 0`); every block stores its `controllerPos`, `stackIndex` and `stackSize`. Only the controller runs recipe processing; non-controllers zero their timer and delegate.
- Each block picks a `MODEL_TYPE` (0 single, 1 bottom, 2 middle, 3 top) so the tower renders as a continuous vessel, and non-controller members are re-oriented to match the controller's facing.
- A **heat source must sit directly beneath the controller.** Per design intent this can be passive (fire, campfire, magma, lava, …), a Chilled [[Radiator]], or a Heated / Superheated blaze burner — whatever satisfies the recipe's required heat condition. The controller reads the block below with `HeatUtilities.GetExtendedHeatLevel`.

## Inputs & Outputs

Capabilities are exposed positionally (all backed by the controller):

- **Fluid input:** the controller's `inputTank` (8000 mB), accepted on the bottom block's side faces except the front and the up/down faces.
- **Item input:** the controller's single-slot `inputInv` (`ItemStackHandler(1)`), accepted on the bottom block's **front** face (`FACING`).
- **Fluid output:** the controller's `outputTank` (8000 mB), exposed on the **top** block's up face only.
- **Filter:** the top block carries a Create `FilteringBehaviour` (value box on its top face) used to select which recipe to run when several match; changing any filter in the stack resets recipe matching on the controller.

## Operation

1. The controller gathers its item slot + input tank into a `DistilleryRecipeInput` and looks for a matching `distillery` recipe (respecting the top block's filter).
2. On a match it sets `timer` to the recipe's processing duration and begins counting down. Progress is shown on the goggle tooltip as a progress bar + remaining time.
3. Each tick `canProcess()` re-validates the recipe. It must still match the filter, have enough input fluid and items, sit on the **exact** required heat condition, be the **exact** required height, and have room in the output tank. Any failure is surfaced through `DistilleryErrorCode` (see below).
4. When the timer reaches zero, `process()` runs once: it drains the item and fluid inputs and fills the output tank. Nothing is consumed or produced mid-fermentation.

**Overpressure / explosion:** if the recipe finishes but the output tank is full (`OUTPUT_SPACE` error), the tower builds `tankFullTime`; it plays a train-whistle warning as it approaches `EXPLOSIVE_DISTILLERY_TIME` (200 ticks) and then explodes at intervals up the tower. Other error states simply pause processing and reset the timer.

**Error codes** (`DistilleryErrorCode`): `NO_RECIPE`, `NO_FILTER_MATCH` ("No recipes match filter"), `INPUT_FLUID` ("Insufficient fluid"), `INPUT_ITEM` ("Insufficient items"), `HEAT` ("Distillery must be <condition>"), `STACK_HEIGHT` ("Distillery must be <n> blocks tall"), `OUTPUT_SPACE` ("Output tank full"), `NO_ERROR`.

## Recipes

- **Type id:** `resourceful_refinement:distillery`
- Built on Create's `StandardProcessingRecipe` with a custom serializer. A recipe adds two fields on top of the standard processing params: `heat_requirement` (an [[ExtendedHeatCondition]], defaulting to `none`) and `distillery_height` (clamped to 2–8). Item ingredients can be given either as plain `ingredients` (count 1) or as `sized_ingredients` (with an explicit count); both are combined for matching and draining.
- Limits: max 1 item input, max 1 fluid input, 0 item outputs, up to 2 fluid outputs, and it may specify a duration and require heat.

Sample (`recipe/distillery/carborax_diesel_distilling.json`):

```json
{
  "type": "resourceful_refinement:distillery",
  "heat_requirement": "heated",
  "distillery_height": 4,
  "processing_time": 15000,
  "ingredients": [
    {
      "type": "neoforge:single",
      "fluid": "resourceful_refinement:unrefined_carborax",
      "amount": 2000
    }
  ],
  "sized_ingredients": [
    {
      "item": "resourceful_refinement:compacted_biomatter",
      "count": 6
    }
  ],
  "results": [
    {
      "id": "resourceful_refinement:carborax_diesel",
      "amount": 3500
    }
  ]
}
```

Fourteen distillery recipes ship in `data/resourceful_refinement/recipe/distillery/`, including syrups (beets, berries, honey, sugar), spirits (apple, potato), organic mush, mead, hot chocolate, liquid glue, polymer sludge, carborax diesel and overcharged carborax. Processing times are long (thousands of ticks), reflecting the fermentation design.

## Rendering

`DistilleryRenderer` (BER) with a baked `DistilleryModel` (`DISTILLERY_MODEL_LAYER`). The `MODEL_TYPE` blockstate selects the segment appearance (single / bottom / middle / top) so a stack reads as one tall vessel. The filter value box renders flat on the top block's top face.

## Implementation

- **Package:** `content/distillery/` (recipes under `content/distillery/recipe/`)
- **Block:** `DistilleryBlock` — `HorizontalDirectionalBlock`, `EntityBlock`, `IBE<DistilleryBlockEntity>`; state `FACING`, `MODEL_TYPE` (0–3).
- **Block entity:** `DistilleryBlockEntity` — `SmartBlockEntity`, `IHaveGoggleInformation`. Holds `inputTank`, `outputTank`, `inputInv`, `FilteringBehaviour`, and the multiblock fields `controllerPos` / `stackIndex` / `stackSize` / `heatLevel`.
- **Status enum:** `DistilleryErrorCode`.
- **Recipe:** `DistilleryRecipe` (extends `StandardProcessingRecipe`, with inner `Serializer` and `DistilleryProcessingRecipeParams`), `DistilleryRecipeInput`, `DistilleryRecipeCategory` (JEI).
- **Renderer / model:** `DistilleryRenderer`, `DistilleryModel`.
- **Registry IDs:** block `ModBlocks.DISTILLERY` (`distillery`), item `ModItems.DISTILLERY_ITEM`, block entity `ModBlockEntities.DISTILLERY_BE`, recipe type/serializer `ModRecipeTypes.DISTILLERY_TYPE` / `DISTILLERY_SERIALIZER` (`DISTILLERY_TYPE_INFO`).
- **Capabilities:** `FluidHandler.BLOCK` (input on bottom sides, output on top up-face) and `ItemHandler.BLOCK` (item input on bottom front face), both routed to the controller in `ResourcefulRefinementMain.registerCapabilities`.
- **Constants:** `TANK_CAPACITY = 8000`, `MIN_STACK_HEIGHT = 2`, `MAX_STACK_HEIGHT = 8`, `EXPLOSIVE_DISTILLERY_TIME = 200`.

## Related

- [[ExtendedHeatCondition]] — supplies the recipe's `heat_requirement` and the tower's heat reading.
- [[Radiator]] — a Chilled radiator is one valid heat source beneath a tower.
- [[Primary Design Doc]] — v0.3 Distillery design intent.
