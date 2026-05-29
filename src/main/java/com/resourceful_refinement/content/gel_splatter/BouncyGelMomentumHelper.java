package com.resourceful_refinement.content.gel_splatter;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks horizontal momentum while airborne so bouncy gel can restore speed after
 * {@link Entity#move} collision scrubs {@code deltaMovement} before {@code entityInside} runs.
 */
final class BouncyGelMomentumHelper {

    private static final Map<Integer, Vec3> AIRBORNE_VELOCITY = new ConcurrentHashMap<>();

    private BouncyGelMomentumHelper() {}

    static void trackAirborne(Entity entity, Vec3 velocity) {
        if (entity.level().isClientSide()) {
            return;
        }
        AIRBORNE_VELOCITY.put(entity.getId(), velocity);
    }

    static void clear(Entity entity) {
        AIRBORNE_VELOCITY.remove(entity.getId());
    }

    /**
     * Returns horizontal components to use for a bounce — prefers the stronger of current
     * velocity and the last tracked airborne velocity.
     */
    static Vec3 resolveHorizontalForBounce(Entity entity, Vec3 currentVelocity) {
        Vec3 tracked = AIRBORNE_VELOCITY.remove(entity.getId());
        if (tracked == null) {
            return new Vec3(currentVelocity.x, 0.0D, currentVelocity.z);
        }

        double currentHSq = currentVelocity.horizontalDistanceSqr();
        double trackedHSq = tracked.horizontalDistanceSqr();
        if (trackedHSq > currentHSq + 1.0E-4) {
            return new Vec3(tracked.x, 0.0D, tracked.z);
        }
        return new Vec3(currentVelocity.x, 0.0D, currentVelocity.z);
    }
}
