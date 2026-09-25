---
title: Nether Geyser Worldgen
category: Worldgen
status: Implemented
introduced: v0.1
recipe_type: n/a
related:
  - "[[Geyser Block]]"
  - "[[Fracking Pump]]"
tags:
  - worldgen
  - multiblock
---

Geyser structures generate across the overworld surface, overworld caves, and the Nether. The Nether case is the hard one: jigsaw structures there cannot use the `WORLD_SURFACE_WG` heightmap, because in the Nether that heightmap resolves to the column *ceiling*, not the walkable cave floor. This page documents how geyser structures are anchored to the correct surface — the custom `NetherSurfaceJigsawStructure` type, the per-template offset data loaded by `GeyserOffsetManager`, and the mixins that make vanilla jigsaw placement respect those offsets.

## Where It Generates

Geyser structures are placed via `minecraft:random_spread` structure sets during the `surface_structures` generation step, restricted to biomes tagged by `#resourceful_refinement:has_structure/*`. Shipped structures:

- **ore_geyser** — overworld surface. Anchored with `project_start_to_heightmap: WORLD_SURFACE_WG`, `max_distance_from_center: 80`.
- **cave_ore_geyser** — overworld caves. Uniform start height.
- **carbonox_geyser** — carbonox geyser variant.
- **nether_scorchia_geyser** — Nether, biomes `#resourceful_refinement:has_structure/nether_geyser`. `start_height` uniform (absolute 32 … below_top 16), `max_distance_from_center: 20`, `terrain_adaptation: beard_thin`. Spread: `spacing 7`, `separation 3`.
- **nether_scorchia_basalt_geyser** — Nether basalt variant (`has_structure/nether_basalt_geyser`).
- **nether_scorchia_ocean** — Nether lava-ocean variant (`has_structure/nether_basalt_ocean_geyser`).

## Structure / Feature

Each structure is a small jigsaw whose start pool places a geyser template piece. The design goal is that the [[Geyser Block]] sits flush with the natural ground — surface for overworld, cave floor for Nether — rather than floating or being buried. In the Nether, the floor is found by scanning the noise column downward from just below the ceiling for the highest solid block that has open (air/liquid) space above it.

## Products

Each generated structure yields a [[Geyser Block]] (plus its surrounding template blocks). The geyser's stored fluid then drives its eruptions and gates which [[Fracking Pump]] recipe can run on it — see [[Fracking Source Blocks]].

## Implementation

### `GeyserOffsetManager` (`worldgen/GeyserOffsetManager`)

At mod init (`GeyserOffsetManager.init()`, called from `ResourcefulRefinementMain`), it reads the `start_pool.json` of each geyser template pool from the classpath and records each element's `bottom_offset`, storing it as a ground-level delta of `-bottom_offset`, keyed by `(poolId → templateId)`. Pools loaded:

- `cave_ore_geyser_template/start_pool.json`
- `nether_scorchia_geyser_template/start_pool.json`
- `ore_geyser_template/start_pool.json`
- `carbonox_geyser_template/start_pool.json`

It also keeps a `ThreadLocal<ResourceLocation>` for the currently-active start pool, set/cleared during placement (see the mixins below), and exposes `getOffset(poolId, templateId)`.

### `NetherSurfaceJigsawStructure` (`worldgen/structure/NetherSurfaceJigsawStructure`)

A custom `Structure` whose `findGenerationPoint` samples the base noise column at the chunk middle and scans **Y = 115 → 32** for the first position where the current block is air/liquid and the block below is solid — the cave floor. It falls back to **Y = 32** (lava sea level) if none is found, then delegates to `JigsawPlacement.addPieces` with the 1.21.1 signature (`PoolAliasLookup.EMPTY`, `DimensionPadding.ZERO`, `LiquidSettings.IGNORE_WATERLOGGING`). Registered as `ModStructureTypes.NETHER_SURFACE_JIGSAW` under the id **`nether_ground_jigsaw`** (`DeferredRegister<StructureType<?>>` on `Registries.STRUCTURE_TYPE`, registered via `ModRegistries`).

### Mixins

Registered in `mixins.resourceful_refinement.json` (package `com.resourceful_refinement.mixin`):

- **`JigsawPlacementMixin`** — injects at HEAD/RETURN of `JigsawPlacement.addPieces`. On entry it captures the start pool's id into `GeyserOffsetManager.setActiveStartPool`; on return it clears it. This scopes the offset lookup to the current placement call.
- **`StructurePoolElementMixin`** — injects into `StructurePoolElement.getGroundLevelDelta` (cancellable). For a `SinglePoolElement`, it looks up the active pool's per-template offset via `GeyserOffsetManager.getOffset` and, if found, overrides the returned ground-level delta. This is what actually seats each geyser template at its intended depth.
- **`SinglePoolElementAccessor`** — a mixin accessor exposing the private `template` field (`Either<ResourceLocation, StructureTemplate>`) of `SinglePoolElement`, used by `StructurePoolElementMixin` to resolve the template id.

> [!note] Implementation
> The custom `nether_ground_jigsaw` structure type is **registered but not currently referenced by any datapack structure**: every JSON under `data/resourceful_refinement/worldgen/structure/` (including the Nether ones) uses `"type": "minecraft:jigsaw"`. The Nether floor-anchoring that actually ships is delivered by the mixin path — `JigsawPlacementMixin` + `StructurePoolElementMixin` overriding the ground-level delta from `GeyserOffsetManager` offsets — layered on top of vanilla jigsaw placement. `NetherSurfaceJigsawStructure` / `nether_ground_jigsaw` is available (via `NetherSurfaceJigsawStructure.CODEC`) as the intended dedicated route but is not wired into the datapack yet. Also note two template-pool directories exist (`nether_scorchia_basalt_geyser_template`, `nether_scorchia_ocean_template`) whose pools are **not** in `GeyserOffsetManager.init()`'s loaded list, so those variants do not get custom offsets.

### Data files

- **Structures:** `data/resourceful_refinement/worldgen/structure/*.json`
- **Structure sets:** `data/resourceful_refinement/worldgen/structure_set/*.json` (`minecraft:random_spread`)
- **Template pools:** `data/resourceful_refinement/worldgen/template_pool/*/start_pool.json`
- **Biome tags:** `data/resourceful_refinement/tags/worldgen/biome/has_structure/*.json`

## Related

- [[Geyser Block]]
- [[Fracking Pump]]
- [[Fracking Source Blocks]]
