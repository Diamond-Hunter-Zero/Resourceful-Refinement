package com.resourceful_refinement.content.brewers_tap.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.fluids.FluidStack;

public record BrewersTapRecipeInput(ItemStack workItem, FluidStack fluid, ItemStack flavourItem) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        return switch (index) {
            case 0 -> workItem;
            case 1 -> flavourItem;
            default -> ItemStack.EMPTY;
        };
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        return workItem.isEmpty() && flavourItem.isEmpty() && (fluid == null || fluid.isEmpty());
    }
}
