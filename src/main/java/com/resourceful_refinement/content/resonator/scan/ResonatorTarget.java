package com.resourceful_refinement.content.resonator.scan;

import com.resourceful_refinement.ResourcefulRefinementMain;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * The world-generation feature categories the Resource Resonator can scan for.
 *
 * <p>Each target maps to the {@code minecraft:random_spread} structure sets that produce it. The scan service
 * ({@link ResourceScanService}) resolves these ids against the live {@code STRUCTURE_SET} registry and predicts
 * their placement directly from the world seed, so a target with no (shipped) structure sets simply yields no
 * pins — which is the intended state for Mineral Deposit and Crystal Fissure until their worldgen exists.</p>
 *
 * <p>The ordinal order here is the wire order used by the menu/payload sync and the filter bit-set, so append
 * new targets to the end rather than reordering.</p>
 */
public enum ResonatorTarget {
    SURFACE_GEYSER("surface_geyser",
            rr("ore_geyser_set"),
            rr("carbonox_geyser_set")),
    // Nether geysers are classified as cave geysers per the Resonator design.
    CAVE_GEYSER("cave_geyser",
            rr("cave_ore_geyser_set"),
            rr("nether_scorchia_geyser_set"),
            rr("nether_scorchia_basalt_geyser_set")),
    // Worldgen not yet implemented — present in the filter UI but currently produces no pins.
    MINERAL_DEPOSIT("mineral_deposit"),
    CRYSTAL_FISSURE("crystal_fissure");

    private static final ResonatorTarget[] VALUES = values();

    private final String id;
    private final List<ResourceLocation> structureSets;

    ResonatorTarget(String id, ResourceLocation... structureSets) {
        this.id = id;
        this.structureSets = List.of(structureSets);
    }

    private static ResourceLocation rr(String path) {
        return ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, path);
    }

    public String id() {
        return id;
    }

    /** The {@code STRUCTURE_SET} ids whose placements feed this target. May be empty (feature not yet in worldgen). */
    public List<ResourceLocation> structureSets() {
        return structureSets;
    }

    /** True once this target has at least one structure set wired up (i.e. it can actually return pins). */
    public boolean hasWorldgen() {
        return !structureSets.isEmpty();
    }

    public static ResonatorTarget byOrdinal(int ordinal) {
        return ordinal >= 0 && ordinal < VALUES.length ? VALUES[ordinal] : null;
    }

    public static int count() {
        return VALUES.length;
    }
}
