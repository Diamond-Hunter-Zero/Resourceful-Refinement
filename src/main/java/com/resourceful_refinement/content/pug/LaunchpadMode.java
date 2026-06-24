package com.resourceful_refinement.content.pug;

public enum LaunchpadMode {
    SEND,
    RECEIVE;

    public boolean canSend() {
        return this == SEND;
    }

    public boolean canReceive() {
        return this == RECEIVE;
    }
}
