package com.resourceful_refinement.utilities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.Collection;

/**
 * Notifies the blocks around a multiblock stack that its exposed capabilities may have changed.
 * <p>
 * Create pipes decide what an adjacent face is (fluid handler, other pipe, or "blocked") once, and keep
 * that until a neighbour update or pressure change. If our capability returned null at that moment
 * (mid-restructure, or before the stack resolved on load), the pipe stays blocked. Capability
 * invalidation alone doesn't reach it, so the pipes must also get a neighbour update.
 * <p>
 * Only blocks of other types are notified: notifying the stack's own blocks would re-run their
 * connectivity update and loop.
 */
public final class StackNeighbourNotifier {

    private StackNeighbourNotifier() {}

    public static void notifyExternalNeighbours(Level level, Collection<BlockPos> stack, Block stackBlock) {
        if (level == null || level.isClientSide) return;

        for (BlockPos pos : stack) {
            level.invalidateCapabilities(pos);
            for (Direction direction : Direction.values()) {
                BlockPos neighbour = pos.relative(direction);
                if (stack.contains(neighbour) || level.getBlockState(neighbour).is(stackBlock)) continue;
                if (!level.isLoaded(neighbour)) continue;
                level.neighborChanged(neighbour, stackBlock, pos);
            }
        }
    }
}
