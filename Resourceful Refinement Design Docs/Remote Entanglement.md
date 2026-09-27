---
title: Remote Entanglement
category: Machine
status: Implemented
introduced: v0.4
recipe_type: n/a
related:
  - "[[GLARE Networks]]"
  - "[[Fluid Properties]]"
tags:
  - machine
  - glare
  - network
  - multiblock
  - heat
---

Remote Entanglement refers to a behaviour system implemented by two [[GLARE Networks|GLARE]]-powered blocks: the Remote Entangler Depot and the Remote Entanglement Transporter. Both BlockEntities share similar logic and functions, but act on different subjects — the Depot moves item stacks, the Transporter moves players.

## Gameplay Role

Both devices use an operational GLARE network to teleport their subject between matching, addressed endpoints — the Depot moves item stacks, the Transporter moves players. They share the same charge-up/cooldown loop and the same requirements (Lux allocation plus a *chilled* heat condition), and are the GLARE network's primary transport payoff.

## Construction & Placement

### Remote Entanglement Depot

The Remote Entangler Depot is a variant of Create's Depot block, functioning like one, just as the [[Casting Depot]] does. However, it can also teleport item stacks between linked depots on the same GLARE network. It is a horizontally directional block.

> [!note] Implementation
> `RemoteEntanglerDepotBlock extends com.simibubi.create.content.logistics.depot.DepotBlock`; the BE extends Create's `DepotBlockEntity` (single held item via `DepotBehaviour`). Blockstate properties `FACING` (horizontal) and `RECEIVING` (boolean, default false = Send). Toggled Send/Receive with Create's wrench. Registered `remote_entangler_depot` (block/item/BE). `getMaxGlareLinks() = 1`.

### Remote Entanglement Transporter

The Remote Entanglement Transporter is a multiblock structure that self-places when its 'controller' block is placed (like a bed or door). The structure consists of the horizontally-directional 'Transporter Controller' block, a solid 'Tank Proxy' block that sits on top of the controller, and two non-solid 'Casing Proxy' blocks occupying the spaces in front of, and in front of and above, the controller. This full assembly occupies a 1×2×2 volume. If any block in the assembly is broken, all blocks are removed and the controller block is dropped. If there is not enough space for the full assembly when the controller is placed, only the controller block is placed, the transporter is considered 'incomplete', and the player receives a warning on their action bar.

> [!note] Implementation
> On `tryAssemble()` the controller places: a tank proxy (`remote_entanglement_transporter_tank`) at `worldPosition.above()`, and two casing proxies (`remote_entanglement_transporter_casing`) at `chamberPos()` (= `worldPosition.relative(FACING)`) and `chamberPos().above()` — controller + tank + 2 casings = 4 blocks. Assembly only proceeds if all three targets `canBeReplaced()`; otherwise the action-bar message `remote_transporter.incomplete` fires ("clear the block above and the two blocks in front"). Proxies are `RenderShape.INVISIBLE` with solid collision, store the controller pos, and verify ownership before removal. Both proxy blocks share BE `remote_entanglement_transporter_proxy`. Controller registered `remote_entanglement_transporter`.

## Inputs & Outputs

### Remote Entanglement Depot

Entangler Depots have 3 Trio Address slots on their front face, used to set their Address ID. Depots are in either a 'Send' or 'Receive' blockstate — by default, depots are 'Send'.

To both charge up and cool down, the Entangler Depot must be receiving 4 Lux from an operating GLARE network. It must also have a *chilled* heat source adjacent to any of its faces (`HeatUtilities.GetExtendedHeatLevel()`). See [[ExtendedHeatCondition]] for the heat states.

> [!note] Implementation
> `RemoteEntanglerDepotBlockEntity.REQUIRED_LUX = 4`. `RemoteEntanglementUtil.isChilled` returns true when any neighbour of any assembly part has extended heat level ≤ −3 (i.e. *chilled*). Address slots are 3 `TelemetryAddressBehaviour` via `Trio.makeSlots` (`TrioAddressSlot(index, 8f, 15.9f, 0f)`); the address is `GlareAddress.of(slot0, slot1, slot2)` and must be complete to send. The state property is named `receiving` (not `send`).

### Remote Entanglement Transporter

The Address slots for a transporter are shown on the controller's front face (3 slots).

To function, the Transporter requires 8 Lux from an operational GLARE network, and either its controller or tank block must be *chilled* from an adjacent source. Transporters also require *liquid_chorus* in their input tank upon charge-up completion in order to teleport a player. This fluid is consumed on successful teleportation. The assembly has fluid interfaces on the back face of the controller and the top face of the tank proxy.

> [!note] Implementation
> `RemoteEntanglementTransporterBlockEntity.REQUIRED_LUX = 8`. `isChilled` checks neighbours of both controller and tank positions. The tank only accepts `ModFluids.LIQUID_CHORUS.source`; `TANK_CAPACITY = 1000` mB.
>
> **Liquid Chorus cost discrepancy.** The design intent is **150 mB** per teleport, but the code drains **`CHORUS_COST = 250` mB** on success. Additionally, the in-code fuel-shortage failure string is hardcoded as `"Not enough Liquid Chorus (150 mB required)"` (`RemoteEntanglementTransporterBlockEntity.java`, line 126) while the guard actually checks against 250 mB — so the message text is inconsistent with the real cost. This is a minor bug worth reconciling (either lower `CHORUS_COST` to 150 to match intent, or update the string to 250). See [[Fluid Properties]] for Liquid Chorus.

## Operation

### Remote Entanglement Depot

If a Send-Depot is holding a non-empty item stack, it charges up for a short 2s period. Upon charging, it attempts to send the stack to a random (chunk-loaded) Receive-Depot with the same address on this network that is not currently holding any items. If successful, the items are teleported to their destination. If no matching empty depot can be found (or the stack was removed from the depot), the teleport fails and a failure message is cached on the depot's goggle tooltip until the next attempt.

Once a teleport attempt has been made, the depot enters an 8s cooldown during which it cannot begin any teleport, before returning to idle (or immediately beginning a new cycle if holding a stack).

If either the Lux or chilled condition is not met, the current charge/cooldown state pauses until satisfied again.

> [!note] Implementation
> `CHARGE_TICKS = 40` (2s), `COOLDOWN_TICKS = 160` (8s). `attemptTransfer` iterates `RemoteEntanglementService.findCandidates` (a shuffled, persisted-network query filtered to `DEPOT` kind, matching complete address, and `canReceive()`) and skips wrong-dimension, unloaded, non-depot, non-receiving, or occupied destinations; the first loaded/empty/receiving depot gets `setHeldItem(held.copy())` and the source is cleared only after acceptance (atomic). Failure strings: "The item stack was removed before entanglement completed" and "No loaded, empty receiving depot matches this address". `isOperational` requires the node ONLINE, on a network, not overloaded, and `luxAllocated <= luxCapacity`.

### Remote Entanglement Transporter

The Transporter works much like the Depot, except it teleports players.

While its conditions are met, the transporter charges up (2s) and cools down (10s).

When a player enters the hitbox region defined by the casing proxies, the transporter begins its charge-up sequence. If the player is still present upon charge-up, there is sufficient liquid_chorus, and a valid random destination exists on the network, the player is teleported. Otherwise they remain, and no liquid_chorus is consumed. The transporter then enters cooldown. If multiple players are present upon charge-up, one is randomly selected to teleport.

Destinations need not be chilled or fuelled themselves; they only need to be connected to the operational GLARE network — and they don't even need to be loaded. Transporters store their connected addresses and locations as part of the server-side network information when their chunks unload, so they remain valid destinations.

When a player is teleported, they suffer a status effect (with no particles) called "teleportation_sickness" for 5s. Players with teleportation_sickness cannot trigger a transporter's charge-up sequence or be teleported.

Assembled transporters can be cycled between 'Auto', 'Send-Only', and 'Receive-Only' modes with a wrench. Send-Only transporters are not valid destinations; Receive-Only transporters cannot initiate teleport sequences. These states are encoded on the server-side GLARE data for unloaded transporters too.

> [!note] Implementation
> `CHARGE_TICKS = 40` (2s), `COOLDOWN_TICKS = 200` (10s). `findEligiblePlayer` picks a random alive `ServerPlayer` inside `chamberBounds()` that does not already have the sickness effect. `attemptTeleport` loads the destination chunk, revalidates the destination controller (assembled, `canReceive`, valid assembly), checks `noCollision` at `arrivalPosition()`, then `teleportTo`. Cost is drained only on success. Modes are the `TransporterMode` blockstate `{ AUTO("auto"), SEND_ONLY, RECEIVE_ONLY }`, cycled with a wrench (message `remote_transporter.mode`), mapping to `RemoteEntanglementMode.TRANSPORTER_AUTO/_SEND_ONLY/_RECEIVE_ONLY` (`canSend`/`canReceive` gate behaviour). Discovery via the shuffled persisted-network `findCandidates`; the destination remains valid while unloaded because the query reads persisted network data.
>
> The effect is registered as **`teleportation_sickness`** (`ModEffects.TELEPORT_SICKNESS`, HARMFUL, `SICKNESS_TICKS = 100` = 5s, amplifier 0, no ambient/particles/icon). Note the Java constant is `TELEPORT_SICKNESS` while the registered id is `teleportation_sickness`.

## Rendering

None of the proxy blocks render visible models — the controller renders a BlockEntityRenderer for the whole assembly.

> [!note] Implementation
> `RemoteEntanglementTransporterRenderer` is currently an explicit placeholder (copper block for the tank, acacia trapdoor + air for the chamber) with `getViewDistance() = 256`. `RemoteEntanglerDepotRenderer` reuses Create's `DepotRenderer.renderItemsOf`.

## Implementation

**Package:** `content/glare/remote/`.

**Key classes:** `RemoteEntanglerDepotBlock`/`BlockEntity`/`Renderer`, `RemoteEntanglementTransporterBlock`/`BlockEntity`/`Renderer`, `RemoteTransporterProxyBlock`/`BlockEntity`, `RemoteEntanglementService` (persisted-network candidate discovery), `RemoteEntanglementUtil` (chilled check), `IRemoteEntanglementEndpoint`, and enums `RemoteEndpointKind {NONE, DEPOT, TRANSPORTER}`, `RemoteEntanglementMode {DEPOT_SEND, DEPOT_RECEIVE, TRANSPORTER_AUTO, TRANSPORTER_SEND_ONLY, TRANSPORTER_RECEIVE_ONLY}`, `TransporterMode {AUTO, SEND_ONLY, RECEIVE_ONLY}`, `RemoteMachineState {IDLE, CHARGING, COOLDOWN}`.

**Registry IDs:** `remote_entangler_depot`, `remote_entanglement_transporter`, `remote_entanglement_transporter_tank`, `remote_entanglement_transporter_casing` (blocks/items); BEs share `remote_entanglement_transporter_proxy` for the two proxy blocks. Effect `teleportation_sickness`. Fluid `liquid_chorus`.

**Key constants:** Depot `REQUIRED_LUX = 4`, `CHARGE_TICKS = 40`, `COOLDOWN_TICKS = 160`. Transporter `REQUIRED_LUX = 8`, `CHARGE_TICKS = 40`, `COOLDOWN_TICKS = 200`, `CHORUS_COST = 250` (intent 150 — see callout above), `TANK_CAPACITY = 1000`, `SICKNESS_TICKS = 100`.

## Related

- [[GLARE Networks]]
- [[Fluid Properties]]
