package com.resourceful_refinement.content.research_terminal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.resourceful_refinement.utilities.FluidBoxRendering;
import com.simibubi.create.content.logistics.depot.DepotRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;

public class ResearchTerminalRenderer extends SafeBlockEntityRenderer<ResearchTerminalBlockEntity> {

    private final ItemRenderer itemRenderer;

    public ResearchTerminalRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    protected void renderSafe(ResearchTerminalBlockEntity be, float partialTicks, PoseStack pose,
            MultiBufferSource buffer, int light, int overlay) {

        renderScaledDepotItem(be, partialTicks, pose, buffer, light, overlay, true);

        if (be.getCycleKind() == ResearchTerminalCycleKind.FLUID)
            renderProcessingFluid(be, pose, buffer, light);
        else if (be.getCycleKind() == ResearchTerminalCycleKind.ITEM)
        {
            // Render an upright item instance of scanned item
            long gametime = be.getLevel() != null ? be.getLevel().getGameTime() : 0;
            pose.pushPose();
            pose.translate(0.5F, 1.25F, 0.5F);
            pose.mulPose(Axis.YP.rotationDegrees(gametime));

            pose.scale(0.85F, 0.85F, 0.85F);
            this.itemRenderer.renderStatic(
                    be.getHeldItem(),
                    ItemDisplayContext.GROUND,
                    light,
                    overlay,
                    pose,
                    buffer,
                    be.getLevel(),
                    (int) be.getBlockPos().asLong()
            );
            pose.popPose();
        }
    }

    private void renderScaledDepotItem(ResearchTerminalBlockEntity be, float partialTicks, PoseStack pose,
            MultiBufferSource buffer, int light, int overlay, boolean shrink) {
        pose.pushPose();
        pose.translate(0.25F, 0.5F, 0.25F);
        if (shrink)
            pose.scale(0.5F, 0.5F, 0.5F);
        DepotRenderer.renderItemsOf(be, partialTicks, pose, buffer, light, overlay, be.getDepotBehaviour());
        pose.popPose();
    }

    private void renderProcessingFluid(ResearchTerminalBlockEntity be, PoseStack pose, MultiBufferSource buffer,
            int light) {
        pose.pushPose();
        pose.translate(0.5F, 0.2F, 0.5F);
        long gametime = be.getLevel() != null ? be.getLevel().getGameTime() : 0;
        pose.mulPose(Axis.YP.rotationDegrees(gametime));

        FluidBoxRendering.renderFluidStack(be.getFluid(), ResearchTerminalBlockEntity.TANK_CAPACITY, pose, buffer,
                light, 16.2F / 16.0F, 6F / 16.0F, 3F / 16.0F, true);
        pose.popPose();
    }
}
