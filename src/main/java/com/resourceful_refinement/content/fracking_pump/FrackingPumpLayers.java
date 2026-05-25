package com.resourceful_refinement.content.fracking_pump;

import com.resourceful_refinement.ResourcefulRefinementMain;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class FrackingPumpLayers {
    public static final ModelLayerLocation OUTLET = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "fracking_pump_outlet"), "main");
    public static final ModelLayerLocation BASE = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "fracking_pump_base"), "main");
    public static final ModelLayerLocation SHAFT = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "fracking_pump_shaft"), "main");
    public static final ModelLayerLocation TOP = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "fracking_pump_top"), "main");
    public static final ModelLayerLocation COUNTERWEIGHT = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "fracking_pump_counterweight"), "main");
}
