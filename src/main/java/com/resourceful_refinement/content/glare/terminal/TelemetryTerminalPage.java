package com.resourceful_refinement.content.glare.terminal;

import net.minecraft.client.gui.GuiGraphics;

public interface TelemetryTerminalPage {
    void init();
    default void removed() {}
    default void tick() {}
    default void snapshotUpdated(TelemetryTerminalSnapshot snapshot) {}
    /** Drawn straight after the panel art, beneath widgets. */
    default void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {}
    /** Drawn above widgets. */
    default void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {}
    default boolean mouseClicked(double mouseX, double mouseY, int button) { return false; }
    default boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) { return false; }
    default void flush() {}
}
