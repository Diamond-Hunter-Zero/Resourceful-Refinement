package com.resourceful_refinement.content.pug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.resourceful_refinement.registry.ModPartialModels;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Once assembled, the controller and its proxies render nothing themselves (empty block models); this renderer
 * draws the whole 3x3 pad model instead, in the texture for the pad's current SEND / RECEIVE mode.
 * <p>
 * The pad model is authored around the pad's middle block, with its front (the controller face) on the north side,
 * so it is rotated to the controller's facing and shifted one block back from the controller.
 */
public class LaunchpadControllerRenderer extends SmartBlockEntityRenderer<LaunchpadControllerBlockEntity> {
    public LaunchpadControllerRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override protected void renderSafe(LaunchpadControllerBlockEntity be, float partialTicks, PoseStack pose,
            MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, pose, buffer, light, overlay);
        BlockState state = be.getBlockState();
        if (!state.getValue(LaunchpadControllerBlock.ASSEMBLED)) return;

        PartialModel model = be.getConfiguration().mode() == LaunchpadMode.RECEIVE
                ? ModPartialModels.LAUNCH_PAD_RECEIVE : ModPartialModels.LAUNCH_PAD_SEND;
        Direction facing = state.getValue(LaunchpadControllerBlock.FACING);

        pose.pushPose();
        pose.translate(0.5, 0, 0.5);
        // Matches blockstate "y" rotations: north 0, east 90, south 180, west 270 (clockwise from above).
        pose.mulPose(Axis.YP.rotationDegrees(180 - facing.toYRot()));
        pose.translate(-0.5, 0, -0.5 + 1);
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(pose.last(),
                buffer.getBuffer(RenderType.cutout()), state, model.get(), 1, 1, 1, light, overlay);
        pose.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(LaunchpadControllerBlockEntity be) {
        return true;
    }

    /** The pad spans the 3x3 footprint, not just the controller block; keep it from being frustum-culled early. */
    @Override
    public AABB getRenderBoundingBox(LaunchpadControllerBlockEntity be) {
        return new AABB(be.getBlockPos()).inflate(2, 1, 2);
    }
}
