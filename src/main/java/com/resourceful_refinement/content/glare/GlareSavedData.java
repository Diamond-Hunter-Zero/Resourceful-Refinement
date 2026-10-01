package com.resourceful_refinement.content.glare;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.gui.PowerTerminalMenu;
import com.resourceful_refinement.content.glare.remote.IRemoteEntanglementEndpoint;
import com.resourceful_refinement.content.glare.remote.RemoteEndpointKind;
import com.resourceful_refinement.content.glare.remote.RemoteEntanglementMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class GlareSavedData extends SavedData {
    private static final String DATA_NAME = "resourceful_refinement_glare_networks";

    private final Map<DimensionalNodePos, NodeRecord> nodes = new HashMap<>();
    /** Spatial index of node positions by dimension → packed chunk key, so chunk-scoped work never scans every node. */
    private final Map<ResourceKey<Level>, Map<Long, Set<DimensionalNodePos>>> nodesByChunk = new HashMap<>();
    private final Set<GlareLink> links = new LinkedHashSet<>();
    private final Map<DimensionalNodePos, LinkedHashSet<GlareLink>> linksByNode = new HashMap<>();
    private final Map<ResourceKey<Level>, ArrayList<GlareLink>> linksByDimension = new HashMap<>();
    private final Map<GlareLink, LinkValidity> linkValidity = new HashMap<>();
    private final Map<GlareLink, LinkKind> linkKinds = new HashMap<>();
    private final Map<UUID, NetworkRecord> networks = new HashMap<>();
    private final Map<Long, TelemetrySubscriber> telemetrySubscribers = new HashMap<>();
    private long nextTelemetrySubscriberId;
    private final Map<ResourceKey<Level>, Integer> linkValidationCursors = new HashMap<>();

    public static SavedData.Factory<GlareSavedData> factory() {
        return new SavedData.Factory<>(GlareSavedData::new, GlareSavedData::load);
    }

    public static GlareSavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(factory(), DATA_NAME);
    }

    /*public static GlareSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(factory(), DATA_NAME);
    }*/

    private static GlareSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        GlareSavedData data = new GlareSavedData();
        data.read(tag);
        data.rebuildNetworks();
        return data;
    }

    static GlareSavedData loadForTests(CompoundTag tag) {
        GlareSavedData data = new GlareSavedData();
        data.read(tag);
        data.rebuildNetworks();
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag nodeList = new ListTag();
        for (NodeRecord node : nodes.values()) {
            nodeList.add(node.save());
        }
        tag.put("Nodes", nodeList);

        ListTag linkList = new ListTag();
        for (GlareLink link : links) {
            CompoundTag linkTag = new CompoundTag();
            DimensionalNodePos.writePos(linkTag, "A", link.a());
            DimensionalNodePos.writePos(linkTag, "B", link.b());
            linkTag.putString("Validity", getLinkValidity(link).name());
            linkTag.putString("Kind", getLinkKind(link).name());
            linkList.add(linkTag);
        }
        tag.put("Links", linkList);

        ListTag networkList = new ListTag();
        for (NetworkRecord network : networks.values()) {
            networkList.add(network.save());
        }
        tag.put("Networks", networkList);
        return tag;
    }

    private void read(CompoundTag tag) {
        nodes.clear();
        nodesByChunk.clear();
        links.clear();
        linksByNode.clear();
        linksByDimension.clear();
        linkValidity.clear();
        linkKinds.clear();
        networks.clear();

        ListTag nodeList = tag.getList("Nodes", Tag.TAG_COMPOUND);
        for (int i = 0; i < nodeList.size(); i++) {
            NodeRecord node = NodeRecord.load(nodeList.getCompound(i));
            if (node == null) {
                continue;
            }
            nodes.put(node.pos, node);
            indexNode(node.pos);
        }

        ListTag linkList = tag.getList("Links", Tag.TAG_COMPOUND);
        for (int i = 0; i < linkList.size(); i++) {
            CompoundTag linkTag = linkList.getCompound(i);
            Optional<DimensionalNodePos> a = DimensionalNodePos.readPos(linkTag, "A");
            Optional<DimensionalNodePos> b = DimensionalNodePos.readPos(linkTag, "B");
            if (a.isPresent() && b.isPresent() && !a.get().equals(b.get())) {
                GlareLink link = new GlareLink(a.get(), b.get());
                links.add(link);
                indexLink(link);
                try {
                    linkValidity.put(link, LinkValidity.valueOf(linkTag.getString("Validity")));
                } catch (IllegalArgumentException ignored) {
                    linkValidity.put(link, LinkValidity.UNKNOWN);
                }
                try {
                    linkKinds.put(link, LinkKind.valueOf(linkTag.getString("Kind")));
                } catch (IllegalArgumentException ignored) {
                    linkKinds.put(link, LinkKind.NORMAL);
                }
            }
        }

        ListTag networkList = tag.getList("Networks", Tag.TAG_COMPOUND);
        for (int i = 0; i < networkList.size(); i++) {
            NetworkRecord network = NetworkRecord.load(networkList.getCompound(i));
            networks.put(network.id, network);
        }
    }

    public Collection<NodeRecord> getNodes() {
        return List.copyOf(nodes.values());
    }

    public Collection<NetworkRecord> getNetworks() {
        return List.copyOf(networks.values());
    }

    public Diagnostics diagnostics(ServerLevel level) {
        int loadedNodes = 0;
        for (NodeRecord node : nodes.values()) {
            if (node.pos.levelKey().equals(level.dimension()) && level.isLoaded(node.pos.pos())
                    && level.getBlockEntity(node.pos.pos()) instanceof IGlareNode) loadedNodes++;
        }
        int valid = 0;
        int blocked = 0;
        int unknown = 0;
        for (GlareLink link : links) {
            switch (getLinkValidity(link)) {
                case VALID -> valid++;
                case BLOCKED -> blocked++;
                case UNKNOWN -> unknown++;
            }
        }
        return new Diagnostics(nodes.size(), loadedNodes, links.size(), valid, blocked, unknown, networks.size());
    }

    public void forceRebuild(ServerLevel level) {
        rebuildNetworks();
        setDirty();
        notifyLoadedEndpoints(level, nodes.keySet());
        GlareDebug.log("Forced full rebuild: {} nodes, {} links, {} networks", nodes.size(), links.size(), networks.size());
    }

    /**
     * Drops nodes (and their links) that live in a dimension the server no longer has — e.g. a datapack
     * dimension that was removed. Without this, such nodes and their networks persist forever, bloating the
     * save and every rebuild. Nodes in valid but currently-unloaded dimensions are left untouched.
     */
    public void pruneOrphanedDimensions(MinecraftServer server) {
        Set<ResourceKey<Level>> knownDimensions = new HashSet<>(server.levelKeys());
        List<DimensionalNodePos> orphaned = new ArrayList<>();
        for (DimensionalNodePos pos : nodes.keySet()) {
            if (!knownDimensions.contains(pos.levelKey())) {
                orphaned.add(pos);
            }
        }
        if (orphaned.isEmpty()) {
            return;
        }
        for (DimensionalNodePos pos : orphaned) {
            nodes.remove(pos);
            unindexNode(pos);
            removeLinksIf(link -> link.contains(pos));
        }
        rebuildNetworks();
        setDirty();
        GlareDebug.log("Pruned {} GLARE node(s) from unknown dimensions", orphaned.size());
    }

    public Optional<NodeRecord> getNode(DimensionalNodePos pos) {
        return Optional.ofNullable(nodes.get(pos));
    }

    public Optional<NetworkRecord> getNetwork(UUID id) {
        return Optional.ofNullable(networks.get(id));
    }

    Optional<UUID> getNetworkId(DimensionalNodePos node) {
        NodeRecord record = nodes.get(node);
        return record == null ? Optional.empty() : Optional.ofNullable(record.networkId);
    }

    boolean sendTelemetry(UUID networkId, GlareMessage message) {
        NetworkRecord network = networks.get(networkId);
        if (network == null) {
            return false;
        }
        List<GlareMessage> inbox = network.telemetryInboxes.computeIfAbsent(message.to(), ignored -> new ArrayList<>());
        inbox.add(message);
        while (inbox.size() > TelemetryService.MAX_MESSAGES) {
            inbox.removeFirst();
        }
        setDirty();
        notifyTelemetrySubscribers(networkId, message.to(), TelemetryService.Mutation.SENT, message);
        return true;
    }

    List<GlareMessage> readTelemetry(UUID networkId, GlareAddress address) {
        NetworkRecord network = networks.get(networkId);
        if (network == null) {
            return List.of();
        }
        return List.copyOf(network.telemetryInboxes.getOrDefault(address, List.of()));
    }

    Optional<GlareMessage> discardTelemetry(UUID networkId, GlareAddress address, UUID messageId) {
        NetworkRecord network = networks.get(networkId);
        if (network == null) {
            return Optional.empty();
        }
        List<GlareMessage> inbox = network.telemetryInboxes.get(address);
        if (inbox == null) {
            return Optional.empty();
        }
        for (int i = 0; i < inbox.size(); i++) {
            if (inbox.get(i).id().equals(messageId)) {
                return discardTelemetryAt(networkId, address, i);
            }
        }
        return Optional.empty();
    }

    Optional<GlareMessage> discardTelemetryAt(UUID networkId, GlareAddress address, int index) {
        NetworkRecord network = networks.get(networkId);
        if (network == null) {
            return Optional.empty();
        }
        List<GlareMessage> inbox = network.telemetryInboxes.get(address);
        if (inbox == null || index < 0 || index >= inbox.size()) {
            return Optional.empty();
        }
        GlareMessage removed = inbox.remove(index);
        if (inbox.isEmpty()) {
            network.telemetryInboxes.remove(address);
        }
        setDirty();
        notifyTelemetrySubscribers(networkId, address, TelemetryService.Mutation.DISCARDED, removed);
        return Optional.of(removed);
    }

    long subscribeTelemetry(DimensionalNodePos owner, GlareAddress address, Consumer<TelemetryService.InboxUpdate> listener) {
        long id = nextTelemetrySubscriberId++;
        telemetrySubscribers.put(id, new TelemetrySubscriber(owner, address, listener));
        return id;
    }

    void unsubscribeTelemetry(long id) {
        telemetrySubscribers.remove(id);
    }

    public List<GlareLink> getLinksFor(DimensionalNodePos pos) {
        LinkedHashSet<GlareLink> indexed = linksByNode.get(pos);
        return indexed == null ? List.of() : List.copyOf(indexed);
    }

    public List<GlareLink> getCountedLinksFor(DimensionalNodePos pos) {
        return getLinksFor(pos).stream()
                .filter(link -> getLinkKind(link).countsTowardLimit())
                .toList();
    }

    public List<DimensionalNodePos> getNeighbours(DimensionalNodePos pos) {
        return getLinksFor(pos).stream().map(link -> link.other(pos)).toList();
    }

    private List<DimensionalNodePos> getActiveNeighbours(DimensionalNodePos pos) {
        return getLinksFor(pos).stream()
                .filter(this::isActiveNetworkLink)
                .map(link -> link.other(pos))
                .toList();
    }

    public LinkValidity getLinkValidity(GlareLink link) {
        return linkValidity.getOrDefault(link, LinkValidity.UNKNOWN);
    }

    public LinkKind getLinkKind(GlareLink link) {
        return linkKinds.getOrDefault(link, LinkKind.NORMAL);
    }

    private boolean isActiveNetworkLink(GlareLink link) {
        return getLinkValidity(link) != LinkValidity.BLOCKED;
    }

    public List<LinkRenderRecord> getRenderableLinksFor(net.minecraft.server.level.ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return List.of();
        }
        double syncDistance = Math.max(256.0D, level.getServer().getPlayerList().getViewDistance() * 16.0D + 64.0D);
        double syncDistanceSq = syncDistance * syncDistance;
        return links.stream()
                .filter(link -> getLinkKind(link).renders())
                .filter(link -> link.a().levelKey().equals(level.dimension()) && link.b().levelKey().equals(level.dimension()))
                .filter(link -> level.isLoaded(link.a().pos()) || level.isLoaded(link.b().pos()))
                .filter(link -> isCloseEnoughToRender(player, link, syncDistanceSq))
                .map(link -> new LinkRenderRecord(link.a(), link.b(), getLinkValidity(link)))
                .toList();
    }

    public void registerNode(ServerLevel level, IGlareNode glareNode) {
        DimensionalNodePos pos = glareNode.getGlareNodePos();
        NodeRecord record = nodes.get(pos);
        boolean topologyChanged = record == null;
        Set<DimensionalNodePos> topologyAffected = topologyChanged ? new LinkedHashSet<>(Set.of(pos))
                : getNetworkNodesFor(pos);
        if (record == null) {
            record = new NodeRecord(pos);
            nodes.put(pos, record);
            indexNode(pos);
        }
        record.maxLinks = Math.max(0, glareNode.getMaxGlareLinks());
        record.manualLinkingEnabled = glareNode.allowsManualGlareLinks();
        record.loaded = true;
        record.removed = false;
        updateLuxState(record, glareNode);
        topologyChanged |= trimLinksToLimit(record);
        if (topologyChanged || record.networkId == null || !networks.containsKey(record.networkId)) {
            Set<DimensionalNodePos> touched = setDirtyAndRebuildAround(topologyAffected);
            notifyLoadedEndpoints(level, touched);
        } else {
            refreshNetworkAggregates(record.networkId);
            setDirty();
        }
        notifyRecordNetworkOrNode(level, record);
    }

    public void markLoaded(ServerLevel level, DimensionalNodePos pos) {
        NodeRecord record = nodes.get(pos);
        if (record != null) {
            record.loaded = true;
            record.removed = false;
            setDirty();
            notifyLoadedNode(level, record);
        }
    }

    public void unregisterLoadedNode(DimensionalNodePos pos) {
        NodeRecord removed = nodes.get(pos);
        if (removed == null) {
            return;
        }
        UUID oldNetworkId = removed.networkId;
        Set<DimensionalNodePos> seeds = new LinkedHashSet<>();
        for (GlareLink link : getLinksFor(pos)) {
            seeds.add(link.other(pos));
        }
        nodes.remove(pos);
        unindexNode(pos);
        removeLinksIf(link -> link.contains(pos));
        setDirtyAndRebuildAround(seeds, oldNetworkId == null ? List.of() : List.of(oldNetworkId));
    }

    public void unregisterLoadedNode(ServerLevel level, DimensionalNodePos pos) {
        NodeRecord removed = nodes.get(pos);
        if (removed == null) {
            return;
        }

        UUID oldNetworkId = removed.networkId;
        Set<DimensionalNodePos> seeds = new LinkedHashSet<>();
        for (GlareLink link : getLinksFor(pos)) {
            seeds.add(link.other(pos));
        }
        nodes.remove(pos);
        unindexNode(pos);
        removeLinksIf(link -> link.contains(pos));
        Set<DimensionalNodePos> touched = setDirtyAndRebuildAround(seeds, oldNetworkId == null ? List.of() : List.of(oldNetworkId));
        notifyLoadedEndpoints(level, touched);
    }

    public void updateNodeState(ServerLevel level, IGlareNode glareNode) {
        DimensionalNodePos pos = glareNode.getGlareNodePos();
        NodeRecord record = nodes.get(pos);
        boolean topologyChanged = record == null;
        Set<DimensionalNodePos> topologyAffected = topologyChanged ? new LinkedHashSet<>(Set.of(pos))
                : getNetworkNodesFor(pos);
        if (record == null) {
            record = new NodeRecord(pos);
            nodes.put(pos, record);
            indexNode(pos);
        }
        record.maxLinks = Math.max(0, glareNode.getMaxGlareLinks());
        record.manualLinkingEnabled = glareNode.allowsManualGlareLinks();
        record.loaded = true;
        record.removed = false;
        updateLuxState(record, glareNode);
        topologyChanged |= trimLinksToLimit(record);
        if (topologyChanged || record.networkId == null || !networks.containsKey(record.networkId)) {
            Set<DimensionalNodePos> touched = setDirtyAndRebuildAround(topologyAffected);
            notifyLoadedEndpoints(level, touched);
        } else {
            refreshNetworkAggregates(record.networkId);
            setDirty();
        }
        notifyRecordNetworkOrNode(level, record);
    }

    private static void updateLuxState(NodeRecord record, IGlareNode glareNode) {
        record.luxProduced = 0;
        record.luxAllocated = 0;
        record.emitter = false;
        record.receiver = false;

        if (glareNode instanceof IGlareEmitter emitter) {
            record.emitter = true;
            record.colour = emitter.getLuxColourCharge();
            record.luxProduced = emitter.isGlareEmitterEnabled() ? Math.max(0, emitter.getProducedLux()) : 0;
        }
        if (glareNode instanceof IGlareReceiver receiver) {
            record.receiver = true;
            record.luxAllocated = Math.max(0, receiver.getAllocatedLux());
            record.status = receiver.getGlareOperationStatus();
        }
        record.telemetryAddress = glareNode instanceof IGlareTelemetryEndpoint endpoint
                ? endpoint.getTelemetryAddress()
                : GlareAddress.empty();
        if (glareNode instanceof IRemoteEntanglementEndpoint endpoint) {
            record.remoteEndpointKind = endpoint.getRemoteEndpointKind();
            record.remoteAddress = endpoint.getRemoteAddress();
            record.remoteMode = endpoint.getRemoteMode();
            record.remoteAssembled = endpoint.isRemoteEndpointAssembled();
        } else {
            record.remoteEndpointKind = RemoteEndpointKind.NONE;
            record.remoteAddress = GlareAddress.empty();
            record.remoteMode = RemoteEntanglementMode.DEPOT_SEND;
            record.remoteAssembled = false;
        }
    }

    public LinkResult tryAddLink(ServerLevel level, DimensionalNodePos a, DimensionalNodePos b) {
        if (a.equals(b)) {
            return LinkResult.FAIL_SAME_NODE;
        }
        NodeRecord first = nodes.get(a);
        NodeRecord second = nodes.get(b);
        if (first == null || second == null) {
            return LinkResult.FAIL_MISSING_NODE;
        }
        GlareLink link = new GlareLink(a, b);
        if (links.contains(link)) {
            return LinkResult.ALREADY_LINKED;
        }
        if (!a.levelKey().equals(b.levelKey())) {
            return LinkResult.FAIL_CROSS_DIMENSION;
        }
        if (first.maxLinks <= 0 || second.maxLinks <= 0) {
            return LinkResult.FAIL_LINK_LIMIT;
        }
        if (!first.manualLinkingEnabled || !second.manualLinkingEnabled) {
            return LinkResult.FAIL_MANUAL_LINK_DISABLED;
        }
        boolean canValidate = GlareLineOfSight.canValidate(level.getServer(), a, b);
        boolean hasLineOfSight = !canValidate || GlareLineOfSight.hasLineOfSight(level.getServer(), a, b);
        if (canValidate && !hasLineOfSight) {
            return LinkResult.FAIL_LINE_OF_SIGHT;
        }

        Set<DimensionalNodePos> affected = new LinkedHashSet<>();
        affected.add(a);
        affected.add(b);
        affected.addAll(evictOldestIfFull(first));
        affected.addAll(evictOldestIfFull(second));
        links.add(link);
        indexLink(link);
        linkValidity.put(link, canValidate ? LinkValidity.VALID : LinkValidity.UNKNOWN);
        linkKinds.put(link, LinkKind.NORMAL);
        Set<DimensionalNodePos> touched = setDirtyAndRebuildAround(affected);
        notifyLoadedEndpoints(level, touched);
        GlareDebug.log("Created link {} <-> {} ({})", a.toShortString(), b.toShortString(), getLinkValidity(link));
        return LinkResult.CREATED;
    }

    public LinkResult tryAddSocketLink(ServerLevel level, DimensionalNodePos a, DimensionalNodePos b) {
        if (a.equals(b)) {
            return LinkResult.FAIL_SAME_NODE;
        }
        if (!a.levelKey().equals(b.levelKey())) {
            return LinkResult.FAIL_CROSS_DIMENSION;
        }
        if (!nodes.containsKey(a) || !nodes.containsKey(b)) {
            return LinkResult.FAIL_MISSING_NODE;
        }
        GlareLink link = new GlareLink(a, b);
        if (links.contains(link)) {
            return getLinkKind(link) == LinkKind.SOCKET ? LinkResult.ALREADY_LINKED : LinkResult.FAIL_LINK_LIMIT;
        }

        Set<DimensionalNodePos> affected = new LinkedHashSet<>(Set.of(a, b));
        affected.addAll(getNetworkNodesFor(a));
        affected.addAll(getNetworkNodesFor(b));
        links.add(link);
        indexLink(link);
        linkValidity.put(link, LinkValidity.VALID);
        linkKinds.put(link, LinkKind.SOCKET);
        Set<DimensionalNodePos> touched = setDirtyAndRebuildAround(affected);
        notifyLoadedEndpoints(level, touched);
        GlareDebug.log("Created socket link {} <-> {}", a.toShortString(), b.toShortString());
        return LinkResult.CREATED;
    }

    private Set<DimensionalNodePos> evictOldestIfFull(NodeRecord record) {
        Set<DimensionalNodePos> affected = new LinkedHashSet<>();
        while (record.maxLinks >= 0 && getCountedLinksFor(record.pos).size() >= record.maxLinks && record.maxLinks > 0) {
            GlareLink oldest = getCountedLinksFor(record.pos).get(0);
            affected.add(oldest.a());
            affected.add(oldest.b());
            links.remove(oldest);
            unindexLink(oldest);
            linkValidity.remove(oldest);
            linkKinds.remove(oldest);
        }
        return affected;
    }

    private boolean trimLinksToLimit(NodeRecord record) {
        boolean changed = false;
        int limit = Math.max(0, record.maxLinks);
        while (getCountedLinksFor(record.pos).size() > limit) {
            GlareLink oldest = getCountedLinksFor(record.pos).getFirst();
            links.remove(oldest);
            unindexLink(oldest);
            linkValidity.remove(oldest);
            linkKinds.remove(oldest);
            changed = true;
        }
        return changed;
    }

    public boolean removeLink(ServerLevel level, DimensionalNodePos a, DimensionalNodePos b) {
        GlareLink link = new GlareLink(a, b);
        boolean removed = links.remove(link);
        if (removed) {
            unindexLink(link);
            linkValidity.remove(link);
            linkKinds.remove(link);
            Set<DimensionalNodePos> touched = setDirtyAndRebuildAround(List.of(a, b));
            notifyLoadedEndpoints(level, touched);
            GlareDebug.log("Removed link {} <-> {}", a.toShortString(), b.toShortString());
        }
        return removed;
    }

    public int validateLoadedLinks(ServerLevel level, int maxChecks) {
        if (maxChecks <= 0) return 0;
        List<GlareLink> dimensionLinks = linksByDimension.get(level.dimension());
        if (dimensionLinks == null) dimensionLinks = List.of();
        if (dimensionLinks.isEmpty()) {
            linkValidationCursors.remove(level.dimension());
            return 0;
        }

        boolean changed = false;
        int validated = 0;
        int examined = 0;
        Set<DimensionalNodePos> affected = new LinkedHashSet<>();
        int start = Math.floorMod(linkValidationCursors.getOrDefault(level.dimension(), 0), dimensionLinks.size());
        int examinationBudget = Math.min(maxChecks, dimensionLinks.size());
        while (examined < examinationBudget) {
            GlareLink link = dimensionLinks.get((start + examined) % dimensionLinks.size());
            examined++;
            if (!getLinkKind(link).requiresLineOfSight()) {
                continue;
            }
            if (!GlareLineOfSight.canValidate(level.getServer(), link.a(), link.b())) {
                continue;
            }
            validated++;
            LinkValidity updated = GlareLineOfSight.hasLineOfSight(level.getServer(), link.a(), link.b()) ? LinkValidity.VALID : LinkValidity.BLOCKED;
            if (getLinkValidity(link) != updated) {
                affected.addAll(getNetworkNodesFor(link.a()));
                affected.addAll(getNetworkNodesFor(link.b()));
                linkValidity.put(link, updated);
                affected.add(link.a());
                affected.add(link.b());
                changed = true;
            }
        }
        linkValidationCursors.put(level.dimension(), (start + examined) % dimensionLinks.size());
        if (changed) {
            Set<DimensionalNodePos> touched = setDirtyAndRebuildAround(affected);
            notifyLoadedEndpoints(level, touched);
            GlareDebug.log("LoS batch changed topology after examining {} links ({} fully validated)", examined, validated);
        }
        return validated;
    }

    public int validateLoadedLinks(ServerLevel level) {
        return validateLoadedLinks(level, Integer.MAX_VALUE);
    }

    public boolean tryResetNetwork(UUID networkId) {
        NetworkRecord network = networks.get(networkId);
        if (network == null) {
            return false;
        }
        if (network.luxAllocated > network.luxCapacity) {
            markNetworkOverloaded(network);
            setDirty();
            return false;
        }
        for (DimensionalNodePos pos : network.nodes) {
            NodeRecord node = nodes.get(pos);
            if (node != null && node.receiver) {
                node.status = GlareOperationStatus.ONLINE;
            }
        }
        network.overloaded = false;
        setDirty();
        return true;
    }

    public boolean tryResetNetwork(ServerLevel level, UUID networkId) {
        boolean reset = tryResetNetwork(networkId);
        NetworkRecord network = networks.get(networkId);
        if (network != null) {
            notifyLoadedEndpoints(level, network.nodes);
        }
        return reset;
    }

    public boolean forceOverloadNetwork(ServerLevel level, UUID networkId) {
        NetworkRecord network = networks.get(networkId);
        if (network == null) {
            return false;
        }
        markNetworkOverloaded(network);
        setDirty();
        notifyLoadedEndpoints(level, network.nodes);
        return true;
    }

    public void sampleLuxHistories(MinecraftServer server, int sampleIntervalTicks, int maxSamples) {
        int interval = Math.max(1, sampleIntervalTicks);
        int limit = Math.max(1, maxSamples);
        long gameTime = server.getTickCount();
        Set<UUID> sampledNetworkIds = new HashSet<>();
        for (NetworkRecord network : networks.values()) {
            if (network.lastLuxSampleGameTime != Long.MIN_VALUE
                    && gameTime - network.lastLuxSampleGameTime < interval) {
                continue;
            }
            network.lastLuxSampleGameTime = gameTime;
            network.luxHistory.add(Math.max(0, network.luxAllocated));
            while (network.luxHistory.size() > limit) {
                network.luxHistory.removeFirst();
            }
            sampledNetworkIds.add(network.id);
        }
        if (sampledNetworkIds.isEmpty()) {
            return;
        }
        setDirty();
        // The Lux history graph is only shown in the Power Terminal GUI, so only re-sync nodes a player is
        // actively viewing through one — not every node in the network. Goggle tooltips don't show history,
        // and lux totals don't change on a pure history sample, so nothing else needs a resync here.
        notifyPowerTerminalViewers(server, sampledNetworkIds);
    }

    private void notifyPowerTerminalViewers(MinecraftServer server, Set<UUID> sampledNetworkIds) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!(player.containerMenu instanceof PowerTerminalMenu terminal)) {
                continue;
            }
            DimensionalNodePos pos = new DimensionalNodePos(player.level().dimension(), terminal.getBlockPos());
            NodeRecord record = nodes.get(pos);
            if (record == null || record.networkId == null || !sampledNetworkIds.contains(record.networkId)) {
                continue;
            }
            notifyLoadedEndpoint(server, pos);
        }
    }

    public void reconcileLoadedChunk(ServerLevel level, BlockPos chunkOrigin) {
        int chunkX = chunkOrigin.getX() >> 4;
        int chunkZ = chunkOrigin.getZ() >> 4;
        Set<DimensionalNodePos> inChunk = nodesInChunk(level.dimension(), ChunkPos.asLong(chunkX, chunkZ));
        if (inChunk.isEmpty()) {
            return;
        }
        boolean topologyChanged = false;
        boolean stateChanged = false;
        Set<UUID> networksToRefresh = new LinkedHashSet<>();
        Set<DimensionalNodePos> affected = new LinkedHashSet<>();
        Set<UUID> removedNetworkIds = new LinkedHashSet<>();
        for (DimensionalNodePos nodePos : new ArrayList<>(inChunk)) {
            NodeRecord record = nodes.get(nodePos);
            if (record == null) {
                continue;
            }
            BlockPos pos = record.pos.pos();
            if (!level.isLoaded(pos)) {
                continue;
            }
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof IGlareNode glareNode) {
                record.loaded = true;
                record.removed = false;
                record.maxLinks = glareNode.getMaxGlareLinks();
                updateLuxState(record, glareNode);
                if (trimLinksToLimit(record)) {
                    topologyChanged = true;
                    affected.addAll(getNetworkNodesFor(record.pos));
                } else {
                    if (record.networkId != null) networksToRefresh.add(record.networkId);
                    stateChanged = true;
                }
            } else {
                affected.add(record.pos);
                for (GlareLink link : getLinksFor(record.pos)) {
                    affected.add(link.other(record.pos));
                }
                if (record.networkId != null) removedNetworkIds.add(record.networkId);
                nodes.remove(record.pos);
                unindexNode(record.pos);
                removeLinksIf(link -> link.contains(record.pos));
                topologyChanged = true;
            }
        }
        if (topologyChanged) {
            Set<DimensionalNodePos> touched = setDirtyAndRebuildAround(affected, removedNetworkIds);
            notifyLoadedEndpoints(level, touched);
            GlareDebug.log("Chunk {},{} reconciliation removed stale nodes or links", chunkX, chunkZ);
        } else if (stateChanged) {
            for (UUID networkId : networksToRefresh) refreshNetworkAggregates(networkId);
            setDirty();
            for (UUID networkId : networksToRefresh) {
                NetworkRecord network = networks.get(networkId);
                if (network != null) notifyLoadedEndpoints(level, network.nodes);
            }
        }
    }

    public void markChunkUnloaded(ServerLevel level, BlockPos chunkOrigin) {
        int chunkX = chunkOrigin.getX() >> 4;
        int chunkZ = chunkOrigin.getZ() >> 4;
        Set<DimensionalNodePos> inChunk = nodesInChunk(level.dimension(), ChunkPos.asLong(chunkX, chunkZ));
        if (inChunk.isEmpty()) {
            return;
        }
        boolean changed = false;
        for (DimensionalNodePos nodePos : inChunk) {
            NodeRecord record = nodes.get(nodePos);
            if (record != null && record.loaded) {
                record.loaded = false;
                changed = true;
            }
        }
        if (changed) setDirty();
    }

    public void rebuildNetworks() {
        Map<UUID, NetworkRecord> oldNetworks = new HashMap<>(networks);
        networks.clear();
        for (NodeRecord node : nodes.values()) {
            node.lastKnownLinks.clear();
        }
        for (GlareLink link : links) {
            NodeRecord a = nodes.get(link.a());
            NodeRecord b = nodes.get(link.b());
            if (a != null && b != null) {
                a.lastKnownLinks.add(link.b());
                b.lastKnownLinks.add(link.a());
            }
        }

        Set<DimensionalNodePos> visited = new HashSet<>();
        Set<UUID> claimedNetworkIds = new HashSet<>();
        for (DimensionalNodePos start : nodes.keySet()) {
            if (!visited.add(start)) {
                continue;
            }
            Set<DimensionalNodePos> component = collectComponent(start, visited, null);
            processComponent(component, oldNetworks, claimedNetworkIds);
        }
        notifyTelemetryRebuildChanges(oldNetworks, null);
        GlareDebug.log("Rebuilt graph: {} nodes, {} links, {} networks", nodes.size(), links.size(), networks.size());
    }

    /**
     * Scoped rebuild: recomputes only the network component(s) reachable from {@code seeds} (and from
     * {@code extraNetworkIds}), leaving every other network untouched. Because networks never share a
     * node, an unaffected network can never overlap the recomputed universe, so this produces identical
     * membership, id-preservation, overload and telemetry results to {@link #rebuildNetworks()} for the
     * affected networks — it is a pure performance scoping of the same algorithm.
     *
     * <p>{@code extraNetworkIds} lets callers force-recompute a network even when no seed still resolves
     * to it (e.g. a lone node was removed and its network must be dropped).</p>
     */
    private Set<DimensionalNodePos> rebuildNetworksAround(Collection<DimensionalNodePos> seeds) {
        return rebuildNetworksAround(seeds, List.of());
    }

    /**
     * @return every node whose network was recomputed (the "universe") — exactly the nodes whose derived
     *         state (network id, overload status, receiver operation status) may have changed, so callers
     *         must notify all of them, not just the seeds. Returning this is what makes an overload (or any
     *         state change) reach every node in the affected network, not only the source's neighbours.
     */
    private Set<DimensionalNodePos> rebuildNetworksAround(Collection<DimensionalNodePos> seeds, Collection<UUID> extraNetworkIds) {
        Set<UUID> affectedNetworkIds = new HashSet<>();
        Set<DimensionalNodePos> universe = new HashSet<>();
        ArrayDeque<DimensionalNodePos> worklist = new ArrayDeque<>(seeds);
        for (UUID id : extraNetworkIds) {
            NetworkRecord network = networks.get(id);
            if (network != null && affectedNetworkIds.add(id)) {
                worklist.addAll(network.nodes);
            }
        }
        // Closure: pull in every node reachable via active links from a seed, plus every node of any old
        // network a reached node belonged to (so a split recomputes all resulting components, and a merge
        // pulls in both prior networks).
        while (!worklist.isEmpty()) {
            DimensionalNodePos pos = worklist.removeFirst();
            NodeRecord record = nodes.get(pos);
            if (record == null || !universe.add(pos)) {
                continue;
            }
            UUID networkId = record.networkId;
            if (networkId != null && networks.containsKey(networkId) && affectedNetworkIds.add(networkId)) {
                worklist.addAll(networks.get(networkId).nodes);
            }
            for (DimensionalNodePos neighbour : getActiveNeighbours(pos)) {
                if (nodes.containsKey(neighbour)) {
                    worklist.add(neighbour);
                }
            }
        }

        Map<UUID, NetworkRecord> oldNetworks = new HashMap<>();
        for (UUID id : affectedNetworkIds) {
            NetworkRecord removed = networks.remove(id);
            if (removed != null) {
                oldNetworks.put(id, removed);
            }
        }

        if (universe.isEmpty()) {
            // The affected networks had no surviving nodes (e.g. lone-node removal) — already dropped above.
            if (!oldNetworks.isEmpty()) {
                notifyTelemetryRebuildChanges(oldNetworks, universe);
            }
            return universe;
        }

        for (DimensionalNodePos pos : universe) {
            NodeRecord record = nodes.get(pos);
            if (record == null) {
                continue;
            }
            record.lastKnownLinks.clear();
            for (GlareLink link : getLinksFor(pos)) {
                record.lastKnownLinks.add(link.other(pos));
            }
        }

        Set<DimensionalNodePos> visited = new HashSet<>();
        Set<UUID> claimedNetworkIds = new HashSet<>();
        for (DimensionalNodePos start : universe) {
            if (!visited.add(start)) {
                continue;
            }
            Set<DimensionalNodePos> component = collectComponent(start, visited, universe);
            processComponent(component, oldNetworks, claimedNetworkIds);
        }
        notifyTelemetryRebuildChanges(oldNetworks, universe);
        GlareDebug.log("Scoped rebuild: {} nodes across {} network(s)", universe.size(), affectedNetworkIds.size());
        return universe;
    }

    private Set<DimensionalNodePos> collectComponent(DimensionalNodePos start, Set<DimensionalNodePos> visited,
            @Nullable Set<DimensionalNodePos> bounds) {
        Set<DimensionalNodePos> component = new LinkedHashSet<>();
        ArrayDeque<DimensionalNodePos> queue = new ArrayDeque<>();
        queue.add(start);
        while (!queue.isEmpty()) {
            DimensionalNodePos current = queue.removeFirst();
            component.add(current);
            for (DimensionalNodePos neighbour : getActiveNeighbours(current)) {
                boolean inBounds = bounds == null ? nodes.containsKey(neighbour) : bounds.contains(neighbour);
                if (inBounds && visited.add(neighbour)) {
                    queue.addLast(neighbour);
                }
            }
        }
        return component;
    }

    private void processComponent(Set<DimensionalNodePos> component, Map<UUID, NetworkRecord> oldNetworks,
            Set<UUID> claimedNetworkIds) {
        UUID id = chooseNetworkId(component, oldNetworks, claimedNetworkIds);
        claimedNetworkIds.add(id);
        boolean preserveOverload = shouldPreserveOverload(component, oldNetworks);
        boolean mergedFromMultipleNetworks = oldNetworkOverlapCount(component, oldNetworks) > 1;
        NetworkRecord oldNetwork = oldNetworks.get(id);
        NetworkRecord network = oldNetwork == null ? new NetworkRecord(id) : oldNetwork.copyForRebuild(preserveOverload);
        if (oldNetwork == null) {
            network.overloaded = preserveOverload;
        }
        for (NetworkRecord candidate : oldNetworks.values()) {
            if (candidate.nodes.stream().anyMatch(component::contains)) {
                network.mergeTelemetryFrom(candidate);
            }
        }
        network.nodes.addAll(component);
        for (DimensionalNodePos current : component) {
            NodeRecord node = nodes.get(current);
            if (node != null) {
                node.networkId = id;
                applyLux(network, node);
                if (node.telemetryAddress.isComplete()) {
                    network.registeredTelemetryAddresses.add(node.telemetryAddress);
                }
            }
        }
        boolean mergedOverloadRecovered = mergedFromMultipleNetworks
                && network.overloaded
                && network.luxAllocated <= network.luxCapacity;
        if (network.luxAllocated > network.luxCapacity || (network.overloaded && !mergedOverloadRecovered)) {
            markNetworkOverloaded(network);
        } else {
            markNetworkOnline(network);
        }
        networks.put(id, network);
    }

    private static boolean shouldPreserveOverload(Set<DimensionalNodePos> component, Map<UUID, NetworkRecord> oldNetworks) {
        for (NetworkRecord oldNetwork : oldNetworks.values()) {
            if (!oldNetwork.overloaded || oldNetwork.nodes.size() <= 1) {
                continue;
            }
            for (DimensionalNodePos pos : oldNetwork.nodes) {
                if (component.contains(pos)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static UUID chooseNetworkId(Set<DimensionalNodePos> component, Map<UUID, NetworkRecord> oldNetworks, Set<UUID> claimedIds) {
        return oldNetworks.values().stream()
                .filter(network -> !claimedIds.contains(network.id))
                .filter(network -> network.nodes.stream().anyMatch(component::contains))
                .max(Comparator.comparingInt(network -> overlapCount(component, network.nodes)))
                .map(network -> network.id)
                .orElseGet(UUID::randomUUID);
    }

    private void notifyTelemetrySubscribers(UUID networkId, GlareAddress address, TelemetryService.Mutation mutation,
            @Nullable GlareMessage changedMessage) {
        List<GlareMessage> snapshot = readTelemetry(networkId, address);
        for (Map.Entry<Long, TelemetrySubscriber> entry : List.copyOf(telemetrySubscribers.entrySet())) {
            TelemetrySubscriber subscriber = entry.getValue();
            NodeRecord owner = nodes.get(subscriber.owner);
            if (owner == null) {
                telemetrySubscribers.remove(entry.getKey());
                continue;
            }
            if (networkId.equals(owner.networkId) && address.equals(subscriber.address)) {
                notifyTelemetrySubscriber(subscriber, new TelemetryService.InboxUpdate(networkId, address, snapshot, mutation, changedMessage));
            }
        }
    }

    private void notifyTelemetryRebuildChanges(Map<UUID, NetworkRecord> oldNetworks, @Nullable Set<DimensionalNodePos> scope) {
        for (Map.Entry<Long, TelemetrySubscriber> entry : List.copyOf(telemetrySubscribers.entrySet())) {
            TelemetrySubscriber subscriber = entry.getValue();
            NodeRecord owner = nodes.get(subscriber.owner);
            if (owner == null) {
                telemetrySubscribers.remove(entry.getKey());
                continue;
            }
            if (scope != null && !scope.contains(subscriber.owner)) {
                // A scoped rebuild left this subscriber's network untouched — nothing changed for it.
                continue;
            }
            if (owner.networkId == null) {
                telemetrySubscribers.remove(entry.getKey());
                continue;
            }
            NetworkRecord oldNetwork = oldNetworks.values().stream()
                    .filter(network -> network.nodes.contains(subscriber.owner))
                    .findFirst()
                    .orElse(null);
            List<GlareMessage> before = oldNetwork == null
                    ? List.of()
                    : List.copyOf(oldNetwork.telemetryInboxes.getOrDefault(subscriber.address, List.of()));
            List<GlareMessage> after = readTelemetry(owner.networkId, subscriber.address);
            if (oldNetwork == null || !oldNetwork.id.equals(owner.networkId) || !before.equals(after)) {
                notifyTelemetrySubscriber(subscriber, new TelemetryService.InboxUpdate(
                        owner.networkId, subscriber.address, after, TelemetryService.Mutation.NETWORK_REBUILT, null));
            }
        }
    }

    private static void notifyTelemetrySubscriber(TelemetrySubscriber subscriber, TelemetryService.InboxUpdate update) {
        try {
            subscriber.listener.accept(update);
        } catch (RuntimeException exception) {
            ResourcefulRefinementMain.LOGGER.error("GLARE telemetry subscriber at {} failed while handling {}",
                    subscriber.owner.toShortString(), update.mutation(), exception);
        }
    }

    private static int overlapCount(Set<DimensionalNodePos> component, Set<DimensionalNodePos> previousNodes) {
        int count = 0;
        for (DimensionalNodePos pos : previousNodes) {
            if (component.contains(pos)) {
                count++;
            }
        }
        return count;
    }

    private static int oldNetworkOverlapCount(Set<DimensionalNodePos> component, Map<UUID, NetworkRecord> oldNetworks) {
        int count = 0;
        for (NetworkRecord network : oldNetworks.values()) {
            if (network.nodes.stream().anyMatch(component::contains)) {
                count++;
            }
        }
        return count;
    }

    private void applyLux(NetworkRecord network, NodeRecord node) {
        network.luxCapacity += Math.max(0, node.luxProduced);
        network.luxAllocated += Math.max(0, node.luxAllocated);
        if (node.emitter && node.luxProduced > 0) {
            network.colourCharges.merge(node.colour, node.luxProduced, Integer::sum);
        }
    }

    /** Recomputes derived state without touching graph membership, network ids, or telemetry inboxes. */
    private void refreshNetworkAggregates(UUID networkId) {
        NetworkRecord network = networks.get(networkId);
        if (network == null) return;
        boolean preserveOverload = network.overloaded;
        network.luxCapacity = 0;
        network.luxAllocated = 0;
        network.colourCharges.clear();
        network.registeredTelemetryAddresses.clear();
        for (DimensionalNodePos pos : network.nodes) {
            NodeRecord node = nodes.get(pos);
            if (node == null) continue;
            applyLux(network, node);
            if (node.telemetryAddress.isComplete()) network.registeredTelemetryAddresses.add(node.telemetryAddress);
        }
        if (preserveOverload || network.luxAllocated > network.luxCapacity) {
            markNetworkOverloaded(network);
        } else {
            markNetworkOnline(network);
        }
    }

    private void markNetworkOverloaded(NetworkRecord network) {
        network.overloaded = true;
        for (DimensionalNodePos pos : network.nodes) {
            NodeRecord node = nodes.get(pos);
            if (node != null && node.receiver) {
                node.status = GlareOperationStatus.OVERLOADED;
            }
        }
    }

    private void markNetworkOnline(NetworkRecord network) {
        network.overloaded = false;
        for (DimensionalNodePos pos : network.nodes) {
            NodeRecord node = nodes.get(pos);
            if (node != null && node.receiver && node.status == GlareOperationStatus.OVERLOADED) {
                node.status = GlareOperationStatus.ONLINE;
            }
        }
    }

    private Set<DimensionalNodePos> setDirtyAndRebuildAround(Collection<DimensionalNodePos> seeds) {
        Set<DimensionalNodePos> touched = rebuildNetworksAround(seeds);
        setDirty();
        return touched;
    }

    private Set<DimensionalNodePos> setDirtyAndRebuildAround(Collection<DimensionalNodePos> seeds, Collection<UUID> extraNetworkIds) {
        Set<DimensionalNodePos> touched = rebuildNetworksAround(seeds, extraNetworkIds);
        setDirty();
        return touched;
    }

    private void notifyLoadedEndpoint(ServerLevel level, DimensionalNodePos pos) {
        if (!pos.levelKey().equals(level.dimension()) || !level.isLoaded(pos.pos())) {
            return;
        }
        BlockEntity be = level.getBlockEntity(pos.pos());
        if (be instanceof IGlareNode glareNode) {
            NodeRecord record = nodes.get(pos);
            if (record != null && record.receiver && be instanceof IGlareReceiver receiver) {
                receiver.applyGlareOperationStatusFromNetwork(record.status);
            }
            // Coalesce the endpoint refresh into a single callback + client sync. Every node BE's
            // onGlareNetworkChanged performs a superset of onGlareLinksChanged (it additionally records
            // the network id), so when the node is networked we only need the former; a node with no
            // network id still needs the links refresh.
            if (record != null && record.networkId != null) {
                glareNode.onGlareNetworkChanged(level, record.networkId);
            } else {
                glareNode.onGlareLinksChanged(level);
            }
        }
    }

    private void notifyLoadedEndpoints(ServerLevel level, Collection<DimensionalNodePos> positions) {
        for (DimensionalNodePos pos : positions) {
            notifyLoadedEndpoint(level, pos);
        }
    }

    private void notifyLoadedEndpoint(MinecraftServer server, DimensionalNodePos pos) {
        ServerLevel level = server.getLevel(pos.levelKey());
        if (level != null) {
            notifyLoadedEndpoint(level, pos);
        }
    }

    private void notifyLoadedEndpoints(MinecraftServer server, Collection<DimensionalNodePos> positions) {
        for (DimensionalNodePos pos : positions) {
            notifyLoadedEndpoint(server, pos);
        }
    }

    private void notifyLoadedNode(ServerLevel level, NodeRecord record) {
        notifyLoadedEndpoint(level, record.pos);
    }

    private void notifyRecordNetworkOrNode(ServerLevel level, NodeRecord record) {
        if (record.networkId == null) {
            notifyLoadedNode(level, record);
            return;
        }
        NetworkRecord network = networks.get(record.networkId);
        if (network == null) {
            notifyLoadedNode(level, record);
            return;
        }
        notifyLoadedEndpoints(level, network.nodes);
    }

    private Set<DimensionalNodePos> getNetworkNodesFor(DimensionalNodePos pos) {
        Set<DimensionalNodePos> positions = new LinkedHashSet<>();
        NodeRecord record = nodes.get(pos);
        if (record == null || record.networkId == null) {
            positions.add(pos);
            return positions;
        }
        NetworkRecord network = networks.get(record.networkId);
        if (network == null) {
            positions.add(pos);
            return positions;
        }
        positions.addAll(network.nodes);
        return positions;
    }

    void setLinkValidityForTests(GlareLink link, LinkValidity validity) {
        if (!links.contains(link)) {
            throw new IllegalArgumentException("Cannot set validity for missing GLARE link");
        }
        linkValidity.put(link, validity);
        rebuildNetworks();
    }

    private boolean removeLinksIf(java.util.function.Predicate<GlareLink> predicate) {
        boolean removed = false;
        for (GlareLink link : new ArrayList<>(links)) {
            if (predicate.test(link)) {
                links.remove(link);
                unindexLink(link);
                linkValidity.remove(link);
                linkKinds.remove(link);
                removed = true;
            }
        }
        return removed;
    }

    public boolean canAcceptLink(DimensionalNodePos pos) {
        NodeRecord node = nodes.get(pos);
        return node != null && node.manualLinkingEnabled && node.maxLinks > 0 && getCountedLinksFor(pos).size() < node.maxLinks;
    }

    public int removeAllLinks(ServerLevel level, DimensionalNodePos pos) {
        List<GlareLink> incident = getCountedLinksFor(pos);
        if (incident.isEmpty()) return 0;
        Set<DimensionalNodePos> affected = getNetworkNodesFor(pos);
        for (GlareLink link : incident) {
            affected.add(link.a());
            affected.add(link.b());
            links.remove(link);
            unindexLink(link);
            linkValidity.remove(link);
            linkKinds.remove(link);
        }
        Set<DimensionalNodePos> touched = setDirtyAndRebuildAround(affected);
        notifyLoadedEndpoints(level, touched);
        GlareDebug.log("Removed all {} links from {}", incident.size(), pos.toShortString());
        return incident.size();
    }

    private static long chunkKeyOf(DimensionalNodePos pos) {
        BlockPos block = pos.pos();
        return ChunkPos.asLong(block.getX() >> 4, block.getZ() >> 4);
    }

    private void indexNode(DimensionalNodePos pos) {
        nodesByChunk.computeIfAbsent(pos.levelKey(), ignored -> new HashMap<>())
                .computeIfAbsent(chunkKeyOf(pos), ignored -> new LinkedHashSet<>())
                .add(pos);
    }

    private void unindexNode(DimensionalNodePos pos) {
        Map<Long, Set<DimensionalNodePos>> byChunk = nodesByChunk.get(pos.levelKey());
        if (byChunk == null) return;
        long key = chunkKeyOf(pos);
        Set<DimensionalNodePos> bucket = byChunk.get(key);
        if (bucket == null) return;
        bucket.remove(pos);
        if (bucket.isEmpty()) byChunk.remove(key);
        if (byChunk.isEmpty()) nodesByChunk.remove(pos.levelKey());
    }

    private Set<DimensionalNodePos> nodesInChunk(ResourceKey<Level> dimension, long chunkKey) {
        Map<Long, Set<DimensionalNodePos>> byChunk = nodesByChunk.get(dimension);
        if (byChunk == null) return Set.of();
        Set<DimensionalNodePos> bucket = byChunk.get(chunkKey);
        return bucket == null ? Set.of() : bucket;
    }

    private void indexLink(GlareLink link) {
        linksByNode.computeIfAbsent(link.a(), ignored -> new LinkedHashSet<>()).add(link);
        linksByNode.computeIfAbsent(link.b(), ignored -> new LinkedHashSet<>()).add(link);
        linksByDimension.computeIfAbsent(link.a().levelKey(), ignored -> new ArrayList<>()).add(link);
    }

    private void unindexLink(GlareLink link) {
        removeIndexedLink(link.a(), link);
        removeIndexedLink(link.b(), link);
        ArrayList<GlareLink> dimensionLinks = linksByDimension.get(link.a().levelKey());
        if (dimensionLinks != null) {
            dimensionLinks.remove(link);
            if (dimensionLinks.isEmpty()) linksByDimension.remove(link.a().levelKey());
        }
    }

    private void removeIndexedLink(DimensionalNodePos pos, GlareLink link) {
        LinkedHashSet<GlareLink> indexed = linksByNode.get(pos);
        if (indexed == null) return;
        indexed.remove(link);
        if (indexed.isEmpty()) linksByNode.remove(pos);
    }

    private static boolean isCloseEnoughToRender(net.minecraft.server.level.ServerPlayer player, GlareLink link, double syncDistanceSq) {
        return player.distanceToSqr(link.a().pos().getX() + 0.5D, link.a().pos().getY() + 0.5D, link.a().pos().getZ() + 0.5D) <= syncDistanceSq
                || player.distanceToSqr(link.b().pos().getX() + 0.5D, link.b().pos().getY() + 0.5D, link.b().pos().getZ() + 0.5D) <= syncDistanceSq;
    }

    public enum LinkResult {
        CREATED,
        ALREADY_LINKED,
        FAIL_SAME_NODE,
        FAIL_MISSING_NODE,
        FAIL_CROSS_DIMENSION,
        FAIL_LINE_OF_SIGHT,
        FAIL_LINK_LIMIT,
        FAIL_MANUAL_LINK_DISABLED
    }

    public enum LinkValidity {
        UNKNOWN,
        VALID,
        BLOCKED
    }

    public enum LinkKind {
        NORMAL(true, true, true),
        SOCKET(false, false, false);

        private final boolean countsTowardLimit;
        private final boolean requiresLineOfSight;
        private final boolean renders;

        LinkKind(boolean countsTowardLimit, boolean requiresLineOfSight, boolean renders) {
            this.countsTowardLimit = countsTowardLimit;
            this.requiresLineOfSight = requiresLineOfSight;
            this.renders = renders;
        }

        public boolean countsTowardLimit() {
            return countsTowardLimit;
        }

        public boolean requiresLineOfSight() {
            return requiresLineOfSight;
        }

        public boolean renders() {
            return renders;
        }
    }

    public record LinkRenderRecord(DimensionalNodePos a, DimensionalNodePos b, LinkValidity validity) {}

    public record Diagnostics(int persistedNodes, int loadedNodesInDimension, int links, int validLinks,
            int blockedLinks, int unknownLinks, int networks) {}

    private record TelemetrySubscriber(DimensionalNodePos owner, GlareAddress address,
                                       Consumer<TelemetryService.InboxUpdate> listener) {}

    public static class NodeRecord {
        public final DimensionalNodePos pos;
        public int maxLinks = 0;
        public boolean loaded;
        public boolean removed;
        public boolean manualLinkingEnabled = true;
        public boolean emitter;
        public boolean receiver;
        public int luxProduced;
        public int luxAllocated;
        public DyeColor colour = DyeColor.WHITE;
        public GlareOperationStatus status = GlareOperationStatus.ONLINE;
        public GlareAddress telemetryAddress = GlareAddress.empty();
        public RemoteEndpointKind remoteEndpointKind = RemoteEndpointKind.NONE;
        public GlareAddress remoteAddress = GlareAddress.empty();
        public RemoteEntanglementMode remoteMode = RemoteEntanglementMode.DEPOT_SEND;
        public boolean remoteAssembled;
        public final List<DimensionalNodePos> lastKnownLinks = new ArrayList<>();
        @Nullable
        public UUID networkId;

        NodeRecord(DimensionalNodePos pos) {
            this.pos = pos;
        }

        CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            DimensionalNodePos.writePos(tag, "", pos);
            tag.putInt("MaxLinks", maxLinks);
            tag.putBoolean("Loaded", loaded);
            tag.putBoolean("Removed", removed);
            tag.putBoolean("ManualLinkingEnabled", manualLinkingEnabled);
            tag.putBoolean("Emitter", emitter);
            tag.putBoolean("Receiver", receiver);
            tag.putInt("LuxProduced", luxProduced);
            tag.putInt("LuxAllocated", luxAllocated);
            tag.putString("Colour", colour.getName());
            tag.putString("Status", status.name());
            tag.put("TelemetryAddress", telemetryAddress.save());
            tag.putString("RemoteEndpointKind", remoteEndpointKind.name());
            tag.put("RemoteAddress", remoteAddress.save());
            tag.putString("RemoteMode", remoteMode.name());
            tag.putBoolean("RemoteAssembled", remoteAssembled);
            if (networkId != null) {
                tag.putUUID("Network", networkId);
            }
            ListTag linkList = new ListTag();
            for (DimensionalNodePos link : lastKnownLinks) {
                CompoundTag linkTag = new CompoundTag();
                DimensionalNodePos.writePos(linkTag, "", link);
                linkList.add(linkTag);
            }
            tag.put("LastKnownLinks", linkList);
            return tag;
        }

        @Nullable
        static NodeRecord load(CompoundTag tag) {
            Optional<DimensionalNodePos> parsed = DimensionalNodePos.readPos(tag, "");
            if (parsed.isEmpty()) {
                // Skip a malformed persisted node (missing/invalid position) rather than aborting the whole
                // saved-data load, which would crash world load. Mirrors the defensive link-loading path.
                ResourcefulRefinementMain.LOGGER.warn("Skipping malformed GLARE node entry (missing or invalid position)");
                return null;
            }
            DimensionalNodePos pos = parsed.get();
            NodeRecord record = new NodeRecord(pos);
            record.maxLinks = tag.getInt("MaxLinks");
            record.loaded = tag.getBoolean("Loaded");
            record.removed = tag.getBoolean("Removed");
            record.manualLinkingEnabled = !tag.contains("ManualLinkingEnabled") || tag.getBoolean("ManualLinkingEnabled");
            record.emitter = tag.getBoolean("Emitter");
            record.receiver = tag.getBoolean("Receiver");
            record.luxProduced = tag.getInt("LuxProduced");
            record.luxAllocated = tag.getInt("LuxAllocated");
            record.colour = DyeColor.byName(tag.getString("Colour"), DyeColor.WHITE);
            try {
                record.status = GlareOperationStatus.valueOf(tag.getString("Status"));
            } catch (IllegalArgumentException ignored) {
                record.status = GlareOperationStatus.ONLINE;
            }
            if (tag.contains("TelemetryAddress", Tag.TAG_COMPOUND)) {
                record.telemetryAddress = GlareAddress.load(tag.getCompound("TelemetryAddress"));
            }
            try {
                record.remoteEndpointKind = RemoteEndpointKind.valueOf(tag.getString("RemoteEndpointKind"));
            } catch (IllegalArgumentException ignored) {
                record.remoteEndpointKind = RemoteEndpointKind.NONE;
            }
            if (tag.contains("RemoteAddress", Tag.TAG_COMPOUND)) {
                record.remoteAddress = GlareAddress.load(tag.getCompound("RemoteAddress"));
            }
            try {
                record.remoteMode = RemoteEntanglementMode.valueOf(tag.getString("RemoteMode"));
            } catch (IllegalArgumentException ignored) {
                record.remoteMode = RemoteEntanglementMode.DEPOT_SEND;
            }
            record.remoteAssembled = tag.getBoolean("RemoteAssembled");
            if (tag.hasUUID("Network")) {
                record.networkId = tag.getUUID("Network");
            }
            ListTag linkList = tag.getList("LastKnownLinks", Tag.TAG_COMPOUND);
            for (int i = 0; i < linkList.size(); i++) {
                DimensionalNodePos.readPos(linkList.getCompound(i), "").ifPresent(record.lastKnownLinks::add);
            }
            return record;
        }
    }

    public static class NetworkRecord {
        public final UUID id;
        public final Set<DimensionalNodePos> nodes = new LinkedHashSet<>();
        public final Map<DyeColor, Integer> colourCharges = new HashMap<>();
        public final Map<GlareAddress, List<GlareMessage>> telemetryInboxes = new HashMap<>();
        public final Set<GlareAddress> registeredTelemetryAddresses = new LinkedHashSet<>();
        public final List<Integer> luxHistory = new ArrayList<>();
        public long lastLuxSampleGameTime = Long.MIN_VALUE;
        public int luxCapacity;
        public int luxAllocated;
        public boolean overloaded;

        NetworkRecord(UUID id) {
            this.id = id;
        }

        NetworkRecord copyForRebuild(boolean preserveOverload) {
            NetworkRecord copy = new NetworkRecord(id);
            copy.overloaded = preserveOverload;
            copy.luxHistory.addAll(luxHistory);
            copy.lastLuxSampleGameTime = lastLuxSampleGameTime;
            return copy;
        }

        void mergeTelemetryFrom(NetworkRecord source) {
            for (Map.Entry<GlareAddress, List<GlareMessage>> entry : source.telemetryInboxes.entrySet()) {
                List<GlareMessage> combined = new ArrayList<>(telemetryInboxes.getOrDefault(entry.getKey(), List.of()));
                Map<UUID, GlareMessage> unique = new LinkedHashMap<>();
                for (GlareMessage message : combined) {
                    unique.put(message.id(), message);
                }
                for (GlareMessage message : entry.getValue()) {
                    unique.putIfAbsent(message.id(), message);
                }
                combined.clear();
                combined.addAll(unique.values());
                combined.sort(Comparator.comparingLong(GlareMessage::gameTime).thenComparing(GlareMessage::id));
                telemetryInboxes.put(entry.getKey(), combined);
            }
        }

        CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("Id", id);
            tag.putInt("LuxCapacity", luxCapacity);
            tag.putInt("LuxAllocated", luxAllocated);
            tag.putBoolean("Overloaded", overloaded);
            tag.putLong("LastLuxSampleGameTime", lastLuxSampleGameTime);
            int[] history = new int[luxHistory.size()];
            for (int i = 0; i < history.length; i++) {
                history[i] = luxHistory.get(i);
            }
            tag.putIntArray("LuxHistory", history);
            ListTag nodeList = new ListTag();
            for (DimensionalNodePos pos : nodes) {
                CompoundTag nodeTag = new CompoundTag();
                DimensionalNodePos.writePos(nodeTag, "", pos);
                nodeList.add(nodeTag);
            }
            tag.put("Nodes", nodeList);
            ListTag colours = new ListTag();
            for (Map.Entry<DyeColor, Integer> entry : colourCharges.entrySet()) {
                CompoundTag colourTag = new CompoundTag();
                colourTag.putString("Colour", entry.getKey().getName());
                colourTag.putInt("Count", entry.getValue());
                colours.add(colourTag);
            }
            tag.put("Colours", colours);
            ListTag inboxes = new ListTag();
            for (Map.Entry<GlareAddress, List<GlareMessage>> entry : telemetryInboxes.entrySet()) {
                CompoundTag inboxTag = new CompoundTag();
                inboxTag.put("Address", entry.getKey().save());
                ListTag messages = new ListTag();
                for (GlareMessage message : entry.getValue()) {
                    messages.add(message.save());
                }
                inboxTag.put("Messages", messages);
                inboxes.add(inboxTag);
            }
            tag.put("TelemetryInboxes", inboxes);
            return tag;
        }

        static NetworkRecord load(CompoundTag tag) {
            NetworkRecord record = new NetworkRecord(tag.hasUUID("Id") ? tag.getUUID("Id") : UUID.randomUUID());
            record.luxCapacity = tag.getInt("LuxCapacity");
            record.luxAllocated = tag.getInt("LuxAllocated");
            record.overloaded = tag.getBoolean("Overloaded");
            record.lastLuxSampleGameTime = tag.contains("LastLuxSampleGameTime", Tag.TAG_LONG)
                    ? tag.getLong("LastLuxSampleGameTime") : Long.MIN_VALUE;
            for (int sample : tag.getIntArray("LuxHistory")) {
                record.luxHistory.add(Math.max(0, sample));
            }
            ListTag nodeList = tag.getList("Nodes", Tag.TAG_COMPOUND);
            for (int i = 0; i < nodeList.size(); i++) {
                DimensionalNodePos.readPos(nodeList.getCompound(i), "").ifPresent(record.nodes::add);
            }
            ListTag colours = tag.getList("Colours", Tag.TAG_COMPOUND);
            for (int i = 0; i < colours.size(); i++) {
                CompoundTag colourTag = colours.getCompound(i);
                record.colourCharges.put(DyeColor.byName(colourTag.getString("Colour"), DyeColor.WHITE), colourTag.getInt("Count"));
            }
            ListTag inboxes = tag.getList("TelemetryInboxes", Tag.TAG_COMPOUND);
            for (int i = 0; i < inboxes.size(); i++) {
                CompoundTag inboxTag = inboxes.getCompound(i);
                GlareAddress address = GlareAddress.load(inboxTag.getCompound("Address"));
                List<GlareMessage> messages = new ArrayList<>();
                ListTag messageList = inboxTag.getList("Messages", Tag.TAG_COMPOUND);
                for (int messageIndex = 0; messageIndex < messageList.size(); messageIndex++) {
                    messages.add(GlareMessage.load(messageList.getCompound(messageIndex)));
                }
                record.telemetryInboxes.put(address, messages);
            }
            return record;
        }
    }
}
