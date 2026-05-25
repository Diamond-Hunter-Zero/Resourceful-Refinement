package com.resourceful_refinement.content.fluids.base;

public enum FluidGroup {
    RAW(10, 3000, 3000, 2, 1300, "raw_still", "raw_flow"),
    CATALYSED(8, 2000, 1500, 2, 800, "molten_still", "molten_flow"),
    ALLOYED(8, 2000, 2500, 2, 1000, "smooth_liquid_still", "smooth_liquid_flow"),
    PURIFIED(8, 2000, 4000, 1, 1200, "template_fluid_still", "template_fluid_flow"),
    CARBORAX(0, 900, 1000, 1, 300, "smooth_liquid_still", "smooth_liquid_flow");

    public final int lightLevel;
    public final int density;
    public final int viscosity;
    public final int dropRate;
    public final int temperature;
    public final String stillTextureID;
    public final String flowTextureID;

    FluidGroup(int lightLevel, int density, int viscosity, int dropRate, int temperature, String stillTextureID, String flowTextureID) {
        this.lightLevel = lightLevel;
        this.density = density;
        this.viscosity = viscosity;
        this.dropRate = dropRate;
        this.temperature = temperature;
        this.stillTextureID = stillTextureID;
        this.flowTextureID = flowTextureID;
    }
}
