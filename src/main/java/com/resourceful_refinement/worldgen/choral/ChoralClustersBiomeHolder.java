package com.resourceful_refinement.worldgen.choral;

import com.resourceful_refinement.registry.ModBiomes;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;

public class ChoralClustersBiomeHolder {
    private static Holder<Biome> choralClusters;

    private ChoralClustersBiomeHolder() {
    }

    public static void resolve(RegistryAccess registryAccess) {
        choralClusters = registryAccess.registry(Registries.BIOME)
                .flatMap(registry -> registry.getHolder(ModBiomes.CHORAL_CLUSTERS))
                .map(holder -> (Holder<Biome>) holder)
                .orElse(null);
    }

    public static Holder<Biome> get() {
        return choralClusters;
    }

    public static void clear() {
        choralClusters = null;
    }
}
