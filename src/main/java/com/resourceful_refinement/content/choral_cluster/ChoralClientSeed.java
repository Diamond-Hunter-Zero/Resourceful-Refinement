package com.resourceful_refinement.content.choral_cluster;

import com.resourceful_refinement.mixin.BiomeManagerAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;

/**
 * Supplies a deterministic, seed-driven value for client-side Choral block tints. It reads the level's
 * biome-zoom seed ({@code BiomeManager.obfuscateSeed(worldSeed)}), which is present on both the
 * integrated server and remote clients, so tints vary per world without any networking.
 */
public final class ChoralClientSeed {
    private ChoralClientSeed() {
    }

    /** The per-world biome-zoom seed, or 0 if no level context is available (e.g. inventory rendering). */
    public static long get(Level level) {
        if (level == null) {
            return 0L;
        }
        BiomeManager manager = level.getBiomeManager();
        if (manager instanceof BiomeManagerAccessor accessor) {
            return accessor.getBiomeZoomSeed();
        }
        return 0L;
    }

    /** A scrambled per-world phase in [0, 1), for offsetting gradient-based tints by seed. */
    public static double seedPhase(Level level) {
        long mixed = get(level) * 0x9E3779B97F4A7C15L;
        return ((mixed >>> 40) & 0xFFFFFFL) / (double) 0x1000000L;
    }
}
