package com.resourceful_refinement.content.research;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.api.research.ResearchNodeDefinition;
import com.resourceful_refinement.api.research.ResearchTreeValidationResult;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

import java.io.Reader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/** Reloads and validates datapack-authored research node definitions. */
@EventBusSubscriber(modid = ResourcefulRefinementMain.MOD_ID)
public final class ResearchDefinitionManager {
    public static final String DIRECTORY = "research_nodes";

    private static volatile Snapshot snapshot = Snapshot.empty();

    private ResearchDefinitionManager() {}

    @SubscribeEvent
    public static void addReloadListener(AddReloadListenerEvent event) {
        event.addListener(new Listener());
    }

    public static Snapshot snapshot() {
        return snapshot;
    }

    public static Optional<ResearchNodeDefinition> getNode(ResourceLocation nodeId) {
        return snapshot.getNode(nodeId);
    }

    public static Optional<ResourceLocation> getLockingNode(ResourceLocation recipeId) {
        return snapshot.getLockingNode(recipeId);
    }

    private static void apply(Map<ResourceLocation, ResearchNodeDefinition> loadedNodes) {
        Snapshot next = buildSnapshot(loadedNodes);
        snapshot = next;
        int errorCount = next.validation().errors().size();
        int warningCount = next.validation().warnings().size();
        if (errorCount > 0 || warningCount > 0) {
            ResourcefulRefinementMain.LOGGER.warn(
                    "[Research] Loaded {} valid research nodes with {} validation errors and {} warnings",
                    next.nodes().size(), errorCount, warningCount);
            for (ResearchTreeValidationResult.Issue issue : next.validation().issues()) {
                ResourcefulRefinementMain.LOGGER.warn("[Research] {}{}: {}", issue.severity(),
                        issue.nodeId().map(id -> " " + id).orElse(""), issue.message());
            }
        } else {
            ResourcefulRefinementMain.LOGGER.info("[Research] Loaded {} research nodes", next.nodes().size());
        }
    }

    private static Snapshot buildSnapshot(Map<ResourceLocation, ResearchNodeDefinition> loadedNodes) {
        Map<ResourceLocation, ResearchNodeDefinition> sorted = new LinkedHashMap<>();
        loadedNodes.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(ResourceLocation::toString)))
                .forEach(entry -> sorted.put(entry.getKey(), entry.getValue()));

        List<ResearchTreeValidationResult.Issue> issues = new ArrayList<>();
        Set<ResourceLocation> invalidNodes = new LinkedHashSet<>();

        validateParents(sorted, issues, invalidNodes);
        validateCycles(sorted, issues, invalidNodes);
        propagateInvalidDependencies(sorted, issues, invalidNodes);

        Map<ResourceLocation, ResourceLocation> recipeToNode = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, ResearchNodeDefinition> entry : sorted.entrySet()) {
            ResourceLocation nodeId = entry.getKey();
            if (invalidNodes.contains(nodeId)) continue;
            for (ResourceLocation recipeId : entry.getValue().recipes()) {
                ResourceLocation previous = recipeToNode.putIfAbsent(recipeId, nodeId);
                if (previous != null) {
                    invalidNodes.add(nodeId);
                    issues.add(ResearchTreeValidationResult.Issue.error(nodeId,
                            "Recipe " + recipeId + " is already locked by " + previous));
                    break;
                }
            }
        }

        Map<ResourceLocation, ResearchNodeDefinition> validNodes = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, ResearchNodeDefinition> entry : sorted.entrySet()) {
            if (!invalidNodes.contains(entry.getKey())) {
                validNodes.put(entry.getKey(), entry.getValue());
            }
        }

        recipeToNode.clear();
        Map<ResourceLocation, List<ResourceLocation>> parentsByNode = new LinkedHashMap<>();
        Map<ResourceLocation, List<ResourceLocation>> childrenByNode = new LinkedHashMap<>();
        for (ResourceLocation nodeId : validNodes.keySet()) {
            childrenByNode.put(nodeId, List.of());
        }
        Map<ResourceLocation, LinkedHashSet<ResourceLocation>> childrenBuilder = new LinkedHashMap<>();
        for (ResourceLocation nodeId : validNodes.keySet()) {
            childrenBuilder.put(nodeId, new LinkedHashSet<>());
        }
        for (Map.Entry<ResourceLocation, ResearchNodeDefinition> entry : validNodes.entrySet()) {
            ResourceLocation nodeId = entry.getKey();
            List<ResourceLocation> validParents = entry.getValue().parents().stream()
                    .filter(validNodes::containsKey)
                    .distinct()
                    .toList();
            parentsByNode.put(nodeId, validParents);
            validParents.forEach(parent -> childrenBuilder.get(parent).add(nodeId));
            entry.getValue().recipes().forEach(recipeId -> recipeToNode.put(recipeId, nodeId));
        }
        childrenBuilder.forEach((node, children) -> childrenByNode.put(node, List.copyOf(children)));

        return new Snapshot(validNodes, parentsByNode, childrenByNode, recipeToNode,
                ResearchTreeValidationResult.of(issues));
    }

    private static void validateParents(Map<ResourceLocation, ResearchNodeDefinition> nodes,
                                        List<ResearchTreeValidationResult.Issue> issues,
                                        Set<ResourceLocation> invalidNodes) {
        for (Map.Entry<ResourceLocation, ResearchNodeDefinition> entry : nodes.entrySet()) {
            ResourceLocation nodeId = entry.getKey();
            for (ResourceLocation parentId : entry.getValue().parents()) {
                if (!nodes.containsKey(parentId)) {
                    invalidNodes.add(nodeId);
                    issues.add(ResearchTreeValidationResult.Issue.error(nodeId, "Missing parent node " + parentId));
                }
                if (nodeId.equals(parentId)) {
                    invalidNodes.add(nodeId);
                    issues.add(ResearchTreeValidationResult.Issue.error(nodeId, "Node cannot depend on itself"));
                }
            }
        }
    }

    private static void validateCycles(Map<ResourceLocation, ResearchNodeDefinition> nodes,
                                       List<ResearchTreeValidationResult.Issue> issues,
                                       Set<ResourceLocation> invalidNodes) {
        Set<ResourceLocation> visited = new HashSet<>();
        Set<ResourceLocation> visiting = new HashSet<>();
        ArrayDeque<ResourceLocation> stack = new ArrayDeque<>();
        for (ResourceLocation nodeId : nodes.keySet()) {
            detectCycle(nodeId, nodes, visited, visiting, stack, issues, invalidNodes);
        }
    }

    private static void propagateInvalidDependencies(Map<ResourceLocation, ResearchNodeDefinition> nodes,
                                                     List<ResearchTreeValidationResult.Issue> issues,
                                                     Set<ResourceLocation> invalidNodes) {
        boolean changed;
        do {
            changed = false;
            for (Map.Entry<ResourceLocation, ResearchNodeDefinition> entry : nodes.entrySet()) {
                ResourceLocation nodeId = entry.getKey();
                if (invalidNodes.contains(nodeId)) continue;
                for (ResourceLocation parentId : entry.getValue().parents()) {
                    if (invalidNodes.contains(parentId)) {
                        invalidNodes.add(nodeId);
                        issues.add(ResearchTreeValidationResult.Issue.error(nodeId,
                                "Parent node " + parentId + " is invalid"));
                        changed = true;
                        break;
                    }
                }
            }
        } while (changed);
    }

    private static void detectCycle(ResourceLocation nodeId, Map<ResourceLocation, ResearchNodeDefinition> nodes,
                                    Set<ResourceLocation> visited, Set<ResourceLocation> visiting,
                                    ArrayDeque<ResourceLocation> stack,
                                    List<ResearchTreeValidationResult.Issue> issues,
                                    Set<ResourceLocation> invalidNodes) {
        if (visited.contains(nodeId) || !nodes.containsKey(nodeId)) return;
        if (!visiting.add(nodeId)) {
            invalidNodes.add(nodeId);
            issues.add(ResearchTreeValidationResult.Issue.error(nodeId, "Research dependency cycle detected"));
            return;
        }
        stack.push(nodeId);
        for (ResourceLocation parentId : nodes.get(nodeId).parents()) {
            if (!nodes.containsKey(parentId)) continue;
            if (visiting.contains(parentId)) {
                invalidNodes.add(parentId);
                invalidNodes.add(nodeId);
                issues.add(ResearchTreeValidationResult.Issue.error(nodeId,
                        "Research dependency cycle includes " + parentId));
            } else {
                detectCycle(parentId, nodes, visited, visiting, stack, issues, invalidNodes);
            }
        }
        stack.pop();
        visiting.remove(nodeId);
        visited.add(nodeId);
    }

    public record Snapshot(Map<ResourceLocation, ResearchNodeDefinition> nodes,
                           Map<ResourceLocation, List<ResourceLocation>> parentsByNode,
                           Map<ResourceLocation, List<ResourceLocation>> childrenByNode,
                           Map<ResourceLocation, ResourceLocation> recipeToNode,
                           ResearchTreeValidationResult validation) {
        public Snapshot {
            nodes = Map.copyOf(nodes);
            parentsByNode = copyListMap(parentsByNode);
            childrenByNode = copyListMap(childrenByNode);
            recipeToNode = Map.copyOf(recipeToNode);
            validation = validation == null ? ResearchTreeValidationResult.ok() : validation;
        }

        public static Snapshot empty() {
            return new Snapshot(Map.of(), Map.of(), Map.of(), Map.of(), ResearchTreeValidationResult.ok());
        }

        public Optional<ResearchNodeDefinition> getNode(ResourceLocation nodeId) {
            return Optional.ofNullable(nodes.get(nodeId));
        }

        public Optional<ResourceLocation> getLockingNode(ResourceLocation recipeId) {
            return Optional.ofNullable(recipeToNode.get(recipeId));
        }

        public List<ResourceLocation> parentsOf(ResourceLocation nodeId) {
            return parentsByNode.getOrDefault(nodeId, List.of());
        }

        public List<ResourceLocation> childrenOf(ResourceLocation nodeId) {
            return childrenByNode.getOrDefault(nodeId, List.of());
        }
    }

    private static Map<ResourceLocation, List<ResourceLocation>> copyListMap(
            Map<ResourceLocation, List<ResourceLocation>> source) {
        Map<ResourceLocation, List<ResourceLocation>> copy = new LinkedHashMap<>();
        source.forEach((key, value) -> copy.put(key, List.copyOf(value)));
        return Map.copyOf(copy);
    }

    private static final class Listener implements PreparableReloadListener {
        @Override
        public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager resourceManager,
                                              ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler,
                                              Executor backgroundExecutor, Executor gameExecutor) {
            return CompletableFuture.supplyAsync(() -> loadDefinitions(resourceManager), backgroundExecutor)
                    .thenCompose(barrier::wait)
                    .thenAcceptAsync(ResearchDefinitionManager::apply, gameExecutor);
        }

        private static Map<ResourceLocation, ResearchNodeDefinition> loadDefinitions(ResourceManager resourceManager) {
            Map<ResourceLocation, ResearchNodeDefinition> loadedNodes = new HashMap<>();
            Map<ResourceLocation, Resource> resources = resourceManager.listResources(DIRECTORY,
                    id -> id.getPath().endsWith(".json"));
            for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
                ResourceLocation resourceId = entry.getKey();
                ResourceLocation nodeId = nodeIdFromResource(resourceId);
                try (Reader reader = entry.getValue().openAsReader()) {
                    JsonElement json = JsonParser.parseReader(reader);
                    ResearchNodeDefinition definition = ResearchNodeDefinition.CODEC.parse(JsonOps.INSTANCE, json)
                            .getOrThrow(message -> new IllegalArgumentException("Invalid research node " + nodeId
                                    + ": " + message));
                    loadedNodes.put(nodeId, definition);
                } catch (Exception exception) {
                    ResourcefulRefinementMain.LOGGER.error("[Research] Failed to load research node {} from {}",
                            nodeId, resourceId, exception);
                }
            }
            return loadedNodes;
        }

        private static ResourceLocation nodeIdFromResource(ResourceLocation resourceId) {
            String path = resourceId.getPath();
            String prefix = DIRECTORY + "/";
            if (path.startsWith(prefix)) {
                path = path.substring(prefix.length());
            }
            if (path.endsWith(".json")) {
                path = path.substring(0, path.length() - ".json".length());
            }
            return ResourceLocation.fromNamespaceAndPath(resourceId.getNamespace(), path);
        }
    }
}
