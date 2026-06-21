package com.resourceful_refinement.content.glare.remote;

public enum RemoteEntanglementMode {
    DEPOT_SEND,
    DEPOT_RECEIVE,
    TRANSPORTER_AUTO,
    TRANSPORTER_SEND_ONLY,
    TRANSPORTER_RECEIVE_ONLY;

    public boolean canSend() {
        return this == DEPOT_SEND || this == TRANSPORTER_AUTO || this == TRANSPORTER_SEND_ONLY;
    }

    public boolean canReceive() {
        return this == DEPOT_RECEIVE || this == TRANSPORTER_AUTO || this == TRANSPORTER_RECEIVE_ONLY;
    }
}
