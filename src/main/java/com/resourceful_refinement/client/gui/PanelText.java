package com.resourceful_refinement.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

/**
 * Draws text at an arbitrary scale while keeping glyphs on the physical pixel grid where possible.
 * <p>
 * The requested scale is snapped to the nearest value for which one font pixel covers a whole number of screen
 * pixels at the current pose transform. If snapping would move the size by more than {@link #MAX_SNAP_ERROR}, the
 * exact scale is used instead (slightly uneven glyphs are better than text that doesn't fit its box).
 */
public final class PanelText {
    private static final float MAX_SNAP_ERROR = 0.2f;

    private PanelText() {}

    /** Physical pixels covered by one unit at the graphics' current transform. */
    public static float pixelsPerUnit(GuiGraphics graphics) {
        return graphics.pose().last().pose().m00() * (float) Minecraft.getInstance().getWindow().getGuiScale();
    }

    public static float snap(GuiGraphics graphics, float desired) {
        return snap(desired, pixelsPerUnit(graphics), false);
    }

    /** Snaps without ever exceeding {@code desired}; use when the text must fit a fixed width. */
    public static float snapDown(GuiGraphics graphics, float desired) {
        return snap(desired, pixelsPerUnit(graphics), true);
    }

    static float snap(float desired, float pixelsPerUnit, boolean down) {
        if (desired <= 0 || pixelsPerUnit <= 0) return desired;
        float pixels = desired * pixelsPerUnit;
        float snappedPixels = down ? (float) Math.floor(pixels + 1e-3f) : Math.round(pixels);
        if (snappedPixels < 1) return desired;
        float snapped = snappedPixels / pixelsPerUnit;
        return Math.abs(snapped - desired) / desired <= MAX_SNAP_ERROR ? snapped : desired;
    }

    public static int width(Font font, Component text, float scale) {
        return Math.round(font.width(text) * scale);
    }

    public static void draw(GuiGraphics graphics, Font font, Component text, float x, float y, int colour, boolean shadow, float scale) {
        draw(graphics, font, text.getVisualOrderText(), x, y, colour, shadow, scale);
    }

    public static void draw(GuiGraphics graphics, Font font, FormattedCharSequence text, float x, float y, int colour, boolean shadow, float scale) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(scale, scale, 1);
        graphics.drawString(font, text, 0, 0, colour, shadow);
        graphics.pose().popPose();
    }

    public static void drawCentered(GuiGraphics graphics, Font font, Component text, float centreX, float y, int colour, boolean shadow, float scale) {
        draw(graphics, font, text, centreX - font.width(text) * scale / 2f, y, colour, shadow, scale);
    }

    public static void drawRight(GuiGraphics graphics, Font font, Component text, float rightX, float y, int colour, boolean shadow, float scale) {
        draw(graphics, font, text, rightX - font.width(text) * scale, y, colour, shadow, scale);
    }

    /**
     * Word-wraps text into a box and draws as many lines as fit. Returns the number of lines drawn.
     */
    public static int drawWrapped(GuiGraphics graphics, Font font, Component text, int x, int y, int maxWidth, int maxHeight,
                                  int colour, float scale) {
        List<FormattedCharSequence> lines = font.split(text, Math.max(1, (int) (maxWidth / scale)));
        // Whole-unit line steps keep every line on the pixel grid.
        int lineHeight = Math.max(1, Math.round(font.lineHeight * scale));
        int drawn = 0;
        for (FormattedCharSequence line : lines) {
            if ((drawn + 1) * lineHeight - Math.round(scale) > maxHeight) break;
            draw(graphics, font, line, x, y + drawn * lineHeight, colour, false, scale);
            drawn++;
        }
        return drawn;
    }
}
