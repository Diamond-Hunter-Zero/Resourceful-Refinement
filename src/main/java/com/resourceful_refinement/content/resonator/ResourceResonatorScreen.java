package com.resourceful_refinement.content.resonator;

import com.resourceful_refinement.content.resonator.scan.ResonatorTarget;
import com.resourceful_refinement.content.resonator.scan.ScannedPoi;
import com.resourceful_refinement.network.ResonatorScanRequestPayload;
import com.resourceful_refinement.network.SetResonatorFilterPayload;
import com.resourceful_refinement.registry.ModItems;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * Radar-style GUI for the Resource Resonator. The left panel is a top-down radar centred on the Resonator; the
 * right panel holds per-type filter toggles, a zoom slider and the Scan button; the bottom-left panel reads out
 * the coordinates of the hovered pin. Pin positions arrive from the server via {@link ScannedPoi} and are drawn
 * relative to the Resonator's world position.
 */
public class ResourceResonatorScreen extends AbstractContainerScreen<ResourceResonatorMenu> {

    // --- Palette (ARGB) ---
    private static final int COLOR_PANEL_DARK = 0xFF1E1E1E;
    private static final int COLOR_PANEL_FRAME = 0xFFE0A526;
    private static final int COLOR_RADAR_BG = 0xFF0C1A0C;
    private static final int COLOR_RADAR_GRID = 0xFF1F5E1F;
    private static final int COLOR_RADAR_GRID_BRIGHT = 0xFF2E7D2E;
    private static final int COLOR_READOUT_BG = 0xFF0C1A0C;
    private static final int COLOR_READOUT_TEXT = 0xFF53C653;
    private static final int COLOR_LABEL = 0xFFBFBFBF;
    private static final int COLOR_CHECK = 0xFFBFBFBF;

    // --- Layout (relative to leftPos/topPos) ---
    private static final int RADAR_X = 8;
    private static final int RADAR_Y = 8;
    private static final int RADAR_SIZE = 192;
    private static final int PANEL_X = 212;
    private static final int PANEL_W = 100;
    private static final int FILTER_Y = 8;
    private static final int FILTER_ROW_H = 22;
    private static final int ZOOM_Y = 162;
    private static final int SCAN_Y = 196;
    private static final int READOUT_Y = 208;
    private static final int READOUT_H = 24;

    private static final int MIN_VIEW_BLOCKS = 64;
    private static final int MAX_VIEW_BLOCKS = 2048;

    private final boolean[] filter;
    private List<ScannedPoi> results = new ArrayList<>();
    private double zoom = 0.5D; // 0 = fully zoomed out (MAX_VIEW), 1 = zoomed in (MIN_VIEW)

    private ScannedPoi hovered;

    public ResourceResonatorScreen(ResourceResonatorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 320;
        this.imageHeight = 240;
        this.filter = menu.getInitialFilter().clone();
        this.results = new ArrayList<>(menu.getInitialResults());
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        addRenderableWidget(new ZoomSlider(leftPos + PANEL_X, topPos + ZOOM_Y, PANEL_W, 16));

        addRenderableWidget(Button.builder(Component.translatable("gui.resourceful_refinement.resonator.scan"),
                        b -> requestScan())
                .bounds(leftPos + PANEL_X, topPos + SCAN_Y, PANEL_W, 20)
                .build());
    }

    private void requestScan() {
        PacketDistributor.sendToServer(new ResonatorScanRequestPayload(menu.getBlockPos()));
    }

    private void sendFilter() {
        PacketDistributor.sendToServer(new SetResonatorFilterPayload(menu.getBlockPos(), filter.clone()));
    }

    /** Called by the S2C result payload when a scan completes. */
    public void acceptScanResults(List<ScannedPoi> incoming) {
        this.results = new ArrayList<>(incoming);
    }

    // --- Interaction ----------------------------------------------------------------------------------------

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Filter checkbox rows.
        for (ResonatorTarget target : ResonatorTarget.values()) {
            if (isInFilterRow(target, mouseX, mouseY)) {
                filter[target.ordinal()] = !filter[target.ordinal()];
                sendFilter();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isInFilterRow(ResonatorTarget target, double mouseX, double mouseY) {
        int rowX = leftPos + PANEL_X;
        int rowY = topPos + FILTER_Y + target.ordinal() * FILTER_ROW_H;
        return mouseX >= rowX && mouseX <= rowX + PANEL_W && mouseY >= rowY && mouseY <= rowY + FILTER_ROW_H - 2;
    }

    // --- Rendering ------------------------------------------------------------------------------------------

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int left = leftPos;
        int top = topPos;

        // Radar frame + background.
        framedPanel(graphics, left + RADAR_X - 4, top + RADAR_Y - 4, RADAR_SIZE + 8, RADAR_SIZE + 8);
        graphics.fill(left + RADAR_X, top + RADAR_Y, left + RADAR_X + RADAR_SIZE, top + RADAR_Y + RADAR_SIZE, COLOR_RADAR_BG);

        int cx = left + RADAR_X + RADAR_SIZE / 2;
        int cy = top + RADAR_Y + RADAR_SIZE / 2;
        int radius = RADAR_SIZE / 2 - 4;
        drawRadarGrid(graphics, cx, cy, radius);

        // Filter panel.
        framedPanel(graphics, left + PANEL_X - 4, top + FILTER_Y - 4,
                PANEL_W + 8, ResonatorTarget.count() * FILTER_ROW_H + 8);
        this.hovered = null;
        renderFilterRows(graphics, left, top, mouseX, mouseY);

        // Pins.
        renderPins(graphics, cx, cy, radius, mouseX, mouseY);

        // Zoom label.
        graphics.drawString(this.font, Component.translatable("gui.resourceful_refinement.resonator.zoom"),
                left + PANEL_X, top + ZOOM_Y - 10, COLOR_LABEL, false);

        // Coordinate readout.
        renderReadout(graphics, left, top);
    }

    private void renderFilterRows(GuiGraphics graphics, int left, int top, int mouseX, int mouseY) {
        for (ResonatorTarget target : ResonatorTarget.values()) {
            int rowX = left + PANEL_X;
            int rowY = top + FILTER_Y + target.ordinal() * FILTER_ROW_H;
            boolean hover = isInFilterRow(target, mouseX, mouseY);
            graphics.fill(rowX, rowY, rowX + PANEL_W, rowY + FILTER_ROW_H - 2, hover ? 0xFF3A3A3A : 0xFF2A2A2A);

            graphics.renderFakeItem(iconFor(target), rowX + 2, rowY + 2);

            graphics.drawString(this.font, targetLabel(target), rowX + 22, rowY + 3, COLOR_LABEL, false);
            String tail = target.hasWorldgen() ? "" : "—";
            if (!tail.isEmpty()) {
                graphics.drawString(this.font, tail, rowX + 22, rowY + 12, 0xFF777777, false);
            }

            // Checkbox at right.
            int boxX = rowX + PANEL_W - 18;
            int boxY = rowY + 2;
            graphics.fill(boxX, boxY, boxX + 16, boxY + 16, 0xFF111111);
            frame(graphics, boxX, boxY, 16, 16, COLOR_CHECK);
            if (filter[target.ordinal()]) {
                drawCheck(graphics, boxX, boxY);
            }
        }
    }

    private void renderPins(GuiGraphics graphics, int cx, int cy, int radius, int mouseX, int mouseY) {
        int resX = menu.getBlockPos().getX();
        int resZ = menu.getBlockPos().getZ();
        double viewBlocks = Mth.lerp(zoom, MAX_VIEW_BLOCKS, MIN_VIEW_BLOCKS);

        // Clip pins to the circular radar.
        graphics.enableScissor(cx - radius, cy - radius, cx + radius, cy + radius);
        ScannedPoi newHover = null;
        for (ScannedPoi poi : results) {
            if (!filter[poi.target().ordinal()]) {
                continue;
            }
            double dx = poi.x() - resX;
            double dz = poi.z() - resZ;
            double distBlocks = Math.sqrt(dx * dx + dz * dz);
            if (distBlocks > viewBlocks) {
                continue;
            }
            int px = cx + (int) Math.round(dx / viewBlocks * radius);
            int py = cy + (int) Math.round(dz / viewBlocks * radius);
            graphics.renderFakeItem(iconFor(poi.target()), px - 8, py - 8);
            if (mouseX >= px - 8 && mouseX <= px + 8 && mouseY >= py - 8 && mouseY <= py + 8) {
                newHover = poi;
            }
        }
        graphics.disableScissor();

        // Centre marker (the Resonator itself).
        graphics.fill(cx - 1, cy - 1, cx + 2, cy + 2, 0xFFFFFFFF);

        if (newHover != null) {
            this.hovered = newHover;
        }
    }

    private void renderReadout(GuiGraphics graphics, int left, int top) {
        int rx = left + RADAR_X - 4;
        int ry = top + READOUT_Y;
        framedPanel(graphics, rx, ry, RADAR_SIZE + 8, READOUT_H + 8);
        graphics.fill(rx + 4, ry + 4, rx + RADAR_SIZE + 4, ry + READOUT_H + 4, COLOR_READOUT_BG);

        if (hovered != null) {
            graphics.renderFakeItem(iconFor(hovered.target()), rx + 8, ry + 8);
            String coords = "X: " + hovered.x() + "   Y: " + (hovered.yApproximate() ? "~" : "") + hovered.y()
                    + "   Z: " + hovered.z();
            graphics.drawString(this.font, coords, rx + 32, ry + 8, COLOR_READOUT_TEXT, false);
            graphics.drawString(this.font, targetLabel(hovered.target()), rx + 32, ry + 20, COLOR_READOUT_TEXT, false);
        } else {
            String hint = results.isEmpty()
                    ? Component.translatable("gui.resourceful_refinement.resonator.press_scan").getString()
                    : Component.translatable("gui.resourceful_refinement.resonator.hover_hint").getString();
            graphics.drawString(this.font, hint, rx + 10, ry + 14, 0xFF2E7D2E, false);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Title drawn via the radar frame; suppress the default inventory label.
    }

    // --- Drawing helpers ------------------------------------------------------------------------------------

    private void framedPanel(GuiGraphics graphics, int x, int y, int w, int h) {
        graphics.fill(x, y, x + w, y + h, COLOR_PANEL_FRAME);
        graphics.fill(x + 3, y + 3, x + w - 3, y + h - 3, COLOR_PANEL_DARK);
    }

    private void frame(GuiGraphics graphics, int x, int y, int w, int h, int color) {
        graphics.fill(x, y, x + w, y + 1, color);
        graphics.fill(x, y + h - 1, x + w, y + h, color);
        graphics.fill(x, y, x + 1, y + h, color);
        graphics.fill(x + w - 1, y, x + w, y + h, color);
    }

    private void drawCheck(GuiGraphics graphics, int x, int y) {
        // Simple tick mark.
        for (int i = 0; i < 4; i++) {
            graphics.fill(x + 3 + i, y + 7 + i, x + 4 + i, y + 9 + i, COLOR_CHECK);
        }
        for (int i = 0; i < 6; i++) {
            graphics.fill(x + 6 + i, y + 10 - i, x + 7 + i, y + 12 - i, COLOR_CHECK);
        }
    }

    private void drawRadarGrid(GuiGraphics graphics, int cx, int cy, int radius) {
        // Crosshair.
        graphics.fill(cx - radius, cy, cx + radius, cy + 1, COLOR_RADAR_GRID_BRIGHT);
        graphics.fill(cx, cy - radius, cx + 1, cy + radius, COLOR_RADAR_GRID_BRIGHT);
        // Concentric rings.
        drawCircle(graphics, cx, cy, radius, COLOR_RADAR_GRID_BRIGHT);
        drawCircle(graphics, cx, cy, radius * 2 / 3, COLOR_RADAR_GRID);
        drawCircle(graphics, cx, cy, radius / 3, COLOR_RADAR_GRID);
    }

    private void drawCircle(GuiGraphics graphics, int cx, int cy, int r, int color) {
        if (r <= 0) {
            return;
        }
        int segments = Math.max(48, r * 3);
        for (int i = 0; i < segments; i++) {
            double a = (Math.PI * 2 * i) / segments;
            int px = cx + (int) Math.round(Math.cos(a) * r);
            int py = cy + (int) Math.round(Math.sin(a) * r);
            graphics.fill(px, py, px + 1, py + 1, color);
        }
    }

    private static String targetLabel(ResonatorTarget target) {
        return Component.translatable("gui.resourceful_refinement.resonator." + target.id()).getString();
    }

    private static ItemStack iconFor(ResonatorTarget target) {
        return switch (target) {
            case SURFACE_GEYSER -> new ItemStack(ModItems.GEYSER_ITEM.get());
            case CAVE_GEYSER -> new ItemStack(Items.DEEPSLATE);
            case MINERAL_DEPOSIT -> new ItemStack(ModItems.MINERAL_DEPOSIT_ITEM.get());
            case CRYSTAL_FISSURE -> new ItemStack(ModItems.CRYSTAL_FISSURE_BUD_ITEM.get());
        };
    }

    // --- Zoom slider ----------------------------------------------------------------------------------------

    private final class ZoomSlider extends AbstractSliderButton {
        ZoomSlider(int x, int y, int width, int height) {
            super(x, y, width, height, Component.empty(), ResourceResonatorScreen.this.zoom);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            int viewBlocks = (int) Math.round(Mth.lerp(ResourceResonatorScreen.this.zoom, MAX_VIEW_BLOCKS, MIN_VIEW_BLOCKS));
            setMessage(Component.literal(viewBlocks + "m"));
        }

        @Override
        protected void applyValue() {
            ResourceResonatorScreen.this.zoom = this.value;
        }
    }
}
