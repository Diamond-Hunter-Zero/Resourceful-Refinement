package com.resourceful_refinement.api.research;

import com.resourceful_refinement.config.ServerConfig;
import com.resourceful_refinement.content.research.ResearchDefinitionManager;
import com.resourceful_refinement.content.research.ResearchSavedData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
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

    public static Collection<ResourceLocation> getKnownLockedRecipeIds(MinecraftServer server) {
        Objects.requireNonNull(server, "server");
        LinkedHashSet<ResourceLocation> recipeIds = new LinkedHashSet<>(ResearchDefinitionManager.snapshot().recipeToNode().keySet());
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
        }
        grantRewards(player, changed);
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
        }
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
        }
        return changed.isEmpty()
                ? UnlockResult.unchanged(UnlockResult.Action.REVOKE, UnlockResult.Scope.GLOBAL, nodeId,
                Optional.empty())
                : UnlockResult.changed(UnlockResult.Action.REVOKE, UnlockResult.Scope.GLOBAL, nodeId, changed,
                Optional.empty());
    }

    /**
     * Resolves the research node currently locking a recipe, preferring manual server overrides.
     */
    public static Optional<ResourceLocation> getLockingNode(MinecraftServer server, ResourceLocation recipeId) {
        Objects.requireNonNull(server, "server");
        Objects.requireNonNull(recipeId, "recipeId");
        if (!isEnabled()) return Optional.empty();
        Optional<ResourceLocation> manualLock = ResearchSavedData.get(server).getManualRecipeLock(recipeId);
        return manualLock.isPresent() ? manualLock : ResearchDefinitionManager.getLockingNode(recipeId);
    }

    /**
     * Resolves static datapack recipe locks only. Prefer the server overload when checking gameplay.
     */
    public static Optional<ResourceLocation> getLockingNode(ResourceLocation recipeId) {
        Objects.requireNonNull(recipeId, "recipeId");
        if (!isEnabled()) return Optional.empty();
        return ResearchDefinitionManager.getLockingNode(recipeId);
    }

    /**
     * Machine-level recipe checks are server/global: a locked recipe is usable if any server unlock satisfies it.
     */
    public static boolean canUseRecipeOnServer(MinecraftServer server, ResourceLocation recipeId) {
        Objects.requireNonNull(server, "server");
        Objects.requireNonNull(recipeId, "recipeId");
        if (!isEnabled()) return true;
        Optional<ResourceLocation> nodeId = getLockingNode(server, recipeId);
        return nodeId.isEmpty() || hasAnyServerUnlock(server, nodeId.get());
    }

    /**
     * Player crafting checks are per-player, while still honoring explicit/global unlocks.
     */
    public static boolean canPlayerCraft(ServerPlayer player, ResourceLocation recipeId) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(recipeId, "recipeId");
        if (!isEnabled()) return true;
        Optional<ResourceLocation> nodeId = getLockingNode(player.server, recipeId);
        return nodeId.isEmpty() || hasUnlocked(player, nodeId.get());
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
}
