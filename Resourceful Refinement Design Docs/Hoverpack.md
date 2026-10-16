---
title: Hoverpack
category: Item
status: Implemented
introduced: v0.4
recipe_type: n/a
related:
  - "[[Prism Cell]]"
tags:
  - item
---
The Hoverpack is a Prism Cell powered alternative to the elytra, which combines the elytra's glide ability with the launch propulsion of rocket fireworks. Its launch capabilities are powered by consuming charge from Prism Cells, wither high grade cells resulting in more effective thrust.

**ID:** *hoverpack*


## Acquisition

Crafted via Resourceful Refinement machines and Create/Vanilla crafting. The Hoverpack is obtainable after the player constructs their first chorus crystal shard drill (post-enderdragon) and begins manufacturing Prism Cells.

## Usage

The Hoverpack functions like an improved elytra, providing additional armor and launch capabilities. Its purpose is to aide navigation of the outer end while players expand resource collection in that dimension.


## State & Data

 The Hoverpack implements `IPrismCellItemConsumer` in order to store its Prism Cell. 
 
 Like most equipment items, it has durability. A Hoverpack has a maximum durability of 512, but does not loose durability while in flight. It is repairable using durasteel sheets.
 
The Hoverpack uses an adapted custom model, based on the vanilla elytra.


## Behaviour

It occupies the player's chestplate slot, and provides 6 points of armor while doing so. Like an Elytra, tapping the Jump key while mid-air causes the hoverpack's wings to open and the user to glide, and tapping again dismisses them.

Players can right-click to insert/remove a Prism Cell into the hoverpack. While a hoverpack has a cell with charge, tapping the Launch key while initially on the ground launches the player upwards with a significant vertical impulse, and a minor forward impulse, and automatically engages the hoverpack's glide, just as if the player had used a firework-rocket. Pressing the Launch key while in flight propels the player forward like a vanilla firework rocket would.

The launch vector for the hoverpack when launching from ground is more vertically aligned than a vanilla firework rocket, but is otherwise similar when in flight. The Launch key is a custom key-binding added by this mod.

Each launch consumes 250 charge from the stored Prism Cell. The strength of the launch also increases with cell grade, with Mid and High grade cells each increasing the launch thrust by 25% cumulatively.


## Implementation

Implemented in v0.4 under the new `content/hoverpack/` package.

- **Item:** `HoverpackItem` (`hoverpack`, registered in `ModItems`) — `extends Item implements Equipable,
  IPrismCellItemConsumer`. 512 durability, repairable with `durasteel_sheet` (`isValidRepairItem`), and grants
  **+6 Armor** on the chest slot via `ItemAttributeModifiers` (`createAttributes`). Right-click equips to the chest
  slot (`Equipable#swapWithEquipmentSlot`).
- **Gliding:** wired through NeoForge's `canElytraFly` / `elytraFlightTick` hooks (not the vanilla hardcoded elytra
  check), so the custom item flies. Glide is free — no durability is spent in flight; the `ELYTRA_GLIDE` game event
  is still emitted. Durability is only lost to normal armor damage-on-hit.
- **Cell storage:** the new **`IPrismCellItemConsumer`** interface (in `content/prism_cell/`) stores the installed
  cell directly on the stack as the existing `prism_cell_battery` data component (present = loaded). Exchange is
  bundle-style: right-click a Prism Cell (on the cursor) onto the Hoverpack in a slot to install it, or right-click
  the Hoverpack (on the cursor) onto an empty slot to pop the cell back out (`overrideOtherStackedOnMe` /
  `overrideStackedOnOther`).
- **Launch:** a new **Launch** keybinding (default **R**, `HoverpackClientSetup`) sends `HoverpackLaunchPayload`
  (C2S) when worn; `HoverpackLaunchHandler` validates charge and applies the impulse. A ground launch gives a strong
  vertical + minor forward impulse and auto-engages glide; an in-air launch gives a firework-rocket-style forward
  boost. Each launch drains `launch_charge_cost` (default **250**); a drain that empties a non-creative cell
  destroys it. Thrust scales by grade via `grade_thrust_step` (default 0.25 → Low ×1, Mid ×1.25, High/Creative
  ×1.5625). All launch tuning lives in `ServerConfig` under the `Hoverpack` section.
- **Rendering:** `HoverpackWingsLayer` subclasses the (NeoForge-patched, overridable) vanilla `ElytraLayer`, added
  to the player renderers via `EntityRenderersEvent.AddLayers`. It reuses the vanilla elytra geometry and swaps in
  the Hoverpack texture. The in-inventory icon uses a flat `item/generated` model.

### Not yet implemented
- **Crafting recipe** — ingredients are unspecified in this doc; the Hoverpack is currently creative-tab only.
- **Final art** — `textures/item/hoverpack.png` and `textures/entity/hoverpack.png` are solid-colour placeholders.
- No JEI/Ponder entries yet.


## Related

- [[Resource Resonator]] — first block to consume Prism Cells
- [[Codebase Overview]]