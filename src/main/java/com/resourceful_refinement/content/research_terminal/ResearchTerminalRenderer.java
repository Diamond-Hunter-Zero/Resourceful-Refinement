package com.resourceful_refinement.content.research_terminal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.utilities.FluidBoxRendering;
import com.simibubi.create.content.logistics.depot.DepotRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;

public class ResearchTerminalRenderer extends SafeBlockEntityRenderer<ResearchTerminalBlockEntity> {

    private final ResearchTerminalModel model;
    private final ItemRenderer itemRenderer;


    public ResearchTerminalRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new ResearchTerminalModel(context.bakeLayer(ResearchTerminalModel.LAYER_LOCATION));
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    protected void renderSafe(ResearchTerminalBlockEntity be, float partialTicks, PoseStack pose,
            MultiBufferSource buffer, int light, int overlay) {

        // Render base depot contents
        renderScaledDepotItem(be, partialTicks, pose, buffer, light, overlay, true);


        // Render scan objects
        boolean isScanning = false;
        if (be.getCycleKind() == ResearchTerminalCycleKind.FLUID)
        {
            renderProcessingFluid(be, pose, buffer, light);
            isScanning = true;
        }
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

            isScanning = true;
        }


        // Render model
        BlockState state = be.getBlockState();
        Direction facing = state.getValue(ResearchTerminalBlock.FACING);

        pose.pushPose();
        pose.translate(0.5, 1.5, 0.5);
        pose.scale(1, -1, -1);
        pose.mulPose(Axis.YP.rotationDegrees(facing.toYRot()));

        model.render(isScanning, pose, buffer, light, overlay);
        pose.popPose();

        // Render scan animation
        if (isScanning)
            model.animateScan((long) ((be.getLevel().getGameTime() + partialTicks) * 50f));
        else
            model.animateScan(0);

    }

    private void renderScaledDepotItem(ResearchTerminalBlockEntity be, float partialTicks, PoseStack pose,
            MultiBufferSource buffer, int light, int overlay, boolean shrink) {
        pose.pushPose();
        pose.translate(0.25F, 0.535F, 0.25F);
        if (shrink)
            pose.scale(0.5F, 0.33F, 0.5F);
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

    @Override
    public int getViewDistance() {
        return 128;
    }
}
