package com.resourceful_refinement.content.glare.remote;

import com.mojang.blaze3d.vertex.PoseStack;
import com.resourceful_refinement.registry.ModBlocks;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;

/** Placeholder assembly renderer; proxy blocks remain invisible and retain collision/capability duties. */
public class RemoteEntanglementTransporterRenderer extends SmartBlockEntityRenderer<RemoteEntanglementTransporterBlockEntity> {
    private final BlockRenderDispatcher blocks;

    public RemoteEntanglementTransporterRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        blocks = context.getBlockRenderDispatcher();
    }

    @Override protected void renderSafe(RemoteEntanglementTransporterBlockEntity be, float partialTicks, PoseStack pose,
            MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, pose, buffer, light, overlay);
        if (!be.isRemoteEndpointAssembled()) return;
        Direction facing = be.getBlockState().getValue(RemoteEntanglementTransporterBlock.FACING);
        BlockPos chamber = BlockPos.ZERO.relative(facing);
        renderAt(pose, buffer, light, overlay, 0, 1, 0, Blocks.COPPER_BLOCK.defaultBlockState());
        renderAt(pose, buffer, light, overlay, chamber.getX(), 0, chamber.getZ(), Blocks.ACACIA_TRAPDOOR.defaultBlockState());
        renderAt(pose, buffer, light, overlay, chamber.getX(), 1, chamber.getZ(), Blocks.AIR.defaultBlockState());
    }

    private void renderAt(PoseStack pose, MultiBufferSource buffer, int light, int overlay,
            double x, double y, double z, net.minecraft.world.level.block.state.BlockState state) {
        pose.pushPose();
        pose.translate(x, y, z);
        blocks.renderSingleBlock(state, pose, buffer, light, overlay);
        pose.popPose();
    }

    @Override public int getViewDistance() { return 256; }
}
