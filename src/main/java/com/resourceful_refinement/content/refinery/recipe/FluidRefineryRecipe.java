package com.resourceful_refinement.content.refinery.recipe;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.registry.ModRecipeTypes;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public class FluidRefineryRecipe extends StandardProcessingRecipe<FluidRefineryRecipeInput> {
    public FluidRefineryRecipe(ProcessingRecipeParams params) {
        super(ModRecipeTypes.FLUID_REFINERY_TYPE_INFO, params);
        //ResourcefulRefinementMain.LOGGER.info("[FluidRefineryRecipe] Instantiated recipe with " + ingredients.size() + " item inputs, " + fluidIngredients.size() + " fluid inputs, and " + fluidResults.size() + " fluid outputs.");
    }

    @Override
    protected int getMaxInputCount() {
        return 2;
    }

    @Override
    protected int getMaxOutputCount() {
        return 0; // The refinery primarily outputs fluids in Phase 5
    }

    @Override
    protected int getMaxFluidInputCount() {
        return 2;
    }

    @Override
    protected int getMaxFluidOutputCount() {
        return 1;
    }

    @Override
    protected boolean canRequireHeat() {
        return true;
    }

    @Override
    protected boolean canSpecifyDuration() {
        return true;
    }

    @Override
    public boolean matches(FluidRefineryRecipeInput input, Level level) {
        if (input.items().size() < ingredients.size()) return false;
        if (input.fluids().size() < fluidIngredients.size()) return false;

        // Check Item Ingredients
        for (net.minecraft.world.item.crafting.Ingredient ingredient : ingredients) {
            boolean found = false;
            for (net.minecraft.world.item.ItemStack stack : input.items()) {
                if (ingredient.test(stack)) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }

        // Check Fluid Ingredients
        for (SizedFluidIngredient ingredient : fluidIngredients) {
            boolean found = false;
            for (net.neoforged.neoforge.fluids.FluidStack stack : input.fluids()) {
                if (ingredient.test(stack)) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }

        return true;
    }

    public boolean matchesFilter(com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour filter) {
        if (filter == null || filter.getFilter().isEmpty()) return true;

        // Check fluid outputs
        for (net.neoforged.neoforge.fluids.FluidStack fluid : getFluidResults()) {
            if (filter.test(fluid)) return true;
        }

        // Check item outputs
        for (com.simibubi.create.content.processing.recipe.ProcessingOutput output : results) {
            if (filter.test(output.getStack())) return true;
        }

        return false;
    }
}
