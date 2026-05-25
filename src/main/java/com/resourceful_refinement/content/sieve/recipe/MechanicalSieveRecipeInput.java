package com.resourceful_refinement.content.sieve.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

/**
 * Recipe input for the Mechanical Sieve.
 * Although the sieve only takes fluid, we implement RecipeInput to satisfy Create's StandardProcessingRecipe.
 */
public record MechanicalSieveRecipeInput(List<FluidStack> fluids) implements RecipeInput {
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
        return fluids.isEmpty();
    }
}
