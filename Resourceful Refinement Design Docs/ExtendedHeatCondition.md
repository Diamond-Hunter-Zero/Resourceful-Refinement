---
title: ExtendedHeatCondition
category: Framework
status: Implemented
introduced: v0.3
recipe_type: n/a
related:
  - "[[Radiator]]"
  - "[[Distillery]]"
  - "[[Combustion Chamber]]"
tags:
  - framework
  - heat
---

`ExtendedHeatCondition` is the mod's replacement for Create's three-state heat model. Create's `HeatCondition` only distinguishes `none`, `heated` and `superheated`; Resourceful Refinement's fluid/heat loop also needs *cold* states and an explicit state for Create's passive tier, so `ExtendedHeatCondition` extends the concept to six tiers spanning chilled through superheated. Machines such as the [[Radiator]], [[Distillery]] and [[Combustion Chamber]] all describe and compare their heat requirements in terms of this enum.

## Concepts

Heat is modelled as a single ordered scale rather than a boolean "hot / not hot". Each tier carries:

- a **colour** for tooltips and GUI text,
- a **blaze heat level** integer that lines up with Create's `BoilerHeater` interface values (so the mod can interoperate with Create basins and boilers), and
- **max / target heat-energy** thresholds used by machines (notably the radiator) that ramp a continuous internal energy value toward a tier.

The six tiers add `chilled`, `cooled` and `passive` on top of Create's `none`, `heated` and `superheated`. `passive` is equivalent to the passive heating value used for boiler data.

| ID | Colour | Blaze heat level (BoilerHeater equivalent) | Max heat energy | Target heat energy |
|---|---|---|---|---|
| `chilled` | `0xa9e8f5` | -3 (conversion treats `<= -3`) | -500 | -750 |
| `cooled` | `0x2758c9` | -2 | -50 | -350 |
| `none` | `0xFFFFFF` (16777215) | -1 | 50 | 0 |
| `passive` | `0x86C43B` | 0 | 333 | 250 |
| `heated` | `0xE88000` | 1 | 666 | 500 |
| `superheated` | `0x5C93E8` | 2 (conversion treats `>= 2`) | 1000 | 750 |

> [!note] Implementation
> An earlier version of this doc listed slightly different colour hex values (e.g. `chilled = 0xBFF2F5`, `cooled = 0x1A83C9`) and did not record the max/target heat-energy fields. The table above reflects the current `ExtendedHeatCondition` enum in code, which is authoritative. Each constant is `NAME(name, colour, maxHeatEnergy, targetHeatEnergy, blazeHeatLevel)`.

## Key Types

- **`ExtendedHeatCondition`** (enum, `StringRepresentable`) — the six tiers above. Exposes `getColor()`, `getMaxHeatEnergy()`, `getTargetHeatEnergy()`, `getBlazeHeatLevel()`, and `getTranslationKey()` (`recipe.heat_requirement.<name>`). Serialises via `CODEC` (`StringRepresentable.fromEnum`) and `STREAM_CODEC` (`CatnipStreamCodecBuilders.ofEnum`), so it can be embedded directly in recipe JSON and network packets.
- **`HeatUtilities`** — the conversion and world-scanning service. Responsibilities:
  - **Heat tags** it defines: block tags `cooled_source`, `chilled_source`, `supports_heater_stand`; fluid tags `chilled_coolants`, `cooled_coolants`, `passive_coolants`, `heated_coolants`, `superheated_coolants`.
  - **Conversions:** `ConvertHeatLevelToExtendedCondition(int)`, `ConvertHeatLevelToCondition(int)` (back to Create's clamped `HeatCondition`), `GetHeatTitle`, `GetHeatColour`, `GetGaugeRotation` (maps a heat level to a gauge needle angle for rendering), and `GetCoolantConditionFromFluid(FluidStack)` (fluid-tag → condition).
  - **World scanning:** `GetExtendedHeatLevel(level, pos)` reads the mod's cold block tags first, then falls back to Create's `BoilerHeater.findHeat` for heated/superheated sources; `HasAdjacentHeatSource`, `GetColdestAdjacentHeatSource`, `GetHottestAdjacentHeatSource` scan the six neighbours.

## How Features Use It

- **[[Radiator]]** — ramps an internal heat energy toward a tier's target and exposes the result as both `HEAT_STATE` (0–5) and Create's `HEAT_LEVEL`; its `BoilerHeater.getHeat` returns the tier's blaze heat level. It also registers the coolant fluid tags' consumption behaviour.
- **[[Distillery]]** — recipes carry a required `ExtendedHeatCondition`; the tower reads the block beneath it via `HeatUtilities.GetExtendedHeatLevel` and matches it against the requirement.
- **[[Combustion Chamber]]** — reads the coldest adjacent heat source (`GetColdestAdjacentHeatSource`) to decide whether a fuel tier is adequately cooled, gating its speed and stress output.

## Implementation

- **Package:** `utilities/heating/`
- **Classes:** `ExtendedHeatCondition` (enum + codecs), `HeatUtilities` (tags, conversions, adjacency scanning, gauge rotation, Create `HeatCondition` conversion).
- **Extension points:** new heat-consuming machines should compare against `ExtendedHeatCondition` values and source their world heat via `HeatUtilities.GetExtendedHeatLevel` / the adjacency helpers, and register any new heat-source blocks into the `cooled_source` / `chilled_source` block tags (or hot sources via Create's `BoilerHeater`).

## Related

- [[Radiator]] — primary producer of heat/cold states.
- [[Distillery]] — consumes a required heat condition.
- [[Combustion Chamber]] — consumes cooling to sustain fuel tiers.
