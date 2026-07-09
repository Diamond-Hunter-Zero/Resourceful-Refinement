package com.resourceful_refinement.content.mechanical_stamper.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.fluids.FluidStack;

public record MechanicalStamperRecipeInput(ItemStack target, ItemStack stamp, ItemStack fillMedium, FluidStack fillFluid,
                                           boolean paired) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        return switch (index) {
            case 0 -> target;
            case 1 -> stamp;
            case 2 -> fillMedium;
            default -> throw new IllegalArgumentException("No item for index " + index);
        };
    }

    @Override
    public int size() {
        return 3;
    }
}
