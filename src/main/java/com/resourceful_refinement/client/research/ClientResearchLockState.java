package com.resourceful_refinement.client.research;

import com.resourceful_refinement.network.ResearchCraftingLockPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class ClientResearchLockState {
    private static final Map<Integer, LockedRecipe> CRAFTING_LOCKS = new HashMap<>();

    private ClientResearchLockState() {}

    public static void handle(ResearchCraftingLockPayload payload) {
        if (payload.locked()) {
            payload.recipeId().ifPresent(recipeId ->
                    CRAFTING_LOCKS.put(payload.containerId(), new LockedRecipe(recipeId, payload.lockingNode())));
        } else {
            CRAFTING_LOCKS.remove(payload.containerId());
        }
    }

    public static boolean isCraftingResultLocked(int containerId) {
        return CRAFTING_LOCKS.containsKey(containerId);
    }

    public static Optional<LockedRecipe> getCraftingLock(int containerId) {
        return Optional.ofNullable(CRAFTING_LOCKS.get(containerId));
    }

    public static void clear(int containerId) {
        CRAFTING_LOCKS.remove(containerId);
    }

    public static void clearAll() {
        CRAFTING_LOCKS.clear();
    }

    public record LockedRecipe(ResourceLocation recipeId, Optional<ResourceLocation> lockingNode) {}
}
