package com.resourceful_refinement.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/** Minimal multiline editor whose rendering wraps to its current width. */
public class WrappedTextArea extends AbstractWidget {
    private final Font font;
    private final int maxLength;
    private final Consumer<String> onChanged;
    private String value = "";
    private int cursor;
    private boolean drawFrame = true;
    private int textColour = 0xFFE6E8E9;

    public WrappedTextArea(Font font, int x, int y, int width, int height, int maxLength, Consumer<String> onChanged) {
        super(x, y, width, height, Component.empty());
        this.font = font;
        this.maxLength = maxLength;
        this.onChanged = onChanged;
    }

    public String getValue() { return value; }
    public void setValue(String value) {
        this.value = value == null ? "" : value.substring(0, Math.min(value.length(), maxLength));
        cursor = this.value.length();
        followCursor = true;
    }

    /** Disables the built-in frame (for use over panel art) and sets the text colour. */
    public WrappedTextArea withStyle(boolean drawFrame, int textColour) {
        this.drawFrame = drawFrame;
        this.textColour = textColour;
        return this;
    }

    public int remaining() { return maxLength - value.length(); }

    private static final int PADDING = 4;
    private static final int SCROLLBAR_WIDTH = 3;
    private static final int SCROLLBAR_GAP = 3;
    private int scrollLine;
    private boolean followCursor = true;
    private boolean draggingThumb;

    private int visibleLines() { return Math.max(1, (height - PADDING * 2 + 1) / font.lineHeight); }

    /** Wrap width, narrowed to make room for the scrollbar once the text overflows. */
    private int wrapWidth() {
        int full = width - PADDING * 2;
        return font.split(Component.literal(value), full).size() > visibleLines() ? full - SCROLLBAR_WIDTH - SCROLLBAR_GAP : full;
    }

    private int lineCount() { return Math.max(1, font.split(Component.literal(value), wrapWidth()).size()); }
    private int maxScroll() { return Math.max(0, lineCount() - visibleLines()); }
    private int trackX() { return getX() + width - PADDING / 2 - SCROLLBAR_WIDTH; }
    private int thumbHeight() { return Math.max(6, (height - 4) * visibleLines() / lineCount()); }
    private int thumbY() { return getY() + 2 + (height - 4 - thumbHeight()) * scrollLine / Math.max(1, maxScroll()); }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (drawFrame) {
            graphics.fill(getX(), getY(), getX() + width, getY() + height, 0xFF171A1C);
            graphics.renderOutline(getX(), getY(), width, height, isFocused() ? 0xFFB8C4C8 : 0xFF555E62);
        }
        int wrap = wrapWidth();
        var lines = font.split(Component.literal(value), wrap);
        int cursorLine = Math.max(0, font.split(Component.literal(value.substring(0, cursor) + "|"), wrap).size() - 1);
        if (followCursor) {
            if (cursorLine < scrollLine) scrollLine = cursorLine;
            if (cursorLine >= scrollLine + visibleLines()) scrollLine = cursorLine - visibleLines() + 1;
            followCursor = false;
        }
        scrollLine = Math.max(0, Math.min(scrollLine, maxScroll()));

        int y = getY() + PADDING;
        for (int i = scrollLine; i < lines.size() && i < scrollLine + visibleLines(); i++) {
            graphics.drawString(font, lines.get(i), getX() + PADDING, y, active ? textColour : 0xFF8A8F91, false);
            y += font.lineHeight;
        }
        if (isFocused() && (System.currentTimeMillis() / 500L) % 2L == 0L && cursorLine >= scrollLine && cursorLine < scrollLine + visibleLines()) {
            var cursorLines = font.split(Component.literal(value.substring(0, cursor) + "|"), wrap);
            if (!cursorLines.isEmpty()) {
                int cursorY = getY() + PADDING + (cursorLine - scrollLine) * font.lineHeight;
                int cursorX = getX() + PADDING + font.width(cursorLines.getLast()) - font.width("|");
                graphics.fill(cursorX, cursorY, cursorX + 1, cursorY + font.lineHeight, 0xFFFFFFFF);
            }
        }
        if (maxScroll() > 0) {
            int tx = trackX();
            graphics.fill(tx, getY() + 2, tx + SCROLLBAR_WIDTH, getY() + height - 2, 0xFF131313);
            boolean overThumb = draggingThumb || (mouseX >= tx && mouseX < tx + SCROLLBAR_WIDTH && mouseY >= thumbY() && mouseY < thumbY() + thumbHeight());
            graphics.fill(tx, thumbY(), tx + SCROLLBAR_WIDTH, thumbY() + thumbHeight(), overThumb ? 0xFFC4C4C4 : 0xFF8E8E8E);
        }
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isMouseOver(mouseX, mouseY)) {
            setFocused(true);
            if (maxScroll() > 0 && mouseX >= trackX() - 1) {
                draggingThumb = true;
                dragTo(mouseY);
            } else {
                cursor = value.length();
                followCursor = true;
            }
            return true;
        }
        setFocused(false);
        return false;
    }

    @Override public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!draggingThumb) return false;
        dragTo(mouseY);
        return true;
    }

    @Override public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean was = draggingThumb;
        draggingThumb = false;
        return was;
    }

    private void dragTo(double mouseY) {
        int travel = Math.max(1, height - 4 - thumbHeight());
        double relative = mouseY - getY() - 2 - thumbHeight() / 2.0;
        scrollLine = (int) Math.round(relative * maxScroll() / travel);
        scrollLine = Math.max(0, Math.min(scrollLine, maxScroll()));
    }

    @Override public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!visible || !isMouseOver(mouseX, mouseY) || maxScroll() == 0) return false;
        scrollLine = Math.max(0, Math.min(maxScroll(), scrollLine - (int) Math.signum(scrollY)));
        return true;
    }

    @Override public boolean charTyped(char codePoint, int modifiers) {
        if (!isFocused() || !active || !visible || codePoint < 32 || codePoint == 127 || value.length() >= maxLength) return false;
        replaceSelection(Character.toString(codePoint));
        return true;
    }

    @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!isFocused()) return false;
        followCursor = true;
        if (Screen.isPaste(keyCode)) {
            String paste = Minecraft.getInstance().keyboardHandler.getClipboard();
            replaceSelection(paste.substring(0, Math.min(paste.length(), maxLength - value.length())));
            return true;
        }
        if (keyCode == 259 && cursor > 0) {
            value = value.substring(0, cursor - 1) + value.substring(cursor--);
            changed();
            return true;
        }
        if (keyCode == 261 && cursor < value.length()) {
            value = value.substring(0, cursor) + value.substring(cursor + 1);
            changed();
            return true;
        }
        if (keyCode == 263) { cursor = Math.max(0, cursor - 1); return true; }
        if (keyCode == 262) { cursor = Math.min(value.length(), cursor + 1); return true; }
        if ((keyCode == 257 || keyCode == 335) && value.length() < maxLength) { replaceSelection("\n"); return true; }
        return false;
    }

    private void replaceSelection(String text) {
        String safe = text.replace('\r', ' ');
        int available = maxLength - value.length();
        safe = safe.substring(0, Math.min(safe.length(), available));
        value = value.substring(0, cursor) + safe + value.substring(cursor);
        cursor += safe.length();
        changed();
    }

    private void changed() {
        followCursor = true;
        onChanged.accept(value);
    }
    @Override protected void updateWidgetNarration(NarrationElementOutput output) {}
}
