package com.resourceful_refinement.content.pug;

import com.resourceful_refinement.ResourcefulRefinementMain;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = ResourcefulRefinementMain.MOD_ID)
public final class ModPugEvents {
    private ModPugEvents() {}

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        int runAt = level.getServer().getTickCount() + 1;
        level.getServer().tell(new net.minecraft.server.TickTask(runAt, () -> PugService.reconcileLevel(level)));
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getChunk() instanceof LevelChunk chunk)) return;
        int runAt = level.getServer().getTickCount() + 1;
        level.getServer().tell(new net.minecraft.server.TickTask(runAt, () -> PugService.reconcileChunk(level, chunk)));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        PugFlightService.tick(event.getServer());
    }
}
