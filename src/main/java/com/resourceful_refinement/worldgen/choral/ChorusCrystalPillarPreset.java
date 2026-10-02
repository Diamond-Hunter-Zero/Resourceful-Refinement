package com.resourceful_refinement.worldgen.choral;

public record ChorusCrystalPillarPreset(
        String name,
        int minCount,
        int maxCount,
        float minRadius,
        float maxRadius,
        float minLength,
        float maxLength,
        float verticalWeight,
        float slantedWeight,
        float pointedWeight,
        float minSlantDegrees,
        float maxSlantDegrees,
        float taperStrength,
        int rootDepth,
        float monumentalChance
) {
    public float totalWeight() {
        return verticalWeight + slantedWeight + pointedWeight;
    }
}
