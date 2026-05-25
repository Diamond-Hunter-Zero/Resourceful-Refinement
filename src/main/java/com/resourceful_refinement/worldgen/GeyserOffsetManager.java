package com.resourceful_refinement.worldgen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.resourceful_refinement.ResourcefulRefinementMain;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class GeyserOffsetManager {
    private static final Map<ResourceLocation, Map<ResourceLocation, Integer>> OFFSETS = new HashMap<>();
    private static final ThreadLocal<ResourceLocation> ACTIVE_START_POOL = new ThreadLocal<>();

    public static void setActiveStartPool(ResourceLocation poolId) {
        ACTIVE_START_POOL.set(poolId);
    }

    public static void clearActiveStartPool() {
        ACTIVE_START_POOL.remove();
    }

    public static ResourceLocation getActiveStartPool() {
        return ACTIVE_START_POOL.get();
    }

    public static void init() {
        String[] poolPaths = {
                "cave_ore_geyser_template/start_pool.json",
                "nether_scorchia_geyser_template/start_pool.json",
                "ore_geyser_template/start_pool.json"
        };

        for (String relativePath : poolPaths) {
            String fullPath = "data/resourceful_refinement/worldgen/template_pool/" + relativePath;
            try (InputStream stream = GeyserOffsetManager.class.getClassLoader().getResourceAsStream(fullPath)) {
                if (stream == null) {
                    ResourcefulRefinementMain.LOGGER.error("[GeyserOffsetManager] Could not find pool file: " + fullPath);
                    continue;
                }

                JsonObject json = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();
                if (json.has("name") && json.has("elements")) {
                    ResourceLocation poolId = ResourceLocation.parse(json.get("name").getAsString());
                    Map<ResourceLocation, Integer> elementOffsets = new HashMap<>();

                    JsonArray elements = json.getAsJsonArray("elements");
                    for (JsonElement elElement : elements) {
                        JsonObject elObj = elElement.getAsJsonObject();
                        if (elObj.has("element")) {
                            JsonObject elementNode = elObj.getAsJsonObject("element");
                            if (elementNode.has("location") && elementNode.has("bottom_offset")) {
                                ResourceLocation templateId = ResourceLocation.parse(elementNode.get("location").getAsString());
                                int bottomOffset = elementNode.get("bottom_offset").getAsInt();
                                // Store ground_level_delta as absolute value of bottomOffset (i.e. -bottomOffset)
                                elementOffsets.put(templateId, -bottomOffset);
                            }
                        }
                    }

                    OFFSETS.put(poolId, elementOffsets);
                    ResourcefulRefinementMain.LOGGER.info("[GeyserOffsetManager] Successfully loaded geyser offsets for pool: " + poolId + " (count: " + elementOffsets.size() + ")");
                }
            } catch (Exception e) {
                ResourcefulRefinementMain.LOGGER.error("[GeyserOffsetManager] Error parsing pool file: " + fullPath, e);
            }
        }
    }

    public static Integer getOffset(ResourceLocation poolId, ResourceLocation templateId) {
        Map<ResourceLocation, Integer> poolOffsets = OFFSETS.get(poolId);
        if (poolOffsets != null) {
            return poolOffsets.get(templateId);
        }
        return null;
    }
}
