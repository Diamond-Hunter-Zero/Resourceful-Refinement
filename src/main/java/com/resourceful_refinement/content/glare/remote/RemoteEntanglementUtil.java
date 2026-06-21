package com.resourceful_refinement.content.glare.remote;

import com.resourceful_refinement.content.glare.GlareSavedData;
import com.resourceful_refinement.content.glare.GlareService;
import com.resourceful_refinement.content.glare.GlareNodePos;
import com.resourceful_refinement.utilities.heating.HeatUtilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import java.util.UUID;

public final class RemoteEntanglementUtil {
    private RemoteEntanglementUtil() {}

    public static boolean isChilled(ServerLevel level, BlockPos... machineParts) {
        for (BlockPos part : machineParts) {
            for (Direction direction : Direction.values()) {
                if (HeatUtilities.GetExtendedHeatLevel(level, part.relative(direction)) <= -3) return true;
            }
        }
        return false;
    }

    public static boolean isOperational(ServerLevel level, GlareNodePos pos) {
        GlareSavedData.NodeRecord node = GlareService.getNode(level, pos).orElse(null);
        if (node == null || node.networkId == null) return false;
        GlareSavedData.NetworkRecord network = GlareService.getNetwork(level, node.networkId).orElse(null);
        return network != null && node.status == com.resourceful_refinement.content.glare.GlareOperationStatus.ONLINE
                && !network.overloaded && network.luxAllocated <= network.luxCapacity;
    }

    public static UUID networkId(ServerLevel level, GlareNodePos pos) {
        return GlareService.getNode(level, pos).map(node -> node.networkId).orElse(null);
    }
}
