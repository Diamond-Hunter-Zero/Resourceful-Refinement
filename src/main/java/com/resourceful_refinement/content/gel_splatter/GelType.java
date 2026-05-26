package com.resourceful_refinement.content.gel_splatter;

import net.minecraft.resources.ResourceLocation;
import com.resourceful_refinement.ResourcefulRefinementMain;

public enum GelType {
    MOLTEN("molten", ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "makes_molten_gel")),
    SPEEDY("speedy", ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "makes_speedy_gel")),
    GOOEY("gooey", ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "makes_gooey_gel")),
    BOUNCY("bouncy", ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "makes_bouncy_gel")),
    CURSED("cursed", ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "makes_cursed_gel")),
    BLESSED("blessed", ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "makes_blessed_gel")),
    INERT("inert", ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "makes_inert_gel")),
    CLEANSE("cleanse", ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "makes_cleanse_gel")),
    POTION("potion", ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "makes_potion_gel")),
    PAINT("paint", ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "makes_paint_gel"));

    private final String id;
    private final ResourceLocation tagLocation;

    GelType(String id, ResourceLocation tagLocation) {
        this.id = id;
        this.tagLocation = tagLocation;
    }

    public String getId() {
        return id;
    }

    public ResourceLocation getTagLocation() {
        return tagLocation;
    }
}
