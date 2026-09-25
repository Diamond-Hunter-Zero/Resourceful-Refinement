package com.resourceful_refinement.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ServerConfig {
    public static final ModConfigSpec.Builder CONFIG_BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    //public static final ModConfigSpec.IntValue EXAMPLE_SETTING;

    public static final ModConfigSpec.DoubleValue CHILLED_COOLANT_CONSUMPTION;
    public static final ModConfigSpec.DoubleValue COOLED_COOLANT_CONSUMPTION;
    public static final ModConfigSpec.DoubleValue PASSIVE_COOLANT_CONSUMPTION;
    public static final ModConfigSpec.DoubleValue HEATED_COOLANT_CONSUMPTION;
    public static final ModConfigSpec.DoubleValue SUPERHEATED_COOLANT_CONSUMPTION;

    public static final ModConfigSpec.IntValue LIQUID_LUCK_BASE;
    public static final ModConfigSpec.IntValue LIQUID_LUCK_INCREMENT_RATE;

    public static final ModConfigSpec.DoubleValue ADVANCED_PUMP_STRENGTH;


    static {
        /*CONFIG_BUILDER.push("Config Section 1");
        EXAMPLE_SETTING = CONFIG_BUILDER
                .comment("Defines a server-side setting (Value between 1 and 100)")
                .defineInRange("exampleSetting", 20, 1, 100);
        CONFIG_BUILDER.pop();*/


        // Coolant Consumption
        CONFIG_BUILDER.push("Radiator Parameters");

        CHILLED_COOLANT_CONSUMPTION = CONFIG_BUILDER
                .comment("The rate at which Chilled conduction fluids are consumed (Value between 0 and 1000)")
                .comment("Decimal values below 1 will be treated as percentile chances to consume 1 mb")
                .defineInRange("chilled_coolant_consumption", 0.25f, 0f, 1000f);

        COOLED_COOLANT_CONSUMPTION = CONFIG_BUILDER
                .comment("The rate at which Cooled conduction fluids are consumed (Value between 0 and 1000)")
                .comment("Decimal values below 1 will be treated as percentile chances to consume 1 mb")
                .defineInRange("cooled_coolant_consumption", 1f, 0f, 1000f);

        PASSIVE_COOLANT_CONSUMPTION = CONFIG_BUILDER
                .comment("The rate at which Passive conduction fluids are consumed (Value between 0 and 1000)")
                .comment("Decimal values below 1 will be treated as percentile chances to consume 1 mb")
                .defineInRange("passive_coolant_consumption", 0.35f, 0f, 1000f);

        HEATED_COOLANT_CONSUMPTION = CONFIG_BUILDER
                .comment("The rate at which Heated conduction fluids are consumed (Value between 0 and 1000)")
                .comment("Decimal values below 1 will be treated as percentile chances to consume 1 mb")
                .defineInRange("heated_coolant_consumption", 0.4f, 0f, 1000f);

        SUPERHEATED_COOLANT_CONSUMPTION = CONFIG_BUILDER
                .comment("The rate at which Superheated conduction fluids are consumed (Value between 0 and 1000)")
                .comment("Decimal values below 1 will be treated as percentile chances to consume 1 mb")
                .defineInRange("superheated_coolant_consumption", 0.8f, 0f, 1000f);

        CONFIG_BUILDER.pop();

        // Coating Parameters
        CONFIG_BUILDER.push("Coating Parameters");

        LIQUID_LUCK_BASE = CONFIG_BUILDER
                .comment("The base enchantment power Liquid Luck coatings set on a tool (Value between 0 and 256)")
                .defineInRange("liquid_luck_base", 2, 0, 256);

        LIQUID_LUCK_INCREMENT_RATE = CONFIG_BUILDER
                .comment("The amount by which Liquid Luck coatings increase existing enchantment power, if the existing enchantment is above the base value (Value between 0 and 256)")
                .defineInRange("liquid_luck_increment_rate", 1, 0, 256);

        CONFIG_BUILDER.pop();

        // Kinetic Parameters
        CONFIG_BUILDER.push("Kinetics");

        ADVANCED_PUMP_STRENGTH = CONFIG_BUILDER
                .comment("The pumping-distance multiplier for the Advanced Pump (Value between 0 and 1000)")
                .defineInRange("advanced_pump_strength", 2f, 0f, 1000f);

        CONFIG_BUILDER.pop();


        // Build config
        SPEC = CONFIG_BUILDER.build();
    }
}
