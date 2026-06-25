package com.resourceful_refinement.content.combustion_chamber;

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

public class CombustionChamberItemRenderer  extends BlockEntityWithoutLevelRenderer {

    public static final CombustionChamberItemRenderer INSTANCE = new CombustionChamberItemRenderer();

    private CombustionChamberModel casing;

    public CombustionChamberItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    private void prepareModel() {
        if (casing != null) {
            return;
        }
        EntityModelSet models = Minecraft.getInstance().getEntityModels();
        casing = new CombustionChamberModel(models.bakeLayer(CombustionChamberModel.LAYER_LOCATION));
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

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(CombustionChamberRenderer.TEXTURE));
        casing.render(poseStack, buffer, packedLight, packedOverlay);

        poseStack.popPose();

        poseStack.popPose();
    }

    private static void applyDisplayTransform(ItemDisplayContext context, PoseStack poseStack) {
        switch (context) {
            case GUI -> {
                poseStack.translate(0.94, 0.24, 0.5);
                poseStack.scale(0.62f, 0.62f, 0.62f);

                // Standard item orientation
                poseStack.mulPose(Axis.YP.rotationDegrees(223f));
                poseStack.mulPose(Axis.ZP.rotationDegrees(-22f));
                poseStack.mulPose(Axis.XP.rotationDegrees(-22f));

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
                poseStack.translate(0.725F, 0.775F, 0.5F);
                poseStack.scale(0.4F * side, -0.4F, -0.4F);
                poseStack.mulPose(Axis.YP.rotationDegrees(220.0F));
                poseStack.mulPose(Axis.XP.rotationDegrees(-7.5F));
            }
            case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND -> {
                poseStack.translate(0.2F, 0.75F, 0.57F);
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
