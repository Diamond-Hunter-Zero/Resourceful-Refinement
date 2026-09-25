---
title: Milking Station
category: Machine
status: Implemented
introduced: v0.3
recipe_type: resourceful_refinement:milking_station
related:
  - "[[Codebase Overview]]"
  - "[[Brewer's Tap]]"
  - "[[Primary Design Doc]]"
tags:
  - machine
  - kinetic
  - fluid
  - entity
---

The Milking Station is a kinetic production block that farms outputs from a captured mob at regular intervals. A player holding a leashed mob right-clicks the station with a lead to secure that mob above it; once the station holds a mob and is given rotational input it slowly produces mob-specific outputs — milk from cows, mushroom stew from mooshrooms, blaze powder from blazes, and so on. A held mob loses its AI but keeps its NBT, and can be released by right-clicking the station with an empty lead. Right-clicking an empty station with an empty hand instead seats the player for a "self-milking" interaction.

## Gameplay Role

The Milking Station automates mob drops that are normally hand-farmed, turning them into a steady, kinetic-powered trickle. It is the intended source of bulk milk for the [[Brewer's Tap]] drink chain, and of assorted mob materials (gunpowder, blaze powder, ghast tears, ink) for wider crafting. Because the mob is stored with its full NBT it can be captured, milked and released without losing its identity, name or state.

## Construction & Placement

- Single block. `MilkingStationBlock` is a Create `KineticBlock` implementing `IBE<MilkingStationBlockEntity>`, with a horizontal `FACING` state set from the placer's look direction.
- **Kinetic input is taken from below:** the rotation axis is `Y` and `hasShaftTowards` returns true only for `Direction.DOWN`, so a shaft must drive the station from underneath.
- The render shape is `INVISIBLE` (the block is drawn entirely by its BER). Its collision/interaction shape is a solid base (16×13×16) with a smaller 12×3×12 nub on top where the mob sits.
- On removal the station releases any captured mob, clears a seated player, and discards its seat entity.

## Inputs & Outputs

- **Captured mob:** stored as `capturedEntityType` + `capturedEntityData` (full saved NBT). Only one mob (or one seated player) at a time.
- **Item output:** `outputInv`, an `ItemStackHandler(4)`. Exposed to automation through `outputItemHandler`, an **extract-only** wrapper (insertion is rejected).
- **Fluid output:** `outputTank`, a `FluidTank` of `OUTPUT_TANK_CAPACITY = 4000` mB. Exposed through `outputFluidHandler`, likewise **drain-only**.
- **Kinetic input:** shaft on the bottom face.

## Operation

**Capturing a mob.** Right-click the station with a `minecraft:lead`: it finds the nearest mob within an 8-block radius that is leashed *to the interacting player* (or a capturable mob riding a leashed boat), stores its NBT via `saveWithoutId`, drops the lead, and discards the live entity. The stored mob renders as a no-AI preview sitting above the station.

**Releasing a mob.** Right-click a station that is holding a mob with a lead: the mob is re-spawned above the station with a fresh UUID, facing the station's front, and re-leashed to the player.

**Player self-milking.** Right-click an empty station (empty main and off hand, no captured mob, no seated player): the player is seated on a `MilkingStationSeatEntity`, turned to face out of the station, and registered as the "seated player". While seated the station runs the `minecraft:player` recipe. The player dismounts (crouch) to stop; the seat is validated each tick and cleared if the player leaves.

**Processing.** Each server tick, if the station holds an active recipe entity (captured mob, or the player when seated) *and* has rotational input (`getSpeed() != 0`):
1. It builds a `MilkingStationRecipeInput` from the active entity type and resolves the matching `milking_station` recipe.
2. On a match it sets `timer` to the recipe's processing duration and counts down.
3. When the timer hits zero, `process()` fills the output tank with the recipe's fluid results and inserts its item results, provided there is room (`canProcess` simulates first); then the timer resets to run again.

The goggle tooltip shows the stress draw at current speed, what the station is holding, and the seconds remaining on the current cycle.

## Recipes

- **Type id:** `resourceful_refinement:milking_station`
- `MilkingStationRecipe` extends Create's `StandardProcessingRecipe`, keyed by a single `entity` `ResourceLocation` and matched purely on the captured entity type. It takes **no** item or fluid inputs, and produces at most one item output **or** one fluid output; it may specify its own duration (`processing_time`, in ticks).

Sample (`recipe/milking_station/cow_milk.json`):

```json
{
  "type": "resourceful_refinement:milking_station",
  "entity": "minecraft:cow",
  "processing_time": 1800,
  "ingredients": [],
  "results": [
    { "id": "minecraft:milk", "amount": 250 }
  ]
}
```

**Shipped mob → output table** (`data/resourceful_refinement/recipe/milking_station/`):

| Mob | Output | Amount | Time (ticks) |
|---|---|---|---|
| `minecraft:cow` | milk (fluid) | 250 mB | 1800 |
| `minecraft:goat` | milk (fluid) | 250 mB | 1800 |
| `minecraft:mooshroom` | mushroom stew | 1 | 4800 |
| `minecraft:blaze` | blaze powder | 1 | 2400 |
| `minecraft:creeper` | gunpowder | 1 | 600 |
| `minecraft:squid` | ink sac | 1 | 1200 |
| `minecraft:glow_squid` | glow ink sac | 1 | 2400 |
| `minecraft:ghast` | ghast tear | 1 | 9600 |
| `minecraft:snow_golem` | snowball | 1 | 200 |
| `minecraft:player` | milk (fluid) | 25 mB | 400 |

> [!note] Implementation
> The design doc lists cow, mooshroom (incl. brown), blaze, creeper, squid, glow squid, ghast and player. The shipped recipes additionally include **goat → milk** and **snow golem → snowball**, and there is no separate brown-mooshroom recipe (one `minecraft:mooshroom` entry covers both). Fluid amounts differ (cow/goat 250 mB, player 25 mB). Processing scales with time only: `getProcessingSpeed()` exists to scale progress with rotation speed, but that line is commented out in `tick()`, so the timer currently decrements a flat 1 per tick regardless of speed — any non-zero speed runs the cycle at the same rate.

## Rendering

- `MilkingStationRenderer` (BER) with a baked `MilkingStationModel` draws the station body, and renders the captured mob (or a preview) posed above it with its AI disabled.
- `MilkingStationSeatRenderer` handles the invisible seat entity used for player self-milking.
- `MilkingStationItemRenderer` renders the block as a held/inventory item.
- `MilkingStationRecipeCategory` provides the JEI category (catalyst: the Milking Station item).

## Implementation

- **Package:** `content/milking_station/` (recipes under `content/milking_station/recipe/`)
- **Block:** `MilkingStationBlock` — `KineticBlock`, `IBE<MilkingStationBlockEntity>`; state `FACING` (horizontal); rotation axis `Y`, shaft towards `DOWN`; render shape `INVISIBLE`.
- **Block entity:** `MilkingStationBlockEntity extends KineticBlockEntity`; holds `outputInv` (`ItemStackHandler(4)`), `outputTank` (`FluidTank(4000)`), the extract-only `outputItemHandler` / `outputFluidHandler`, the captured-entity NBT, `timer`, and `seatedPlayerId`.
- **Seat entity:** `MilkingStationSeatEntity` — invisible, no-gravity, no-save rider used for player self-milking; auto-discards when unmounted or when its block is gone.
- **Recipe:** `MilkingStationRecipe` (extends `StandardProcessingRecipe`, inner `Serializer`), `MilkingStationRecipeInput` (wraps the entity type), `MilkingStationRecipeCategory` (JEI).
- **Renderer / model:** `MilkingStationRenderer`, `MilkingStationModel`, `MilkingStationSeatRenderer`, `MilkingStationItemRenderer`.
- **Registry IDs:** block `ModBlocks.MILKING_STATION` (`milking_station`), item `ModItems.MILKING_STATION_ITEM`, block entity `ModBlockEntities.MILKING_STATION_BE`, seat entity `ModEntities.MILKING_STATION_SEAT` (`milking_station_seat`, `noSave`), recipe type/serializer `ModRecipeTypes.MILKING_STATION_TYPE` / `MILKING_STATION_SERIALIZER` (`MILKING_STATION_TYPE_INFO`).
- **Stress:** impact `ModStressValues.MILKING_STATION_STRESS = 4` SU.
- **Constants:** `OUTPUT_TANK_CAPACITY = 4000`; mob search radius 8 blocks.

## Related

- [[Codebase Overview]] — where this feature sits in the registry/content map.
- [[Brewer's Tap]] — primary consumer of the milk this station produces.
- [[Primary Design Doc]] — v0.3 Milking Station design intent.
