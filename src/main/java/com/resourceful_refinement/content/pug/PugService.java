package com.resourceful_refinement.content.pug;

import com.resourceful_refinement.content.glare.GlareAddress;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class PugService {
    private PugService() {}

    public static void syncController(LaunchpadControllerBlockEntity controller) {
        if (!(controller.getLevel() instanceof ServerLevel level)) return;
        LaunchpadId id = LaunchpadId.of(level, controller.getBlockPos());
        if (!controller.isAssembled()) {
            unregister(level, id);
            return;
        }
        LaunchpadConfiguration configuration = controller.getConfiguration();
        PugSavedData.PadRecord record = new PugSavedData.PadRecord(id, configuration.mode(),
                configuration.localAddress(), controller.hasClearSky());
        PugSavedData.get(level).upsert(record);
        PugChunkLoading.ensureControllerChunk(level, controller.getBlockPos());
    }

    public static void unregister(ServerLevel level, BlockPos controllerPos) {
        unregister(level, LaunchpadId.of(level, controllerPos));
    }

    private static void unregister(ServerLevel level, LaunchpadId id) {
        PugSavedData.get(level).remove(id);
        PugChunkLoading.releaseControllerChunk(level, id.controllerPos());
    }

    public static PugDestinationResult resolveDestination(MinecraftServer server, GlareAddress address) {
        if (!address.isComplete()) return PugDestinationResult.of(PugDestinationResult.Status.INCOMPLETE_ADDRESS);
        List<PugSavedData.PadRecord> candidates = PugSavedData.get(server).findReceivers(address);
        if (candidates.isEmpty()) return PugDestinationResult.of(PugDestinationResult.Status.MISSING);
        if (candidates.size() > 1) {
            return new PugDestinationResult(PugDestinationResult.Status.AMBIGUOUS, null, candidates.size());
        }

        PugSavedData.PadRecord record = candidates.getFirst();
        ServerLevel destinationLevel = server.getLevel(record.id().dimension());
        if (destinationLevel == null) {
            return new PugDestinationResult(PugDestinationResult.Status.ENDPOINT_UNAVAILABLE, null, 1);
        }
        PugChunkLoading.ensureControllerChunk(destinationLevel, record.id().controllerPos());
        destinationLevel.getChunk(record.id().controllerPos());
        BlockEntity blockEntity = destinationLevel.getBlockEntity(record.id().controllerPos());
        if (!(blockEntity instanceof LaunchpadControllerBlockEntity controller)
                || !controller.isAssembled() || !controller.validateAssembly()
                || !controller.getConfiguration().mode().canReceive()
                || !controller.getConfiguration().localAddress().equals(address)) {
            if (blockEntity instanceof LaunchpadControllerBlockEntity controller) syncController(controller);
            else unregister(destinationLevel, record.id());
            return new PugDestinationResult(PugDestinationResult.Status.ENDPOINT_UNAVAILABLE, null, 1);
        }
        boolean skyClear = controller.hasClearSky();
        syncController(controller);
        if (!skyClear) {
            return new PugDestinationResult(PugDestinationResult.Status.SKY_OBSTRUCTED, null, 1);
        }
        return new PugDestinationResult(PugDestinationResult.Status.FOUND,
                new LaunchpadEndpoint(destinationLevel.dimension(), controller.getBlockPos(), address,
                        controller.getConfiguration().mode()), 1);
    }

    public static void reconcileLevel(ServerLevel level) {
        PugSavedData data = PugSavedData.get(level);
        for (PugSavedData.PadRecord record : data.getPadsInDimension(level.dimension())) {
            PugChunkLoading.ensureControllerChunk(level, record.id().controllerPos());
            level.getChunk(record.id().controllerPos());
            reconcileRecord(level, record);
        }
    }

    public static void reconcileChunk(ServerLevel level, LevelChunk chunk) {
        PugSavedData data = PugSavedData.get(level);
        Set<BlockPos> liveControllers = new HashSet<>();
        for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
            if (blockEntity instanceof LaunchpadControllerBlockEntity controller) {
                liveControllers.add(controller.getBlockPos());
                if (controller.isAssembled() && controller.validateAssembly()) syncController(controller);
                else unregister(level, controller.getBlockPos());
            }
        }
        ChunkPos chunkPos = chunk.getPos();
        for (PugSavedData.PadRecord record : data.getPadsInDimension(level.dimension())) {
            if (new ChunkPos(record.id().controllerPos()).equals(chunkPos)
                    && !liveControllers.contains(record.id().controllerPos())) {
                unregister(level, record.id());
            }
        }
    }

    private static void reconcileRecord(ServerLevel level, PugSavedData.PadRecord record) {
        BlockEntity blockEntity = level.getBlockEntity(record.id().controllerPos());
        if (blockEntity instanceof LaunchpadControllerBlockEntity controller
                && controller.isAssembled() && controller.validateAssembly()) {
            syncController(controller);
        } else {
            unregister(level, record.id());
        }
    }
}
