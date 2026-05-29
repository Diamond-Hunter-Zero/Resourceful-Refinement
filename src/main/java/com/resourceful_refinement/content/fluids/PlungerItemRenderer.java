package com.resourceful_refinement.content.fluids;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.resourceful_refinement.ResourcefulRefinementMain;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class PlungerItemRenderer extends BlockEntityWithoutLevelRenderer {

    public static final PlungerItemRenderer INSTANCE = new PlungerItemRenderer();

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "plunger"), "main");

    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "textures/item/plunger.png");

    private PlungerModel model;

    public PlungerItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    private void prepareModel() {
        if (model != null) {
            return;
        }
        EntityModelSet models = Minecraft.getInstance().getEntityModels();
        model = new PlungerModel(models.bakeLayer(LAYER));
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
        poseStack.scale(1.0F, -1.0F, -1.0F);
        //poseStack.translate(-0.8F, -1.4F, 0.0F);

        var cutout = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        model.render(poseStack, cutout, packedLight, packedOverlay);

        poseStack.popPose();
        poseStack.popPose();
    }

    private static void applyDisplayTransform(ItemDisplayContext context, PoseStack poseStack) {
        switch (context) {
            case GUI -> {
                poseStack.scale(0.85F, 0.85F, 0.85F);
                poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(225.0F));
                poseStack.translate(-0.8F, 1.4F, 0F);
            }
            case GROUND -> {
                poseStack.translate(0.5F, 1F, 0.5F);
                poseStack.scale(0.4F, 0.4F, 0.4F);
                poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            }
            case FIXED -> {
                poseStack.translate(0.5F, 1.25F, 0.5F);
                poseStack.scale(0.75F, 0.75F, 0.75F);
                //poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            }
            case FIRST_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND -> {
                float side = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ? -1.0F : 1.0F;

                poseStack.translate((0.55F + 0.1F * side), 0.35F, 0.8F);
                poseStack.mulPose(Axis.ZP.rotationDegrees(-180.0F));
                poseStack.mulPose(Axis.XP.rotationDegrees(40.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(-00.0F));

                poseStack.scale(0.55F, 0.55F, 0.55F);

            }
            case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND -> {
                poseStack.scale(0.6F, 0.6F, 0.6F);
                //poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
                poseStack.translate(-0.85F, 0.0F, 0.9F);
            }
            default -> {
                poseStack.translate(0.5F, 0.5F, 0.5F);
                poseStack.scale(0.5F, 0.5F, 0.5F);
                poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
            }
        }
    }
}
