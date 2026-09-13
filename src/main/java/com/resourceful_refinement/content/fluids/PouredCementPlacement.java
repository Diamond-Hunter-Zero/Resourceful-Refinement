package com.resourceful_refinement.content.fluids;

import com.resourceful_refinement.registry.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

public class PouredCementPlacement {
    public static final int LIQUID_CONCRETE_PER_SOURCE_MB = 125;

    private PouredCementPlacement() {
    }

    public static boolean shouldPlacePouredCement(FluidStack stack) {
        return !stack.isEmpty()
            && stack.getAmount() >= LIQUID_CONCRETE_PER_SOURCE_MB
            && stack.getFluid().isSame(ModFluids.LIQUID_CONCRETE.source.get());
    }

    public static int placementAmountFor(FluidStack stack, int fallbackAmount) {
        return stack.getFluid().isSame(ModFluids.LIQUID_CONCRETE.source.get())
            ? LIQUID_CONCRETE_PER_SOURCE_MB
            : fallbackAmount;
    }

    public static Fluid fluidToPlace(Fluid fluid) {
        return fluid.isSame(ModFluids.LIQUID_CONCRETE.source.get())
            ? ModFluids.POURED_CEMENT.source.get()
            : fluid;
    }

    public static boolean tryPlaceAtPipeOutlet(Level level, BlockPos pos, FluidStack stack, boolean simulate) {
        if (level == null || !level.isLoaded(pos) || !shouldPlacePouredCement(stack))
            return false;

        BlockState currentState = level.getBlockState(pos);
        if (!currentState.getFluidState().isEmpty() && currentState.getFluidState().isSource())
            return false;
        if (!currentState.canBeReplaced())
            return false;

        if (simulate)
            return true;

        BlockState pouredCement = ModFluids.POURED_CEMENT.source.get()
            .defaultFluidState()
            .createLegacyBlock();
        if (pouredCement.hasProperty(LiquidBlock.LEVEL))
            pouredCement = pouredCement.setValue(LiquidBlock.LEVEL, 0);

        return level.setBlock(pos, pouredCement, 3);
    }
}
