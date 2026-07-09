package com.resourceful_refinement.content.pug;

public enum PugFlightState {
    DOCKED,
    ASCENDING,
    IN_TRANSIT,
    QUEUED,
    DESCENDING,
    UNLOADING,
    CRASHED;

    public boolean isSimulated() {
        return this == IN_TRANSIT || this == QUEUED;
    }

    public boolean isPhysical() {
        return !isSimulated();
    }
}
