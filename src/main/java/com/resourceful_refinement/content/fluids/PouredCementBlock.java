package com.resourceful_refinement.content.fluids;

import com.resourceful_refinement.registry.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FlowingFluid;


public class PouredCementBlock extends LiquidBlock {

    public static final IntegerProperty SET_STAGE = IntegerProperty.create("set_stage", 0, 1);

    public PouredCementBlock(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
        registerDefaultState(defaultBlockState().setValue(SET_STAGE, 0));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SET_STAGE);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Only fire on source blocks
        if (state.getValue(LiquidBlock.LEVEL) == 0) {

            // Don't set if we're above sources of poured cement
            if (level.getBlockState(pos.below()).is(ModFluids.POURED_CEMENT.block) && level.getFluidState(pos.below()).isSource())
                return;
            if (!level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP))
                return;

            int setStage = state.getValue(SET_STAGE);
            if (setStage == 1)
                level.setBlock(pos, Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState(), 3);
            else
                level.setBlock(pos, state.setValue(SET_STAGE, setStage + 1), 3);
        }
    }
}
