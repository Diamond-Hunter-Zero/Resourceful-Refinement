package com.resourceful_refinement.registry;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import com.resourceful_refinement.ResourcefulRefinementMain;
import net.minecraft.resources.ResourceLocation;

public class ModPartialModels {
    public static final PartialModel SHAFT_X = PartialModel.of(ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "block/shafts/shaft_axis_x"));
    public static final PartialModel SHAFT_Z = PartialModel.of(ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "block/shafts/shaft_axis_z"));
    public static final PartialModel SHAFT_VERTICAL = PartialModel.of(ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "block/shafts/shaft_vertical"));
    public static final PartialModel GEYSER_CASING = PartialModel.of(ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "block/geyser_block"));
    public static final PartialModel NETHERRACK_GEYSER_CASING = PartialModel.of(ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "block/netherrack_geyser_block"));
}
