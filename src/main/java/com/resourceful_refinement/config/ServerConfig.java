package com.resourceful_refinement.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ServerConfig {
    public static final ModConfigSpec.Builder CONFIG_BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue EXAMPLE_SETTING;

    public static final ModConfigSpec.DoubleValue CHILLED_COOLANT_CONSUMPTION;
    public static final ModConfigSpec.DoubleValue COOLED_COOLANT_CONSUMPTION;
    public static final ModConfigSpec.DoubleValue PASSIVE_COOLANT_CONSUMPTION;
    public static final ModConfigSpec.DoubleValue HEATED_COOLANT_CONSUMPTION;
    public static final ModConfigSpec.DoubleValue SUPERHEATED_COOLANT_CONSUMPTION;
    public static final ModConfigSpec.BooleanValue GLARE_DEBUG_LOGGING;
    public static final ModConfigSpec.IntValue GLARE_LOS_CHECKS_PER_TICK;
    public static final ModConfigSpec.IntValue GLARE_LINK_SYNC_INTERVAL;
    public static final ModConfigSpec.IntValue GLARE_LUX_HISTORY_SAMPLE_INTERVAL;
    public static final ModConfigSpec.IntValue GLARE_LUX_HISTORY_SAMPLES;
    public static final ModConfigSpec.IntValue PUG_TANK_CAPACITY_MB;
    public static final ModConfigSpec.IntValue PUG_BASE_FUEL_MB;
    public static final ModConfigSpec.IntValue PUG_FUEL_PER_STEP_MB;
    public static final ModConfigSpec.IntValue PUG_FUEL_STEP_BLOCKS;
    public static final ModConfigSpec.IntValue PUG_CROSS_DIMENSION_FUEL_MB;
    public static final ModConfigSpec.IntValue PUG_BASE_TRAVEL_TICKS;
    public static final ModConfigSpec.IntValue PUG_TRAVEL_TICKS_PER_STEP;
    public static final ModConfigSpec.IntValue PUG_TRAVEL_STEP_BLOCKS;
    public static final ModConfigSpec.IntValue PUG_CROSS_DIMENSION_TRAVEL_TICKS;


    static {
        CONFIG_BUILDER.push("Config Section 1");
        EXAMPLE_SETTING = CONFIG_BUILDER
                .comment("Defines a server-side setting (Value between 1 and 100)")
                .defineInRange("exampleSetting", 20, 1, 100);
        CONFIG_BUILDER.pop();


        // Coolant Consumption
        CONFIG_BUILDER.push("Radiator Parameters");

        CHILLED_COOLANT_CONSUMPTION = CONFIG_BUILDER
                .comment("The rate at which Chilled coolants are consumed (Value between 0 and 1000)")
                .comment("Decimal values below 1 will be treated as percentile chances to consume 1 mb")
                .defineInRange("chilled_coolant_consumption", 0.25f, 0f, 1000f);

        COOLED_COOLANT_CONSUMPTION = CONFIG_BUILDER
                .comment("The rate at which Cooled coolants are consumed (Value between 0 and 1000)")
                .comment("Decimal values below 1 will be treated as percentile chances to consume 1 mb")
                .defineInRange("cooled_coolant_consumption", 0.75f, 0f, 1000f);

        PASSIVE_COOLANT_CONSUMPTION = CONFIG_BUILDER
                .comment("The rate at which Passive coolants are consumed (Value between 0 and 1000)")
                .comment("Decimal values below 1 will be treated as percentile chances to consume 1 mb")
                .defineInRange("passive_coolant_consumption", 0.25f, 0f, 1000f);

        HEATED_COOLANT_CONSUMPTION = CONFIG_BUILDER
                .comment("The rate at which Heated coolants are consumed (Value between 0 and 1000)")
                .comment("Decimal values below 1 will be treated as percentile chances to consume 1 mb")
                .defineInRange("heated_coolant_consumption", 0.20f, 0f, 1000f);

        SUPERHEATED_COOLANT_CONSUMPTION = CONFIG_BUILDER
                .comment("The rate at which Superheated coolants are consumed (Value between 0 and 1000)")
                .comment("Decimal values below 1 will be treated as percentile chances to consume 1 mb")
                .defineInRange("superheated_coolant_consumption", 0.5f, 0f, 1000f);

        CONFIG_BUILDER.pop();

        CONFIG_BUILDER.push("GLARE Networks");
        GLARE_DEBUG_LOGGING = CONFIG_BUILDER
                .comment("Logs GLARE topology, reconciliation, and line-of-sight mutations for diagnostics")
                .define("debug_logging", false);
        GLARE_LOS_CHECKS_PER_TICK = CONFIG_BUILDER
                .comment("Maximum GLARE links examined for line-of-sight changes per dimension tick")
                .defineInRange("los_checks_per_tick", 32, 1, 4096);
        GLARE_LINK_SYNC_INTERVAL = CONFIG_BUILDER
                .comment("Ticks between GLARE link render synchronization packets")
                .defineInRange("link_sync_interval", 20, 1, 200);
        GLARE_LUX_HISTORY_SAMPLE_INTERVAL = CONFIG_BUILDER
                .comment("Ticks between GLARE network Lux history samples")
                .defineInRange("lux_history_sample_interval", 40, 1, 20 * 60);
        GLARE_LUX_HISTORY_SAMPLES = CONFIG_BUILDER
                .comment("Maximum Lux history samples retained per GLARE network")
                .defineInRange("lux_history_samples", 16, 1, 256);
        CONFIG_BUILDER.pop();

        CONFIG_BUILDER.push("PUG Logistics");
        PUG_TANK_CAPACITY_MB = CONFIG_BUILDER
                .comment("Launchpad fuel tank capacity in millibuckets")
                .defineInRange("tank_capacity_mb", 16_000, 1_000, 1_000_000);
        PUG_BASE_FUEL_MB = CONFIG_BUILDER
                .comment("Fuel charged for every launch before distance and dimension costs")
                .defineInRange("base_fuel_mb", 500, 0, 1_000_000);
        PUG_FUEL_PER_STEP_MB = CONFIG_BUILDER
                .comment("Fuel charged for each started distance step")
                .defineInRange("fuel_per_step_mb", 500, 0, 1_000_000);
        PUG_FUEL_STEP_BLOCKS = CONFIG_BUILDER
                .comment("Horizontal blocks in one fuel distance step")
                .defineInRange("fuel_step_blocks", 1_000, 1, 30_000_000);
        PUG_CROSS_DIMENSION_FUEL_MB = CONFIG_BUILDER
                .comment("Additional fuel charged when source and destination dimensions differ")
                .defineInRange("cross_dimension_fuel_mb", 2_000, 0, 1_000_000);
        PUG_BASE_TRAVEL_TICKS = CONFIG_BUILDER
                .comment("Base flight duration in ticks (400 ticks = 20 seconds)")
                .defineInRange("base_travel_ticks", 400, 0, Integer.MAX_VALUE);
        PUG_TRAVEL_TICKS_PER_STEP = CONFIG_BUILDER
                .comment("Ticks added for each started travel distance step")
                .defineInRange("travel_ticks_per_step", 20, 0, Integer.MAX_VALUE);
        PUG_TRAVEL_STEP_BLOCKS = CONFIG_BUILDER
                .comment("Horizontal blocks in one travel-time distance step")
                .defineInRange("travel_step_blocks", 50, 1, 30_000_000);
        PUG_CROSS_DIMENSION_TRAVEL_TICKS = CONFIG_BUILDER
                .comment("Additional flight duration when dimensions differ (2400 ticks = 120 seconds)")
                .defineInRange("cross_dimension_travel_ticks", 2_400, 0, Integer.MAX_VALUE);
        CONFIG_BUILDER.pop();

        SPEC = CONFIG_BUILDER.build();
    }
}
