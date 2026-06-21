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
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(getX(), getY(), getX() + width, getY() + height, 0xFF171A1C);
        graphics.renderOutline(getX(), getY(), width, height, isFocused() ? 0xFFB8C4C8 : 0xFF555E62);
        int y = getY() + 4;
        for (var line : font.split(Component.literal(value), width - 8)) {
            if (y + font.lineHeight > getY() + height - 3) break;
            graphics.drawString(font, line, getX() + 4, y, 0xFFE6E8E9, false);
            y += font.lineHeight;
        }
        if (isFocused() && (System.currentTimeMillis() / 500L) % 2L == 0L) {
            String before = value.substring(0, cursor);
            var lines = font.split(Component.literal(before + "|"), width - 8);
            if (!lines.isEmpty()) {
                int cursorY = getY() + 4 + (lines.size() - 1) * font.lineHeight;
                int cursorX = getX() + 4 + font.width(lines.getLast()) - font.width("|");
                if (cursorY < getY() + height - font.lineHeight) graphics.fill(cursorX, cursorY, cursorX + 1, cursorY + font.lineHeight, 0xFFFFFFFF);
            }
        }
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isMouseOver(mouseX, mouseY)) {
            setFocused(true);
            cursor = value.length();
            return true;
        }
        setFocused(false);
        return false;
    }

    @Override public boolean charTyped(char codePoint, int modifiers) {
        if (!isFocused() || !active || !visible || codePoint < 32 || codePoint == 127 || value.length() >= maxLength) return false;
        replaceSelection(Character.toString(codePoint));
        return true;
    }

    @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!isFocused()) return false;
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

    private void changed() { onChanged.accept(value); }
    @Override protected void updateWidgetNarration(NarrationElementOutput output) {}
}
