package com.resourceful_refinement.content.glare;

/** A GLARE node that owns an address reachable by telemetry messages. */
public interface IGlareTelemetryEndpoint {
    GlareAddress getTelemetryAddress();
}
