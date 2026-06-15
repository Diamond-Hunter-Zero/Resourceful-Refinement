package com.resourceful_refinement.content.choral_cluster;

/**
 * Tuning surface for the Chorus Crystal world-position tint.
 *
 * The gradient phase is computed as:
 * {@code (x * X_AXIS_SLOPE + y + z * Z_AXIS_SLOPE + PHASE_OFFSET) / PERIOD_BLOCKS}
 *
 * Color keys are sampled as a looping gradient. Add, remove, or reorder keys to change the banding
 * palette without touching rendering code.
 */
public class ChorusCrystalColorSettings {
    /** Vertical block distance before the gradient repeats. Larger values make wider color bands. */
    public static final double PERIOD_BLOCKS = 20.0D;

    /** How many vertical blocks of phase are added per +X block. Positive values slant bands downward toward +X. */
    public static final double X_AXIS_SLOPE = 0.36D;

    /** How many vertical blocks of phase are added per +Z block. Positive values slant bands downward toward +Z. */
    public static final double Z_AXIS_SLOPE = -0.22D;

    /** Global phase offset in blocks. Use this to slide all bands up/down without changing their spacing. */
    public static final double PHASE_OFFSET = 0.0D;

    /**
     * Blend strength between the untinted texture and gradient color.
     * 0.0 keeps the texture white, 1.0 fully applies the gradient color.
     */
    public static final float TINT_INTENSITY = 0.95F;

    /** Multiplies final RGB brightness after tinting. Useful for bright crystalline colors. */
    public static final float BRIGHTNESS_MULTIPLIER = 1.18F;

    /** Item rendering has no world position, so this fixed phase gives inventory stacks a representative color. */
    public static final double ITEM_PHASE = 0.18D;

    public static final ColorKey[] COLOR_KEYS = new ColorKey[] {
            new ColorKey(0.00F, 0xd546ff),
            new ColorKey(0.2F, 0xff5fae),
            new ColorKey(0.4F, 0xb57ff9),
            new ColorKey(0.6F, 0x6c59ff),
            new ColorKey(0.75F, 0x874cff),
            new ColorKey(0.9F, 0xb254ff),
            new ColorKey(1.00F, 0xd546ff)
    };

    public static final ColorKey[] RAINBOW_COLOR_KEYS = new ColorKey[] {
            new ColorKey(0.00F, 0x48FFE1),
            new ColorKey(0.2F, 0x78B8FF),
            new ColorKey(0.36F, 0xd977f9),
            new ColorKey(0.525F, 0xff6380),
            new ColorKey(0.625F, 0xff8349),
            new ColorKey(0.75F, 0xffe88f),
            new ColorKey(0.9F, 0xa9ffa3),
            new ColorKey(1.00F, 0x48FFE1)
    };

    public static final ColorKey[] NETHER_COLOR_KEYS = new ColorKey[] {
            new ColorKey(0.00F, 0xff702f),
            new ColorKey(0.2F, 0xbb241c),
            new ColorKey(0.45F, 0xf99b48),
            new ColorKey(0.65F, 0xffd88d),
            new ColorKey(0.8F, 0xff9220),
            new ColorKey(1.00F, 0xff702f)
    };

    private ChorusCrystalColorSettings() {
    }

    public record ColorKey(float position, int color) {
    }
}
