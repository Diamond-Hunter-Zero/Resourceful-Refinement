package com.resourceful_refinement.content.glare;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.network.GlareLinkSyncPayload;
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
            GlareService.validateLoadedLinks(serverLevel);
        }));
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel) || serverLevel.getGameTime() % 20L != 0L) {
            return;
        }
        GlareService.validateLoadedLinks(serverLevel);
        for (ServerPlayer player : serverLevel.players()) {
            PacketDistributor.sendToPlayer(player, GlareLinkSyncPayload.of(serverLevel.dimension().location(), GlareService.getRenderableLinks(player)));
        }
    }
}
