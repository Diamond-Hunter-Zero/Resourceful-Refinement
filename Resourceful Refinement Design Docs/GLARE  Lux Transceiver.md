---
title: GLARE Lux Transceiver
category: Machine
status: Implemented
introduced: v0.4
recipe_type: n/a
related:
  - "[[GLARE Networks]]"
  - "[[Cyclotron Forge]]"
  - "[[Drill Pylon]]"
  - "[[GLARE Chromatic Transceiver]]"
tags:
  - machine
  - glare
  - network
---

The Lux Transceiver is a directional GLARE network block which connects to a [[GLARE Networks|GLARE network]] as a node, and pipes network Lux into any block exposing a Lux-Socket interface on its back face. It is **not** the [[GLARE Chromatic Transceiver]] — that is a separate block for colour-charge redstone logic.

**ID:** *lux_transceiver*

## Gameplay Role

The Lux Transceiver is the bridge between a [[GLARE Networks|GLARE network]] and any machine that wants to consume Lux without being a network node itself. Rather than making every Lux-hungry machine a targetable GLARE node, the transceiver docks against a machine's Lux-Socket face and pipes network Lux into it, keeping the connection logic in one reusable block. This is how the [[Cyclotron Forge]] and [[Drill Pylon]] controllers join a network as non-targetable receivers.

## Construction & Placement

The Lux Transceiver is a directional block that can face any of the 6 directions. Like other GLARE nodes it can be connected to a network and supports a single link.

It is placed so that its local back face abuts a BlockEntity exposing a Lux-Socket. When placed against a socket while connected to a network, it automatically forms its special socket link (see Operation).

> [!note] Implementation
> `LuxTransceiverBlock extends DirectionalBlock` with `FACING = DirectionalBlock.FACING` (all 6 directions, default NORTH). `getStateForPlacement` sets `FACING` to `context.getNearestLookingDirection().getOpposite()`. Block/BE/item all registered `lux_transceiver`. `MAX_LINK_COUNT = 1`.

## Inputs & Outputs

- **GLARE link:** a single standard network link, established like any other GLARE node.
- **Back face (Lux-Socket bind):** the transceiver's local back face binds to a `LuxSocket` interface exposed on an adjacent BlockEntity's face. BlockEntities can expose a `LuxSocket` on their faces, much like Item and Fluid Handlers. The socket points to a GLARE node implementation on the BlockEntity (or its controller).

The Lux Transceiver does not consume any Lux itself.

> [!note] Implementation
> `LuxTransceiverBlockEntity.getAllocatedLux()` returns `0` — it is a pass-through. `LuxSocket` declares `@Nullable IGlareNode getLuxSocketNode(Direction side)` and `default boolean isLuxSocketEnabled(Direction side)`. `findSocketNode` looks at `worldPosition.relative(FACING.getOpposite())`; if that BE is a `LuxSocket` and `isLuxSocketEnabled(facing)` is true, the transceiver binds to the node it returns (the controller BE). The actual Lux draw belongs to that controller, which is the real `IGlareReceiver`.

## Operation

Like other GLARE nodes, it connects to a network and supports a single link.

When a Lux Transceiver is placed such that its local back face connects to a socket, and the transceiver is connected to a network, the transceiver forms a link with the BlockEntity's GLARE node. This link is special: it does not care about LoS (and will not participate in such checks), will not render any beams, and does not contribute to any link limits or counts on the transceiver or the BlockEntity. The socket interface (and the BlockEntity/controller) is not a GLARE node itself, and cannot be targeted by other GLARE nodes or by the connection tools.

When either the transceiver or the BlockEntity is moved/removed/rotated, the link may break as per normal. If the socket interface is ever disabled or removed, the link also breaks as expected.

If a BlockEntity (or controller) has multiple sockets connected to functioning transceivers, the transceivers' networks merge as they would with any other relay chain. Attaching multiple transceivers to a BlockEntity does not multiply the amount of Lux consumed, as only the BlockEntity is actually being allocated Lux. It merely allows more connected networks to be built.

> [!note] Implementation
> The link is created as a `SOCKET`-kind link via `GlareService.trySocketLink` → `GlareSavedData.tryAddSocketLink` (`LinkKind.SOCKET` = non-counting, no-LoS, non-rendered; see [[GLARE Networks#SOCKET Links]]). `refreshSocketLink()` (server-only) is called on load, place, and neighbour change; it removes a stale socket link before forming the new one and stores the bound node as `socketNodePos`. `onRemove` calls `clearSocketLink()` then `GlareService.onNodeRemoved`. Consumers found in code: `CyclotronProxyBlockEntity` / `CyclotronControllerBlockEntity` ([[Cyclotron Forge]]) and `DrillPylonProxyBlockEntity` (role `LUX_SOCKET`) / `DrillPylonHeadBlockEntity` ([[Drill Pylon]]) — each proxy implements `LuxSocket` and returns its controller as the node, gated by an `isLuxSocketSide` check. Right-clicking the transceiver with an empty hand opens the [[GLARE Networks#GLARE Power Terminal|GLARE Power Terminal]].

## Rendering

The Lux Transceiver uses a BlockEntityRenderer for its visuals.

> [!note] Implementation
> `LuxTransceiverRenderer implements BlockEntityRenderer` but is currently a **no-op** (empty `render`), consistent with SOCKET links being non-rendered — there is no beam/link visual for the socket binding. The block's own model provides its appearance.

## Related

- [[GLARE Networks]]
- [[Cyclotron Forge]]
- [[Drill Pylon]]
- [[GLARE Chromatic Transceiver]]
