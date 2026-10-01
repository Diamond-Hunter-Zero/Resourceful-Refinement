---
title: Telemetry Terminal GUI
category: Machine
status: Implemented
introduced: v0.4
recipe_type: n/a
related:
  - "[[GLARE Networks]]"
tags:
  - machine
  - glare
  - network
  - client
  - gui
---

The Telemetry Terminal is a horizontally rotatable Create SmartBlockEntity which connects to a [[GLARE Networks|GLARE network]] as a receiver, and enables network-wide communication through the telemetry system.

The Terminal cannot form direct GLARE links itself. Instead it exposes a **Lux Socket** on its local-back face; a [[GLARE Networks|Lux Transceiver]] seated against that face bridges the Terminal into the Transceiver's connected network. While bridged, the Terminal allocates a configurable amount of Lux (default 1).

**ID:** *glare_telemetry_terminal*

## Gameplay Role

The Telemetry Terminal is the player-facing front-end for the GLARE network's Telemetry Messaging system (see [[GLARE Networks]]). It lets players read, compose, send, and automate inbox messages over the network, turning the addressed inbox model into an in-game e-mail client with manual and redstone-automated modes.

## Construction & Placement

A horizontally rotatable Create SmartBlockEntity. Each Terminal is assigned an Address by using a row of 3 Create item-slot behaviours positioned on the top face of the block (similar to the implementation in Redstone Links).

> [!note] Implementation
> `TelemetryTerminalBlock extends GlareNodeBlock` (horizontal `FACING`, default NORTH); `TelemetryTerminalBlockEntity extends GlareSmartNodeBlockEntity implements IGlareReceiver, IGlareTelemetryEndpoint, LuxSocket, MenuProvider`. `MAX_LINK_COUNT = 1`. The three top-face slots are `TelemetryAddressBehaviour` (extends Create's `FilteringBehaviour`, one item each, forced count 1) laid out via `Trio.makeSlots`; they form the terminal's own `GlareAddress` (`GlareAddress.of(item0, item1, item2)`). A legacy `AddressInventory` NBT layout is migrated on load.
>
> **Lux Socket connectivity:** `allowsManualGlareLinks()` returns `false`, so the Terminal is rejected by every manual-link path (`GlareSavedData.tryAddLink`/`canAcceptLink` and the Relay Wrench all gate on this flag). It implements `LuxSocket.getLuxSocketNode(side)`, returning itself on its local-back face (`FACING.getOpposite()`); an adjacent `LuxTransceiverBlockEntity` bridges it via `GlareService.trySocketLink` (a socket link, which bypasses the manual-link flag). `onLoad` calls `refreshAdjacentLuxTransceiver()` to (re)form the socket link once the node is registered — the Transceiver's own `neighborChanged`/`onPlace`/`onRemove` handle placement and teardown. `getAllocatedLux()` returns `ServerConfig.GLARE_TELEMETRY_TERMINAL_LUX` (default 1).

## Inputs & Outputs

- **GLARE link:** the Terminal does not link directly to relay nodes. It exposes a Lux Socket on its local-back face; a Lux Transceiver placed against that face joins the Terminal to the Transceiver's network. The Terminal allocates `telemetry_terminal_lux` Lux (default 1) while bridged.
- **Address slots:** the 3 top-face item-slot behaviours set the terminal's Address ID.
- **Redstone:** in Auto-Send mode a redstone pulse triggers a send; in Auto-Receive mode a matching message emits a redstone pulse.
- **Create Display Links:** in Auto-Send mode, a terminal that is the display target of one or more Display Links reads its outgoing body from them (see Operation).

## Operation

The Telemetry Terminal can be right-clicked to open a versatile GUI that lets players view, send, and automate the handling of inbox messages. A row of browser-like tabs along the top switches the terminal between its 3 modes: MANUAL, AUTO-SEND, and AUTO-RECEIVE. Each mode represents a different function of the terminal, and a different GUI state or sub-screen.

Where possible, existing confirm and trash button icons from the project textures are reused.

> [!note] Implementation
> Modes are the `TelemetryTerminalMode` enum `{ MANUAL, AUTO_SEND, AUTO_RECEIVE }`, cycled by right-clicking with a wrench (`cycleMode()`, action-bar message `telemetry_terminal.mode`). The screen (`TelemetryTerminalScreen`) maps each mode to a page: `ManualTelemetryTerminalPage`, `AutoSendTelemetryTerminalPage`, `AutoReceiveTelemetryTerminalPage` (the last two share `ComposeTelemetryTerminalPage`). Menu id `glare_telemetry_terminal` (`TelemetryTerminalMenu`, no player slots — the GUI is snapshot-driven).

### Manual

In its default manual mode, the Telemetry Terminal GUI behaves like an email program. It has two sub-screens toggled by tabs on the top-left: an 'Inbox' view and a 'Compose Message' view.

#### Inbox

The tab for the Inbox view says "Inbox (X messages)" when the address is storing 1 or more messages, or "Inbox" otherwise.

This view consists of a scroll-view along the left-hand edge showing a summary of each message currently in the inbox, with a top row showing the sender's Address code as rendered items, and a bottom row of grey text showing the first few characters of the message.

The right-hand two thirds of the view shows a large readout of the message content itself. When a message tab is selected in the left-side scroll-view, it is displayed in full on the right-hand side, with the address code along the top, and the sent date/time anchored to the top-right. The full text message is then printed below. The method for rendering the text content over multiple lines is defined in a generalised way that allows adjusting the size and width of the right-hand-side panel when proper UI assets are made. At the bottom-right of the content panel is a "Discard" button; pressing it removes the message from the inbox.

#### Compose Message

The Compose Message view lets the user type a new message to send to an address.

On the left-hand edge of the view is a scroll-view of all the terminal's 'saved' Addresses (contacts). Clicking one clears the current address and repopulates it with the saved one.

The rest of the view is the message editor. Along the top, it has 3 item-slot buttons for setting the receiver's address code — pressing these opens a mini Creative-mode-style search window for finding any Minecraft item (its own screen). There is also a small 'Save' button to the right, which saves the current code as a saved contact if fully populated and not already listed.

The middle of the viewer is a large text field for the message body text, wrapping text that goes over the panel's width. In the bottom-left corner is a 'Clear' button which clears all text.

At the bottom of the view is a "Send" button, enabled and functional only if the address is filled out and the body is non-empty. Pressing 'Send' sends the message to the target address if it exists on the network. If the address does not exist, the warning line along the bottom says "Address not found on this network"; otherwise it briefly says "Message sent!" and resets the view.

> [!note] Implementation
> The item-code picker is `TelemetryItemPickerScreen`. Compose actions map to `TelemetryTerminalActionPayload` actions `SET_MANUAL_VIEW`, `SAVE_MANUAL_DRAFT`, `ADD_CONTACT`, `SEND_MANUAL`, `DISCARD_MESSAGE`. The inbox list shows 6 visible rows with a scrollbar; the contacts list likewise.

### Auto-Send

In auto-send mode, the GUI uses an altered variant of the Compose Message view. The address code and body behave the same. There is no 'Send' button; instead, whenever the user edits the view, the terminal caches the content and sends it to the address whenever it receives a redstone pulse. The bottom of the UI shows warning labels that now display either "Last message successfully sent" or "Last message unable to find address", depending on the most-recent operation.

Alternatively, if the Telemetry Terminal is the display target of one or more Create Display Links, the body editor is disabled and a "Readout from Display Link" header is shown, with the text produced by all attached display links below. In this mode, the combined content of the linked display links is sent as the body on a redstone pulse (truncated if needed).

> [!note] Implementation
> AUTO_SEND is a rising-edge trigger: `onNeighborChanged` calls `performAutoSend` when neighbour signal goes high (tracked via `wasRedstonePowered`). `TelemetryTerminalDisplayTarget extends DisplayTarget` provides `DisplayTargetStats(8, 64)` (8 lines × 64 chars). Readouts are stored per source `BlockPos`, pruned after 100 ticks, joined and sanitised to 512 chars; `effectiveAutoSendBody()` prefers the display-link text over the cached body. Drafts persist via `SAVE_AUTO_DRAFT`.

### Auto-Receive

In auto-receive mode, the GUI has a unique view: a wide scroll-view of all current string-filters cached on the terminal, each with a small 'delete' (trash) button anchored to the right. Below the scroll-view is a text input field with a confirm button; entering text and pressing confirm adds it as a new filter entry (if not already present) and clears the input. Above the scroll-view is a centred button that toggles between "Keep Messages" and "Discard Messages".

When an auto-receive terminal receives a message containing any of its string-filters, it emits a redstone pulse for 1 tick (even if already emitting). If set to "Discard Messages", it then discards the triggering message from the inbox. String-filters are always compared case-insensitively.

Auto-discarding is deferred so that any other auto-receive terminals listening to the same address still trigger their comparison-and-emit logic for that same message that tick.

> [!note] Implementation
> The filter list shows 7 visible rows. Actions: `ADD_FILTER`, `REMOVE_FILTER`, `SAVE_FILTER_DRAFT` (10-tick debounce), `SET_DISCARD`. Filters are sanitised (trimmed, lowercased, truncated to `MAX_FILTER_LENGTH = 64`). The pulse is implemented as a deferred `TickTask` setting `pulseTicks = 2` (≈2-tick, 15-strength signal at the terminal and the block below); `tick()` clears it. Discard-after-match runs via `TelemetryService.discard` only after the pulse, so concurrent listeners still see the message that tick.

## Rendering

`TelemetryTerminalRenderer extends SmartBlockEntityRenderer`; the 3 address slots render their filter items on the top face (`TelemetryAddressRenderer` / `TelemetryAddressBehaviour`).

## Implementation

**Package:** `content/glare/terminal/`.

**Key classes:** `TelemetryTerminalBlock`, `TelemetryTerminalBlockEntity`, `TelemetryTerminalMenu`, `TelemetryTerminalScreen`, `TelemetryItemPickerScreen`, `TelemetryTerminalRenderer`, `TelemetryAddressBehaviour`, `TelemetryAddressRenderer`, `TelemetryTerminalMode`, `TelemetryTerminalSnapshot`, `TelemetryTerminalResult`, `TelemetryTerminalDisplayTarget`, and the GUI pages `TelemetryTerminalPage` / `Manual…` / `Compose…` / `AutoSend…` / `AutoReceive…`.

**Registry IDs:** block/item/BE `glare_telemetry_terminal`; menu `glare_telemetry_terminal`.

**Caps & constants:**
- `TelemetryService.MAX_MESSAGES = 16` (per-inbox cap, enforced on send).
- `GlareMessage.MAX_BODY_LENGTH = 512` (message body cap).
- `TelemetryTerminalSnapshot.MAX_CONTACTS = 32`, `MAX_FILTERS = 32`, `MAX_FILTER_LENGTH = 64`, `MAX_SYNCED_MESSAGES = 256` (the client snapshot may sync up to 256 messages, but the actual inbox never exceeds 16).
- `ServerConfig.GLARE_TELEMETRY_TERMINAL_LUX` (`telemetry_terminal_lux`, default 1, range 0–1000000, under *GLARE Networks*) — Lux the Terminal allocates from its network while bridged.

**State sync:** the terminal broadcasts a revisioned `TelemetryTerminalSnapshot` — every mutation calls `changed()` which does `revision++`, then (server-side) refreshes the inbox subscription, saves, and calls `TelemetryTerminalStatePayload.broadcast` to all players viewing that terminal. The client `TelemetryTerminalMenu.applySnapshot` ignores stale packets (`revision` must be `>=` current); `revision` is persisted in NBT. Loaded-only automation is driven by a non-persisted `TelemetryService.Subscription`.

**Network payloads:** `telemetry_terminal_action` (`TelemetryTerminalActionPayload`, C2S; 12 actions — `SET_MODE`, `SET_MANUAL_VIEW`, `SAVE_MANUAL_DRAFT`, `SAVE_AUTO_DRAFT`, `ADD_CONTACT`, `REMOVE_CONTACT`, `ADD_FILTER`, `SAVE_FILTER_DRAFT`, `REMOVE_FILTER`, `SET_DISCARD`, `SEND_MANUAL`, `DISCARD_MESSAGE`) and `telemetry_terminal_state` (`TelemetryTerminalStatePayload`, S2C snapshot).

**Redstone:** `isSignalSource() = true`; `getSignal` returns 15 while pulsing.

## Related

- [[GLARE Networks]]
