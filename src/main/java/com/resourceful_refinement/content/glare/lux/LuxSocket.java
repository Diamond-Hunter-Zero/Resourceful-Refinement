package com.resourceful_refinement.content.glare.lux;

import com.resourceful_refinement.content.glare.IGlareNode;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

/**
 * Face-sensitive Lux access point for machines that consume GLARE network Lux through a Lux Transceiver.
 */
public interface LuxSocket {
    @Nullable
    IGlareNode getLuxSocketNode(Direction side);

    default boolean isLuxSocketEnabled(Direction side) {
        return getLuxSocketNode(side) != null;
    }
}
