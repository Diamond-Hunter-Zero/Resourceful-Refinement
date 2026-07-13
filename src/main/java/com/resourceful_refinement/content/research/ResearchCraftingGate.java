package com.resourceful_refinement.content.research;

import com.resourceful_refinement.api.research.ResearchApi;
import com.resourceful_refinement.network.ResearchCraftingLockPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Objects;
import java.util.Optional;

/**
 * Server-side helper for player-bound crafting recipe locks.
 */
public final class ResearchCraftingGate {
    public static final Component RECIPE_LOCKED_MESSAGE = Component.literal("Recipe Locked");

    private ResearchCraftingGate() {}

    public static Optional<Result> findLockedRecipe(ServerPlayer player, CraftingContainer craftSlots) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(craftSlots, "craftSlots");
        if (!ResearchApi.isEnabled()) return Optional.empty();

        Optional<RecipeHolder<CraftingRecipe>> recipe = player.serverLevel().getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, craftSlots.asCraftInput(), player.serverLevel());
        if (recipe.isEmpty() || ResearchApi.canPlayerCraft(player, recipe.get().id())) {
            return Optional.empty();
        }

        Optional<ResourceLocation> lockingNode = ResearchApi.getLockingNodes(player.server, recipe.get().id()).stream()
                .filter(nodeId -> !ResearchApi.hasUnlocked(player, nodeId))
                .findFirst();
        return Optional.of(new Result(recipe.get().id(), lockingNode, RECIPE_LOCKED_MESSAGE));
    }

    public static Optional<Result> findLockedServerRecipe(Level level, CraftingContainer craftSlots) {
        Objects.requireNonNull(craftSlots, "craftSlots");
        if (!ResearchApi.isEnabled() || level == null || level.isClientSide || level.getServer() == null) {
            return Optional.empty();
        }

        Optional<RecipeHolder<CraftingRecipe>> recipe = level.getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, craftSlots.asCraftInput(), level);
        if (recipe.isEmpty()) {
            return Optional.empty();
        }

        ResearchRecipeGate.Result result = ResearchRecipeGate.checkServerRecipe(level, recipe.get());
        return result.allowed()
                ? Optional.empty()
                : Optional.of(new Result(recipe.get().id(), result.lockingNode(), RECIPE_LOCKED_MESSAGE));
    }

    public static boolean canPlayerTakeCraftingResult(ServerPlayer player, CraftingContainer craftSlots) {
        return findLockedRecipe(player, craftSlots).isEmpty();
    }

    public static Optional<Result> syncCraftingLockState(ServerPlayer player, int containerId,
                                                         CraftingContainer craftSlots) {
        Optional<Result> result = findLockedRecipe(player, craftSlots);
        PacketDistributor.sendToPlayer(player, result
                .map(locked -> ResearchCraftingLockPayload.locked(containerId, locked.recipeId(), locked.lockingNode()))
                .orElseGet(() -> ResearchCraftingLockPayload.unlocked(containerId)));
        return result;
    }

    public static Optional<Result> syncServerCraftingLockState(ServerPlayer player, int containerId,
                                                               CraftingContainer craftSlots) {
        Optional<Result> result = findLockedServerRecipe(player.level(), craftSlots);
        PacketDistributor.sendToPlayer(player, result
                .map(locked -> ResearchCraftingLockPayload.locked(containerId, locked.recipeId(), locked.lockingNode()))
                .orElseGet(() -> ResearchCraftingLockPayload.unlocked(containerId)));
        return result;
    }

    public record Result(ResourceLocation recipeId, Optional<ResourceLocation> lockingNode, Component message) {}
}
