---
title: GLARE Kinetic Receiver
category: Machine
status: Implemented
introduced: v0.4
recipe_type: n/a
related:
  - "[[GLARE Networks]]"
  - "[[GLARE Emitter Dish]]"
  - "[[GLARE Relay]]"
tags:
  - machine
  - glare
  - kinetic
  - network
  - generator
---

The GLARE Kinetic Receiver is the mirror of the [[GLARE Emitter Dish]]: a receiver node that draws Lux from a [[GLARE Networks|GLARE network]] and turns it back into Create rotational power, letting a network drive kinetic machinery remotely across gaps that shafts cannot span. It allocates Lux, supports one link, and acts as a Create **generator** — outputting a kinetic shaft from its local-back face while powered.

**ID:** `glare_kinetic_receiver`

## Gameplay Role

Where the [[GLARE Emitter Dish]] spends rotation to make Lux, the Kinetic Receiver spends Lux to make rotation, closing the loop so a GLARE network can transmit kinetic power wirelessly. It is a receiver: it allocates Lux from the network, and if total allocation ever exceeds capacity it contributes to overloading the network (at which point it stops generating until the network is restored).

## Construction & Placement

A **fully directional** node block (a Create kinetic block orientable to any of the six directions). It is placed and linked like other GLARE nodes — the **front** face points toward the player on placement, and the **shaft is emitted from the opposing back face**, so the shaft can be aimed up, down, or along any horizontal axis. It supports a single link (`MAX_LINK_COUNT = 1`); extend its reach through a [[GLARE Relay]].

## Inputs & Outputs

- **GLARE link:** one (`MAX_LINK_COUNT = 1`).
- **Lux (input):** allocates a configurable amount of Lux from the network — default **1** (`kinetic_receiver_lux`).
- **Kinetic output:** a rotating shaft on the **local-back face** (opposite the front/`FACING`, any of the six directions), with the rotation axis aligned to the block's facing. Any Create kinetics abutting that face receive the rotation.

## Operation

While the receiver is linked to a network that is **online** (its operation status is `ONLINE` and the network is not overloaded), it generates rotation:

- **Speed:** configurable output RPM — default **32 RPM** (`kinetic_receiver_rpm`).
- **Stress capacity:** a configurable per-RPM capacity — default **4 su/RPM** (`kinetic_receiver_stress`). Following Create's standard generator convention, total stress provided = capacity × speed, so the defaults yield **128 su at 32 RPM**.

If the network overloads (allocation exceeds capacity), the network flips the receiver's operation status and it stops generating; once capacity is restored, a player interacting through the [[GLARE Networks|Power Terminal]] brings it — and the rest of the network — back online, and generation resumes. An unlinked receiver produces nothing.

## Recipes

None.

## Rendering

Standard node block model; the shaft connects logically from the back face. The goggle tooltip reports the link count, Lux draw, and, while powered, the generated `su`/RPM; otherwise it shows the receiver's status (Overloaded / other) or "Unlinked".

## Implementation

- **Block:** `GlareKineticReceiverBlock` (extends Create `DirectionalKineticBlock` — full six-way `FACING` — implements `IBE`). `hasShaftTowards` is true only for the back face (`FACING.getOpposite()`); `getRotationAxis` is the facing axis; placement (inherited from `DirectionalKineticBlock`) sets the front toward the player. The blockstate JSON maps all six `facing` values (up/down via `x` rotation, horizontals via `y`).
- **Block entity:** `GlareKineticReceiverBlockEntity` (extends Create `GeneratingKineticBlockEntity`; implements `IGlareNode`, `IGlareReceiver`, `IHaveGoggleInformation`, `GlareNetworkSnapshotProvider`).
  - `getGeneratedSpeed()` returns the configured RPM while powered, else 0.
  - `calculateAddedStressCapacity()` returns the configured per-RPM capacity while powered (Create multiplies it by speed for the network total) — the standard Create generator definition.
  - `updateGeneratedRotation()` is re-run on load, on network/link changes, on operation-status changes, and as a per-tick guard, so the shaft tracks the network's power state.
  - Powered condition: linked to a network **and** operation status `ONLINE`.
- **Config** (`ServerConfig`, "Kinetics" section): `kinetic_receiver_rpm` (default 32, 0–4096), `kinetic_receiver_stress` (per-RPM capacity, default 4, 1–1,000,000), `kinetic_receiver_lux` (default 1, 0–1,000,000).
- **Registry IDs:** block/item `resourceful_refinement:glare_kinetic_receiver`; block entity type `ModBlockEntities.GLARE_KINETIC_RECEIVER_BE`.
- **State:** a `GlareOperationStatus` (`ONLINE` by default) persisted under the `Status` NBT key, plus the synced GLARE network summary.

## Related

- [[GLARE Networks]]
- [[GLARE Emitter Dish]]
- [[GLARE Relay]]
