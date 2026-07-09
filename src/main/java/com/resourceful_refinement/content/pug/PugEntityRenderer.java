package com.resourceful_refinement.content.pug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.milking_station.MilkingStationModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Blocks;

/** Temporary two-block copper cube used until the final PUG model and animation pass. */
public class PugEntityRenderer extends EntityRenderer<PugEntity> {
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "textures/entity/pug_lander.png");

    private final PugLanderModel model;

    public PugEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new PugLanderModel(context.bakeLayer(PugLanderModel.LAYER_LOCATION));
        shadowRadius = 1.0F;
    }

    @Override
    public void render(PugEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
            MultiBufferSource bufferSource, int packedLight) {

        VertexConsumer casingBuffer = bufferSource.getBuffer(RenderType.entityCutout(TEXTURE));
        poseStack.pushPose();

        poseStack.scale(1.0F, -1.0F, -1.0F);
        poseStack.translate(0D, -1.5D, 0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(entityYaw));

        model.render(poseStack, casingBuffer, packedLight, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(PugEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
