---
title: GLARE Relay
category: Machine
status: Implemented
introduced: v0.4
recipe_type: n/a
related:
  - "[[GLARE Networks]]"
  - "[[GLARE Emitter Dish]]"
  - "[[Relay Wrench]]"
tags:
  - machine
  - glare
  - network
---

The GLARE Relay is the universal junction of a [[GLARE Networks|GLARE network]]. It produces no Lux and consumes none — it exists purely to connect other nodes together and extend a network's reach, supporting far more links than any functional node.

**ID:** *glare_relay*

## Gameplay Role

The Relay is the plumbing of GLARE. Emitters and receivers cap out at a single link each, so the Relay is what actually lets you build a branching network: it acts as a hub, fanning a single emitter out to many receivers or bridging line-of-sight gaps between distant clusters. If you want to split, merge, or route a network, you route it through Relays.

## Construction & Placement

A horizontal directional node block. Placed like any GLARE node, it orients to face the placer and can be linked with the [[Relay Wrench]] or by right-clicking with held GLARE node block-items before placement.

## Inputs & Outputs

- **GLARE links:** up to eight (`MAX_LINK_COUNT = 8`) — the highest link budget of any GLARE node.
- **Lux:** none produced, none allocated. The Relay is neither an emitter nor a receiver; it only relays membership and connectivity.

## Operation

The Relay has no kinetic, heat, or redstone behaviour of its own. It participates in the network graph as a plain node: it holds its links, reports its network membership, and passes through the network's Lux/colour/overload state for display. Right-clicking with an empty hand opens the GLARE power terminal; right-clicking with a [[Relay Wrench]] edits its links.

## Rendering

Standard node block model. Goggle tooltip (inherited from `GlareNodeBlockEntity`) reports link count out of eight and, when networked, the network's Lux allocation, online/overloaded status, and colour-charge summary.

## Implementation

- **Block:** `GlareNodeBlock` (the generic node block, constructed with the `GlareRelayBlockEntity::new` factory).
- **Block entity:** `GlareRelayBlockEntity` (extends `GlareNodeBlockEntity`, passing `MAX_LINK_COUNT = 8` as its max-link count). It adds no state of its own beyond the base node.
- **Registry IDs:** block/item `resourceful_refinement:glare_relay`; block entity type `ModBlockEntities.GLARE_RELAY_BE`.
- **Constants:** `MAX_LINK_COUNT = 8` (also `IGlareNode.getMaxGlareLinks()` → 8).
- Node lifecycle handled by the shared `GlareNodeBlockEntity` / `GlareService` machinery.

## Related

- [[GLARE Networks]]
- [[GLARE Emitter Dish]]
- [[Relay Wrench]]
