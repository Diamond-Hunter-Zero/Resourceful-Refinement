package com.resourceful_refinement.content.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Small reusable client-side renderer for research-locked recipe outputs.
 */
public class LockedRecipeIndicator {
    public static final Component DEFAULT_LABEL = Component.literal("Recipe Locked");
    public static final int SLOT_SIZE = 18;
    public static final int DEFAULT_HEIGHT = 18;

    private static final int MIN_WIDTH = 82;
    private static final int HORIZONTAL_PADDING = 6;
    private static final int ICON_SIZE = 10;
    private static final int PANEL = 0xE61A1A1A;
    private static final int BORDER = 0xFF8E5A5A;
    private static final int ICON_BACKING = 0xFF2C1616;
    private static final int ICON = 0xFFE8D8B8;
    private static final int TEXT = 0xFFE8D8B8;

    private int x;
    private int y;
    private Component label;

    public LockedRecipeIndicator(int x, int y) {
        this(x, y, DEFAULT_LABEL);
    }

    public LockedRecipeIndicator(int x, int y, Component label) {
        this.x = x;
        this.y = y;
        this.label = label == null ? DEFAULT_LABEL : label;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setLabel(Component label) {
        this.label = label == null ? DEFAULT_LABEL : label;
    }

    public void render(GuiGraphics graphics, Font font) {
        render(graphics, font, x, y, label);
    }

    public static void renderBelowSlot(GuiGraphics graphics, Font font, int slotX, int slotY, int screenWidth) {
        int width = width(font, DEFAULT_LABEL);
        int x = clamp(slotX + SLOT_SIZE / 2 - width / 2, 2, Math.max(2, screenWidth - width - 2));
        render(graphics, font, x, slotY + SLOT_SIZE + 2, DEFAULT_LABEL);
    }

    public static void render(GuiGraphics graphics, Font font, int x, int y) {
        render(graphics, font, x, y, DEFAULT_LABEL);
    }

    public static void render(GuiGraphics graphics, Font font, int x, int y, Component label) {
        Component text = label == null ? DEFAULT_LABEL : label;
        int width = width(font, text);
        graphics.fill(x, y, x + width, y + DEFAULT_HEIGHT, PANEL);
        graphics.renderOutline(x, y, width, DEFAULT_HEIGHT, BORDER);

        int iconX = x + HORIZONTAL_PADDING;
        int iconY = y + 4;
        graphics.fill(iconX - 2, iconY - 2, iconX + ICON_SIZE + 2, iconY + ICON_SIZE + 2, ICON_BACKING);
        drawLockIcon(graphics, iconX, iconY);
        graphics.drawString(font, text, iconX + ICON_SIZE + 6, y + 5, TEXT, false);
    }

    public static int width(Font font, Component label) {
        Component text = label == null ? DEFAULT_LABEL : label;
        return Math.max(MIN_WIDTH, HORIZONTAL_PADDING * 2 + ICON_SIZE + 6 + font.width(text));
    }

    private static void drawLockIcon(GuiGraphics graphics, int x, int y) {
        graphics.fill(x + 2, y + 4, x + 8, y + 10, ICON);
        graphics.fill(x + 3, y + 2, x + 7, y + 4, ICON);
        graphics.fill(x + 2, y + 3, x + 4, y + 6, ICON);
        graphics.fill(x + 6, y + 3, x + 8, y + 6, ICON);
        graphics.fill(x + 4, y + 6, x + 6, y + 8, ICON_BACKING);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
