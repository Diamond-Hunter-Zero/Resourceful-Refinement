package com.resourceful_refinement.content.forge_mould.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.fluids.FluidStack;


/**
 * Recipe input for the Mechanical Forge Mould.
 *
 * @param hasCastingDepot Whether a CastingDepot is present at the target workspace.
 *                        Used by recipes with {@code "casting": true} to restrict themselves
 *                        to runs where the correct depot type is below the machine.
 */
public record MechanicalForgeMouldRecipeInput(ItemStack item, FluidStack fluid, boolean hasCastingDepot, ItemStack depotItem) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        if (index == 0) return item;
        return ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return item.isEmpty() && (fluid == null || fluid.isEmpty());
    }
}
