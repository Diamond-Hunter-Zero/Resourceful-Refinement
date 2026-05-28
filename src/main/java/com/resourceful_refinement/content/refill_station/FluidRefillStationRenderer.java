package com.resourceful_refinement.content.refill_station;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

import static com.resourceful_refinement.content.refill_station.FluidRefillStationBlock.FACING;

public class FluidRefillStationRenderer extends SafeBlockEntityRenderer<FluidRefillStationBlockEntity> {

    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "textures/block/fluid_refill_station.png");

    /** Interior tank bounds in block-space units (inside the casing). */
    private static final float INNER_MIN = 2.5F / 16F;
    private static final float INNER_MAX = 13.5F / 16F;
    private static final float INNER_MIN_HEIGHT = 4F / 16F;
    private static final float INNER_MAX_HEIGHT = 13.5F / 16F;
    private static final float INNER_HEIGHT = INNER_MAX_HEIGHT - INNER_MIN_HEIGHT;

    private final FluidRefillStationCasingModel casing;

    public FluidRefillStationRenderer(BlockEntityRendererProvider.Context context) {
        this.casing = new FluidRefillStationCasingModel(context.bakeLayer(FluidRefillStationLayers.CASING));
    }

    @Override
    protected void renderSafe(FluidRefillStationBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
            int light, int overlay) {
        BlockState state = be.getBlockState();
        Direction facing = state.getValue(FACING);

        VertexConsumer casingBuffer = buffer.getBuffer(RenderType.entityCutout(TEXTURE));

        ms.pushPose();
        ms.translate(0.5, 1.5, 0.5);
        ms.scale(1, -1, -1);
        ms.mulPose(Axis.YP.rotationDegrees(facing.toYRot()));
        casing.render(ms, casingBuffer, light, overlay);
        ms.popPose();

        FluidStack fluid = be.tank.getFluid();
        if (!fluid.isEmpty()) {
            renderInteriorFluid(be, ms, buffer, light, facing);
        }
    }

    private void renderInteriorFluid(FluidRefillStationBlockEntity be, PoseStack ms, MultiBufferSource buffer, int light,
            Direction facing) {
        FluidStack stack = be.tank.getFluid();
        float fillHeight = INNER_MIN_HEIGHT + INNER_HEIGHT * be.getFillRatio();
        if (fillHeight <= INNER_MIN_HEIGHT) {
            return;
        }

        IClientFluidTypeExtensions props = IClientFluidTypeExtensions.of(stack.getFluid());
        int color = props.getTintColor(stack);
        if ((color >> 24 & 0xFF) == 0xFF) {
            color = (color & 0x00FFFFFF) | (0xC8 << 24);
        }

        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(props.getStillTexture(stack));

        ms.pushPose();

        renderFluidBox(ms, buffer.getBuffer(RenderType.cutout()),
                INNER_MIN, INNER_MIN_HEIGHT, INNER_MIN,
                INNER_MAX, fillHeight, INNER_MAX,
                color, light, sprite);
        ms.popPose();
    }

    private static void renderFluidBox(PoseStack ms, VertexConsumer consumer,
            float x1, float y1, float z1, float x2, float y2, float z2,
            int color, int light, TextureAtlasSprite sprite) {
        PoseStack.Pose pose = ms.last();
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        int a = (color >> 24) & 0xFF;

        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        // Bottom (-Y)
        vertex(consumer, pose, x1, y1, z1, r, g, b, a, u0, v1, 0, -1, 0, light);
        vertex(consumer, pose, x2, y1, z1, r, g, b, a, u1, v1, 0, -1, 0, light);
        vertex(consumer, pose, x2, y1, z2, r, g, b, a, u1, v0, 0, -1, 0, light);
        vertex(consumer, pose, x1, y1, z2, r, g, b, a, u0, v0, 0, -1, 0, light);

        // Top (+Y)
        vertex(consumer, pose, x1, y2, z1, r, g, b, a, u0, v1, 0, 1, 0, light);
        vertex(consumer, pose, x1, y2, z2, r, g, b, a, u0, v0, 0, 1, 0, light);
        vertex(consumer, pose, x2, y2, z2, r, g, b, a, u1, v0, 0, 1, 0, light);
        vertex(consumer, pose, x2, y2, z1, r, g, b, a, u1, v1, 0, 1, 0, light);

        // North (-Z)
        vertex(consumer, pose, x1, y1, z1, r, g, b, a, u0, v0, 0, 0, -1, light);
        vertex(consumer, pose, x1, y2, z1, r, g, b, a, u0, v1, 0, 0, -1, light);
        vertex(consumer, pose, x2, y2, z1, r, g, b, a, u1, v1, 0, 0, -1, light);
        vertex(consumer, pose, x2, y1, z1, r, g, b, a, u1, v0, 0, 0, -1, light);

        // South (+Z)
        vertex(consumer, pose, x1, y1, z2, r, g, b, a, u0, v0, 0, 0, 1, light);
        vertex(consumer, pose, x2, y1, z2, r, g, b, a, u1, v0, 0, 0, 1, light);
        vertex(consumer, pose, x2, y2, z2, r, g, b, a, u1, v1, 0, 0, 1, light);
        vertex(consumer, pose, x1, y2, z2, r, g, b, a, u0, v1, 0, 0, 1, light);

        // West (-X)
        vertex(consumer, pose, x1, y1, z1, r, g, b, a, u0, v0, -1, 0, 0, light);
        vertex(consumer, pose, x1, y1, z2, r, g, b, a, u1, v0, -1, 0, 0, light);
        vertex(consumer, pose, x1, y2, z2, r, g, b, a, u1, v1, -1, 0, 0, light);
        vertex(consumer, pose, x1, y2, z1, r, g, b, a, u0, v1, -1, 0, 0, light);

        // East (+X)
        vertex(consumer, pose, x2, y1, z1, r, g, b, a, u0, v0, 1, 0, 0, light);
        vertex(consumer, pose, x2, y2, z1, r, g, b, a, u0, v1, 1, 0, 0, light);
        vertex(consumer, pose, x2, y2, z2, r, g, b, a, u1, v1, 1, 0, 0, light);
        vertex(consumer, pose, x2, y1, z2, r, g, b, a, u1, v0, 1, 0, 0, light);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose,
            float x, float y, float z,
            int r, int g, int b, int a,
            float u, float v,
            float nx, float ny, float nz, int light) {
        consumer.addVertex(pose, x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }
}
