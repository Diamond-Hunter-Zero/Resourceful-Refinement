package com.resourceful_refinement.api.research;

import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Result returned by research grant/revoke API calls.
 * Changed nodes contains every node whose persisted unlock state was mutated by the call.
 */
public record UnlockResult(Action action, Status status, Scope scope, ResourceLocation requestedNode,
                           List<ResourceLocation> changedNodes, Optional<UUID> playerId) {
    public UnlockResult {
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(scope, "scope");
        Objects.requireNonNull(requestedNode, "requestedNode");
        changedNodes = List.copyOf(Objects.requireNonNull(changedNodes, "changedNodes"));
        playerId = Objects.requireNonNull(playerId, "playerId");
    }

    public static UnlockResult disabled(Action action, Scope scope, ResourceLocation requestedNode, Optional<UUID> playerId) {
        return new UnlockResult(action, Status.DISABLED, scope, requestedNode, List.of(), playerId);
    }

    public static UnlockResult unknownNode(Action action, Scope scope, ResourceLocation requestedNode, Optional<UUID> playerId) {
        return new UnlockResult(action, Status.UNKNOWN_NODE, scope, requestedNode, List.of(), playerId);
    }

    public static UnlockResult unchanged(Action action, Scope scope, ResourceLocation requestedNode, Optional<UUID> playerId) {
        Status status = action == Action.GRANT ? Status.ALREADY_UNLOCKED : Status.ALREADY_REVOKED;
        return new UnlockResult(action, status, scope, requestedNode, List.of(), playerId);
    }

    public static UnlockResult changed(Action action, Scope scope, ResourceLocation requestedNode,
                                       List<ResourceLocation> changedNodes, Optional<UUID> playerId) {
        Status status = action == Action.GRANT ? Status.UNLOCKED : Status.REVOKED;
        return new UnlockResult(action, status, scope, requestedNode, changedNodes, playerId);
    }

    public boolean changed() {
        return !changedNodes.isEmpty();
    }

    public enum Action {
        GRANT,
        REVOKE
    }

    public enum Scope {
        PLAYER,
        GLOBAL
    }

    public enum Status {
        DISABLED,
        UNKNOWN_NODE,
        ALREADY_UNLOCKED,
        UNLOCKED,
        ALREADY_REVOKED,
        REVOKED
    }
}
