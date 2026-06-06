package com.resourceful_refinement.content.gel_tracking;

import com.resourceful_refinement.content.gel_splatter.GelSplatterBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Breaks gel splatters for a tracking ID in batches so redstone resets do not spike the server.
 */
public final class GelTrackingPurgeScheduler {

    /** Max splatter blocks removed per dimension per server tick. */
    public static final int BATCH_SIZE = 48;

    private static final Map<ResourceKey<Level>, PurgeQueue> QUEUES = new HashMap<>();

    private GelTrackingPurgeScheduler() {}

    public static void schedulePurge(ServerLevel level, String trackingId) {
        if (trackingId == null || trackingId.isEmpty()) {
            return;
        }
        queueFor(level).enqueue(level, trackingId);
    }

    public static void tick(ServerLevel level) {
        queueFor(level).tick(level);
    }

    private static PurgeQueue queueFor(ServerLevel level) {
        return QUEUES.computeIfAbsent(level.dimension(), key -> new PurgeQueue());
    }

    private static final class PurgeQueue {
        private final ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        private final Set<BlockPos> queued = new HashSet<>();
        @Nullable
        private String trackingId;

        void enqueue(ServerLevel level, String trackingId) {
            this.trackingId = trackingId;
            for (BlockPos pos : GelTrackingSavedData.get(level).getSplatterPositions(trackingId)) {
                if (queued.add(pos)) {
                    pending.addLast(pos);
                }
            }
        }

        void tick(ServerLevel level) {
            if (pending.isEmpty()) {
                finishIfDone(level);
                return;
            }

            int budget = BATCH_SIZE;
            while (budget-- > 0 && !pending.isEmpty()) {
                BlockPos pos = pending.peekFirst();
                if (pos == null) {
                    break;
                }
                if (!level.isLoaded(pos)) {
                    pending.removeFirst();
                    pending.addLast(pos);
                    continue;
                }
                pending.removeFirst();
                queued.remove(pos);
                breakSplatter(level, pos);
            }

            finishIfDone(level);
        }

        private void finishIfDone(ServerLevel level) {
            if (!pending.isEmpty() || trackingId == null) {
                return;
            }
            GelTrackingSavedData.get(level).tryPruneId(trackingId);
            trackingId = null;
        }

        private static void breakSplatter(ServerLevel level, BlockPos pos) {
            if (!GelSplatterBlocks.is(level.getBlockState(pos))) {
                GelTrackingService.onSplatterRemoved(level, pos);
                return;
            }
            level.removeBlock(pos, false);
        }
    }
}
