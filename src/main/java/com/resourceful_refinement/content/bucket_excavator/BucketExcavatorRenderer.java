package com.resourceful_refinement.content.bucket_excavator;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.registry.ModPartialModels;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import static com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer.getAngleForBe;

public class BucketExcavatorRenderer extends SafeBlockEntityRenderer<BucketExcavatorBlockEntity> {

    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "textures/block/bucket_excavator.png");

    private final BucketExcavatorModel model;

    public BucketExcavatorRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new BucketExcavatorModel(context.bakeLayer(BucketExcavatorModel.LAYER_LOCATION));
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    protected void renderSafe(BucketExcavatorBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
                              int light, int overlay) {
        BlockState state = be.getBlockState();
        Direction facing = state.getValue(BucketExcavatorBlock.FACING);
        float speed = Math.abs(be.getSpeed());
        VertexConsumer casingBuffer = buffer.getBuffer(RenderType.entityCutout(TEXTURE));

        ms.pushPose();
        ms.translate(0.5, 1.5, 0.5);
        ms.scale(1, -1, -1);
        ms.mulPose(Axis.YP.rotationDegrees(facing.toYRot()));

        // --- Render Wheel Animation ---
        if (be.isOperational())
            model.animateWheel((long) ((be.getLevel().getGameTime() + partialTicks) * 50f));
        else
            model.animateWheel(0);

        // --- Render Block Model ---
        model.render(ms, casingBuffer, light, overlay);
        ms.popPose();

        // --- Shaft Kinetic Rendering ---
        renderKineticShaft(be, state, ms, buffer, light);
    }

    private void renderKineticShaft(BucketExcavatorBlockEntity be, BlockState state, PoseStack ms, MultiBufferSource buffer, int light) {
        Direction.Axis shaftAxis = getRotationAxisOf(be);
        SuperByteBuffer shaft = CachedBuffers.partial(shaftAxis == Direction.Axis.X ?
                ModPartialModels.SHAFT_X :
                ModPartialModels.SHAFT_Z, state);

        ms.pushPose();
        float shaftAngle = getAngleForBe(be, be.getBlockPos(), shaftAxis);
        shaft.rotateCentered(shaftAngle, shaftAxis)
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.solid()));
        ms.popPose();
    }

    protected Direction.Axis getRotationAxisOf(BucketExcavatorBlockEntity be) {
        return be.getBlockState().getValue(BucketExcavatorBlock.FACING).getClockWise().getAxis();
    }
}
