package com.resourceful_refinement.content.glare;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.network.GlareLinkSyncPayload;
import com.resourceful_refinement.config.ServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = ResourcefulRefinementMain.MOD_ID)
public final class ModGlareEvents {
    private ModGlareEvents() {}

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel) || !(event.getChunk() instanceof LevelChunk chunk)) {
            return;
        }
        BlockPos origin = new BlockPos(chunk.getPos().getMinBlockX(), serverLevel.getMinBuildHeight(), chunk.getPos().getMinBlockZ());
        int runAt = serverLevel.getServer().getTickCount() + 1;
        serverLevel.getServer().tell(new net.minecraft.server.TickTask(runAt, () -> {
            GlareService.reconcileLoadedChunk(serverLevel, origin);
            GlareService.validateLoadedLinks(serverLevel, ServerConfig.GLARE_LOS_CHECKS_PER_TICK.get());
        }));
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        BlockPos origin = new BlockPos(event.getChunk().getPos().getMinBlockX(), serverLevel.getMinBuildHeight(),
                event.getChunk().getPos().getMinBlockZ());
        GlareService.markChunkUnloaded(serverLevel, origin);
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        GlareService.validateLoadedLinks(serverLevel, ServerConfig.GLARE_LOS_CHECKS_PER_TICK.get());
        if (serverLevel == serverLevel.getServer().overworld()) {
            GlareService.sampleLuxHistories(serverLevel.getServer(),
                    ServerConfig.GLARE_LUX_HISTORY_SAMPLE_INTERVAL.get(),
                    ServerConfig.GLARE_LUX_HISTORY_SAMPLES.get());
        }
        if (serverLevel.getGameTime() % ServerConfig.GLARE_LINK_SYNC_INTERVAL.get() != 0L) return;
        for (ServerPlayer player : serverLevel.players()) {
            PacketDistributor.sendToPlayer(player, GlareLinkSyncPayload.of(serverLevel.dimension().location(), GlareService.getRenderableLinks(player)));
        }
    }
}
