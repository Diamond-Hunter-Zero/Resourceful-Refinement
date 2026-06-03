package com.resourceful_refinement.content.refinery;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Kinetic blender blade — applies tangential push and contact damage to entities in the swept blade volume.
 * Hit tests use blade-local coordinates by inverting the current kinetic rotation angle (the block model does not rotate in-world).
 */
public class BlenderBladeBlockEntity extends KineticBlockEntity {

    /** Arm reach from shaft centre (blocks); total tip-to-tip span ≈ 2 × this value. */
    public static final double ARM_LENGTH = 1.5;
    /** Half-thickness of the blade plane (blocks), normal to the arm span. */
    public static final double PLANE_HALF_THICKNESS = 0.5;
    /** Minimum |RPM| from {@link #getSpeed()} before the blade affects entities. */
    public static final float MIN_EFFECT_SPEED = 1f;
    /** Base tangential impulse scale per tick at reference speed. */
    public static final double BASE_PUSH = 0.05;
    /** Reference |speed| for full push strength. */
    public static final float REFERENCE_SPEED = 8f;
    /** Damage dealt on damage ticks. */
    public static final float CONTACT_DAMAGE = 0.5f;
    public static final int DAMAGE_INTERVAL = 15;

    public BlenderBladeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**
     * Server-side rotation angle (radians), matching Create's client
     * {@code KineticBlockEntityRenderer.getAngleForBe} but using level game time instead of
     * {@code AnimationTickHolder.getRenderTime}.
     */
    public static float computeRotationAngleRadians(KineticBlockEntity be) {
        Level level = be.getLevel();
        if (level == null) return 0f;
        float time = level.getGameTime();
        float angleDegrees = (time * be.getSpeed() * 3.0f / 10.0f) % 360.0f;
        return angleDegrees * ((float) Math.PI / 180.0f);
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide) return;

        float speed = getSpeed();
        if (Math.abs(speed) < MIN_EFFECT_SPEED) return;

        Direction.Axis axis = getBlockState().getValue(RotatedPillarKineticBlock.AXIS);
        float bladeAngle = computeRotationAngleRadians(this);
        AABB sweep = getBladeSweepAABB(worldPosition, axis);
        List<Entity> entities = level.getEntitiesOfClass(Entity.class, sweep, this::canAffectEntity);

        Vec3 centre = Vec3.atCenterOf(worldPosition);
        double pushStrength = BASE_PUSH * Mth.clamp(Math.abs(speed) / REFERENCE_SPEED, 0.05, 1.5);
        boolean damageTick = level.getGameTime() % DAMAGE_INTERVAL == 0;

        for (Entity entity : entities) {
            //if (!isEntityInBladeVolume(entity, centre, axis, bladeAngle)) continue;

            Vec3 push = computeTangentPush(entity.position(), centre, axis, speed, pushStrength);
            if (push.lengthSqr() > 1.0E-8) {

                if (!(entity instanceof ItemEntity itemEntity))
                    push.scale(0.5f);

                entity.setDeltaMovement(entity.getDeltaMovement().add(push));
                entity.hurtMarked = true;
                if (entity instanceof ServerPlayer serverPlayer) {
                    serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
                }
            }

            if (damageTick && entity instanceof LivingEntity living) {
                living.hurt(level.damageSources().generic(), CONTACT_DAMAGE);
            }
        }
    }

    private boolean canAffectEntity(Entity entity) {
        if (!entity.isAlive() || entity.isSpectator()) return false;
        if (entity instanceof Player player && (player.isCreative() || player.isSpectator())) return false;
        return true;
    }

    /** Broad-phase AABB — axis-aligned cube enclosing the full diameter of rotation. */
    public static AABB getBladeSweepAABB(BlockPos pos, Direction.Axis shaftAxis) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.5;
        double cz = pos.getZ() + 0.5;
        double arm = ARM_LENGTH;

        return switch (shaftAxis) {
            case Y -> new AABB(cx - arm, pos.getY() - 0.05, cz - arm, cx + arm, pos.getY() + 1.05, cz + arm);
            case X -> new AABB(pos.getX() - 0.05, cy - arm, cz - arm, pos.getX() + 1.05, cy + arm, cz + arm);
            case Z -> new AABB(cx - arm, cy - arm, pos.getZ() - 0.05, cx + arm, cy + arm, pos.getZ() + 1.05);
        };
    }

    /**
     * Transforms a world-space offset into the blade's rest frame (arms along span axis, thin along plane normal).
     * Applies inverse rotation around the shaft axis by {@code angleRad}.
     */
    public static Vec3 worldOffsetToBladeFrame(Vec3 worldOffset, Direction.Axis shaftAxis, float angleRad) {
        float cos = Mth.cos(angleRad);
        float sin = Mth.sin(angleRad);
        return switch (shaftAxis) {
            case Y -> new Vec3(
                    worldOffset.x * cos + worldOffset.z * sin,
                    worldOffset.y,
                    -worldOffset.x * sin + worldOffset.z * cos);
            case X -> new Vec3(
                    worldOffset.x,
                    worldOffset.y * cos + worldOffset.z * sin,
                    -worldOffset.y * sin + worldOffset.z * cos);
            case Z -> new Vec3(
                    worldOffset.x * cos + worldOffset.y * sin,
                    -worldOffset.x * sin + worldOffset.y * cos,
                    worldOffset.z);
        };
    }

    /** Narrow-phase check in blade-local coordinates at the current rotation angle. */
    public static boolean isEntityInBladeVolume(Entity entity, Vec3 centre, Direction.Axis shaftAxis, float angleRad) {
        Vec3 worldLocal = entity.position().subtract(centre);
        Vec3 bladeLocal = worldOffsetToBladeFrame(worldLocal, shaftAxis, angleRad);
        return isOffsetInBladeFrame(bladeLocal, shaftAxis);
    }

    /** At rest orientation: arms span perpendicular to shaft, thin along plane normal. */
    public static boolean isOffsetInBladeFrame(Vec3 bladeLocal, Direction.Axis shaftAxis) {
        double arm = ARM_LENGTH;
        double thin = PLANE_HALF_THICKNESS;
        double shaftHalf = 0.55;

        return switch (shaftAxis) {
            case Y -> Math.abs(bladeLocal.x) <= arm && Math.abs(bladeLocal.z) <= thin && Math.abs(bladeLocal.y) <= shaftHalf;
            case X -> Math.abs(bladeLocal.y) <= arm && Math.abs(bladeLocal.z) <= thin && Math.abs(bladeLocal.x) <= shaftHalf;
            case Z -> Math.abs(bladeLocal.y) <= arm && Math.abs(bladeLocal.x) <= thin && Math.abs(bladeLocal.z) <= shaftHalf;
        };
    }

    /**
     * Tangential push in the plane perpendicular to the shaft, matching Create rotation sign.
     */
    public static Vec3 computeTangentPush(Vec3 entityPos, Vec3 centre, Direction.Axis shaftAxis, float speed, double magnitude) {
        Vec3 r = entityPos.subtract(centre);
        double sign = -Math.signum(speed);

        Vec3 tangent = (switch (shaftAxis) {
            case Y -> new Vec3(-r.z, 0, r.x);
            case X -> new Vec3(0, r.z, -r.y);
            case Z -> new Vec3(r.y, -r.x, 0);
        });

        double radial = switch (shaftAxis) {
            case Y -> Math.hypot(r.x, r.z);
            case X -> Math.hypot(r.y, r.z);
            case Z -> Math.hypot(r.x, r.y);
        };

        if (radial < 0.1) return Vec3.ZERO;

        //double tipScale = Math.min(radial / ARM_LENGTH, 1.0);
        return tangent.normalize().scale(r.length() * -speed * Math.PI/(14f * 120f)).subtract(r.normalize().scale(0.00633f));
        //return tangent.normalize().scale(magnitude * (0.75 + 0.25 * tipScale));
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return getBladeSweepAABB(worldPosition, getBlockState().getValue(RotatedPillarKineticBlock.AXIS)).inflate(0.25);
    }
}
