package com.resourceful_refinement.network;

import com.resourceful_refinement.content.glare.GlareDebug;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class ServerPayloadGuard {
    public static final double DEFAULT_USE_DISTANCE_SQUARED = 64.0D;

    private ServerPayloadGuard() {}

    public static <T extends BlockEntity> T loadedNearbyBlockEntity(ServerPlayer player, BlockPos pos,
            Class<T> expectedType) {
        if (!player.level().isLoaded(pos) || player.distanceToSqr(pos.getCenter()) > DEFAULT_USE_DISTANCE_SQUARED) {
            GlareDebug.log("Rejected {} payload access from {} at {}", expectedType.getSimpleName(),
                    player.getGameProfile().getName(), pos.toShortString());
            return null;
        }
        BlockEntity blockEntity = player.level().getBlockEntity(pos);
        return expectedType.isInstance(blockEntity) ? expectedType.cast(blockEntity) : null;
    }
}
