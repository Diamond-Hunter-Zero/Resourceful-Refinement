---
title: Crystal Node Worldgen Plan
category: Worldgen
status: Planned
introduced: v0.4
recipe_type: n/a
related:
  - "[[Crystal Fissure Bud]]"
  - "[[Resonance Crystal]]"
  - "[[Resource Resonator]]"
  - "[[Choral Clusters]]"
  - "[[Nether Geyser Worldgen]]"
tags:
  - worldgen
  - structure
  - planning
  - resonator
---

Design plan for generating the **Resonance Crystal** and **Crystal Fissure Bud** resource nodes across all three dimensions, and making them detectable on the [[Resource Resonator]]. This page records the approach decisions and the implementation shape; it is a plan, not yet built.

## The overarching decision: these are all structures

**Every detectable node is a `minecraft:random_spread` structure.** This is forced by the already-implemented [[Resource Resonator]], not a free choice:

- `ResourceScanService` predicts node positions *purely from the world seed*, with **no chunk generation**, by iterating a target's **structure sets** and calling `RandomSpreadStructurePlacement.getPotentialStructureChunk(seed, …)`, then biome-filtering via the live `BiomeSource`.
- It only understands `random_spread` **structures**. A **placed feature**'s position depends on per-chunk decoration RNG *and the generated heightmap/ block predicates*, which can't be known without generating the chunk — so placed features are invisible to the Resonator.
- `ResonatorTarget` already encodes this: `CRYSTAL_FISSURE` exists with an empty set list ("produces no pins until its worldgen exists"), and there is **no `RESONANCE_CRYSTAL` target yet**.

So: scatter decoration (chorus growths, sprouts) stays as biome placed-features, but **the resource nodes themselves become structures**. This also matches the existing [[Nether Geyser Worldgen|geyser]] pattern (geysers are already `random_spread` structures the Resonator reads).

> [!note] Implementation
> **Resonator accuracy principle.** The scan matches a candidate structure-chunk to a structure purely by **biome** — it does *not* call `findGenerationPoint`. So if a structure's `findGenerationPoint` returns empty (no valid surface, failed flatness check, exclusion zone), the Resonator shows a **false-positive pin** there. Therefore our `findGenerationPoint` must **rarely reject**: prefer *adapting* to the terrain (seat on whatever surface is found) over rejecting, and express exclusions through **biome restriction** (which the scan replicates) rather than in-code distance checks. Hard rejects should be limited to genuine "nothing here" cases (the End void, no Nether floor).

## Resource Resonator wiring

What needs to change in `content/resonator/scan/`:

1. **Add a `RESONANCE_CRYSTAL` target** to `ResonatorTarget` (append to the end — ordinal order is the wire/filter order), listing the three resonance structure sets.
2. **Wire `CRYSTAL_FISSURE`** to the three fissure structure sets.
3. **Pin icons** (`ResonatorPinIcons`) for the two categories (and optional per-dimension variants — see below).
4. **GUI filter** gains the ResonanceCrystal toggle automatically from the enum, but check the screen's filter layout has room.

> [!note] Implementation
> **Icons.** Resonance Crystals use a **single icon** (no variant). Crystal Fissures use **one icon per type — chorus / terran / quartz — like geysers**. Because each fissure is its own structure, the variant is a trivial structure-id → name map (`crystal_fissure_end`→`chorus`, `_nether`→`quartz`, `_overworld`→`terran`), far simpler than `GeyserVariants` (no RNG replay needed). Extend the scan's variant resolution to handle this alongside `GeyserVariants`. Y is reported "approximate" for any non-`SURFACE_GEYSER` target (X/Z exact); fine for a radar.

## Shared technology

### A. One configurable "Crystal Node" structure for the small placements

Four of the six (both resonance variants + End & Nether fissures) are small surface placements that differ only in *content*, so build **one custom `Structure` type driven by a config codec**, instantiated as several structure JSONs. Config knobs (all datapack-tweakable):

| Knob | Purpose |
|---|---|
| `node_block` | centre block — `resonance_crystal` or `crystal_fissure_bud` |
| `source_item` (opt.) | if the node has a BE source (the fissure bud), set it at generation → selects [[Drill Pylon]] recipe + [[Crystal Fissure Bud Tints\|tint]] |
| `base_blocks` (weighted list) + `base_radius` + `base_fill_chance` (opt.) | a **noisy circular surface disc** that replaces the top terrain layer under/around the node — a weighted random mix with an irregular (noise) edge. `base_fill_chance` (<1) also leaves scattered natural-surface cells inside the disc. Nether = `blackstone` + `gilded_blackstone`; Overworld = `stone` + `tuff` + `andesite`; End fissure = `chorus_crystal` at 0.85 fill (glows, so visible from distance). |
| `scatter_block` + `scatter_count` + `scatter_radius` | companion scatter (chorus/terran growths around, or ore blobs below) |
| `scatter_mode` | `around_surface` (growths, facing up) vs `below` (ore under the bud) |
| `decor_block` + `decor_count` (opt.) | extra surface decor scattered on the base — `minecraft:small_amethyst_bud` for the Nether/OW resonance crystals |
| `anchor` | `surface` or `nether_floor` (see below) |
| `footprint_radius` | overall size (≈3 for End resonance, ≈5 for the rest) |

This keeps ~1 structure + 1 piece of code for four variants; each dimension is a JSON + its own structure set (independent rarity). The noisy-disc base and the amethyst-bud decor are small reusable helpers in the piece (a weighted block picker + a radial noise mask).

### B. Anchoring per dimension

- **Overworld / End surface** — sample `getBaseHeight(WORLD_SURFACE_WG)` (as `ChoralClusterMountainStructure` already does). In the End, reject only if `y ≤ 5` (void between islands).
- **Nether floor** — the hard case: `WORLD_SURFACE_WG` is the cave *ceiling*. Reuse the existing cave-floor scan from `NetherSurfaceJigsawStructure.findGenerationPoint` (scan Y 115→32 for air-over-solid). Extract it into a shared `NetherFloorFinder` helper used by both the geyser path and the new `anchor: nether_floor` mode.

### C. Setting the fissure bud's source item in worldgen

After the piece places `crystal_fissure_bud`, fetch the BE and call `setSourceItem(...)` with the configured item. BE NBT persists, so the bud carries the right source (recipe + tint) with no player interaction. Sources: End→`chorus_crystal`, Nether→`minecraft:quartz`, Overworld→`terran_crystal`.

### D. The Overworld sinkhole = its own carving structure

The Overworld fissure is a terrain-*carving* formation, too different for the shared node structure. It gets a dedicated `Structure` + piece (sibling to the choral mountain, but digging down):

- Carve a **conical eroded bowl** from the surface down to stone: per column within radius, remove soil/dirt/grass (and stone to the floor depth) following a cone/parabola profile (deeper at centre), leaving a flat stone floor. Replace only soft terrain + a controlled stone depth; leave a natural eroded rim.
- Floor: scattered **Terran Crystal** blobs (ore-like clusters), a single **Crystal Fissure Bud** (source `terran_crystal`) at the bottom centre.
- Walls & floor: **Terran Crystal Growths** scattered throughout (reuse the `attached_growth` feature logic, or place inline).
- Handle water/lava intersections gracefully (don't leave floating liquids; this is the main edge case).

## Per-node specifications

| Node | Dim | Approach | Biome gate | Rarity | Contents | Source | Resonator icon |
|---|---|---|---|---|---|---|---|
| Resonance Crystal | End | Node structure, `surface` | outer-End biomes (see below) | rare | 1 resonance crystal + few `chorus_crystal_growth`, ~3³, minimal cut, no base | — | ResonanceCrystal (single) |
| Resonance Crystal | Nether | Node structure, `nether_floor` | nether biomes | very rare | 1 resonance crystal on a noisy `blackstone`/`gilded_blackstone` disc + scattered `small_amethyst_bud`, ~5³ | — | ResonanceCrystal (single) |
| Resonance Crystal | Overworld | Node structure, `surface` | overworld surface biomes | very rare | 1 resonance crystal on a noisy `stone`/`tuff`/`andesite` disc + scattered `small_amethyst_bud`, ~5³ | — | ResonanceCrystal (single) |
| Crystal Fissure | End | Node structure, `surface` | `choral_clusters` only | moderate | 1 bud on a noisy `chorus_crystal` base disc (85% fill) + many `chorus_crystal_growth`, ~5³ | `chorus_crystal` | CrystalFissure / **chorus** |
| Crystal Fissure | Nether | Node structure, `nether_floor` | netherrack biomes | rare | 1 bud + `nether_quartz_ore` blob below, ~5³ | `minecraft:quartz` | CrystalFissure / **quartz** |
| Crystal Fissure | Overworld | **Sinkhole** structure | plains / hills / forest | rare | eroded bowl, `terran_crystal` blobs, `terran_crystal_growth`, 1 bud at centre | `terran_crystal` | CrystalFissure / **terran** |

### The End 1024 exclusion — solved by biome

Rather than an in-code distance check (which the Resonator can't see → false positives), restrict the End resonance structure to the **outer-End biomes** (`end_highlands`, `end_midlands`, `end_barrens`, `small_end_islands`) and exclude the central `minecraft:the_end` biome. The central island biome covers roughly the inner ~1000 blocks, so this gives an *approximate, biome-accurate, scan-consistent* exclusion for free. (Documented as "approx 1024" — exact radius follows the biome boundary.)

## New content & data this depends on

**Blocks — all already exist, nothing new to make:**
- `resourceful_refinement:terran_crystal` (block + item) — the Overworld crystal and the fissure source "Terran Crystal". Green-blue palette. `ModBlocks.TERRAN_CRYSTAL`.
- `resourceful_refinement:terran_crystal_growth` (`AmethystClusterBlock` + item) — used in the sinkhole. `ModBlocks.TERRAN_CRYSTAL_GROWTH`; blockstate already has the 6-way `FACING` variants, like [[Choral Vegetation|chorus_crystal_growth]].
- Bases use **vanilla blocks only**: Nether `blackstone` + `gilded_blackstone`; Overworld `stone` + `tuff` + `andesite`.
- Decor uses vanilla `minecraft:small_amethyst_bud`.
- Nether fissure ore uses vanilla `minecraft:nether_quartz_ore`.

**Data:**
- [[Crystal Fissure Bud Tints|Fissure tint profiles]] for `chorus_crystal`, `minecraft:quartz`, `terran_crystal` (so each dimension's bud glows distinctly).
- Biome tags: `has_structure/*` for each structure (End-outer, choral-only, nether, overworld-plains-hills-forest).
- Structure JSONs + `random_spread` structure sets (one per node) + Resonator target wiring + fissure pin icons (chorus/terran/quartz) and a resonance pin icon.
- Reuse `attached_growth` for growth scatter where possible.

> [!note] Deferred
> **No [[Drill Pylon]] recipes for the fissure sources yet** — you'll author the `chorus_crystal` / `quartz` / `terran_crystal` drill recipes later. Until then the generated buds carry the right source (and tint) but produce nothing when drilled.

**Existing content reused:** `resonance_crystal` (natural) block, `crystal_fissure_bud` block + BE + tint system, `chorus_crystal_growth`, `terran_crystal(_growth)`, the Nether cave-floor finder, `ChoralClusterMountainStructure` as a code pattern.

## Rarity (starting `random_spread` values, tweakable)

| Set | spacing | separation | notes |
|---|---|---|---|
| resonance_crystal_end | 40 | 12 | rare across outer End |
| resonance_crystal_nether | 48 | 16 | very rare |
| resonance_crystal_overworld | 56 | 20 | very rare |
| crystal_fissure_end | 16 | 6 | moderate, choral only |
| crystal_fissure_nether | 40 | 12 | rare |
| crystal_fissure_overworld | 48 | 16 | rare, big footprint |

Each set needs a unique `salt`. (These are first guesses — tune after seeing them in-world.)

## Resolved decisions

1. **Terran Crystal** — already implemented (green-blue palette); `terran_crystal` + `terran_crystal_growth` reused as-is. No new blocks/art.
2. **Resonance bases** — vanilla, as a noisy circular disc: Nether `blackstone`+`gilded_blackstone`; Overworld `stone`+`tuff`+`andesite`.
3. **Drill Pylon recipes** — deferred (author later). Buds still carry their source + tint.
4. **Resonance decor** — scattered vanilla `small_amethyst_bud` (Nether & Overworld).
5. **Resonator icons** — Resonance Crystal: one icon. Crystal Fissure: one icon per type (chorus / terran / quartz), via a structure-id variant map.

Remaining tunables (not blockers): the `random_spread` spacing/separation values below, exact footprint radii, scatter/decor counts, and the sinkhole profile — all dialled in after first in-world look.

## Suggested build order

Blocks already exist, so building starts at the structure:

1. ✅ **Configurable Crystal Node structure** (type + piece + config codec) + the shared `NetherFloorFinder` + the noisy-disc / weighted-block / decor helpers. — *done; `worldgen/crystalnode/` + `worldgen/structure/NetherFloorFinder`, registered as `crystal_node` structure + `crystal_node_piece`.*
2. ✅ **End + Nether fissures** and **all three resonance crystals** as node-structure JSONs + `random_spread` sets + `has_structure/*` biome tags. — *done; 5 structures + 5 sets + 5 biome tags under `worldgen/structure`, `worldgen/structure_set`, `tags/worldgen/biome/has_structure`.*
3. ✅ **Resonator wiring** — added the `RESONANCE_CRYSTAL` target (3 sets), wired `CRYSTAL_FISSURE` (end + nether now, overworld pre-wired for step 5), `CrystalFissureVariants` (chorus/quartz/terran icon map), scan variant branch, `resonance_crystal` generic + `quartz` variant pins (placeholder art), and the lang label. *Existing resonators default the 2 new filter rows off until toggled; new ones default on.*
4. ✅ **[[Crystal Fissure Bud Tints|Fissure tint profiles]]** for `chorus_crystal` (magenta/violet), `quartz` (whites/creams), `terran_crystal` (green-blue) — in `…/resourceful_refinement/fissure_bud_tints/`.
5. ✅ **Overworld sinkhole** structure (the big custom carve) — `worldgen/sinkhole/` (`overworld_sinkhole` structure + `overworld_sinkhole_piece`), datapack `crystal_fissure_overworld` + `_set` + biome tag (plains/hills/forest). Auto-detects as the pre-wired **terran** fissure pin.
6. Tune rarities/footprints; update [[Crystal Fissure Bud]], [[Resonance Crystal]], [[Resource Resonator]] docs Planned → Implemented. (Drill Pylon recipes authored separately by you.)

## Related

- [[Crystal Fissure Bud]] · [[Crystal Fissure Bud Tints]] · [[Resonance Crystal]] · [[Drill Pylon]]
- [[Resource Resonator]] · [[Choral Clusters]] · [[Choral Vegetation]] · [[Nether Geyser Worldgen]]
