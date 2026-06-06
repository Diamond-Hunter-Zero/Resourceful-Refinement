package com.resourceful_refinement.content.hosegun;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.resourceful_refinement.ResourcefulRefinementMain;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

public class HosegunItemRenderer extends BlockEntityWithoutLevelRenderer {

    public static final HosegunItemRenderer INSTANCE = new HosegunItemRenderer();

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "hosegun"), "main");

    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "textures/item/hosegun.png");

    private HosegunModel model;

    public HosegunItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    private void prepareModel() {
        if (model != null) {
            return;
        }
        EntityModelSet models = Minecraft.getInstance().getEntityModels();
        model = new HosegunModel(models.bakeLayer(LAYER));
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

        FluidStack fluid = new HosegunItem.HosegunFluidHandler(stack).getFluid();
        float fillRatio = fluid.isEmpty() ? 0.0F : (float) fluid.getAmount() / HosegunItem.CAPACITY;
        int fluidTint = HosegunFluidColors.getTint(fluid);

        poseStack.pushPose();
        applyDisplayTransform(context, poseStack);

        poseStack.pushPose();
        poseStack.scale(1.0F, -1.0F, -1.0F);
        poseStack.translate(0.0F, -1.5F, 0.0F);

        var cutout = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        model.renderBody(poseStack, cutout, packedLight, packedOverlay);

        if (fillRatio > 0.0F) {
            var fluidBuffer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
            model.renderFluidTanks(poseStack, fluidBuffer, packedLight, OverlayTexture.NO_OVERLAY, fillRatio, fluidTint);
        }

        poseStack.popPose();
        poseStack.popPose();
    }

    private static void applyDisplayTransform(ItemDisplayContext context, PoseStack poseStack) {
        switch (context) {
            case GUI -> {
                poseStack.translate(1.075F, 0.65F, 0F);
                poseStack.scale(0.7F, 0.7F, 0.7F);
                poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
                poseStack.translate(-0.5F, -0.5F, -0.5F);
            }
            case GROUND -> {
                poseStack.translate(0.7F, 0.5F, 0.375F);
                poseStack.scale(0.3F, 0.3F, 0.3F);
                poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
                poseStack.translate(-0.5F, -0.5F, -0.5F);
            }
            case FIXED -> {
                poseStack.translate(1.1F, 0.8F, -0.05F);
                poseStack.scale(0.9F, 0.9F, 0.9F);
                poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
                poseStack.translate(-0.5F, -0.5F, -0.5F);
            }
            case FIRST_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND -> {
                float sideFactor = (context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ? -1f : 1f);
                float sideOffset = (context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ? 13f : 1f);
                poseStack.translate(sideOffset * 0.075F, 1F, 0.25F);
                poseStack.scale(0.5F * sideFactor, 0.5F, 0.5F);
                poseStack.mulPose(Axis.YP.rotationDegrees(180));
                poseStack.mulPose(Axis.XP.rotationDegrees(-15.0F));
                //poseStack.mulPose(Axis.ZP.rotationDegrees(left ? 5.0F : -5.0F));
                poseStack.translate(-0.5F, -0.5F, -0.5F);
            }
            case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND -> {
                boolean left = context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
                poseStack.translate(0.25F, 0.6F, 0F);
                poseStack.scale(0.55F, 0.55F, 0.55F);
                poseStack.mulPose(Axis.YP.rotationDegrees(180));
                poseStack.mulPose(Axis.XP.rotationDegrees(0.0F));
                poseStack.translate(-0.5F, -0.75F, -0.5F);
            }
            default -> {
                poseStack.translate(0.5F, 0.5F, 0.5F);
                poseStack.scale(0.5F, 0.5F, 0.5F);
                poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
                poseStack.translate(-0.5F, -0.5F, -0.5F);
            }
        }
    }
}
