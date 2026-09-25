---
title: Radiator
category: Machine
status: Implemented
introduced: v0.3
recipe_type: n/a
related:
  - "[[ExtendedHeatCondition]]"
  - "[[Distillery]]"
  - "[[Combustion Chamber]]"
tags:
  - machine
  - heat
  - kinetic
  - fluid
---

Radiators are pipe-like blocks which heat or cool their surroundings. Depending on the fluid passing through them, they act as a heat (or cold) source for other Create and Resourceful Refinement machines, standing in for a Blaze Burner or providing the Cooled/Chilled conditions that fuelled machines need. They are the mod's main way of turning a flowing fluid into an [[ExtendedHeatCondition]] state that neighbouring blocks can read.

**ID:** `radiator_pipe`

## Gameplay Role

The radiator converts a supply of conducting fluid into an ambient heat state. It sits between the fluid-refinement loop and the heat-hungry machines: pump the right fluid through it and it becomes a reusable heat source. Cooled and Chilled radiators are used to cool [[Combustion Chamber]]s or [[Distillery]] towers; Heated radiators can be used in place of Blaze Burners for any heating purpose.

Because effective fluids are consumed as they flow through (see Operation), a closed loop of radiators carrying an effective fluid slowly drains its fluid content, so the loop must be topped up over time rather than being free forever.

## Construction & Placement

A single fully-directional block. It behaves like Create's pumps in that it has a front and a back connecting fluid port aligned to its `FACING` axis, but it is bi-directional — it does not pull or push fluids or reset the pipe distance the way a pump does. It behaves like a standard Create pipe, except that it does **not** connect to other pipes on its (local) side faces; flow is only along the facing axis.

On placement the radiator orients to the player's looking direction (or its opposite when not sneaking), waterlogs if placed in water, and forces Create to rebuild the surrounding pipe network graph so pressure re-propagates correctly.

## Inputs & Outputs

- **Fluid faces:** the two faces on the `FACING` axis. Fluid may enter the input face (opposite the facing) and leave the output face (the facing direction). Side faces are inert.
- **Internal tank:** a 50 mB `FluidTank` (`TANK_CAPACITY = 50`), with a minimum fill threshold of 25 mB (`MIN_TANK_FILL_THRESHOLD`) held back so the radiator never fully empties while conducting.
- **Active pushing:** when holding more than the minimum threshold, the radiator actively pushes fluid into a directly-connected tank/machine on its output face (so it works even without a pipe between them). Flow is rate-limited tick-by-tick to what it received the previous tick.
- **Food cooking:** if the radiator is Heated (or hotter), up to four smokable food items may be right-clicked onto its top face; they cook via vanilla `SMOKING` recipes at 8× the recipe cooking time and pop out when done, campfire-style.

## Operation

Heat is driven entirely by the fluid in the tank:

- Most fluids do not change a radiator's heat. Fluids that do are matched against the coolant fluid tags in [[ExtendedHeatCondition]]'s `HeatUtilities` and are **partially consumed** at a configurable rate as they flow through.
- Consumption rate is per-condition and config-driven (see Implementation → config keys). A rate below 1 is treated as a per-tick percentage chance to consume 1 mB.
- Internally the block entity tracks a `heatTemperature` energy value (roughly -1000 … 1000). Each tick it moves toward the target energy of the current fluid's condition (`HEAT_GROWTH = 5` per step) while an effective fluid is present and affordable, and decays back toward zero (`HEAT_DECAY = 8`, ~1-in-4 ticks) when it is not.

Worked examples of the fluid → heat mapping:

- Water pumped through a radiator → **Cooled**.
- Coolant pumped through → **Chilled**.
- Overcharged Carborax pumped through → **Heated**.

> [!note] Implementation
> The design intent describes three heat states (Cooled, Chilled, Heated), but the block actually supports the full six-tier [[ExtendedHeatCondition]] range. The `HEAT_STATE` blockstate is an `IntegerProperty` 0–5 (blaze heat level + `HEAT_STATE_OFFSET` of 3), covering chilled / cooled / none / passive / heated / superheated, and the block also carries Create's `HEAT_LEVEL` (NONE / SMOULDERING / KINDLED / SEETHING) so it reads as a Blaze Burner to Create basins and boilers. Default `HEAT_STATE` is 2 (none/inert).

**Chilled side-effects:** a Chilled radiator occasionally freezes adjacent fluids — a water source becomes ice, flowing water becomes powder snow, and a lava source becomes deepslate (a low random chance per tick).

**Goggle info:** wearing Create's goggles shows the radiator's current heat condition (name + colour), its raw heat energy in degrees, and whether flow is "Operational" or "Stagnant".

## Rendering

The block uses standard Java JSON block models, one per heat state driven by the `HEAT_STATE` property (the original design listed four: `radiator_inert`, `radiator_chilled`, `radiator_cooled`, `radiator_heated`). A `RadiatorRenderer` block-entity renderer additionally draws up to four food items lying flat on the top face, one per quadrant, when items are cooking. A separate entity-style `RadiatorModel` is baked from `RADIATOR_MODEL_LAYER` and reused by the JEI category to render the block at each heat colour.

## Recipes

The radiator has no datapack recipe type of its own. Its fluid → heat behaviour is surfaced in JEI through a **virtual** category so players can discover which fluids produce which heat state and at what consumption rate:

- `RadiatorVirtualHeatingCategory` — JEI recipe category, type id `resourceful_refinement:radiator_heat_conversion`, titled "Radiator Heating". It renders the fluid (cycling all members of the matched tag), the consumption rate as mB/s, and the resulting heat condition, alongside the radiator model tinted to that heat.
- `RadiatorVirtualHeatingRecord` — the backing record `(TagKey<Fluid> fluidTag, ExtendedHeatCondition resultingHeat, Double consumptionRate)`. Five are registered, one per coolant tag (chilled / cooled / passive / heated / superheated), each reading its consumption rate from `ServerConfig`.

## Implementation

- **Package:** `content/radiator/`
- **Block:** `RadiatorBlock` — `WrenchableDirectionalBlock`, `EntityBlock`, `SimpleWaterloggedBlock`; state `FACING`, `HEAT_STATE` (0–5), `WATERLOGGED`, and Create's `BlazeBurnerBlock.HEAT_LEVEL`.
- **Block entity:** `RadiatorBlockEntity` — `SmartBlockEntity`, `IHaveGoggleInformation`, and Create's `BoilerHeater` (its `getHeat` maps the condition to boiler heat levels: chilled -3, cooled -2, passive `PASSIVE_HEAT`/0, heated 1, superheated 2, else `NO_HEAT`/-1). Adds an inner `RadiatorFluidTransportBehaviour` (Create `FluidTransportBehaviour`) for pipe pressure and a `RadiatorProxyFluidHandler` exposing per-face fill (input face) / drain (output face).
- **Renderer / model:** `RadiatorRenderer` (BER, cooking items), `RadiatorModel` (baked layer, shared with JEI).
- **JEI:** `RadiatorVirtualHeatingCategory` + `RadiatorVirtualHeatingRecord` (registered as a catalyst on the radiator item in `ModJeiPlugin`).
- **Registry IDs:** block `ModBlocks.RADIATOR_PIPE` (`radiator_pipe`), item `ModItems.RADIATOR_PIPE_ITEM`, block entity `ModBlockEntities.RADIATOR_PIPE_BE`.
- **Capability:** `Capabilities.FluidHandler.BLOCK` registered per-side via `RadiatorBlockEntity.getFluidHandler(direction)` (valid only on the facing axis).
- **Config keys** (`config/ServerConfig`, "Radiator Parameters" section, each 0–1000, defaults shown; values below 1 are a per-tick chance):
  - `chilled_coolant_consumption` = 0.25
  - `cooled_coolant_consumption` = 1.0
  - `passive_coolant_consumption` = 0.35
  - `heated_coolant_consumption` = 0.4
  - `superheated_coolant_consumption` = 0.8
- **Constants:** `TANK_CAPACITY = 50`, `MIN_TANK_FILL_THRESHOLD = 25`, `HEAT_GROWTH = 5`, `HEAT_DECAY = 8`, `HEAT_STATE_OFFSET = 3`.

## Related

- [[ExtendedHeatCondition]] — the six-tier heat model the radiator reads and writes.
- [[Distillery]] — uses a Chilled radiator (among other sources) as its heat source.
- [[Combustion Chamber]] — cooled by adjacent Cooled/Chilled radiators.
