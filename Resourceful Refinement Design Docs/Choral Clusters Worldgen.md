---
title: Choral Clusters Worldgen
category: Worldgen
status: Implemented
introduced: v0.4
recipe_type: n/a
related:
  - "[[Choral Clusters]]"
  - "[[Chorus Crystal Pillars]]"
  - "[[Choral End Stone]]"
  - "[[Nether Geyser Worldgen]]"
tags:
  - worldgen
  - biome
  - structure
  - mixin
  - technical
---

Technical breakdown of how the [[Choral Clusters]] biome is injected into the End and how its mountain terrain is generated. Two independent systems cooperate: a **runtime biome-source injection** (mixins) that makes the biome appear in the outer End, and a **custom structure** (`choral_cluster_mountain`) that raises the mountains and seeds the [[Chorus Crystal Pillars]] inside the biome blob.

All Java lives under `src/main/java/com/resourceful_refinement/worldgen/choral/`. All data lives under `src/main/resources/data/`.

## Biome Registration & Placement

The biome is **not** a vanilla multi-noise End entry; it is a datapack biome that is spliced into `TheEndBiomeSource` at runtime.

### The biome definition

- **Key:** `ModBiomes.CHORAL_CLUSTERS` — just a `ResourceKey<Biome>` for `resourceful_refinement:choral_clusters`; there is no `DeferredRegister` for biomes (they are datapack-defined).
- **JSON:** `worldgen/biome/choral_clusters.json` — purple sky (`2631720`), lilac fog (`9868950`), magenta grass/foliage (`16745939`), sparse `minecraft:end_rod` ambient particles, cave mood sound, and a single monster spawner: `minecraft:enderman` (weight 50, 1–3). Carvers and spawn costs are empty. The only populated feature step is **`VEGETAL_DECORATION`** (index 9), which holds `resourceful_refinement:sparse_chorus_plant`; all other terrain/crystal decoration comes from the structure below.
- **Chorus plants:** `worldgen/placed_feature/sparse_chorus_plant.json` wraps the **vanilla `minecraft:chorus_plant` configured feature** with a sparse placement (`rarity_filter` chance 4 → ~1 attempt per 4 chunks, `in_square`, `heightmap: MOTION_BLOCKING`, `biome`). Vanilla's `ChorusPlantFeature` requires `minecraft:end_stone` *directly below* the origin, so plants populate the End-stone surface and naturally skip [[Choral End Stone]] patches.
- **Tags:** added to `minecraft:is_end`, `c:is_end_highland`, and `resourceful_refinement:has_structure/choral_cluster_mountain`.

### Runtime holder

`ChoralClustersBiomeHolder` caches the resolved `Holder<Biome>`:

- `ResourcefulRefinementMain.onServerAboutToStart` → `ChoralClustersBiomeHolder.resolve(registryAccess)`.
- `onServerStopping` → `ChoralClustersBiomeHolder.clear()`.

### Mixins (the actual injection)

Registered in `mixins.resourceful_refinement.json`:

- **`ChoralClustersEndBiomeSourceMixin`** (`@Mixin(TheEndBiomeSource.class)`):
  - `create(...)` HEAD — captures the choral biome holder into a static field (`biomeGetter.get(CHORAL_CLUSTERS)`), **unless** a vanilla registry-validation / bootstrap call is on the stack (guarded by `isVanillaRegistryValidation()`, which scans for `VanillaRegistries` / `Bootstrap`). This prevents the datapack biome from contaminating datagen.
  - `collectPossibleBiomes(...)` RETURN — appends the choral biome to the possible-biome stream (`distinct()`).
  - `getNoiseBiome(...)` RETURN — the placement rule. If the vanilla result is **End Highlands or End Midlands** *and* `ChoralClusterNoise.shouldPlaceChoralBiome(x, z)` is true, it swaps in the choral biome.
- **`BiomeSourceMixin`** (`@Mixin(BiomeSource.class)`): injects `possibleBiomes()` RETURN for any `TheEndBiomeSource`, adding the runtime holder so the engine's biome set includes Choral Clusters even though it isn't in the codec-loaded set.

> [!note] Implementation
> Because placement only overrides **highlands/midlands** columns, Choral Clusters only appear where the outer End would otherwise be high/mid island terrain — never on the small islands or the central island. Both mixins read the biome through `ChoralClustersBiomeHolder.get()` first and fall back to the mixin's own captured static field, so placement survives whichever resolves first.

### Placement maths — `ChoralClusterNoise`

`shouldPlaceChoralBiome(quartX, quartZ)` (biome coords are quart = block >> 2):

1. Convert to block coords (`<< 2`).
2. **Distance gate:** require `x² + z² ≥ 1024²` (`OUTER_END_MIN_BLOCK_DISTANCE = 1024`) — i.e. at least 1024 blocks from the End origin. This keeps the main/inner End vanilla.
3. **Noise gate:** require `biomeNoise > 0.24` (`BIOME_THRESHOLD`).

`biomeNoise(seed, …)` blends two octaves of a custom value-noise: a broad shape (`BIOME_NOISE_SCALE = 0.0019`, weight 0.78) plus detail (`BIOME_DETAIL_NOISE_SCALE = 0.008`, weight 0.22). The noise is a self-contained hash-based value noise (`valueNoise` / `randomValue` / quintic `smooth`) now **mixed with the world seed** (the octave salts `4141` / `9917` are XORed with it), so the biome's blob layout is **deterministic per seed and differs between worlds**.

> [!note] Implementation
> **Seed source.** `ChoralClusterNoise` takes a `long seed`. For biome placement (server-side) the mixin passes `ChoralSeed.worldSeed()` — the real world seed, captured at `ServerAboutToStartEvent` from `getWorldData().worldGenOptions().seed()` and cleared at `ServerStoppingEvent` (`ChoralSeed`). The hashes are still pure functions of `(seed, x, z)`, so generation stays deterministic and a future [[Resource Resonator]]-style predictive scan is still possible — it just needs the world seed as input. Client-side tints can't see the raw world seed, so they use the biome-zoom seed instead (see [[Chorus Crystal]] / [[Choral End Stone]]).

## The Mountain Structure

Terrain is produced by a custom `Structure`, not by jigsaw templates.

### Registration

- `ModStructureTypes.CHORAL_CLUSTER_MOUNTAIN` → `resourceful_refinement:choral_cluster_mountain` (`StructureType` on `Registries.STRUCTURE_TYPE`).
- `ModStructurePieceTypes.CHORAL_CLUSTER_MOUNTAIN_PIECE` → `choral_cluster_mountain_piece` (`StructurePieceType` on `Registries.STRUCTURE_PIECE`).
- Both deferred registers are attached in `ModRegistries`.

### Data wiring

- **Structure** `worldgen/structure/choral_cluster_mountain.json`: `type` = the modded structure, `biomes` = `#has_structure/choral_cluster_mountain` (= just `choral_clusters`), `step` = **`raw_generation`**, `terrain_adaptation` = `none`.
- **Structure set** `worldgen/structure_set/choral_cluster_mountain_set.json`: `minecraft:random_spread`, **`spacing 3`, `separation 2`**, `spread_type linear`, `salt 91720431`.

> [!note] Implementation
> `spacing 3 / separation 2` is **extremely dense** (for comparison, vanilla villages use spacing 34). Combined with the large island radii below, mountain pieces heavily overlap and tile the whole biome blob — which is how the biome reads as continuous mountains rather than discrete structures. It is also the single biggest performance/intensity knob; thinning the spread is the obvious first lever if generation is too heavy. The matching `STRUCTURE_SPACING / SEPARATION / SALT` constants in `ChoralClusterGenerationSettings` mirror these values but the JSON is what the game reads.

### `ChoralClusterMountainStructure.findGenerationPoint`

1. Pick a block position inside the chunk (offset 4–12 on each axis).
2. Sample base height via `WORLD_SURFACE_WG`; **reject if `y ≤ 5`** (no End terrain here, e.g. the void between islands).
3. Resolve the biome at that point from the biome source.
4. Emit a single `ChoralClusterMountainPiece`.

`generatePieces` rolls the island shape:
- **radius** = `MIN_ISLAND_RADIUS … MAX_ISLAND_RADIUS` = **24 … 108** blocks.
- **height** = `radius × (MIN_HEIGHT_MULTIPLIER … MAX_HEIGHT_MULTIPLIER)` = `radius × (0.9 … 1.05)`.

### `ChoralClusterMountainPiece.postProcess` — terrain build

Runs per chunk the bounding box touches. For each column `(x, z)` within `radius` of the piece centre:

1. **Base Y** = `max(WORLD_SURFACE, WORLD_SURFACE_WG)` first-available; skip columns with base `< 10`.
2. **Radial falloff** = `1 − (d²/r²)^0.3` (`VALLEY_SHARPNESS = 0.3`) — a broad dome that falls to 0 at the rim.
3. **Edge clamp** (`getBiomeEdgeClamp`) — samples the biome in a 12-block radius (step 4, distance-weighted) and returns the fraction that is Choral Clusters. Columns outside the biome blob get clamp 0 and are skipped, so mounds fade out at the biome border instead of cutting off hard.
4. **Terrain noise** — two octaves of `ChoralClusterNoise.valueNoise` (scales `0.045` and `0.095`) modulate the height by `× (0.78 + primary·0.28 + secondary·0.12)` for a rough, irregular surface. This gives the mound top `surfaceY = centerY + terrainHeight`.
5. **Fill (welded downward)** — if `surfaceY < baseY` the mound doesn't rise above the existing ground here, so the column is skipped. Otherwise the column is filled with **vanilla `end_stone`** from `surfaceY` down to `weldFloor = baseY − SURFACE_LAYER_DEPTH` (the pre-fill surface, plus one surface layer), replacing air / `end_stone` / `cave_air` / `choral_end_stone`. Because the bottom bound is the real surface, the mound is solid all the way to the ground it sits on — no floating lip — and the fill never drips into the void below the island. Extending one layer *past* `baseY` reclaims any [[Choral End Stone]] an earlier overlapping piece painted on the old surface, so it isn't left **sandwiched** as a buried band beneath this piece's fill.
6. **Surface patches** — if `ChoralClusterNoise.isSurfacePatch(world.getSeed(), x, z)` and the block above `surfaceY` is air (exposed), the top `SURFACE_LAYER_DEPTH` (default 1) `end_stone` blocks are repainted to [[Choral End Stone]]. The patch noise is **world-seed-driven** (the `SURFACE_PATCH_SEED` salt is XORed with `world.getSeed()`); within a world the seed is constant, so patches stay continuous across the overlapping pieces that tile the biome.
7. After the column loop, runs the [[Chorus Crystal Pillars|pillar generator]] for the chunk with a per-chunk RNG (`(terrainSeed << 32) ^ chunkPos`).

The piece persists `center`, `radius`, `height`, `terrain_seed`, and `biome` to NBT. Its bounding box extends `radius+4` horizontally, `max(16, radius·0.35)` below centre, and `max(radius,height)+80` above — the generous headroom is what lets tall/slanted [[Chorus Crystal Pillars]] fit.

> [!note] Implementation
> **Terrain body is vanilla End stone; [[Choral End Stone]] is a surface-only skin** (noise patches on the exposed top layer). Fill is welded **downward to the pre-fill surface `baseY`** rather than an upward run from a fixed `centerY`-relative floor — this is the fix for the floating-lip seam that was visible at mound bases in the exploration screenshots. The fill preserves [[Chorus Crystal]] blocks (they're excluded from `canReplaceTerrain`), so overlapping pieces don't overwrite crystals an earlier pass placed. Tuning lives in `ChoralClusterGenerationSettings` (`SURFACE_PATCH_*`, `SURFACE_LAYER_DEPTH`).

## Related

- [[Choral Clusters]] — content overview and integration map.
- [[Chorus Crystal Pillars]] — the crystal structures seeded per chunk here.
- [[Choral End Stone]] — the terrain block.
- [[Nether Geyser Worldgen]] — the other custom-structure worldgen in the mod (jigsaw + mixin pattern, for contrast).
