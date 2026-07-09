package com.resourceful_refinement.content.glare.terminal;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import com.resourceful_refinement.client.gui.widget.VerticalScrollBar;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/** Compact searchable item picker used by telemetry address controls. */
public class TelemetryItemPickerScreen extends Screen {
    private static final int COLUMNS = 9;
    private static final int ROWS = 5;
    private final Screen parent;
    private final Consumer<Item> selection;
    private final List<Item> allItems = new ArrayList<>();
    private final List<Item> filteredItems = new ArrayList<>();
    private EditBox search;
    private VerticalScrollBar scrollBar;
    private int scrollRow;
    private int left;
    private int top;

    public TelemetryItemPickerScreen(Screen parent, Consumer<Item> selection) {
        super(Component.translatable("gui.resourceful_refinement.telemetry_terminal.item_picker"));
        this.parent = parent;
        this.selection = selection;
        BuiltInRegistries.ITEM.stream().filter(item -> item != Items.AIR).sorted(Comparator.comparing(item -> BuiltInRegistries.ITEM.getKey(item).toString())).forEach(allItems::add);
        filteredItems.addAll(allItems);
    }

    @Override protected void init() {
        left = (width - 206) / 2;
        top = (height - 140) / 2;
        search = new EditBox(font, left + 10, top + 10, 186, 18, Component.empty());
        search.setHint(Component.translatable("gui.resourceful_refinement.telemetry_terminal.search_items"));
        search.setResponder(this::filter);
        addRenderableWidget(search);
        scrollBar = addRenderableWidget(new VerticalScrollBar(left + 202, top + 38, 6, ROWS * 20 - 2,
                totalRows(), ROWS, scrollRow, offset -> scrollRow = offset));
        setInitialFocus(search);
    }

    private void filter(String query) {
        String needle = query.toLowerCase(java.util.Locale.ROOT);
        filteredItems.clear();
        for (Item item : allItems) {
            String id = BuiltInRegistries.ITEM.getKey(item).toString();
            String name = item.getDescription().getString().toLowerCase(java.util.Locale.ROOT);
            if (id.contains(needle) || name.contains(needle)) filteredItems.add(item);
        }
        scrollRow = 0;
        if (scrollBar != null) {
            scrollBar.setTotalItems(totalRows());
            scrollBar.setOffset(0);
        }
    }

    private int totalRows() {
        return (filteredItems.size() + COLUMNS - 1) / COLUMNS;
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xB0000000);
        graphics.fill(left, top, left + 206, top + 140, 0xFF292F32);
        graphics.renderOutline(left, top, 206, 140, 0xFF778287);
        super.render(graphics, mouseX, mouseY, partialTick);
        int start = scrollRow * COLUMNS;
        for (int i = 0; i < COLUMNS * ROWS && start + i < filteredItems.size(); i++) {
            int column = i % COLUMNS;
            int row = i / COLUMNS;
            int x = left + 12 + column * 21;
            int y = top + 38 + row * 20;
            boolean hovered = mouseX >= x && mouseX < x + 18 && mouseY >= y && mouseY < y + 18;
            if (hovered) graphics.fill(x - 1, y - 1, x + 18, y + 18, 0x60FFFFFF);
            ItemStack stack = new ItemStack(filteredItems.get(start + i));
            graphics.renderItem(stack, x, y);
            if (hovered) graphics.renderTooltip(font, stack, mouseX, mouseY);
        }
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int column = (int) (mouseX - (left + 12)) / 21;
            int row = (int) (mouseY - (top + 38)) / 20;
            if (column >= 0 && column < COLUMNS && row >= 0 && row < ROWS) {
                int index = scrollRow * COLUMNS + row * COLUMNS + column;
                if (index >= 0 && index < filteredItems.size()) {
                    selection.accept(filteredItems.get(index));
                    minecraft.setScreen(parent);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return scrollBar != null && scrollBar.scrollBy(-(int) Math.signum(scrollY));
    }

    @Override public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (scrollBar != null && scrollBar.isDraggingThumb()
                && scrollBar.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (scrollBar != null && scrollBar.isDraggingThumb() && scrollBar.mouseReleased(mouseX, mouseY, button)) return true;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override public void onClose() { minecraft.setScreen(parent); }
}
