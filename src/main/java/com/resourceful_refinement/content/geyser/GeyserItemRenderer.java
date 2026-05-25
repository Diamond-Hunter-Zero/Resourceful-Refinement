package com.resourceful_refinement.content.geyser;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.resourceful_refinement.content.sieve.MechanicalSieveCasingModel;
import com.resourceful_refinement.content.sieve.MechanicalSieveCogModel;
import com.resourceful_refinement.content.sieve.MechanicalSieveLayers;
import com.resourceful_refinement.content.sieve.MechanicalSieveRenderer;
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

public class GeyserItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final GeyserItemRenderer INSTANCE = new GeyserItemRenderer();

    public GeyserItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {

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
                ms.translate(0.05, 0.8, 0.5);
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
                ms.translate(0.25, 0.25, 0.3);
                ms.scale(0.5f, 0.5f, 0.5f);
            }
        }

        // Render block model
        SuperByteBuffer shaft = CachedBuffers.partial(ModPartialModels.GEYSER_CASING, Blocks.AIR.defaultBlockState());
        shaft.light(light).renderInto(ms, buffer.getBuffer(RenderType.cutout()));

        ms.popPose();
    }
}
