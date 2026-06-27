package com.resourceful_refinement.content.glare;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public interface IGlareNode {
    int getMaxGlareLinks();

    default Vec3 getGlareLinkEndpoint() {
        BlockPos pos = getGlareNodePos().pos();
        return Vec3.atCenterOf(pos);
    }

    DimensionalNodePos getGlareNodePos();

    default void onGlareNetworkChanged(ServerLevel level, UUID networkId) {}

    default void onGlareLinksChanged(ServerLevel level) {}
}
