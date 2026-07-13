package com.resourceful_refinement.content.gui;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Converts a full-screen GUI into a stable virtual-width canvas.
 * <p>
 * Minecraft's configured GUI scale changes the logical screen size. Screens with dense layouts can use this helper to
 * render against a consistent reference width, then scale the whole canvas to the current window.
 */
public final class ScaledGuiCanvas {
    private final int screenWidth;
    private final int screenHeight;
    private final int width;
    private final int height;
    private final float scale;

    private ScaledGuiCanvas(int screenWidth, int screenHeight, int referenceWidth) {
        this.screenWidth = Math.max(1, screenWidth);
        this.screenHeight = Math.max(1, screenHeight);
        this.width = Math.max(1, referenceWidth);
        this.scale = this.screenWidth / (float) this.width;
        this.height = Math.max(1, (int) Math.ceil(this.screenHeight / this.scale));
    }

    public static ScaledGuiCanvas referenceWidth(int screenWidth, int screenHeight, int referenceWidth) {
        return new ScaledGuiCanvas(screenWidth, screenHeight, referenceWidth);
    }

    public int screenWidth() {
        return screenWidth;
    }

    public int screenHeight() {
        return screenHeight;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public float scale() {
        return scale;
    }

    public int toCanvasX(double screenX) {
        return (int) Math.floor(screenX / scale);
    }

    public int toCanvasY(double screenY) {
        return (int) Math.floor(screenY / scale);
    }

    public double toCanvasDelta(double screenDelta) {
        return screenDelta / scale;
    }

    public void push(GuiGraphics graphics) {
        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 1.0f);
    }

    public void pop(GuiGraphics graphics) {
        graphics.pose().popPose();
    }

    public void enableScissor(GuiGraphics graphics, int left, int top, int right, int bottom) {
        graphics.enableScissor(
                (int) Math.floor(left * scale),
                (int) Math.floor(top * scale),
                (int) Math.ceil(right * scale),
                (int) Math.ceil(bottom * scale));
    }
}
