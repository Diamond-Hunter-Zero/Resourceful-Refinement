package com.resourceful_refinement.content.conveyor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ConveyorBlockMover {
    private ConveyorBlockMover() {
    }

    public static final int FASTEST_TICK_INTERVAL = 2;
    public static final int FAST_TICK_INTERVAL = 4;
    public static final int MEDIUM_TICK_INTERVAL = 6;
    public static final int SLOW_TICK_INTERVAL = 8;

    public static int movementIntervalForSpeed(float speed) {
        float absSpeed = Math.abs(speed);
        if (absSpeed >= 128) {
            return FASTEST_TICK_INTERVAL;
        }
        if (absSpeed >= 64) {
            return FAST_TICK_INTERVAL;
        }
        if (absSpeed >= 16) {
            return MEDIUM_TICK_INTERVAL;
        }
        return SLOW_TICK_INTERVAL;
    }

    public static Direction directionForSpeed(Direction positiveDirection, float speed) {
        return speed < 0 ? positiveDirection.getOpposite() : positiveDirection;
    }

    public static ConveyorMovementResult tryMoveBlockAbove(Level level, BlockPos conveyorPos, Direction movementDirection) {
        return tryMoveBlock(level, conveyorPos.above(), movementDirection);
    }

    public static ConveyorMovementResult tryMoveBlock(Level level, BlockPos sourcePos, Direction movementDirection) {
        BlockPos destinationPos = sourcePos.relative(movementDirection);

        if (level.isClientSide) {
            return ConveyorMovementResult.blocked(ConveyorMovementResult.FailureReason.CLIENT_LEVEL, sourcePos,
                    destinationPos, Blocks.AIR.defaultBlockState());
        }
        if (!level.isLoaded(sourcePos) || !level.isLoaded(destinationPos)) {
            return ConveyorMovementResult.blocked(ConveyorMovementResult.FailureReason.UNLOADED_POSITION, sourcePos,
                    destinationPos, Blocks.AIR.defaultBlockState());
        }

        BlockState sourceState = level.getBlockState(sourcePos);
        if (sourceState.isAir()) {
            return ConveyorMovementResult.blocked(ConveyorMovementResult.FailureReason.NO_SOURCE_BLOCK, sourcePos,
                    destinationPos, sourceState);
        }

        ConveyorMovementResult.FailureReason failureReason = getMovementFailure(level, sourcePos, destinationPos,
                sourceState, movementDirection);
        if (failureReason != ConveyorMovementResult.FailureReason.NONE) {
            return ConveyorMovementResult.blocked(failureReason, sourcePos, destinationPos, sourceState);
        }

        BlockState destinationState = level.getBlockState(destinationPos);
        if (!destinationState.isAir()) {
            level.destroyBlock(destinationPos, false);
        }

        boolean placed = level.setBlock(destinationPos, sourceState, Block.UPDATE_ALL);
        if (!placed) {
            return ConveyorMovementResult.blocked(ConveyorMovementResult.FailureReason.DESTINATION_BLOCKED, sourcePos,
                    destinationPos, sourceState);
        }

        level.setBlock(sourcePos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        return ConveyorMovementResult.moved(sourcePos, destinationPos, sourceState);
    }

    public static ConveyorMovementResult.FailureReason getMovementFailure(Level level, BlockPos sourcePos,
                                                                          Direction movementDirection) {
        BlockPos destinationPos = sourcePos.relative(movementDirection);
        if (!level.isLoaded(sourcePos) || !level.isLoaded(destinationPos)) {
            return ConveyorMovementResult.FailureReason.UNLOADED_POSITION;
        }
        BlockState sourceState = level.getBlockState(sourcePos);
        if (sourceState.isAir()) {
            return ConveyorMovementResult.FailureReason.NO_SOURCE_BLOCK;
        }
        return getMovementFailure(level, sourcePos, destinationPos, sourceState, movementDirection);
    }

    public static ConveyorMovementResult.FailureReason getSourceMovementFailure(Level level, BlockPos sourcePos,
                                                                                Direction movementDirection) {
        if (!level.isLoaded(sourcePos)) {
            return ConveyorMovementResult.FailureReason.UNLOADED_POSITION;
        }

        BlockState sourceState = level.getBlockState(sourcePos);
        if (sourceState.isAir()) {
            return ConveyorMovementResult.FailureReason.NO_SOURCE_BLOCK;
        }

        return getSourceMovementFailure(level, sourcePos, sourceState, movementDirection);
    }

    private static ConveyorMovementResult.FailureReason getMovementFailure(Level level, BlockPos sourcePos,
                                                                           BlockPos destinationPos,
                                                                           BlockState sourceState,
                                                                           Direction movementDirection) {
        BlockState destinationState = level.getBlockState(destinationPos);
        if (!destinationState.isAir() && !destinationState.canBeReplaced()) {
            return ConveyorMovementResult.FailureReason.DESTINATION_BLOCKED;
        }

        return getSourceMovementFailure(level, sourcePos, sourceState, movementDirection);
    }

    private static ConveyorMovementResult.FailureReason getSourceMovementFailure(Level level, BlockPos sourcePos,
                                                                                 BlockState sourceState,
                                                                                 Direction movementDirection) {
        PushReaction reaction = sourceState.getPistonPushReaction();
        if (reaction == PushReaction.BLOCK) {
            return ConveyorMovementResult.FailureReason.PUSH_REACTION_BLOCK;
        }
        if (reaction == PushReaction.DESTROY) {
            return ConveyorMovementResult.FailureReason.PUSH_REACTION_DESTROY;
        }

        if (level.getBlockEntity(sourcePos) != null) {
            return ConveyorMovementResult.FailureReason.BLOCK_ENTITY;
        }

        if (!PistonBaseBlock.isPushable(sourceState, level, sourcePos, movementDirection, false, movementDirection)) {
            return ConveyorMovementResult.FailureReason.PISTON_RULE;
        }

        return ConveyorMovementResult.FailureReason.NONE;
    }

    public static ConveyorLineMovementResult processLineFrontToBack(Level level, List<BlockPos> conveyorPositions,
                                                                    Direction movementDirection) {
        List<BlockPos> sortedPositions = conveyorPositions.stream()
                .sorted(frontToBackComparator(movementDirection))
                .toList();
        return processLineInOrder(level, sortedPositions, movementDirection);
    }

    public static ConveyorLineMovementResult processLineInOrder(Level level, List<BlockPos> conveyorPositions,
                                                                Direction movementDirection) {
        List<ConveyorMovementResult> results = new ArrayList<>();
        int successfulMoves = 0;

        for (BlockPos conveyorPos : conveyorPositions) {
            ConveyorMovementResult result = tryMoveBlockAbove(level, conveyorPos, movementDirection);
            results.add(result);
            if (result.moved()) {
                successfulMoves++;
            }
        }

        return new ConveyorLineMovementResult(results.size(), successfulMoves, List.copyOf(results));
    }

    private static Comparator<BlockPos> frontToBackComparator(Direction movementDirection) {
        return Comparator.comparingInt((BlockPos pos) -> movementDirection.getAxis()
                        .choose(pos.getX(), pos.getY(), pos.getZ()) * movementDirection.getAxisDirection().getStep())
                .reversed();
    }
}
