---
title: PUG & Launch Pads
category: Machine
status: Implemented
introduced: v0.4
recipe_type: n/a
related:
  - "[[GLARE Networks]]"
  - "[[Telemetry Terminal GUI]]"
  - "[[Primary Design Doc]]"
tags:
  - machine
  - multiblock
  - logistics
  - glare
  - entity
---

The PUG (Payload Uncrewed Gadget) system moves resources over long distances or between dimensions. It consists of **Launch Pads** (flat 3×3 multiblocks that define send/receive endpoints and provide interfaces for cargo, destination addressing, and fuel) and **PUG entities** (which carry cargo and travel — partly as entities, partly as simulated data objects).

## Gameplay Role

Launch Pads paired with PUGs transport items between locations or dimensions (outside the Nether). The system is the late-game logistics tier for moving cargo across the world and between dimensions without physical belts, complementing the shorter-range and network-based options such as [[GLARE Networks]] and the Remote Entanglement family.

## Construction & Placement

Launch Pads are a flat 3×3 multiblock, constructed when a **Launchpad Controller** is placed. The controller is horizontally rotatable, so pads take any orientation. It places its 8 **Launchpad Proxies** behind and beside it, so the controller sits at the centre-front edge of the pad (`LaunchpadStructure`: `WIDTH = 3`, `DEPTH = 3`, `PROXY_COUNT = 8`; local depth grows behind the controller, lateral spans −1..+1). If the controller cannot find enough replaceable space, assembly fails and it remains non-functional; a player can right-click to retry assembly of an incomplete controller instead of opening its GUI. If any proxy or the controller is broken, the whole assembly is destroyed and the controller drops.

Assembled launchpads are globally recorded and known to the server (position, mode, and address), and act as chunk loaders for the chunk the controller occupies.

> [!note] Implementation
> Assembly and validation live in `LaunchpadControllerBlockEntity` (a `SmartBlockEntity`), which re-validates its proxies periodically (`SKY_CHECK_INTERVAL = 20`) and tears down the assembly if a proxy goes missing. The `ASSEMBLED` block state drives the controller's model. Global state / chunk loading is handled through `PugService`, `PugSavedData`, and `PugChunkLoading`.

## Inputs & Outputs

- The **rear-centre proxy's** back face is the fuel intake for the controller's fuel tank (`isRearCenter()` = lateral 0, deepest row; fuel accepted only on `rearFace(facing)`).
- The **side faces of the other proxies** act as in/out item interfaces to the controller inventory.

Launchpads have a **12-slot** inventory (`INVENTORY_SLOTS = 12`). A single PUG launch carries up to **6 slots** of cargo (`CARGO_SLOTS = 6`), pulled round-robin from the inventory.

Fuel is any fluid in the `resourceful_refinement:carborax_fuel` tag (`PugTags.CARBORAX_FUEL`), held in a `FluidTank` of capacity `ServerConfig.PUG_TANK_CAPACITY_MB`.

> [!note] Implementation
> Item automation goes through an `ArrivalAwareItemHandler` that also exposes a docked/unloading PUG's cargo slots; the fuel handler is insert-only. The rear-centre proxy is item-inert (fuel only).

## Operation

The controller holds a `LaunchpadConfiguration` record: `mode`, `localAddress`, `destinationAddress`, `launchCondition`, `timerSeconds`. It carries a 3-item **GlareAddress** set via the address slots along the top of the block (the same `TelemetryAddressBehaviour` used by the [[Telemetry Terminal GUI]]).

- **Mode** — `LaunchpadMode { SEND, RECEIVE }`. Toggled via wrench or in the GUI; each mode shows a different view (menu id `launchpad`, opened by right-clicking an assembled controller).
- **Launch Condition** — `LaunchCondition { WHEN_FULL, REDSTONE, TIMER }`.
- **Launch Timer** — `timerSeconds`, 0 s to `MAX_TIMER_SECONDS = 3600` (1 hr), default 120 s; only relevant on `TIMER`.

A **SEND** controller's GUI shows, on the left, the destination address input, launch condition, and (in timer mode) the launch timer; top-right a fuel indicator (reserves vs fuel needed for the current address); bottom-right a printout of any conditions halting the next launch; and the pad + player inventory slots.

![[Launchpad Controller Send GUI Mock-up.png]]

A **RECEIVE** controller's GUI shows the pad inventory and an infrequently-updating track of the count and progress of PUGs currently inbound.

![[Launchpad Controller Receive GUI Mock-up.png]]

When cargo enters a SEND pad, and it has a valid destination, enough fuel to reach it, clear sky above the footprint, and its Launch Condition is met, a PUG is deployed: it flies upward carrying up to 6 cargo slots and the required fuel is drained. A launch always fails if there is no cargo. If multiple RECEIVE pads share the same address, the destination is ambiguous and launch halts.

> [!note] Implementation
> All the halt reasons are enumerated by `LaunchpadFailureReason`: `NONE`, `INCOMPLETE_ASSEMBLY`, `WRONG_MODE`, `NO_CARGO`, `PUG_NOT_FULL`, `INCOMPLETE_DESTINATION_ADDRESS`, `DESTINATION_MISSING`, `DESTINATION_AMBIGUOUS`, `SOURCE_SKY_OBSTRUCTED`, `DESTINATION_SKY_OBSTRUCTED`, `INSUFFICIENT_FUEL`, `WAITING_FOR_REDSTONE`, `WAITING_FOR_TIMER`, `PAD_OCCUPIED`. Launch attempts return a `LaunchAttemptResult` from `PugFlightService.attemptLaunch`. Clear-sky is scanned column-by-column above the footprint to build height.

### PUG Flight

PUGs do not travel as entities the whole way, to save performance and bypass chunk-loading limits. Flight is modelled by `PugFlightState`: `DOCKED → ASCENDING → IN_TRANSIT → QUEUED → DESCENDING → UNLOADING`, plus `CRASHED`. `IN_TRANSIT` and `QUEUED` are **simulated** (no entity); the rest are physical.

On launch the PUG (a `PugEntity`, `FLIGHT_STATE` synched) ascends to world height, then de-instantiates into a simulated `PugFlightRecord` (`IN_TRANSIT`). The server increments travel progress each tick over all simulated PUGs. Travel time and fuel are computed from distance (and a cross-dimension surcharge) via `PugRouteCalculator` using `ServerConfig` values (`PUG_BASE_FUEL_MB`, `PUG_FUEL_PER_STEP_MB`, `PUG_FUEL_STEP_BLOCKS`, `PUG_CROSS_DIMENSION_FUEL_MB`, and the matching `*_TRAVEL_TICKS*` keys).

Once travel completes, the PUG waits for its destination pad to be free; multiple finished PUGs may sit in a `QUEUED` state. When the pad is available the flight claims it (`tryClaim`), respawns as a physical entity `DESCENDING`, lands, and enters `UNLOADING`, transferring its cargo into the pad's inventory (remaining as an entity until all items fit). It is then discarded and the pad released.

If the destination no longer exists at the end of travel (controller removed, address changed, or moved), the PUG instead **crash-lands** at a random block within 2 blocks of its last known location (never the exact coordinates) and becomes a world entity with a lootable inventory. A crashed PUG can also be destroyed by attacking it, dropping its contents. PUGs are immune to all damage while not crashed, and cannot be leashed, boated/minecarted, or spawned from an egg.

> [!note] Implementation
> Flight orchestration is split across `PugFlightService` (per-controller ticking, launch, claiming), `PugService` / `PugSavedData` (global registry, persistence), `PugRouteCalculator` (`PugRouteParameters` / `PugRouteQuote` — fuel + ticks), and `PugFlightRecord` (the simulated in-transit payload). Endpoints and ids use `LaunchpadEndpoint` / `LaunchpadId` / `LaunchpadSnapshot`. Events in `ModPugEvents`; game tests in `PugGameTests`.

## Rendering

The PUG renders as a lander vehicle (`PugEntityRenderer` + `PugLanderModel`) only while `ASCENDING` from a SEND pad and `DESCENDING`/`UNLOADING` at a RECEIVE pad; between those phases it is a simulated data object with no in-world rendering. Crashed PUGs render as a world entity with a lootable inventory. Sounds: `pug_launch`, `pug_flight`, `pug_land`, `pug_crash` (`ModSounds`).

## Implementation

- **Package:** `content/pug/`
- **Controller:** `LaunchpadControllerBlock` / `LaunchpadControllerBlockEntity` (`SmartBlockEntity`, `MenuProvider`), id `launchpad_controller`.
- **Proxy:** `LaunchpadProxyBlock` / `LaunchpadProxyBlockEntity`, id `launchpad_proxy`.
- **Structure:** `LaunchpadStructure` (`WIDTH = 3`, `DEPTH = 3`, `PROXY_COUNT = 8`).
- **Config records:** `LaunchpadConfiguration`, `LaunchpadMode`, `LaunchCondition`, `LaunchpadFailureReason`, `LaunchAttemptResult`.
- **Menu / screen:** `LaunchpadMenu` (menu id `launchpad`), `LaunchpadScreen`, `LaunchControllerGuiTextures`; network sync via `LaunchpadStatePayload`.
- **Entity:** `PugEntity` (id `resourceful_refinement:pug`), `PugEntityRenderer`, `PugLanderModel`; `PugFlightState`, `PugFlightRecord`.
- **Services / persistence:** `PugFlightService`, `PugService`, `PugSavedData`, `PugRouteCalculator` (`PugRouteParameters`, `PugRouteQuote`, `PugDestinationResult`), `PugChunkLoading`, `ModPugEvents`, `PugTags`.
- **Config keys (`ServerConfig`):** `PUG_TANK_CAPACITY_MB`, `PUG_BASE_FUEL_MB`, `PUG_FUEL_PER_STEP_MB`, `PUG_FUEL_STEP_BLOCKS`, `PUG_CROSS_DIMENSION_FUEL_MB`, `PUG_BASE_TRAVEL_TICKS`, `PUG_TRAVEL_TICKS_PER_STEP`, `PUG_TRAVEL_STEP_BLOCKS`, `PUG_CROSS_DIMENSION_TRAVEL_TICKS`.
- **Fuel tag:** `resourceful_refinement:carborax_fuel`.
- Constants: `INVENTORY_SLOTS = 12`, `CARGO_SLOTS = 6`, `SKY_CHECK_INTERVAL = 20`, `MAX_TIMER_SECONDS = 3600`.

## Related

- [[GLARE Networks]]
- [[Telemetry Terminal GUI]]
- [[Primary Design Doc]]
