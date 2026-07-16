package com.resourceful_refinement.content.manifold;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class ManifoldAssemblySession {
    public static final int DEFAULT_DURATION = 40;

    private ManifoldAssemblySession() {
    }

    public static boolean canApply(Level level, BlockPos pos, ManifoldAssemblyAction action) {
        return level.getBlockEntity(pos) instanceof ManifoldBlockEntity manifold
                && !action.apply(manifold.assemblyRecord()).equals(manifold.assemblyRecord());
    }

    public static boolean apply(Level level, BlockPos pos, ManifoldAssemblyAction action) {
        return level.getBlockEntity(pos) instanceof ManifoldBlockEntity manifold
                && manifold.applyAssemblyAction(action);
    }

    public static void holdTarget(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof ManifoldBlockEntity manifold) {
            manifold.holdForAssemblyTicks(2);
        }
    }

    public static Direction worldFaceToLocalFace(BlockState manifoldState, Direction worldFace) {
        if (!manifoldState.hasProperty(DirectionalBlock.FACING)) {
            return worldFace;
        }

        Direction localNorth = manifoldState.getValue(DirectionalBlock.FACING);
        Direction localUp = localNorth.getAxis() == Direction.Axis.Y ? Direction.NORTH : Direction.UP;
        Direction localEast = cross(localNorth, localUp);

        if (worldFace == localNorth) {
            return Direction.NORTH;
        }
        if (worldFace == localNorth.getOpposite()) {
            return Direction.SOUTH;
        }
        if (worldFace == localUp) {
            return Direction.UP;
        }
        if (worldFace == localUp.getOpposite()) {
            return Direction.DOWN;
        }
        if (worldFace == localEast) {
            return Direction.EAST;
        }
        if (worldFace == localEast.getOpposite()) {
            return Direction.WEST;
        }
        return worldFace;
    }

    private static Direction cross(Direction first, Direction second) {
        int x = first.getStepY() * second.getStepZ() - first.getStepZ() * second.getStepY();
        int y = first.getStepZ() * second.getStepX() - first.getStepX() * second.getStepZ();
        int z = first.getStepX() * second.getStepY() - first.getStepY() * second.getStepX();
        return Direction.fromDelta(x, y, z);
    }
}
