package com.resourceful_refinement.content.brewers_tap;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;


public class BrewersTapRenderer implements BlockEntityRenderer<BrewersTapBlockEntity> {

    private final ItemRenderer itemRenderer;

    public BrewersTapRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(BrewersTapBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {

        if (be.hasFlavourItem())
        {
            var itemstack = be.flavourInv.getStackInSlot(0);

            ms.pushPose();
            Direction facing = be.getBlockState().getValue(BrewersTapBlock.FACING);
            ms.translate(0.5F, 0.5F, 0.5F);
            ms.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));

            if (itemstack.getItem() instanceof BlockItem && !itemstack.is(ItemTags.VILLAGER_PLANTABLE_SEEDS))
                ms.translate(0F, -0.03F, -0.275F);
            else
                ms.translate(0F, 0.02F, -0.275F);
            ms.mulPose(Axis.YP.rotationDegrees(45));

            ms.scale(0.85F, 0.85F, 0.85F);
            this.itemRenderer.renderStatic(
                    be.flavourInv.getStackInSlot(0),
                    ItemDisplayContext.GROUND,
                    light,
                    overlay,
                    ms,
                    buffer,
                    be.getLevel(),
                    (int) be.getBlockPos().asLong()
            );

            ms.popPose();
        }
    }

    @Override
    public int getViewDistance() {
        return 128;
    }
}