package com.resourceful_refinement.content.glare.remote;

import com.resourceful_refinement.content.glare.GlareAddress;

/**
 * Persisted identity of an endpoint. Implementations must only expose state that remains meaningful while unloaded;
 * live inventories, fluid tanks and entities are intentionally absent from this contract.
 */
public interface IRemoteEntanglementEndpoint {
    RemoteEndpointKind getRemoteEndpointKind();

    GlareAddress getRemoteAddress();

    RemoteEntanglementMode getRemoteMode();

    default boolean isRemoteEndpointAssembled() {
        return true;
    }
}
