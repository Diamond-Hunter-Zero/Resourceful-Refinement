---
title: Paint Nozzle
category: Machine
status: Implemented
introduced: v0.2
recipe_type: n/a
related:
  - "[[Hosegun]]"
  - "[[Gel Splatter]]"
  - "[[Paint Fluids]]"
tags:
  - gel
  - fluid
  - kinetic
---

The Paint Nozzle is a directional, wall/pipe-mounted block that connects to Create pipe networks and turns piped fluid directly into gel-blobs — an automated, stationary counterpart to the [[Hosegun]]. While open it drains its internal tank to fire a continuous stream of gel-blobs in the direction it faces.

**ID:** `paint_nozzle`

## Gameplay Role

The Paint Nozzle lets players wire the gel system into Create fluid logistics: pump a fluid to a nozzle, open its valve, and it sprays [[Gel Splatter]] blobs (or paint/cleanse/potion effects) hands-free. It is the fixed-emplacement version of the Hosegun.

## Construction & Placement

A single directional block that can be placed facing any of the 6 directions (it faces away from the placing player's look direction). It has a per-facing collision shape and auto-connects to pipes on its back (model-local south) face.

- **Pipe face** = the opposite of `FACING` (the back of the nozzle body); this is the only face treated as a pipe interface.
- **Spray direction** = `FACING`; gel-blobs are emitted from a point just in front of this face.

## Inputs & Outputs

- **Internal tank:** 500 mb, holding a single fluid (`TANK_CAPACITY = 500`). Fluid flows in through the pipe face.
- **Output:** gel-blob projectile entities fired from the spray face while the valve is open.

## Operation

The nozzle has two states, driven by the `valve_open` blockstate property:

- **Closed:** fluid may flow into the internal tank, but nothing else happens.
- **Open:** the nozzle drains its tank to fire gel-blobs every 2 ticks (the same cadence as a held-down Hosegun), consuming the gel type's ammo cost per blob. Firing pauses if the tank drops below the cost.

**Toggling the valve:** right-click with an empty hand to toggle open/closed. This flips `valve_open`, plays a trapdoor sound, and resets the spray timer. The block model updates from the blockstate.

**Flow speed:** a configurable multiplier applied to the speed of the gel-blobs the nozzle produces, cycled by right-clicking with a Create Wrench. Default is Medium.

| Speed | Velocity factor |
|---|---|
| Low | 0.2 |
| Medium | 0.7 |
| High | 1.15 |

Blob launch velocity is `1.6 × 1.15 (global gel-blob factor) × flow-speed factor`.

> [!note] Implementation
> The velocity factors in `PaintNozzleFlowSpeed` are `LOW 0.2`, `MEDIUM 0.7`, `HIGH 1.15`. Older design text listed Low 0.45 / Medium 0.75 / High 1.2; the intent (three ascending speed tiers, Medium default) is unchanged but the current numbers differ.

A Create goggle overlay (`IHaveGoggleInformation`) shows the valve state, current flow speed, and tank contents.

## Recipes

The nozzle produces gel-blobs; it has no processing recipe. Gel-blob impact behaviour is fluid-/gel-type-driven — see [[Gel Splatter]].

## Rendering

Standard block model driven by the `valve_open` blockstate property, so toggling the valve swaps the visible model. Per-facing `VoxelShape`s give it a correct mounted silhouette on each of the 6 faces.

## Implementation

- **Block:** `content/paint_nozzle/PaintNozzleBlock` (extends `DirectionalBlock`; `FACING` + `valve_open` properties; wrench cycles speed, empty-hand toggles valve; server ticker fires blobs).
- **Block entity:** `PaintNozzleBlockEntity` (500 mb `FluidTank`, spray timer, flow speed; syncs on contents/valve/speed change; goggle tooltip). Fires `GelBlobEntity` via `HosegunItem.GEL_BLOB_VELOCITY_FACTOR`.
- **Flow speed:** `PaintNozzleFlowSpeed` enum (`LOW`/`MEDIUM`/`HIGH`, `MEDIUM` default, `next()` cycling, ordinal persistence).
- Shares the `GelBlobEntity` projectile and gel-type system with the [[Hosegun]].

## Related

- [[Hosegun]]
- [[Gel Splatter]]
- [[Paint Fluids]]
