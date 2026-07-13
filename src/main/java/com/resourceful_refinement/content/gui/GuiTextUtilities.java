package com.resourceful_refinement.content.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Shared helpers for fitting GUI text into fixed-width controls without truncating content.
 */
public final class GuiTextUtilities {
    private GuiTextUtilities() {}

    public static void drawFittedString(GuiGraphics graphics, Font font, Component text, int x, int y, int maxWidth,
                                        int color, boolean shadow) {
        drawFittedString(graphics, font, text.getString(), x, y, maxWidth, color, shadow);
    }

    public static void drawFittedString(GuiGraphics graphics, Font font, String text, int x, int y, int maxWidth,
                                        int color, boolean shadow) {
        if (text == null || text.isEmpty() || maxWidth <= 0) {
            return;
        }
        int textWidth = font.width(text);
        if (textWidth <= maxWidth) {
            graphics.drawString(font, text, x, y, color, shadow);
            return;
        }

        float scale = maxWidth / (float) textWidth;
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(scale, scale, 1.0f);
        graphics.drawString(font, text, 0, 0, color, shadow);
        graphics.pose().popPose();
    }
}
