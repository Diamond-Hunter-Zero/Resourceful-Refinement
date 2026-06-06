package com.resourceful_refinement.content.gel_tracking;

import com.resourceful_refinement.content.refill_station.FluidRefillStationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public final class GelTrackingService {

    private GelTrackingService() {}

    public static int getGelCount(Level level, String trackingId) {
        if (!(level instanceof ServerLevel server) || trackingId == null || trackingId.isEmpty()) {
            return 0;
        }
        return GelTrackingSavedData.get(server).getGelCount(trackingId);
    }

    public static int getGelCountForStation(FluidRefillStationBlockEntity station) {
        if (station.getLevel() == null || !station.hasTrackingId()) {
            return 0;
        }
        return getGelCount(station.getLevel(), station.getTrackingId());
    }

    public static List<String> getAllTrackingIds(ServerLevel level) {
        return GelTrackingSavedData.get(level).getAllTrackingIds();
    }

    public static Set<BlockPos> getSplatterPositions(ServerLevel level, String trackingId) {
        if (trackingId == null || trackingId.isEmpty()) {
            return Set.of();
        }
        return GelTrackingSavedData.get(level).getSplatterPositions(trackingId);
    }

    public static void onStationTrackingIdChanged(ServerLevel level, BlockPos pos, String oldId, String newId) {
        GelTrackingSavedData data = GelTrackingSavedData.get(level);
        data.unregisterStation(pos);
        if (newId != null && !newId.isEmpty()) {
            data.registerStation(pos, newId);
        }
        if (oldId != null && !oldId.isEmpty()) {
            data.tryPruneId(oldId);
        }
    }

    public static void onStationLoaded(ServerLevel level, BlockPos pos, String trackingId) {
        if (trackingId == null || trackingId.isEmpty()) {
            return;
        }
        GelTrackingSavedData.get(level).registerStation(pos, trackingId);
    }

    public static void onStationRemoved(ServerLevel level, BlockPos pos) {
        GelTrackingSavedData.get(level).unregisterStation(pos);
    }

    public static void onSplatterAdded(ServerLevel level, BlockPos pos, String trackingId) {
        if (trackingId == null || trackingId.isEmpty()) {
            return;
        }
        GelTrackingSavedData.get(level).onSplatterAdded(pos, trackingId);
    }

    public static void onSplatterRemoved(@Nullable LevelAccessor level, BlockPos pos) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        GelTrackingSavedData.get(server).onSplatterRemoved(pos);
    }

    public static void reconcileLevel(ServerLevel level) {
        GelTrackingSavedData.get(level).reconcileLevel(level);
    }

    public static void validateChunk(ServerLevel level, BlockPos chunkOrigin, @Nullable LevelChunk chunk) {
        GelTrackingSavedData.get(level).validateChunk(level, chunkOrigin, chunk);
    }

    /** Queues indexed gel splatters for batched removal (e.g. refill station redstone reset). */
    public static void scheduleGelPurge(ServerLevel level, String trackingId) {
        GelTrackingPurgeScheduler.schedulePurge(level, trackingId);
    }

    public static void tickPurgeQueues(ServerLevel level) {
        GelTrackingPurgeScheduler.tick(level);
    }
}
