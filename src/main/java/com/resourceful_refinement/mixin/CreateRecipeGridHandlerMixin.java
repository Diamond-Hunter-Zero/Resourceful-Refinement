package com.resourceful_refinement.mixin;

import com.resourceful_refinement.content.research.ResearchRecipeGate;
import com.simibubi.create.content.kinetics.crafter.MechanicalCraftingInput;
import com.simibubi.create.content.kinetics.crafter.RecipeGridHandler;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(value = RecipeGridHandler.class, remap = false)
public abstract class CreateRecipeGridHandlerMixin {
    @Inject(method = "tryToApplyRecipe", at = @At("HEAD"), cancellable = true, remap = false)
    private static void resourceful_refinement$blockLockedRegularCraftingRecipe(Level level,
            RecipeGridHandler.GroupedItems items, CallbackInfoReturnable<ItemStack> cir) {
        if (level == null || level.isClientSide || level.getServer() == null) {
            return;
        }

        items.calcStats();
        CraftingInput input = MechanicalCraftingInput.of(items);
        Optional<RecipeHolder<CraftingRecipe>> recipe = level.getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, input, level);
        if (recipe.isPresent()
                && RecipeGridHandler.isRecipeAllowed(recipe.get(), input)
                && !ResearchRecipeGate.canUseServerRecipe(level, recipe.get())) {
            cir.setReturnValue(null);
        }
    }
}
