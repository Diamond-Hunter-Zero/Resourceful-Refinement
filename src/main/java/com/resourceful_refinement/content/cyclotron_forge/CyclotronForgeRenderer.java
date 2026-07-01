package com.resourceful_refinement.content.cyclotron_forge;

import com.mojang.blaze3d.vertex.PoseStack;
import com.resourceful_refinement.registry.ModBlocks;
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
import net.minecraft.world.phys.AABB;

public class CyclotronForgeRenderer implements BlockEntityRenderer<CyclotronControllerBlockEntity> {
    public CyclotronForgeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CyclotronControllerBlockEntity be, float partialTick, PoseStack poseStack,
            MultiBufferSource bufferSource, int light, int overlay) {
        if (!be.isAssembled()) {
            renderBlock(ModBlocks.CYCLOTRON_CONTROLLER.get().defaultBlockState(), poseStack, bufferSource, light, 0, 0, 0);
            return;
        }

        renderOutputCap(be, poseStack, bufferSource, light);
        for (int depth = 1; depth <= be.getCoilLength(); depth++) {
            renderCoilSlice(be, poseStack, bufferSource, light, depth);
        }
        renderInputCap(be, poseStack, bufferSource, light, be.getCoilLength() + 1);
    }

    private static void renderOutputCap(CyclotronControllerBlockEntity be, PoseStack poseStack,
            MultiBufferSource bufferSource, int light) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                BlockState state;
                if (x == 0 && y == 0) {
                    state = ModBlocks.CYCLOTRON_CONTROLLER.get().defaultBlockState()
                            .setValue(CyclotronControllerBlock.FACING, be.getFacing())
                            .setValue(CyclotronControllerBlock.ASSEMBLED, true);
                } else if (y == 0 && x == -1) {
                    state = Blocks.COPPER_BLOCK.defaultBlockState();
                } else if (y == 0 && x == 1) {
                    state = Blocks.CUT_COPPER.defaultBlockState();
                } else if ((Math.abs(x) == 1 && y == 0) || (x == 0 && Math.abs(y) == 1)) {
                    state = Blocks.AMETHYST_BLOCK.defaultBlockState();
                } else {
                    state = AllBlocks.BRASS_CASING.getDefaultState();
                }
                renderBlock(state, poseStack, bufferSource, light, offset(be, 0, x, y));
            }
        }
    }

    private static void renderCoilSlice(CyclotronControllerBlockEntity be, PoseStack poseStack,
            MultiBufferSource bufferSource, int light, int depth) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                if (x == 0 && y == 0) continue;
                renderBlock(ModBlocks.HEAVY_PLATE_SHIELDING.get().defaultBlockState(), poseStack, bufferSource, light,
                        offset(be, depth, x, y));
            }
        }
    }

    private static void renderInputCap(CyclotronControllerBlockEntity be, PoseStack poseStack,
            MultiBufferSource bufferSource, int light, int depth) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                BlockState state;
                if (y == 1 && Math.abs(x) == 1) {
                    state = AllBlocks.ITEM_VAULT.getDefaultState();
                } else if (y == -1 && Math.abs(x) == 1) {
                    state = AllBlocks.FLUID_TANK.getDefaultState();
                } else if (x == 0 && y == 0) {
                    state = AllBlocks.SHAFT.getDefaultState();
                } else {
                    state = AllBlocks.BRASS_CASING.getDefaultState();
                }
                renderBlock(state, poseStack, bufferSource, light, offset(be, depth, x, y));
            }
        }
    }

    private static BlockPos offset(CyclotronControllerBlockEntity be, int depth, int lateral, int vertical) {
        Direction back = be.getFacing().getOpposite();
        Direction right = be.getFacing().getClockWise();
        return BlockPos.ZERO.relative(back, depth).relative(right, lateral).above(vertical);
    }

    private static void renderBlock(BlockState state, PoseStack poseStack, MultiBufferSource bufferSource, int light,
            BlockPos offset) {
        renderBlock(state, poseStack, bufferSource, light, offset.getX(), offset.getY(), offset.getZ());
    }

    private static void renderBlock(BlockState state, PoseStack poseStack, MultiBufferSource bufferSource, int light,
            int x, int y, int z) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state, poseStack, bufferSource, light,
                OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(CyclotronControllerBlockEntity be) {
        if (!be.isAssembled()) return new AABB(be.getBlockPos());
        Direction facing = be.getFacing();
        BlockPos controller = be.getBlockPos();
        BlockPos inputCenter = controller.relative(facing.getOpposite(), be.getCoilLength() + 1);
        BlockPos p1 = controller.relative(facing.getClockWise(), 1).below();
        BlockPos p2 = inputCenter.relative(facing.getCounterClockWise(), 1).above();
        return new AABB(controller).minmax(new AABB(p1)).minmax(new AABB(p2)).inflate(1.0);
    }

    @Override
    public boolean shouldRenderOffScreen(CyclotronControllerBlockEntity be) {
        return be.isAssembled();
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
