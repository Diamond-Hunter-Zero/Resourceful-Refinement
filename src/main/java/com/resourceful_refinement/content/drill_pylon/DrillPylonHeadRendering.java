package com.resourceful_refinement.content.drill_pylon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.resourceful_refinement.utilities.ShaftUtilities;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.joml.Vector3f;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import static com.resourceful_refinement.content.drill_pylon.DrillPylonHeadBlock.FACING;

/**
 * Client-side drawing of the standalone (unassembled) Drill Pylon Head, shared by the block entity
 * renderer and contraption rendering. Draws DrillPylonHeadModel oriented to the block's FACING with its
 * drill bit spun to a given angle, plus the half shaft on the block's back face.
 */
public final class DrillPylonHeadRendering {

    private static DrillPylonHeadModel contraptionModel;

    private DrillPylonHeadRendering() {
    }

    /**
     * Renders the head with the pose stack at the block's origin.
     *
     * @param angle rotation of the drill bit and shaft, in radians, about the positive direction of the
     *              facing axis (the same convention as Create's kinetic shaft angles)
     */
    public static void render(DrillPylonHeadModel model, BlockState state, float angle, PoseStack ms,
                              MultiBufferSource buffer, int light, int overlay) {
        Direction facing = state.getValue(FACING);

        // The bit spins about the model's z axis, which ends up pointing opposite to facing in the world
        boolean facesPositive = facing.getAxisDirection() == Direction.AxisDirection.POSITIVE;
        model.setDrillBitSpin(facesPositive ? -angle : angle);

        ms.pushPose();
        transformToFacing(ms, facing);
        model.render(ms, buffer.getBuffer(RenderType.entityCutout(DrillPylonRenderer.TEXTURE)), light, overlay);
        ms.popPose();

        ShaftUtilities.RenderKineticShaft(state, facing.getOpposite(), true, angle, ms, buffer, light);
    }

    /**
     * Transforms from block origin into model space for a head facing the given direction. The model is
     * authored facing north; it is turned to the facing about the block centre, then flipped into entity
     * model space (y down) with its 24px ground plane on the block's bottom face.
     */
    private static void transformToFacing(PoseStack ms, Direction facing) {
        ms.translate(0.5, 0.5, 0.5);
        switch (facing) {
            case SOUTH -> ms.mulPose(Axis.YP.rotationDegrees(180));
            case EAST -> ms.mulPose(Axis.YP.rotationDegrees(-90));
            case WEST -> ms.mulPose(Axis.YP.rotationDegrees(90));
            case UP -> ms.mulPose(Axis.XP.rotationDegrees(90));
            case DOWN -> ms.mulPose(Axis.XP.rotationDegrees(-90));
            default -> {
            }
        }
        ms.mulPose(Axis.ZP.rotationDegrees(180));
        ms.translate(0, -1, 0);
    }

    /**
     * Renders a head carried by a contraption. Mirrors Create's drill: the bit spins at the contraption's
     * animation speed while it is stalled or moving away from its back face, and holds still otherwise.
     */
    public static void renderInContraption(MovementContext context, BlockAndTintGetter renderWorld,
                                           ContraptionMatrices matrices, MultiBufferSource buffer) {
        if (contraptionModel == null) {
            contraptionModel = new DrillPylonHeadModel(
                    Minecraft.getInstance().getEntityModels().bakeLayer(DrillPylonHeadModel.LAYER_LOCATION));
        }

        BlockState state = context.state;
        Direction facing = state.getValue(FACING);
        boolean spinning = context.contraption.stalled
                || !VecHelper.isVecPointingTowards(context.relativeMotion, facing.getOpposite());
        float speed = spinning ? context.getAnimationSpeed() : 0;
        float time = AnimationTickHolder.getRenderTime() / 20;
        float angle = (float) Math.toRadians((time * speed) % 360);

        // The render world reports the head as inside its own (opaque) contraption blocks, so sample the
        // real world's light at the head's current world position, as Create's useLevelLight does
        Vector3f worldPos = matrices.getWorld().transformPosition(
                context.localPos.getX() + 0.5F, context.localPos.getY() + 0.5F, context.localPos.getZ() + 0.5F, new Vector3f());
        int light = LevelRenderer.getLightColor(context.world, BlockPos.containing(worldPos.x, worldPos.y, worldPos.z));

        PoseStack ms = matrices.getViewProjection();
        ms.pushPose();
        ms.mulPose(matrices.getModel().last().pose());
        render(contraptionModel, state, angle, ms, buffer, light, OverlayTexture.NO_OVERLAY);
        ms.popPose();
    }
}
