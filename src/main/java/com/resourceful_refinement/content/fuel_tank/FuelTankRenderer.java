package com.resourceful_refinement.content.fuel_tank;

import com.mojang.blaze3d.vertex.PoseStack;
import com.resourceful_refinement.utilities.FluidBoxRendering;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class FuelTankRenderer implements BlockEntityRenderer<FuelTankBlockEntity> {

    private static final float FLUID_STACK_START_Y = (2f/16f);
    private static final float FLUID_STACK_RADII = (5.75f/16f);

    public FuelTankRenderer(BlockEntityRendererProvider.Context context) {
    }


    @Override
    public void render(FuelTankBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {

        ms.pushPose();

        // Render fluid stacks
        if (!be.tank.isEmpty())
        {
            ms.pushPose();
            ms.translate(0.5,0, 0.5);
            float height = 0.75f;
            FluidBoxRendering.renderFluidsForTanks(ms, buffer, light, height, FLUID_STACK_START_Y, FLUID_STACK_RADII, true, be.tank);
            ms.popPose();
        }

        ms.popPose();
    }

    @Override
    public int getViewDistance() {
        return 72;
    }
}
