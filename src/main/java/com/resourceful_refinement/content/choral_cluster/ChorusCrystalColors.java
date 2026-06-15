package com.resourceful_refinement.content.choral_cluster;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.reflect.Field;

public class ChorusCrystalColors {
    private ChorusCrystalColors() {
    }

    private static Field sodiumLevelField = null;
    private static boolean checkedSodium = false;

    public static Level getLevelSafely(BlockGetter blockGetter) {
        // 1. Standard Vanilla check
        if (blockGetter instanceof Level level) {
            return level;
        }

        // 2. Fallback check for Sodium / Embeddium's "LevelSlice"
        if (blockGetter != null) {
            String className = blockGetter.getClass().getName();

            // Check if the class is a Sodium/Embeddium slice wrapper
            if (className.contains("LevelSlice") || className.contains("sodium")) {
                try {
                    if (!checkedSodium) {
                        // Locate the 'world' or 'level' field inside Sodium's LevelSlice
                        // Sodium's codebase historically names this field 'world' or 'level'
                        for (Field field : blockGetter.getClass().getDeclaredFields()) {
                            if (Level.class.isAssignableFrom(field.getType())) {
                                sodiumLevelField = field;
                                sodiumLevelField.setAccessible(true);
                                break;
                            }
                        }
                        checkedSodium = true;
                    }

                    if (sodiumLevelField != null) {
                        return (Level) sodiumLevelField.get(blockGetter);
                    }
                } catch (Exception e) {
                    // Fail gracefully so your mod doesn't crash the game if Sodium alters their field names
                    e.printStackTrace();
                }
            }
        }

        return null; // Could not determine the Level context
    }


    public static int blockTint(BlockState state, BlockAndTintGetter level, BlockPos pos, int tintIndex) {
        if (tintIndex != 0) {
            return 0xFFFFFFFF;
        }
        if (level == null || pos == null) {
            return itemTint(tintIndex);
        }
        double phaseBlocks = pos.getX() * ChorusCrystalColorSettings.X_AXIS_SLOPE
                + pos.getY()
                + pos.getZ() * ChorusCrystalColorSettings.Z_AXIS_SLOPE
                + ChorusCrystalColorSettings.PHASE_OFFSET;
        return colorAtPhase(phaseBlocks / ChorusCrystalColorSettings.PERIOD_BLOCKS, getLevelSafely(level));
    }

    public static int itemTint(int tintIndex) {
        return tintIndex == 0 ? colorAtPhase(ChorusCrystalColorSettings.ITEM_PHASE, null) : 0xFFFFFFFF;
    }

    public static int colorAtPhase(double phase, Level blockGetter) {
        float wrapped = (float) (phase - Math.floor(phase));

        ChorusCrystalColorSettings.ColorKey[] keys;
        if (blockGetter != null)
        {
            if (blockGetter.dimension() == Level.NETHER)
                keys = ChorusCrystalColorSettings.NETHER_COLOR_KEYS;
            else if (blockGetter.dimension() == Level.END)
                keys = ChorusCrystalColorSettings.COLOR_KEYS;
            else
                keys = ChorusCrystalColorSettings.RAINBOW_COLOR_KEYS;
        }
        else
            keys = ChorusCrystalColorSettings.COLOR_KEYS;

        if (keys.length == 0) {
            return 0xFFFFFFFF;
        }
        if (keys.length == 1) {
            return applyIntensity(keys[0].color());
        }

        ChorusCrystalColorSettings.ColorKey previous = keys[0];
        for (int i = 1; i < keys.length; i++) {
            ChorusCrystalColorSettings.ColorKey next = keys[i];
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
        float intensity = Mth.clamp(ChorusCrystalColorSettings.TINT_INTENSITY, 0.0F, 1.0F);
        float brightness = Math.max(0.0F, ChorusCrystalColorSettings.BRIGHTNESS_MULTIPLIER);
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
