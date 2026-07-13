package com.resourceful_refinement.client.research;

import com.resourceful_refinement.network.ResearchTreeSyncPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ResearchTreeLayout {
    public static final int NODE_SIZE = 42;
    private static final int COLUMN_SPACING = 132;
    private static final int ROW_SPACING = 82;

    private final Map<ResourceLocation, PositionedNode> nodes;
    private final Map<ResourceLocation, ResourceLocation> parentAliases;
    private final Bounds bounds;

    private ResearchTreeLayout(Map<ResourceLocation, PositionedNode> nodes,
                               Map<ResourceLocation, ResourceLocation> parentAliases,
                               Bounds bounds) {
        this.nodes = Map.copyOf(nodes);
        this.parentAliases = Map.copyOf(parentAliases);
        this.bounds = bounds;
    }

    public static ResearchTreeLayout build(List<ResearchTreeSyncPayload.NodeEntry> entries) {
        Map<ResourceLocation, ResearchTreeSyncPayload.NodeEntry> byId = new LinkedHashMap<>();
        entries.stream()
                .sorted(Comparator.comparing(node -> node.id().toString()))
                .forEach(node -> byId.put(node.id(), node));
        Map<ResourceLocation, ResourceLocation> parentAliases = buildParentAliases(byId);

        Map<ResourceLocation, Integer> depths = new HashMap<>();
        for (ResourceLocation id : byId.keySet()) {
            resolveDepth(id, byId, parentAliases, depths, new HashSet<>());
        }

        Map<ResourceLocation, ResourceLocation> primaryParents = new HashMap<>();
        Map<ResourceLocation, List<ResourceLocation>> primaryChildren = new HashMap<>();
        byId.keySet().forEach(id -> primaryChildren.put(id, new ArrayList<>()));
        for (ResearchTreeSyncPayload.NodeEntry node : byId.values()) {
            ResourceLocation parent = choosePrimaryParent(node, byId, parentAliases, depths);
            if (parent != null) {
                primaryParents.put(node.id(), parent);
                primaryChildren.get(parent).add(node.id());
            }
        }
        primaryChildren.values().forEach(children -> children.sort(Comparator.comparing(ResourceLocation::toString)));

        List<ResourceLocation> roots = byId.keySet().stream()
                .filter(id -> !primaryParents.containsKey(id))
                .sorted(Comparator.comparing(ResourceLocation::toString))
                .toList();

        Map<ResourceLocation, Double> yPositions = new HashMap<>();
        RowCursor cursor = new RowCursor();
        Set<ResourceLocation> visited = new LinkedHashSet<>();
        for (ResourceLocation root : roots) {
            assignY(root, primaryChildren, yPositions, cursor, visited);
        }
        for (ResourceLocation id : byId.keySet()) {
            if (!visited.contains(id)) {
                assignY(id, primaryChildren, yPositions, cursor, visited);
            }
        }

        Map<ResourceLocation, PositionedNode> positioned = new LinkedHashMap<>();
        int minX = 0;
        int minY = 0;
        int maxX = NODE_SIZE;
        int maxY = NODE_SIZE;
        for (ResearchTreeSyncPayload.NodeEntry node : byId.values()) {
            int x = depths.getOrDefault(node.id(), 0) * COLUMN_SPACING;
            int y = (int) Math.round(yPositions.getOrDefault(node.id(), 0.0) * ROW_SPACING);
            positioned.put(node.id(), new PositionedNode(node, x, y));
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x + NODE_SIZE);
            maxY = Math.max(maxY, y + NODE_SIZE);
        }

        return new ResearchTreeLayout(positioned, parentAliases, new Bounds(minX, minY, maxX, maxY));
    }

    private static int resolveDepth(ResourceLocation id, Map<ResourceLocation, ResearchTreeSyncPayload.NodeEntry> byId,
                                    Map<ResourceLocation, ResourceLocation> parentAliases,
                                    Map<ResourceLocation, Integer> depths, Set<ResourceLocation> visiting) {
        Integer known = depths.get(id);
        if (known != null) return known;
        if (!visiting.add(id)) return 0;
        ResearchTreeSyncPayload.NodeEntry node = byId.get(id);
        int depth = 0;
        if (node != null) {
            for (ResourceLocation parent : node.parents()) {
                ResourceLocation layoutParent = resolveParentId(parent, byId, parentAliases);
                if (layoutParent != null) {
                    depth = Math.max(depth, resolveDepth(layoutParent, byId, parentAliases, depths, visiting) + 1);
                }
            }
        }
        visiting.remove(id);
        depths.put(id, depth);
        return depth;
    }

    private static ResourceLocation choosePrimaryParent(ResearchTreeSyncPayload.NodeEntry node,
                                                        Map<ResourceLocation, ResearchTreeSyncPayload.NodeEntry> byId,
                                                        Map<ResourceLocation, ResourceLocation> parentAliases,
                                                        Map<ResourceLocation, Integer> depths) {
        return node.parents().stream()
                .map(parent -> resolveParentId(parent, byId, parentAliases))
                .filter(parent -> parent != null)
                .max(Comparator.comparingInt(parent -> depths.getOrDefault(parent, 0)))
                .orElse(null);
    }

    private static Map<ResourceLocation, ResourceLocation> buildParentAliases(
            Map<ResourceLocation, ResearchTreeSyncPayload.NodeEntry> byId) {
        Map<String, ResourceLocation> uniqueLeafIds = new HashMap<>();
        Set<String> duplicateLeaves = new HashSet<>();
        for (ResourceLocation id : byId.keySet()) {
            String leaf = leafPath(id);
            if (uniqueLeafIds.putIfAbsent(leaf, id) != null) {
                duplicateLeaves.add(leaf);
            }
        }
        duplicateLeaves.forEach(uniqueLeafIds::remove);

        Map<ResourceLocation, ResourceLocation> aliases = new HashMap<>();
        for (ResearchTreeSyncPayload.NodeEntry node : byId.values()) {
            for (ResourceLocation parent : node.parents()) {
                if (!byId.containsKey(parent)) {
                    ResourceLocation alias = uniqueLeafIds.get(leafPath(parent));
                    if (alias != null && !alias.equals(node.id())) {
                        aliases.put(parent, alias);
                    }
                }
            }
        }
        return aliases;
    }

    private static ResourceLocation resolveParentId(ResourceLocation parentId,
                                                    Map<ResourceLocation, ResearchTreeSyncPayload.NodeEntry> byId,
                                                    Map<ResourceLocation, ResourceLocation> parentAliases) {
        if (byId.containsKey(parentId)) return parentId;
        return parentAliases.get(parentId);
    }

    private static String leafPath(ResourceLocation id) {
        String path = id.getPath();
        int slash = path.lastIndexOf('/');
        return slash >= 0 ? path.substring(slash + 1) : path;
    }

    private static double assignY(ResourceLocation id, Map<ResourceLocation, List<ResourceLocation>> children,
                                  Map<ResourceLocation, Double> yPositions, RowCursor cursor,
                                  Set<ResourceLocation> visited) {
        if (!visited.add(id)) {
            return yPositions.getOrDefault(id, 0.0);
        }
        List<ResourceLocation> childIds = children.getOrDefault(id, List.of());
        if (childIds.isEmpty()) {
            double y = cursor.next++;
            yPositions.put(id, y);
            return y;
        }

        double first = -1;
        double last = -1;
        for (ResourceLocation child : childIds) {
            double childY = assignY(child, children, yPositions, cursor, visited);
            if (first < 0) first = childY;
            last = childY;
        }
        double centered = (first + last) / 2.0;
        yPositions.put(id, centered);
        return centered;
    }

    public List<PositionedNode> nodes() {
        return nodes.values().stream()
                .sorted(Comparator.comparing(positioned -> positioned.entry().id().toString()))
                .toList();
    }

    public PositionedNode node(ResourceLocation nodeId) {
        return nodes.get(nodeId);
    }

    public PositionedNode parentNode(ResourceLocation parentId) {
        ResourceLocation resolved = nodes.containsKey(parentId) ? parentId : parentAliases.get(parentId);
        return resolved == null ? null : nodes.get(resolved);
    }

    public Bounds bounds() {
        return bounds;
    }

    private static final class RowCursor {
        private int next;
    }

    public record PositionedNode(ResearchTreeSyncPayload.NodeEntry entry, int x, int y) {}

    public record Bounds(int minX, int minY, int maxX, int maxY) {
        public int width() {
            return maxX - minX;
        }

        public int height() {
            return maxY - minY;
        }
    }
}
