package com.resourceful_refinement.content.glare.remote;

import net.minecraft.util.StringRepresentable;

public enum TransporterMode implements StringRepresentable {
    AUTO("auto", RemoteEntanglementMode.TRANSPORTER_AUTO),
    SEND_ONLY("send_only", RemoteEntanglementMode.TRANSPORTER_SEND_ONLY),
    RECEIVE_ONLY("receive_only", RemoteEntanglementMode.TRANSPORTER_RECEIVE_ONLY);

    private final String serializedName;
    private final RemoteEntanglementMode remoteMode;

    TransporterMode(String serializedName, RemoteEntanglementMode remoteMode) {
        this.serializedName = serializedName;
        this.remoteMode = remoteMode;
    }

    @Override public String getSerializedName() { return serializedName; }
    public RemoteEntanglementMode remoteMode() { return remoteMode; }

    public TransporterMode next() {
        TransporterMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
