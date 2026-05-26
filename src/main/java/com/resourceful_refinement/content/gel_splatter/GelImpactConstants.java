package com.resourceful_refinement.content.gel_splatter;

import net.minecraft.core.BlockPos;

/**
 * Shared impact radii for gel-blob block effects (particles, paint/cleanse sweeps, splatter spread).
 */
public final class GelImpactConstants {

    /** Paint and cleanse gel-blobs (dye/clear area). */
    public static final float IMPACT_RADIUS_LARGE = 3.0F;

    /** All other gel-blobs (splatter placement and splash). */
    public static final float IMPACT_RADIUS_SMALL = 1.5F;

    private GelImpactConstants() {}

    public static float getImpactRadius(GelType type) {
        return switch (type) {
            case PAINT, CLEANSE -> IMPACT_RADIUS_LARGE;
            default -> IMPACT_RADIUS_SMALL;
        };
    }

    /** Integer half-extent for iterating a cube before spherical filtering. */
    public static int getBlockSearchExtent(float radius) {
        return (int) Math.ceil(radius);
    }

    public static boolean isWithinImpactSphere(BlockPos center, BlockPos pos, float radius) {
        int dx = pos.getX() - center.getX();
        int dy = pos.getY() - center.getY();
        int dz = pos.getZ() - center.getZ();
        float radiusSq = radius * radius;
        return dx * dx + dy * dy + dz * dz <= radiusSq;
    }
}
