package com.resourceful_refinement.content.sieve;

import com.resourceful_refinement.ResourcefulRefinementMain;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class MechanicalSieveLayers {
    public static final ModelLayerLocation CASING = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "mechanical_sieve_casing"), "main");
    public static final ModelLayerLocation CASING_BOTTOM = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "mechanical_sieve_casing_bottom"), "main");
    public static final ModelLayerLocation CASING_MIDDLE = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "mechanical_sieve_casing_middle"), "main");
    public static final ModelLayerLocation CASING_TOP = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "mechanical_sieve_casing_top"), "main");

    public static final ModelLayerLocation COG = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "mechanical_sieve_cog"), "main");
}
