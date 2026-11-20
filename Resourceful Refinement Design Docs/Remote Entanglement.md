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
> `RemoteEntanglerDepotBlock extends com.simibubi.create.content.logistics.depot.DepotBlock`; the BE extends Create's `DepotBlockEntity` (single held item via `DepotBehaviour`). Blockstate properties `FACING` (horizontal) and `RECEIVING` (boolean, default false = Send). Toggled Send/Receive with Create's wrench. Blockstate also has `OFFLINE` (boolean, default true), which selects the `remote_entangler_depot_offline` model. Registered `remote_entangler_depot` (block/item/BE). The depot is an ordinary linkable GLARE node with `getMaxGlareLinks() = 1`.

### Remote Entanglement Transporter

The Remote Entanglement Transporter is a multiblock structure that self-places when its 'controller' block is placed (like a bed or door). The structure consists of the horizontally-directional 'Transporter Controller' block, a solid 'Tank Proxy' block that sits on top of the controller, and two non-solid 'Casing Proxy' blocks occupying the spaces in front of, and in front of and above, the controller. This full assembly occupies a 1×2×2 volume. If any block in the assembly is broken, all blocks are removed and the controller block is dropped. If there is not enough space for the full assembly when the controller is placed, only the controller block is placed, the transporter is considered 'incomplete', and the player receives a warning on their action bar.

> [!note] Implementation
> On `tryAssemble()` the controller places: a tank proxy (`remote_entanglement_transporter_tank`) at `worldPosition.above()`, and two casing proxies (`remote_entanglement_transporter_casing`) at `chamberPos()` (= `worldPosition.relative(FACING)`) and `chamberPos().above()` — controller + tank + 2 casings = 4 blocks. Assembly only proceeds if all three targets `canBeReplaced()`; otherwise the action-bar message `remote_transporter.incomplete` fires ("clear the block above and the two blocks in front"). Proxies are `RenderShape.INVISIBLE` with solid collision, store the controller pos, and verify ownership before removal. Both proxy blocks share BE `remote_entanglement_transporter_proxy`. Controller registered `remote_entanglement_transporter`.
>
> The controller blockstate has `FACING`, `MODE` and `ASSEMBLED` (boolean, set false on placement). `setAssembled()` keeps the blockstate in sync with the BE's `assembled` field.

### GLARE Connection (Lux Socket)

The transporter controller **cannot be linked directly** with a GLARE linking tool. It joins a network the same way the Telemetry Terminal and Drill Pylon do: a Lux Transceiver placed on the assembly's **Lux Socket**, which is the **top face of the tank proxy**. The socket only exists while the assembly is complete.

> [!note] Implementation
> `allowsManualGlareLinks() = false`; `getGlareLinkEndpoint()` is the centre of the tank block. `RemoteTransporterProxyBlockEntity implements LuxSocket` and delegates `getLuxSocketNode` to `controller.isLuxSocket(proxy, side)`, which is true only for `Direction.UP` on `tankPos()` while assembled. Assembly changes call `refreshAdjacentLuxTransceiver()`, which nudges the transceiver at `tankPos().above()` (`LuxTransceiverBlockEntity.refreshSocketLink()`). `getAllocatedLux()` is 8 while assembled and 0 otherwise.

## Inputs & Outputs

### Remote Entanglement Depot

Entangler Depots have 3 Trio Address slots on their front face, near the top edge, used to set their Address ID. Depots are in either a 'Send' or 'Receive' blockstate — by default, depots are 'Send'.

To both charge up and cool down, the Entangler Depot must be receiving 4 Lux from an operating GLARE network. It must also have a *chilled* heat source adjacent to any of its faces (`HeatUtilities.GetExtendedHeatLevel()`). See [[ExtendedHeatCondition]] for the heat states.

> [!note] Implementation
> `RemoteEntanglerDepotBlockEntity.REQUIRED_LUX = 4`. `RemoteEntanglementUtil.isChilled` returns true when any neighbour of any assembly part has extended heat level ≤ −3 (i.e. *chilled*). Address slots are 3 `TelemetryAddressBehaviour` via `Trio.makeSlots` (`TrioAddressSlot(index, 10f, 15.9f, 0f)`); the address is `GlareAddress.of(slot0, slot1, slot2)` and must be complete to send. The state property is named `receiving` (not `send`).

### Remote Entanglement Transporter

The transporter's 3 Address slots are on the controller's front face near its top edge, on the socket texture. The slots face into the chamber.

To function, the Transporter requires 8 Lux from an operational GLARE network, and either its controller or tank block must be *chilled* from an adjacent source. Transporters also require *liquid_chorus* in their input tank upon charge-up completion in order to teleport a player. This fluid is consumed on successful teleportation. The only fluid interface is the **back face of the controller**. The tank proxy has no fluid capability, so its top face is reserved for the Lux Socket.

> [!note] Implementation
> `RemoteEntanglementTransporterBlockEntity.REQUIRED_LUX = 8`. Address slots: `TrioAddressSlot(index, 15f, 16.05f, 0f)`. The slots sit just proud of the face; at 15.9 they were hidden inside it. The `FluidHandler.BLOCK` capability is only registered on the controller BE and only answers for `FACING.getOpposite()`. `isChilled` checks neighbours of both controller and tank positions. The tank only accepts `ModFluids.LIQUID_CHORUS.source`; `TANK_CAPACITY = 1000` mB.
>
> **Liquid Chorus cost.** The design intent was **150 mB** per teleport, but the code drains **`CHORUS_COST = 250` mB**. The failure message is now built from the constant (`"Not enough Liquid Chorus (" + CHORUS_COST + "mb required)"`), so it always matches. Decide whether 150 or 250 is the intended cost. See [[Fluid Properties]] for Liquid Chorus.

## Operation

### Remote Entanglement Depot

If a Send-Depot is holding a non-empty item stack, it charges up for a short 2s period. Upon charging, it attempts to send the stack to a random (chunk-loaded) Receive-Depot with the same address on this network that is not currently holding any items. If successful, the items are teleported to their destination. If no matching empty depot can be found (or the stack was removed from the depot), the teleport fails and a failure message is cached on the depot's goggle tooltip until the next attempt.

Once a teleport attempt has been made, the depot enters an 8s cooldown during which it cannot begin any teleport, before returning to idle (or immediately beginning a new cycle if holding a stack).

If either the Lux or chilled condition is not met, the current charge/cooldown state pauses until satisfied again.

A depot is **offline** when its address is incomplete, it isn't chilled, or its network isn't operational (no network, overloaded, or not enough Lux). An offline depot switches to its offline model.

**Feedback:**
- While a Send-Depot charges, a thin ring of lightning hovers over its top plate and drifts up and down as if scanning the item.
- A failed send puts out a small puff of smoke.

> [!note] Implementation
> `CHARGE_TICKS = 40` (2s), `COOLDOWN_TICKS = 160` (8s). `attemptTransfer` iterates `RemoteEntanglementService.findCandidates` (a shuffled, persisted-network query filtered to `DEPOT` kind, matching complete address, and `canReceive()`) and skips wrong-dimension, unloaded, non-depot, non-receiving, or occupied destinations; the first loaded/empty/receiving depot gets `setHeldItem(held.copy())` and the source is cleared only after acceptance (atomic). Failure strings: "The item stack was removed before entanglement completed" and "No loaded, empty receiving depot matches this address" (the second also calls `playSendFailure`, 6 `SMOKE` particles). Each tick, `updateOfflineState(!canRun || !address.isComplete())` writes `OFFLINE` with `UPDATE_CLIENTS` when it changes. `RemoteEntanglerDepotClientFx.setScanning` is driven from the synced `machineState` in `read(clientPacket)` and stopped in `invalidate()`. It draws one ring of 6 unbranched `ClientLightning` arcs at radius 0.36, centred 14.5/16 up and scanning ±1.5/16 over 40 ticks. `isOperational` requires the node ONLINE, on a network, not overloaded, and `luxAllocated <= luxCapacity`.

### Remote Entanglement Transporter

The Transporter works much like the Depot, except it teleports players.

A transporter is **offline** when any send requirement fails: it isn't fuelled (less than one teleport's worth of Liquid Chorus), it isn't chilled, or its network isn't operational. Charging can **only start, and only continue,** while the transporter is online in a send-capable mode (Auto or Send-Only). If any requirement drops mid-charge, the charge resets to idle rather than stalling. Cooldown (10s) pauses while unpowered or unchilled, like the Depot.

When an eligible player is in the chamber (the space covered by the casing proxies) and the transporter is online, it begins its 2s charge-up. If the player is still present upon charge-up, there is sufficient liquid_chorus, and a valid random destination exists on the network, the player is teleported. Otherwise they remain, and no liquid_chorus is consumed. The transporter then enters cooldown. If multiple players are present upon charge-up, one is randomly selected to teleport.

Destinations need not be chilled or fuelled themselves; they only need to be connected to the operational GLARE network — and they don't even need to be loaded. Transporters store their connected addresses and locations as part of the server-side network information when their chunks unload, so they remain valid destinations.

When a player is teleported, they suffer a status effect (with no particles) called "teleportation_sickness" for 5s. Players with teleportation_sickness cannot trigger a transporter's charge-up sequence or be teleported.

Assembled transporters can be cycled between 'Auto', 'Send-Only', and 'Receive-Only' modes with a Create wrench, used on the controller **or any of its proxy blocks**. Send-Only transporters are not valid destinations; Receive-Only transporters cannot initiate teleport sequences. These states are encoded on the server-side GLARE data for unloaded transporters too.

> [!note] Implementation
> `CHARGE_TICKS = 40` (2s), `COOLDOWN_TICKS = 200` (10s). The `offline` field is set each tick to `!poweredAndChilled || tank < CHORUS_COST`; it is forced true when unassembled and synced to clients as `"Offline"`. The tick guard `if (!mode.canSend() || offline) { resetCharge(); return; }` fixed a bug where a player entering an unfuelled or unchilled transporter left it stuck in CHARGING. `findEligiblePlayer` picks a random alive `ServerPlayer` inside `chamberBounds()` that does not already have the sickness effect. `attemptTeleport` loads the destination chunk, revalidates the destination controller (assembled, `canReceive`, valid assembly), checks `noCollision` at `arrivalPosition()`, then `teleportTo`. Cost is drained only on success. Modes are the `TransporterMode` blockstate `{ AUTO("auto"), SEND_ONLY, RECEIVE_ONLY }`, cycled through the shared static `RemoteEntanglementTransporterBlock.cycleMode(level, pos, state, player)` (message `remote_transporter.mode`). `RemoteTransporterProxyBlock.useItemOn` resolves the controller and calls it when holding a Create wrench, mapping to `RemoteEntanglementMode.TRANSPORTER_AUTO/_SEND_ONLY/_RECEIVE_ONLY` (`canSend`/`canReceive` gate behaviour). Discovery via the shuffled persisted-network `findCandidates`; the destination remains valid while unloaded because the query reads persisted network data.
>
> The effect is registered as **`teleportation_sickness`** (`ModEffects.TELEPORT_SICKNESS`, HARMFUL, `SICKNESS_TICKS = 100` = 5s, amplifier 0, no ambient/particles/icon). Note the Java constant is `TELEPORT_SICKNESS` while the registered id is `teleportation_sickness`.

## Rendering & Feedback

### Remote Entanglement Depot

Baked block models: `remote_entangler_depot` while online and `remote_entangler_depot_offline` while offline, picked by the blockstate. `RemoteEntanglerDepotRenderer` reuses Create's `DepotRenderer.renderItemsOf` for the held stack. The charging ring and failure smoke are described under Operation.

### Remote Entanglement Transporter

The proxy blocks are invisible (`RenderShape.INVISIBLE`). The controller's blockstate picks the model:
- **Unassembled:** `remote_entanglement_transporter_base` (controller only). The item model also uses this.
- **Assembled:** `remote_entanglement_transporter`, the full 1×2×2 structure with tank, pad and chamber walls.

Both use the `remote_entanglement_transporter` texture, with `durasteel_particle` for particles.

The controller's block entity renderer adds two dynamic parts.

**Glow overlay** (`remote_entanglement_glow`, drawn full-bright on the chamber pad and the controller's front face):

| State | Colour |
|---|---|
| Offline (unfuelled, unchilled, or no healthy network) | Dark grey, steady |
| Auto | Pink, pulsing |
| Send-Only | Green, pulsing |
| Receive-Only | Red, pulsing — **always**, even when the send requirements aren't met, because a receiver never sends |

**Tank fluid:** a cuboid inside the tank casing whose height follows the Liquid Chorus fill level.

**FX:**
- **Charging:** three thin lightning rings scan up and down the chamber, out of phase with each other.
- **Successful teleport:** a vertical ELECTRIC lightning bolt strikes through the chamber at both the source and the destination.
- **Failed teleport:** smoke and large smoke in the chamber.

**Goggles:** the tooltip has a "Remote Entanglement Transporter" header (no generic GLARE node info) and shows mode, state, Liquid Chorus level and the last failure. Every proxy block forwards the controller's tooltip, so goggles work on any part of the assembly.

> [!note] Implementation
> Blockstate `remote_entanglement_transporter.json` has 8 variants (facing × assembled). The glow is a separate `PartialModel` (`ModPartialModels.REMOTE_TRANSPORTER_GLOW` → `models/block/remote_entanglement_transporter_glow.json`, `tintindex 0`, `shade false`), registered in `registerAdditionalModels`. The renderer draws it every frame with `CachedBuffers.partial(...).light(FULL_BRIGHT).color(argb)` because baked tints can't animate. The pose is rotated `180 − facing.toYRot()` about the block centre. Colours are `OFFLINE 0x3A3A3A`, `AUTO 0xF07CFF`, `SEND 0x5CFF7A` and `RECEIVE 0xFF4A4A`; the pulse runs over 40 ticks with a minimum brightness of 0.55. The fluid box is `[2.5, 16.5, 6.5]`→`[13.5, 16.5 + 12·fill, 13.5]`/16 (model space), drawn `RenderType.translucent()` with the fluid's still sprite and tint. `shouldRenderOffScreen = true`, `getViewDistance() = 256`.
>
> `RemoteEntanglementTransporterClientFx`: `setScanning` is driven from the synced CHARGING state in `read(clientPacket)` and stopped in `invalidate()`. It draws 3 rings × 8 unbranched arcs at radius 0.42, scanning y 2/16→29/16 over 40 ticks. `playTeleportStrike` (ELECTRIC, 31/16→1.1/16, 10 ticks) fires from `RemoteEntanglementTransporterBlock.triggerEvent` (`EVENT_TELEPORTED = 1`, returns true). The server sends the event via `server.blockEvent` on both endpoints. Failure smoke is `playTeleportFailure`: 12 `SMOKE` + 4 `LARGE_SMOKE`. `RemoteTransporterProxyBlockEntity implements IHaveGoggleInformation` and forwards to the controller.

## Implementation

**Package:** `content/glare/remote/`.

**Key classes:** `RemoteEntanglerDepotBlock`/`BlockEntity`/`Renderer`/`ClientFx`, `RemoteEntanglementTransporterBlock`/`BlockEntity`/`Renderer`/`ClientFx`, `RemoteTransporterProxyBlock`/`BlockEntity`, `RemoteEntanglementService` (persisted-network candidate discovery), `RemoteEntanglementUtil` (chilled check), `IRemoteEntanglementEndpoint`, and enums `RemoteEndpointKind {NONE, DEPOT, TRANSPORTER}`, `RemoteEntanglementMode {DEPOT_SEND, DEPOT_RECEIVE, TRANSPORTER_AUTO, TRANSPORTER_SEND_ONLY, TRANSPORTER_RECEIVE_ONLY}`, `TransporterMode {AUTO, SEND_ONLY, RECEIVE_ONLY}`, `RemoteMachineState {IDLE, CHARGING, COOLDOWN}`.

**Registry IDs:** `remote_entangler_depot`, `remote_entanglement_transporter`, `remote_entanglement_transporter_tank`, `remote_entanglement_transporter_casing` (blocks/items); BEs share `remote_entanglement_transporter_proxy` for the two proxy blocks. Effect `teleportation_sickness`. Fluid `liquid_chorus`.

**Key constants:** Depot `REQUIRED_LUX = 4`, `CHARGE_TICKS = 40`, `COOLDOWN_TICKS = 160`. Transporter `REQUIRED_LUX = 8`, `CHARGE_TICKS = 40`, `COOLDOWN_TICKS = 200`, `CHORUS_COST = 250` (intent was 150; see the callout above), `TANK_CAPACITY = 1000`, `SICKNESS_TICKS = 100`.

## Related

- [[GLARE Networks]]
- [[Fluid Properties]]
