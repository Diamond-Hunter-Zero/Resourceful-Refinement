package com.resourceful_refinement.content.paint_nozzle;

import net.minecraft.network.chat.Component;

public enum PaintNozzleFlowSpeed {
    LOW(0.2F),
    MEDIUM(0.7F),
    HIGH(1.15F);

    public static final PaintNozzleFlowSpeed DEFAULT = MEDIUM;

    private final float velocityFactor;

    PaintNozzleFlowSpeed(float velocityFactor) {
        this.velocityFactor = velocityFactor;
    }

    public float getVelocityFactor() {
        return velocityFactor;
    }

    public PaintNozzleFlowSpeed next() {
        PaintNozzleFlowSpeed[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public Component displayName() {
        return Component.translatable("paint_nozzle.flow_speed." + name().toLowerCase());
    }

    public static PaintNozzleFlowSpeed fromOrdinal(int ordinal) {
        PaintNozzleFlowSpeed[] values = values();
        if (ordinal < 0 || ordinal >= values.length) {
            return DEFAULT;
        }
        return values[ordinal];
    }
}
