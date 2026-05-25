package com.resourceful_refinement.content.fracking_pump.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

/**
 * @param sourceBlock The block directly below the fracking pump outlet.
 * @param fluid       The fluid currently in the pump's input tank.
 * @param geyserFluid The associated fluid of the geyser at the source position,
 *                    or {@code null} / {@link Fluids#EMPTY} if the source block is not a geyser.
 */
public record FrackingPumpRecipeInput(Block sourceBlock, FluidStack fluid, @Nullable Fluid geyserFluid) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return fluid == null || fluid.isEmpty();
    }
}
