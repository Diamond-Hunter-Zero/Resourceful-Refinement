---
title: Relay Wrench
category: Tool
status: Implemented
introduced: v0.4
recipe_type: n/a
related:
  - "[[GLARE Networks]]"
  - "[[GLARE Relay]]"
  - "[[GLARE Emitter Dish]]"
tags:
  - tool
  - glare
  - network
---

The Relay Wrench is the link editor for [[GLARE Networks|GLARE networks]]. It is a two-click tool for creating links between GLARE nodes and, with a crouch-click, for bulk-unlinking a node. It complements the placement-target workflow carried by GLARE node block-items themselves.

**ID:** *relay_wrench*

## Acquisition

Obtained as a standard item (stacks to 1). No processing recipe is associated with it here.

## Usage

The wrench works as a two-click link editor between GLARE nodes:

1. **First click** on a GLARE node **selects** it as the pending link endpoint. The selection is stored on the held stack in the `relay_wrench_target` data component and the selected node is drawn with a **yellow outline** client-side. Selecting is refused (with an "at limit" message) if that node cannot accept another link.
2. **Second click** on a different node **links** the two, then clears the selection. Clicking the same node again cancels the selection; a crouch-use in the air also clears a pending selection.

**Shift-click (crouch) on a node bulk-unlinks it** — `GlareService.removeAllLinks` removes every link attached to that node, reporting how many were removed. (Crouch-clicking the currently selected node instead just clears the selection.)

Link attempts surface feedback: connected, already-linked, target-missing, or a specific failure reason from `GlareSavedData.LinkResult`.

Separately, **held GLARE node block-items** (e.g. [[GLARE Relay]], [[GLARE Emitter Dish]]) carry their own placement-target list — up to **8** targets in the `glare_targets` data component (`GlareTargetsData`, `MAX_TARGETS = 8`). Those targets are drawn as **blue outlines** and are linked automatically when the node is placed; crouch-using such an item clears its targets. The block-item shows an enchantment-glint (foil) while it holds targets.

## State & Data

- **`relay_wrench_target`** (`RELAY_WRENCH_TARGET`): a `DimensionalNodePos` stored on the wrench stack holding the currently selected endpoint. Shown in the wrench tooltip when present.
- **`glare_targets`** (`GLARE_TARGETS`): a `GlareTargetsData` record on GLARE node block-items, a distinct/de-duplicated list capped at `MAX_TARGETS = 8` placement targets.

## Behaviour

- Selecting, linking, cancelling, at-limit, and unlink outcomes each show an action-bar message.
- Link creation goes through `GlareService.tryLink`; if either endpoint cannot accept a link the wrench reports "at limit". Node registration is ensured (`ensureLiveNodeRegistered`) before operations.
- Client-side outlines are rendered every client tick by `RelayWrenchClientHandler`: wrench selection in yellow (`0xF2C94C`), block-item placement targets in blue (`0x70A8E8`), using Create/Catnip's `Outliner`.

## Implementation

- **Item:** `RelayWrenchItem` (extends `Item`, `stacksTo(1)`). Link logic in `interactWithNode`; air-use crouch-clear in `use`; tooltip in `appendHoverText`.
- **Client renderer:** `RelayWrenchClientHandler` (`@EventBusSubscriber`, `Dist.CLIENT`) draws selection and placement-target outlines each `ClientTickEvent.Post`.
- **Block-item:** `GlareNodeBlockItem` stores/uses `GlareTargetsData`; links targets on `place`.
- **Data components:** `ModDataComponents.RELAY_WRENCH_TARGET` (`resourceful_refinement:relay_wrench_target`) and `ModDataComponents.GLARE_TARGETS` (`resourceful_refinement:glare_targets`).
- **Registry ID:** item `resourceful_refinement:relay_wrench` (`ModItems.RELAY_WRENCH`).
- **Service:** `GlareService` (`tryLink`, `removeAllLinks`, `canAcceptLink`, `tryAddTarget`, `clearTargets`, `ensureLiveNodeRegistered`).

## Related

- [[GLARE Networks]]
- [[GLARE Relay]]
- [[GLARE Emitter Dish]]
