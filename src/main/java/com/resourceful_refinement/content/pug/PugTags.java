package com.resourceful_refinement.content.pug;

import com.resourceful_refinement.ResourcefulRefinementMain;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public final class PugTags {
    public static final TagKey<Fluid> CARBORAX_FUEL = TagKey.create(Registries.FLUID,
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "carborax_fuel"));

    private PugTags() {}
}
