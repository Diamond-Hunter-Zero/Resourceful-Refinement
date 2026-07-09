package com.resourceful_refinement.client.gui.widget;

import net.createmod.catnip.gui.widget.AbstractSimiWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.function.IntConsumer;

/** Reusable integer-offset scrollbar for fixed-row lists. */
public class VerticalScrollBar extends AbstractSimiWidget {
    private static final int MIN_THUMB_HEIGHT = 8;
    private static final int TRACK_COLOUR = 0xFF1B1F21;
    private static final int THUMB_COLOUR = 0xFF8C969B;
    private static final int THUMB_HOVERED_COLOUR = 0xFFBCC4C7;

    private int totalItems;
    private final int visibleItems;
    private final IntConsumer onOffsetChanged;
    private int offset;
    private boolean dragging;
    private double dragGrabOffset;

    public VerticalScrollBar(int x, int y, int width, int height, int totalItems, int visibleItems,
            int initialOffset, IntConsumer onOffsetChanged) {
        super(x, y, width, height, Component.empty());
        this.totalItems = Math.max(0, totalItems);
        this.visibleItems = Math.max(1, visibleItems);
        this.onOffsetChanged = onOffsetChanged;
        this.offset = clamp(initialOffset);
        this.active = maxOffset() > 0;
    }

    public int getOffset() {
        return offset;
    }

    public boolean setOffset(int offset) {
        int clamped = clamp(offset);
        if (clamped == this.offset) {
            return false;
        }
        this.offset = clamped;
        onOffsetChanged.accept(clamped);
        return true;
    }

    public boolean scrollBy(int rows) {
        return setOffset(offset + rows);
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = Math.max(0, totalItems);
        this.active = maxOffset() > 0;
        setOffset(offset);
    }

    public boolean isDraggingThumb() {
        return dragging;
    }

    @Override
    protected void doRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(getX(), getY(), getX() + width, getY() + height, TRACK_COLOUR);
        if (!active) {
            return;
        }
        int thumbY = thumbY();
        int colour = dragging || isMouseOverThumb(mouseX, mouseY) ? THUMB_HOVERED_COLOUR : THUMB_COLOUR;
        graphics.fill(getX() + 1, thumbY, getX() + width - 1, thumbY + thumbHeight(), colour);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!active || !visible || button != 0 || !isMouseOver(mouseX, mouseY)) {
            return false;
        }
        int currentThumbY = thumbY();
        dragGrabOffset = mouseY >= currentThumbY && mouseY < currentThumbY + thumbHeight()
                ? mouseY - currentThumbY
                : thumbHeight() / 2.0;
        dragging = true;
        updateFromMouse(mouseY);
        playDownSound(Minecraft.getInstance().getSoundManager());
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!dragging || button != 0) {
            return false;
        }
        updateFromMouse(mouseY);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button != 0 || !dragging) {
            return false;
        }
        dragging = false;
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!active || !visible || !isMouseOver(mouseX, mouseY)) {
            return false;
        }
        return scrollBy(-(int) Math.signum(scrollY));
    }

    private void updateFromMouse(double mouseY) {
        int travel = Math.max(1, height - thumbHeight());
        double relative = mouseY - getY() - dragGrabOffset;
        setOffset((int) Math.round(relative * maxOffset() / travel));
    }

    private boolean isMouseOverThumb(double mouseX, double mouseY) {
        return mouseX >= getX() && mouseX < getX() + width
                && mouseY >= thumbY() && mouseY < thumbY() + thumbHeight();
    }

    private int thumbY() {
        return getY() + (height - thumbHeight()) * offset / Math.max(1, maxOffset());
    }

    private int thumbHeight() {
        return Math.max(MIN_THUMB_HEIGHT, height * visibleItems / Math.max(visibleItems, totalItems));
    }

    private int maxOffset() {
        return Math.max(0, totalItems - visibleItems);
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(maxOffset(), value));
    }
}
