package com.resourceful_refinement.content.glare;

public interface IGlareReceiver {
    int getAllocatedLux();

    GlareOperationStatus getGlareOperationStatus();

    void setGlareOperationStatus(GlareOperationStatus status);

    default void applyGlareOperationStatusFromNetwork(GlareOperationStatus status) {
        setGlareOperationStatus(status);
    }
}
