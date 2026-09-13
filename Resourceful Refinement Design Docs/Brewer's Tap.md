#### **Description**
The Brewer's Tap is a horizontally rotatable block, which can be used to turn fluids into drinks.

**ID:** *brewers_tap*

#### **Behaviour**
A Brewer's Tap can be placed on the side of any Distillery, Fuel Tank, or Fluid Tank. Like the Paint Nozzle, it has two states; a "valve_open" state, and a (default) "valve_closed" state, which players can toggle between by right-clicking with an empty hand.

The Brewer's Tap also has an input item slot. Players can right-click the tap while holding an item to place that item into the input slot, or swap the held item for the currently stored itemstack. Players can also crouch-right-click with an empty hand to remove any stored itemstack from that slot.

When opened, a Brewer's Tap will check for any items placed on a depot or belt below it, as well as the fluid contents of its attached tank. If attached to a Distillery, the tap only checks against the output tank of the controller.

If both the item below and the tank fluid are valid inputs for a BrewingTapRecipe, the tap begins a processing cycle, in which it pours particle effects down onto the work-item for a short duration, then consumes the work item and specified fluid and spawns the product on the depot/belt, like a spout.

A BrewersTap should check that it still has valid criteria for a recipe at the start and end of a processing cycle, and well as check for new matching recipes whenever a processing cycle finishes, it's flavour item changes, the fluid contents/amount of its attached tank changes, the presence of an item on a below or depot below changes, or the when the tap is opened.

**Flavoring**
Flavours are an additional subsystem linked to the Brewer's Tap. They are enum types, stored on a data component placed on items produced by BrewingTapRecipes.

BrewingTapRecipes can specify a list of string "flavour tags" as part of their structure. When an itemstack matching one of those tags is inside the BrewerTap's input slot while it processes a work item, the itemstack's count is reduced by 1, and the 'Flavour' data component is applied to the output item with the matching tag.

A Flavour data-component applies a short status effect when the item is consumed as food, according to its matching tag. An item with a Flavour component also grants 2 additional saturation points when consumed. The different flavour tags and their effects are as follows:

| **Tag**         | **Effect**              |
| --------------- | ----------------------- |
| fruit_flavour   | Regeneration I - 5s     |
| sweet_flavour   | Speed I - 15s           |
| veg_flavour     | Jump Boost II - 15s     |
| spice_flavour   | Resistance I - 20s      |
| chilled_flavour | Fire Resistance I - 10s |
| cosmic_flavour  | Absorption II - 20s     |

*Example Recipe:*

| **Input Fluid**      | Input Item             | **Flavour Item** | Time | **Output Item**                                                                       |
| -------------------- | ---------------------- | ---------------- | ---- | ------------------------------------------------------------------------------------- |
| minecraft:milk 250mb | minecraft:glass_bottle | minecraft:apple  | 40t  | resourceful_resources:milk_glass (with Flavour component listing "fruit_flavour" tag) |
