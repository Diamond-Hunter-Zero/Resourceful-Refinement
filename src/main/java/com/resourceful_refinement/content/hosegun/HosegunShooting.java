package com.resourceful_refinement.content.hosegun;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Shared muzzle placement for gel-blobs and spray particles (hand offset + forward from the player).
 */
public final class HosegunShooting {

    /** Blocks in front of the eyes along the look vector. */
    public static final double FORWARD_OFFSET = 0.55D;

    /** Horizontal offset from the body centre toward the holding arm. */
    public static final double SIDE_OFFSET = 0.32D;

    /** Slightly below eye height toward the hands. */
    public static final double VERTICAL_OFFSET = -0.15D;

    private HosegunShooting() {}

    public static Vec3 getMuzzlePosition(LivingEntity shooter, InteractionHand hand) {
        Vec3 look = shooter.getLookAngle();
        Vec3 right = look.cross(new Vec3(0.0D, 1.0D, 0.0D));
        if (right.lengthSqr() < 1.0E-6D) {
            right = look.cross(new Vec3(1.0D, 0.0D, 0.0D));
        }
        right = right.normalize();

        double side = sideSign(shooter, hand);
        return shooter.getEyePosition()
                .add(0.0D, VERTICAL_OFFSET, 0.0D)
                .add(look.scale(FORWARD_OFFSET))
                .add(right.scale(side * SIDE_OFFSET));
    }

    private static double sideSign(LivingEntity shooter, InteractionHand hand) {
        boolean mainHand = hand == InteractionHand.MAIN_HAND;
        HumanoidArm itemArm = mainHand ? shooter.getMainArm() : shooter.getMainArm().getOpposite();
        double side = itemArm == HumanoidArm.RIGHT ? 1.0D : -1.0D;
        if (shooter.getMainArm() == HumanoidArm.LEFT) {
            side = -side;
        }
        return side;
    }
}
