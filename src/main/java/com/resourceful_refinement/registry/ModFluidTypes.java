package com.resourceful_refinement.registry;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.fluids.base.FluidGroup;
import com.resourceful_refinement.content.fluids.base.GeneralizedFluidType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModFluidTypes {
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, ResourcefulRefinementMain.MOD_ID);

    public static DeferredHolder<FluidType, FluidType> registerType(String name, int color, FluidGroup group) {
        int alpha = (group == FluidGroup.RAW || group == FluidGroup.CATALYSED || group == FluidGroup.CARBORAX) ? 0xFA000000 : 0xFF000000;
        return FLUID_TYPES.register(name, () -> new GeneralizedFluidType(
                FluidType.Properties.create()
                        .descriptionId("fluid_type.resourceful_refinement." + name)
                        .density(group.density)
                        .viscosity(group.viscosity)
                        .temperature(group.temperature)
                        .lightLevel(group.lightLevel)
                        .canExtinguish(false),
                alpha | color,
                ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID,"block/fluids/" + group.stillTextureID),
                ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID,"block/fluids/" + group.flowTextureID)
        ));
    }
}
