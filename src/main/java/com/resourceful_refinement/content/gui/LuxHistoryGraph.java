package com.resourceful_refinement.content.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import java.util.Arrays;
import java.util.function.Supplier;

public class LuxHistoryGraph {
    private static final int BACKGROUND = 0xFF000000;
    private static final int FRAME = 0xFF0A7EA1;
    private static final int BAR = 0xFF15A4D8;
    private static final int OVERLOAD_BAR = 0xFFE11B22;
    private static final int GRID = 0x5520C6F4;

    private int x;
    private int y;
    private int width;
    private int height;
    private final Supplier<? extends GlareNetworkGuiData> data;

    public LuxHistoryGraph(int x, int y, int width, int height, Supplier<? extends GlareNetworkGuiData> data) {
        this.data = data;
        setBounds(x, y, width, height);
    }

    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = Math.max(1, width);
        this.height = Math.max(1, height);
    }

    public void render(GuiGraphics graphics) {
        render(graphics, x, y, width, height, data.get());
    }

    public static void render(GuiGraphics graphics, int x, int y, int width, int height, GlareNetworkGuiData data) {
        int safeWidth = Math.max(1, width);
        int safeHeight = Math.max(1, height);
        graphics.fill(x, y, x + safeWidth, y + safeHeight, BACKGROUND);

        int innerX = x + Math.max(2, safeWidth / 40);
        int innerY = y + Math.max(2, safeHeight / 24);
        int innerWidth = Math.max(1, safeWidth - (innerX - x) * 2);
        int innerHeight = Math.max(1, safeHeight - (innerY - y) * 2);
        int baselineY = innerY + innerHeight - 1;

        graphics.fill(innerX, innerY, innerX + innerWidth, innerY + 1, GRID);
        graphics.fill(innerX, baselineY, innerX + innerWidth, baselineY + 1, FRAME);

        int[] samples = samples(data);
        int maxValue = Math.max(1, data == null ? 0 : data.maxLux());
        for (int sample : samples) {
            maxValue = Math.max(maxValue, sample);
        }

        if (samples.length == 0) {
            return;
        }

        int gap = samples.length > 1 && innerWidth >= samples.length * 3 ? 2 : 1;
        int totalGap = gap * Math.max(0, samples.length - 1);
        int barWidth = Math.max(1, (innerWidth - totalGap) / samples.length);
        int usedWidth = samples.length * barWidth + totalGap;
        int startX = innerX + Math.max(0, innerWidth - usedWidth);
        int barColor = data != null && data.overloaded() ? OVERLOAD_BAR : BAR;

        for (int i = 0; i < samples.length; i++) {
            int value = Mth.clamp(samples[i], 0, maxValue);
            int barHeight = Math.max(value > 0 ? 1 : 0, (int) Math.ceil(value * (innerHeight - 2) / (double) maxValue));
            int barX = startX + i * (barWidth + gap);
            graphics.fill(barX, baselineY - barHeight, barX + barWidth, baselineY, barColor);
        }
    }

    private static int[] samples(GlareNetworkGuiData data) {
        if (data == null || !data.hasNetwork()) {
            return new int[0];
        }
        int[] history = data.luxHistory();
        if (history.length > 0) {
            return Arrays.stream(history).map(value -> Math.max(0, value)).toArray();
        }
        return new int[] { Math.max(0, data.currentLux()) };
    }
}
