package com.resourceful_refinement.content.forge_mould;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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

public class ForgeMouldItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final ForgeMouldItemRenderer INSTANCE = new ForgeMouldItemRenderer();

    private ForgeMouldCasingModel casing;
    private ForgeMouldPressModel press;

    public ForgeMouldItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    // Since BERP context isn't available here, we bake layers manually in the first render or a setup method
    private void prepareModels() {
        if (casing != null) return;
        EntityModelSet models = Minecraft.getInstance().getEntityModels();
        this.casing = new ForgeMouldCasingModel(models.bakeLayer(ForgeMouldLayers.CASING));
        this.press = new ForgeMouldPressModel(models.bakeLayer(ForgeMouldLayers.PRESS));
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
                ms.translate(0.5, 1, 0.5);
                ms.scale(-0.5f, -0.5f, 0.5f);
            }
        }


        // Render shaft
        ms.pushPose();
        ms.scale(1, 1, 1);
        ms.translate(-0.5, 0.5, -0.5);
        SuperByteBuffer shaft = CachedBuffers.partial(ModPartialModels.SHAFT_X, Blocks.AIR.defaultBlockState());
        shaft.light(light).renderInto(ms, buffer.getBuffer(RenderType.solid()));
        ms.popPose();

        // Render stationary parts
        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutout(ForgeMouldRenderer.TEXTURE));
        casing.render(ms, vc, light, overlay);

        // Render the press at a default position (e.g., fully retracted)
        ms.pushPose();
        ms.translate(0, 1/16f, 0);
        press.render(ms, vc, light, overlay);
        ms.popPose();

        ms.popPose();
    }
}
