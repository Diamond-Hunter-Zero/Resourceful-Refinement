package com.resourceful_refinement.client.gui;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

/**
 * Sizes a fixed-resolution pixel-art panel so it fills a sensible share of the game window regardless of the
 * player's GUI-scale setting.
 * <p>
 * The panel is authored in "panel units" (1 unit = 1 texel of its background art). The scaler picks the largest
 * whole number of physical pixels per unit that keeps the panel inside {@link #MAX_WIDTH_FRACTION} x
 * {@link #MAX_HEIGHT_FRACTION} of the window, so the art stays crisp (no uneven texels). On windows too small for
 * even 1 pixel per unit it falls back to a fractional scale so the panel never goes offscreen.
 * <p>
 * The result is expressed in Minecraft GUI units ({@link #scale()} GUI units per panel unit, panel origin at
 * {@link #originX()}/{@link #originY()}), ready to feed into a {@code PoseStack} transform.
 */
public final class PixelPanelScaler {
    public static final float MAX_WIDTH_FRACTION = 0.78f;
    public static final float MAX_HEIGHT_FRACTION = 0.84f;

    private final int panelWidth;
    private final int panelHeight;
    private float pixelsPerUnit = 1;
    private float scale = 1;
    private float originX;
    private float originY;

    public PixelPanelScaler(int panelWidth, int panelHeight) {
        this.panelWidth = panelWidth;
        this.panelHeight = panelHeight;
    }

    /** Recomputes the layout for the current window. Call from {@code Screen.init()} (which also runs on resize). */
    public void update() {
        Window window = Minecraft.getInstance().getWindow();
        int windowWidth = Math.max(1, window.getWidth());
        int windowHeight = Math.max(1, window.getHeight());
        double guiScale = window.getGuiScale();

        float fit = Math.min(windowWidth * MAX_WIDTH_FRACTION / panelWidth, windowHeight * MAX_HEIGHT_FRACTION / panelHeight);
        if (fit >= 1) {
            pixelsPerUnit = Mth.floor(fit);
        } else {
            // Tiny window: never exceed the window, even if that costs crispness.
            pixelsPerUnit = Math.min(windowWidth / (float) panelWidth, windowHeight / (float) panelHeight);
        }

        // Snap the origin to whole physical pixels so every texel edge lands on a pixel boundary.
        int originPixelsX = Math.max(0, Mth.floor((windowWidth - panelWidth * pixelsPerUnit) / 2f));
        int originPixelsY = Math.max(0, Mth.floor((windowHeight - panelHeight * pixelsPerUnit) / 2f));
        scale = (float) (pixelsPerUnit / guiScale);
        originX = (float) (originPixelsX / guiScale);
        originY = (float) (originPixelsY / guiScale);
    }

    public int panelWidth() { return panelWidth; }
    public int panelHeight() { return panelHeight; }
    /** Physical screen pixels per panel unit (a whole number unless the window is tiny). */
    public float pixelsPerUnit() { return pixelsPerUnit; }
    /** GUI units per panel unit. */
    public float scale() { return scale; }
    /** Panel origin, in GUI units. */
    public float originX() { return originX; }
    public float originY() { return originY; }

    public double toPanelX(double guiX) { return (guiX - originX) / scale; }
    public double toPanelY(double guiY) { return (guiY - originY) / scale; }
    public double toGuiX(double panelX) { return originX + panelX * scale; }
    public double toGuiY(double panelY) { return originY + panelY * scale; }
}
