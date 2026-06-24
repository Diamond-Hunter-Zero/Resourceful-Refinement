package com.resourceful_refinement.content.pug;

import com.resourceful_refinement.config.ServerConfig;

/** Server-controlled balance values used by route quoting. */
public record PugRouteParameters(int baseFuelMb, int fuelMbPerStep, int fuelStepBlocks,
        int crossDimensionFuelMb, int baseTravelTicks, int travelTicksPerStep, int travelStepBlocks,
        int crossDimensionTravelTicks) {
    public static final int DEFAULT_TANK_CAPACITY_MB = 16_000;
    public static final int DEFAULT_BASE_FUEL_MB = 500;
    public static final int DEFAULT_FUEL_PER_STEP_MB = 500;
    public static final int DEFAULT_FUEL_STEP_BLOCKS = 1_000;
    public static final int DEFAULT_CROSS_DIMENSION_FUEL_MB = 2_000;
    public static final int DEFAULT_BASE_TRAVEL_TICKS = 20 * 20;
    public static final int DEFAULT_TRAVEL_TICKS_PER_STEP = 20;
    public static final int DEFAULT_TRAVEL_STEP_BLOCKS = 50;
    public static final int DEFAULT_CROSS_DIMENSION_TRAVEL_TICKS = 120 * 20;

    public PugRouteParameters {
        if (baseFuelMb < 0 || fuelMbPerStep < 0 || fuelStepBlocks <= 0 || crossDimensionFuelMb < 0
                || baseTravelTicks < 0 || travelTicksPerStep < 0 || travelStepBlocks <= 0
                || crossDimensionTravelTicks < 0) {
            throw new IllegalArgumentException("PUG route parameters must be non-negative and step sizes positive");
        }
    }

    public static PugRouteParameters defaults() {
        return new PugRouteParameters(DEFAULT_BASE_FUEL_MB, DEFAULT_FUEL_PER_STEP_MB, DEFAULT_FUEL_STEP_BLOCKS,
                DEFAULT_CROSS_DIMENSION_FUEL_MB, DEFAULT_BASE_TRAVEL_TICKS, DEFAULT_TRAVEL_TICKS_PER_STEP,
                DEFAULT_TRAVEL_STEP_BLOCKS, DEFAULT_CROSS_DIMENSION_TRAVEL_TICKS);
    }

    public static PugRouteParameters fromServerConfig() {
        return new PugRouteParameters(ServerConfig.PUG_BASE_FUEL_MB.get(), ServerConfig.PUG_FUEL_PER_STEP_MB.get(),
                ServerConfig.PUG_FUEL_STEP_BLOCKS.get(), ServerConfig.PUG_CROSS_DIMENSION_FUEL_MB.get(),
                ServerConfig.PUG_BASE_TRAVEL_TICKS.get(), ServerConfig.PUG_TRAVEL_TICKS_PER_STEP.get(),
                ServerConfig.PUG_TRAVEL_STEP_BLOCKS.get(), ServerConfig.PUG_CROSS_DIMENSION_TRAVEL_TICKS.get());
    }
}
