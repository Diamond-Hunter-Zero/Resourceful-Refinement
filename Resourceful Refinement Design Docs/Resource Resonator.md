---
title: Resource Resonator
category: Machine
status: Planned
introduced: v0.4
recipe_type: n/a
related:
  - "[[Geyser Block]]"
  - "[[Mineral Deposit]]"
  - "[[Crystal Fissure Bud]]"
  - "[[Codebase Overview]]"
tags:
  - machine
  - tool
  - worldgen
  - gui
  - planned
---

The **Resource Resonator** is a placed block-entity scanning tool. It analyses the surrounding world for
generation features — geysers, mineral deposits, crystal fissures — and plots their horizontal (X/Z)
positions on a radar-style GUI, so players can locate resource nodes before digging them out.

**ID:** `resource_resonator`

## Gameplay Role

A prospecting aid for the v0.4 extraction loop. Rather than wandering to find [[Geyser Block]] deposits,
[[Mineral Deposit]] surface nodes, or [[Crystal Fissure Bud]] drill sites, the player places a Resonator
and scans for them, reading off coordinates from a radar screen.

The Resource Resonator interface allows users to filter for deposit types (surface geyser, cave geyser, mineral, crystal fissure), only showing map pins for those enabled by the current GUI filter.

## Construction & Placement

A horizontally directional block. Placed down, then interacted with by right-clicking to open its GUI.

## Operation

- Right-clicking opens the GUI: a circular radar-style screen, with a control panel on the right for
  choosing which world-gen feature type(s) to scan for and a **Scan** button.
- A scan analyses every chunk within a configurable chunk distance (server config) and locates the spawn
  positions of all features matching the current filter. The scan is **seed-based**, so it can assess
  chunks that have not generated yet without loading or generating them.
- Results are cached locally in the Resonator (persistence across sessions is not required) and drawn on
  the radar as per-type icons. Hovering a point shows its block coordinates and type. Each scan refreshes
  the cache and the display.

Refer to the GUI mock-up below for functionality layout:
![[Resource Resonator GUI.png|557]]

## Rendering

Intended to use a `BlockEntityRenderer` with a modded Java entity model for its visuals and animation.

## Implementation

Not yet implemented — design target for v0.4. When built, expect: a directional block + block entity
(scan cache + filter state), a menu/screen pair for the radar GUI, a seed-based chunk-scanning service
(likely reusing the worldgen structure/feature lookup used by [[Nether Geyser Worldgen]]), a server-config
chunk-radius key, and a BER + model.

## Related

- [[Geyser Block]] · [[Mineral Deposit]] · [[Crystal Fissure Bud]]
- [[Codebase Overview]]
