---
title: Combustion Chamber
category: Machine
status: Implemented
introduced: v0.3
recipe_type: n/a
related:
  - "[[Fuel Tank]]"
  - "[[Radiator]]"
  - "[[ExtendedHeatCondition]]"
  - "[[Primary Design Doc]]"
tags:
  - machine
  - kinetic
  - heat
  - fluid
  - multiblock
---

The Combustion Chamber is an industrial engine that burns carborax fuels into kinetic rotation. It has a fluid intake on one face and a shaft output on the opposing face. Placed in a line, chambers share a single fluid intake and shaft output but combine their fuel consumption and power output, forming a longer, stronger engine bank. Higher-grade fuels demand cooling — supplied by an adjacent [[Radiator]] at the right [[ExtendedHeatCondition]] — or the engine underperforms or overheats.

## Gameplay Role

The Combustion Chamber is the mod's fluid-powered generator, the counterpart to Create's water wheels and steam engines but fed from the carborax fuel chain (see [[Fuel Tank]]). Fuel grade trades raw stress and speed against a cooling requirement, so building a strong engine bank means also building the cooling to sustain it. This ties the power system back into the radiator/heat loop.

## Construction & Placement

- Each chamber is a `KineticBlock` (`FACING`, horizontal). Its shaft points along the facing direction; the fluid intake is the opposing (back) face.
- Chambers placed in a straight line along their shared facing axis link into a **chain** up to 8 long (`MAX_CHAIN_LENGTH = 8`); a longer row is split into consecutive chains. Connectivity is recomputed on placement, removal and neighbour changes.
- The **controller** is the first block of the chain; the **output engine** is the last block (`chainIndex == chainSize - 1`) and is the only one that actually exposes a shaft and generates rotation. Every member stores `controllerPos`, `chainIndex` and `chainSize`.
- The whole bank draws fuel through one logical intake (the controller's back face) and delivers all its power through the single output shaft.

## Inputs & Outputs

- **Fluid intake:** exposed only on the controller's back face (`FACING.getOpposite()`), via a `ChainInputFluidHandler` that spreads incoming fuel across the chain members' individual 1000 mB tanks (`TANK_CAPACITY = 1000`; reported capacity is `1000 × chainSize`). Fuel piped or pushed in — e.g. from an adjacent [[Fuel Tank]] — is distributed to the emptiest members first.
- **Shaft output:** the output engine's front face. It is a Create generating source; rotation direction is selectable via a scroll value box on the output engine (windmill-style clockwise / counter-clockwise).
- **Redstone:** a redstone signal on any chamber deactivates the whole chain (zero speed, zero stress, no fuel burn).

## Operation

Fuel is graded into three burn tiers by fluid tag, each with a per-mB burn time:

| Fuel (design name) | Fluid tag | Fuel state | Burn time / mB | Base speed |
|---|---|---|---|---|
| Unrefined carborax | `passive_fuel` | 1 | 6 ticks (`PASSIVE_FUEL_BURN_TIME`) | 16 RPM |
| Catalysed carborax | `heated_fuel` | 2 | 9 ticks (`HEATED_FUEL_BURN_TIME`) | 32 RPM |
| Overcharged carborax | `superheated_fuel` | 3 | 12 ticks (`SUPERHEATED_FUEL_BURN_TIME`) | 48 RPM |

**Cooling requirement** (design intent, driven by the coldest adjacent [[ExtendedHeatCondition]] source around any chain member):

- **Unrefined** — requires no cooling; produces very little stress at low speed but burns a lot of fuel.
- **Catalysed** — full stress and medium speed when **Cooled**; running passively (no cooling) drops it to half speed and flags "underperforming".
- **Overcharged** — full stress at high speed when **Chilled**; a third / half when only **Cooled**; it will **not** run passively — with no cooling it overheats and stops (speed 0). The chain reads the *coldest* adjacent source, so a single well-placed radiator can serve the bank.

A chain's fuel state is the highest state among its members, so mixing fuels runs the whole bank at the best grade present (and its cooling demand). The output engine's generated speed and added stress capacity are derived from the chain's fuel state and cooling, then multiplied by an optional intake-fan bonus.

**Intake fan:** an Encased Fan placed directly in front of the output engine, facing the same way, is detected as an "intake fan" and multiplies both generated speed and stress capacity by `INTAKE_FAN_SPEED_MULTIPLIER = 1.25`.

**Stress:** each member contributes `(fuelState² × 512) / maxSpeed` stress capacity (×1.25 with an intake fan); the base per-block generation capacity registered with Create is 10 (`ModStressValues`). The goggle tooltip reports linked engine count, current fuel and burn rate (mB/s), generated su at RPM, and warnings to supply a Cooled or Chilled radiator when underperforming or overheated.

## Rendering

The block itself is `RenderShape.INVISIBLE`; it is drawn by `CombustionChamberRenderer` (BER) from a baked `CombustionChamberModel` (`LAYER_LOCATION`), with `CombustionChamberItemRenderer` for the item form.

**Fan model swap:** when an Encased Fan is acting as a combustion-chamber intake fan, its model is swapped for `CombustionChamberFanModel`. This is wired through `CombustionChamberFanIntegration` plus an `EncasedFanBlockMixin` that injects into Create's `EncasedFanBlock.hasShaftTowards` so the fan accepts drive from the chamber's output shaft; `ModelSwapper` replaces the fan's blockstate models at model-bake time.

## Implementation

- **Package:** `content/combustion_chamber/`
- **Block:** `CombustionChamberBlock` — `KineticBlock`, `IBE<CombustionChamberBlockEntity>`; state `FACING`.
- **Block entity:** `CombustionChamberBlockEntity` — `GeneratingKineticBlockEntity`, `IHaveGoggleInformation`, `IRotate`. Holds a per-chamber `inputTank`, chain fields, burn timer/state, heat and redstone state, and an inner `ChainInputFluidHandler` (per-side fluid handler exposed on the back face only).
- **Item / renderer / model:** `CombustionChamberItem`, `CombustionChamberItemRenderer`, `CombustionChamberRenderer`, `CombustionChamberModel`, `CombustionChamberFanModel`.
- **Fan integration:** `CombustionChamberFanIntegration` + `mixin/EncasedFanBlockMixin` (swaps Create's fan model / shaft behaviour when driven by a chamber).
- **Registry IDs:** block `ModBlocks.COMBUSTION_CHAMBER` (`combustion_chamber`), item `ModItems.COMBUSTION_CHAMBER_ITEM`, block entity `ModBlockEntities.COMBUSTION_CHAMBER_BE`.
- **Capability:** `FluidHandler.BLOCK` via `getFluidHandler(side)` (controller only, back face).
- **Stress:** `ModStressValues.registerCapacity(COMBUSTION_CHAMBER, 10)`.
- **Fuel tags:** `passive_fuel`, `heated_fuel`, `superheated_fuel` (fluid tags).
- **Constants:** `TANK_CAPACITY = 1000`, `MAX_CHAIN_LENGTH = 8`, `INTAKE_FAN_SPEED_MULTIPLIER = 1.25`, burn times 6 / 9 / 12, max speeds 16 / 32 / 48 RPM.

## Related

- [[Fuel Tank]] — stores and feeds carborax fuels to chambers.
- [[Radiator]] — supplies the Cooled / Chilled cooling that higher fuel grades need.
- [[ExtendedHeatCondition]] — the heat scale the cooling requirement is measured on.
- [[Primary Design Doc]] — v0.3 Combustion Chamber design intent.
