package com.resourceful_refinement.content.resonator.scan;

import com.resourceful_refinement.ResourcefulRefinementMain;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

/**
 * Maps a Crystal Fissure structure to its Resonator pin variant (one icon per dimension/source type).
 * Unlike {@code GeyserVariants}, no seed RNG is replayed — each fissure is its own structure, so the
 * variant is a fixed structure-id → name lookup.
 */
public final class CrystalFissureVariants {

    private static final Map<ResourceLocation, String> BY_STRUCTURE = Map.of(
            rr("crystal_fissure_end"), "chorus_crystal",
            rr("crystal_fissure_nether"), "quartz",
            rr("crystal_fissure_overworld"), "terran_crystal");

    private CrystalFissureVariants() {}

    /** The pin variant for a fissure structure id, or {@code null} to fall back to the generic icon. */
    public static String forStructure(ResourceLocation structureId) {
        return structureId == null ? null : BY_STRUCTURE.get(structureId);
    }

    private static ResourceLocation rr(String path) {
        return ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, path);
    }
}
