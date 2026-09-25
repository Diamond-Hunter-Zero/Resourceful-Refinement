package com.resourceful_refinement.content.sports_ball;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.resourceful_refinement.ResourcefulRefinementMain;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Quaternionf;

/** Draws the {@link SportsBallEntity} with its accumulated roll applied in world space. */
@OnlyIn(Dist.CLIENT)
public class SportsBallRenderer extends EntityRenderer<SportsBallEntity> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "sports_ball"), "main");

    public static final ResourceLocation DEBUG_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "textures/entity/sports_ball_debug.png");
    public static final ResourceLocation BASKETBALL_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "textures/entity/sports_ball_basketball.png");
    public static final ResourceLocation SOCCER_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "textures/entity/sports_ball_soccer.png");
    public static final ResourceLocation NETBALL_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "textures/entity/sports_ball_netball.png");

    /** Scratch quaternion — the renderer must never mutate the entity's own rotation state. */
    private final Quaternionf spin = new Quaternionf();

    private final SportsBallModel model;

    public SportsBallRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new SportsBallModel(context.bakeLayer(LAYER));
        this.shadowRadius = SportsBallEntity.RADIUS;
    }

    @Override
    public void render(
            SportsBallEntity entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight
    ) {
        poseStack.pushPose();

        // The pose starts at the entity's feet; lift to the ball's centre so the spin pivots there.
        poseStack.translate(0.0F, SportsBallEntity.RADIUS, 0.0F);
        poseStack.mulPose(entity.getVisualRotation(partialTick, spin));
        poseStack.translate(0.0F, 3.33*SportsBallEntity.RADIUS, 0.0F);

        // Standard entity-model orientation (equivalent to a 180 degree turn about Z, so it does
        // not disturb the world-space spin applied above). ModelPart already divides by 16.
        poseStack.scale(-1.0F, -1.0F, 1.0F);

        // Set texture
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(getBallTypeTexture(entity)));
        model.render(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(SportsBallEntity entity) {
        return getBallTypeTexture(entity);
    }

    public static ResourceLocation getBallTypeTexture(SportsBallEntity entity)
    {
        int type = entity.getBallType();

        if (type == 0)
            return BASKETBALL_TEXTURE;
        if (type == 1)
                return SOCCER_TEXTURE;
        if (type == 2)
            return NETBALL_TEXTURE;

        return DEBUG_TEXTURE;
    }
}
