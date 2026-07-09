package com.resourceful_refinement.content.pug;

import com.resourceful_refinement.content.glare.GlareAddress;

import java.util.Objects;

public record LaunchpadConfiguration(LaunchpadMode mode, GlareAddress localAddress, GlareAddress destinationAddress,
        LaunchCondition launchCondition, int timerSeconds) {
    public static final int MAX_TIMER_SECONDS = 3_600;

    public LaunchpadConfiguration {
        mode = Objects.requireNonNull(mode, "mode");
        localAddress = Objects.requireNonNull(localAddress, "localAddress");
        destinationAddress = Objects.requireNonNull(destinationAddress, "destinationAddress");
        launchCondition = Objects.requireNonNull(launchCondition, "launchCondition");
        if (timerSeconds < 0 || timerSeconds > MAX_TIMER_SECONDS) {
            throw new IllegalArgumentException("timerSeconds must be between 0 and " + MAX_TIMER_SECONDS);
        }
    }

    public static LaunchpadConfiguration defaults() {
        return new LaunchpadConfiguration(LaunchpadMode.SEND, GlareAddress.empty(), GlareAddress.empty(),
                LaunchCondition.WHEN_FULL, 120);
    }
}
