---
title: Fuel Tank
category: Machine
status: Implemented
introduced: v0.3
recipe_type: n/a
related:
  - "[[Combustion Chamber]]"
tags:
  - machine
  - fluid
  - storage
---

The Fuel Tank is a placeable fluid tank dedicated to storing carborax fuels for [[Combustion Chamber]]s and other fuel consumers. Unlike Create's fluid tanks, it retains its stored fluid when broken and picked up — the contents are written to the item so a filled tank can be relocated without spilling — and it exposes a fluid handler for piping fuel in and out.

## Gameplay Role

The Fuel Tank is the buffer and transport for the fuel side of the power loop: fill it from the [[Distillery]]/refinement chain, carry it to where the engines are, and let it feed adjacent [[Combustion Chamber]]s automatically. Being able to move a full tank without losing its contents makes fuel logistics far less fiddly than with vanilla or Create tanks.

## Construction & Placement

- A single non-directional block occupying a 14×16×14 core (`Block.box(1,0,1,15,16,15)`), with a full-block collision/support shape so blocks and pipes attach cleanly.
- It tracks four "port" blockstate booleans — `FRONT_PORT`, `EAST_PORT`, `SOUTH_PORT`, `WEST_PORT` — set automatically when a neighbour on that side exposes a Create pipe interface or a fluid-handler capability, so the model can show a connection nub where fluid can flow. Ports refresh on placement and neighbour changes.

## Inputs & Outputs

- **Internal tank:** a 4000 mB `FluidTank` (`TANK_CAPACITY = 4000`).
- **Fluid capability:** the tank is exposed as `FluidHandler.BLOCK` on **every** side, so fuel can be piped in or out from any face.
- **Auto-feed:** each server tick, while non-empty, the tank pushes fuel into any directly-adjacent Combustion Chamber that will accept it, up to `MAX_PUSH_PER_TICK = 50` mB per neighbour per tick.
- **Manual fill:** right-clicking the tank with a single fluid-carrying item (bucket, another item fluid-handler) drains that container into the tank and returns the empty container (unless in creative), with a bucket-empty sound.

## State & Data

The tank's contents survive being broken or picked up:

- On break (`getDrops`) and on pick-block (`getCloneItemStack`), a non-empty tank writes its fluid onto the dropped/created item as the `fuel_tank_fluid` data component (`SimpleFluidContent`), and also saves the block-entity NBT to the item.
- When the item is placed again (`updateCustomBlockEntityTag`), the stored `fuel_tank_fluid` content is loaded back into the new block entity's tank.
- The item itself carries a `FluidHandler.ITEM` capability (`FuelTankItemFluidHandler`, a `FluidHandlerItemStack` backed by the same data component), so a filled tank item reads and behaves as a fluid container in inventories and other machines.
- The item tooltip shows the stored fluid and fill level (reading either the `fuel_tank_fluid` component or, as a fallback, the saved `Tank` NBT).

## Behaviour

The Fuel Tank is passive storage with a short-range auto-feed: it does not process or convert fluid, only holds it and hands it to adjacent consumers. Its value over a generic tank is the persistent contents and the item-level fluid handler, which together let it act as a portable, refillable fuel canister as well as a placed buffer.

## Implementation

- **Package:** `content/fuel_tank/`
- **Block:** `FuelTankBlock` — `Block`, `EntityBlock`; state `FRONT_PORT` / `EAST_PORT` / `SOUTH_PORT` / `WEST_PORT`. Handles manual fill, port connection updates, and contents-preserving drops / pick-block.
- **Block entity:** `FuelTankBlockEntity` — `BlockEntity`, `IHaveGoggleInformation`; holds the `tank`, server-tick auto-push to adjacent Combustion Chambers, and client sync.
- **Item:** `FuelTankBlockItem` (`BlockItem`) with inner `FuelTankItemFluidHandler` (`FluidHandlerItemStack`); reads/writes the `fuel_tank_fluid` data component and renders the fill tooltip.
- **Renderer:** `FuelTankRenderer` (BER) draws the fluid level inside the tank.
- **Data component:** `ModDataComponents.FUEL_TANK_FLUID` — id `resourceful_refinement:fuel_tank_fluid`, a persistent, network-synchronised `SimpleFluidContent`.
- **Registry IDs:** block `ModBlocks.FUEL_TANK` (`fuel_tank`), item `ModItems.FUEL_TANK_ITEM`, block entity `ModBlockEntities.FUEL_TANK_BE`.
- **Capabilities:** `FluidHandler.BLOCK` on all sides (the tank), and `FluidHandler.ITEM` on the item (registered in `ResourcefulRefinementMain.registerCapabilities`).
- **Constants:** `TANK_CAPACITY = 4000`, `MAX_PUSH_PER_TICK = 50`.

## Related

- [[Combustion Chamber]] — the primary consumer the Fuel Tank auto-feeds.
