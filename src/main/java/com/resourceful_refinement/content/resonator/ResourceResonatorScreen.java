package com.resourceful_refinement.content.resonator;

import com.resourceful_refinement.content.resonator.scan.ResonatorTarget;
import com.resourceful_refinement.content.resonator.scan.ScannedPoi;
import com.resourceful_refinement.network.ResonatorScanRequestPayload;
import com.resourceful_refinement.network.SetResonatorFilterPayload;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
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
    private static final int COLOR_SLIDER_TRACK = 0xFF555555;
    private static final int COLOR_LOADING_TRACK = 0xFF0C1A0C;
    private static final int LOADING_RGB = 0x53C653; // green, combined with an animated alpha

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
    private static final int LOADING_Y = 242;
    private static final int LOADING_H = 6;

    // Zoom slider geometry.
    private static final int SLIDER_X = PANEL_X;
    private static final int SLIDER_Y = ZOOM_Y;
    private static final int SLIDER_W = PANEL_W;
    private static final int SLIDER_H = 14;
    private static final int KNOB_W = 8;
    private static final double ZOOM_STEP = 0.08D;

    /** Block-radius the radar shows when fully zoomed in. The zoomed-out extent is the configured scan radius. */
    private static final int MIN_VIEW_BLOCKS = 64;

    /** Safety net: if no scan response arrives within this window, re-enable the UI anyway. */
    private static final long SCAN_TIMEOUT_MS = 10_000L;

    private final boolean[] filter;
    private List<ScannedPoi> results = new ArrayList<>();
    private double zoom = 0.5D; // 0 = fully zoomed out (whole scan radius), 1 = zoomed in (MIN_VIEW_BLOCKS)
    private boolean draggingZoom;

    private boolean scanning;
    private long scanStartMillis;

    private Button scanButton;
    private ScannedPoi hovered;

    public ResourceResonatorScreen(ResourceResonatorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 320;
        this.imageHeight = 252;
        this.filter = menu.getInitialFilter().clone();
        this.results = new ArrayList<>(menu.getInitialResults());
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        this.scanButton = addRenderableWidget(Button.builder(
                        Component.translatable("gui.resourceful_refinement.resonator.scan"), b -> requestScan())
                .bounds(leftPos + PANEL_X, topPos + SCAN_Y, PANEL_W, 20)
                .build());
    }

    private void requestScan() {
        if (scanning) {
            return;
        }
        scanning = true;
        scanStartMillis = Util.getMillis();
        PacketDistributor.sendToServer(new ResonatorScanRequestPayload(menu.getBlockPos()));
    }

    private void sendFilter() {
        PacketDistributor.sendToServer(new SetResonatorFilterPayload(menu.getBlockPos(), filter.clone()));
    }

    /** Called by the S2C result payload when a scan completes. */
    public void acceptScanResults(List<ScannedPoi> incoming) {
        this.results = new ArrayList<>(incoming);
        this.scanning = false;
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
        // Zoom slider: begin dragging and jump to the clicked value.
        if (button == 0 && isInSlider(mouseX, mouseY)) {
            draggingZoom = true;
            setZoomFromMouse(mouseX);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingZoom && button == 0) {
            setZoomFromMouse(mouseX);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            draggingZoom = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // Scroll over the radar (or the slider) to zoom; wheel up = zoom in.
        if (isInRadar(mouseX, mouseY) || isInSlider(mouseX, mouseY)) {
            zoom = Mth.clamp(zoom + scrollY * ZOOM_STEP, 0.0D, 1.0D);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void setZoomFromMouse(double mouseX) {
        double usable = SLIDER_W - KNOB_W;
        double rel = (mouseX - (leftPos + SLIDER_X) - KNOB_W / 2.0D) / usable;
        zoom = Mth.clamp(rel, 0.0D, 1.0D);
    }

    private boolean isInFilterRow(ResonatorTarget target, double mouseX, double mouseY) {
        int rowX = leftPos + PANEL_X;
        int rowY = topPos + FILTER_Y + target.ordinal() * FILTER_ROW_H;
        return mouseX >= rowX && mouseX <= rowX + PANEL_W && mouseY >= rowY && mouseY <= rowY + FILTER_ROW_H - 2;
    }

    private boolean isInSlider(double mouseX, double mouseY) {
        int sx = leftPos + SLIDER_X;
        int sy = topPos + SLIDER_Y;
        return mouseX >= sx && mouseX <= sx + SLIDER_W && mouseY >= sy && mouseY <= sy + SLIDER_H;
    }

    private boolean isInRadar(double mouseX, double mouseY) {
        int cx = leftPos + RADAR_X + RADAR_SIZE / 2;
        int cy = topPos + RADAR_Y + RADAR_SIZE / 2;
        int radius = RADAR_SIZE / 2 - 4;
        double dx = mouseX - cx;
        double dy = mouseY - cy;
        return dx * dx + dy * dy <= (double) radius * radius;
    }

    // --- Rendering ------------------------------------------------------------------------------------------

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Auto-recover if a response never arrives, and keep the scan button disabled while scanning.
        if (scanning && Util.getMillis() - scanStartMillis > SCAN_TIMEOUT_MS) {
            scanning = false;
        }
        if (scanButton != null) {
            scanButton.active = !scanning;
        }

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

        // Zoom label + slider.
        renderZoom(graphics, left, top);

        // Coordinate readout.
        renderReadout(graphics, left, top);

        // Loading bar (only while awaiting a scan response).
        if (scanning) {
            renderLoadingBar(graphics, left, top);
        }
    }

    private void renderFilterRows(GuiGraphics graphics, int left, int top, int mouseX, int mouseY) {
        for (ResonatorTarget target : ResonatorTarget.values()) {
            int rowX = left + PANEL_X;
            int rowY = top + FILTER_Y + target.ordinal() * FILTER_ROW_H;
            boolean hover = isInFilterRow(target, mouseX, mouseY);
            graphics.fill(rowX, rowY, rowX + PANEL_W, rowY + FILTER_ROW_H - 2, hover ? 0xFF3A3A3A : 0xFF2A2A2A);

            drawIcon(graphics, ResonatorPinIcons.generic(target), rowX + 2, rowY + 2);

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

    private void renderZoom(GuiGraphics graphics, int left, int top) {
        graphics.drawString(this.font, Component.translatable("gui.resourceful_refinement.resonator.zoom"),
                left + PANEL_X, top + ZOOM_Y - 10, COLOR_LABEL, false);

        // View-distance readout, right-aligned above the slider.
        String dist = (int) Math.round(viewBlocks()) + "m";
        graphics.drawString(this.font, dist, left + PANEL_X + PANEL_W - this.font.width(dist), top + ZOOM_Y - 10,
                COLOR_LABEL, false);

        int sx = left + SLIDER_X;
        int sy = top + SLIDER_Y;
        int trackY = sy + SLIDER_H / 2;
        graphics.fill(sx, trackY - 1, sx + SLIDER_W, trackY + 1, COLOR_SLIDER_TRACK);

        int knobX = sx + (int) Math.round(zoom * (SLIDER_W - KNOB_W));
        graphics.fill(knobX, sy + 1, knobX + KNOB_W, sy + SLIDER_H - 1, COLOR_PANEL_FRAME);
        frame(graphics, knobX, sy + 1, KNOB_W, SLIDER_H - 2, 0xFF000000);
    }

    /** Furthest the radar can zoom out: the full configured scan radius (chunks → blocks). */
    private int maxViewBlocks() {
        return Math.max(1, menu.getScanRadiusChunks() * 16);
    }

    /** Closest zoom-in extent, never exceeding the zoomed-out extent (matters only for tiny scan radii). */
    private int minViewBlocks() {
        return Math.min(MIN_VIEW_BLOCKS, maxViewBlocks());
    }

    private double viewBlocks() {
        return Mth.lerp(zoom, maxViewBlocks(), minViewBlocks());
    }

    private void renderPins(GuiGraphics graphics, int cx, int cy, int radius, int mouseX, int mouseY) {
        int resX = menu.getBlockPos().getX();
        int resZ = menu.getBlockPos().getZ();
        double viewBlocks = viewBlocks();

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
            drawIcon(graphics, ResonatorPinIcons.iconFor(poi), px - 8, py - 8);
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
            drawIcon(graphics, ResonatorPinIcons.iconFor(hovered), rx + 8, ry + 8);
            String coords = "X: " + hovered.x() + "      Z: " + hovered.z();
            graphics.drawString(this.font, coords, rx + 32, ry + 8, COLOR_READOUT_TEXT, false);
            String typeLine = hovered.hasVariant() ? prettify(hovered.variant()) : targetLabel(hovered.target());
            graphics.drawString(this.font, typeLine, rx + 32, ry + 20, COLOR_READOUT_TEXT, false);
        } else {
            String hint = results.isEmpty()
                    ? Component.translatable("gui.resourceful_refinement.resonator.press_scan").getString()
                    : Component.translatable("gui.resourceful_refinement.resonator.hover_hint").getString();
            graphics.drawString(this.font, hint, rx + 10, ry + 14, 0xFF2E7D2E, false);
        }
    }

    /**
     * Thin indeterminate progress bar: a soft green segment sweeps left-to-right and pulses in brightness
     * (Windows-explorer style), shown only while a scan is in flight.
     */
    private void renderLoadingBar(GuiGraphics graphics, int left, int top) {
        int barX = left + RADAR_X - 4;
        int barW = (PANEL_X + PANEL_W + 4) - (RADAR_X - 4);
        int barY = top + LOADING_Y;

        graphics.fill(barX, barY, barX + barW, barY + LOADING_H, COLOR_LOADING_TRACK);

        long t = Util.getMillis();
        double phase = (t % 1300L) / 1300.0D;          // sweep position 0..1
        float pulse = 0.65f + 0.35f * (float) Math.sin(t / 170.0D); // brightness pulse

        int segW = Math.max(16, barW / 3);
        int travel = barW + segW;
        int segStart = barX - segW + (int) Math.round(phase * travel);

        for (int i = 0; i < segW; i++) {
            int x = segStart + i;
            if (x < barX || x >= barX + barW) {
                continue;
            }
            double falloff = Math.sin((double) i / segW * Math.PI); // 0 at edges, 1 at centre
            int alpha = (int) (Mth.clamp(falloff * pulse, 0.0D, 1.0D) * 255.0D);
            graphics.fill(x, barY, x + 1, barY + LOADING_H, (alpha << 24) | LOADING_RGB);
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

    /** Turns a template id like {@code "crimsite_geyser_small"} into {@code "Crimsite Geyser Small"} for display. */
    private static String prettify(String id) {
        StringBuilder sb = new StringBuilder();
        for (String part : id.split("_")) {
            if (part.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.toString();
    }

    /** Draws a 16×16 radar-pin texture at the given top-left position. */
    private void drawIcon(GuiGraphics graphics, ResourceLocation texture, int x, int y) {
        graphics.blit(texture, x, y, 0, 0.0F, 0.0F, 16, 16, 16, 16);
    }
}
