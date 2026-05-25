package com.resourceful_refinement.content.refinery.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public record FluidRefineryRecipeInput(List<ItemStack> items, List<FluidStack> fluids) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        return items.get(index);
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack item : items) {
            if (!item.isEmpty()) {
                return false;
            }
        }
        for (FluidStack fluid : fluids) {
            if (fluid != null && !fluid.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
