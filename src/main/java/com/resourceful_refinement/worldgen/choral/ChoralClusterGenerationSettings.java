package com.resourceful_refinement.worldgen.choral;

/**
 * Central tuning surface for Choral Clusters generation.
 *
 * The values here are intentionally public constants/records for early content iteration. Once the
 * terrain style settles, high-value knobs can be mirrored into ServerConfig.
 */
public class ChoralClusterGenerationSettings {
    public static final int OUTER_END_MIN_BLOCK_DISTANCE = 1024;
    public static final double BIOME_NOISE_SCALE = 0.0019D;
    public static final double BIOME_DETAIL_NOISE_SCALE = 0.008D;
    public static final double BIOME_THRESHOLD = 0.24D;

    public static final int STRUCTURE_SPACING = 3;
    public static final int STRUCTURE_SEPARATION = 2;
    public static final int STRUCTURE_SALT = 91720431;

    public static final float MIN_ISLAND_RADIUS = 24.0F;
    public static final float MAX_ISLAND_RADIUS = 108.0F;
    public static final float MIN_HEIGHT_MULTIPLIER = 0.9F;
    public static final float MAX_HEIGHT_MULTIPLIER = 1.05F;
    public static final int EDGE_SAMPLE_RADIUS = 12;
    public static final double TERRAIN_PRIMARY_NOISE_SCALE = 0.045D;
    public static final double TERRAIN_SECONDARY_NOISE_SCALE = 0.095D;
    public static final double VALLEY_SHARPNESS = 0.3D;


    // Presets
    public static final ChorusCrystalPillarPreset MODEST = new ChorusCrystalPillarPreset(
            "modest",
            0, 1,
            1.0F, 4F,
            4.0F, 15.0F,
            0F, 0.1F, 0.9F,
            12.0F, 40.0F,
            0.52F,
            4,
            0.15F
    );

    public static final ChorusCrystalPillarPreset BALANCED = new ChorusCrystalPillarPreset(
            "balanced",
            5, 10,
            1.0F, 3.25F,
            10.0F, 42.0F,
            0.44F, 0.36F, 0.20F,
            8.0F, 38.0F,
            0.52F,
            4,
            0.42F
    );

    public static final ChorusCrystalPillarPreset DENSE_SPIRES = new ChorusCrystalPillarPreset(
            "dense_spires",
            9, 16,
            0.8F, 2.4F,
            14.0F, 54.0F,
            0.68F, 0.18F, 0.14F,
            5.0F, 24.0F,
            0.72F,
            5,
            0.56F
    );

    public static final ChorusCrystalPillarPreset WIDE_ASTEROIDS = new ChorusCrystalPillarPreset(
            "wide_asteroids",
            4, 8,
            2.0F, 5.0F,
            8.0F, 34.0F,
            0.52F, 0.30F, 0.18F,
            6.0F, 28.0F,
            0.35F,
            3,
            0.34F
    );

    public static final ChorusCrystalPillarPreset CHAOTIC_SHARDS = new ChorusCrystalPillarPreset(
            "chaotic_shards",
            7, 13,
            1.0F, 3.6F,
            16.0F, 66.0F,
            0.22F, 0.52F, 0.26F,
            18.0F, 58.0F,
            0.86F,
            6,
            0.70F
    );

    public static final ChorusCrystalPillarPreset ACTIVE_PRESET = MODEST;

    private ChoralClusterGenerationSettings() {
    }
}
