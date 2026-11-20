package com.resourceful_refinement.content.tesla_coil;

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

/** Item form of the Tesla Coil: the upright head on its own. */
public class TeslaCoilItemRenderer extends BlockEntityWithoutLevelRenderer {

    public static final TeslaCoilItemRenderer INSTANCE = new TeslaCoilItemRenderer();

    private TeslaCoilHeadModel model;

    public TeslaCoilItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    private void prepareModel() {
        if (model != null) {
            return;
        }
        EntityModelSet models = Minecraft.getInstance().getEntityModels();
        model = new TeslaCoilHeadModel(models.bakeLayer(TeslaCoilHeadModel.LAYER_LOCATION));
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack,
                             MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        prepareModel();

        poseStack.pushPose();
        applyDisplayTransform(context, poseStack);

        poseStack.translate(0.5F, 1.5F, 0.5F);
        poseStack.scale(1.0F, -1.0F, -1.0F);

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TeslaCoilRenderer.TEXTURE));
        model.render(poseStack, buffer, packedLight, packedOverlay);

        poseStack.popPose();
    }

    private static void applyDisplayTransform(ItemDisplayContext context, PoseStack poseStack) {
        switch (context) {
            case GUI -> {
                poseStack.translate(0.5F, 0.5F, 0.5F);
                poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(225.0F));
                poseStack.scale(0.625F, 0.625F, 0.625F);
                poseStack.translate(-0.5F, -0.45F, -0.5F);
            }
            case GROUND -> {
                poseStack.translate(0.5F, 0.3F, 0.5F);
                poseStack.scale(0.3F, 0.3F, 0.3F);
                poseStack.translate(-0.5F, 0.0F, -0.5F);
            }
            case FIXED -> {
                poseStack.translate(0.5F, 0.5F, 0.5F);
                poseStack.scale(0.5F, 0.5F, 0.5F);
                poseStack.translate(-0.5F, -0.5F, -0.5F);
            }
            case FIRST_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND -> {
                poseStack.translate(0.5F, 0.4F, 0.5F);
                poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
                poseStack.scale(0.4F, 0.4F, 0.4F);
                poseStack.translate(-0.5F, -0.5F, -0.5F);
            }
            case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND -> {
                poseStack.translate(0.5F, 0.55F, 0.5F);
                poseStack.mulPose(Axis.XP.rotationDegrees(75.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
                poseStack.scale(0.375F, 0.375F, 0.375F);
                poseStack.translate(-0.5F, -0.5F, -0.5F);
            }
            default -> {
                poseStack.translate(0.5F, 0.5F, 0.5F);
                poseStack.scale(0.5F, 0.5F, 0.5F);
                poseStack.translate(-0.5F, -0.5F, -0.5F);
            }
        }
    }
}
