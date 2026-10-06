package com.resourceful_refinement.content.drill_pylon.tint;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.List;

/**
 * A colour-cycle definition for a {@link com.resourceful_refinement.content.drill_pylon.CrystalFissureBudBlock}.
 * The whole block is tinted by lerping through {@link #colors()} over client game-time, spending
 * {@link #secondsPerColor()} on each segment before looping back to the first colour.
 *
 * <p>Loaded from the {@code resourceful_refinement:fissure_bud_tints} datapack registry (synced to
 * clients). Each profile lists the {@link #sourceItems()} it applies to; a profile with
 * {@link #fallback()} = true becomes the default for any stored item without its own profile.
 */
public record FissureTintProfile(
        List<Integer> colors,
        float secondsPerColor,
        List<ResourceLocation> sourceItems,
        boolean fallback
) {
    /** Accepts a hex string ("#RRGGBB" / "0xRRGGBB" / "RRGGBB", alpha ignored) or a raw int. */
    public static final Codec<Integer> COLOR_CODEC = Codec.either(Codec.STRING, Codec.INT).xmap(
            either -> either.map(FissureTintProfile::parseColor, i -> i & 0xFFFFFF),
            color -> Either.left(toHex(color))
    );

    public static final Codec<FissureTintProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            COLOR_CODEC.listOf().fieldOf("colors").forGetter(FissureTintProfile::colors),
            Codec.FLOAT.optionalFieldOf("seconds_per_color", 3.0F).forGetter(FissureTintProfile::secondsPerColor),
            ResourceLocation.CODEC.listOf().optionalFieldOf("source_items", List.of()).forGetter(FissureTintProfile::sourceItems),
            Codec.BOOL.optionalFieldOf("fallback", false).forGetter(FissureTintProfile::fallback)
    ).apply(instance, FissureTintProfile::new));

    /**
     * The RGB tint (0xRRGGBB) for a given client game-time, smooth-lerped between adjacent colours and
     * looping. Empty → white; single colour → that colour (no animation).
     */
    public int colorAt(double timeTicks) {
        int n = colors.size();
        if (n == 0) {
            return 0xFFFFFF;
        }
        if (n == 1) {
            return colors.get(0) & 0xFFFFFF;
        }
        double stepTicks = Math.max(1.0D, secondsPerColor * 20.0D);
        double scaled = timeTicks / stepTicks;
        int from = Math.floorMod((int) Math.floor(scaled), n);
        int to = (from + 1) % n;
        float t = (float) (scaled - Math.floor(scaled));
        t = t * t * (3.0F - 2.0F * t); // smoothstep for a gentle ease between colours
        return lerpColor(colors.get(from), colors.get(to), t);
    }

    private static int lerpColor(int from, int to, float delta) {
        int r = Mth.floor(Mth.lerp(delta, (from >> 16) & 0xFF, (to >> 16) & 0xFF));
        int g = Mth.floor(Mth.lerp(delta, (from >> 8) & 0xFF, (to >> 8) & 0xFF));
        int b = Mth.floor(Mth.lerp(delta, from & 0xFF, to & 0xFF));
        return (r << 16) | (g << 8) | b;
    }

    private static int parseColor(String raw) {
        String hex = raw.trim();
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        } else if (hex.startsWith("0x") || hex.startsWith("0X")) {
            hex = hex.substring(2);
        }
        try {
            return (int) (Long.parseLong(hex, 16) & 0xFFFFFF);
        } catch (NumberFormatException e) {
            return 0xFFFFFF;
        }
    }

    private static String toHex(int color) {
        return String.format("#%06X", color & 0xFFFFFF);
    }
}
