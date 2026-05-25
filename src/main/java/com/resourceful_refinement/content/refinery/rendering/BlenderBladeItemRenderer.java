package com.resourceful_refinement.content.refinery.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.resourceful_refinement.registry.ModPartialModels;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class BlenderBladeItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final BlenderBladeItemRenderer INSTANCE = new BlenderBladeItemRenderer();

    private RefineryBlenderModel blender;

    public BlenderBladeItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    private void prepareModels() {
        if (this.blender != null) return;
        EntityModelSet models = Minecraft.getInstance().getEntityModels();
        this.blender = new RefineryBlenderModel(models.bakeLayer(RefineryLayers.BLENDER));
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        prepareModels();

        ms.pushPose();

        // Standardise display orientations by scaling and rotating around the block's physical center
        switch (context) {
            case GUI -> {
                ms.translate(0.5, 0.5, 0.5);
                ms.scale(0.625f, 0.625f, 0.625f);
                ms.mulPose(Axis.XP.rotationDegrees(30));
                ms.mulPose(Axis.YP.rotationDegrees(225));
                ms.translate(-0.5, -0.5, -0.5);
            }
            case GROUND -> {
                ms.translate(0.5, 0.5, 0.5);
                ms.scale(0.25f, 0.25f, 0.25f);
                ms.translate(-0.5, -0.5, -0.5);
            }
            case FIXED -> {
                // Fixed context is usually already centered, do nothing
            }
            case FIRST_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND -> {
                ms.translate(0.5, 0.5, 0.5);
                ms.scale(0.4f, 0.4f, 0.4f);
                ms.mulPose(Axis.XP.rotationDegrees(30));
                ms.mulPose(Axis.YP.rotationDegrees(225));
                ms.translate(-0.5, -0.5, -0.5);
            }
            case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND -> {
                ms.translate(0.5, 0.5, 0.5);
                ms.scale(0.4f, 0.4f, 0.4f);
                ms.mulPose(Axis.XP.rotationDegrees(30));
                ms.mulPose(Axis.YP.rotationDegrees(225));
                ms.translate(-0.5, -0.5, -0.5);
            }
            default -> {
                ms.translate(0.5, 0.5, 0.5);
                ms.scale(0.5f, 0.5f, 0.5f);
                ms.translate(-0.5, -0.5, -0.5);
            }
        }

        // Center, align, and render the blender blades
        ms.pushPose();
        ms.translate(0.5, 0.5, 0.5);
        ms.scale(-1.0F, -1.0F, 1.0F);
        ms.translate(0, -1.0, 0);
        blender.render(ms, buffer.getBuffer(RenderType.entityCutout(BlenderBladeRenderer.TEXTURE)), light, overlay);
        ms.popPose();

        ms.popPose();
    }
}
