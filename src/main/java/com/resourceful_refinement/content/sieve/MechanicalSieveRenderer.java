package com.resourceful_refinement.content.sieve;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class MechanicalSieveRenderer extends KineticBlockEntityRenderer<MechanicalFluidSieveBlockEntity> {

    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "textures/block/mechanical_sieve.png");

    private final MechanicalSieveCasingModel casingSingle;
    private final MechanicalSieveCasingBottomModel casingBottom;
    private final MechanicalSieveCasingMiddleModel casingMiddle;
    private final MechanicalSieveCasingTopModel casingTop;
    private final MechanicalSieveCogModel cog;

    public MechanicalSieveRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.casingSingle = new MechanicalSieveCasingModel(context.bakeLayer(MechanicalSieveLayers.CASING));
        this.casingBottom = new MechanicalSieveCasingBottomModel(context.bakeLayer(MechanicalSieveLayers.CASING_BOTTOM));
        this.casingMiddle = new MechanicalSieveCasingMiddleModel(context.bakeLayer(MechanicalSieveLayers.CASING_MIDDLE));
        this.casingTop = new MechanicalSieveCasingTopModel(context.bakeLayer(MechanicalSieveLayers.CASING_TOP));
        this.cog = new MechanicalSieveCogModel(context.bakeLayer(MechanicalSieveLayers.COG));
    }

    @Override
    protected void renderSafe(MechanicalFluidSieveBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        Direction facing = be.getBlockState().getValue(MechanicalFluidSieveBlock.FACING);
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutout(TEXTURE));

        ms.pushPose();

        // 1. Move to center of block
        ms.translate(0.5, 0, 0.5);

        // 2. Rotate based on facing (front face)
        ms.mulPose(Axis.YP.rotationDegrees(-facing.toYRot() + 180));

        // 3. Align with entity model coordinate system (upside down)
        ms.scale(-1.0F, -1.0F, 1.0F);
        ms.translate(0, -1.5, 0);

        // Select correct casing model
        if (be.stackSize <= 1) {
            casingSingle.render(ms, vertexConsumer, light, overlay);
        } else if (be.stackIndex == 0) {
            casingBottom.render(ms, vertexConsumer, light, overlay);
        } else if (be.stackIndex == be.stackSize - 1) {
            casingTop.render(ms, vertexConsumer, light, overlay);
        } else {
            casingMiddle.render(ms, vertexConsumer, light, overlay);
        }

        // Calculate kinetic rotation angle (radians to degrees)
        float networkAngleRad = getAngleForBe(be, be.getBlockPos(), Direction.Axis.Y);
        float networkAngleDeg = networkAngleRad * Mth.RAD_TO_DEG;
        float altNetworkAngleDeg = networkAngleDeg * 0.5f;

        if (be.getSpeed() == 0)
        {
            networkAngleDeg = 0;
            altNetworkAngleDeg = 22.5f;
        }

        // Render Top Cog (Inverted direction from middle, Half speed)
        // Raised by 2 pixels: -8/16f -> -10/16f
        ms.pushPose();
        ms.translate(0, -10 / 16f, 0);
        ms.mulPose(Axis.YP.rotationDegrees(altNetworkAngleDeg));
        cog.render(ms, vertexConsumer, light, overlay);
        ms.popPose();

        // Render Middle Cog (Network speed and direction)
        // Raised by 2 pixels: -4/16f -> -6/16f
        ms.pushPose();
        ms.translate(0, -6 / 16f, 0);
        ms.mulPose(Axis.YP.rotationDegrees(-networkAngleDeg));
        cog.render(ms, vertexConsumer, light, overlay);
        ms.popPose();

        // Render Bottom Cog (Inverted direction from middle, Half speed)
        // Raised by 2 pixels: 0 -> -2/16f
        ms.pushPose();
        ms.translate(0, -2 / 16f, 0);
        ms.mulPose(Axis.YP.rotationDegrees(altNetworkAngleDeg));
        cog.render(ms, vertexConsumer, light, overlay);
        ms.popPose();

        ms.popPose();

        // Render filter slot
        if (be.stackIndex == be.stackSize - 1)
            FilteringRenderer.renderOnBlockEntity(be, partialTicks, ms, buffer, light, overlay);
    }

    @Override
    public int getViewDistance() {
        return 128;
    }
}
