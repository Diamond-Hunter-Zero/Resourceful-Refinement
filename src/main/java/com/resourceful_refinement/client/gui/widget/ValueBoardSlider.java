package com.resourceful_refinement.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.resourceful_refinement.client.gui.PanelText;
import com.resourceful_refinement.content.gui.CommonSqrButtonTextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;

/**
 * A GUI version of Create's value-settings board (as used by the Pulse Timer): one or more rows, each a track of
 * steps in its own unit, with a brass value tag riding on the selected step.
 * <p>
 * Row tracks and row labels are expected to come from the panel art; this widget draws only the value tag (plus a
 * faint ghost tag under the cursor) and handles click, drag and scroll. Values are in base units, so a row with
 * {@code unit = 60} over seconds is a "Minutes" row.
 */
public class ValueBoardSlider extends AbstractWidget {
    /** One board row: its track centre line, its unit in base units, and its selectable step range. */
    public record Row(int centreY, int unit, int minStep, int maxStep) {}

    private static final int TAG_HEIGHT = 9;
    private static final int TAG_TEXT = 0xFF3A2208;
    private static final float GHOST_ALPHA = 0.45f;

    private final List<Row> rows;
    private final int trackStartX;
    private final int trackEndX;
    private final int maxValue;
    private final IntFunction<Component> formatter;
    private final IntConsumer onChanged;
    private int value;
    private boolean dragging;
    private int dragRow = -1;

    /**
     * Steps are spread linearly from {@code trackStartX} (step 0) to {@code trackEndX} (each row's {@code maxStep}).
     */
    public ValueBoardSlider(int x, int y, int width, int height, List<Row> rows, int trackStartX, int trackEndX,
                            int maxValue, int initialValue, IntFunction<Component> formatter, IntConsumer onChanged) {
        super(x, y, width, height, Component.empty());
        this.rows = List.copyOf(rows);
        this.trackStartX = trackStartX;
        this.trackEndX = trackEndX;
        this.maxValue = maxValue;
        this.formatter = formatter;
        this.onChanged = onChanged;
        this.value = Mth.clamp(initialValue, 0, maxValue);
    }

    public int getValue() { return value; }

    public void setValue(int value) {
        if (!dragging) this.value = Mth.clamp(value, 0, maxValue);
    }

    public boolean isDragging() { return dragging; }

    /** The row a value is shown on: the coarsest row it reaches at least one step of. */
    private int rowFor(int value) {
        for (int i = rows.size() - 1; i >= 0; i--) {
            Row row = rows.get(i);
            if (value >= row.unit() * Math.max(1, row.minStep())) return i;
        }
        return 0;
    }

    private int stepX(Row row, int step) {
        return trackStartX + Math.round((trackEndX - trackStartX) * step / (float) row.maxStep());
    }

    private int stepAt(Row row, double mouseX) {
        float t = (float) ((mouseX - trackStartX) / (trackEndX - trackStartX));
        return Mth.clamp(Math.round(t * row.maxStep()), row.minStep(), row.maxStep());
    }

    private int rowAt(double mouseY) {
        int best = 0;
        double bestDistance = Double.MAX_VALUE;
        for (int i = 0; i < rows.size(); i++) {
            double distance = Math.abs(mouseY - rows.get(i).centreY());
            if (distance < bestDistance) {
                bestDistance = distance;
                best = i;
            }
        }
        return best;
    }

    private void setAndNotify(int next) {
        next = Mth.clamp(next, 0, maxValue);
        if (next != value) {
            value = next;
            onChanged.accept(value);
        }
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Font font = Minecraft.getInstance().font;
        float scale = PanelText.snap(graphics, 0.75f);
        // Ghost preview only on rows other than the one the value currently sits on.
        int hoveredRow = rowAt(mouseY);
        if (active && isHovered() && !dragging && hoveredRow != rowFor(value)) {
            Row row = rows.get(hoveredRow);
            int step = stepAt(row, mouseX);
            if (step * row.unit() != value) drawTag(graphics, font, row, step * row.unit(), step, scale, true);
        }
        Row row = rows.get(rowFor(value));
        drawTag(graphics, font, row, value, Math.round(value / (float) row.unit()), scale, false);
    }

    private void drawTag(GuiGraphics graphics, Font font, Row row, int tagValue, int step, float scale, boolean ghost) {
        Component text = formatter.apply(tagValue);
        int tagWidth = Math.round(font.width(text) * scale) + 6;
        int tagX = Mth.clamp(stepX(row, step) - tagWidth / 2, getX(), getX() + width - tagWidth);
        int tagY = row.centreY() - TAG_HEIGHT / 2;
        RenderSystem.enableBlend();
        if (ghost) RenderSystem.setShaderColor(1, 1, 1, GHOST_ALPHA);
        CommonSqrButtonTextures.BRASS_BUTTON.renderNineSlice(graphics, tagX, tagY, tagWidth, TAG_HEIGHT);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
        int colour = ghost ? ((int) (GHOST_ALPHA * 255) << 24) | (TAG_TEXT & 0xFFFFFF) : TAG_TEXT;
        PanelText.drawCentered(graphics, font, text, tagX + tagWidth / 2f,
                Math.round(tagY + TAG_HEIGHT / 2f - 7 * scale / 2f), colour, false, scale);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!active || !visible || button != 0 || !isMouseOver(mouseX, mouseY)) return false;
        dragging = true;
        dragRow = rowAt(mouseY);
        Row row = rows.get(dragRow);
        setAndNotify(stepAt(row, mouseX) * row.unit());
        playDownSound(Minecraft.getInstance().getSoundManager());
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!dragging || button != 0) return false;
        // Like Create's board, dragging vertically moves the tag between rows (units).
        dragRow = rowAt(mouseY);
        Row row = rows.get(dragRow);
        setAndNotify(stepAt(row, mouseX) * row.unit());
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean was = dragging;
        dragging = false;
        return was;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!active || !visible || !isMouseOver(mouseX, mouseY) || scrollY == 0) return false;
        Row row = rows.get(rowFor(value));
        int step = Mth.clamp(Math.round(value / (float) row.unit()) + (int) Math.signum(scrollY), row.minStep(), row.maxStep());
        setAndNotify(step * row.unit());
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
