package com.resourceful_refinement.content.pug;

import com.resourceful_refinement.ResourcefulRefinementMain;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import net.neoforged.neoforge.common.world.chunk.TicketHelper;
import net.neoforged.neoforge.common.world.chunk.TicketSet;

import java.util.Map;

public final class PugChunkLoading {
    public static final ResourceLocation CONTROLLER_ID = ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "launchpads");
    public static final TicketController TICKET_CONTROLLER = new TicketController(CONTROLLER_ID,
            PugChunkLoading::validateLoadedTickets);

    private PugChunkLoading() {}

    public static void registerTicketController(RegisterTicketControllersEvent event) {
        event.register(TICKET_CONTROLLER);
    }

    public static void ensureControllerChunk(ServerLevel level, BlockPos controllerPos) {
        ChunkPos chunk = new ChunkPos(controllerPos);
        TICKET_CONTROLLER.forceChunk(level, controllerPos, chunk.x, chunk.z, true, true);
    }

    public static void releaseControllerChunk(ServerLevel level, BlockPos controllerPos) {
        ChunkPos chunk = new ChunkPos(controllerPos);
        TICKET_CONTROLLER.forceChunk(level, controllerPos, chunk.x, chunk.z, false, true);
    }

    private static void validateLoadedTickets(ServerLevel level, TicketHelper helper) {
        PugSavedData data = PugSavedData.get(level);
        for (Map.Entry<BlockPos, TicketSet> entry : helper.getBlockTickets().entrySet()) {
            BlockPos owner = entry.getKey();
            LaunchpadId id = LaunchpadId.of(level, owner);
            if (!data.contains(id)) {
                helper.removeAllTickets(owner);
                continue;
            }
            long expectedChunk = new ChunkPos(owner).toLong();
            entry.getValue().nonTicking().forEach(chunk -> helper.removeTicket(owner, chunk, false));
            entry.getValue().ticking().stream().filter(chunk -> chunk != expectedChunk)
                    .forEach(chunk -> helper.removeTicket(owner, chunk, true));
        }
    }

    static boolean shouldRetainTicket(PugSavedData data, ServerLevel level, BlockPos owner, long chunk,
            boolean ticking) {
        return ticking && data.contains(LaunchpadId.of(level, owner)) && chunk == new ChunkPos(owner).toLong();
    }
}
