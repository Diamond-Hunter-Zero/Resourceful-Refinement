package com.resourceful_refinement.content.refill_station;

import net.createmod.catnip.gui.widget.AbstractSimiWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;

/**
 * Collapsible scroll list styled like Create's {@code SelectionScrollInput} tooltip entries.
 */
public class TrackingIdPickerList extends AbstractSimiWidget {

    private static final int ROW_HEIGHT = 12;
    private static final int PADDING = 4;
    private static final int SCROLLBAR_WIDTH = 6;
    private static final int BACKGROUND = 0xF0100010;
    private static final int BORDER = 0xFF505050;

    private final List<String> options;
    private final int visibleRows;
    private final Component emptyLabel;

    private int scrollOffset;
    private int hoveredIndex = -1;
    private Consumer<String> onSelected;

    public TrackingIdPickerList(int x, int y, int width, int visibleRows, List<String> options, Component emptyLabel) {
        super(x, y, width, PADDING * 2 + visibleRows * ROW_HEIGHT, Component.empty());
        this.visibleRows = visibleRows;
        this.options = options;
        this.emptyLabel = emptyLabel;
        visible = false;
        active = false;
    }

    public TrackingIdPickerList calling(Consumer<String> onSelected) {
        this.onSelected = onSelected;
        return this;
    }

    public boolean isOpen() {
        return visible;
    }

    public void setOpen(boolean open) {
        visible = open;
        active = open;
        if (!open) {
            hoveredIndex = -1;
        } else {
            clampScrollOffset();
        }
    }

    public void toggleOpen() {
        setOpen(!visible);
    }

    @Override
    protected void doRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!visible) {
            return;
        }

        graphics.fill(getX(), getY(), getX() + width, getY() + height, BACKGROUND);
        graphics.renderOutline(getX(), getY(), width, height, BORDER);

        int listX = getX() + PADDING;
        int listY = getY() + PADDING;
        int listWidth = width - PADDING * 2 - (needsScrollbar() ? SCROLLBAR_WIDTH + 2 : 0);

        hoveredIndex = -1;

        if (options.isEmpty()) {
            graphics.drawString(Minecraft.getInstance().font, emptyLabel, listX, listY + 2, 0xFFD3D3D3, false);
            return;
        }

        for (int row = 0; row < visibleRows; row++) {
            int index = scrollOffset + row;
            if (index >= options.size()) {
                break;
            }

            int rowY = listY + row * ROW_HEIGHT;
            boolean hovered = mouseX >= listX && mouseX < listX + listWidth
                    && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT;
            if (hovered) {
                hoveredIndex = index;
                graphics.fill(listX, rowY, listX + listWidth, rowY + ROW_HEIGHT, 0x40FFFFFF);
            }

            String id = options.get(index);
            Component prefix = hovered
                    ? Component.literal("-> ").withStyle(ChatFormatting.WHITE)
                    : Component.literal("> ").withStyle(ChatFormatting.GRAY);
            Component line = prefix.copy().append(Component.literal(id).withStyle(hovered ? ChatFormatting.WHITE : ChatFormatting.GRAY));
            graphics.drawString(Minecraft.getInstance().font, line, listX, rowY + 2, 0xFFFFFF, false);
        }

        if (needsScrollbar()) {
            renderScrollbar(graphics);
        }
    }

    private void renderScrollbar(GuiGraphics graphics) {
        int trackX = getX() + width - PADDING - SCROLLBAR_WIDTH;
        int trackY = getY() + PADDING;
        int trackHeight = visibleRows * ROW_HEIGHT;
        graphics.fill(trackX, trackY, trackX + SCROLLBAR_WIDTH, trackY + trackHeight, 0xFF303030);

        int thumbHeight = Math.max(8, trackHeight * visibleRows / options.size());
        int maxScroll = Math.max(1, options.size() - visibleRows);
        int thumbY = trackY + (trackHeight - thumbHeight) * scrollOffset / maxScroll;
        graphics.fill(trackX + 1, thumbY, trackX + SCROLLBAR_WIDTH - 1, thumbY + thumbHeight, 0xFFAAAAAA);
    }

    private boolean needsScrollbar() {
        return options.size() > visibleRows;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!visible || !isMouseOver(mouseX, mouseY) || options.size() <= visibleRows) {
            return false;
        }

        scrollOffset -= (int) Math.signum(scrollY);
        clampScrollOffset();
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible || !isMouseOver(mouseX, mouseY) || button != 0) {
            return false;
        }

        if (hoveredIndex >= 0 && hoveredIndex < options.size()) {
            if (onSelected != null) {
                onSelected.accept(options.get(hoveredIndex));
            }
            playDownSound(Minecraft.getInstance().getSoundManager());
            return true;
        }

        return true;
    }

    private void clampScrollOffset() {
        int maxOffset = Math.max(0, options.size() - visibleRows);
        if (scrollOffset < 0) {
            scrollOffset = 0;
        }
        if (scrollOffset > maxOffset) {
            scrollOffset = maxOffset;
        }
    }
}
