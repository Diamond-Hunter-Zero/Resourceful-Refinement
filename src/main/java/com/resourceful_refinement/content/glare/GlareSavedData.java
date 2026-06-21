package com.resourceful_refinement.content.glare;

import com.resourceful_refinement.ResourcefulRefinementMain;
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
import net.minecraft.world.item.DyeColor;
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

    private final Map<GlareNodePos, NodeRecord> nodes = new HashMap<>();
    private final Set<GlareLink> links = new LinkedHashSet<>();
    private final Map<GlareLink, LinkValidity> linkValidity = new HashMap<>();
    private final Map<UUID, NetworkRecord> networks = new HashMap<>();
    private final Map<Long, TelemetrySubscriber> telemetrySubscribers = new HashMap<>();
    private long nextTelemetrySubscriberId;

    public static SavedData.Factory<GlareSavedData> factory() {
        return new SavedData.Factory<>(GlareSavedData::new, GlareSavedData::load);
    }

    public static GlareSavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(factory(), DATA_NAME);
    }

    public static GlareSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(factory(), DATA_NAME);
    }

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
            writePos(linkTag, "A", link.a());
            writePos(linkTag, "B", link.b());
            linkTag.putString("Validity", getLinkValidity(link).name());
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
        links.clear();
        linkValidity.clear();
        networks.clear();

        ListTag nodeList = tag.getList("Nodes", Tag.TAG_COMPOUND);
        for (int i = 0; i < nodeList.size(); i++) {
            NodeRecord node = NodeRecord.load(nodeList.getCompound(i));
            nodes.put(node.pos, node);
        }

        ListTag linkList = tag.getList("Links", Tag.TAG_COMPOUND);
        for (int i = 0; i < linkList.size(); i++) {
            CompoundTag linkTag = linkList.getCompound(i);
            Optional<GlareNodePos> a = readPos(linkTag, "A");
            Optional<GlareNodePos> b = readPos(linkTag, "B");
            if (a.isPresent() && b.isPresent() && !a.get().equals(b.get())) {
                GlareLink link = new GlareLink(a.get(), b.get());
                links.add(link);
                try {
                    linkValidity.put(link, LinkValidity.valueOf(linkTag.getString("Validity")));
                } catch (IllegalArgumentException ignored) {
                    linkValidity.put(link, LinkValidity.UNKNOWN);
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

    public Optional<NodeRecord> getNode(GlareNodePos pos) {
        return Optional.ofNullable(nodes.get(pos));
    }

    public Optional<NetworkRecord> getNetwork(UUID id) {
        return Optional.ofNullable(networks.get(id));
    }

    Optional<UUID> getNetworkId(GlareNodePos node) {
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

    long subscribeTelemetry(GlareNodePos owner, GlareAddress address, Consumer<TelemetryService.InboxUpdate> listener) {
        long id = nextTelemetrySubscriberId++;
        telemetrySubscribers.put(id, new TelemetrySubscriber(owner, address, listener));
        return id;
    }

    void unsubscribeTelemetry(long id) {
        telemetrySubscribers.remove(id);
    }

    public List<GlareLink> getLinksFor(GlareNodePos pos) {
        return links.stream().filter(link -> link.contains(pos)).toList();
    }

    public List<GlareNodePos> getNeighbours(GlareNodePos pos) {
        return getLinksFor(pos).stream().map(link -> link.other(pos)).toList();
    }

    private List<GlareNodePos> getActiveNeighbours(GlareNodePos pos) {
        return getLinksFor(pos).stream()
                .filter(this::isActiveNetworkLink)
                .map(link -> link.other(pos))
                .toList();
    }

    public LinkValidity getLinkValidity(GlareLink link) {
        return linkValidity.getOrDefault(link, LinkValidity.UNKNOWN);
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
                .filter(link -> link.a().levelKey().equals(level.dimension()) && link.b().levelKey().equals(level.dimension()))
                .filter(link -> level.isLoaded(link.a().pos()) || level.isLoaded(link.b().pos()))
                .filter(link -> isCloseEnoughToRender(player, link, syncDistanceSq))
                .map(link -> new LinkRenderRecord(link.a(), link.b(), getLinkValidity(link)))
                .toList();
    }

    public void registerNode(ServerLevel level, IGlareNode glareNode) {
        GlareNodePos pos = glareNode.getGlareNodePos();
        NodeRecord record = nodes.computeIfAbsent(pos, NodeRecord::new);
        record.maxLinks = Math.max(0, glareNode.getMaxGlareLinks());
        record.loaded = true;
        record.removed = false;
        updateLuxState(record, glareNode);
        setDirtyAndRebuild();
        notifyRecordNetworkOrNode(level, record);
    }

    public void markLoaded(ServerLevel level, GlareNodePos pos) {
        NodeRecord record = nodes.get(pos);
        if (record != null) {
            record.loaded = true;
            record.removed = false;
            setDirty();
            notifyLoadedNode(level, record);
        }
    }

    public void unregisterLoadedNode(GlareNodePos pos) {
        NodeRecord removed = nodes.remove(pos);
        if (removed != null) {
            removeLinksIf(link -> link.contains(pos));
            setDirtyAndRebuild();
        }
    }

    public void unregisterLoadedNode(ServerLevel level, GlareNodePos pos) {
        NodeRecord removed = nodes.remove(pos);
        if (removed == null) {
            return;
        }

        Set<GlareNodePos> affected = new LinkedHashSet<>();
        affected.add(pos);
        for (GlareLink link : getLinksFor(pos)) {
            affected.add(link.other(pos));
        }
        removeLinksIf(link -> link.contains(pos));
        setDirtyAndRebuild();
        notifyLoadedEndpoints(level, affected);
    }

    public void updateNodeState(ServerLevel level, IGlareNode glareNode) {
        GlareNodePos pos = glareNode.getGlareNodePos();
        NodeRecord record = nodes.computeIfAbsent(pos, NodeRecord::new);
        record.maxLinks = Math.max(0, glareNode.getMaxGlareLinks());
        record.loaded = true;
        record.removed = false;
        updateLuxState(record, glareNode);
        setDirtyAndRebuild();
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

    public LinkResult tryAddLink(ServerLevel level, GlareNodePos a, GlareNodePos b) {
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
        boolean canValidate = GlareLineOfSight.canValidate(level.getServer(), a, b);
        boolean hasLineOfSight = !canValidate || GlareLineOfSight.hasLineOfSight(level.getServer(), a, b);
        if (canValidate && !hasLineOfSight) {
            return LinkResult.FAIL_LINE_OF_SIGHT;
        }

        Set<GlareNodePos> affected = new LinkedHashSet<>();
        affected.add(a);
        affected.add(b);
        affected.addAll(evictOldestIfFull(first));
        affected.addAll(evictOldestIfFull(second));
        links.add(link);
        linkValidity.put(link, canValidate ? LinkValidity.VALID : LinkValidity.UNKNOWN);
        setDirtyAndRebuild();
        notifyLoadedEndpoints(level, affected);
        NodeRecord rebuiltFirst = nodes.get(a);
        if (rebuiltFirst != null && rebuiltFirst.networkId != null) {
            NetworkRecord network = networks.get(rebuiltFirst.networkId);
            if (network != null) {
                notifyLoadedEndpoints(level, network.nodes);
            }
        }
        return LinkResult.CREATED;
    }

    private Set<GlareNodePos> evictOldestIfFull(NodeRecord record) {
        Set<GlareNodePos> affected = new LinkedHashSet<>();
        while (record.maxLinks >= 0 && getLinksFor(record.pos).size() >= record.maxLinks && record.maxLinks > 0) {
            GlareLink oldest = getLinksFor(record.pos).get(0);
            affected.add(oldest.a());
            affected.add(oldest.b());
            links.remove(oldest);
            linkValidity.remove(oldest);
        }
        return affected;
    }

    public boolean removeLink(GlareNodePos a, GlareNodePos b) {
        GlareLink link = new GlareLink(a, b);
        boolean removed = links.remove(link);
        if (removed) {
            linkValidity.remove(link);
            setDirtyAndRebuild();
        }
        return removed;
    }

    public void validateLoadedLinks(ServerLevel level) {
        boolean changed = false;
        Set<GlareNodePos> affected = new LinkedHashSet<>();
        for (GlareLink link : links) {
            if (!link.a().levelKey().equals(level.dimension()) || !link.b().levelKey().equals(level.dimension())) {
                continue;
            }
            if (!GlareLineOfSight.canValidate(level.getServer(), link.a(), link.b())) {
                continue;
            }
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
        if (changed) {
            setDirtyAndRebuild();
            for (GlareNodePos pos : new ArrayList<>(affected)) {
                affected.addAll(getNetworkNodesFor(pos));
            }
            notifyLoadedEndpoints(level, affected);
        }
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
        for (GlareNodePos pos : network.nodes) {
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

    public void reconcileLoadedChunk(ServerLevel level, BlockPos chunkOrigin) {
        int minX = chunkOrigin.getX();
        int minZ = chunkOrigin.getZ();
        int maxX = minX + 15;
        int maxZ = minZ + 15;
        boolean changed = false;
        Set<GlareNodePos> affected = new LinkedHashSet<>();
        for (NodeRecord record : new ArrayList<>(nodes.values())) {
            BlockPos pos = record.pos.pos();
            if (!record.pos.levelKey().equals(level.dimension()) || pos.getX() < minX || pos.getX() > maxX || pos.getZ() < minZ || pos.getZ() > maxZ) {
                continue;
            }
            if (!level.isLoaded(pos)) {
                continue;
            }
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof IGlareNode glareNode) {
                record.loaded = true;
                record.removed = false;
                record.maxLinks = glareNode.getMaxGlareLinks();
                updateLuxState(record, glareNode);
                changed = true;
            } else {
                affected.add(record.pos);
                for (GlareLink link : getLinksFor(record.pos)) {
                    affected.add(link.other(record.pos));
                }
                nodes.remove(record.pos);
                removeLinksIf(link -> link.contains(record.pos));
                changed = true;
            }
        }
        if (changed) {
            setDirtyAndRebuild();
            notifyLoadedEndpoints(level, affected);
        }
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

        Set<GlareNodePos> visited = new HashSet<>();
        Set<UUID> claimedNetworkIds = new HashSet<>();
        for (GlareNodePos start : nodes.keySet()) {
            if (!visited.add(start)) {
                continue;
            }
            Set<GlareNodePos> component = new LinkedHashSet<>();
            ArrayDeque<GlareNodePos> queue = new ArrayDeque<>();
            queue.add(start);
            while (!queue.isEmpty()) {
                GlareNodePos current = queue.removeFirst();
                component.add(current);
                for (GlareNodePos neighbour : getActiveNeighbours(current)) {
                    if (nodes.containsKey(neighbour) && visited.add(neighbour)) {
                        queue.addLast(neighbour);
                    }
                }
            }

            UUID id = chooseNetworkId(component, oldNetworks, claimedNetworkIds);
            claimedNetworkIds.add(id);
            boolean preserveOverload = shouldPreserveOverload(component, oldNetworks);
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
            for (GlareNodePos current : component) {
                NodeRecord node = nodes.get(current);
                if (node != null) {
                    node.networkId = id;
                    applyLux(network, node);
                    if (node.telemetryAddress.isComplete()) {
                        network.registeredTelemetryAddresses.add(node.telemetryAddress);
                    }
                }
            }
            if (network.luxAllocated > network.luxCapacity || network.overloaded) {
                markNetworkOverloaded(network);
            } else {
                markNetworkOnline(network);
            }
            networks.put(id, network);
        }
        notifyTelemetryRebuildChanges(oldNetworks);
    }

    private static boolean shouldPreserveOverload(Set<GlareNodePos> component, Map<UUID, NetworkRecord> oldNetworks) {
        for (NetworkRecord oldNetwork : oldNetworks.values()) {
            if (!oldNetwork.overloaded || oldNetwork.nodes.size() <= 1) {
                continue;
            }
            for (GlareNodePos pos : oldNetwork.nodes) {
                if (component.contains(pos)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static UUID chooseNetworkId(Set<GlareNodePos> component, Map<UUID, NetworkRecord> oldNetworks, Set<UUID> claimedIds) {
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

    private void notifyTelemetryRebuildChanges(Map<UUID, NetworkRecord> oldNetworks) {
        for (Map.Entry<Long, TelemetrySubscriber> entry : List.copyOf(telemetrySubscribers.entrySet())) {
            TelemetrySubscriber subscriber = entry.getValue();
            NodeRecord owner = nodes.get(subscriber.owner);
            if (owner == null || owner.networkId == null) {
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

    private static int overlapCount(Set<GlareNodePos> component, Set<GlareNodePos> previousNodes) {
        int count = 0;
        for (GlareNodePos pos : previousNodes) {
            if (component.contains(pos)) {
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

    private void markNetworkOverloaded(NetworkRecord network) {
        network.overloaded = true;
        for (GlareNodePos pos : network.nodes) {
            NodeRecord node = nodes.get(pos);
            if (node != null && node.receiver) {
                node.status = GlareOperationStatus.OVERLOADED;
            }
        }
    }

    private void markNetworkOnline(NetworkRecord network) {
        network.overloaded = false;
        for (GlareNodePos pos : network.nodes) {
            NodeRecord node = nodes.get(pos);
            if (node != null && node.receiver && node.status == GlareOperationStatus.OVERLOADED) {
                node.status = GlareOperationStatus.ONLINE;
            }
        }
    }

    private void setDirtyAndRebuild() {
        rebuildNetworks();
        setDirty();
    }

    private void notifyLoadedEndpoint(ServerLevel level, GlareNodePos pos) {
        if (!pos.levelKey().equals(level.dimension()) || !level.isLoaded(pos.pos())) {
            return;
        }
        BlockEntity be = level.getBlockEntity(pos.pos());
        if (be instanceof IGlareNode glareNode) {
            NodeRecord record = nodes.get(pos);
            if (record != null && record.receiver && be instanceof IGlareReceiver receiver) {
                receiver.applyGlareOperationStatusFromNetwork(record.status);
            }
            if (record != null && record.networkId != null) {
                glareNode.onGlareNetworkChanged(level, record.networkId);
            }
            glareNode.onGlareLinksChanged(level);
        }
    }

    private void notifyLoadedEndpoints(ServerLevel level, Collection<GlareNodePos> positions) {
        for (GlareNodePos pos : positions) {
            notifyLoadedEndpoint(level, pos);
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

    private Set<GlareNodePos> getNetworkNodesFor(GlareNodePos pos) {
        Set<GlareNodePos> positions = new LinkedHashSet<>();
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
                linkValidity.remove(link);
                removed = true;
            }
        }
        return removed;
    }

    private static boolean isCloseEnoughToRender(net.minecraft.server.level.ServerPlayer player, GlareLink link, double syncDistanceSq) {
        return player.distanceToSqr(link.a().pos().getX() + 0.5D, link.a().pos().getY() + 0.5D, link.a().pos().getZ() + 0.5D) <= syncDistanceSq
                || player.distanceToSqr(link.b().pos().getX() + 0.5D, link.b().pos().getY() + 0.5D, link.b().pos().getZ() + 0.5D) <= syncDistanceSq;
    }

    private static void writePos(CompoundTag tag, String prefix, GlareNodePos pos) {
        tag.putString(prefix + "Level", pos.levelKey().location().toString());
        tag.putInt(prefix + "X", pos.pos().getX());
        tag.putInt(prefix + "Y", pos.pos().getY());
        tag.putInt(prefix + "Z", pos.pos().getZ());
    }

    private static Optional<GlareNodePos> readPos(CompoundTag tag, String prefix) {
        if (!tag.contains(prefix + "Level")) {
            return Optional.empty();
        }
        ResourceLocation levelId = ResourceLocation.tryParse(tag.getString(prefix + "Level"));
        if (levelId == null) {
            return Optional.empty();
        }
        ResourceKey<Level> levelKey = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, levelId);
        return Optional.of(new GlareNodePos(levelKey, new BlockPos(tag.getInt(prefix + "X"), tag.getInt(prefix + "Y"), tag.getInt(prefix + "Z"))));
    }

    public enum LinkResult {
        CREATED,
        ALREADY_LINKED,
        FAIL_SAME_NODE,
        FAIL_MISSING_NODE,
        FAIL_CROSS_DIMENSION,
        FAIL_LINE_OF_SIGHT,
        FAIL_LINK_LIMIT
    }

    public enum LinkValidity {
        UNKNOWN,
        VALID,
        BLOCKED
    }

    public record LinkRenderRecord(GlareNodePos a, GlareNodePos b, LinkValidity validity) {}

    private record TelemetrySubscriber(GlareNodePos owner, GlareAddress address,
            Consumer<TelemetryService.InboxUpdate> listener) {}

    public static class NodeRecord {
        public final GlareNodePos pos;
        public int maxLinks = 0;
        public boolean loaded;
        public boolean removed;
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
        public final List<GlareNodePos> lastKnownLinks = new ArrayList<>();
        @Nullable
        public UUID networkId;

        NodeRecord(GlareNodePos pos) {
            this.pos = pos;
        }

        CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            writePos(tag, "", pos);
            tag.putInt("MaxLinks", maxLinks);
            tag.putBoolean("Loaded", loaded);
            tag.putBoolean("Removed", removed);
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
            for (GlareNodePos link : lastKnownLinks) {
                CompoundTag linkTag = new CompoundTag();
                writePos(linkTag, "", link);
                linkList.add(linkTag);
            }
            tag.put("LastKnownLinks", linkList);
            return tag;
        }

        static NodeRecord load(CompoundTag tag) {
            GlareNodePos pos = readPos(tag, "").orElseThrow();
            NodeRecord record = new NodeRecord(pos);
            record.maxLinks = tag.getInt("MaxLinks");
            record.loaded = tag.getBoolean("Loaded");
            record.removed = tag.getBoolean("Removed");
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
                readPos(linkList.getCompound(i), "").ifPresent(record.lastKnownLinks::add);
            }
            return record;
        }
    }

    public static class NetworkRecord {
        public final UUID id;
        public final Set<GlareNodePos> nodes = new LinkedHashSet<>();
        public final Map<DyeColor, Integer> colourCharges = new HashMap<>();
        public final Map<GlareAddress, List<GlareMessage>> telemetryInboxes = new HashMap<>();
        public final Set<GlareAddress> registeredTelemetryAddresses = new LinkedHashSet<>();
        public int luxCapacity;
        public int luxAllocated;
        public boolean overloaded;

        NetworkRecord(UUID id) {
            this.id = id;
        }

        NetworkRecord copyForRebuild(boolean preserveOverload) {
            NetworkRecord copy = new NetworkRecord(id);
            copy.overloaded = preserveOverload;
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
            ListTag nodeList = new ListTag();
            for (GlareNodePos pos : nodes) {
                CompoundTag nodeTag = new CompoundTag();
                writePos(nodeTag, "", pos);
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
            ListTag nodeList = tag.getList("Nodes", Tag.TAG_COMPOUND);
            for (int i = 0; i < nodeList.size(); i++) {
                readPos(nodeList.getCompound(i), "").ifPresent(record.nodes::add);
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
