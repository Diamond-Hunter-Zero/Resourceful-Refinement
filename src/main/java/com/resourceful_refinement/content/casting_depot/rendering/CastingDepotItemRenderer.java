package com.resourceful_refinement.content.casting_depot.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.resourceful_refinement.content.forge_mould.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class CastingDepotItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final CastingDepotItemRenderer INSTANCE = new CastingDepotItemRenderer();

    private CastingDepotModel depotModel;

    public CastingDepotItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    // Since BERP context isn't available here, we bake layers manually in the first render or a setup method
    private void prepareModels() {
        if (depotModel != null) return;
        EntityModelSet models = Minecraft.getInstance().getEntityModels();
        this.depotModel = new CastingDepotModel(models.bakeLayer(CastingDepotLayers.CASTING_DEPOT));
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        prepareModels();

        ms.pushPose();
        switch (context)
        {
            case FIRST_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND -> {
                ms.translate(0.5, 0.9, 0.5);
                ms.scale(0.4f, -0.4f, -0.4f);
                ms.mulPose(Axis.YP.rotationDegrees(45));
            }
            case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND -> {
                ms.translate(0.5, 0.75, 0.85);
                ms.scale(0.4f, -0.4f, -0.4f);
                ms.mulPose(Axis.XP.rotationDegrees(80));
                ms.mulPose(Axis.YP.rotationDegrees(45));
            }
            case GUI ->
            {
                ms.translate(0.5, 1.05, 0.5);
                ms.scale(0.625f, -0.625f, -0.625f);

                // Standard item orientation
                ms.mulPose(Axis.YP.rotationDegrees(45f));
                ms.mulPose(Axis.ZP.rotationDegrees(20f));
                ms.mulPose(Axis.XP.rotationDegrees(20f));
            }
            case GROUND -> {
                ms.translate(0.5, 0.85, 0.5);
                ms.scale(0.25f, -0.25f, -0.25f);
            }
            default ->
            {
                ms.translate(0.5, 0, 0.5);
                ms.scale(0.5f, 0.5f, 0.5f);
            }
        }

        // Render block model
        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutout(CastingDepotRenderer.TEXTURE));
        depotModel.render(ms, vc, light, overlay);

        ms.popPose();
    }
}
