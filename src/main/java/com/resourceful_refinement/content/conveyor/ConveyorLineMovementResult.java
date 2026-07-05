package com.resourceful_refinement.content.conveyor;

import java.util.List;

public record ConveyorLineMovementResult(
        int attemptedMoves,
        int successfulMoves,
        List<ConveyorMovementResult> results
) {
    public boolean movedAny() {
        return successfulMoves > 0;
    }
}
