---
title: Fluid Refinery
category: Machine
status: Implemented
introduced: v0.1
recipe_type: resourceful_refinement:fluid_refinery
related:
  - "[[Blender Blade]]"
  - "[[Detailed Fluid Refinery Implementation Plan]]"
  - "[[Fluid Processing Recipes]]"
  - "[[Codebase Overview]]"
tags:
  - machine
  - multiblock
  - kinetic
  - heat
  - fluid
---

The **Fluid Refinery** is a large multiblock structure that acts as another crafting station for
Create, specialising in combining fluids and items with each other. It carries internal tanks for
two input fluids and one output fluid, and accepts up to two kinds of input item. The refinery is
used to improve the quality of molten minerals, create molten alloys, and refine combustible fuel.
It is driven by rotational input through a central [[Blender Blade]] column and can be heated by
fuel or by a [[Radiator]] beneath it.

## Gameplay Role

The refinery sits at the heart of the mod's fluid refinement loop: molten ores are catalysed,
alloyed or purified, and combustible fuels are refined, all through `fluid_refinery` recipes. It is
a heat- and rotation-gated processing station — the player must supply both a spinning shaft and an
appropriate heat level for a recipe to proceed, making it a mid-progression factory centrepiece
rather than an early-game block.

## Construction & Placement

The Fluid Refinery is assembled as a multiblock with a base footprint and a minimum height of 3
blocks, extendable up to a configurable maximum. The top and bottom layers always follow the same
pattern; the middle layers are a single slice repeated vertically.

- **Bottom layer** — a square of Copper Casings (`create:copper_casing`) with an (empty) Blaze
  Burner (`create:blaze_burner`) in each corner. In the centre of one edge sits the **Refinery
  Access Port** (`refinery_access_port`). This denotes the *front face* of the refinery, to which
  all other block placement is orientated.
- **Middle layers** — a solid square of glass with a [[Blender Blade]] (`blender_blade`) in the
  centre.
- **Top layer** — another square of glass with a Blender Blade in the centre. The two *back* corners
  are each a Fluid Tank (`create:fluid_tank`); the two *front* corners are each an Item Vault
  (`create:item_vault`).

Right-clicking an unassembled Refinery Access Port scans for a valid structure. On success it
replaces every block in the structure's region — except the Access Port itself — with invisible
proxy blocks, and the Access Port renders a procedural [BlockEntityRenderer](#Rendering)
representing the entire multiblock. If no valid structure is found, a warning is printed to the
player's action bar describing the first block that failed validation.

> [!note] Implementation
> The design intent allows a **3×3 or 5×5** base, but `RefineryStructureHelper` only validates and
> assembles a **3×3** footprint. Height ranges from **3 to `MAX_HEIGHT` (8)**
> (`RefineryAccessPortBlockEntity.MAX_HEIGHT`). Height is auto-detected top-down by
> `detectHeight()` — the tallest valid stack of middle layers plus a valid top layer wins. The
> glass check accepts anything in the `minecraft:impermeable` block tag as well as `glass` and
> `tinted_glass`. Blaze Burners must be the empty variant (`AllBlocks.BLAZE_BURNER`, not the lit
> one).

## Inputs & Outputs

I/O faces on the assembled structure are strictly sided, delegated from the proxy blocks to the
controller:

- **Fluid input** — only the **top faces** of the two **back top corners** (where the Fluid Tanks
  were placed). The west back corner feeds Tank A, the east back corner feeds Tank B.
- **Item input** — the **outward side faces** of the two **front top corners** (where the Item
  Vaults were placed). Excludes the up face and the front (Access-Port-facing) face.
- **Fuel input** — the **outward side faces** of the four **bottom-layer corner blocks**. Fuel
  raises the heat level for a duration equal to its burn time. Fuel may also be inserted by
  right-clicking a fuel item on those faces, or directly on the Access Port when assembled.
- **Fluid output** — the **front face** of the Refinery Access Port is the only fluid output.
- **Rotational input** — the central [[Blender Blade]] column carries rotation; when assembled, the
  centre cells become kinetic proxies linked to the network (see [Operation](#Operation)).

Internal storage lives on the controller block entity:

| Storage | Type | Capacity |
|---|---|---|
| Input Tank A | `FluidTank` | `(height − 2) × 1000` mB (assembled); 1000 mB default |
| Input Tank B | `FluidTank` | `(height − 2) × 1000` mB (assembled); 1000 mB default |
| Output Tank | `FluidTank` | `(height − 2) × 1000` mB (assembled); 1000 mB default |
| Item Input | `ItemStackHandler` (2 slots) | 64 per slot; the two slots must hold **distinct** items |
| Fuel | `ItemStackHandler` (1 slot) | 64 |

> [!note] Implementation
> Tank capacity is dynamic — `getDynamicTankCapacity()` returns `(structureHeight − 2) × 1000` mB
> once assembled (so a minimum 3-high refinery holds 1000 mB per tank, growing with height), and
> resets to `DEFAULT_TANK_CAPACITY` (1000 mB) when disassembled. The item-input handler's
> `isItemValid` rejects a stack whose item already occupies the other slot, enforcing two *kinds*
> of item ingredient.

## Operation

The multiblock behaves as one giant block. Right-clicking any assembled block opens the refinery's
interaction path (currently: fuel insertion, filter adjustment, and empty-hand inventory
clear-out); breaking, moving or replacing any block in the assembled structure triggers
disassembly, restoring the region to its original blocks.

The Access Port and the proxies use a **Controller/Proxy** relationship. Each proxy knows its
controller's position and its own local offset within the structure. The controller owns all fluid
and item inventories, crafting logic and sided capability handlers; proxies simply forward
capability and interaction queries — keyed by their relative position and the queried face — to the
controller.

Crafting requires **both** rotation and the correct heat:

- **Rotation** — the central kinetic proxies must spin at least `(height − 2) × 32` RPM
  (`getSpeedRequirement()`), read from the `RefineryKineticProxyBlockEntity`. Below that, crafting
  stalls.
- **Heat** — each recipe declares a required [[ExtendedHeatCondition]]. The effective heat level is
  the higher of the fuel heat and any [[Radiator]] heat sensed directly below a base block. Heat is
  matched **exactly** (`heatLevel == required`), not "at least".
- **Fuel** — burning fuel sets `HEATED`; Create's Blaze Cake sets `SUPERHEATED`. Non-cake fuel is
  capped at 10000 ticks of stored burn time and cannot override an active superheated state.
- **Processing** — while spinning and heated, `craftingProgress` climbs toward the recipe duration,
  scaled by `StructureProcessingSpeedModifier()` = `0.75 + (height − 3) × 0.25` (taller refineries
  process faster). On completion the output tank is filled and item/fluid inputs are consumed. If
  the output tank cannot hold the result, processing pauses.

A `FilteringBehaviour` on the middle-front block lets the player pin which recipe output the
refinery should target when inputs match more than one recipe.

Goggle tooltips (`IHaveGoggleInformation`) report assembly state, current stress draw, heat level
and source (fuel vs radiator), item inputs, tank contents and a crafting progress bar.

## Recipes

The Fluid Refinery is a Create-style crafting station using the recipe type
`resourceful_refinement:fluid_refinery`, serialised as JSON for the mod and datapacks. A recipe
takes up to **2 fluids** and up to **2 item types**, requires rotational input to process, and may
specify a heat level. It always outputs a fluid.

Current in-repo recipe format (single fluid ingredient shown; items use `{ "item": ... }`, fluids
use `{ "type": "neoforge:single", "fluid": ..., "amount": ... }`, and a fluid result uses the
`FluidStack` codec's `id`/`amount` keys):

```json
{
  "type": "resourceful_refinement:fluid_refinery",
  "heat_requirement": "heated",
  "processing_time": 120,
  "ingredients": [
    { "item": "minecraft:charcoal" },
    { "type": "neoforge:single", "fluid": "resourceful_refinement:catalysed_iron", "amount": 100 },
    { "type": "neoforge:single", "fluid": "resourceful_refinement:catalysed_zinc", "amount": 100 }
  ],
  "results": [
    { "id": "resourceful_refinement:durasteel_alloy", "amount": 125 }
  ]
}
```

> [!note] Implementation
> The original design sample used a `count` field on items and a `"fluid"` key on the result. The
> shipped serializer (`FluidRefineryRecipe.Serializer` +
> `FluidRefineryRecipe.FluidRefineryProcessingRecipeParams`) instead reads `ingredients` as an
> `Either<SizedFluidIngredient, Ingredient>` list and `results` as an
> `Either<FluidStack, ProcessingOutput>` list, so a **fluid** result is written with the
> `FluidStack` codec (`"id"` + `"amount"`). Vanilla item ingredients get an implicit count of 1;
> use the optional `sized_ingredients` field for counted item ingredients. `heat_requirement`
> defaults to `NONE` and `processing_time` to `0` if omitted. Validation limits: max 2 item
> ingredients, max 2 fluid ingredients, exactly 1 fluid result (`getMaxInputCount` = 2,
> `getMaxFluidInputCount` = 2, `getMaxFluidOutputCount` = 1, `getMaxOutputCount` = 0).

Shipped recipes live under `data/resourceful_refinement/recipe/fluid_refinery/` — catalysed metals,
purified metals, alloys, paints, coolants and carborax refining. See [[Fluid Processing Recipes]].

## Rendering

To support variable height without one giant model per height, the refinery uses **segmented
rendering** driven by a `BlockEntityRenderer` on the controller (Access Port):

- **Base** — the bottom slice with the output port and blaze-burner heaters.
- **Middle (repeatable)** — a 1-block-tall slice that tiles vertically; its top/bottom texture
  edges are seamless so stacked slices don't seam.
- **Top (cap)** — the control head that caps the structure at whatever height was detected.

The BER reads the recorded height and renders `base_model` at `y = 0`, `middle_model` at
`y = 1 … height − 2`, and `top_model` at `y = height − 1`, plus animated fluid surfaces, heat
flames, the spinning central rotor and the filter slot.

> [!note] Implementation
> The controller BER is `content/refinery/rendering/FluidRefineryRenderer` (a Create
> `SafeBlockEntityRenderer`), with model parts `RefineryBaseModel`, `RefineryMiddleModel`,
> `RefineryTopModel` and `RefineryBlenderModel` (registered via `RefineryLayers`). It draws the
> fluid columns (`FluidBoxRendering`), the rotor (Create's `getAngleForBe`), heat flames (vanilla
> fire / soul fire plus a mod `passive_flames` sprite) and the `FilteringRenderer` slot. Proxy
> blocks are rendered by `RefineryProxyRenderer` / `RefineryKineticProxyRenderer`; both blocks are
> `RenderShape.INVISIBLE` with a full-cube collision/selection shape.

## Implementation

| Concern | Class / ID |
|---|---|
| Controller block | `content/refinery/RefineryAccessPortBlock` → `refinery_access_port` |
| Controller BE | `content/refinery/RefineryAccessPortBlockEntity` (`SmartBlockEntity`, `IHaveGoggleInformation`) |
| Proxy block / BE | `content/refinery/RefineryProxyBlock` / `RefineryProxyBlockEntity` → `refinery_proxy` |
| Kinetic proxy block / BE | `content/refinery/RefineryKineticProxyBlock` / `RefineryKineticProxyBlockEntity` → `refinery_kinetic_proxy` |
| Structure logic | `content/refinery/RefineryStructureHelper` (`tryAssemble`, `disassemble`, `toWorldPos`) |
| Recipe | `content/refinery/recipe/FluidRefineryRecipe`, `FluidRefineryRecipeInput` |
| JEI category | `content/refinery/recipe/FluidRefineryRecipeCategory` |
| Rendering | `content/refinery/rendering/FluidRefineryRenderer` and `Refinery*Model` / `RefineryLayers` |
| Central rotor | `content/refinery/BlenderBladeBlock` / `BlenderBladeBlockEntity` — see [[Blender Blade]] |

- **Block state properties** — Access Port has `FACING` (horizontal) and `ASSEMBLED` (boolean); it
  faces away from the player on placement.
- **Registries** — blocks in `ModBlocks`, block entities in `ModBlockEntities`, block items in
  `ModItems` (the proxy has *no* obtainable item — it is placed only during assembly). Recipe type
  `FLUID_REFINERY_TYPE` / serializer `FLUID_REFINERY_SERIALIZER` and `FLUID_REFINERY_TYPE_INFO`
  (`IRecipeTypeInfo`) in `ModRecipeTypes`.
- **Capabilities** — `getFluidHandlerForProxy` / `getItemHandlerForProxy` on the controller resolve
  sided handlers from a proxy's `(dx, dy, dz, side)`; capabilities are re-registered per proxy
  during assembly via `level.invalidateCapabilities`.
- **Stress** — `REFINERY_STRESS = 12` su, registered in `ModStressValues` against
  `REFINERY_KINETIC_PROXY` (the central column is the kinetic sink; the Access Port itself carries
  no stress impact).
- **Heat** — uses the mod's five-state [[ExtendedHeatCondition]] (chilled / cooled / passive /
  heated / superheated), combining fuel and [[Radiator]] heat. Exact-match gating.
- **Persistence** — assembly flag, height, heat/fuel/false-render state, crafting progress, proxy
  position ↔ original-state map and all tanks/handlers are saved in NBT and synced to the client via
  `getUpdateTag` / block updates.

## Related

- [[Blender Blade]]
- [[Detailed Fluid Refinery Implementation Plan]]
- [[Fluid Processing Recipes]]
- [[Radiator]]
- [[ExtendedHeatCondition]]
- [[Codebase Overview]]
