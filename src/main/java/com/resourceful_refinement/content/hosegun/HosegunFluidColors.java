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

    /**
     * Opaque ARGB for {@link net.minecraft.client.model.geom.ModelPart} vertex tinting.
     * Registry fluid colours use reduced alpha (e.g. {@code 0xFA}) for tanks/particles; item cubes need {@code 0xFF}.
     */
    public static int getTint(Fluid fluid) {
        int tint = GelFluidTintColors.getGelTint(fluid);
        int rgb = tint & 0x00FFFFFF;
        if (rgb == 0) {
            return 0xFFFFFFFF;
        }
        return 0xFF000000 | rgb;
    }
}
