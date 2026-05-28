package com.resourceful_refinement.content.gel_tracking;

import com.resourceful_refinement.content.gel_splatter.GelSplatterBlockEntity;
import com.resourceful_refinement.content.gel_splatter.GelSplatterBlocks;
import com.resourceful_refinement.content.refill_station.FluidRefillStationBlockEntity;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

public class GelTrackingSavedData extends SavedData {

    private static final String DATA_NAME = "resourceful_refinement_gel_tracking";

    private final Map<String, TrackingIdStats> statsById = new HashMap<>();
    private final Long2ObjectOpenHashMap<String> splatterIndex = new Long2ObjectOpenHashMap<>();
    private final Long2ObjectOpenHashMap<String> stationIndex = new Long2ObjectOpenHashMap<>();

    public GelTrackingSavedData() {}

    public static SavedData.Factory<GelTrackingSavedData> factory() {
        return new SavedData.Factory<>(GelTrackingSavedData::new, GelTrackingSavedData::load);
    }

    public static GelTrackingSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(factory(), DATA_NAME);
    }

    private static GelTrackingSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        GelTrackingSavedData data = new GelTrackingSavedData();
        data.readFromTag(tag);
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag statsList = new ListTag();
        for (Map.Entry<String, TrackingIdStats> entry : statsById.entrySet()) {
            CompoundTag statsTag = new CompoundTag();
            statsTag.putString("Id", entry.getKey());
            statsTag.putInt("Gel", entry.getValue().gelBlockCount);
            statsTag.putInt("Stations", entry.getValue().stationRefCount);
            statsList.add(statsTag);
        }
        tag.put("Stats", statsList);
        tag.put("Splatters", writePosIdList(splatterIndex));
        tag.put("Stations", writePosIdList(stationIndex));
        return tag;
    }

    private void readFromTag(CompoundTag tag) {
        statsById.clear();
        splatterIndex.clear();
        stationIndex.clear();

        if (tag.contains("Stats", Tag.TAG_LIST)) {
            ListTag statsList = tag.getList("Stats", Tag.TAG_COMPOUND);
            for (int i = 0; i < statsList.size(); i++) {
                CompoundTag statsTag = statsList.getCompound(i);
                String id = statsTag.getString("Id");
                if (id.isEmpty()) {
                    continue;
                }
                TrackingIdStats stats = new TrackingIdStats();
                stats.gelBlockCount = statsTag.getInt("Gel");
                stats.stationRefCount = statsTag.getInt("Stations");
                statsById.put(id, stats);
            }
        }

        readPosIdList(tag.getList("Splatters", Tag.TAG_COMPOUND), splatterIndex);
        readPosIdList(tag.getList("Stations", Tag.TAG_COMPOUND), stationIndex);
    }

    private static ListTag writePosIdList(Long2ObjectOpenHashMap<String> index) {
        ListTag list = new ListTag();
        for (Long2ObjectMap.Entry<String> entry : index.long2ObjectEntrySet()) {
            CompoundTag entryTag = new CompoundTag();
            BlockPos pos = BlockPos.of(entry.getLongKey());
            entryTag.putInt("X", pos.getX());
            entryTag.putInt("Y", pos.getY());
            entryTag.putInt("Z", pos.getZ());
            entryTag.putString("Id", entry.getValue());
            list.add(entryTag);
        }
        return list;
    }

    private static void readPosIdList(ListTag list, Long2ObjectOpenHashMap<String> index) {
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entryTag = list.getCompound(i);
            BlockPos pos = new BlockPos(entryTag.getInt("X"), entryTag.getInt("Y"), entryTag.getInt("Z"));
            String id = entryTag.getString("Id");
            if (!id.isEmpty()) {
                index.put(pos.asLong(), id);
            }
        }
    }

    public int getGelCount(String trackingId) {
        TrackingIdStats stats = statsById.get(trackingId);
        return stats == null ? 0 : stats.gelBlockCount;
    }

    public Set<BlockPos> getSplatterPositions(String trackingId) {
        Set<BlockPos> positions = new HashSet<>();
        for (Long2ObjectMap.Entry<String> entry : splatterIndex.long2ObjectEntrySet()) {
            if (trackingId.equals(entry.getValue())) {
                positions.add(BlockPos.of(entry.getLongKey()));
            }
        }
        return Collections.unmodifiableSet(positions);
    }

    void registerStation(BlockPos pos, String trackingId) {
        long key = pos.asLong();
        String existing = stationIndex.get(key);
        if (trackingId.equals(existing)) {
            return;
        }
        if (existing != null) {
            decrementStationRef(existing);
        }
        if (trackingId.isEmpty()) {
            stationIndex.remove(key);
        } else {
            stationIndex.put(key, trackingId);
            incrementStationRef(trackingId);
        }
        setDirty();
    }

    void unregisterStation(BlockPos pos) {
        long key = pos.asLong();
        String existing = stationIndex.remove(key);
        if (existing != null) {
            decrementStationRef(existing);
            tryPruneId(existing);
        }
        setDirty();
    }

    void onSplatterAdded(BlockPos pos, String trackingId) {
        long key = pos.asLong();
        String existing = splatterIndex.get(key);
        if (trackingId.equals(existing)) {
            return;
        }
        if (existing != null) {
            decrementGel(existing);
        }
        splatterIndex.put(key, trackingId);
        incrementGel(trackingId);
        if (existing != null) {
            tryPruneId(existing);
        }
        setDirty();
    }

    @Nullable
    String onSplatterRemoved(BlockPos pos) {
        long key = pos.asLong();
        String existing = splatterIndex.remove(key);
        if (existing != null) {
            decrementGel(existing);
            tryPruneId(existing);
            setDirty();
        }
        return existing;
    }

    void reconcileLevel(ServerLevel level) {
        revalidateSplatterIndex(level);
        reconcileStationIndex(level);
        recomputeGelCountsFromIndex();
        reconcileStationRefCountsFromIndex();
        pruneOrphans();
        setDirty();
    }

    void validateChunk(ServerLevel level, BlockPos chunkOrigin, @Nullable LevelChunk chunk) {
        if (chunk != null) {
            discoverTrackedSplattersInChunk(chunk);
        }

        int minX = chunkOrigin.getX();
        int minZ = chunkOrigin.getZ();
        int maxX = minX + 15;
        int maxZ = minZ + 15;

        Iterator<Long2ObjectMap.Entry<String>> splatterIt = splatterIndex.long2ObjectEntrySet().iterator();
        while (splatterIt.hasNext()) {
            Long2ObjectMap.Entry<String> entry = splatterIt.next();
            BlockPos pos = BlockPos.of(entry.getLongKey());
            if (pos.getX() < minX || pos.getX() > maxX || pos.getZ() < minZ || pos.getZ() > maxZ) {
                continue;
            }
            if (!reconcileSplatterIndexEntry(level, entry)) {
                splatterIt.remove();
            }
        }

        Iterator<Long2ObjectMap.Entry<String>> stationIt = stationIndex.long2ObjectEntrySet().iterator();
        while (stationIt.hasNext()) {
            Long2ObjectMap.Entry<String> entry = stationIt.next();
            BlockPos pos = BlockPos.of(entry.getLongKey());
            if (pos.getX() < minX || pos.getX() > maxX || pos.getZ() < minZ || pos.getZ() > maxZ) {
                continue;
            }
            if (!reconcileStationIndexEntry(level, entry)) {
                stationIt.remove();
            }
        }

        recomputeGelCountsFromIndex();
        reconcileStationRefCountsFromIndex();
        pruneOrphans();
        setDirty();
    }

    private void reconcileStationIndex(ServerLevel level) {
        Iterator<Long2ObjectMap.Entry<String>> it = stationIndex.long2ObjectEntrySet().iterator();
        while (it.hasNext()) {
            Long2ObjectMap.Entry<String> entry = it.next();
            BlockPos pos = BlockPos.of(entry.getLongKey());
            if (!level.isLoaded(pos)) {
                continue;
            }
            if (!reconcileStationIndexEntry(level, entry)) {
                it.remove();
            }
        }
    }

    private void revalidateSplatterIndex(ServerLevel level) {
        Iterator<Long2ObjectMap.Entry<String>> it = splatterIndex.long2ObjectEntrySet().iterator();
        while (it.hasNext()) {
            Long2ObjectMap.Entry<String> entry = it.next();
            BlockPos pos = BlockPos.of(entry.getLongKey());
            if (!level.isLoaded(pos)) {
                continue;
            }
            if (!reconcileSplatterIndexEntry(level, entry)) {
                it.remove();
            }
        }
    }

    /** Sync index entry from world/BE when valid; returns false if the position is no longer a tracked splatter. */
    private static boolean reconcileSplatterIndexEntry(ServerLevel level, Long2ObjectMap.Entry<String> entry) {
        BlockPos pos = BlockPos.of(entry.getLongKey());
        if (isValidSplatterAt(level, pos, entry.getValue())) {
            return true;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof GelSplatterBlockEntity gel && gel.hasTrackingId()) {
            entry.setValue(gel.getTrackingId());
            return isValidSplatterAt(level, pos, entry.getValue());
        }
        return false;
    }

    private void discoverTrackedSplattersInChunk(LevelChunk chunk) {
        for (BlockEntity be : chunk.getBlockEntities().values()) {
            if (be instanceof GelSplatterBlockEntity gel && gel.hasTrackingId()) {
                onSplatterAdded(gel.getBlockPos(), gel.getTrackingId());
            }
        }
    }

    private static boolean reconcileStationIndexEntry(ServerLevel level, Long2ObjectMap.Entry<String> entry) {
        BlockPos pos = BlockPos.of(entry.getLongKey());
        if (isValidStationAt(level, pos, entry.getValue())) {
            return true;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof FluidRefillStationBlockEntity station && station.hasTrackingId()) {
            entry.setValue(station.getTrackingId());
            return isValidStationAt(level, pos, entry.getValue());
        }
        return false;
    }

    private void recomputeGelCountsFromIndex() {
        for (TrackingIdStats stats : statsById.values()) {
            stats.gelBlockCount = 0;
        }
        for (String id : splatterIndex.values()) {
            getOrCreateStats(id).gelBlockCount++;
        }
    }

    private void reconcileStationRefCountsFromIndex() {
        for (TrackingIdStats stats : statsById.values()) {
            stats.stationRefCount = 0;
        }
        for (String id : stationIndex.values()) {
            getOrCreateStats(id).stationRefCount++;
        }
    }

    private void pruneOrphans() {
        statsById.entrySet().removeIf(entry -> isOrphan(entry.getKey()));
    }

    private boolean isOrphan(String trackingId) {
        TrackingIdStats stats = statsById.get(trackingId);
        if (stats == null) {
            return true;
        }
        if (stats.gelBlockCount > 0 || stats.stationRefCount > 0) {
            return false;
        }
        for (String splatterId : splatterIndex.values()) {
            if (trackingId.equals(splatterId)) {
                return false;
            }
        }
        for (String stationId : stationIndex.values()) {
            if (trackingId.equals(stationId)) {
                return false;
            }
        }
        return true;
    }

    void tryPruneId(String trackingId) {
        if (trackingId == null || trackingId.isEmpty()) {
            return;
        }
        if (isOrphan(trackingId)) {
            statsById.remove(trackingId);
            setDirty();
        }
    }

    private static boolean isValidSplatterAt(ServerLevel level, BlockPos pos, String expectedId) {
        if (!GelSplatterBlocks.is(level.getBlockState(pos))) {
            return false;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof GelSplatterBlockEntity gel)) {
            return false;
        }
        return gel.hasTrackingId() && expectedId.equals(gel.getTrackingId());
    }

    private static boolean isValidStationAt(ServerLevel level, BlockPos pos, String expectedId) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof FluidRefillStationBlockEntity station)) {
            return false;
        }
        return station.hasTrackingId() && expectedId.equals(station.getTrackingId());
    }

    private TrackingIdStats getOrCreateStats(String trackingId) {
        return statsById.computeIfAbsent(trackingId, id -> new TrackingIdStats());
    }

    private void incrementGel(String trackingId) {
        getOrCreateStats(trackingId).gelBlockCount++;
    }

    private void decrementGel(String trackingId) {
        TrackingIdStats stats = statsById.get(trackingId);
        if (stats != null && stats.gelBlockCount > 0) {
            stats.gelBlockCount--;
        }
    }

    private void incrementStationRef(String trackingId) {
        getOrCreateStats(trackingId).stationRefCount++;
    }

    private void decrementStationRef(String trackingId) {
        TrackingIdStats stats = statsById.get(trackingId);
        if (stats != null && stats.stationRefCount > 0) {
            stats.stationRefCount--;
        }
    }

    private static final class TrackingIdStats {
        int gelBlockCount;
        int stationRefCount;
    }
}
