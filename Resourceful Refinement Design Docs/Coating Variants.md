---
title: Coating Variants
category: Framework
status: Partial
introduced: v0.2
recipe_type: resourceful_refinement:coating
related:
  - "[[Coating]]"
tags:
  - framework
  - reference
  - tool
  - data-component
---

This is the reference table of every [[Coating]] variant defined by the `CoatingType` enum. Each variant defines a display name, an associated colour (used to tint the coating integrity bar), a maximum durability (coating integrity), and an effect that applies while the coated tool is held or used. Colours and durabilities below are taken from `content/coating/CoatingType.java` (the code is authoritative); effect descriptions preserve the original design intent.

## Active Variants

### Obsidianite
- **Colour:** `#9532A8`
- **Durability:** 160
- **Effect:** While held, grants the user +2 armour.
- **Status:** Implemented (attribute modifier in `ModToolEvents.onAttribute`).

### Quicksilver
- **Colour:** `#C8E3E0`
- **Durability:** 128
- **Effect:** While held, grants the user the Haste II effect.
- **Status:** Implemented (`ModToolEvents.handleTickEffect`, `DIG_SPEED` amplifier 1).

### Durasteel
- **Colour:** `#286152`
- **Durability:** 320
- **Effect:** While held, increases the user's knockback resistance by 25%.
- **Status:** Implemented (attribute modifier, `+0.25` knockback resistance).

### Liquid Luck
- **Colour:** `#E8BD31`
- **Durability:** 96
- **Effect:** Affects the tool as if it had Fortune 2 (or bumps tools with Fortune 2 or higher up one level).
- **Status:** Planned effect. The variant exists and can be applied, but no Fortune handler exists in `ModToolEvents` yet, so the effect is not currently wired up.

### Uplift
- **Colour:** `#E065AF`
- **Durability:** 128
- **Effect:** While held, grants the user the Slow Fall effect.
- **Status:** Implemented (`ModToolEvents.handleTickEffect`, `SLOW_FALLING`).

### Conduction
- **Colour:** `#EB9B2D`
- **Durability:** 192
- **Effect:** When used (block break or attack), has a small chance (~4%) to spawn a lightning strike on a block near the target.
- **Status:** Implemented (`ModToolEvents.SpawnConductionLightning`).

### Gloopy
- **Colour:** `#B4CC58`
- **Durability:** 128
- **Effect:** "Get Glooped!" — when used (block break or attack), has a chance to spawn sticky gel splatter blocks around the target in a splatter pattern.
- **Status:** Implemented (`ModToolEvents.SpawnGloopSplatter`, places `GEL_SPLATTER_STICKY`).

## Planned / Disabled Variants

These entries exist in `CoatingType.java` but are currently commented out and cannot be produced or applied.

### Luminite
- **Colour:** `#FFFF00`
- **Durability:** 500
- **Effect:** Not yet defined.
- **Status:** Planned/disabled (commented out in the enum).

### Obsidian
- **Colour:** `#221133`
- **Durability:** 1000
- **Effect:** Not yet defined.
- **Status:** Planned/disabled (commented out in the enum).

> [!note] Implementation
> The code differs from earlier design notes in several places, and the values above follow the code:
> - **Obsidianite** durability is **160** in code (older notes said 128).
> - **Uplift** colour is **`#E065AF`** in code (older notes said `#db7bae`).
> - **Conduction** colour is **`#EB9B2D`** in code (older notes said `#cc7f27`).
> - **Gloopy** is a variant present in code but absent from earlier notes.
> - **Liquid Luck**'s effect is described but has no handler in `ModToolEvents`, so it is currently inert.
> - **Luminite** and **Obsidian** are commented out and therefore disabled.

## Related

- [[Coating]]
