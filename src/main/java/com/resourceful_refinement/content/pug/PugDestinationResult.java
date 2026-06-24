package com.resourceful_refinement.content.pug;

import org.jetbrains.annotations.Nullable;

public record PugDestinationResult(Status status, @Nullable LaunchpadEndpoint endpoint, int matchingPads) {
    public static PugDestinationResult of(Status status) {
        return new PugDestinationResult(status, null, 0);
    }

    public boolean found() {
        return status == Status.FOUND && endpoint != null;
    }

    public enum Status {
        FOUND,
        INCOMPLETE_ADDRESS,
        MISSING,
        AMBIGUOUS,
        SKY_OBSTRUCTED,
        ENDPOINT_UNAVAILABLE
    }
}
