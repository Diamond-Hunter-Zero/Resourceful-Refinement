package com.resourceful_refinement.mixin;

import com.resourceful_refinement.content.research.ResearchRecipeGate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(CrafterBlock.class)
public abstract class CrafterBlockMixin {
    @Inject(method = "getPotentialResults", at = @At("RETURN"), cancellable = true)
    private static void resourceful_refinement$filterLockedResearchRecipes(Level level, CraftingInput input,
            CallbackInfoReturnable<Optional<RecipeHolder<CraftingRecipe>>> cir) {
        Optional<RecipeHolder<CraftingRecipe>> recipe = cir.getReturnValue();
        if (recipe.isPresent() && !ResearchRecipeGate.canUseServerRecipe(level, recipe.get())) {
            cir.setReturnValue(Optional.empty());
        }
    }

    @Inject(method = "dispenseItem", at = @At("HEAD"), cancellable = true)
    private void resourceful_refinement$blockLockedResearchRecipe(ServerLevel level, BlockPos pos,
            CrafterBlockEntity crafter, ItemStack stack, BlockState state, RecipeHolder<?> recipe, CallbackInfo ci) {
        if (!ResearchRecipeGate.canUseServerRecipe(level, recipe)) {
            ci.cancel();
        }
    }
}
