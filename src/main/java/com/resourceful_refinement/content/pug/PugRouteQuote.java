package com.resourceful_refinement.content.pug;

public record PugRouteQuote(double horizontalDistance, boolean crossDimension, int fuelCostMb, int travelTicks) {
    public PugRouteQuote {
        if (!Double.isFinite(horizontalDistance) || horizontalDistance < 0 || fuelCostMb < 0 || travelTicks < 0) {
            throw new IllegalArgumentException("Invalid PUG route quote");
        }
    }
}
