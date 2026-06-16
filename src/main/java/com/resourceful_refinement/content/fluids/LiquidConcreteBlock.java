package com.resourceful_refinement.content.fluids;

import com.resourceful_refinement.registry.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;


public class LiquidConcreteBlock extends LiquidBlock {
    public LiquidConcreteBlock(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Only fire on source blocks
        if (state.getValue(LiquidBlock.LEVEL) == 0) {

            // Don't set if we're above sources of liquid concrete
            if (level.getBlockState(pos.below()).is(ModFluids.POURED_CEMENT.block) && level.getFluidState(pos.below()).isSource())
                return;

            level.setBlock(pos, Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState(), 3);
        }
    }
}
