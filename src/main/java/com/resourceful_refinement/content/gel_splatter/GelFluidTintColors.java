package com.resourceful_refinement.content.gel_splatter;

import com.resourceful_refinement.registry.FluidEntry;
import com.resourceful_refinement.registry.ModFluids;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.function.Function;

/**
 * Resolves gel tint colours for splatters, hosegun tanks, and other tinted gel visuals.
 */
public final class GelFluidTintColors {

    private static Function<Fluid, Integer> compatTint = fluid -> 0xFFFFFFFF;
    private static Runnable compatCacheClear = () -> {};

    private GelFluidTintColors() {}

    /** Wired from {@link com.resourceful_refinement.registry.ModClientEvents} on the client. */
    public static void bindClientCompat(Function<Fluid, Integer> resolver, Runnable cacheClear) {
        compatTint = resolver;
        compatCacheClear = cacheClear;
    }

    public static int getGelTint(Fluid fluid) {
        if (fluid == null || fluid == Fluids.EMPTY) {
            return 0xFFFFFFFF;
        }

        Fluid resolved = resolveSourceFluid(fluid);

        for (FluidEntry entry : ModFluids.ENTRIES) {
            if (entry.source.get() == resolved) {
                return entry.color;
            }
        }

        if (resolved.isSame(Fluids.WATER) || resolved.isSame(Fluids.FLOWING_WATER)) {
            return 0x3F76E4;
        }
        if (resolved.isSame(Fluids.LAVA) || resolved.isSame(Fluids.FLOWING_LAVA)) {
            return 0xFF4500;
        }

        return compatTint.apply(resolved);
    }

    public static void clearCompatCache() {
        compatCacheClear.run();
    }

    private static Fluid resolveSourceFluid(Fluid fluid) {
        if (fluid instanceof FlowingFluid flowing) {
            Fluid source = flowing.getSource();
            if (source != null && source != Fluids.EMPTY) {
                return source;
            }
        }
        return fluid;
    }
}
