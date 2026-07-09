package com.resourceful_refinement.content.drill_pylon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class DrillPylonRenderer implements BlockEntityRenderer<DrillPylonHeadBlockEntity> {
    public DrillPylonRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(DrillPylonHeadBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) {
        renderBlock(Blocks.DEEPSLATE.defaultBlockState(), poseStack, bufferSource, light, 0, 0, 0);
        if (!be.isAssembled()) return;

        Direction front = be.getFront();
        for (int y = 1; y <= 3; y++) {
            renderBlock(AllBlocks.GEARBOX.getDefaultState(), poseStack, bufferSource, light, 0, y, 0);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) continue;
                    BlockState state = AllBlocks.BRASS_CASING.getDefaultState();
                    BlockPos rel = new BlockPos(dx, y, dz);
                    if (rel.equals(new BlockPos(front.getStepX(), 1, front.getStepZ()))) {
                        state = Blocks.COPPER_BLOCK.defaultBlockState();
                    } else if (rel.equals(new BlockPos(front.getOpposite().getStepX(), 2, front.getOpposite().getStepZ()))) {
                        state = Blocks.AMETHYST_BLOCK.defaultBlockState();
                    }
                    renderBlock(state, poseStack, bufferSource, light, dx, y, dz);
                }
            }
        }

        for (int y = 0; y <= 3; y++) {
            for (int dx : new int[]{-2, 2}) {
                for (int dz : new int[]{-2, 2}) {
                    renderBlock(AllBlocks.METAL_GIRDER.getDefaultState(), poseStack, bufferSource, light, dx, y, dz);
                }
            }
        }
    }

    private static void renderBlock(BlockState state, PoseStack poseStack, MultiBufferSource bufferSource, int light, int x, int y, int z) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state, poseStack, bufferSource, light, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
