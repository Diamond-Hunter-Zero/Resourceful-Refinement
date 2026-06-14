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

        SPEC = CONFIG_BUILDER.build();
    }
}
