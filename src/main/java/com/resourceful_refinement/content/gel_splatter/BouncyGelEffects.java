package com.resourceful_refinement.content.gel_splatter;

import com.simibubi.create.AllParticleTypes;
import com.simibubi.create.content.fluids.particle.FluidParticleData;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

/** Particles and sounds when an entity bounces off bouncy gel. */
final class BouncyGelEffects {

    private BouncyGelEffects() {}

    static void spawn(ServerLevel level, double impactX, double impactY, double impactZ, Fluid fluid) {
        level.playSound(
                null,
                impactX,
                impactY,
                impactZ,
                SoundEvents.SLIME_BLOCK_FALL,
                SoundSource.BLOCKS,
                0.55F,
                0.85F + level.random.nextFloat() * 0.25F
        );

        level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SLIME_BLOCK.defaultBlockState()),
                impactX,
                impactY + 0.05D,
                impactZ,
                6,
                0.18D,
                0.04D,
                0.18D,
                0.07D
        );

        if (fluid == null || fluid == Fluids.EMPTY) {
            return;
        }

        FluidStack particleFluid = new FluidStack(GelPropertiesManager.resolveSourceFluid(fluid), 1000);
        for (int i = 0; i < 4; i++) {
            double offsetX = (level.random.nextDouble() - 0.5D) * 0.28D;
            double offsetZ = (level.random.nextDouble() - 0.5D) * 0.28D;
            level.sendParticles(
                    new FluidParticleData(AllParticleTypes.FLUID_PARTICLE.get(), particleFluid.copy()),
                    impactX + offsetX,
                    impactY + 0.05D,
                    impactZ + offsetZ,
                    1,
                    0.0D,
                    0.1D + level.random.nextDouble() * 0.08D,
                    0.0D,
                    0.35D
            );
        }
    }
}
