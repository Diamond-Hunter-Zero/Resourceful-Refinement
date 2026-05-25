package com.resourceful_refinement.content.fluids.base;

import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

import java.util.function.Supplier;

public abstract class GeneralizedFlowingFluid extends BaseFlowingFluid {
    private final int dropRate;

    protected GeneralizedFlowingFluid(Properties properties, int dropRate) {
        super(properties);
        this.dropRate = dropRate;
    }

    @Override
    protected int getDropOff(LevelReader worldIn) {
        return this.dropRate;
    }

    public static class Source extends GeneralizedFlowingFluid {
        public Source(Properties properties, int dropRate) {
            super(properties, dropRate);
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }

        @Override
        public int getAmount(FluidState state) {
            return 8;
        }
    }

    public static class Flowing extends GeneralizedFlowingFluid {
        public Flowing(Properties properties, int dropRate) {
            super(properties, dropRate);
        }

        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }
    }
}
