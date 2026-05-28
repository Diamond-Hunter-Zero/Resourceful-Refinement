package com.resourceful_refinement.content.refill_station;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Renders the Fluid Refill Station casing model for inventory, GUI, and held displays.
 */
public class FluidRefillStationItemRenderer extends BlockEntityWithoutLevelRenderer {

    public static final FluidRefillStationItemRenderer INSTANCE = new FluidRefillStationItemRenderer();

    private FluidRefillStationCasingModel casing;

    public FluidRefillStationItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    private void prepareModel() {
        if (casing != null) {
            return;
        }
        EntityModelSet models = Minecraft.getInstance().getEntityModels();
        casing = new FluidRefillStationCasingModel(models.bakeLayer(FluidRefillStationLayers.CASING));
    }

    @Override
    public void renderByItem(
            ItemStack stack,
            ItemDisplayContext context,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        prepareModel();

        poseStack.pushPose();
        applyDisplayTransform(context, poseStack);

        poseStack.pushPose();
        poseStack.translate(0.5F, 1.5F, 0.5F);
        poseStack.scale(1.0F, -1.0F, -1.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(FluidRefillStationRenderer.TEXTURE));
        casing.render(poseStack, buffer, packedLight, packedOverlay);

        poseStack.popPose();
        poseStack.popPose();
    }

    private static void applyDisplayTransform(ItemDisplayContext context, PoseStack poseStack) {
        switch (context) {
            case GUI -> {
                poseStack.translate(0.5F, 0.95F, 0.5F);
                poseStack.scale(0.55F, -0.55F, -0.55F);
                poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
                poseStack.mulPose(Axis.XP.rotationDegrees(25.0F));
            }
            case GROUND -> {
                poseStack.translate(0.5F, 0.85F, 0.5F);
                poseStack.scale(0.3F, -0.3F, -0.3F);
            }
            case FIXED -> {
                poseStack.translate(0.5F, 0.75F, 0.5F);
                poseStack.scale(0.45F, -0.45F, -0.45F);
                poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
            }
            case FIRST_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND -> {
                float side = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ? -1.0F : 1.0F;
                poseStack.translate(0.5F, 0.9F, 0.5F);
                poseStack.scale(0.4F * side, -0.4F, -0.4F);
                poseStack.mulPose(Axis.YP.rotationDegrees(200.0F));
                poseStack.mulPose(Axis.XP.rotationDegrees(-10.0F));
            }
            case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND -> {
                poseStack.translate(0.5F, 0.75F, 0.65F);
                poseStack.scale(0.4F, -0.4F, -0.4F);
                poseStack.mulPose(Axis.XP.rotationDegrees(75.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
            }
            default -> {
                poseStack.translate(0.5F, 0.5F, 0.5F);
                poseStack.scale(0.5F, -0.5F, -0.5F);
            }
        }
    }
}
