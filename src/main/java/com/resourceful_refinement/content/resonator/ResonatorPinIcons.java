package com.resourceful_refinement.content.resonator;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.resonator.scan.ResonatorTarget;
import com.resourceful_refinement.content.resonator.scan.ScannedPoi;
import net.minecraft.resources.ResourceLocation;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Client-side registry of radar-pin icon textures for the Resource Resonator.
 *
 * <p>Two tiers of icon:</p>
 * <ul>
 *   <li><b>Generic</b> — one per {@link ResonatorTarget} (the four generation features). Used for the filter-row
 *       icon and as the fallback pin icon when a scanned feature's specific variant has no bespoke texture.</li>
 *   <li><b>Variant</b> — an optional bespoke texture per (target, predicted template variant), e.g. a crimsite
 *       surface geyser vs a crimsite cave geyser. Keyed on both target and variant because the same template id
 *       (e.g. {@code crimsite_geyser_small}) appears in both the surface and cave pools but wants a different icon.</li>
 * </ul>
 *
 * <p>All textures live under {@code assets/resourceful_refinement/textures/gui/radar_pins/} and are 16×16.</p>
 */
public final class ResonatorPinIcons {

    private static final String BASE = "textures/gui/radar_pins/";

    private static final Map<ResonatorTarget, ResourceLocation> GENERIC = new EnumMap<>(ResonatorTarget.class);
    private static final Map<String, ResourceLocation> VARIANT = new HashMap<>();

    private ResonatorPinIcons() {}

    /** The generic icon for a feature category (filter icon + fallback pin icon). */
    public static ResourceLocation generic(ResonatorTarget target) {
        return GENERIC.get(target);
    }

    /** The icon for a scanned pin: its registered (target, variant) texture if any, else the category generic. */
    public static ResourceLocation iconFor(ScannedPoi poi) {
        if (poi.hasVariant()) {
            ResourceLocation tex = VARIANT.get(key(poi.target(), poi.variant()));
            if (tex != null) {
                return tex;
            }
        }
        return GENERIC.get(poi.target());
    }

    private static String key(ResonatorTarget target, String variant) {
        return target.name() + '/' + variant;
    }

    private static ResourceLocation tex(String name) {
        return ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, BASE + name + ".png");
    }

    private static void generic(ResonatorTarget target, String texName) {
        GENERIC.put(target, tex(texName));
    }

    private static void variant(ResonatorTarget target, String variantId, String texName) {
        VARIANT.put(key(target, variantId), tex(texName));
    }

    static {
        // --- Generic (one per generation feature) ---
        generic(ResonatorTarget.SURFACE_GEYSER, "geyser");
        generic(ResonatorTarget.CAVE_GEYSER, "cave_geyser");
        generic(ResonatorTarget.MINERAL_DEPOSIT, "mineral_deposit");
        generic(ResonatorTarget.CRYSTAL_FISSURE, "crystal_fissure");

        // --- Surface geyser sub-types (ore_geyser + carbonox_geyser pools) ---
        // "_small" variants share their material's icon; carbonox maps to the carborax texture.
        variant(ResonatorTarget.SURFACE_GEYSER, "lava_geyser_small", "lava_geyser");
        variant(ResonatorTarget.SURFACE_GEYSER, "crimsite_geyser", "crimsite_geyser");
        variant(ResonatorTarget.SURFACE_GEYSER, "crimsite_geyser_small", "crimsite_geyser");
        variant(ResonatorTarget.SURFACE_GEYSER, "veridium_geyser", "veridium_geyser");
        variant(ResonatorTarget.SURFACE_GEYSER, "veridium_geyser_small", "veridium_geyser");
        variant(ResonatorTarget.SURFACE_GEYSER, "ochrum_geyser", "ochrum_geyser");
        variant(ResonatorTarget.SURFACE_GEYSER, "ochrum_geyser_small", "ochrum_geyser");
        variant(ResonatorTarget.SURFACE_GEYSER, "asurine_geyser", "asurine_geyser");
        variant(ResonatorTarget.SURFACE_GEYSER, "asurine_geyser_small", "asurine_geyser");
        variant(ResonatorTarget.SURFACE_GEYSER, "carbonox_geyser_small", "carborax_geyser");

        // --- Cave geyser sub-types (cave_ore_geyser pool) ---
        variant(ResonatorTarget.CAVE_GEYSER, "lava_geyser_small", "lava_cave_geyser");
        variant(ResonatorTarget.CAVE_GEYSER, "crimsite_geyser_small", "crimsite_cave_geyser");
        variant(ResonatorTarget.CAVE_GEYSER, "veridium_geyser_small", "veridium_cave_geyser");
        variant(ResonatorTarget.CAVE_GEYSER, "ochrum_geyser_small", "ochrum_cave_geyser");
        variant(ResonatorTarget.CAVE_GEYSER, "asurine_geyser_small", "asurine_cave_geyser");
        variant(ResonatorTarget.CAVE_GEYSER, "carbonox_geyser_small", "carborax_cave_geyser");

        // --- Nether geyser sub-types (classified as cave geysers). Scorchia variants share the scorchia icon;
        // the nether lava geyser uses the cave lava icon. ---
        variant(ResonatorTarget.CAVE_GEYSER, "nether_lava_geyser_small", "lava_cave_geyser");
        variant(ResonatorTarget.CAVE_GEYSER, "scorchia_geyser", "scorchia_geyser");
        variant(ResonatorTarget.CAVE_GEYSER, "scorchia_geyser_small", "scorchia_geyser");
        variant(ResonatorTarget.CAVE_GEYSER, "scorchia_geyser_basalt_pool", "scorchia_geyser");
        variant(ResonatorTarget.CAVE_GEYSER, "scorchia_geyser_basalt_small", "scorchia_geyser");
        variant(ResonatorTarget.CAVE_GEYSER, "scorchia_geyser_basalt_tall", "scorchia_geyser");

        // --- Future sub-types (no worldgen yet; keyed on the texture name so they light up once their
        // variants are produced by a scan). Harmless until then — no POIs carry these variants. ---
        variant(ResonatorTarget.MINERAL_DEPOSIT, "andesite_deposit", "andesite_deposit");
        variant(ResonatorTarget.MINERAL_DEPOSIT, "coal_deposit", "coal_deposit");
        variant(ResonatorTarget.MINERAL_DEPOSIT, "granite_deposit", "granite_deposit");
        variant(ResonatorTarget.MINERAL_DEPOSIT, "sand_deposit", "sand_deposit");
        variant(ResonatorTarget.MINERAL_DEPOSIT, "stone_deposit", "stone_deposit");
        variant(ResonatorTarget.MINERAL_DEPOSIT, "endstone_deposit", "endstone_deposit");
        variant(ResonatorTarget.CRYSTAL_FISSURE, "chorus_crystal", "chorus_crystal");
        variant(ResonatorTarget.CRYSTAL_FISSURE, "terran_crystal", "terran_crystal");
    }
}
