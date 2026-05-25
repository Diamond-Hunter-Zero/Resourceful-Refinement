package com.resourceful_refinement.content.plushie;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.registry.ModPartialModels;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class PlushieItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final PlushieItemRenderer INSTANCE = new PlushieItemRenderer();
    private PlushieModel model;

    public PlushieItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    private void prepareModels() {
        if (this.model != null) return;

        EntityModelSet models = Minecraft.getInstance().getEntityModels();
        this.model = new PlushieModel(models.bakeLayer(PlushieRenderer.LAYER_LOCATION));
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        prepareModels();

        ms.pushPose();
        switch (context)
        {
            case FIRST_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND -> {
                ms.translate(0.225, 0.7, 0.5);
                ms.scale(0.4f, -0.4f, -0.4f);
                ms.mulPose(Axis.YP.rotationDegrees(45));
            }
            case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND -> {
                ms.translate(0.25, 0.7, 0.7);
                ms.scale(0.4f, -0.4f, -0.4f);
                ms.mulPose(Axis.XP.rotationDegrees(80));
                ms.mulPose(Axis.YP.rotationDegrees(45));
            }
            case GUI ->
            {
                ms.translate(0.5, 1, 0.5);
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
                ms.translate(0.5, 1, 0.4);
                ms.scale(-0.5f, -0.5f, 0.5f);
            }
        }

        // 2. Fetch Texture
        // If the item also has variants (via NBT), you can read it here
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(
                ResourcefulRefinementMain.MOD_ID, "textures/block/plushie/fox_plushie.png"
        );

        // 3. Render
        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));
        this.model.renderToBuffer(ms, vc, light, overlay, 0xFFFFFFFF);

        ms.popPose();
    }
}
