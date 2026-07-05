package com.resourceful_refinement.content.conveyor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public record ConveyorMovementResult(
        boolean moved,
        FailureReason failureReason,
        BlockPos sourcePos,
        BlockPos destinationPos,
        BlockState movedState
) {
    public static ConveyorMovementResult moved(BlockPos sourcePos, BlockPos destinationPos, BlockState movedState) {
        return new ConveyorMovementResult(true, FailureReason.NONE, sourcePos, destinationPos, movedState);
    }

    public static ConveyorMovementResult blocked(FailureReason failureReason, BlockPos sourcePos, BlockPos destinationPos,
                                                 BlockState movedState) {
        return new ConveyorMovementResult(false, failureReason, sourcePos, destinationPos, movedState);
    }

    public enum FailureReason {
        NONE,
        CLIENT_LEVEL,
        UNLOADED_POSITION,
        NO_SOURCE_BLOCK,
        DESTINATION_BLOCKED,
        PUSH_REACTION_BLOCK,
        PUSH_REACTION_DESTROY,
        BLOCK_ENTITY,
        PISTON_RULE
    }
}
