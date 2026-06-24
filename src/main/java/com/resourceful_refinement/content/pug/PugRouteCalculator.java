package com.resourceful_refinement.content.pug;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Objects;

public final class PugRouteCalculator {
    private PugRouteCalculator() {}

    public static PugRouteQuote quote(LaunchpadEndpoint source, LaunchpadEndpoint destination,
            PugRouteParameters parameters) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(destination, "destination");
        return quote(source.dimension(), source.controllerPos(), destination.dimension(), destination.controllerPos(),
                parameters);
    }

    public static PugRouteQuote quote(ResourceKey<Level> sourceDimension, BlockPos sourcePos,
            ResourceKey<Level> destinationDimension, BlockPos destinationPos, PugRouteParameters parameters) {
        Objects.requireNonNull(sourceDimension, "sourceDimension");
        Objects.requireNonNull(sourcePos, "sourcePos");
        Objects.requireNonNull(destinationDimension, "destinationDimension");
        Objects.requireNonNull(destinationPos, "destinationPos");
        Objects.requireNonNull(parameters, "parameters");

        double distance = Math.hypot((double) destinationPos.getX() - sourcePos.getX(),
                (double) destinationPos.getZ() - sourcePos.getZ());
        boolean crossDimension = !sourceDimension.equals(destinationDimension);
        long fuelSteps = startedSteps(distance, parameters.fuelStepBlocks());
        long travelSteps = startedSteps(distance, parameters.travelStepBlocks());
        long fuel = parameters.baseFuelMb() + fuelSteps * parameters.fuelMbPerStep()
                + (crossDimension ? parameters.crossDimensionFuelMb() : 0L);
        long ticks = parameters.baseTravelTicks() + travelSteps * parameters.travelTicksPerStep()
                + (crossDimension ? parameters.crossDimensionTravelTicks() : 0L);
        return new PugRouteQuote(distance, crossDimension, saturatingInt(fuel), saturatingInt(ticks));
    }

    static long startedSteps(double distance, int stepSize) {
        return distance <= 0 ? 0 : (long) Math.ceil(distance / stepSize);
    }

    private static int saturatingInt(long value) {
        return value >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
    }
}
