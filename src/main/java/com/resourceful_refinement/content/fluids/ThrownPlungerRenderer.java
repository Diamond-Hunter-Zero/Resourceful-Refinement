package com.resourceful_refinement.content.fluids;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * In-flight and in-ground rendering for {@link ThrownPlunger}, using the same model as {@link PlungerItemRenderer}.
 */
@OnlyIn(Dist.CLIENT)
public class ThrownPlungerRenderer extends EntityRenderer<ThrownPlunger> {

    private final PlungerModel model;

    public ThrownPlungerRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new PlungerModel(context.bakeLayer(PlungerItemRenderer.LAYER));
    }

    @Override
    public void render(
            ThrownPlunger entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight
    ) {
        poseStack.pushPose();

        // Match vanilla trident projectile orientation so the plunger flies point-first.
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTick, entity.xRotO, entity.getXRot()) + 90.0F));
        if (entity.isStuckInGround()) {
            poseStack.mulPose(Axis.XP.rotationDegrees(0.0F));
        }

        poseStack.scale(-1F, -1F, 1F);
        poseStack.translate(-0.25F, -1.35F, 0F);

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entity)));
        model.render(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(ThrownPlunger entity) {
        return PlungerItemRenderer.TEXTURE;
    }
}
