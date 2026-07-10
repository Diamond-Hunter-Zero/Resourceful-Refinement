package com.resourceful_refinement.content.research;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Overworld-backed persistent authority for research unlocks and manual recipe locks. */
public final class ResearchSavedData extends SavedData {
    private static final String DATA_NAME = "resourceful_refinement_research";
    private static final int DATA_VERSION = 1;

    private final Map<ResourceLocation, LinkedHashSet<UUID>> playerUnlocksByNode = new LinkedHashMap<>();
    private final Set<ResourceLocation> globalUnlocks = new LinkedHashSet<>();
    private final Map<ResourceLocation, ResourceLocation> manualRecipeLocks = new LinkedHashMap<>();

    public static SavedData.Factory<ResearchSavedData> factory() {
        return new SavedData.Factory<>(ResearchSavedData::new, ResearchSavedData::load);
    }

    public static ResearchSavedData get(ServerLevel level) {
        ServerLevel storageLevel = level.dimension().equals(Level.OVERWORLD) ? level : level.getServer().overworld();
        return storageLevel.getDataStorage().computeIfAbsent(factory(), DATA_NAME);
    }

    public static ResearchSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(factory(), DATA_NAME);
    }

    private static ResearchSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        ResearchSavedData data = new ResearchSavedData();
        int version = tag.getInt("Version");
        data.readPlayerUnlocks(tag.getList("PlayerUnlocks", Tag.TAG_COMPOUND));
        data.readGlobalUnlocks(tag.getList("GlobalUnlocks", Tag.TAG_STRING));
        data.readManualRecipeLocks(tag.getList("ManualRecipeLocks", Tag.TAG_COMPOUND));
        if (version > DATA_VERSION) {
            // Future versions should remain readable when their v1-compatible fields are present.
        }
        return data;
    }

    public static ResearchSavedData loadForTests(CompoundTag tag, HolderLookup.Provider registries) {
        return load(tag, registries);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("Version", DATA_VERSION);
        tag.put("PlayerUnlocks", writePlayerUnlocks());
        tag.put("GlobalUnlocks", writeResourceLocationSet(globalUnlocks));
        tag.put("ManualRecipeLocks", writeManualRecipeLocks());
        return tag;
    }

    public boolean grantPlayer(ResourceLocation nodeId, UUID playerId) {
        LinkedHashSet<UUID> players = playerUnlocksByNode.computeIfAbsent(nodeId, ignored -> new LinkedHashSet<>());
        if (!players.add(playerId)) return false;
        setDirty();
        return true;
    }

    public boolean revokePlayer(ResourceLocation nodeId, UUID playerId) {
        LinkedHashSet<UUID> players = playerUnlocksByNode.get(nodeId);
        if (players == null || !players.remove(playerId)) return false;
        if (players.isEmpty()) playerUnlocksByNode.remove(nodeId);
        setDirty();
        return true;
    }

    public boolean hasPlayerUnlocked(ResourceLocation nodeId, UUID playerId) {
        Set<UUID> players = playerUnlocksByNode.get(nodeId);
        return players != null && players.contains(playerId);
    }

    public Set<UUID> getPlayersForNode(ResourceLocation nodeId) {
        Set<UUID> players = playerUnlocksByNode.get(nodeId);
        return players == null ? Set.of() : Set.copyOf(players);
    }

    public Map<ResourceLocation, Set<UUID>> getPlayerUnlocksByNode() {
        Map<ResourceLocation, Set<UUID>> copy = new LinkedHashMap<>();
        playerUnlocksByNode.forEach((nodeId, players) -> copy.put(nodeId, Set.copyOf(players)));
        return Map.copyOf(copy);
    }

    public boolean grantGlobal(ResourceLocation nodeId) {
        if (!globalUnlocks.add(nodeId)) return false;
        setDirty();
        return true;
    }

    public boolean revokeGlobal(ResourceLocation nodeId) {
        if (!globalUnlocks.remove(nodeId)) return false;
        setDirty();
        return true;
    }

    public boolean isGloballyUnlocked(ResourceLocation nodeId) {
        return globalUnlocks.contains(nodeId);
    }

    public Set<ResourceLocation> getGlobalUnlocks() {
        return Set.copyOf(globalUnlocks);
    }

    public boolean hasAnyUnlock(ResourceLocation nodeId) {
        return isGloballyUnlocked(nodeId) || !getPlayersForNode(nodeId).isEmpty();
    }

    public boolean setManualRecipeLock(ResourceLocation recipeId, ResourceLocation nodeId) {
        ResourceLocation previous = manualRecipeLocks.put(recipeId, nodeId);
        if (nodeId.equals(previous)) return false;
        setDirty();
        return true;
    }

    public boolean clearManualRecipeLock(ResourceLocation recipeId) {
        if (manualRecipeLocks.remove(recipeId) == null) return false;
        setDirty();
        return true;
    }

    public Optional<ResourceLocation> getManualRecipeLock(ResourceLocation recipeId) {
        return Optional.ofNullable(manualRecipeLocks.get(recipeId));
    }

    public Map<ResourceLocation, ResourceLocation> getManualRecipeLocks() {
        return Map.copyOf(manualRecipeLocks);
    }

    private ListTag writePlayerUnlocks() {
        ListTag entries = new ListTag();
        playerUnlocksByNode.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(ResourceLocation::toString)))
                .forEach(entry -> {
                    CompoundTag nodeTag = new CompoundTag();
                    nodeTag.putString("Node", entry.getKey().toString());
                    ListTag players = new ListTag();
                    entry.getValue().stream()
                            .sorted(Comparator.comparing(UUID::toString))
                            .map(UUID::toString)
                            .map(StringTag::valueOf)
                            .forEach(players::add);
                    nodeTag.put("Players", players);
                    entries.add(nodeTag);
                });
        return entries;
    }

    private void readPlayerUnlocks(ListTag entries) {
        playerUnlocksByNode.clear();
        for (int index = 0; index < entries.size(); index++) {
            CompoundTag nodeTag = entries.getCompound(index);
            ResourceLocation nodeId = parseId(nodeTag.getString("Node")).orElse(null);
            if (nodeId == null) continue;
            LinkedHashSet<UUID> players = new LinkedHashSet<>();
            ListTag playerTags = nodeTag.getList("Players", Tag.TAG_STRING);
            for (int playerIndex = 0; playerIndex < playerTags.size(); playerIndex++) {
                try {
                    players.add(UUID.fromString(playerTags.getString(playerIndex)));
                } catch (IllegalArgumentException ignored) {
                }
            }
            if (!players.isEmpty()) {
                playerUnlocksByNode.put(nodeId, players);
            }
        }
    }

    private static ListTag writeResourceLocationSet(Collection<ResourceLocation> ids) {
        ListTag list = new ListTag();
        ids.stream().sorted(Comparator.comparing(ResourceLocation::toString))
                .map(ResourceLocation::toString)
                .map(StringTag::valueOf)
                .forEach(list::add);
        return list;
    }

    private void readGlobalUnlocks(ListTag entries) {
        globalUnlocks.clear();
        for (int index = 0; index < entries.size(); index++) {
            parseId(entries.getString(index)).ifPresent(globalUnlocks::add);
        }
    }

    private ListTag writeManualRecipeLocks() {
        ListTag entries = new ListTag();
        manualRecipeLocks.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(ResourceLocation::toString)))
                .forEach(entry -> {
                    CompoundTag lockTag = new CompoundTag();
                    lockTag.putString("Recipe", entry.getKey().toString());
                    lockTag.putString("Node", entry.getValue().toString());
                    entries.add(lockTag);
                });
        return entries;
    }

    private void readManualRecipeLocks(ListTag entries) {
        manualRecipeLocks.clear();
        for (int index = 0; index < entries.size(); index++) {
            CompoundTag lockTag = entries.getCompound(index);
            Optional<ResourceLocation> recipeId = parseId(lockTag.getString("Recipe"));
            Optional<ResourceLocation> nodeId = parseId(lockTag.getString("Node"));
            if (recipeId.isPresent() && nodeId.isPresent()) {
                manualRecipeLocks.put(recipeId.get(), nodeId.get());
            }
        }
    }

    private static Optional<ResourceLocation> parseId(String value) {
        try {
            return Optional.of(ResourceLocation.parse(value));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }
}
