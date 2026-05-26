package com.resourceful_refinement.content.hosegun;

import com.resourceful_refinement.registry.FluidEntry;
import com.resourceful_refinement.registry.ModFluids;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
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
        Fluid resolved = fluid;
        if (fluid instanceof net.minecraft.world.level.material.FlowingFluid flowing) {
            Fluid source = flowing.getSource();
            if (source != null) {
                resolved = source;
            }
        }

        for (FluidEntry entry : ModFluids.ENTRIES) {
            if (entry.source.get() == resolved) {
                return entry.color | 0xFF000000;
            }
        }

        ResourceLocation id = BuiltInRegistries.FLUID.getKey(resolved);
        if (resolved.isSame(Fluids.WATER) || resolved.isSame(Fluids.FLOWING_WATER)) {
            return 0xFF3F76E4;
        }
        if (resolved.isSame(Fluids.LAVA) || resolved.isSame(Fluids.FLOWING_LAVA)) {
            return 0xFFFF4500;
        }
        return 0xFFAAAAAA;
    }
}
