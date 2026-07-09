package com.resourceful_refinement.content.gui;

import com.resourceful_refinement.content.glare.GlareOperationStatus;

public interface GlareNetworkGuiData {
    boolean hasNetwork();

    int currentLux();

    int maxLux();

    int[] luxHistory();

    GlareOperationStatus status();

    boolean overloaded();
}
