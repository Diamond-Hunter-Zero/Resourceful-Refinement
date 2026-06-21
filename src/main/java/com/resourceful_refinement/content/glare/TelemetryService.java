package com.resourceful_refinement.content.glare;

import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/** Server-side access to telemetry state owned and persisted by GLARE networks. */
public final class TelemetryService {
    public static final int MAX_MESSAGES = 16;

    private TelemetryService() {}

    public static SendResult send(ServerLevel level, UUID networkId, GlareAddress from, GlareAddress to, String body) {
        GlareMessage message = new GlareMessage(from, to, body, level.getGameTime());
        return GlareSavedData.get(level).sendTelemetry(networkId, message) ? SendResult.SENT : SendResult.NETWORK_MISSING;
    }

    public static SendResult sendFromNode(ServerLevel level, GlareNodePos senderNode, GlareAddress from,
            GlareAddress to, String body) {
        GlareSavedData data = GlareSavedData.get(level);
        Optional<UUID> networkId = data.getNetworkId(senderNode);
        if (networkId.isEmpty()) {
            return SendResult.NODE_MISSING;
        }
        GlareMessage message = new GlareMessage(from, to, body, level.getGameTime());
        return data.sendTelemetry(networkId.get(), message) ? SendResult.SENT : SendResult.NETWORK_MISSING;
    }

    public static List<GlareMessage> read(ServerLevel level, UUID networkId, GlareAddress address) {
        return GlareSavedData.get(level).readTelemetry(networkId, address);
    }

    public static boolean addressExists(ServerLevel level, UUID networkId, GlareAddress address) {
        return GlareSavedData.get(level).getNetwork(networkId)
                .map(network -> network.registeredTelemetryAddresses.contains(address)
                        || network.telemetryInboxes.containsKey(address))
                .orElse(false);
    }

    public static boolean addressExistsFromNode(ServerLevel level, GlareNodePos node, GlareAddress address) {
        GlareSavedData data = GlareSavedData.get(level);
        return data.getNetworkId(node)
                .flatMap(data::getNetwork)
                .map(network -> network.registeredTelemetryAddresses.contains(address)
                        || network.telemetryInboxes.containsKey(address))
                .orElse(false);
    }

    public static List<GlareMessage> readFromNode(ServerLevel level, GlareNodePos node, GlareAddress address) {
        GlareSavedData data = GlareSavedData.get(level);
        return data.getNetworkId(node).map(id -> data.readTelemetry(id, address)).orElseGet(List::of);
    }

    public static Optional<GlareMessage> discard(ServerLevel level, UUID networkId, GlareAddress address, UUID messageId) {
        return GlareSavedData.get(level).discardTelemetry(networkId, address, messageId);
    }

    public static Optional<GlareMessage> discardAt(ServerLevel level, UUID networkId, GlareAddress address, int index) {
        return GlareSavedData.get(level).discardTelemetryAt(networkId, address, index);
    }

    public static Optional<GlareMessage> discardFromNode(ServerLevel level, GlareNodePos node,
            GlareAddress address, UUID messageId) {
        GlareSavedData data = GlareSavedData.get(level);
        return data.getNetworkId(node)
                .flatMap(networkId -> data.discardTelemetry(networkId, address, messageId));
    }

    public static Subscription subscribe(ServerLevel level, GlareNodePos owner, GlareAddress address,
            Consumer<InboxUpdate> listener) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(address, "address");
        Objects.requireNonNull(listener, "listener");
        GlareSavedData data = GlareSavedData.get(level);
        long id = data.subscribeTelemetry(owner, address, listener);
        return new Subscription(data, id);
    }

    public enum SendResult {
        SENT,
        NODE_MISSING,
        NETWORK_MISSING
    }

    public enum Mutation {
        SENT,
        DISCARDED,
        NETWORK_REBUILT
    }

    public record InboxUpdate(UUID networkId, GlareAddress address, List<GlareMessage> messages,
            Mutation mutation, @Nullable GlareMessage changedMessage) {
        public InboxUpdate {
            messages = List.copyOf(messages);
        }
    }

    public static final class Subscription implements AutoCloseable {
        private GlareSavedData data;
        private final long id;

        private Subscription(GlareSavedData data, long id) {
            this.data = data;
            this.id = id;
        }

        @Override
        public void close() {
            if (data != null) {
                data.unsubscribeTelemetry(id);
                data = null;
            }
        }
    }
}
