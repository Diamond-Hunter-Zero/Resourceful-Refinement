package com.resourceful_refinement.content.hosegun;

import com.resourceful_refinement.content.gel_splatter.GelFluidTintColors;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

public final class HosegunFluidColors {

    private HosegunFluidColors() {}

    public static int getTint(FluidStack stack) {
        if (stack.isEmpty()) {
            return 0xFFFFFFFF;
        }
        return getTint(stack.getFluid());
    }

    public static int getTint(Fluid fluid) {
        int tint = GelFluidTintColors.getGelTint(fluid);
        if ((tint >>> 24) == 0) {
            return tint | 0xFF000000;
        }
        return tint;
    }
}
