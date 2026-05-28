package com.resourceful_refinement.content.refill_station;

import com.resourceful_refinement.ResourcefulRefinementMain;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;

public class FluidRefillStationLayers {

    public static final ModelLayerLocation CASING = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "fluid_refill_station_casing"),
            "main"
    );

    public static LayerDefinition createCasingLayer() {
        return FluidRefillStationCasingModel.createBodyLayer();
    }
}
