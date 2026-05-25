package com.resourceful_refinement.content.sieve.recipe;

import com.resourceful_refinement.registry.ModRecipeTypes;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public class MechanicalSieveRecipe extends StandardProcessingRecipe<MechanicalSieveRecipeInput> {
    public MechanicalSieveRecipe(ProcessingRecipeParams params) {
        super(ModRecipeTypes.MECHANICAL_SIEVE_TYPE_INFO, params);
    }

    @Override
    protected int getMaxInputCount() {
        return 0;
    }

    @Override
    protected int getMaxOutputCount() {
        return 1;
    }

    @Override
    protected int getMaxFluidInputCount() {
        return 1;
    }

    @Override
    protected int getMaxFluidOutputCount() {
        return 1;
    }

    @Override
    protected boolean canSpecifyDuration() {
        return true;
    }

    @Override
    public boolean matches(MechanicalSieveRecipeInput input, Level level) {
        if (input.fluids().isEmpty()) return false;
        
        // Match the single fluid ingredient
        if (fluidIngredients.isEmpty()) return false;
        SizedFluidIngredient ingredient = fluidIngredients.get(0);
        
        for (FluidStack stack : input.fluids()) {
            if (ingredient.test(stack)) {
                return true;
            }
        }
        
        return false;
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
