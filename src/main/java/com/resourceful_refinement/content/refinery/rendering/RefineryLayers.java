package com.resourceful_refinement.content.refinery.rendering;

import com.resourceful_refinement.ResourcefulRefinementMain;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class RefineryLayers {
    // These strings must be unique within your mod
    public static final ModelLayerLocation BASE = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "refinery_base"), "main");

    public static final ModelLayerLocation MIDDLE = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "refinery_middle"), "main");

    public static final ModelLayerLocation TOP = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "refinery_top"), "main");

    public static final ModelLayerLocation BLENDER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "refinery_blender"), "main");
}