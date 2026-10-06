package com.resourceful_refinement.content.choral_cluster;

/**
 * Tuning surface for the Choral End Stone surface tint.
 *
 * The tint is driven entirely by a world-position value noise (NOT the biome grass colour map), so it
 * is independent of neighbouring vanilla End biomes and never blends toward their default green. The
 * noise selects a phase along {@link #COLOR_KEYS}, a looping purple/pink gradient — add, remove or
 * reorder keys to re-tune the palette without touching rendering code.
 */
public class ChoralEndStoneColorSettings {
    /** Horizontal scale of the broad colour blotches. Smaller = larger, softer patches. */
    public static final double NOISE_SCALE = 0.035D;

    /** Horizontal scale of the finer break-up octave layered on top of the broad blotches. */
    public static final double DETAIL_NOISE_SCALE = 0.11D;

    /** Fixed (seed-independent) hash salt so patches are stable and continuous across the biome. */
    public static final int NOISE_SEED = 0x6ACE11;

    /** Blend strength between white (untinted texture) and the palette colour. 1.0 = full palette colour. */
    public static final float TINT_INTENSITY = 1.0F;

    /** Multiplies final RGB brightness after tinting. >1.0 brightens, <1.0 darkens. */
    public static final float BRIGHTNESS_MULTIPLIER = 1.0F;

    /** Item rendering has no world position, so this fixed phase gives inventory stacks a representative colour. */
    public static final double ITEM_PHASE = 0.6D;

    public static final ColorKey[] COLOR_KEYS = new ColorKey[] {
            new ColorKey(0.00F, 0xB94AE0),
            new ColorKey(0.22F, 0x9B4FD6),
            new ColorKey(0.45F, 0xC06BF0),
            new ColorKey(0.65F, 0xE75FC6),
            new ColorKey(0.82F, 0xFF7ABF),
            new ColorKey(1.00F, 0xB94AE0)
    };

    private ChoralEndStoneColorSettings() {
    }

    public record ColorKey(float position, int color) {
    }
}
