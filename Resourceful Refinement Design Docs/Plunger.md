---
title: Plunger
category: Tool
status: Implemented
introduced: v0.2
recipe_type: n/a
related:
  - "[[Hosegun]]"
tags:
  - tool
  - fluid
---

The Plunger is a throwable weapon that inherits from the vanilla trident: it can be held and thrown at entities or blocks from range, and picked back up. It deals only light damage. Its distinguishing feature is fluid draining — right-clicking a fluid-containing block empties every tank on that block.

**ID:** `plunger`

## Acquisition

The Plunger is a crafted tool item. It stacks to 1 and has 512 durability.

## Usage

As a weapon the Plunger behaves like a trident: hold to charge, release to throw, and it can be picked up again after landing. It uses its own set of sound effects rather than the vanilla trident's. It carries an attack-speed modifier of `-2.9` and has an enchantment value of 0.

The drain functionality is triggered by holding the Plunger in the **main hand** and right-clicking on any block that exposes fluid tanks, emptying all of that block's fluid tanks. This works on Create content and on blocks added by this mod. A throw requires a minimum charge of 10 ticks before it releases; charge power scales up to the trident's shoot power over roughly 20 ticks.

> [!note] Implementation
> The Plunger deals `PlungerItem.THROW_DAMAGE = 1.0F` (0.5 hearts) for both melee and thrown hits — `ThrownPlunger.onHitEntity` forces the base damage to `1.0F`, overriding the trident's hardcoded 8.0. Older design text said "3 points of damage"; the implemented value is 1.

## State & Data

- **Charging flag:** the `plunger_charging` data component (`Boolean`) is set true while the Plunger is being drawn and cleared on release. `inventoryTick` includes a fail-safe that clears the flag if the stack is no longer actively in use.
- Thrown state (coating, durability, etc.) is preserved through the pickup stack copied into the `ThrownPlunger` entity.

## Behaviour

Draining collects every distinct `IFluidHandler` reachable at the clicked block — the clicked face, a null-side query, and all six directions — then drains each tank fully. Several mod multiblocks are handled explicitly so their controller tanks drain correctly regardless of which part was clicked:

- **[[Fluid Refinery]]** access port / proxies: drains `inputTankA`, `inputTankB` and `outputTank` on the controller.
- **[[Combustion Chamber]]**: drains the input tank of every chain member.
- **[[Distillery]]**: drains the controller's input and output tanks.

On a successful drain the Plunger plays its empty-tank sound, awards the use stat, and swings.

When thrown, `ThrownPlunger` replicates standard trident impact physics and audio (using the mod's custom hit sounds), applies enchantment damage modifiers, and skips knockback effects against endermen. It exposes `isStuckInGround()` for the renderer.

## Rendering

The Plunger uses a Java item model via a custom BEWLR (`PlungerItemRenderer` + `PlungerModel`) for both in-hand and GUI rendering. The thrown entity is drawn by `ThrownPlungerRenderer`.

## Implementation

- **Item:** `content/plunger/PlungerItem` (extends `TridentItem`; `plunger_charging` component, durability 512).
- **Thrown entity:** `ThrownPlunger` (`ModEntities.THROWN_PLUNGER`, id `thrown_plunger`; extends `ThrownTrident`) + `ThrownPlungerRenderer`.
- **Fluid draining:** `PlungerFluidInteractions` (`hasDrainableFluid`, `tryEmptyBlockTanks`, plus per-multiblock controller handlers).
- **Client:** `PlungerClientEvents`, `PlungerItemRenderer`, `PlungerModel`.
- **Sounds:** `PlungerSounds` (`THROW`, `HIT_GROUND`, `EMPTY_TANK`).

## Related

- [[Hosegun]]
