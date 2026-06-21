package com.resourceful_refinement.utilities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.resourceful_refinement.content.refinery.RefineryAccessPortBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class FluidBoxRendering {

    public static void renderFluidsForTanks(PoseStack ms, MultiBufferSource buffer, int light, float availableHeight, float startY, float stackRadii, boolean isTranslucent, FluidTank... tanks) {

        float tankHeightLimit = availableHeight / tanks.length;
        float currentY = startY;

        for (int i = 0; i < tanks.length; i++) {
            if (tanks[i] == null)
                continue;

            currentY += renderFluidStack(tanks[i].getFluid(), tanks[i].getCapacity(), ms, buffer, light,
                    currentY, tankHeightLimit, stackRadii, isTranslucent);
        }
    }

    /**
     * Renders a fluid stack and returns the actual height it occupied.
     *
     * Tweakable Parameters:
     * - uScale / vScale: Controls texture tiling.
     * Since fluids are on the block atlas, values > 1.0 / box_width may cause
     * bleeding.
     * Default (1.0) stretches the texture once across the face.
     */
    public static float renderFluidStack(FluidStack stack, int capacity, PoseStack ms, MultiBufferSource buffer, int light,
                                   float yStart, float maxHeight, float stackRadii,boolean isTranslucent) {
        if (stack.isEmpty() || stack.getAmount() <= 0)
            return 0;

        float h = (float) stack.getAmount() / capacity * maxHeight;
        IClientFluidTypeExtensions props = IClientFluidTypeExtensions.of(stack.getFluid());
        int color = props.getTintColor(stack);
        ResourceLocation stillTexture = props.getStillTexture(stack);
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(stillTexture);

        // Texture Tiling Settings
        float uScale = 1.0f;
        float vScale = 0.85f;

        // Make sure it's translucent
        if ((color >> 24 & 0xFF) == 0xFF) {
            color = (color & 0x00FFFFFF) | (0xFF << 24);
        }

        VertexConsumer vc = isTranslucent? buffer.getBuffer(RenderType.translucent()) : buffer.getBuffer(RenderType.cutout());
        /*renderBox(ms, vc, -stackRadii, yStart, -stackRadii, stackRadii,
                yStart + h,
                stackRadii,
                color, light, sprite, uScale, vScale);*/

        renderTiledBox(ms, vc,
                -stackRadii, yStart, -stackRadii, stackRadii, yStart + h, stackRadii,
                color, light, sprite, 1f);

        return h;
    }

    public static void renderBox(PoseStack ms, VertexConsumer consumer, float x1, float y1, float z1, float x2, float y2,
                           float z2, int color, int light, TextureAtlasSprite sprite, float uScale, float vScale) {
        PoseStack.Pose pose = ms.last();

        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        int a = (color >> 24) & 0xFF;

        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        // Calculate UV widths based on scale
        float uw = (u1 - u0) * uScale;
        float vw = (v1 - v0) * vScale;

        // Bottom
        vertex(consumer, pose, x1, y1, z1, r, g, b, a, u0, v0, 0, -1, 0, light);
        vertex(consumer, pose, x2, y1, z1, r, g, b, a, u0 + uw, v0, 0, -1, 0, light);
        vertex(consumer, pose, x2, y1, z2, r, g, b, a, u0 + uw, v0 + vw, 0, -1, 0, light);
        vertex(consumer, pose, x1, y1, z2, r, g, b, a, u0, v0 + vw, 0, -1, 0, light);

        // Top
        vertex(consumer, pose, x1, y2, z1, r, g, b, a, u0, v0, 0, 1, 0, light);
        vertex(consumer, pose, x1, y2, z2, r, g, b, a, u0, v0 + vw, 0, 1, 0, light);
        vertex(consumer, pose, x2, y2, z2, r, g, b, a, u0 + uw, v0 + vw, 0, 1, 0, light);
        vertex(consumer, pose, x2, y2, z1, r, g, b, a, u0 + uw, v0, 0, 1, 0, light);

        // North
        vertex(consumer, pose, x1, y1, z1, r, g, b, a, u0, v0 + vw, 0, 0, -1, light);
        vertex(consumer, pose, x1, y2, z1, r, g, b, a, u0, v0, 0, 0, -1, light);
        vertex(consumer, pose, x2, y2, z1, r, g, b, a, u0 + uw, v0, 0, 0, -1, light);
        vertex(consumer, pose, x2, y1, z1, r, g, b, a, u0 + uw, v0 + vw, 0, 0, -1, light);

        // South
        vertex(consumer, pose, x1, y1, z2, r, g, b, a, u0, v0 + vw, 0, 0, 1, light);
        vertex(consumer, pose, x2, y1, z2, r, g, b, a, u0 + uw, v0 + vw, 0, 0, 1, light);
        vertex(consumer, pose, x2, y2, z2, r, g, b, a, u0 + uw, v0, 0, 0, 1, light);
        vertex(consumer, pose, x1, y2, z2, r, g, b, a, u0, v0, 0, 0, 1, light);

        // West
        vertex(consumer, pose, x1, y1, z1, r, g, b, a, u0, v0 + vw, -1, 0, 0, light);
        vertex(consumer, pose, x1, y1, z2, r, g, b, a, u0 + uw, v0 + vw, -1, 0, 0, light);
        vertex(consumer, pose, x1, y2, z2, r, g, b, a, u0 + uw, v0, -1, 0, 0, light);
        vertex(consumer, pose, x1, y2, z1, r, g, b, a, u0, v0, -1, 0, 0, light);

        // East
        vertex(consumer, pose, x2, y1, z1, r, g, b, a, u0, v0 + vw, 1, 0, 0, light);
        vertex(consumer, pose, x2, y2, z1, r, g, b, a, u0, v0, 1, 0, 0, light);
        vertex(consumer, pose, x2, y2, z2, r, g, b, a, u0 + uw, v0, 1, 0, 0, light);
        vertex(consumer, pose, x2, y1, z2, r, g, b, a, u0 + uw, v0 + vw, 1, 0, 0, light);
    }

    public static void renderTiledBox(PoseStack ms, VertexConsumer consumer, float x1, float y1, float z1, float x2, float y2, float z2,
                                      int color, int light, TextureAtlasSprite sprite, float tileSize) {
        PoseStack.Pose pose = ms.last();

        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        int a = (color >> 24) & 0xFF;

        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        float W = x2 - x1;
        float H = y2 - y1;
        float D = z2 - z1;

        // Bottom
        for (float dx = 0; dx < W; dx += tileSize) {
            float drawW = Math.min(W - dx, tileSize);
            float uEnd = u0 + (u1 - u0) * (drawW / tileSize);
            for (float dz = 0; dz < D; dz += tileSize) {
                float drawD = Math.min(D - dz, tileSize);
                float vEnd = v0 + (v1 - v0) * (drawD / tileSize);

                float sx1 = x1 + dx, sx2 = sx1 + drawW;
                float sz1 = z1 + dz, sz2 = sz1 + drawD;

                vertex(consumer, pose, sx1, y1, sz1, r, g, b, a, u0, v0, 0, -1, 0, light);
                vertex(consumer, pose, sx2, y1, sz1, r, g, b, a, uEnd, v0, 0, -1, 0, light);
                vertex(consumer, pose, sx2, y1, sz2, r, g, b, a, uEnd, vEnd, 0, -1, 0, light);
                vertex(consumer, pose, sx1, y1, sz2, r, g, b, a, u0, vEnd, 0, -1, 0, light);
            }
        }

        // Top
        for (float dx = 0; dx < W; dx += tileSize) {
            float drawW = Math.min(W - dx, tileSize);
            float uEnd = u0 + (u1 - u0) * (drawW / tileSize);
            for (float dz = 0; dz < D; dz += tileSize) {
                float drawD = Math.min(D - dz, tileSize);
                float vEnd = v0 + (v1 - v0) * (drawD / tileSize);

                float sx1 = x1 + dx, sx2 = sx1 + drawW;
                float sz1 = z1 + dz, sz2 = sz1 + drawD;

                vertex(consumer, pose, sx1, y2, sz1, r, g, b, a, u0, v0, 0, 1, 0, light);
                vertex(consumer, pose, sx1, y2, sz2, r, g, b, a, u0, vEnd, 0, 1, 0, light);
                vertex(consumer, pose, sx2, y2, sz2, r, g, b, a, uEnd, vEnd, 0, 1, 0, light);
                vertex(consumer, pose, sx2, y2, sz1, r, g, b, a, uEnd, v0, 0, 1, 0, light);
            }
        }

        // North
        for (float dx = 0; dx < W; dx += tileSize) {
            float drawW = Math.min(W - dx, tileSize);
            float uEnd = u0 + (u1 - u0) * (drawW / tileSize);
            for (float dy = 0; dy < H; dy += tileSize) {
                float drawH = Math.min(H - dy, tileSize);
                float vEnd = v0 + (v1 - v0) * (drawH / tileSize);

                float sx1 = x1 + dx, sx2 = sx1 + drawW;
                float sy2 = y2 - dy, sy1 = sy2 - drawH;

                vertex(consumer, pose, sx1, sy1, z1, r, g, b, a, u0, vEnd, 0, 0, -1, light);
                vertex(consumer, pose, sx1, sy2, z1, r, g, b, a, u0, v0, 0, 0, -1, light);
                vertex(consumer, pose, sx2, sy2, z1, r, g, b, a, uEnd, v0, 0, 0, -1, light);
                vertex(consumer, pose, sx2, sy1, z1, r, g, b, a, uEnd, vEnd, 0, 0, -1, light);
            }
        }

        // South
        for (float dx = 0; dx < W; dx += tileSize) {
            float drawW = Math.min(W - dx, tileSize);
            float uEnd = u0 + (u1 - u0) * (drawW / tileSize);
            for (float dy = 0; dy < H; dy += tileSize) {
                float drawH = Math.min(H - dy, tileSize);
                float vEnd = v0 + (v1 - v0) * (drawH / tileSize);

                float sx1 = x1 + dx, sx2 = sx1 + drawW;
                float sy2 = y2 - dy, sy1 = sy2 - drawH;

                vertex(consumer, pose, sx1, sy1, z2, r, g, b, a, u0, vEnd, 0, 0, 1, light);
                vertex(consumer, pose, sx2, sy1, z2, r, g, b, a, uEnd, vEnd, 0, 0, 1, light);
                vertex(consumer, pose, sx2, sy2, z2, r, g, b, a, uEnd, v0, 0, 0, 1, light);
                vertex(consumer, pose, sx1, sy2, z2, r, g, b, a, u0, v0, 0, 0, 1, light);
            }
        }

        // West
        for (float dz = 0; dz < D; dz += tileSize) {
            float drawD = Math.min(D - dz, tileSize);
            float uEnd = u0 + (u1 - u0) * (drawD / tileSize);
            for (float dy = 0; dy < H; dy += tileSize) {
                float drawH = Math.min(H - dy, tileSize);
                float vEnd = v0 + (v1 - v0) * (drawH / tileSize);

                float sz1 = z1 + dz, sz2 = sz1 + drawD;
                float sy2 = y2 - dy, sy1 = sy2 - drawH;

                vertex(consumer, pose, x1, sy1, sz1, r, g, b, a, u0, vEnd, -1, 0, 0, light);
                vertex(consumer, pose, x1, sy1, sz2, r, g, b, a, uEnd, vEnd, -1, 0, 0, light);
                vertex(consumer, pose, x1, sy2, sz2, r, g, b, a, uEnd, v0, -1, 0, 0, light);
                vertex(consumer, pose, x1, sy2, sz1, r, g, b, a, u0, v0, -1, 0, 0, light);
            }
        }

        // East
        for (float dz = 0; dz < D; dz += tileSize) {
            float drawD = Math.min(D - dz, tileSize);
            float uEnd = u0 + (u1 - u0) * (drawD / tileSize);
            for (float dy = 0; dy < H; dy += tileSize) {
                float drawH = Math.min(H - dy, tileSize);
                float vEnd = v0 + (v1 - v0) * (drawH / tileSize);

                float sz1 = z1 + dz, sz2 = sz1 + drawD;
                float sy2 = y2 - dy, sy1 = sy2 - drawH;

                vertex(consumer, pose, x2, sy1, sz1, r, g, b, a, u0, vEnd, 1, 0, 0, light);
                vertex(consumer, pose, x2, sy2, sz1, r, g, b, a, u0, v0, 1, 0, 0, light);
                vertex(consumer, pose, x2, sy2, sz2, r, g, b, a, uEnd, v0, 1, 0, 0, light);
                vertex(consumer, pose, x2, sy1, sz2, r, g, b, a, uEnd, vEnd, 1, 0, 0, light);
            }
        }
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z, int r, int g,
                        int b, int a, float u, float v, float nx, float ny, float nz, int light) {
        consumer.addVertex(pose, x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }

}
