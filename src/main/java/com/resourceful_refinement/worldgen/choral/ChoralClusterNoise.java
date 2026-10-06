package com.resourceful_refinement.worldgen.choral;

import net.minecraft.util.Mth;

public class ChoralClusterNoise {
    private ChoralClusterNoise() {
    }

    public static double biomeNoise(long seed, int quartX, int quartZ) {
        int blockX = quartX << 2;
        int blockZ = quartZ << 2;
        double broad = valueNoise(blockX * ChoralClusterGenerationSettings.BIOME_NOISE_SCALE, blockZ * ChoralClusterGenerationSettings.BIOME_NOISE_SCALE, seed ^ 4141L);
        double detail = valueNoise(blockX * ChoralClusterGenerationSettings.BIOME_DETAIL_NOISE_SCALE, blockZ * ChoralClusterGenerationSettings.BIOME_DETAIL_NOISE_SCALE, seed ^ 9917L);
        return broad * 0.78D + detail * 0.22D;
    }

    public static boolean shouldPlaceChoralBiome(long seed, int quartX, int quartZ) {
        int blockX = quartX << 2;
        int blockZ = quartZ << 2;
        long distanceSquared = (long) blockX * blockX + (long) blockZ * blockZ;
        long min = (long) ChoralClusterGenerationSettings.OUTER_END_MIN_BLOCK_DISTANCE * ChoralClusterGenerationSettings.OUTER_END_MIN_BLOCK_DISTANCE;
        return distanceSquared >= min && biomeNoise(seed, quartX, quartZ) > ChoralClusterGenerationSettings.BIOME_THRESHOLD;
    }

    /**
     * Seed-stable 2D test for whether a surface column falls inside a Choral End Stone patch.
     * Blends a broad octave with a finer detail octave so patch edges are irregular rather than circular.
     */
    public static boolean isSurfacePatch(long seed, int blockX, int blockZ) {
        double broad = valueNoise(
                blockX * ChoralClusterGenerationSettings.SURFACE_PATCH_NOISE_SCALE,
                blockZ * ChoralClusterGenerationSettings.SURFACE_PATCH_NOISE_SCALE,
                seed ^ ChoralClusterGenerationSettings.SURFACE_PATCH_SEED);
        double detail = valueNoise(
                blockX * ChoralClusterGenerationSettings.SURFACE_PATCH_DETAIL_SCALE,
                blockZ * ChoralClusterGenerationSettings.SURFACE_PATCH_DETAIL_SCALE,
                seed ^ (ChoralClusterGenerationSettings.SURFACE_PATCH_SEED ^ 0x1B3F));
        return broad * 0.72D + detail * 0.28D > ChoralClusterGenerationSettings.SURFACE_PATCH_THRESHOLD;
    }

    public static double valueNoise(double x, double z, long seed) {
        int x0 = Mth.floor(x);
        int z0 = Mth.floor(z);
        double fx = x - x0;
        double fz = z - z0;
        double sx = smooth(fx);
        double sz = smooth(fz);
        double a = randomValue(x0, z0, seed);
        double b = randomValue(x0 + 1, z0, seed);
        double c = randomValue(x0, z0 + 1, seed);
        double d = randomValue(x0 + 1, z0 + 1, seed);
        return Mth.lerp(sz, Mth.lerp(sx, a, b), Mth.lerp(sx, c, d));
    }

    public static double randomValue(int x, int z, long seed) {
        long value = seed;
        value ^= x * 341873128712L;
        value ^= z * 132897987541L;
        value = (value ^ (value >>> 30)) * 0xbf58476d1ce4e5b9L;
        value = (value ^ (value >>> 27)) * 0x94d049bb133111ebL;
        value ^= value >>> 31;
        return ((value & 0xFFFFFF) / (double) 0x7FFFFF) - 1.0D;
    }

    private static double smooth(double value) {
        return value * value * value * (value * (value * 6.0D - 15.0D) + 10.0D);
    }
}
