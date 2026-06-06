package com.resourceful_refinement.content.hosegun;

import com.resourceful_refinement.content.gel_splatter.GelImpactConstants;
import com.resourceful_refinement.content.gel_splatter.GelPropertiesManager;
import com.resourceful_refinement.content.gel_splatter.GelType;
import com.simibubi.create.AllParticleTypes;
import com.simibubi.create.content.fluids.particle.FluidParticleData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Tinted Create fluid particles for the hosegun spray and gel-blob impacts.
 */
public final class HosegunParticleEffects {

    /** Particles spawned each tick while the hosegun is held. */
    public static final int SPRAY_PARTICLES_PER_TICK = 8;

    /** Spacing between successive particles along the spray direction. */
    private static final double ALONG_LOOK_SPACING = 0.09;

    private HosegunParticleEffects() {}

    /**
     * Matches gel-blob {@link net.minecraft.world.entity.projectile.Projectile#shoot} spread so
     * particles move at the same speed as projectiles.
     */
    public static Vec3 computeSprayVelocity(
            LivingEntity shooter,
            float velocity,
            float inaccuracy,
            RandomSource random
    ) {
        Vec3 movement = shooter.getLookAngle().scale(velocity);
        double spread = 0.0075 * inaccuracy;
        return movement.add(
                random.nextGaussian() * spread,
                random.nextGaussian() * spread,
                random.nextGaussian() * spread
        );
    }

    /**
     * Continuous stream segment for one tick; particles are staggered along {@code velocity}.
     */
    public static void spawnSprayStream(
            ServerLevel level,
            LivingEntity shooter,
            InteractionHand hand,
            FluidStack fluidStack,
            Vec3 velocity,
            int streamTick,
            RandomSource random
    ) {
        if (fluidStack.isEmpty() || velocity.lengthSqr() < 1.0E-8) {
            return;
        }

        FluidStack particleFluid = new FluidStack(GelPropertiesManager.resolveSourceFluid(fluidStack.getFluid()), 1000);
        Vec3 look = velocity.normalize();
        Vec3 right = look.cross(new Vec3(0.0, 1.0, 0.0));
        if (right.lengthSqr() < 1.0E-6) {
            right = look.cross(new Vec3(1.0, 0.0, 0.0));
        }
        right = right.normalize();
        Vec3 up = look.cross(right).normalize();

        Vec3 muzzle = HosegunShooting.getMuzzlePosition(shooter, hand);
        double tickAlongOffset = (streamTick % 4) * ALONG_LOOK_SPACING * 0.5;
        double lateralSpread = 0.06;

        for (int i = 0; i < SPRAY_PARTICLES_PER_TICK; i++) {
            double along = tickAlongOffset + i * ALONG_LOOK_SPACING;
            double lateral = (random.nextDouble() - 0.5) * lateralSpread;
            double vertical = (random.nextDouble() - 0.5) * lateralSpread;

            Vec3 pos = muzzle
                    .add(look.scale(along))
                    .add(right.scale(lateral))
                    .add(up.scale(vertical));

            double vx = velocity.x + (random.nextDouble() - 0.5) * 0.02;
            double vy = velocity.y + (random.nextDouble() - 0.5) * 0.02;
            double vz = velocity.z + (random.nextDouble() - 0.5) * 0.02;

            level.sendParticles(
                    new FluidParticleData(AllParticleTypes.FLUID_PARTICLE.get(), particleFluid.copy()),
                    pos.x, pos.y, pos.z,
                    0,
                    vx, vy, vz,
                    1.0
            );
        }
    }

    public static void spawnImpactSplash(
            ServerLevel server,
            Vec3 hitPos,
            Fluid fluid,
            GelType type,
            RandomSource random
    ) {
        FluidStack particleFluid = new FluidStack(GelPropertiesManager.resolveSourceFluid(fluid), 1000);
        float radius = GelImpactConstants.getImpactRadius(type);
        int particleCount = (int) (8 + radius * radius * 6);

        for (int i = 0; i < particleCount; i++) {
            double theta = random.nextDouble() * Math.PI * 2;
            double phi = Math.acos(2 * random.nextDouble() - 1);
            double distance = radius * Math.cbrt(random.nextDouble());

            double offsetX = distance * Math.sin(phi) * Math.cos(theta);
            double offsetY = distance * Math.sin(phi) * Math.sin(theta);
            double offsetZ = distance * Math.cos(phi);

            double x = hitPos.x + offsetX;
            double y = hitPos.y + offsetY;
            double z = hitPos.z + offsetZ;

            double speed = 0.08 + random.nextDouble() * 0.12;
            server.sendParticles(
                    new FluidParticleData(AllParticleTypes.FLUID_PARTICLE.get(), particleFluid.copy()),
                    x, y, z,
                    0,
                    offsetX * speed + (random.nextDouble() - 0.5) * 0.04,
                    offsetY * speed + 0.06 + random.nextDouble() * 0.06,
                    offsetZ * speed + (random.nextDouble() - 0.5) * 0.04,
                    0.5
            );
        }
    }
}
