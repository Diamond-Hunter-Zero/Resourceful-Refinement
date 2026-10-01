package com.resourceful_refinement.content.mineral_deposit;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import com.resourceful_refinement.ResourcefulRefinementMain;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Client-side registry of Mineral Deposit visual mappings.
 *
 * <p>Loaded from every resource pack's copy of
 * {@code assets/resourceful_refinement/mineral_deposit/visuals.json} on each client resource
 * reload. The file is a dictionary keyed by the expected {@code associatedBlock} id:
 *
 * <pre>{@code
 * {
 *   "minecraft:coal_block": { "base_mineral": "minecraft:stone", "deposit_material": "minecraft:coal_ore" }
 * }
 * }</pre>
 *
 * <p>Copies from higher-priority packs override lower ones key-by-key, so packs and addon mods can
 * extend the table without replacing it. Parsing is per-entry tolerant: a malformed entry or one
 * referencing an absent block is logged and skipped rather than discarding the whole file.
 */
public final class MineralDepositVisuals implements ResourceManagerReloadListener {

    public static final MineralDepositVisuals INSTANCE = new MineralDepositVisuals();

    private static final Logger LOGGER = LogUtils.getLogger();

    /** Path under {@code assets/<modid>/} holding the visual dictionary. */
    private static final ResourceLocation FILE =
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "mineral_deposit/visuals.json");

    /** Immutable snapshot swapped in wholesale on reload; keyed by the expected associatedBlock. */
    private volatile Map<Block, MineralDepositVisual> lookup = Map.of();

    private MineralDepositVisuals() {}

    /** @return the visual mapping configured for the given associated block, if any. */
    public static Optional<MineralDepositVisual> get(Block associatedBlock) {
        return Optional.ofNullable(INSTANCE.lookup.get(associatedBlock));
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        Map<Block, MineralDepositVisual> merged = new HashMap<>();

        // getResourceStack returns lowest -> highest priority, so later puts win.
        for (Resource resource : resourceManager.getResourceStack(FILE)) {
            try (Reader reader = resource.openAsReader()) {
                JsonObject root = GsonHelper.parse(reader);
                for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                    parseEntry(entry.getKey(), entry.getValue(), resource.sourcePackId(), merged);
                }
            } catch (IOException | RuntimeException e) {
                LOGGER.error("[Mineral Deposit] Failed to read visuals from pack '{}': {}",
                        resource.sourcePackId(), e.getMessage());
            }
        }

        this.lookup = Map.copyOf(merged);
        LOGGER.info("[Mineral Deposit] Loaded {} deposit visual mapping(s).", this.lookup.size());
    }

    private static void parseEntry(String key, JsonElement value, String packId, Map<Block, MineralDepositVisual> out) {
        ResourceLocation blockId = ResourceLocation.tryParse(key);
        if (blockId == null || !BuiltInRegistries.BLOCK.containsKey(blockId)) {
            LOGGER.warn("[Mineral Deposit] Skipping visual entry with unknown/invalid key '{}' (pack '{}').", key, packId);
            return;
        }
        Block associated = BuiltInRegistries.BLOCK.get(blockId);

        MineralDepositVisual.CODEC.parse(JsonOps.INSTANCE, value)
                .resultOrPartial(err -> LOGGER.warn(
                        "[Mineral Deposit] Skipping visual entry '{}' (pack '{}'): {}", key, packId, err))
                .ifPresent(visual -> out.put(associated, visual));
    }
}
