# Fluid Refill Station — Implementation Plan

**Mod ID:** `resourceful_refinement` · **Block ID:** `fluid_refill_station`  
**Design spec:** `Resourceful Refinement Design Docs/Fluid Refill Station.md`  
**Code package:** `com.resourceful_refinement.content.refill_station`

When design docs disagree with code, trust the code unless this plan explicitly overrides it.

**Related code (for agents):**

| Area | Path |
|------|------|
| Block / BE / render | `src/main/java/.../content/refill_station/` |
| Container refill | `content/refill_station/FluidRefillStationInteractions.java` |
| Tracking ID GUI / network | `FluidRefillStationScreen`, `FluidRefillStationMenu`, `network/SetRefillStationTrackingIdPayload` |
| Hosegun tracking ID | `content/hosegun/HosegunTracking.java`, `ModDataComponents.HOSEGUN_TRACKING_ID` |
| Hosegun | `content/hosegun/HosegunItem.java`, `HosegunFluidHandler` |
| Gel splatter | `content/gel_splatter/GelSplatterBlockEntity.java` |
| Gel blob placement | `content/hosegun/GelBlobEntity.java` |
| Gel tracking (planned) | `content/gel_tracking/` — `GelTrackingSavedData`, `GelTrackingService` |
| Registries | `registry/ModBlocks`, `ModBlockEntities`, `ModItems` |
| Capabilities / client | `ResourcefulRefinementMain` |

---

## Behaviour summary (from design doc)

- Horizontally directional block; **solid faces**; **1000 mB** internal tank.
- **Pipe IO:** all faces **except** the front (`FACING` = model-local north / player-facing front).
- **Right-click with fluid container:** drain station → item (empty or same fluid only). Discrete containers need minimum amount; dynamic containers (hosegun) take whatever is available. **Cannot** fill the station from a held container.
- **Empty-hand right-click:** GUI to set a **Tracking ID** (optional; default none).
- **Sneak + right-click with hosegun** on a labelled station: bind/unbind hosegun to that Tracking ID.
- **Gel tracking:** world-level `SavedData` — bound hosegun splatters increment/decrement global count per ID; splatter blocks store associated ID.
- **Create Display Link:** with ID → `"{Tracking ID}: {gel count}"`; without ID → `"{amount}mb {fluid name}"`.
- **Redstone:** when powered and station has Tracking ID → destroy all gel splatters for that ID (performant); count returns to 0.
- **Rendering:** BER casing + interior fluid box scaled 0–100% by fill level (animated still texture).

---

## Progress overview

| Phase | Description | Status |
|-------|-------------|--------|
| 1 | Core block, BE, tank, registration, renderer skeleton | **Done** |
| 2 | Player fluid interaction (container refill) | **Done** |
| 3 | Tracking ID GUI | **Done** |
| 4 | Hosegun binding | **Done** |
| 5 | World gel tracking (`SavedData`) | **Done** |
| 6 | Create Display Link integration | Not started |
| 7 | Redstone gel reset | Not started |
| 8 | Polish (art, ponder, JEI, recipe) | Not started |

*Update the **Status** column and checkboxes below as work completes.*

---

## Phase 1 — Core block & tank ✅

- [x] `FluidRefillStationBlock` — horizontal `FACING`, solid collision, `RenderShape.INVISIBLE`
- [x] `FluidRefillStationBlockEntity` — 1000 mB `FluidTank`, `trackingId` field, NBT + block update sync
- [x] Fluid capability — all sides except front face
- [x] `FluidRefillStationRenderer` — placeholder casing + scaled interior fluid box
- [x] `FluidRefillStationCasingModel` / `FluidRefillStationLayers`
- [x] Register in `ModBlocks`, `ModBlockEntities`, `ModItems`
- [x] BER + layer defs in `ResourcefulRefinementMain.ClientModEvents`
- [x] Lang, blockstate, fallback block/item models
- [ ] **Texture:** `assets/.../textures/block/fluid_refill_station.png` (placeholder referenced; art TBD)
- [ ] Replace placeholder casing mesh with final Blockbench export

---

## Phase 2 — Player fluid interaction ✅

- [x] `useItemOn` on block: attempt to fill held item from station tank (`FluidRefillStationInteractions`)
- [x] Reject if station empty or fluid mismatch on item
- [x] Reject filling **into** station from held container (drain-only interaction — only `fill` on item, never `drain` from item)
- [x] Discrete containers (bucket, bottle): require full minimum fill amount when empty; partial discrete needs station ≥ remaining space
- [x] Dynamic containers (hosegun): fill up to `min(available, capacity - current)`
- [x] Use NeoForge `Capabilities.FluidHandler.ITEM` / existing `HosegunFluidHandler`
- [x] Server-side logic; `BOTTLE_FILL` sound on success
- [ ] Manual test: bucket, bottle, hosegun, wrong fluid, partial hosegun

---

## Phase 3 — Tracking ID GUI ✅

- [x] Empty-hand `useWithoutItem` opens configuration UI
- [x] `FluidRefillStationScreen` — text field, Save, Clear; Enter saves
- [x] `SetRefillStationTrackingIdPayload` + `ModNetworking` (server applies + sanitise)
- [x] `sanitiseTrackingId` — trim, max 32 chars, strip control characters
- [x] Sync `trackingId` to client via existing BE block update / NBT
- [x] `ModMenus` + `FluidRefillStationMenu` registered
- [x] Lang keys for GUI labels, hint, Save, Clear

---

## Phase 4 — Hosegun binding ✅

- [x] `hosegun_tracking_id` data component in `ModDataComponents`
- [x] `HosegunTracking` helper (get/set/clear on ItemStack)
- [x] Sneak + right-click with hosegun: bind to station ID, or unbind if already matched
- [x] Sneak + hosegun on station without ID → action bar hint (no refill)
- [x] Normal right-click with hosegun still refills (Phase 2)
- [x] Hosegun tooltip: bound ID or “unbound”
- [x] Lang keys for bind/unbind/no-ID messages

---

## Phase 5 — World gel tracking (revised architecture)

> **Plan revision (pre-implementation):** global data must **persist** across saves, support **station reference counts** per ID, and **prune** unused ID entries so maps do not grow forever. See architecture below before coding.

### Goals

| Requirement | Approach |
|-------------|----------|
| Persist gel counts & indices | Per-dimension `SavedData` (`data/resourceful_refinement_gel_tracking.dat`) |
| Display / minigame count | `gelBlockCount` per Tracking ID (not stored on stations) |
| Redstone purge (Phase 7) | `splatterIndex`: block pos → Tracking ID |
| No unbounded map growth | **Prune** IDs with `stationRefCount == 0` and `gelBlockCount == 0` and empty splatter index |
| Multi-station same ID | Shared `gelBlockCount`; each station increments `stationRefCount` |
| Correct counts after reload | **Reconcile** on dimension load + incremental deltas at runtime |

### Data model (`GelTrackingSavedData`)

One `SavedData` instance per **server level** (overworld / nether / end each have their own file).

```text
Per TrackingId (string key, sanitised):
  gelBlockCount     — number of gel splatter blocks tagged with this ID
  stationRefCount   — number of fluid_refill_station BEs currently using this ID

Global indexes (for fast updates & Phase 7):
  splatterIndex: Map<BlockPos (or long), TrackingId>
    — every gel splatter block that carries a Tracking ID
```

**Do not** store hosegun bindings here (item component only).

**Splatter block NBT** (`GelSplatterBlockEntity`): add `TrackingId` string when placed from a bound hosegun. Block NBT is the **per-block source of truth**; SavedData is the **aggregated index + counts** for queries (Display Link, purge).

### Service layer (`GelTrackingService`)

Static façade used by refill stations, gel blobs, splatter BEs, and events — always resolves `GelTrackingSavedData` from `ServerLevel.getDataStorage()`.

| API (conceptual) | When |
|------------------|------|
| `onStationTrackingIdChanged(level, pos, oldId, newId)` | GUI save, clear, or first assign |
| `onStationRemoved(level, pos, id)` | Station broken or unloaded after remove |
| `onStationLoaded(level, pos, id)` | Chunk load / BE `setRemoved` inverse — (re)register ref if `id` non-empty |
| `onSplatterAdded(level, pos, id)` | Gel blob creates/updates splatter with bound hosegun |
| `onSplatterRemoved(level, pos)` | Splatter broken, exploded, replaced, fluid wash, redstone purge |
| `getGelCount(id)` / `getGelCountForStation(station)` | Display Link, debug |
| `getSplatterPositions(id)` | Phase 7 redstone reset (iterate index, not world scan) |
| `tryPruneId(id)` | After any delta that may zero out refs |
| `reconcileLevel(level)` | After dimension load (deferred) |
| `pruneOrphans(level)` | End of reconcile; optional light pass after deltas |

### Station reference counting

| Event | Action |
|-------|--------|
| Station assigned ID `B` (was empty) | `stationRefCount[B]++`, create entry if needed |
| Station ID changed `A` → `B` | `A--`, `B++`, `tryPrune(A)` |
| Station ID cleared | `id--`, `tryPrune(id)` |
| Station broken | `onStationRemoved` with last known ID |
| Station loaded from disk | `onStationLoaded` if `hasTrackingId()` — **required** so refs survive restart |

Hook points: `FluidRefillStationBlockEntity.setTrackingId`, `setRemoved` / block break, `onLoad` (server).

**Important:** gel splatters **do not** change Tracking ID when a station is relabelled; only new shots use the hosegun’s current binding.

### Gel block counting

| Event | Action |
|-------|--------|
| `GelBlobEntity` creates/updates splatter | Read shooter’s `HosegunTracking`; if present → set splatter NBT + `onSplatterAdded` (guard: if pos already in index for same ID, skip duplicate increment) |
| Splatter broken / block removed | `onSplatterRemoved` — read ID from index or BE NBT before TE gone |
| Splatter loaded from chunk | **Do not** increment on load; index restored from SavedData **or** rebuilt during reconcile |

### Cleanup (orphan pruning)

**Definition:** Tracking ID entry is **orphan** when:

- `stationRefCount == 0`, and  
- `gelBlockCount == 0`, and  
- no positions in `splatterIndex` for that ID  

**`tryPruneId(id)`** — cheap; call after every station/splatter delta that might zero an ID.

**`pruneOrphans(level)`** — scan all keys; remove orphans. Call:

1. **End of `reconcileLevel`** (session/chunk-load safety net), and  
2. Optionally throttled after burst activity (e.g. redstone purge) — not every single splatter delta if `tryPruneId` already runs.

Avoid full-world block scans for cleanup; only prune **map keys** using maintained counters + index size.

### Reconcile (dimension load / optional recovery)

After server level is ready (e.g. `ServerLevelEvent.Load` or first tick enqueue):

1. **Rebuild `stationRefCount`** — reset counts to 0, iterate loaded refill station BEs (or register via each station’s `onLoad` walking only loaded chunks). Prefer **per-BE `onLoad` register** plus a “reconcile pending” flag to avoid scanning entire world.  
2. **Validate `splatterIndex` vs level** — for each indexed pos in **loaded chunks**, verify block is still a gel splatter with matching NBT; drop stale entries, adjust `gelBlockCount`. Unloaded chunks: trust index until chunk loads (on chunk load, validate entries in that chunk).  
3. **`gelBlockCount`** — recompute per ID from `splatterIndex` after validation, or trust incremental if index trusted.  
4. **`pruneOrphans`**.

Schedule reconcile **once per dimension load**, not on every chunk load (chunk load can validate only its slice incrementally).

### Persistence

- Extend `SavedData`; NBT for `Stats` + `Splatters` + `Stations` (`GelTrackingSavedData.save` / `load`).  
- `setDirty()` on every mutating API; file: `data/<world>/data/resourceful_refinement_gel_tracking.dat` per dimension.  
- **Load healing:** `GelSplatterBlockEntity.onLoad` re-registers from BE `TrackingId` NBT; chunk validate discovers BEs missing from index; reconcile syncs index from BE before removing stale entries.  
- **Do not** drop index entries for stations/splatters in unloaded chunks on world load.

### Implementation checklist (Phase 5) ✅

- [x] `content/gel_tracking/GelTrackingSavedData` — NBT load/save, stats + `splatterIndex` + `stationIndex`, `setDirty`
- [x] `content/gel_tracking/GelTrackingService` — public API façade
- [x] `content/gel_tracking/ModGelTrackingEvents` — level load reconcile, chunk validate, gel break
- [x] `GelSplatterBlockEntity` — `TrackingId` NBT, `applyTrackingId`, `setRemoved` unregister
- [x] `GelBlobEntity` — `TrackingId` on blob (from hosegun at spawn); cleanse calls `onSplatterRemoved`
- [x] `FluidRefillStationBlockEntity` — `setTrackingId`, `onLoad`, `setRemoved` station index
- [ ] Manual test: bind hosegun → shoot → count++; break splatter → count--; station ID change; restart world; prune

### Edge cases

| Case | Handling |
|------|----------|
| Duplicate `onSplatterAdded` same pos | Index replace; only increment if new pos or ID changed |
| Break splatter without BE | Use `splatterIndex` in block break event / `onRemove` |
| Station ID cleared while gel remains | ID entry kept until gel count 0 (`stationRefCount` 0, `gelBlockCount` > 0) |
| All stations gone, gel remains | Same — count still valid for minigame |
| All gel gone, stations use ID | Keep entry while `stationRefCount > 0` (count 0 for display) |
| `/forceload` + old index | Reconcile on chunk load validates positions |

### Phase 6 / 7 dependencies

- **Display Link (6):** `GelTrackingService.getGelCount(station.getTrackingId())` — show `0` if no ID.  
- **Redstone reset (7):** `getSplatterPositions(id)` + batch break; then `gelBlockCount = 0`, clear index slice for ID, `tryPruneId`.

---

## Phase 6 — Create Display Link

- [x] Register Display Link source for refill station (`ModDisplaySources` + `FluidRefillStationDisplaySource`)
- [x] With Tracking ID: display `"{id}: {count}"` via `GelTrackingService.getGelCountForStation`
- [x] Without Tracking ID: display `"{tankAmount}mb {fluidName}"` (empty tank → `0mb`)
- [ ] Test with Create display board + link (manual in-game)

---

## Phase 7 — Redstone gel reset

- [x] `onNeighborChanged` / redstone rising edge when station becomes powered
- [x] If `trackingId` non-empty: `GelTrackingService.scheduleGelPurge` snapshots `getSplatterPositions(id)`
- [x] `GelTrackingPurgeScheduler` — 48 breaks/tick/dimension; unloads rotate in queue
- [x] `level.removeBlock` → `GelSplatterBlockEntity.setRemoved` → `onSplatterRemoved`; `tryPruneId` when queue drains
- [ ] Manual test: redstone pulse clears gel + Display Link shows 0

---

## Phase 8 — Polish

- [ ] Final casing model + texture
- [ ] Crafting recipe / advancement (if desired)
- [ ] Create Ponder scene
- [ ] JEI / goggles tooltip (tank, tracking ID, bound count when applicable)
- [ ] Update `docs/AGENT_PROJECT_OVERVIEW.md` class map when feature is complete

---

## Agent notes

- **Front face:** `FluidRefillStationBlock.getFrontFace(state)` == `FACING`. No fluid capability on that side.
- **Placement:** block faces opposite player look direction (same as casting depot / forge mould).
- **Do not** store gel **counts** on refill stations — use `GelTrackingSavedData` per dimension. Stations only hold their **label** (`trackingId` field).
- **Do** maintain `stationRefCount` in SavedData for prune/reconcile — updated on ID change, load, and break.
- **Prune** orphan IDs after deltas and after dimension reconcile — prevents unbounded `Map` growth.
- **Splatter `TrackingId` NBT** + `splatterIndex` must stay in sync; prefer hooking removal in block/BE lifecycle.
- **Australian English** in new user-facing lang strings.
- After substantive progress, update the **Progress overview** table and phase checkboxes in this file.

---

## Changelog

| Date | Agent / author | Change |
|------|----------------|--------|
| 2026-05-27 | — | Initial plan; Phase 1 skeleton merged |
| 2026-05-27 | — | Phase 2: `FluidRefillStationInteractions` + `useItemOn` refill logic |
| 2026-05-27 | — | Phase 3: Tracking ID menu, screen, payload, empty-hand open |
| 2026-05-27 | — | Phase 4: hosegun_tracking_id component, sneak-bind, tooltips |
| 2026-05-27 | — | Phase 5 plan revision: persistence, station refs, splatter index, prune + reconcile |
| 2026-05-27 | — | Phase 5 implemented: `GelTrackingSavedData`, service, events, splatter/station hooks |
