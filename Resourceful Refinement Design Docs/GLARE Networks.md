---
title: GLARE Networks
category: Framework
status: Implemented
introduced: v0.4
recipe_type: n/a
related:
  - "[[GLARE  Lux Transceiver]]"
  - "[[GLARE Emitter Dish]]"
  - "[[GLARE Relay]]"
  - "[[GLARE Kinetic Receiver]]"
  - "[[GLARE Chromatic Transceiver]]"
  - "[[Resonance Crystal]]"
  - "[[Relay Wrench]]"
  - "[[Telemetry Terminal GUI]]"
  - "[[Remote Entanglement]]"
  - "[[Cyclotron Forge]]"
  - "[[Drill Pylon]]"
  - "[[Harmonic Cyclotron]]"
  - "[[Primary Design Doc]]"
tags:
  - framework
  - glare
  - network
  - multiblock
---

GLARE Networks are a graph network formed by in-world blocks, connected to each other by line-of-sight laser beams, and synchronously handled by a global data structure. A GLARE network provides and allocates power called 'Lux Capacity', stores and transfers messages in an e-mail-like system, and enables teleportation between locations.

## Concepts

### GLARE Network Rules

A GLARE network consists of a graph of links between BlockEntities. A network encodes no sense of directionality; only membership to the network. At a high level, a network exists as a structural data-object server-side, associating nodes (blocks) in the network with the network ID, properties, and their immediate neighbours.

When a link addition or subtraction occurs, a network should query whether it needs to rebuild itself and split into multiple networks, or merge existing networks together, as well as update nodes of their neighbours or new Lux conditions. Limits on the number of connections per node (and handling duplicate connections) should preferably be done at a BlockEntity level.

Links between two nodes require clear line of sight (treating air, fluids, and replaceable blocks as 'clear'). When establishing a link, connection is blocked if LoS is not available. Nodes with established links occasionally query their LoS validity every so often (throttled from tick updates to optimise performance). When LoS is broken, a network may need to split, just as if a node was removed. Similarly, if LoS is regained, it may have to merge networks together.

> [!note] Implementation
> Networks are **connected components** of the link graph. All state lives in `GlareSavedData` (a `SavedData` persisted under `resourceful_refinement_glare_networks`, always stored on the **overworld** data storage but keyed per-dimension by `DimensionalNodePos`, so it is one global store spanning dimensions — links themselves are still restricted to a single dimension). `GlareService` is a static facade over it.
> - **Split/merge** is handled by `rebuildNetworks()`, which BFS-walks each component over non-`BLOCKED` links. Network UUIDs are preserved by `chooseNetworkId()`, which reuses the old UUID with the **largest node overlap**: on a split the larger fragment keeps the id and the smaller gets a fresh one; on a merge the combined component adopts the id of the largest contributor.
> - Links are undirected `GlareLink(a, b)` records that canonicalise endpoint order so `(a,b) == (b,a)`; cross-dimension and self links are rejected.
> - LoS is sampled by `GlareLineOfSight.hasLineOfSight` at ~4 samples per block; a block blocks the beam only when it is non-air, has no fluid, is not replaceable, and has a non-empty collision shape. Unloaded chunks along the path are treated as clear. Validation is capped at 512 chunk-steps and requires both endpoints (and the chunk-line between them) loaded.
> - Per-tick maintenance lives in `ModGlareEvents`: chunk loads schedule `reconcileLoadedChunk` + `validateLoadedLinks`; each level tick validates a budget of links; the overworld tick also samples Lux history; every `link_sync_interval` ticks a `GlareLinkSyncPayload` is sent to players for beam rendering.

### Establishing Connections

To establish a connection between an existing node (BlockEntity) and a newly-placed node, we use Create's targeting approach it employs for blocks such as Ejectors and Display Links; an item with the IGlareNode interface can right-click on any existing BlockEntity with the IGlareNode to visually highlight that position and add it to its temporary list of potential connection-targets. The number of connection points a node can link to (as an actual block or held-item) is limited to a configurable count (8). If the item is right-clicked anywhere while crouching, the temporary list and highlighted locations are cleared.

Once placed, the new node then tries to form a link with each recorded position, failing and discarding it if a node no longer exists at that location, and either forming a link or not with others depending on their LoS, and updating forming networks as required.

If a targeted node is already at the limit of links it can support, existing links are broken in order to make room for the new link.

Links between GLARE nodes visually appear as a laser-beam-like line-renderer, similar to the beam produced by Guardians and Elder Guardians. It tiles between its two end-points, and uses a distinct texture while LoS is broken.

> [!note] Implementation
> Two complementary linking paths exist — see **Nodes & Linking** below. Held-item targeting is stored on the item stack as the `glare_targets` data component (`GlareTargetsData`, capped at 8, most-recent-wins FIFO eviction); on placement `GlareNodeBlockItem.place` links the placed node to each stored target via `GlareService.tryLink` and clears the component. `LinkResult` values are `CREATED`, `ALREADY_LINKED`, `FAIL_SAME_NODE`, `FAIL_MISSING_NODE`, `FAIL_CROSS_DIMENSION`, `FAIL_LINE_OF_SIGHT`, `FAIL_LINK_LIMIT`, `FAIL_MANUAL_LINK_DISABLED`. When a node is at its limit, `evictOldestIfFull` removes its oldest counted link to make room.

### Implementation Protocols

Because the GLARE system is widely used and very versatile, its code implementation is made as generalised and universal as possible, taking full advantage of class inheritance, shared utility classes, and interfaces. There are many kinds of emitters, receivers, and nodes.

### Server Protocol

GLARE networks are persistent across sessions and clients. Therefore, they function as data structures on the server. Each network has an identifiable slug/GUID, and contains the previously discussed information. Server management also exists for cleaning up empty or stale network objects.

### Lux

Lux is a power resource, analogous to Create's Stress, but for GLARE networks. Like kinetic networks, GLARE networks have an available Lux Capacity, dependent on the number of 'emitters' linked to the network.

'Emitters' are nodes on a GLARE network which produce an amount of Lux (int >= 0), and may supply a Colour-Charge. Emitters add their contribution to the total amount of Lux Capacity on the network, and the total Colour-Charge counts. When an emitter is added, removed, or updates its amount of produced Lux, the network updates its capacity (likewise for Colour-Charge). Emitters implement an IGlareEmitter interface.

'Receivers' are nodes on a GLARE network which allocate a portion of a network's Lux to themselves, in order to do work. Some receivers may require 0 Lux, while others require an integer amount. If the amount of 'allocated' Lux ever exceeds the network's capacity, the entire network enters an 'Overload' state, where nothing requiring Lux operates. Receivers implement an IGlareReceiver interface.

To restart an overloaded network, Lux allocations must be reduced to below or equal to the network's capacity, and a player must interact with any Receiver to toggle its 'Operation Status' back to 'Online'. If successful, this re-enables operation across all nodes and removes the overload state. If the network is still over-allocated when a player tries to switch it to online, it instead returns to the Overload state. This behaviour replicates the 'Power-Grid Overloads' system from Satisfactory.

> [!note] Implementation
> Overload is **all-or-nothing at the network level** — there is no per-receiver throttling. A `NetworkRecord` is `overloaded` iff `luxAllocated > luxCapacity` (capacity = sum of emitter `getProducedLux()`; allocation = sum of receiver `getAllocatedLux()`). `markNetworkOverloaded` sets every receiver's status to `OVERLOADED`; `markNetworkOnline` flips them back to `ONLINE`. Reset is via `GlareService.tryResetNetwork` (fails and re-overloads if still over-allocated) or `forceOverloadNetwork`; both are driven by the `toggle_glare_network` payload from the [[#GLARE Power Terminal]]. Overload is preserved across graph rebuilds: a split keeps its fragments overloaded, but a merge whose combined capacity now suffices clears the overload automatically (`mergedOverloadRecovered`). `GlareOperationStatus` has `ONLINE`, `OFFLINE`, `OVERLOADED` — `OFFLINE` exists but no aggregation path assigns it.

### Variable Lux

Some receivers don't just require a constant amount of Lux. Instead, they consume a fluctuating amount of Lux depending on where they might be in a crafting/processing cycle.

Certain recipe types or machines can vary the amount of Lux their IGlareReceiver allocates/consumes. Typically these BlockEntities define a base 'idle' allocation amount (used when not performing work), and then broadcast a 'processing' amount while progressing through a recipe.

This amount is defined, either in the recipe JSON, or as a final in the class, in the form of an array of ints. Each entry represents an equalised portion, whose value is the amount of Lux allocated to the receiver during that portion of the processing cycle. These arrays are normalised over the processing duration of the recipe, meaning that a processing cycle gradually moves across a curve of Lux values dictated by this array.

E.g.) A recipe lasting 200 ticks might define a Lux consumption array as: `[2,2,4,8,10,8,4,2]`, creating a crude bell curve.

> [!note] Implementation
> The curve is sampled by `GlareLuxCalculator.sampleVariableLux(int[] curve, int progress, int duration)` — step/segment sampling (not interpolation): `index = min(curve.length-1, (int)((long)clamped * curve.length / duration))`, returning `max(0, curve[index])`. With the `{2,2,4,8,10,8,4,2}` curve over a 200-tick duration, progress 0 → 2, progress 100 → 10, progress 199 → 2 (verified by `GlareGameTests`). Curves come from recipes via `recipe.getLuxCurveArray()`; the current consumers are the [[Cyclotron Forge]] (`CyclotronControllerBlockEntity`) and [[Drill Pylon]] (`DrillPylonHeadBlockEntity`), which attach to the network through a [[GLARE  Lux Transceiver|Lux Transceiver]] rather than being nodes themselves.

### Colour Charge

Emitters on a GLARE network also add a 'colour charge' to their network's total. A Colour-Charge is an enum value equivalent to Minecraft's 16 dyes/colours. By default, emitters produce White Lux.

A network tracks the total amount of each (non-zero) type of Colour-Charge being produced across its network. This data can be accessed by specialised receivers, who can utilise the information to run things like filtering or boolean logic, allowing long-distance redstone or communications logic.

> [!note] Implementation
> `NetworkRecord.colourCharges` is a `Map<DyeColor, Integer>`; each emitter contributes its `getProducedLux()` keyed by its colour (set on an [[GLARE Emitter Dish]] by right-clicking with a stained-glass block). Charges are synced to clients as an `int[16]`. The [[GLARE Chromatic Transceiver]] reads these to drive redstone via AND/OR/XOR logic and per-colour thresholds.

### Telemetry Messages & Inboxes

The GLARE network also supports a secondary server-wide system called 'Telemetry Messaging'. A Telemetry system is a child of a GLARE network, and access to it is mediated through GLARE network behaviour.

The Telemetry system manages a dynamic list of 'Inbox Addresses' (defined as unique ordered codes consisting of 3 item IDs which act as a unique key/UUID). Each address has an associated 'Inbox', which is a collection of string messages paired with the address they were sent from, stored in order of arrival. Addresses on a GLARE network will only communicate between themselves — they will not communicate with matching Addresses on other GLARE networks.

The number of messages an inbox can hold is limited to 16 (with newer messages replacing older ones), and messages cannot contain more than 512 characters.

The Telemetry system exposes functionality for BlockEntities or other classes to interact with Inboxes and Messages (such as sending, viewing, or discarding them), as well as ways to subscribe to updates to a specific Inbox.

The Telemetry system also handles creating new Inboxes when a message is first sent to an address, as well as cleaning up empty Inboxes once they are no longer used.

When GLARE networks merge, matching Addresses/Inboxes are merged together (temporarily bypassing inbox size limits). When GLARE networks split, each new network retains a copy of any existing Addresses/Inboxes.

> [!note] Implementation
> Enforced by `TelemetryService` (`MAX_MESSAGES = 16`) and `GlareMessage` (`MAX_BODY_LENGTH = 512`; a `record GlareMessage(UUID id, GlareAddress from, GlareAddress to, String body, long gameTime)`). The 16-message trim happens on **send** (`while inbox.size() > MAX_MESSAGES removeFirst()`). On a graph rebuild, `NetworkRecord.mergeTelemetryFrom` merges inboxes into a `LinkedHashMap` keyed by message UUID (dedup, existing wins), then sorts by `gameTime` then id — it does **not** re-trim, so a merge can temporarily exceed 16 until the next send (matching the design). A split copies inbox contents forward to each fragment. Subscribers receive `InboxUpdate` events with a `Mutation` of `SENT`, `DISCARDED`, or `NETWORK_REBUILT`; `SendResult` is `SENT` / `NODE_MISSING` / `NETWORK_MISSING`. Addresses are `GlareAddress(first, second, third)` item `ResourceLocation`s; `isComplete()` requires none to be air.

## Nodes & Linking

Every GLARE-aware block implements `IGlareNode` (`getMaxGlareLinks()`, `getGlareNodePos()`, and the default `allowsManualGlareLinks()`), and most extend the shared bases `GlareNodeBlock` + `GlareNodeBlockEntity` (or `GlareSmartNodeBlockEntity` for Create `SmartBlockEntity`-based nodes such as the [[Telemetry Terminal GUI|Telemetry Terminal]]). The base BlockEntity tracks the synced network summary (link count, Lux capacity/allocated, overload, operation status, colour charges, Lux history) and exposes it through `GlareNetworkSnapshotProvider` and the goggle tooltip.

There are two ways to create links:

- **Held node-item targeting.** Right-clicking existing nodes with a `GlareNodeBlockItem` (relay, emitter dish, etc.) records each clicked node onto the item's `glare_targets` component (`GlareTargetsData`, max **8** targets, most-recent-wins). The stack shows a foil shimmer and a "Linking to N target(s)" tooltip while it holds targets. Shift-right-click clears them. When the block is then placed, it auto-links to every stored target.
- **The [[Relay Wrench]].** A two-click in-world editor: click a node to select it (persisted on the wrench as the `relay_wrench_target` component), click a second node to link the two. Shift-click a node to strip all its links; shift-click the selected node (or shift-use in air) to cancel the selection.

Nodes that only accept SOCKET links (see below) return `allowsManualGlareLinks() == false` and are skipped by both tools.

## SOCKET Links

Beyond ordinary node-to-node links, GLARE supports a second, special **SOCKET** link kind used by machine controllers that want to consume Lux without being a targetable network node. A [[GLARE  Lux Transceiver|Lux Transceiver]] binds its back face to a `LuxSocket` interface exposed on an adjacent BlockEntity (or its controller) and forms a SOCKET link to that BE's GLARE node.

SOCKET links differ from normal links in three ways: they **do not count toward link limits**, they **do not require or participate in line-of-sight checks**, and they **are not rendered** as beams. The socket target itself is not a targetable node and cannot be picked up by the connection tools. This is how the [[Cyclotron Forge]] and [[Drill Pylon]] controllers join a network — via a Lux Transceiver against a socket face — see [[GLARE  Lux Transceiver]].

> [!note] Implementation
> `GlareSavedData.LinkKind` has exactly two values: `NORMAL(countsTowardLimit=true, requiresLineOfSight=true, renders=true)` and `SOCKET(false, false, false)`. `GlareService.trySocketLink` → `GlareSavedData.tryAddSocketLink` bypasses the limit and manual-link checks and always marks validity `VALID`. Link validity itself is tracked separately as `LinkValidity { UNKNOWN, VALID, BLOCKED }`.

## GLARE Power Terminal

Right-clicking most GLARE blocks with an empty hand opens the **GLARE Power Terminal** overlay — the network's status readout. It shows current vs. maximum Lux, a Lux-history bar graph, the overload state, and a single toggle button. When the network is overloaded the button reads "Restore Network" and attempts to bring it back Online; when healthy it reads "Overload Network" and force-overloads it (useful for testing and for cutting power).

> [!note] Implementation
> Lives in `content/gui`: `PowerTerminal` (overlay widget), `PowerTerminalScreen`, `PowerTerminalMenu` (menu id `power_terminal`, no slots), opened by `GlarePowerTerminalOpener.open` from the emitter dish, relay/kinetic-receiver node blocks, the Lux Transceiver, and the assembled Cyclotron/Drill Pylon controllers. The button sends the `toggle_glare_network` payload (`ToggleGlareNetworkPayload`, a single `BlockPos`); the server branches on overload state to call `tryResetNetwork` or `forceOverloadNetwork`. Snapshot data is `GlareNetworkSnapshot(hasNetwork, currentLux, maxLux, int[] luxHistory, GlareOperationStatus, overloaded)` supplied through `GlareNetworkSnapshotProvider`. History length equals the network's retained samples (config `lux_history_samples`, default 16); the graph auto-scales bars to fit.

## Key Types

These are the framework's core abstractions (exact class names):

- **IGlareNode** — any block/BlockEntity (or held item) that can be a member of a GLARE network and be targeted by the connection tool. Declares `getMaxGlareLinks()`, `getGlareNodePos()`, `allowsManualGlareLinks()` (default true), `getGlareLinkEndpoint()`, and network/link change hooks.
- **IGlareEmitter** — a node that produces an amount of Lux (`getProducedLux()`, int >= 0), supplies a Colour-Charge (`getLuxColourCharge()`, default White), and can be gated (`isGlareEmitterEnabled()`).
- **IGlareReceiver** — a node that allocates Lux (`getAllocatedLux()`) and carries an Operation Status used to recover from Overload. Variable-Lux receivers drive their allocation from a per-cycle curve via `GlareLuxCalculator`.
- **IGlareTelemetryEndpoint** — a node that owns a `GlareAddress` reachable by telemetry messages.
- **LuxSocket** — a face-sensitive interface (`getLuxSocketNode(Direction)` / `isLuxSocketEnabled(Direction)`) that lets a non-node BlockEntity expose a GLARE node on one of its faces for a [[GLARE  Lux Transceiver]] to bind to.
- **GlareService / GlareSavedData** — the static facade and its persistent store (graph, networks, links, telemetry).
- Supporting enums/records: `GlareOperationStatus`, `GlareLogicMode {AND, OR, XOR}`, `GlareComparison`, `GlareLink`, `DimensionalNodePos`, `GlareAddress`, `GlareMessage`, `GlareTargetsData`.

## How Features Use It

The following entries are specific implementations of blocks utilising the GLARE network framework, or are related to its construction/operation. Each now has its own page.

### Resonance Crystals

Resonance Crystals are (by intent) terrain features found rarely throughout the overworld and nether, and commonly in the End. Alternatively, players can craft Artificial Resonance Crystals themselves using resources obtained from Crystal Drills. Placing Artificial Resonance Crystals in the End Dimension causes a deadly explosion. GLARE Emitter Dishes can be placed on top of Resonance Crystals to power a GLARE network. See [[Resonance Crystal]].

> [!note] Implementation
> Both `resonance_crystal` and `artificial_resonance_crystal` blocks/items exist (`ResonanceCrystalBlock`, sharing a class with an `artificial` flag); the artificial variant detonates (`explode(..., 6.0F, BLOCK)`) when placed in the End. **Acquisition gap:** there is currently **no worldgen feature** placing natural crystals and **no crafting recipe** for either crystal in this source tree (only blockstate/model/item/lang assets and a block loot table). Crystal Drills are not yet implemented. In practice the crystals are only obtainable via creative/commands until an obtain path is authored.

### GLARE Emitter Dish

Emitters provide an integer amount of Lux to a GLARE network. Emitters must be placed on top of *Resonance Crystals* to function, and can be toggled off by redstone. Emitters can also emit a colour-code, set by right-clicking with a stained glass block (default White). Emitter Dishes target only 1 other connection point. See [[GLARE Emitter Dish]].

> [!note] Implementation
> `GlareEmitterDishBlockEntity extends KineticBlockEntity`: `BASE_LUX = 8`, `REQUIRED_RPM = 32.0`, `MAX_LINK_COUNT = 1`, stress impact **8** (`ModStressValues.GLARE_EMITTER_STRESS`). Enabled only when a resonance crystal sits directly below, no redstone signal is present, `|RPM| >= 32`, and it is not overstressed.

### GLARE Relay

Relays act as universal connection nodes in GLARE networks. They accept up to 8 different connections. See [[GLARE Relay]].

> [!note] Implementation
> `GlareRelayBlockEntity.MAX_LINK_COUNT = 8`; a plain `GlareNodeBlockEntity` with no emitter/receiver role.

### GLARE Kinetic Receiver

Kinetic Receivers are intended to consume 1 Lux and produce 256 units of Stress at 128 RPM when powered by a GLARE network. They target only 1 other connection point. See [[GLARE Kinetic Receiver]].

> [!note] Implementation
> **Partial.** `GlareKineticReceiverBlockEntity` allocates `getAllocatedLux() = 1` and tracks its operation status, but it does **not** currently produce any Stress or RPM — it does not extend a Create kinetic base and generates no rotation. As of v0.4 it is a functional 1-Lux no-op receiver; the 256-stress-at-128-RPM behaviour is unimplemented design intent.

### GLARE Chromatic Transceiver

Transceivers produce a redstone output according to the colour-charges present on their network. They can be configured to filter for a select list of colour-charges using AND/OR/XOR logic and count thresholds. Chromatic Transceivers target only 1 other connection point. See [[GLARE Chromatic Transceiver]].

> [!note] Implementation
> `GlareChromaticTransceiverBlockEntity` draws **0 Lux** (`getAllocatedLux() = 0`), `MAX_LINK_COUNT = 1`. Per-`DyeColor` `thresholds[]`/`comparisons[]` (`GlareComparison`, `DISABLED_FILTER = -1`, `MAX_THRESHOLD = 1_000_000`) combined by `GlareLogicMode` (AND/OR/XOR). Configured through its menu (`glare_chromatic_transceiver`) and the `configure_glare_transceiver` payload.

### Telemetry Terminals

Telemetry Terminals let players use GLARE networks for communication. Each terminal carries a 3-item Address ID and offers MANUAL, AUTO-SEND, and AUTO-RECEIVE modes. See [[Telemetry Terminal GUI]] for the full terminal specification.

### Remote Entangler Depot & Transporter

The Remote Entangler Depot teleports item stacks between addressed depots on the same GLARE network; the Remote Entanglement Transporter teleports players between addressed transporters. Both require an operational network, Lux allocation, and a *chilled* heat source. See [[Remote Entanglement]] for the full specification.

## Implementation

The GLARE framework is implemented under `content/glare/` (subpackages `common`, `graph`, `lux`, `remote`, `rendering`, `telemetry`, `terminal`, `ui`) and the overlay GUI under `content/gui/`.

**Core classes:** `GlareService` (static facade), `GlareSavedData` (persistent graph/network/telemetry store, incl. nested `NodeRecord`, `NetworkRecord`, `LinkKind`, `LinkValidity`, `LinkResult`, `Diagnostics`), `GlareLink`, `DimensionalNodePos`, `GlareLineOfSight`, `GlareLuxCalculator`, `TelemetryService`, `GlareMessage`, `GlareAddress`, `ModGlareEvents` (tick/chunk hooks), `GlareCommands`, `GlareDebug`, `GlareGameTests`.

**Interfaces:** `IGlareNode`, `IGlareEmitter`, `IGlareReceiver`, `IGlareTelemetryEndpoint`, `content/glare/lux/LuxSocket`.

**Node bases:** `GlareNodeBlock`, `GlareNodeBlockEntity`, `GlareSmartNodeBlockEntity`, `GlareNodeBlockItem`, plus per-feature nodes (`GlareRelayBlockEntity`, `GlareEmitterDishBlockEntity`, `GlareKineticReceiverBlockEntity`, `GlareChromaticTransceiverBlockEntity`).

**Registry IDs:**
- Blocks/items (`ModBlocks`, `ModItems`): `glare_relay`, `glare_emitter_dish`, `glare_kinetic_receiver`, `glare_chromatic_transceiver`, `glare_telemetry_terminal`, `lux_transceiver`, `remote_entangler_depot`, `remote_entanglement_transporter` (+ proxies `remote_entanglement_transporter_tank` / `_casing`), `resonance_crystal`, `artificial_resonance_crystal`, `relay_wrench`, `flux_dust`.
- BlockEntities (`ModBlockEntities`): matching ids, plus `remote_entanglement_transporter_proxy` (shared by tank + casing).
- Menus (`ModMenus`): `glare_chromatic_transceiver`, `glare_telemetry_terminal`, `power_terminal`.
- Data components (`ModDataComponents`): `glare_targets` (`GlareTargetsData`), `relay_wrench_target` (`DimensionalNodePos`).

**Config keys** (`ServerConfig`, section "GLARE Networks"):
- `debug_logging` — default `false`; logs topology/reconciliation/LoS mutations.
- `los_checks_per_tick` — default `32` (range 1–4096); LoS links examined per dimension tick.
- `link_sync_interval` — default `20` (range 1–200); ticks between link render-sync packets.
- `lux_history_sample_interval` — default `40` (range 1–1200); ticks between Lux history samples.
- `lux_history_samples` — default `16` (range 1–256); Lux history samples retained per network.

**Network payloads** (`network/`): `glare_link_sync` (`GlareLinkSyncPayload`, S2C beam sync), `configure_glare_transceiver` (`ConfigureGlareTransceiverPayload`), `telemetry_terminal_action` (C2S) / `telemetry_terminal_state` (S2C), `toggle_glare_network` (`ToggleGlareNetworkPayload`).

**Commands:** `/rrglare stats | inspect [pos] | validate [budget] | rebuild` (requires permission level 2).

## Related

- [[GLARE  Lux Transceiver]]
- [[GLARE Emitter Dish]]
- [[GLARE Relay]]
- [[GLARE Kinetic Receiver]]
- [[GLARE Chromatic Transceiver]]
- [[Resonance Crystal]]
- [[Relay Wrench]]
- [[Telemetry Terminal GUI]]
- [[Remote Entanglement]]
- [[Cyclotron Forge]]
- [[Drill Pylon]]
- [[Harmonic Cyclotron]]
- [[Primary Design Doc]]
