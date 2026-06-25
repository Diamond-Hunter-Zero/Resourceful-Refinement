package com.resourceful_refinement.content.radiator;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class RadiatorRenderer implements BlockEntityRenderer<RadiatorBlockEntity> {

    public RadiatorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(RadiatorBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        BlockState state = be.getBlockState();
        NonNullList<ItemStack> items = be.getItems();

        Direction direction = state.hasProperty(RadiatorBlock.FACING) ? state.getValue(RadiatorBlock.FACING) : Direction.UP;
        int facingIndex = direction.getAxis().isHorizontal() ? direction.get2DDataValue() : Direction.NORTH.get2DDataValue();

        int seed = (int) be.getBlockPos().asLong();

        for (int j = 0; j < items.size(); ++j) {
            ItemStack itemStack = items.get(j);
            if (!itemStack.isEmpty()) {
                ms.pushPose();

                // Position the item flat on top of the radiator with small offset
                ms.translate(0.5F, 1.001F, 0.5F);

                // Determine rotation quadrant based on slot index and radiator horizontal direction.
                Direction rotationDir = Direction.from2DDataValue(Math.floorMod(j + facingIndex, 4));
                float yRot = -rotationDir.toYRot();

                ms.mulPose(Axis.YP.rotationDegrees(yRot));
                // Lay the item flat (facing up)
                ms.mulPose(Axis.XP.rotationDegrees(90.0F));

                // Offset the item from the center of the block into its quadrant.
                ms.translate(-0.25F, -0.25F, 0.0F);

                // Scale the item to fit nicely.
                ms.scale(0.375F, 0.375F, 0.375F);

                Minecraft.getInstance().getItemRenderer().renderStatic(
                        itemStack,
                        ItemDisplayContext.FIXED,
                        light,
                        overlay,
                        ms,
                        buffer,
                        be.getLevel(),
                        seed + j
                );

                ms.popPose();
            }
        }
    }
}
