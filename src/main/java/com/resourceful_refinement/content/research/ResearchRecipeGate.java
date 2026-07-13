package com.resourceful_refinement.content.research;

import com.resourceful_refinement.api.research.ResearchApi;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import java.util.Objects;
import java.util.Optional;

/**
 * Server-side helper for checking whether a machine recipe is currently unlocked.
 * Client worlds and missing recipe IDs are treated as allowed so render/UI paths stay passive.
 */
public final class ResearchRecipeGate {
    public static final Component RECIPE_LOCKED_MESSAGE = Component.literal("Recipe Locked");

    private ResearchRecipeGate() {}

    public static Result checkServerRecipe(Level level, RecipeHolder<?> recipe) {
        Objects.requireNonNull(recipe, "recipe");
        return checkServerRecipe(level, recipe.id());
    }

    public static Result checkServerRecipe(Level level, ResourceLocation recipeId) {
        if (recipeId == null || level == null || level.isClientSide || level.getServer() == null) {
            return Result.allowed(recipeId);
        }

        Optional<ResourceLocation> lockingNode = ResearchApi.getLockingNodes(level.getServer(), recipeId).stream()
                .filter(nodeId -> !ResearchApi.hasAnyServerUnlock(level.getServer(), nodeId))
                .findFirst();
        if (lockingNode.isEmpty()) {
            return Result.allowed(recipeId);
        }
        return Result.blocked(recipeId, lockingNode.get());
    }

    public static boolean canUseServerRecipe(Level level, RecipeHolder<?> recipe) {
        return checkServerRecipe(level, recipe).allowed();
    }

    public static boolean canUseServerRecipe(Level level, ResourceLocation recipeId) {
        return checkServerRecipe(level, recipeId).allowed();
    }

    public record Result(boolean allowed, ResourceLocation recipeId, Optional<ResourceLocation> lockingNode,
                         Component message) {
        private static Result allowed(ResourceLocation recipeId) {
            return new Result(true, recipeId, Optional.empty(), Component.empty());
        }

        private static Result blocked(ResourceLocation recipeId, ResourceLocation lockingNode) {
            return new Result(false, recipeId, Optional.of(lockingNode), RECIPE_LOCKED_MESSAGE);
        }
    }
}
