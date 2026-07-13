package com.resourceful_refinement.api.research;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;

/**
 * Immutable progress snapshot for a single research node's requirements.
 */
public record ResearchProgress(Map<ResourceLocation, Integer> itemProgress,
                               Map<ResourceLocation, Integer> fluidProgress,
                               int requiredUnits,
                               int completedUnits) {
    public ResearchProgress {
        itemProgress = Map.copyOf(itemProgress);
        fluidProgress = Map.copyOf(fluidProgress);
        requiredUnits = Math.max(0, requiredUnits);
        completedUnits = Math.max(0, Math.min(completedUnits, requiredUnits));
    }

    public boolean isComplete() {
        return requiredUnits == 0 || completedUnits >= requiredUnits;
    }

    public boolean hasAnyProgress() {
        return completedUnits > 0;
    }

    public double ratio() {
        return requiredUnits == 0 ? 1.0 : completedUnits / (double) requiredUnits;
    }
}
