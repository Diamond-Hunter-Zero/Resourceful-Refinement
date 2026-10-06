package com.resourceful_refinement.worldgen.choral;

/**
 * Holds the active world seed for server-side Choral Clusters generation so the biome-placement and
 * surface-patch noise become seed-driven and deterministic (identical for a given seed, different
 * across seeds). Set at {@code ServerAboutToStartEvent} and cleared at {@code ServerStoppingEvent}.
 *
 * <p>Client-side tinting does not use this — the raw world seed isn't available on a remote client.
 * It uses the biome-zoom seed instead (see {@code ChoralClientSeed}).
 */
public final class ChoralSeed {
    private static volatile long worldSeed;

    private ChoralSeed() {
    }

    public static void setWorldSeed(long seed) {
        worldSeed = seed;
    }

    public static long worldSeed() {
        return worldSeed;
    }

    public static void clear() {
        worldSeed = 0L;
    }
}
