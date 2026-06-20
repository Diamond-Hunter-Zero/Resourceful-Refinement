package com.resourceful_refinement.content.glare;

import com.resourceful_refinement.client.gui.widget.VerticalScrollBar;
import com.resourceful_refinement.network.ConfigureGlareTransceiverPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.network.PacketDistributor;

public class GlareChromaticTransceiverScreen extends AbstractContainerScreen<GlareChromaticTransceiverMenu> {
    private static final int VISIBLE_ROWS = 7;
    private static final int ROW_HEIGHT = 20;
    private static final int PANEL = 0xFF252A2D;
    private static final int PANEL_LIGHT = 0xFF3B4246;
    private static final int TEXT = 0xFFE5E7E8;

    private final boolean[] enabled = new boolean[16];
    private final Button[] toggles = new Button[16];
    private final Button[] comparisonButtons = new Button[16];
    private final EditBox[] thresholdFields = new EditBox[16];
    private final GlareComparison[] comparisons = new GlareComparison[16];
    private GlareLogicMode mode;
    private Button modeButton;
    private VerticalScrollBar scrollBar;
    private int scrollOffset;

    public GlareChromaticTransceiverScreen(GlareChromaticTransceiverMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 256;
        imageHeight = 224;
        inventoryLabelY = 1000;
        mode = menu.getInitialMode();
        for (DyeColor colour : DyeColor.values()) {
            enabled[colour.ordinal()] = menu.getInitialThreshold(colour) >= 0;
            comparisons[colour.ordinal()] = menu.getInitialComparison(colour);
        }
    }

    @Override
    protected void init() {
        super.init();
        for (DyeColor colour : DyeColor.values()) {
            int index = colour.ordinal();
            toggles[index] = addRenderableWidget(Button.builder(toggleLabel(index), button -> toggleFilter(index))
                    .bounds(0, 0, 18, 18).build());
            comparisonButtons[index] = addRenderableWidget(Button.builder(comparisonLabel(index), button -> cycleComparison(index))
                    .bounds(0, 0, 24, 18).build());
            EditBox field = new EditBox(font, 0, 0, 36, 18, Component.translatable("gui.resourceful_refinement.glare_transceiver.threshold"));
            field.setMaxLength(7);
            field.setFilter(value -> value.isEmpty() || value.chars().allMatch(Character::isDigit));
            int threshold = menu.getInitialThreshold(colour);
            field.setValue(Integer.toString(Math.max(0, threshold)));
            thresholdFields[index] = addRenderableWidget(field);
        }
        modeButton = addRenderableWidget(Button.builder(modeLabel(), button -> cycleMode())
                .bounds(leftPos + 178, topPos + 18, 62, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.resourceful_refinement.glare_transceiver.save"), button -> saveAndClose())
                .bounds(leftPos + 178, topPos + 166, 62, 20).build());
        scrollBar = addRenderableWidget(new VerticalScrollBar(
                leftPos + 168, topPos + 42, 6, VISIBLE_ROWS * ROW_HEIGHT - 2,
                DyeColor.values().length, VISIBLE_ROWS, scrollOffset, this::setScrollOffset));
        updateRows();
    }

    private void toggleFilter(int index) {
        enabled[index] = !enabled[index];
        toggles[index].setMessage(toggleLabel(index));
        thresholdFields[index].setEditable(enabled[index]);
        comparisonButtons[index].active = enabled[index];
    }

    private Component toggleLabel(int index) {
        return Component.literal(enabled[index] ? "x" : "");
    }

    private void cycleComparison(int index) {
        GlareComparison[] values = GlareComparison.values();
        comparisons[index] = values[(comparisons[index].ordinal() + 1) % values.length];
        comparisonButtons[index].setMessage(comparisonLabel(index));
    }

    private Component comparisonLabel(int index) {
        return Component.literal(comparisons[index].symbol());
    }

    private void cycleMode() {
        GlareLogicMode[] values = GlareLogicMode.values();
        mode = values[(mode.ordinal() + 1) % values.length];
        modeButton.setMessage(modeLabel());
    }

    private Component modeLabel() {
        return Component.translatable("gui.resourceful_refinement.glare_transceiver.mode", mode.name());
    }

    private void updateRows() {
        for (int i = 0; i < 16; i++) {
            int visibleRow = i - scrollOffset;
            boolean visible = visibleRow >= 0 && visibleRow < VISIBLE_ROWS;
            int y = topPos + 42 + visibleRow * ROW_HEIGHT;
            toggles[i].setPosition(leftPos + 12, y);
            toggles[i].visible = visible;
            comparisonButtons[i].setPosition(leftPos + 103, y);
            comparisonButtons[i].visible = visible;
            comparisonButtons[i].active = enabled[i];
            thresholdFields[i].setPosition(leftPos + 129, y);
            thresholdFields[i].visible = visible;
            thresholdFields[i].setEditable(enabled[i]);
        }
    }

    private void setScrollOffset(int offset) {
        scrollOffset = offset;
        updateRows();
    }

    private void saveAndClose() {
        int[] thresholds = new int[16];
        for (int i = 0; i < thresholds.length; i++) {
            if (!enabled[i]) {
                thresholds[i] = GlareChromaticTransceiverBlockEntity.DISABLED_FILTER;
                continue;
            }
            try {
                thresholds[i] = Math.min(Integer.parseInt(thresholdFields[i].getValue()), GlareChromaticTransceiverBlockEntity.MAX_THRESHOLD);
            } catch (NumberFormatException ignored) {
                thresholds[i] = 0;
            }
        }
        int[] comparisonOrdinals = new int[comparisons.length];
        for (int i = 0; i < comparisons.length; i++) comparisonOrdinals[i] = comparisons[i].ordinal();
        PacketDistributor.sendToServer(new ConfigureGlareTransceiverPayload(menu.getBlockPos(), mode, thresholds, comparisonOrdinals));
        onClose();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX >= leftPos + 8 && mouseX < leftPos + 176 && mouseY >= topPos + 40 && mouseY < topPos + 182) {
            if (scrollBar != null && scrollBar.scrollBy(-(int) Math.signum(scrollY))) {
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (scrollBar != null && scrollBar.isDraggingThumb()
                && scrollBar.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (scrollBar != null && scrollBar.isDraggingThumb()
                && scrollBar.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, PANEL);
        graphics.fill(leftPos + 7, topPos + 39, leftPos + 176, topPos + 183, PANEL_LIGHT);
        graphics.fill(leftPos + 183, topPos + 43, leftPos + 245, topPos + 157, 0xFF1C2022);
        for (int row = 0; row < VISIBLE_ROWS; row++) {
            int index = row + scrollOffset;
            DyeColor colour = DyeColor.values()[index];
            int y = topPos + 47 + row * ROW_HEIGHT;
            graphics.fill(leftPos + 36, y, leftPos + 46, y + 10, 0xFF000000 | colour.getTextureDiffuseColor());
            graphics.drawString(font, Component.translatable("color.minecraft." + colour.getName()), leftPos + 50, y + 1, TEXT, false);
        }
        graphics.drawString(font, Component.translatable("gui.resourceful_refinement.glare_transceiver.filters"), leftPos + 10, topPos + 27, TEXT, false);
        graphics.drawString(font, Component.translatable("gui.resourceful_refinement.glare_transceiver.logic"), leftPos + 178, topPos + 8, TEXT, false);
        graphics.drawString(font, (scrollOffset + 1) + "-" + (scrollOffset + VISIBLE_ROWS) + "/16", leftPos + 119, topPos + 27, 0xFF9DA5A8, false);
        renderColourTotals(graphics);
    }

    private void renderColourTotals(GuiGraphics graphics) {
        graphics.drawString(font, Component.translatable("gui.resourceful_refinement.glare_transceiver.live_charges"), leftPos + 9, topPos + 190, TEXT, false);
        DyeColor[] colours = DyeColor.values();
        for (int i = 0; i < colours.length; i++) {
            int column = i % 8;
            int row = i / 8;
            DyeColor colour = colours[i];
            String compact = "▮" + menu.getColourCharge(colour);
            Component text = Component.literal(compact).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(colour.getTextColor())));
            graphics.drawString(font, text, leftPos + 9 + column * 30, topPos + 202 + row * 10, 0xFFFFFFFF, false);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 9, 8, TEXT, false);
    }
}
