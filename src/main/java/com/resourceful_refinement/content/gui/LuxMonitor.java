package com.resourceful_refinement.content.gui;

import com.resourceful_refinement.content.glare.GlareOperationStatus;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.function.Supplier;

public class LuxMonitor {
    private static final int PANEL = 0xFF1A1A1A;
    private static final int TEXT = 0xFF9A9A9A;
    private static final int LUX_TEXT = 0xFF18BDEB;
    private static final int ONLINE = 0xFF3FC96C;
    private static final int OFFLINE = 0xFF9A9A9A;
    private static final int OVERLOADED = 0xFFE11B22;

    private int x;
    private int y;
    private int width;
    private int height;
    private final Supplier<? extends GlareNetworkGuiData> data;
    private final LuxHistoryGraph graph;

    public LuxMonitor(int x, int y, int width, int height, Supplier<? extends GlareNetworkGuiData> data) {
        this.data = data;
        this.graph = new LuxHistoryGraph(x, y, width, height, data);
        setBounds(x, y, width, height);
    }

    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = Math.max(64, width);
        this.height = Math.max(42, height);
        int footerHeight = Math.min(22, Math.max(14, this.height / 5));
        graph.setBounds(this.x + 4, this.y + 4, this.width - 44, this.height - footerHeight - 8);
    }

    public void render(GuiGraphics graphics, Font font) {
        GlareNetworkGuiData snapshot = data.get();
        if (snapshot == null) {
            snapshot = GlareNetworkSnapshot.EMPTY;
        }
        graphics.fill(x, y, x + width, y + height, PANEL);
        graph.render(graphics);

        int footerY = y + height - 16;
        graphics.drawString(font, Component.literal("Current:"), x + 6, footerY, TEXT, false);
        graphics.drawString(font, twoDigits(snapshot.currentLux()), x + 74, footerY, LUX_TEXT, false);
        graphics.drawString(font, Component.literal("Max:"), x + width / 2 - 70, footerY, TEXT, false);
        graphics.drawString(font, Integer.toString(snapshot.maxLux()), x + width / 2 - 26, footerY, LUX_TEXT, false);
        graphics.drawString(font, Component.literal("Status:"), x + width - 134, footerY, TEXT, false);
        graphics.drawString(font, statusText(snapshot), x + width - 80, footerY, statusColor(snapshot), false);
        graphics.drawString(font, Integer.toString(snapshot.maxLux()), x + width - 28, y + 8, LUX_TEXT, false);
    }

    private static Component twoDigits(int value) {
        return Component.literal(String.format(Locale.ROOT, "%02d", Math.max(0, value)));
    }

    private static Component statusText(GlareNetworkGuiData data) {
        if (data.overloaded()) {
            return Component.literal("Overloaded");
        }
        GlareOperationStatus status = data.status();
        String text = status == null ? "Offline" : status.name().toLowerCase(Locale.ROOT);
        return Component.literal(text.substring(0, 1).toUpperCase(Locale.ROOT) + text.substring(1));
    }

    private static int statusColor(GlareNetworkGuiData data) {
        if (data.overloaded()) {
            return OVERLOADED;
        }
        return data.status() == GlareOperationStatus.ONLINE ? ONLINE : OFFLINE;
    }
}
