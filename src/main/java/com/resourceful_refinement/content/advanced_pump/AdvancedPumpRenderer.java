package com.resourceful_refinement.content.advanced_pump;

import com.mojang.blaze3d.vertex.PoseStack;
import com.resourceful_refinement.registry.ModPartialModels;
import com.simibubi.create.content.fluids.pump.PumpBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;

import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class AdvancedPumpRenderer extends KineticBlockEntityRenderer<AdvancedPumpBlockEntity> {

    public AdvancedPumpRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(AdvancedPumpBlockEntity be, BlockState state) {
        return CachedBuffers.partialFacing(ModPartialModels.ADVANCED_PUMP_COG, getVisualState(state));
    }

    @Override
    protected void renderSafe(AdvancedPumpBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
                              int light, int overlay) {
        BlockState state = getVisualState(be.getBlockState());
        SuperByteBuffer cog = CachedBuffers.partialFacing(ModPartialModels.ADVANCED_PUMP_COG, state);
        kineticRotationTransform(cog, be, state.getValue(PumpBlock.FACING).getAxis(),
                getAngleForBe(be, be.getBlockPos(), state.getValue(PumpBlock.FACING).getAxis()), light)
                .renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));
    }

    @Override
    public int getViewDistance() {
        return 72;
    }

    private BlockState getVisualState(BlockState state) {
        if (!state.hasProperty(PumpBlock.FACING))
            return state;

        Direction facing = state.getValue(PumpBlock.FACING);
        if (state.hasProperty(AdvancedPumpBlock.POWERED) && state.getValue(AdvancedPumpBlock.POWERED))
            facing = facing.getOpposite();

        return state.setValue(PumpBlock.FACING, facing);
    }
}
