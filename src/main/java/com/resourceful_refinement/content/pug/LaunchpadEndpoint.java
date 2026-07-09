package com.resourceful_refinement.content.pug;

import com.resourceful_refinement.content.glare.GlareAddress;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Objects;

/** A snapshot of a launchpad endpoint used for exact-identity arrival validation. */
public record LaunchpadEndpoint(ResourceKey<Level> dimension, BlockPos controllerPos, GlareAddress address,
        LaunchpadMode mode) {
    public LaunchpadEndpoint {
        dimension = Objects.requireNonNull(dimension, "dimension");
        controllerPos = Objects.requireNonNull(controllerPos, "controllerPos").immutable();
        address = Objects.requireNonNull(address, "address");
        mode = Objects.requireNonNull(mode, "mode");
    }

    public boolean samePad(LaunchpadEndpoint other) {
        return dimension.equals(other.dimension) && controllerPos.equals(other.controllerPos);
    }
}
