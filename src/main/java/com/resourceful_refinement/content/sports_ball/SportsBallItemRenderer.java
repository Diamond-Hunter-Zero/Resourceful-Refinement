package com.resourceful_refinement.content.sports_ball;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.plushie.PlushieItemRenderer;
import com.resourceful_refinement.content.plushie.PlushieModel;
import com.resourceful_refinement.content.plushie.PlushieRenderer;
import com.resourceful_refinement.registry.ModDataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Quaternionf;

import static net.minecraft.world.item.ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
import static net.minecraft.world.item.ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;

/** Draws the {@link SportsBallEntity} with its accumulated roll applied in world space. */
@OnlyIn(Dist.CLIENT)
public class SportsBallItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final SportsBallItemRenderer INSTANCE = new SportsBallItemRenderer();

    private SportsBallModel model;

    public SportsBallItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }
    private void prepareModels() {
        if (this.model != null) return;

        EntityModelSet models = Minecraft.getInstance().getEntityModels();
        this.model = new SportsBallModel(models.bakeLayer(SportsBallRenderer.LAYER));
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        prepareModels();

        ms.pushPose();
        switch (context)
        {
            case FIRST_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND -> {
                ms.translate(0.35, 1.65, 0.6);
                ms.scale(0.8f, -0.8f, -0.8f);
            }
            case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND -> {
                float handSign = 1f;
                if (context == THIRD_PERSON_RIGHT_HAND)
                    handSign = -1f;

                ms.translate(0.5 + 0.25 * handSign, 0.8, 1.4);
                ms.scale(0.8f, -0.8f, -0.8f);
                ms.mulPose(Axis.XP.rotationDegrees(80));
                ms.mulPose(Axis.YP.rotationDegrees(25));
            }
            case GUI ->
            {
                ms.translate(0.5, 1.5, 0.5);
                ms.scale(1f, -1f, -1f);

                // Standard item orientation
                ms.mulPose(Axis.YP.rotationDegrees(45f));
                ms.mulPose(Axis.ZP.rotationDegrees(20f));
                ms.mulPose(Axis.XP.rotationDegrees(20f));
            }
            case GROUND -> {
                ms.translate(0.5, 1.1, 0.5);
                ms.scale(0.5f, -0.5f, -0.5f);
            }
            default ->
            {
                ms.translate(0.5, 1.66, 0.5);
                ms.scale(-1f, -1f, 1f);
            }
        }

        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(getBallTypeTexture(stack)));
        this.model.render(ms, vc, light, overlay);

        ms.popPose();
    }

    public static ResourceLocation getBallTypeTexture(ItemStack itemStack)
    {
        int type = 0;
        if (itemStack.has(ModDataComponents.BALL_TYPE.get()))
            type = itemStack.get(ModDataComponents.BALL_TYPE.get());

        if (type == 0)
            return SportsBallRenderer.BASKETBALL_TEXTURE;
        if (type == 1)
            return SportsBallRenderer.SOCCER_TEXTURE;
        if (type == 2)
            return SportsBallRenderer.NETBALL_TEXTURE;

        return SportsBallRenderer.DEBUG_TEXTURE;
    }
}
