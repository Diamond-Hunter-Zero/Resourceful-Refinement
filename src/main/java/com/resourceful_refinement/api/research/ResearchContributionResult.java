package com.resourceful_refinement.api.research;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.UUID;

/**
 * Result of applying item/fluid contributions toward a research node.
 */
public record ResearchContributionResult(ResourceLocation nodeId,
                                         UUID playerId,
                                         boolean global,
                                         boolean changed,
                                         boolean unlocked,
                                         Map<ResourceLocation, Integer> acceptedItems,
                                         Map<ResourceLocation, Integer> acceptedFluids,
                                         ResearchProgress progress) {
    public ResearchContributionResult {
        acceptedItems = Map.copyOf(acceptedItems);
        acceptedFluids = Map.copyOf(acceptedFluids);
    }
}
