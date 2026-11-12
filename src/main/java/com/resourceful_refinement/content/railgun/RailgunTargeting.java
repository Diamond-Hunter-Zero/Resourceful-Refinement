package com.resourceful_refinement.content.railgun;

import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Beam geometry shared by the server (damage) and client (rendering), so both trace the same line: from the
 * railgun's muzzle to the first block collision under the shooter's reticle, or to the maximum range.
 */
public final class RailgunTargeting {

    /** Third-person muzzle placement relative to the eyes (blocks). Tuned for the CROSSBOW_HOLD arm pose. */
    public static final double MUZZLE_FORWARD = 1.05D;
    public static final double MUZZLE_SIDE = 0.22D;
    public static final double MUZZLE_DOWN = 0.28D;

    /** Extra radius added to entity hitboxes when testing them against the beam line. */
    public static final double HIT_RADIUS = 0.3D;

    private RailgunTargeting() {}

    /** Ray from the eyes along the look direction, stopping at the first block collision shape (fluids ignored). */
    public static BlockHitResult trace(LivingEntity shooter, double range, float partialTick) {
        Vec3 eye = shooter.getEyePosition(partialTick);
        Vec3 end = eye.add(shooter.getViewVector(partialTick).scale(range));
        // A miss reports the ray's end as its location, so getLocation() is always the beam end.
        return shooter.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, shooter));
    }

    public static Vec3 beamEnd(LivingEntity shooter, double range, float partialTick) {
        return trace(shooter, range, partialTick).getLocation();
    }

    /**
     * Approximate world-space muzzle of a railgun held in {@code hand}, for third-person views and the server. The
     * local player's first-person muzzle is computed on the client from the item transform instead.
     */
    public static Vec3 muzzle(LivingEntity shooter, InteractionHand hand, float partialTick) {
        Vec3 look = shooter.getViewVector(partialTick);
        float yaw = shooter.getViewYRot(partialTick) * Mth.DEG_TO_RAD;
        Vec3 right = new Vec3(-Mth.cos(yaw), 0.0D, -Mth.sin(yaw));
        HumanoidArm arm = hand == InteractionHand.MAIN_HAND ? shooter.getMainArm() : shooter.getMainArm().getOpposite();
        double side = arm == HumanoidArm.RIGHT ? 1.0D : -1.0D;
        return shooter.getEyePosition(partialTick)
                .add(look.scale(MUZZLE_FORWARD))
                .add(right.scale(side * MUZZLE_SIDE))
                .add(0.0D, -MUZZLE_DOWN, 0.0D);
    }

    /** Living entities (other than the shooter and spectators) whose hitbox the segment passes through. */
    public static List<LivingEntity> entitiesOnBeam(LivingEntity shooter, Vec3 start, Vec3 end) {
        List<LivingEntity> hits = new ArrayList<>();
        AABB sweep = new AABB(start, end).inflate(HIT_RADIUS + 1.0D);
        for (Entity entity : shooter.level().getEntities(shooter, sweep,
                e -> e instanceof LivingEntity && e.isAlive() && !e.isSpectator())) {
            AABB box = entity.getBoundingBox().inflate(HIT_RADIUS);
            if (box.contains(start) || box.clip(start, end).isPresent()) {
                hits.add((LivingEntity) entity);
            }
        }
        return hits;
    }
}
