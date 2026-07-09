package com.resourceful_refinement.content.glare.remote;

/** Shared persistent lifecycle for remote machines. Paused ticks leave progress untouched. */
public enum RemoteMachineState {
    IDLE,
    CHARGING,
    COOLDOWN
}
