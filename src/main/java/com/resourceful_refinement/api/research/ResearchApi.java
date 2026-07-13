package com.resourceful_refinement.api.research;

import com.resourceful_refinement.config.ServerConfig;
import com.resourceful_refinement.content.research.ResearchDefinitionManager;
import com.resourceful_refinement.content.research.ResearchSavedData;
import com.resourceful_refinement.network.ResearchUnlockToastPayload;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Public server-side facade for research definitions, unlock state, and recipe locks.
 * Datapack definitions are static content; player/global progression is persisted separately in SavedData.
 */
public final class ResearchApi {
    private ResearchApi() {}

    public static boolean isEnabled() {
        return ServerConfig.RESEARCH_ENABLED.get();
    }

    public static Optional<ResearchNodeDefinition> getNode(ResourceLocation nodeId) {
        Objects.requireNonNull(nodeId, "nodeId");
        return ResearchDefinitionManager.getNode(nodeId);
    }

    public static Collection<ResearchNodeDefinition> getNodes() {
        return List.copyOf(ResearchDefinitionManager.snapshot().nodes().values());
    }

    public static Collection<ResourceLocation> getNodeIds() {
        return Set.copyOf(ResearchDefinitionManager.snapshot().nodes().keySet());
    }

    public static Optional<ResearchTreeDefinition> getTree(ResourceLocation treeId) {
        Objects.requireNonNull(treeId, "treeId");
        return ResearchDefinitionManager.snapshot().getTree(treeId);
    }

    public static Collection<ResearchTreeDefinition> getTrees() {
        return List.copyOf(ResearchDefinitionManager.snapshot().trees().values());
    }

    public static Collection<ResourceLocation> getTreeIds() {
        return Set.copyOf(ResearchDefinitionManager.snapshot().trees().keySet());
    }

    public static Collection<ResourceLocation> getKnownLockedRecipeIds(MinecraftServer server) {
        Objects.requireNonNull(server, "server");
        LinkedHashSet<ResourceLocation> recipeIds = new LinkedHashSet<>(ResearchDefinitionManager.snapshot().recipeToNodes().keySet());
        recipeIds.addAll(ResearchSavedData.get(server).getManualRecipeLocks().keySet());
        return recipeIds.stream().sorted(Comparator.comparing(ResourceLocation::toString)).toList();
    }

    /**
     * Returns whether the player can be treated as having this node unlocked.
     * Global unlocks satisfy this check for every player.
     */
    public static boolean hasUnlocked(ServerPlayer player, ResourceLocation nodeId) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(nodeId, "nodeId");
        if (!isEnabled()) return true;
        ResearchSavedData data = ResearchSavedData.get(player.server);
        return data.isGloballyUnlocked(nodeId) || data.hasPlayerUnlocked(nodeId, player.getUUID());
    }

    /**
     * Returns whether the UUID owner can be treated as having this node unlocked.
     * This is intended for server-side machines that store player ownership without requiring the owner to be online.
     */
    public static boolean hasUnlocked(MinecraftServer server, UUID playerId, ResourceLocation nodeId) {
        Objects.requireNonNull(server, "server");
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(nodeId, "nodeId");
        if (!isEnabled()) return true;
        ResearchSavedData data = ResearchSavedData.get(server);
        return data.isGloballyUnlocked(nodeId) || data.hasPlayerUnlocked(nodeId, playerId);
    }

    /**
     * Returns whether at least one player, or global progression, has unlocked the node on this server.
     */
    public static boolean hasAnyServerUnlock(MinecraftServer server, ResourceLocation nodeId) {
        Objects.requireNonNull(server, "server");
        Objects.requireNonNull(nodeId, "nodeId");
        if (!isEnabled()) return true;
        return ResearchSavedData.get(server).hasAnyUnlock(nodeId);
    }

    public static UnlockResult grant(ServerPlayer player, ResourceLocation nodeId, boolean cascade) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(nodeId, "nodeId");
        if (ServerConfig.RESEARCH_GLOBAL_UNLOCKS.get()) {
            return grantGlobal(player.server, nodeId, cascade);
        }
        if (!isEnabled()) {
            return UnlockResult.disabled(UnlockResult.Action.GRANT, UnlockResult.Scope.PLAYER, nodeId,
                    Optional.of(player.getUUID()));
        }
        ResearchDefinitionManager.Snapshot snapshot = ResearchDefinitionManager.snapshot();
        if (!snapshot.nodes().containsKey(nodeId)) {
            return UnlockResult.unknownNode(UnlockResult.Action.GRANT, UnlockResult.Scope.PLAYER, nodeId,
                    Optional.of(player.getUUID()));
        }

        ResearchSavedData data = ResearchSavedData.get(player.server);
        List<ResourceLocation> targetNodes = cascade ? collectPrerequisites(snapshot, nodeId) : List.of(nodeId);
        List<ResourceLocation> changed = new ArrayList<>();
        UUID playerId = player.getUUID();
        for (ResourceLocation targetNode : targetNodes) {
            if (data.grantPlayer(targetNode, playerId)) {
                changed.add(targetNode);
            }
            data.clearPlayerProgress(targetNode, playerId);
        }
        grantRewards(player, changed);
        sendUnlockToasts(player, changed);
        return changed.isEmpty()
                ? UnlockResult.unchanged(UnlockResult.Action.GRANT, UnlockResult.Scope.PLAYER, nodeId,
                Optional.of(playerId))
                : UnlockResult.changed(UnlockResult.Action.GRANT, UnlockResult.Scope.PLAYER, nodeId, changed,
                Optional.of(playerId));
    }

    public static UnlockResult grantGlobal(MinecraftServer server, ResourceLocation nodeId, boolean cascade) {
        Objects.requireNonNull(server, "server");
        Objects.requireNonNull(nodeId, "nodeId");
        if (!isEnabled()) {
            return UnlockResult.disabled(UnlockResult.Action.GRANT, UnlockResult.Scope.GLOBAL, nodeId, Optional.empty());
        }
        ResearchDefinitionManager.Snapshot snapshot = ResearchDefinitionManager.snapshot();
        if (!snapshot.nodes().containsKey(nodeId)) {
            return UnlockResult.unknownNode(UnlockResult.Action.GRANT, UnlockResult.Scope.GLOBAL, nodeId,
                    Optional.empty());
        }

        ResearchSavedData data = ResearchSavedData.get(server);
        List<ResourceLocation> targetNodes = cascade ? collectPrerequisites(snapshot, nodeId) : List.of(nodeId);
        List<ResourceLocation> changed = new ArrayList<>();
        for (ResourceLocation targetNode : targetNodes) {
            if (data.grantGlobal(targetNode)) {
                changed.add(targetNode);
            }
            data.clearGlobalProgress(targetNode);
        }
        sendGlobalUnlockToasts(server, changed);
        return changed.isEmpty()
                ? UnlockResult.unchanged(UnlockResult.Action.GRANT, UnlockResult.Scope.GLOBAL, nodeId, Optional.empty())
                : UnlockResult.changed(UnlockResult.Action.GRANT, UnlockResult.Scope.GLOBAL, nodeId, changed,
                Optional.empty());
    }

    public static UnlockResult revoke(ServerPlayer player, ResourceLocation nodeId, boolean cascade) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(nodeId, "nodeId");
        if (ServerConfig.RESEARCH_GLOBAL_UNLOCKS.get()) {
            return revokeGlobal(player.server, nodeId, cascade);
        }
        if (!isEnabled()) {
            return UnlockResult.disabled(UnlockResult.Action.REVOKE, UnlockResult.Scope.PLAYER, nodeId,
                    Optional.of(player.getUUID()));
        }
        ResearchDefinitionManager.Snapshot snapshot = ResearchDefinitionManager.snapshot();
        if (!snapshot.nodes().containsKey(nodeId)) {
            return UnlockResult.unknownNode(UnlockResult.Action.REVOKE, UnlockResult.Scope.PLAYER, nodeId,
                    Optional.of(player.getUUID()));
        }

        ResearchSavedData data = ResearchSavedData.get(player.server);
        List<ResourceLocation> targetNodes = cascade ? collectDependents(snapshot, nodeId) : List.of(nodeId);
        List<ResourceLocation> changed = new ArrayList<>();
        UUID playerId = player.getUUID();
        for (ResourceLocation targetNode : targetNodes) {
            if (data.revokePlayer(targetNode, playerId)) {
                changed.add(targetNode);
            }
            data.clearPlayerProgress(targetNode, playerId);
        }
        return changed.isEmpty()
                ? UnlockResult.unchanged(UnlockResult.Action.REVOKE, UnlockResult.Scope.PLAYER, nodeId,
                Optional.of(playerId))
                : UnlockResult.changed(UnlockResult.Action.REVOKE, UnlockResult.Scope.PLAYER, nodeId, changed,
                Optional.of(playerId));
    }

    public static UnlockResult revokeGlobal(MinecraftServer server, ResourceLocation nodeId, boolean cascade) {
        Objects.requireNonNull(server, "server");
        Objects.requireNonNull(nodeId, "nodeId");
        if (!isEnabled()) {
            return UnlockResult.disabled(UnlockResult.Action.REVOKE, UnlockResult.Scope.GLOBAL, nodeId,
                    Optional.empty());
        }
        ResearchDefinitionManager.Snapshot snapshot = ResearchDefinitionManager.snapshot();
        if (!snapshot.nodes().containsKey(nodeId)) {
            return UnlockResult.unknownNode(UnlockResult.Action.REVOKE, UnlockResult.Scope.GLOBAL, nodeId,
                    Optional.empty());
        }

        ResearchSavedData data = ResearchSavedData.get(server);
        List<ResourceLocation> targetNodes = cascade ? collectDependents(snapshot, nodeId) : List.of(nodeId);
        List<ResourceLocation> changed = new ArrayList<>();
        for (ResourceLocation targetNode : targetNodes) {
            if (data.revokeGlobal(targetNode)) {
                changed.add(targetNode);
            }
            data.clearGlobalProgress(targetNode);
        }
        return changed.isEmpty()
                ? UnlockResult.unchanged(UnlockResult.Action.REVOKE, UnlockResult.Scope.GLOBAL, nodeId,
                Optional.empty())
                : UnlockResult.changed(UnlockResult.Action.REVOKE, UnlockResult.Scope.GLOBAL, nodeId, changed,
                Optional.empty());
    }

    /**
     * Resolves all research nodes currently locking a recipe.
     * A recipe locked by multiple nodes requires all of them to be unlocked.
     */
    public static List<ResourceLocation> getLockingNodes(MinecraftServer server, ResourceLocation recipeId) {
        Objects.requireNonNull(server, "server");
        Objects.requireNonNull(recipeId, "recipeId");
        if (!isEnabled()) return List.of();
        LinkedHashSet<ResourceLocation> locks = new LinkedHashSet<>();
        ResearchSavedData.get(server).getManualRecipeLock(recipeId).ifPresent(locks::add);
        locks.addAll(ResearchDefinitionManager.getLockingNodes(recipeId));
        return locks.stream().filter(id -> ResearchDefinitionManager.getNode(id).isPresent()).toList();
    }

    /**
     * Resolves the first research node currently locking a recipe. Prefer {@link #getLockingNodes}.
     */
    public static Optional<ResourceLocation> getLockingNode(MinecraftServer server, ResourceLocation recipeId) {
        Objects.requireNonNull(server, "server");
        Objects.requireNonNull(recipeId, "recipeId");
        return getLockingNodes(server, recipeId).stream().findFirst();
    }

    /**
     * Resolves static datapack recipe locks only. Prefer the server overload when checking gameplay.
     */
    public static List<ResourceLocation> getLockingNodes(ResourceLocation recipeId) {
        Objects.requireNonNull(recipeId, "recipeId");
        if (!isEnabled()) return List.of();
        return ResearchDefinitionManager.getLockingNodes(recipeId);
    }

    /**
     * Resolves the first static datapack recipe lock only. Prefer {@link #getLockingNodes(ResourceLocation)}.
     */
    public static Optional<ResourceLocation> getLockingNode(ResourceLocation recipeId) {
        Objects.requireNonNull(recipeId, "recipeId");
        return getLockingNodes(recipeId).stream().findFirst();
    }

    /**
     * Machine-level recipe checks are server/global: a locked recipe is usable only if every locking node has
     * at least one server-scope unlock.
     */
    public static boolean canUseRecipeOnServer(MinecraftServer server, ResourceLocation recipeId) {
        Objects.requireNonNull(server, "server");
        Objects.requireNonNull(recipeId, "recipeId");
        if (!isEnabled()) return true;
        return getLockingNodes(server, recipeId).stream().allMatch(nodeId -> hasAnyServerUnlock(server, nodeId));
    }

    /**
     * Player crafting checks require every locking node to be unlocked for that player, while still honoring
     * explicit/global unlocks.
     */
    public static boolean canPlayerCraft(ServerPlayer player, ResourceLocation recipeId) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(recipeId, "recipeId");
        if (!isEnabled()) return true;
        return getLockingNodes(player.server, recipeId).stream().allMatch(nodeId -> hasUnlocked(player, nodeId));
    }

    public static void setManualRecipeLock(MinecraftServer server, ResourceLocation recipeId, ResourceLocation nodeId) {
        Objects.requireNonNull(server, "server");
        Objects.requireNonNull(recipeId, "recipeId");
        Objects.requireNonNull(nodeId, "nodeId");
        if (!isEnabled() || ResearchDefinitionManager.getNode(nodeId).isEmpty()) return;
        ResearchSavedData.get(server).setManualRecipeLock(recipeId, nodeId);
    }

    public static void clearManualRecipeLock(MinecraftServer server, ResourceLocation recipeId) {
        Objects.requireNonNull(server, "server");
        Objects.requireNonNull(recipeId, "recipeId");
        if (!isEnabled()) return;
        ResearchSavedData.get(server).clearManualRecipeLock(recipeId);
    }

    public static ResearchProgress getProgress(MinecraftServer server, UUID playerId, ResourceLocation nodeId) {
        Objects.requireNonNull(server, "server");
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(nodeId, "nodeId");
        Optional<ResearchNodeDefinition> definition = getNode(nodeId);
        if (definition.isEmpty()) {
            return new ResearchProgress(Map.of(), Map.of(), 0, 0);
        }
        ResearchSavedData data = ResearchSavedData.get(server);
        ResearchSavedData.RequirementProgress progress = ServerConfig.RESEARCH_GLOBAL_UNLOCKS.get()
                ? data.getGlobalProgress(nodeId)
                : data.getPlayerProgress(nodeId, playerId);
        return snapshotProgress(definition.get().requirements(), progress);
    }

    public static ResearchContributionResult contribute(MinecraftServer server, UUID playerId, ResourceLocation nodeId,
                                                        Map<ResourceLocation, Integer> offeredItems,
                                                        Map<ResourceLocation, Integer> offeredFluids) {
        Objects.requireNonNull(server, "server");
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(nodeId, "nodeId");
        Objects.requireNonNull(offeredItems, "offeredItems");
        Objects.requireNonNull(offeredFluids, "offeredFluids");

        boolean global = ServerConfig.RESEARCH_GLOBAL_UNLOCKS.get();
        Optional<ResearchNodeDefinition> definition = getNode(nodeId);
        if (!isEnabled() || definition.isEmpty()) {
            return new ResearchContributionResult(nodeId, playerId, global, false, false, Map.of(), Map.of(),
                    new ResearchProgress(Map.of(), Map.of(), 0, 0));
        }

        ResearchSavedData data = ResearchSavedData.get(server);
        if (global ? data.isGloballyUnlocked(nodeId) : data.hasPlayerUnlocked(nodeId, playerId)) {
            return new ResearchContributionResult(nodeId, playerId, global, false, true, Map.of(), Map.of(),
                    snapshotProgress(definition.get().requirements(), completedProgress(definition.get().requirements())));
        }

        ResearchRequirement requirements = definition.get().requirements();
        if (requirements.isEmpty()) {
            boolean unlocked = unlockAfterProgress(server, playerId, nodeId, global);
            return new ResearchContributionResult(nodeId, playerId, global, unlocked, unlocked, Map.of(), Map.of(),
                    snapshotProgress(requirements, ResearchSavedData.RequirementProgress.empty()));
        }

        ResearchSavedData.RequirementProgress progress = global
                ? data.getOrCreateGlobalProgress(nodeId)
                : data.getOrCreatePlayerProgress(nodeId, playerId);
        Map<ResourceLocation, Integer> acceptedItems = new LinkedHashMap<>();
        Map<ResourceLocation, Integer> acceptedFluids = new LinkedHashMap<>();

        for (ResearchRequirement.ItemRequirement requirement : requirements.items()) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(requirement.item());
            int offered = Math.max(0, offeredItems.getOrDefault(itemId, 0));
            int accepted = progress.addItem(itemId, offered, requirement.count());
            if (accepted > 0) acceptedItems.put(itemId, accepted);
        }
        for (ResearchRequirement.FluidRequirement requirement : requirements.fluids()) {
            ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(requirement.fluid());
            int offered = Math.max(0, offeredFluids.getOrDefault(fluidId, 0));
            int accepted = progress.addFluid(fluidId, offered, requirement.amount());
            if (accepted > 0) acceptedFluids.put(fluidId, accepted);
        }

        boolean changed = !acceptedItems.isEmpty() || !acceptedFluids.isEmpty();
        if (changed) data.setDirty();
        ResearchProgress progressSnapshot = snapshotProgress(requirements, progress);
        boolean unlocked = progressSnapshot.isComplete() && unlockAfterProgress(server, playerId, nodeId, global);
        return new ResearchContributionResult(nodeId, playerId, global, changed || unlocked, unlocked, acceptedItems,
                acceptedFluids, unlocked ? snapshotProgress(requirements, completedProgress(requirements)) : progressSnapshot);
    }

    public static boolean clearProgress(MinecraftServer server, UUID playerId, ResourceLocation nodeId) {
        Objects.requireNonNull(server, "server");
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(nodeId, "nodeId");
        ResearchSavedData data = ResearchSavedData.get(server);
        return ServerConfig.RESEARCH_GLOBAL_UNLOCKS.get()
                ? data.clearGlobalProgress(nodeId)
                : data.clearPlayerProgress(nodeId, playerId);
    }

    public static ResearchContributionResult fulfillProgress(MinecraftServer server, UUID playerId,
                                                             ResourceLocation nodeId) {
        Objects.requireNonNull(server, "server");
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(nodeId, "nodeId");
        boolean global = ServerConfig.RESEARCH_GLOBAL_UNLOCKS.get();
        Optional<ResearchNodeDefinition> definition = getNode(nodeId);
        if (!isEnabled() || definition.isEmpty()) {
            return new ResearchContributionResult(nodeId, playerId, global, false, false, Map.of(), Map.of(),
                    new ResearchProgress(Map.of(), Map.of(), 0, 0));
        }
        ResearchRequirement requirements = definition.get().requirements();
        ResearchSavedData data = ResearchSavedData.get(server);
        ResearchSavedData.RequirementProgress progress = completedProgress(requirements);
        if (global) {
            data.clearGlobalProgress(nodeId);
            ResearchSavedData.RequirementProgress stored = data.getOrCreateGlobalProgress(nodeId);
            copyProgress(progress, stored);
        } else {
            data.clearPlayerProgress(nodeId, playerId);
            ResearchSavedData.RequirementProgress stored = data.getOrCreatePlayerProgress(nodeId, playerId);
            copyProgress(progress, stored);
        }
        data.setDirty();
        boolean unlocked = unlockAfterProgress(server, playerId, nodeId, global);
        return new ResearchContributionResult(nodeId, playerId, global, true, unlocked, Map.of(), Map.of(),
                snapshotProgress(requirements, completedProgress(requirements)));
    }

    private static List<ResourceLocation> collectPrerequisites(ResearchDefinitionManager.Snapshot snapshot,
                                                               ResourceLocation nodeId) {
        LinkedHashSet<ResourceLocation> ordered = new LinkedHashSet<>();
        collectPrerequisitesInto(snapshot, nodeId, ordered);
        return List.copyOf(ordered);
    }

    private static void collectPrerequisitesInto(ResearchDefinitionManager.Snapshot snapshot, ResourceLocation nodeId,
                                                 LinkedHashSet<ResourceLocation> ordered) {
        for (ResourceLocation parentId : snapshot.parentsOf(nodeId)) {
            collectPrerequisitesInto(snapshot, parentId, ordered);
        }
        ordered.add(nodeId);
    }

    private static List<ResourceLocation> collectDependents(ResearchDefinitionManager.Snapshot snapshot,
                                                            ResourceLocation nodeId) {
        LinkedHashSet<ResourceLocation> ordered = new LinkedHashSet<>();
        collectDependentsInto(snapshot, nodeId, ordered);
        return List.copyOf(ordered);
    }

    private static void collectDependentsInto(ResearchDefinitionManager.Snapshot snapshot, ResourceLocation nodeId,
                                              LinkedHashSet<ResourceLocation> ordered) {
        ordered.add(nodeId);
        for (ResourceLocation childId : snapshot.childrenOf(nodeId)) {
            collectDependentsInto(snapshot, childId, ordered);
        }
    }

    private static void grantRewards(ServerPlayer player, List<ResourceLocation> changedNodes) {
        for (ResourceLocation nodeId : changedNodes) {
            ResearchDefinitionManager.getNode(nodeId).ifPresent(node -> {
                for (ResearchReward reward : node.rewards()) {
                    player.getInventory().placeItemBackInInventory(reward.toStack());
                }
            });
        }
    }

    private static boolean unlockAfterProgress(MinecraftServer server, UUID playerId, ResourceLocation nodeId,
                                               boolean global) {
        ResearchSavedData data = ResearchSavedData.get(server);
        if (global) {
            boolean changed = data.grantGlobal(nodeId);
            if (changed && server.getPlayerList().getPlayer(playerId) instanceof ServerPlayer player) {
                sendUnlockToast(player, nodeId);
            }
            return changed;
        }
        boolean changed = data.grantPlayer(nodeId, playerId);
        if (changed && server.getPlayerList().getPlayer(playerId) instanceof ServerPlayer player) {
            grantRewards(player, List.of(nodeId));
            sendUnlockToast(player, nodeId);
        }
        return changed;
    }

    private static ResearchProgress snapshotProgress(ResearchRequirement requirements,
                                                     ResearchSavedData.RequirementProgress progress) {
        Map<ResourceLocation, Integer> items = new LinkedHashMap<>();
        Map<ResourceLocation, Integer> fluids = new LinkedHashMap<>();
        int requiredUnits = 0;
        int completedUnits = 0;
        for (ResearchRequirement.ItemRequirement requirement : requirements.items()) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(requirement.item());
            int required = requirement.count();
            int current = Math.min(required, progress.getItem(itemId));
            items.put(itemId, current);
            requiredUnits += required;
            completedUnits += current;
        }
        for (ResearchRequirement.FluidRequirement requirement : requirements.fluids()) {
            ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(requirement.fluid());
            int required = requirement.amount();
            int current = Math.min(required, progress.getFluid(fluidId));
            fluids.put(fluidId, current);
            requiredUnits += required / ResearchRequirement.BUCKET_AMOUNT;
            completedUnits += current / ResearchRequirement.BUCKET_AMOUNT;
        }
        return new ResearchProgress(items, fluids, requiredUnits, completedUnits);
    }

    private static ResearchSavedData.RequirementProgress completedProgress(ResearchRequirement requirements) {
        ResearchSavedData.RequirementProgress progress = new ResearchSavedData.RequirementProgress();
        for (ResearchRequirement.ItemRequirement requirement : requirements.items()) {
            progress.addItem(BuiltInRegistries.ITEM.getKey(requirement.item()), requirement.count(), requirement.count());
        }
        for (ResearchRequirement.FluidRequirement requirement : requirements.fluids()) {
            progress.addFluid(BuiltInRegistries.FLUID.getKey(requirement.fluid()), requirement.amount(),
                    requirement.amount());
        }
        return progress;
    }

    private static void copyProgress(ResearchSavedData.RequirementProgress source,
                                     ResearchSavedData.RequirementProgress target) {
        source.items().forEach((id, amount) -> target.addItem(id, amount, amount));
        source.fluids().forEach((id, amount) -> target.addFluid(id, amount, amount));
    }

    private static void sendUnlockToasts(ServerPlayer player, List<ResourceLocation> changedNodes) {
        for (ResourceLocation nodeId : changedNodes) {
            sendUnlockToast(player, nodeId);
        }
    }

    private static void sendGlobalUnlockToasts(MinecraftServer server, List<ResourceLocation> changedNodes) {
        if (changedNodes.isEmpty()) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            sendUnlockToasts(player, changedNodes);
        }
    }

    private static void sendUnlockToast(ServerPlayer player, ResourceLocation nodeId) {
        ResearchDefinitionManager.getNode(nodeId)
                .map(ResearchUnlockToastPayload::from)
                .ifPresent(payload -> PacketDistributor.sendToPlayer(player, payload));
    }
}
