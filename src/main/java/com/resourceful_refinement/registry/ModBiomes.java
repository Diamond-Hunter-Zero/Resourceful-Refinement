package com.resourceful_refinement.registry;

import com.resourceful_refinement.ResourcefulRefinementMain;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

public class ModBiomes {
    public static final ResourceKey<Biome> CHORAL_CLUSTERS = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "choral_clusters")
    );
}
