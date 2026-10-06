package com.resourceful_refinement.registry;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.worldgen.choral.feature.AttachedGrowthConfiguration;
import com.resourceful_refinement.worldgen.choral.feature.AttachedGrowthFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, ResourcefulRefinementMain.MOD_ID);

    public static final DeferredHolder<Feature<?>, AttachedGrowthFeature> ATTACHED_GROWTH =
            FEATURES.register("attached_growth", () -> new AttachedGrowthFeature(AttachedGrowthConfiguration.CODEC));
}
