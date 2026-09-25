---
title: Fluid Refill Station
category: Machine
status: Implemented
introduced: v0.2
recipe_type: n/a
related:
  - "[[Hosegun]]"
  - "[[Gel Tracking]]"
tags:
  - machine
  - fluid
  - gel
  - redstone
---

The Fluid Refill Station is a horizontally directional block which lets the user quickly refill fluid containers — such as [[Hosegun|Hoseguns]], buckets, or glass bottles — from an internal tank. It also acts as a node in the [[Gel Tracking]] network, letting players track the number of gel splatters created by linked Hoseguns.

**ID:** `fluid_refill_station`

## Gameplay Role

The refill station is the refuelling point for fluid-consuming tools, most notably the [[Hosegun]]. Beyond simple refilling, a labelled station forms part of a world-level gel-tracking network that can drive minigames: multiple stations can subscribe to the same Tracking ID and count how many gel splatter blocks a team or player has created.

## Construction & Placement

A single block with **solid faces**. It is horizontally directional: on placement, `FACING` is set to the opposite of the player's horizontal look direction (the same convention as the Casting Depot / [[Forge Mould]]), so the front face points back at the player. It renders via a BlockEntityRenderer (`RenderShape.INVISIBLE`), but uses a full-cube collision/support shape so levers, redstone dust, and similar can attach to any face.

## Inputs & Outputs

- **Internal tank:** 1000 mB `FluidTank` (`TANK_CAPACITY = 1000`).
- **Pipe interfacing:** accepted on every face **except** the front (`FACING`, model-local north) face. The pipe-face test is `FluidRefillStationBlock.isPipeFace`, which excludes the front face (and, per the design, the front-facing orientation is not a pipe face).
- **Container refill (output):** right-click with a fluid container drains the station into the item. Discrete containers (buckets, bottles) require the minimum fill amount; dynamic containers (the [[Hosegun]]) take whatever is available. As with standard fluid behaviour, the container must be empty or already contain the same fluid.
- The station **cannot** be refilled by right-clicking it with a filled or partially-filled container (drain-to-item only, never fill-from-item).

## Operation

- **Refilling:** if the station contains fluid, right-clicking with a valid container fills the held item (server-side, with a fill sound on success).
- **Tracking ID:** right-clicking with an empty hand opens a GUI with a text input to set a 'Tracking ID' label. By default a station has no ID. IDs are sanitised on save (trimmed, max 32 characters, control characters stripped).
- **Hosegun binding:** crouch-right-click a labelled station with a [[Hosegun]] to bind that Hosegun to the station's Tracking ID (or unbind it if the Hosegun already carries the exact matching ID). Sneak + Hosegun on a station with no ID gives a hint and does not refill.
- **Gel tracking:** when gel blobs from a bound Hosegun create or update gel splatter blocks, those blocks increment a global count for that Tracking ID; when such a block is removed, the count decrements. This count is world-level data (see [[Gel Tracking]]), not stored on individual stations, so multiple stations can share an ID and all serve as refill points for the same team.
- **Redstone reset:** stations accept redstone input. On a rising edge, if the station has a Tracking ID, all gel splatter blocks associated with that ID are destroyed (returning the global count to 0), performed as a throttled, server-performant purge. The `POWERED` blockstate tracks the redstone state and a `CONDUIT_DEACTIVATE` sound plays on purge.
- **Display Link:** stations can be read by Create Display Links as an information source. With a Tracking ID, the link shows `"{Tracking ID}: {number of associated gel blocks}"`; without one, it shows `"{current tank fluid amount}mb {fluid name}"` (empty tank → `0mb`).

## Rendering

Refill Stations use a `FluidRefillStationRenderer` (BER) for their model. Inside the outer casing model, an interior box is rendered with the animated still texture of the fluid the tank contains; the box height scales dynamically from 0% to 100% according to the tank fill amount. A goggle tooltip (`IHaveGoggleInformation`) surfaces the tracking label and tank contents.

## Implementation

- Package: `content/refill_station/`.
- **Block:** `FluidRefillStationBlock` — `HorizontalDirectionalBlock` with `FACING` and `POWERED` properties; `isPipeFace` for pipe-face gating; `useWithoutItem` opens the menu; `useItemOn` handles refill and hosegun bind; `neighborChanged` drives the redstone purge.
- **Block entity:** `FluidRefillStationBlockEntity` — 1000 mB `FluidTank`, `trackingId` field, NBT + block-update sync, `MenuProvider`, goggle tooltip; calls into `GelTrackingService` on load/remove/ID-change and schedules gel purge on redstone rising edge.
- **GUI:** `FluidRefillStationMenu`, `FluidRefillStationScreen`, `RefillStationGuiTextures`, `RefillStationHoverButton`, `TrackingIdPickerList`.
- **Display source:** `FluidRefillStationDisplaySource` (registered via `ModDisplaySources`).
- **Interactions:** `FluidRefillStationInteractions` (container fill logic, hosegun binding).
- **Rendering:** `FluidRefillStationRenderer`, `FluidRefillStationCasingModel`, `FluidRefillStationLayers`, `FluidRefillStationItemRenderer`, plus `FluidRefillStationItem`.
- **Networking:** `network/SetRefillStationTrackingIdPayload` — client→server payload carrying `(BlockPos, String)`; the handler validates the position is loaded and within usable distance before applying the sanitised ID.
- **Registration:** `registry/ModBlocks`, `ModBlockEntities`, `ModItems`; BER + layers in `ResourcefulRefinementMain.ClientModEvents`.

> [!note] Implementation
> Redstone gel reset and the Create Display Link integration are implemented in code (see `docs/FLUID_REFILL_STATION_IMPLEMENTATION.md`, Phases 6–7 marked done), though the impl plan's own status table lists remaining manual in-game tests and Phase 8 polish (final casing model/texture, crafting recipe, Ponder scene). Placeholder casing art may still be in use.

## Related

- [[Hosegun]]
- [[Gel Tracking]]
