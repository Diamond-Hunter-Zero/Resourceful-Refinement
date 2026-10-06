package com.resourceful_refinement.content.drill_pylon.tint;

import com.resourceful_refinement.ResourcefulRefinementMain;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry key + lookup for {@link FissureTintProfile}s.
 *
 * <p>Profiles live in the synced datapack registry {@code resourceful_refinement:fissure_bud_tints}
 * (registered in {@code DataPackRegistryEvent.NewRegistry}). This class resolves a stored source item
 * to its profile, falling back to a datapack-provided default entry ({@code "fallback": true}) and
 * finally to the hardcoded {@link #FALLBACK}. The item→profile map is cached and rebuilt whenever the
 * backing registry instance changes (datapack reload / world change).
 */
public final class FissureTintProfiles {
    public static final ResourceKey<Registry<FissureTintProfile>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "fissure_bud_tints"));

    /** Last-resort profile used when neither a per-item nor a datapack default profile is available. */
    public static final FissureTintProfile FALLBACK = new FissureTintProfile(
            List.of(0xD546FF, 0xFF5FAE, 0x6C59FF, 0x874CFF),
            4.0F,
            List.of(),
            true
    );

    private static Registry<FissureTintProfile> cachedRegistry;
    private static Map<Item, FissureTintProfile> cachedByItem = Map.of();
    private static FissureTintProfile cachedDefault = FALLBACK;

    private FissureTintProfiles() {
    }

    /** Profile for the given stored source item, or the active default if none is defined. */
    public static FissureTintProfile get(Item item, RegistryAccess access) {
        rebuildIfNeeded(access);
        FissureTintProfile profile = cachedByItem.get(item);
        return profile != null ? profile : cachedDefault;
    }

    /** The active default profile (datapack {@code fallback} entry, else {@link #FALLBACK}). */
    public static FissureTintProfile getDefault(RegistryAccess access) {
        rebuildIfNeeded(access);
        return cachedDefault;
    }

    private static void rebuildIfNeeded(RegistryAccess access) {
        Registry<FissureTintProfile> registry = access == null
                ? null
                : access.registry(REGISTRY_KEY).orElse(null);
        if (registry == cachedRegistry) {
            return;
        }
        cachedRegistry = registry;
        if (registry == null) {
            cachedByItem = Map.of();
            cachedDefault = FALLBACK;
            return;
        }

        Map<Item, FissureTintProfile> byItem = new HashMap<>();
        FissureTintProfile def = FALLBACK;
        for (FissureTintProfile profile : registry) {
            for (ResourceLocation id : profile.sourceItems()) {
                Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
                if (item != null && item != Items.AIR) {
                    byItem.put(item, profile);
                }
            }
            if (profile.fallback()) {
                def = profile;
            }
        }
        cachedByItem = Map.copyOf(byItem);
        cachedDefault = def;
    }
}
