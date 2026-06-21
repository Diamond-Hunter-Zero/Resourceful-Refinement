package com.resourceful_refinement.content.glare.terminal;

/** Auto-Send deliberately reuses the compose editor without a manual Send command. */
public class AutoSendTelemetryTerminalPage extends ComposeTelemetryTerminalPage {
    public AutoSendTelemetryTerminalPage(TelemetryTerminalScreen screen) {
        super(screen, true, 0);
    }
}
