---
title: GLARE Chromatic Transceiver
category: Machine
status: Implemented
introduced: v0.4
recipe_type: n/a
related:
  - "[[GLARE Networks]]"
  - "[[GLARE Emitter Dish]]"
  - "[[GLARE  Lux Transceiver]]"
tags:
  - machine
  - glare
  - network
  - redstone
---

The GLARE Chromatic Transceiver is a redstone-output receiver node: it reads the aggregate colour charge of its [[GLARE Networks|GLARE network]] and emits a redstone signal when configurable per-colour thresholds are satisfied. It turns a network's colour state into a redstone condition, making it the network's comparator.

**ID:** *glare_chromatic_transceiver*

> Distinct from the [[GLARE  Lux Transceiver]], which pipes Lux into a machine's Lux-Socket face. The Chromatic Transceiver instead reads colour charge and outputs redstone.

## Gameplay Role

Emitters stamp a colour charge onto the network based on the stained glass used to tint them (see [[GLARE Emitter Dish]]). The Chromatic Transceiver lets you act on that colour bookkeeping: set thresholds and comparisons per colour, combine them with a logic mode, and drive redstone from whether the network currently satisfies your rule. It is the primary way GLARE colour state feeds back into vanilla/Create redstone automation.

## Construction & Placement

A horizontal directional node block (extends [[GLARE Relay|the generic GLARE node block]]). It is a **redstone source** on all sides — `isSignalSource` is true — and supports a single GLARE link (`MAX_LINK_COUNT = 1`). Configure it by right-clicking with an empty hand, which opens its menu/screen.

## Inputs & Outputs

- **GLARE link:** one (`MAX_LINK_COUNT = 1`).
- **Lux:** allocates **0** Lux (`getAllocatedLux()` → 0) — it reads network state without drawing power.
- **Redstone output:** signal strength **15** on every side when its filter condition is met, otherwise 0.

## Operation

The transceiver holds a per-colour configuration: for each of the 16 dye colours it stores a **threshold** and a **comparison operator**, plus a single network-wide **logic mode**.

- **Thresholds:** `DISABLED_FILTER = -1` means that colour is ignored. Enabled thresholds are clamped to `0 … MAX_THRESHOLD = 1_000_000`.
- **Comparisons** (`GlareComparison`): `≥` (`GREATER_THAN_OR_EQUAL`, default), `≤` (`LESS_THAN_OR_EQUAL`), `>` (`GREATER_THAN`), `<` (`LESS_THAN`), `=` (`EQUAL`), `≠` (`NOT_EQUAL`). Each enabled colour tests its network colour charge against its threshold with its comparison.
- **Logic mode** (`GlareLogicMode`) combines the enabled per-colour results: `AND` (all enabled colours match), `OR` (at least one matches), `XOR` (exactly one matches).

Output powers on only if at least one colour filter is enabled **and** the logic-mode combination of the enabled results is satisfied. Whenever the network changes (membership, links, colour charge) or the configuration is reapplied, the output is recomputed; on a change it updates neighbours at its own position and below, and syncs to the client.

## Recipes

Not a processing machine — no recipe type.

## Rendering

Configured through a dedicated menu and screen (`GlareChromaticTransceiverMenu` / `GlareChromaticTransceiverScreen`). The screen exposes per-colour threshold entry, per-colour comparison selection, and the AND/OR/XOR logic-mode toggle. Goggle tooltip is inherited from `GlareNodeBlockEntity`.

## Implementation

- **Block:** `GlareChromaticTransceiverBlock` (extends `GlareNodeBlock`; `isSignalSource` → true; `getSignal` → 15 when `isOutputPowered()`).
- **Block entity:** `GlareChromaticTransceiverBlockEntity` (extends `GlareNodeBlockEntity`, implements `IGlareReceiver` and `MenuProvider`).
- **Menu / Screen:** `GlareChromaticTransceiverMenu`, `GlareChromaticTransceiverScreen`.
- **Config payload:** `configure_glare_transceiver` (`ConfigureGlareTransceiverPayload`, registered in `ModNetworking`), carrying the logic mode, per-colour thresholds, and per-colour comparison ordinals; applied via `applyConfiguration`.
- **Registry IDs:** block/item `resourceful_refinement:glare_chromatic_transceiver`; block entity type `ModBlockEntities.GLARE_CHROMATIC_TRANSCEIVER_BE`; menu type registered under the same path in `ModMenus`.
- **Constants:** `MAX_LINK_COUNT = 1`, `DISABLED_FILTER = -1`, `MAX_THRESHOLD = 1_000_000`; default comparison `GREATER_THAN_OR_EQUAL`, default logic mode `AND`.
- **State:** `thresholds[]`, `comparisons[]`, `logicMode`, `status` (`GlareOperationStatus`), and `outputPowered`, all persisted (NBT keys `ColourThresholds`, `ColourComparisons`, `LogicMode`, `Status`, `OutputPowered`).
- **Enums:** `GlareComparison`, `GlareLogicMode`.

## Related

- [[GLARE Networks]]
- [[GLARE Emitter Dish]]
- [[GLARE  Lux Transceiver]]
