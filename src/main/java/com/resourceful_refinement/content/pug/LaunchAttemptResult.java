package com.resourceful_refinement.content.pug;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record LaunchAttemptResult(boolean launched, LaunchpadFailureReason failureReason,
        @Nullable UUID pugId, int fuelConsumedMb) {
    public static LaunchAttemptResult failed(LaunchpadFailureReason reason) {
        return new LaunchAttemptResult(false, reason, null, 0);
    }

    public static LaunchAttemptResult launched(UUID pugId, int fuelConsumedMb) {
        return new LaunchAttemptResult(true, LaunchpadFailureReason.NONE, pugId, fuelConsumedMb);
    }
}
