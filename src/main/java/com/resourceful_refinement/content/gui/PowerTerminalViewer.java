package com.resourceful_refinement.content.gui;

import net.minecraft.core.BlockPos;

/**
 * Implemented by any container menu whose screen shows a {@link PowerTerminal}. The server re-syncs the viewed
 * node whenever its network records a new Lux history sample, so the graph stays live while the screen is open.
 */
public interface PowerTerminalViewer {
    /** Position of the GLARE node whose network the terminal displays. */
    BlockPos getPowerTerminalPos();
}
