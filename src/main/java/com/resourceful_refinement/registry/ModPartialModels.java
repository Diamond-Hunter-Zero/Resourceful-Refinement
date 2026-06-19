package com.resourceful_refinement.registry;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import com.resourceful_refinement.ResourcefulRefinementMain;
import net.minecraft.resources.ResourceLocation;

public class ModPartialModels {
    // Generic Models
    public static final PartialModel SHAFT_X = PartialModel.of(ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "block/shafts/shaft_axis_x"));
    public static final PartialModel SHAFT_Z = PartialModel.of(ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "block/shafts/shaft_axis_z"));
    public static final PartialModel SHAFT_VERTICAL = PartialModel.of(ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "block/shafts/shaft_vertical"));
    public static final PartialModel GEYSER_CASING = PartialModel.of(ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "block/geyser_block"));
    public static final PartialModel NETHERRACK_GEYSER_CASING = PartialModel.of(ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "block/netherrack_geyser_block"));
    public static final PartialModel INDUSTRIAL_HEATER_STAND = PartialModel.of(ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "block/distillery/industrial_heater_stand"));
    public static final PartialModel ADVANCED_PUMP_COG = PartialModel.of(ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "block/advanced_pump/advanced_pump_cog"));

    // Combustion Chamber
    public static final PartialModel COMBUSTION_FAN_NORTH = PartialModel.of(ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "block/combustion_chamber/combustion_chamber_fan_casing_north"));
    public static final PartialModel COMBUSTION_FAN_EAST = PartialModel.of(ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "block/combustion_chamber/combustion_chamber_fan_casing_east"));
    public static final PartialModel COMBUSTION_FAN_SOUTH = PartialModel.of(ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "block/combustion_chamber/combustion_chamber_fan_casing_south"));
    public static final PartialModel COMBUSTION_FAN_WEST = PartialModel.of(ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "block/combustion_chamber/combustion_chamber_fan_casing_west"));
}
