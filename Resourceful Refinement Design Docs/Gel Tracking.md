---
title: Gel Tracking
category: Framework
status: Implemented
introduced: v0.2
recipe_type: n/a
related:
  - "[[Fluid Refill Station]]"
  - "[[Hosegun]]"
  - "[[Gel Splatter]]"
tags:
  - gel
  - framework
---

Gel Tracking is a server-side framework that indexes placed [[Gel Splatter]] blocks by a **tracking ID**, so that a [[Fluid Refill Station]] or a bound [[Hosegun]] can remotely count, refill or purge them. It underpins gel-based minigames (parkour courses, splatter arenas) where you need to know how many gels of a given ID exist, and clear them all at once. It also feeds Create display links so a station can print its gel count to a sign or screen.

## Concepts

A tracking ID is an arbitrary sanitised string. A [[Hosegun]] bound to a [[Fluid Refill Station]] stamps that ID onto every gel-blob it fires; each blob that becomes a [[Gel Splatter]] tags its block entity with the ID. The framework keeps a persistent per-dimension index mapping:

- **splatter position → tracking ID** — every tracked gel splatter in the world.
- **station position → tracking ID** — every station currently claiming an ID (keeps an ID alive even when no gels exist yet).
- **tracking ID → stats** — a per-ID cache of gel block count and station reference count.

An ID is pruned automatically once it has no tracked splatters and no station references (`tryPruneId` / `pruneOrphans`). The index self-heals: splatters and stations are re-validated against the live world on level load and per chunk load, and reconciled counts are recomputed from the index.

## Key Types

- **`GelTrackingService`** — stateless facade over the saved data. Exposes `getGelCount`, `getGelCountForStation`, `getAllTrackingIds`, `getSplatterPositions`, the station/splatter add/remove hooks (`onStationTrackingIdChanged`, `onStationLoaded`, `onStationRemoved`, `onSplatterAdded`, `onSplatterRemoved`), reconciliation (`reconcileLevel`, `validateChunk`), and purge control (`scheduleGelPurge`, `tickPurgeQueues`).
- **`GelTrackingSavedData`** — the persistent `SavedData` (`resourceful_refinement_gel_tracking`), stored per dimension via `DimensionDataStorage`. Holds the splatter index, station index and per-ID `TrackingIdStats`; handles NBT save/load, count reconciliation, chunk validation and orphan pruning. Uses fastutil `Long2ObjectOpenHashMap` keyed on packed block positions.
- **`GelTrackingPurgeScheduler`** — batched removal queue, one `PurgeQueue` per dimension. Breaks at most `BATCH_SIZE = 48` splatters per dimension per server tick so a redstone reset does not spike the server; unloaded positions are re-queued until loaded, and the ID is pruned once the queue drains.
- **`ModGelTrackingEvents`** — `@EventBusSubscriber` wiring: reconciles a level one tick after `LevelEvent.Load`, validates each chunk one tick after `ChunkEvent.Load`, ticks purge queues on `LevelTickEvent.Post`, and removes an index entry on `BlockEvent.BreakEvent` for gel splatter blocks.

## How Features Use It

- **[[Gel Splatter]]** — `GelSplatterBlockEntity` calls `onSplatterAdded` / `onSplatterRemoved` when a tracked splatter is placed, retextured, cleared, loaded or removed (`applyTrackingId`, `clearTracking`, `onLoad`, `setRemoved`). Water/cleanse gel-blobs that clear splatters also notify removal.
- **[[Hosegun]]** — a Hosegun bound to a station carries the station's tracking ID (`hosegun_tracking_id`); its `GelBlobEntity` copies the ID onto placed splatters (`tagSplatterTracking`).
- **[[Fluid Refill Station]]** — registers/unregisters its tracking ID, reads gel counts (`getGelCountForStation`), and can schedule a full purge (e.g. on a redstone reset). Its Create display source (`FluidRefillStationDisplaySource`) prints `"<id>: <count>"` to display targets.

## Implementation

- **Package:** `content/gel_tracking/`.
- **Classes:** `GelTrackingService`, `GelTrackingSavedData` (`SavedData`, data name `resourceful_refinement_gel_tracking`), `GelTrackingPurgeScheduler` (`BATCH_SIZE = 48`), `ModGelTrackingEvents` (event subscriber).
- **Persistence:** per-dimension `SavedData` via `ServerLevel.getDataStorage()`; positions packed with `BlockPos.asLong()`.
- **Server-side only:** all operations early-out unless the level is a `ServerLevel`.
- **Display link:** `content/refill_station/FluidRefillStationDisplaySource` (a Create `SingleLineDisplaySource`) reads `GelTrackingService.getGelCountForStation`.
- **ID sanitisation:** shared with the station via `FluidRefillStationBlockEntity.sanitiseTrackingId`.

## Related

- [[Fluid Refill Station]]
- [[Hosegun]]
- [[Gel Splatter]]
