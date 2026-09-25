---
title: Gel Splatter
category: Fluid
status: Implemented
introduced: v0.2
recipe_type: n/a
related:
  - "[[Hosegun]]"
  - "[[Paint Fluids]]"
  - "[[Gel Tracking]]"
tags:
  - gel
  - fluid
  - decorative
---

Gel Splatters are multi-face blocks (configurable interior faces like glow lichen or amber) that represent in-world layers of fluid, placed down by the [[Hosegun]] and [[Paint Nozzle]]. They are heavily inspired by the gels from Portal 2 and the paints from Splatoon. Each splatter is linked to a fluid, which drives its tint colour and physical properties. Almost any fluid except water and potions can be turned into a Gel Splatter.

**ID:** `gel_splatter` (plus the sticky / slippery / molten / bouncy variants below)

## Shape & Placement

Gel Splatters are placed by firing [[Hosegun]] or [[Paint Nozzle]] gel-blobs. When a gel-blob impacts a solid block face that borders an empty or replaceable space, it attempts to create a splatter in that space. The new splatter automatically populates any valid faces adjacent to solid blocks with the corresponding region of its model, and gel spread flood-fills nearby reachable positions within the small impact radius.

- If a splatter already exists in the impacted space and is the **same** fluid type, it repopulates all valid faces.
- If a splatter already exists and is a **different** fluid type, it replaces the stored fluid (swapping to the correct block variant) and repopulates all valid faces.
- Like other multi-face blocks, individual faces can be broken by hand.
- Water/cleanse gel-blobs do not create splatters; they clear existing splatters within a 3-block radius, letting players "clean up" gels.

Impact radii come from `GelImpactConstants`: paint and cleanse blobs use a large radius of `3.0`, all other blobs use `1.5`.

## Gel Types

Every fluid is associated with a gel type, which determines the splatter's properties — physical characteristics (slipperiness, stickiness, bounciness) and/or tick effects applied while a living entity collides with the splatter (status effects, contact damage). Custom mod fluids are assigned their gel type at registration; all other fluids (vanilla lava, Create fluids, other mods) are mapped at runtime from fluid tags. Fluids listed under a `doesNotMakeGel`-style exclusion do not make gels (like water), and any untagged fluid defaults to `Inert`.

The originally-specified gel types and their intended fluid assignments:

| **Gel Type ID** | **Properties** | **Creates Gel Splatter?** | **Tag ID** | **Registered Fluids** |
| --- | --- | --- | --- | --- |
| `Molten` | - Deals contact damage (like magma blocks)<br>- Deals damage to entities its gel-blobs collide with | Yes | `makesMoltenGel` | molten_crimsite, molten_veridium, molten_ochrum, molten_asurine, molten_scorchia, lava |
| `Speedy` | - Slippery<br>- Applies Speed 1 | Yes | `makesSpeedyGel` | catalysed_iron, catalysed_copper, catalysed_gold, catalysed_zinc, catalysed_redstone, catalysed_sparkpowder, catalysed_carborax, builders_tea |
| `Gooey` | - Sticky | Yes | `makesGooeyGel` | honey, chocolate, carborax_diesel |
| `Bouncy` | - High bounciness<br>- Gel-blob applies moderate knockback on impacting entities | Yes | `makesBouncyGel` | durasteel_alloy, purified_durasteel, overcharged_carborax |
| `Cursed` | - Applies Slowness 2<br>- Applies Weakness 1 | Yes | `makesCursedGel` | unrefined_carborax |
| `Blessed` | - Applies Strength 1 | Yes | `makesBlessedGel` | |
| `Inert` | - No effect | Yes | `makesInertGel` | silica_substrate, purified_iron, purified_copper, purified_gold, purified_zinc |
| `Cleanse` | - Removes gel splatters | No | `makesCleanseGel` | water |
| `Potion` | - Gel-blob applies potion effect on impacting entity | No | `makesPotionGel` | *Any potion fluids* |
| `Paint` | - Gel-blob dyes entities on impact (sheep wool, dog & cat collars)<br>- Gel-blob dyes coloured blocks on impact | No | `makesPaintGel` | *Any paint fluids* |

Two further gel types exist in code beyond the original list:

- **Frozen** (`makes_frozen_gel`): the gel-blob places terrain around the impact rather than a splatter — ice into source water, powder snow into flowing water, deepslate into source lava, and a snow layer on sturdy ground.
- **Concrete** (`makes_concrete_gel`): the gel-blob places light grey concrete into replaceable positions around the impact.

> [!note] Implementation
> The tag IDs actually registered use snake_case resource paths — `makes_molten_gel`, `makes_speedy_gel`, `makes_gooey_gel`, `makes_bouncy_gel`, `makes_cursed_gel`, `makes_blessed_gel`, `makes_inert_gel`, `makes_cleanse_gel`, `makes_potion_gel`, `makes_paint_gel`, `makes_frozen_gel`, `makes_concrete_gel` — not the camelCase names in the table above. Mapping is rebuilt on tag reload (`TagsUpdatedEvent`); water and flowing_water are hardcoded to `Cleanse`. Untagged fluids fall through to `Inert`, and any fluid whose id path contains "potion" resolves to `Potion`.

> [!note] Implementation — likely bug
> In `GelType.java` both `FROZEN` and `CONCRETE` are constructed with the **same** id string `"frozen"` (lines ~17–18):
> ```java
> FROZEN("frozen", ... "makes_frozen_gel"),
> CONCRETE("frozen", ... "makes_concrete_gel");
> ```
> Their tag locations differ (`makes_frozen_gel` vs `makes_concrete_gel`), so tag-driven resolution still distinguishes them, but `CONCRETE.getId()` returns `"frozen"`. Any code path relying on `getId()` to tell the two apart (e.g. serialisation, model/state keys) will conflate concrete with frozen. This should almost certainly read `CONCRETE("concrete", ...)` — verify and fix.

## Behaviour

Physical and status effects are applied from `GelSplatterBlock`:

- **Molten:** deals `molten_gel_damage` (`ModDamageTypes.moltenGel`, 2.0) to living entities inside it; emits light level 10; spawns lava particles.
- **Speedy:** friction raised to `0.96` (slippery); grants Movement Speed (amplifier 4 for players, 2 for other living entities), and accelerates non-living entities that are already moving; emits light level 4.
- **Gooey:** sticky variant (dedicated block).
- **Bouncy:** bounces entities that land on the thin gel geometry, with a launch bonus when the entity carries speed-gel momentum; players get a short speed boost and slime-block audio; emits light level 4.
- **Cursed:** applies Slowness and Weakness.
- **Blessed:** applies Strength (Damage Boost).

Molten gel-blobs also damage entities they strike in flight, and bouncy blobs knock entities back on impact (see [[Hosegun]]).

## Block Variants

Five physical block variants share a single block entity (`GEL_SPLATTER_BE` / `GelSplatterBlockEntity`); the variant is chosen from the stored fluid's gel type, preserving multiface attachments when switching:

| Gel type | Block | Default fluid when empty |
|---|---|---|
| Gooey | `gel_splatter_sticky` | Liquid Glue |
| Speedy | `gel_splatter_slippery` | Catalysed Copper |
| Molten | `gel_splatter_molten` | Molten Crimsite |
| Bouncy | `gel_splatter_bouncy` | Durasteel Alloy |
| All others (inert, cursed, blessed, paint, cleanse, potion, frozen, concrete) | `gel_splatter` | Molten Andesite Blend |

## Sources & Uses

- **Source:** fired from the [[Hosegun]] and [[Paint Nozzle]] as gel-blobs, then placed on impact. Creative players can also right-click an existing splatter with a fluid-containing item to retexture it (`creativeRetextureWithFluid`); cleanse fluids remove it, potions are rejected.
- **Uses:** parkour/movement gels (speedy, bouncy, gooey), hazards (molten, cursed), buffs (blessed), decoration and dyeing (paint), and cleanup (cleanse). Placed splatters can be registered to the [[Gel Tracking]] network via a bound Hosegun for remote counting/purging.

## Rendering

The block uses a custom Java block model instead of the standard 6-face cube; each face element is named after its parent face (`north`, `south`, `east`, …). Face tint is driven per-block from the stored fluid (`GelFluidTintColors` / `GelFluidTintColorsClient`, `RegisterRendererTint`). A `fluid_update_index` blockstate property (0–7) is bumped on fluid change to force client tint re-sync. The block propagates skylight and uses full shade brightness to avoid harsh internal face shadows.

## Implementation

- **Block:** `content/gel_splatter/GelSplatterBlock` (extends `MultifaceBlock implements EntityBlock`; `fluid_update_index` property; friction, `entityInside` effects, bounce logic, light emission, creative retexture).
- **Block entity:** `GelSplatterBlockEntity` (stores fluid + tracking ID; variant/tint sync; tracking hooks on load/remove) implementing `GelSplatterBlockEntityAccess`.
- **Variant helpers:** `GelSplatterBlocks` (`getBlockForGelType`, `withVariantForFluid`).
- **Gel type + mapping:** `GelType` enum, `GelPropertiesManager` (tag-driven, reload-aware, ammo-cost constants).
- **Impact geometry:** `GelImpactConstants` (radii, sphere test).
- **Tint:** `GelFluidTintColors` / `GelFluidTintColorsClient`.
- **Tracking:** integrates with [[Gel Tracking]] (`GelTrackingService`).

## Related

- [[Hosegun]]
- [[Paint Nozzle]]
- [[Paint Fluids]]
- [[Gel Tracking]]
