package com.resourceful_refinement.content.forge_mould;

import com.resourceful_refinement.ResourcefulRefinementMain;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class ForgeMouldLayers {
    public static final ModelLayerLocation CASING = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "forge_mould_casing"), "main");
    public static final ModelLayerLocation PRESS = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "forge_mould_press"), "main");
    public static final ModelLayerLocation TUBE = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "forge_mould_tube"), "main");
}
