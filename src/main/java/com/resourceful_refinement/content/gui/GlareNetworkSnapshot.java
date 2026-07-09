package com.resourceful_refinement.content.gui;

import com.resourceful_refinement.content.glare.GlareOperationStatus;

import java.util.Arrays;

public record GlareNetworkSnapshot(boolean hasNetwork, int currentLux, int maxLux, int[] luxHistory,
        GlareOperationStatus status, boolean overloaded) implements GlareNetworkGuiData {
    public static final GlareNetworkSnapshot EMPTY = new GlareNetworkSnapshot(false, 0, 0, new int[0],
            GlareOperationStatus.OFFLINE, false);

    public GlareNetworkSnapshot {
        currentLux = Math.max(0, currentLux);
        maxLux = Math.max(0, maxLux);
        luxHistory = luxHistory == null ? new int[0] : Arrays.copyOf(luxHistory, luxHistory.length);
        status = status == null ? GlareOperationStatus.OFFLINE : status;
    }

    @Override
    public int[] luxHistory() {
        return Arrays.copyOf(luxHistory, luxHistory.length);
    }

    public static GlareNetworkSnapshot of(boolean hasNetwork, int currentLux, int maxLux, int[] luxHistory,
            GlareOperationStatus status, boolean overloaded) {
        return new GlareNetworkSnapshot(hasNetwork, currentLux, maxLux, luxHistory, status, overloaded);
    }
}
