---
title: GLARE Kinetic Receiver
category: Machine
status: Partial
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
---

The GLARE Kinetic Receiver is intended to be the mirror of the [[GLARE Emitter Dish]]: a receiver node that draws Lux from a [[GLARE Networks|GLARE network]] and turns it back into Create rotational power, letting a network drive kinetic machinery remotely. It allocates a single unit of Lux and supports one link.

**ID:** *glare_kinetic_receiver*

## Gameplay Role

Where the [[GLARE Emitter Dish]] spends rotation to make Lux, the Kinetic Receiver is designed to spend Lux to make rotation — closing the loop so a GLARE network can transmit kinetic power across gaps that shafts cannot span. It is a receiver: it allocates Lux from the network and, if total allocation ever exceeds capacity, contributes to overloading the network.

## Construction & Placement

A horizontal directional node block, placed and linked like other GLARE nodes. It supports a single link (`MAX_LINK_COUNT = 1`); extend its reach through a [[GLARE Relay]].

## Inputs & Outputs

- **GLARE link:** one (`MAX_LINK_COUNT = 1`).
- **Lux:** allocates 1 Lux from the network (`getAllocatedLux()` → 1).
- **Kinetic output (intended):** rotational Stress/RPM supplied to connected Create kinetics when the network is online and powered.

## Operation

By design, when the receiver is online and the network can satisfy its 1-Lux allocation, it should output Create Stress/RPM to drive kinetic machinery; if the network overloads, its operation status flips and output should cease until the network is restored and a player toggles the receiver back online.

> [!note] Implementation
> In the current code the Kinetic Receiver is a **functional no-op**. `GlareKineticReceiverBlockEntity` extends the plain `GlareNodeBlockEntity` and only implements `IGlareReceiver` (allocating 1 Lux and tracking an operation status). It does **not** extend any kinetic block entity and produces **no kinetic output whatsoever** — the Stress/RPM generation described above is not yet implemented. Functionally it is presently just a 1-Lux receiver node that occupies network capacity.

## Rendering

Standard node block model. Goggle tooltip (inherited from `GlareNodeBlockEntity`) reports link count, network Lux allocation, online/overloaded status, receiver operation status, and colour-charge summary.

## Implementation

- **Block:** `GlareNodeBlock` (constructed with the `GlareKineticReceiverBlockEntity::new` factory).
- **Block entity:** `GlareKineticReceiverBlockEntity` (extends `GlareNodeBlockEntity`, implements `IGlareReceiver`).
- **Registry IDs:** block/item `resourceful_refinement:glare_kinetic_receiver`; block entity type `ModBlockEntities.GLARE_KINETIC_RECEIVER_BE`.
- **Constants:** `MAX_LINK_COUNT = 1`; `getAllocatedLux()` returns 1.
- **State:** a `GlareOperationStatus` (`ONLINE` by default), persisted under the `Status` NBT key, toggled/synced via `IGlareReceiver`.
- **Not implemented:** no kinetic block entity base, no Stress/RPM output.

## Related

- [[GLARE Networks]]
- [[GLARE Emitter Dish]]
- [[GLARE Relay]]
