package com.resourceful_refinement.content.geyser;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.resourceful_refinement.registry.ModPartialModels;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public class GeyserRenderer extends SafeBlockEntityRenderer<GeyserBlockEntity> {

    public GeyserRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void renderSafe(GeyserBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        BlockState state = be.getBlockState();
        
        // --- 1. Render Randomized Outer Casing ---
        // Use the block position as a seed for stable randomization
        long seed = be.getBlockPos().asLong();
        RandomSource random = RandomSource.create(seed);
        
        float xRot = random.nextInt(4) * (float)Math.PI / 2f;
        float yRot = random.nextInt(4) * (float)Math.PI / 2f;
        float zRot = random.nextInt(4) * (float)Math.PI / 2f;

        SuperByteBuffer casing;
        if (be.getLevel() != null && be.getLevel().dimension() == Level.NETHER)
            casing = CachedBuffers.partial(ModPartialModels.NETHERRACK_GEYSER_CASING, state);
        else
            casing = CachedBuffers.partial(com.resourceful_refinement.registry.ModPartialModels.GEYSER_CASING, state);
        
        ms.pushPose();
        casing.rotateCentered(xRot, Direction.Axis.X)
              .rotateCentered(yRot, Direction.Axis.Y)
              .rotateCentered(zRot, Direction.Axis.Z)
              .light(light)
              .renderInto(ms, buffer.getBuffer(RenderType.cutout()));
        ms.popPose();

        // --- 2. Render Interior Fluid ---
        renderFluidCube(ms, buffer, be.getAssociatedFluid(), light, 0.005f);
    }

    static void renderFluidCube(PoseStack ms, MultiBufferSource buffer, Fluid fluid, int light, float margin) {
        if (fluid == null || fluid == Fluids.EMPTY) return;

        FluidStack stack = new FluidStack(fluid, 1000);
        IClientFluidTypeExtensions props = IClientFluidTypeExtensions.of(fluid);
        int color = props.getTintColor(stack);
        net.minecraft.resources.ResourceLocation stillTexture = props.getStillTexture(stack);

        if (stillTexture == null) return;

        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(stillTexture);

        // Make fluid visible but translucent (alpha 80%) if opaque
        if ((color >> 24 & 0xFF) == 0xFF) {
            color = (color & 0x00FFFFFF) | (0xC8 << 24);
        }

        VertexConsumer consumer = buffer.getBuffer(RenderType.translucent());
        renderBox(ms, consumer, margin, margin, margin, 1f - margin, 1f - margin, 1f - margin, color, light, sprite);
    }

    private static void renderBox(PoseStack ms, VertexConsumer consumer, float x1, float y1, float z1, float x2, float y2,
                           float z2, int color, int light, TextureAtlasSprite sprite) {
        PoseStack.Pose pose = ms.last();

        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        int a = (color >> 24) & 0xFF;

        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        // Bottom
        vertex(consumer, pose, x1, y1, z1, r, g, b, a, u0, v0, 0, -1, 0, light);
        vertex(consumer, pose, x2, y1, z1, r, g, b, a, u1, v0, 0, -1, 0, light);
        vertex(consumer, pose, x2, y1, z2, r, g, b, a, u1, v1, 0, -1, 0, light);
        vertex(consumer, pose, x1, y1, z2, r, g, b, a, u0, v1, 0, -1, 0, light);

        // Top
        vertex(consumer, pose, x1, y2, z1, r, g, b, a, u0, v0, 0, 1, 0, light);
        vertex(consumer, pose, x1, y2, z2, r, g, b, a, u0, v1, 0, 1, 0, light);
        vertex(consumer, pose, x2, y2, z2, r, g, b, a, u1, v1, 0, 1, 0, light);
        vertex(consumer, pose, x2, y2, z1, r, g, b, a, u1, v0, 0, 1, 0, light);

        // North
        vertex(consumer, pose, x1, y1, z1, r, g, b, a, u0, v1, 0, 0, -1, light);
        vertex(consumer, pose, x1, y2, z1, r, g, b, a, u0, v0, 0, 0, -1, light);
        vertex(consumer, pose, x2, y2, z1, r, g, b, a, u1, v0, 0, 0, -1, light);
        vertex(consumer, pose, x2, y1, z1, r, g, b, a, u1, v1, 0, 0, -1, light);

        // South
        vertex(consumer, pose, x1, y1, z2, r, g, b, a, u0, v1, 0, 0, 1, light);
        vertex(consumer, pose, x2, y1, z2, r, g, b, a, u1, v1, 0, 0, 1, light);
        vertex(consumer, pose, x2, y2, z2, r, g, b, a, u1, v0, 0, 0, 1, light);
        vertex(consumer, pose, x1, y2, z2, r, g, b, a, u0, v0, 0, 0, 1, light);

        // West
        vertex(consumer, pose, x1, y1, z1, r, g, b, a, u0, v1, -1, 0, 0, light);
        vertex(consumer, pose, x1, y1, z2, r, g, b, a, u1, v1, -1, 0, 0, light);
        vertex(consumer, pose, x1, y2, z2, r, g, b, a, u1, v0, -1, 0, 0, light);
        vertex(consumer, pose, x1, y2, z1, r, g, b, a, u0, v0, -1, 0, 0, light);

        // East
        vertex(consumer, pose, x2, y1, z1, r, g, b, a, u0, v1, 1, 0, 0, light);
        vertex(consumer, pose, x2, y2, z1, r, g, b, a, u0, v0, 1, 0, 0, light);
        vertex(consumer, pose, x2, y2, z2, r, g, b, a, u1, v0, 1, 0, 0, light);
        vertex(consumer, pose, x2, y1, z2, r, g, b, a, u1, v1, 1, 0, 0, light);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z, int r, int g,
                        int b, int a, float u, float v, float nx, float ny, float nz, int light) {
        consumer.addVertex(pose, x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }

    @Override
    public int getViewDistance() {
        return 128;
    }
}
