package com.resourceful_refinement.content.gel_tracking;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.gel_splatter.GelSplatterBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = ResourcefulRefinementMain.MOD_ID)
public final class ModGelTrackingEvents {

    private ModGelTrackingEvents() {}

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        LevelAccessor level = event.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        // Defer until block entities from loaded chunks have finished loading from disk.
        int runAt = serverLevel.getServer().getTickCount() + 1;
        serverLevel.getServer().tell(new net.minecraft.server.TickTask(runAt, () -> GelTrackingService.reconcileLevel(serverLevel)));
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!(event.getChunk() instanceof LevelChunk chunk)) {
            return;
        }
        BlockPos origin = new BlockPos(chunk.getPos().getMinBlockX(), serverLevel.getMinBuildHeight(), chunk.getPos().getMinBlockZ());
        int runAt = serverLevel.getServer().getTickCount() + 1;
        serverLevel.getServer().tell(new net.minecraft.server.TickTask(runAt, () -> GelTrackingService.validateChunk(serverLevel, origin, chunk)));
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            GelTrackingService.tickPurgeQueues(serverLevel);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (!GelSplatterBlocks.is(event.getState())) {
            return;
        }
        GelTrackingService.onSplatterRemoved(event.getLevel(), event.getPos());
    }
}
