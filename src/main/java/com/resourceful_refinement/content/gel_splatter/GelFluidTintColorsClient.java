package com.resourceful_refinement.content.gel_splatter;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Vector3f;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
public final class GelFluidTintColorsClient {

    private static final float FOG_COLOR_EPSILON = 0.002f;
    /** Baseline passed to {@link IClientFluidTypeExtensions#modifyFogColor} for default-fog detection. */
    private static final Vector3f FOG_COLOR_PROBE = new Vector3f(0.5f, 0.75f, 1.0f);

    private static final Map<ResourceLocation, Integer> COMPAT_TINT_CACHE = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, Block> LIQUID_BLOCK_CACHE = new ConcurrentHashMap<>();
    private static final Set<ResourceLocation> NO_LIQUID_BLOCK = ConcurrentHashMap.newKeySet();

    private GelFluidTintColorsClient() {}

    public static void install() {
        GelFluidTintColors.bindClientCompat(
                GelFluidTintColorsClient::resolveCompatTint,
                GelFluidTintColorsClient::clearCompatCache);
    }

    static int resolveCompatTint(Fluid fluid) {
        ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid);
        if (id == null) {
            return 0xFFFFFFFF;
        }

        return COMPAT_TINT_CACHE.computeIfAbsent(id, key -> computeCompatTint(fluid));
    }

    static void clearCompatCache() {
        COMPAT_TINT_CACHE.clear();
        LIQUID_BLOCK_CACHE.clear();
        NO_LIQUID_BLOCK.clear();
    }

    private static int computeCompatTint(Fluid fluid) {
        IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(fluid);
        FluidStack stack = new FluidStack(fluid, 1000);

        Integer fogRgb = resolveRegisteredFogColor(extensions);
        if (fogRgb != null) {
            return fogRgb;
        }

        Integer mapRgb = resolveFluidMapColor(fluid);
        if (mapRgb != null) {
            return mapRgb;
        }

        int textureTint = extensions.getTintColor(stack);
        if ((textureTint & 0xFFFFFF) != 0xFFFFFF) {
            return textureTint & 0xFFFFFF;
        }

        return 0xFFFFFFFF;
    }

    /**
     * Uses {@link IClientFluidTypeExtensions#modifyFogColor} when it differs from the interface default
     * (i.e. the fluid registered a custom fog colour).
     */
    private static Integer resolveRegisteredFogColor(IClientFluidTypeExtensions extensions) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            return null;
        }

        Camera camera = minecraft.gameRenderer.getMainCamera();
        int renderDistance = minecraft.options.getEffectiveRenderDistance();
        Vector3f probe = new Vector3f(FOG_COLOR_PROBE);

        Vector3f defaultFog = IClientFluidTypeExtensions.DEFAULT.modifyFogColor(
                camera, 1.0F, level, renderDistance, 0.0F, new Vector3f(probe));
        Vector3f customFog = extensions.modifyFogColor(
                camera, 1.0F, level, renderDistance, 0.0F, new Vector3f(probe));

        if (!fogColorsApproximatelyEqual(defaultFog, customFog)) {
            return vector3fToRgb(customFog);
        }

        return null;
    }

    private static Integer resolveFluidMapColor(Fluid fluid) {
        Block block = findLiquidBlock(fluid);
        if (block == null) {
            return null;
        }

        BlockState state = block.defaultBlockState();
        MapColor mapColor = state.getMapColor(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
        if (mapColor == MapColor.NONE) {
            return null;
        }

        return mapColor.col & 0xFFFFFF;
    }

    private static Block findLiquidBlock(Fluid fluid) {
        ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid);
        if (id == null || NO_LIQUID_BLOCK.contains(id)) {
            return null;
        }

        Block cached = LIQUID_BLOCK_CACHE.get(id);
        if (cached != null) {
            return cached;
        }

        for (Block block : BuiltInRegistries.BLOCK) {
            if (block instanceof LiquidBlock liquidBlock && liquidBlock.fluid.isSame(fluid)) {
                LIQUID_BLOCK_CACHE.put(id, block);
                return block;
            }
        }

        NO_LIQUID_BLOCK.add(id);
        return null;
    }

    private static boolean fogColorsApproximatelyEqual(Vector3f a, Vector3f b) {
        return Math.abs(a.x - b.x) < FOG_COLOR_EPSILON
                && Math.abs(a.y - b.y) < FOG_COLOR_EPSILON
                && Math.abs(a.z - b.z) < FOG_COLOR_EPSILON;
    }

    private static int vector3fToRgb(Vector3f color) {
        int r = Math.clamp(Math.round(color.x * 255.0F), 0, 255);
        int g = Math.clamp(Math.round(color.y * 255.0F), 0, 255);
        int b = Math.clamp(Math.round(color.z * 255.0F), 0, 255);
        return (r << 16) | (g << 8) | b;
    }
}
