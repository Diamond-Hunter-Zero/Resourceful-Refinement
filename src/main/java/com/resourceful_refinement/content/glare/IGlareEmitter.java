package com.resourceful_refinement.content.glare;

import net.minecraft.world.item.DyeColor;

public interface IGlareEmitter {
    int getProducedLux();

    default DyeColor getLuxColourCharge() {
        return DyeColor.WHITE;
    }

    default boolean isGlareEmitterEnabled() {
        return true;
    }
}
