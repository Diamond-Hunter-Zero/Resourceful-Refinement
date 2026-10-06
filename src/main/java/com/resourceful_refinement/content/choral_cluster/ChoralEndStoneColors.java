package com.resourceful_refinement.content.choral_cluster;

import com.resourceful_refinement.worldgen.choral.ChoralClusterNoise;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * World-position purple/pink tint for Choral End Stone. Replaces the old biome-grass-average tint, which
 * blended toward the green of neighbouring vanilla End biomes. Tuning lives in
 * {@link ChoralEndStoneColorSettings}.
 */
public class ChoralEndStoneColors {
    private ChoralEndStoneColors() {
    }

    public static int blockTint(BlockState state, BlockAndTintGetter level, BlockPos pos, int tintIndex) {
        if (tintIndex != 0) {
            return -1;
        }
        if (pos == null) {
            return itemTint(tintIndex);
        }
        long seed = ChoralClientSeed.get(ChorusCrystalColors.getLevelSafely(level));
        return colorAtPhase(phaseAt(seed, pos.getX(), pos.getZ()));
    }

    public static int itemTint(int tintIndex) {
        return tintIndex == 0 ? colorAtPhase(ChoralEndStoneColorSettings.ITEM_PHASE) : -1;
    }

    private static double phaseAt(long seed, int x, int z) {
        double broad = ChoralClusterNoise.valueNoise(
                x * ChoralEndStoneColorSettings.NOISE_SCALE,
                z * ChoralEndStoneColorSettings.NOISE_SCALE,
                seed ^ ChoralEndStoneColorSettings.NOISE_SEED);
        double detail = ChoralClusterNoise.valueNoise(
                x * ChoralEndStoneColorSettings.DETAIL_NOISE_SCALE,
                z * ChoralEndStoneColorSettings.DETAIL_NOISE_SCALE,
                seed ^ (ChoralEndStoneColorSettings.NOISE_SEED ^ 0x2B7F));
        double n = broad * 0.7D + detail * 0.3D;
        return (n + 1.0D) * 0.5D;
    }

    public static int colorAtPhase(double phase) {
        float wrapped = (float) (phase - Math.floor(phase));

        ChoralEndStoneColorSettings.ColorKey[] keys = ChoralEndStoneColorSettings.COLOR_KEYS;
        if (keys.length == 0) {
            return -1;
        }
        if (keys.length == 1) {
            return applyIntensity(keys[0].color());
        }

        ChoralEndStoneColorSettings.ColorKey previous = keys[0];
        for (int i = 1; i < keys.length; i++) {
            ChoralEndStoneColorSettings.ColorKey next = keys[i];
            if (wrapped <= next.position()) {
                float span = Math.max(0.0001F, next.position() - previous.position());
                float local = Mth.clamp((wrapped - previous.position()) / span, 0.0F, 1.0F);
                return applyIntensity(lerpColor(previous.color(), next.color(), smooth(local)));
            }
            previous = next;
        }

        return applyIntensity(keys[keys.length - 1].color());
    }

    private static int lerpColor(int from, int to, float delta) {
        int r = Mth.floor(Mth.lerp(delta, (from >> 16) & 0xFF, (to >> 16) & 0xFF));
        int g = Mth.floor(Mth.lerp(delta, (from >> 8) & 0xFF, (to >> 8) & 0xFF));
        int b = Mth.floor(Mth.lerp(delta, from & 0xFF, to & 0xFF));
        return (r << 16) | (g << 8) | b;
    }

    private static int applyIntensity(int color) {
        float intensity = Mth.clamp(ChoralEndStoneColorSettings.TINT_INTENSITY, 0.0F, 1.0F);
        float brightness = Math.max(0.0F, ChoralEndStoneColorSettings.BRIGHTNESS_MULTIPLIER);
        int r = applyChannel((color >> 16) & 0xFF, intensity, brightness);
        int g = applyChannel((color >> 8) & 0xFF, intensity, brightness);
        int b = applyChannel(color & 0xFF, intensity, brightness);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private static int applyChannel(int channel, float intensity, float brightness) {
        float blended = Mth.lerp(intensity, 255.0F, channel);
        return Mth.clamp(Mth.floor(blended * brightness), 0, 255);
    }

    private static float smooth(float value) {
        return value * value * (3.0F - 2.0F * value);
    }
}
