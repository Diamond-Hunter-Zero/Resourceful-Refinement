package com.resourceful_refinement.client.research;

import com.resourceful_refinement.network.ResearchTreeSyncPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ClientResearchTreeData {
    private static final Map<ResourceLocation, ResearchTreeSyncPayload.TreeEntry> TREES = new LinkedHashMap<>();
    private static final Map<ResourceLocation, ResearchTreeSyncPayload.NodeEntry> NODES = new LinkedHashMap<>();
    private static final Map<ResourceLocation, ResearchTreeLayout> LAYOUTS = new LinkedHashMap<>();
    private static ResourceLocation selectedTreeId;
    private static long version;

    private ClientResearchTreeData() {}

    public static void apply(ResearchTreeSyncPayload payload) {
        TREES.clear();
        NODES.clear();
        LAYOUTS.clear();
        payload.trees().stream()
                .sorted(Comparator.comparing(entry -> entry.id().toString()))
                .forEach(entry -> TREES.put(entry.id(), entry));
        payload.nodes().stream()
                .sorted(Comparator.comparing(entry -> entry.id().toString()))
                .forEach(entry -> NODES.put(entry.id(), entry));
        if (selectedTreeId == null || !TREES.containsKey(selectedTreeId)) {
            selectedTreeId = TREES.keySet().stream().findFirst().orElse(null);
        }
        version++;
    }

    public static long version() {
        return version;
    }

    public static boolean hasData() {
        return !TREES.isEmpty() || !NODES.isEmpty();
    }

    public static Collection<ResearchTreeSyncPayload.TreeEntry> trees() {
        return List.copyOf(TREES.values());
    }

    public static Collection<ResearchTreeSyncPayload.NodeEntry> nodes() {
        return List.copyOf(NODES.values());
    }

    public static List<ResearchTreeSyncPayload.NodeEntry> nodesForTree(ResourceLocation treeId) {
        return NODES.values().stream()
                .filter(node -> node.treeId().equals(treeId))
                .sorted(Comparator.comparing(node -> node.id().toString()))
                .toList();
    }

    public static Optional<ResearchTreeSyncPayload.TreeEntry> tree(ResourceLocation treeId) {
        return Optional.ofNullable(TREES.get(treeId));
    }

    public static Optional<ResearchTreeSyncPayload.NodeEntry> node(ResourceLocation nodeId) {
        return Optional.ofNullable(NODES.get(nodeId));
    }

    public static ResourceLocation selectedTreeId() {
        if (selectedTreeId == null || !TREES.containsKey(selectedTreeId)) {
            selectedTreeId = TREES.keySet().stream().findFirst().orElse(null);
        }
        return selectedTreeId;
    }

    public static void selectTree(ResourceLocation treeId) {
        if (TREES.containsKey(treeId)) {
            selectedTreeId = treeId;
        }
    }

    public static ResearchTreeLayout layout(ResourceLocation treeId) {
        return LAYOUTS.computeIfAbsent(treeId, id -> ResearchTreeLayout.build(nodesForTree(id)));
    }
}
