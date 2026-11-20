package com.resourceful_refinement.content.resonator;

import com.mojang.blaze3d.systems.RenderSystem;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.client.gui.PanelText;
import com.resourceful_refinement.client.gui.ScaledContainerScreen;
import com.resourceful_refinement.client.gui.widget.ThreeStateButton;
import com.resourceful_refinement.client.gui.widget.VerticalScrollBar;
import com.resourceful_refinement.content.gui.CommonSqrButtonTextures;
import com.resourceful_refinement.content.gui.CommonSqrButtonTextures.ButtonStyle;
import com.resourceful_refinement.content.gui.SqrHoverButton;
import com.resourceful_refinement.content.prism_cell.PrismCellTier;
import com.resourceful_refinement.content.resonator.scan.ResonatorTarget;
import com.resourceful_refinement.content.resonator.scan.ScannedPoi;
import com.resourceful_refinement.network.PingBeaconResultPayload;
import com.resourceful_refinement.network.PlacePingBeaconPayload;
import com.resourceful_refinement.network.ResonatorScanRequestPayload;
import com.resourceful_refinement.network.SetResonatorFilterPayload;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * Radar-style GUI for the Resource Resonator, laid out 1:1 on {@code resource_resonator_gui.png} and scaled to the
 * window by {@link ScaledContainerScreen}.
 * <p>
 * Left: a top-down radar centred on the Resonator (grid baked into the art) with a details readout beneath it.
 * Right: the installed Prism Cell readout, a scrolling list of per-type filter toggles, and the zoom slider.
 * Bottom centre: the Scan button. Pin positions arrive from the server via {@link ScannedPoi}.
 */
public class ResourceResonatorScreen extends ScaledContainerScreen<ResourceResonatorMenu> {
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "textures/gui/resource_resonator_gui.png");
    private static final int PANEL_WIDTH = 243;
    private static final int PANEL_HEIGHT = 222;
    private static final int SHEET_SIZE = 256;

    // --- Regions of the panel art, {x, y, width, height} ---
    private static final int[] RADAR = {12, 25, 116, 116};
    private static final int RADAR_CX = 70, RADAR_CY = 82, RADAR_RADIUS = 57;
    private static final int[] READOUT = {12, 162, 116, 18};
    private static final int[] CELL_LABEL = {141, 28, 61, 12};
    private static final int[] CELL_VALUE = {204, 30, 31, 8};
    private static final int[] CELL_BAR = {141, 42, 94, 3};
    private static final int[] FILTER_LIST = {146, 60, 76, 80};
    private static final int[] FILTER_SCROLL = {228, 57, 5, 87};
    private static final int[] ZOOM_LABEL = {145, 161, 54, 14};
    private static final int[] ZOOM_VALUE = {200, 162, 29, 8};
    private static final int SLIDER_X = 145, SLIDER_W = 86, SLIDER_TRACK_Y = 176;
    private static final int KNOB_W = 6, KNOB_H = 9;
    private static final int[] SCAN_BUTTON = {101, 202, 40, 16};
    private static final int CLOSE_X = 219, CLOSE_Y = 4;

    // Filter rows
    private static final int FILTER_ROW_H = 20;
    private static final int VISIBLE_FILTER_ROWS = FILTER_LIST[3] / FILTER_ROW_H;
    private static final int CHECK_SIZE = 11;
    private static final int ROW_ICON_X = 2, ROW_LABEL_X = 20;
    private static final int ROW_LABEL_W = FILTER_LIST[2] - ROW_LABEL_X - CHECK_SIZE - 4;

    private static final int PIN_SIZE = 12;
    private static final double ZOOM_STEP = 0.08D;

    // --- Palette (ARGB) ---
    private static final int COLOR_READOUT_TEXT = 0xFF53C653;
    private static final int COLOR_HINT_TEXT = 0xFF2E7D2E;
    private static final int COLOR_ERROR_TEXT = 0xFFE05A3C;
    private static final int COLOR_LABEL = 0xFFBFBFBF;
    private static final int COLOR_DIM = 0xFF777777;
    private static final int COLOR_ROW_HOVER = 0xFF262626;
    private static final int COLOR_CELL_BAR = 0xFF3CC8E6;
    private static final int COLOR_CELL_EMPTY = 0xFF8A8A8A;
    private static final int LOADING_RGB = 0x53C653; // green, combined with an animated alpha

    /** Block-radius the radar shows when fully zoomed in. The zoomed-out extent is the configured scan radius. */
    private static final int MIN_VIEW_BLOCKS = 64;
    /** Safety net: if no scan response arrives within this window, re-enable the UI anyway. */
    private static final long SCAN_TIMEOUT_MS = 10_000L;
    /** Duration and depth of the shrink-grow "button press" pulse when a pin is clicked. */
    private static final long PULSE_MS = 180L;
    private static final double PULSE_DEPTH = 0.3D;
    /** How long a ping status message (success or failure) lingers in the details panel. */
    private static final long PING_STATUS_MS = 3_250L;

    private final boolean[] filter;
    private List<ScannedPoi> results;

    // Installed-cell state, synced from the menu on open and refreshed by ping results.
    private boolean cellPresent;
    private int cellTier;
    private int cellCharge;
    private int cellMaxCharge;
    private final int pingChargeCost;
    private double zoom = 0.5D; // 0 = fully zoomed out (whole scan radius), 1 = zoomed in (MIN_VIEW_BLOCKS)
    private boolean draggingZoom;

    private boolean scanning;
    private long scanStartMillis;

    private ScannedPoi pulsingPoi;
    private long pulseStartMillis;

    private String pingStatus;
    private int pingStatusColor;
    private long pingStatusExpiryMillis;

    private ThreeStateButton scanButton;
    private VerticalScrollBar filterScroll;
    private int filterOffset;
    private ScannedPoi hovered;

    public ResourceResonatorScreen(ResourceResonatorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, PANEL_WIDTH, PANEL_HEIGHT);
        this.inventoryLabelY = 1000;
        this.titleLabelY = 1000;
        this.filter = menu.getInitialFilter().clone();
        this.results = new ArrayList<>(menu.getInitialResults());
        this.cellPresent = menu.isCellPresent();
        this.cellTier = menu.getCellTier();
        this.cellCharge = menu.getCellCharge();
        this.cellMaxCharge = menu.getCellMaxCharge();
        this.pingChargeCost = menu.getPingChargeCost();
    }

    /** Whether the installed cell can currently power a Ping Beacon (creative cells always can). */
    private boolean canPing() {
        if (!cellPresent) {
            return false;
        }
        return PrismCellTier.byIndex(cellTier).isCreative() || cellCharge >= pingChargeCost;
    }

    @Override
    protected void init() {
        super.init();
        scanButton = addRenderableWidget(new ThreeStateButton(SCAN_BUTTON[0], SCAN_BUTTON[1], SCAN_BUTTON[2], SCAN_BUTTON[3],
                Component.translatable("gui.resourceful_refinement.resonator.scan"), ButtonStyle.GREY_SLATE, this::requestScan)
                .label(1f, false));
        filterScroll = addRenderableWidget(new VerticalScrollBar(FILTER_SCROLL[0], FILTER_SCROLL[1], FILTER_SCROLL[2], FILTER_SCROLL[3],
                ResonatorTarget.count(), VISIBLE_FILTER_ROWS, filterOffset, offset -> filterOffset = offset)
                .withStyle(false, 0xFF8E8E8E, 0xFFC4C4C4, 1));
        addRenderableWidget(new SqrHoverButton(CLOSE_X, CLOSE_Y, CommonSqrButtonTextures.HOVER_CLOSE, this::onClose));
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
    protected boolean panelMouseClicked(double mouseX, double mouseY, int button) {
        // Filter rows (whole row toggles).
        ResonatorTarget row = filterRowAt(mouseX, mouseY);
        if (button == 0 && row != null) {
            filter[row.ordinal()] = !filter[row.ordinal()];
            sendFilter();
            playPingSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F);
            return true;
        }
        // Zoom slider: begin dragging and jump to the clicked value.
        if (button == 0 && isInSlider(mouseX, mouseY)) {
            draggingZoom = true;
            setZoomFromMouse(mouseX);
            return true;
        }
        // Clicking a hovered radar pin deploys a Ping Beacon at its location.
        if (button == 0 && hovered != null) {
            deployPing(hovered);
            return true;
        }
        return super.panelMouseClicked(mouseX, mouseY, button);
    }

    private void deployPing(ScannedPoi poi) {
        // The Ping Beacon runs on the installed cell's charge; block the request (and explain) when it can't fire.
        if (!canPing()) {
            String key = cellPresent
                    ? "message.resourceful_refinement.resonator.ping_no_charge"
                    : "message.resourceful_refinement.resonator.ping_no_cell";
            setPingStatus(Component.translatable(key).getString(), COLOR_ERROR_TEXT);
            playPingSound(SoundEvents.DISPENSER_FAIL, 0.8F);
            return;
        }
        pulsingPoi = poi;
        pulseStartMillis = Util.getMillis();
        PacketDistributor.sendToServer(new PlacePingBeaconPayload(menu.getBlockPos(),
                new BlockPos(poi.x(), poi.y(), poi.z())));
    }

    /**
     * Called by the S2C result payload: shows a transient status message in the details panel and plays a feedback
     * sound — a "Pinged Location" confirmation + beacon chime on success, or the failure message + a short deny
     * sound otherwise.
     */
    public void acceptPingResult(PingBeaconResultPayload result) {
        // Keep the installed-cell readout and Ping availability in sync with the server's post-action state.
        this.cellPresent = result.cellPresent();
        this.cellTier = result.cellTier();
        this.cellCharge = result.cellCharge();
        this.cellMaxCharge = result.cellMaxCharge();

        if (result.success()) {
            setPingStatus(Component.translatable("message.resourceful_refinement.resonator.ping_success").getString(),
                    COLOR_READOUT_TEXT);
            playPingSound(SoundEvents.BEACON_POWER_SELECT, 1.4F);
        } else {
            setPingStatus(Component.translatable(result.messageKey()).getString(), COLOR_ERROR_TEXT);
            playPingSound(SoundEvents.DISPENSER_FAIL, 0.8F);
        }
    }

    private void setPingStatus(String message, int color) {
        pingStatus = message;
        pingStatusColor = color;
        pingStatusExpiryMillis = Util.getMillis() + PING_STATUS_MS;
    }

    private static void playPingSound(SoundEvent sound, float pitch) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch));
    }

    private boolean pingStatusActive() {
        return pingStatus != null && Util.getMillis() < pingStatusExpiryMillis;
    }

    @Override
    protected boolean panelMouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingZoom && button == 0) {
            setZoomFromMouse(mouseX);
            return true;
        }
        if (filterScroll != null && filterScroll.isDraggingThumb()
                && filterScroll.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }
        return super.panelMouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    protected boolean panelMouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            draggingZoom = false;
        }
        if (filterScroll != null && filterScroll.isDraggingThumb() && filterScroll.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        return super.panelMouseReleased(mouseX, mouseY, button);
    }

    @Override
    protected boolean panelMouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // Scroll over the radar (or the slider) to zoom; wheel up = zoom in.
        if (isInRadar(mouseX, mouseY) || isInSlider(mouseX, mouseY)) {
            zoom = Mth.clamp(zoom + scrollY * ZOOM_STEP, 0.0D, 1.0D);
            return true;
        }
        boolean overFilters = mouseX >= FILTER_LIST[0] && mouseX < FILTER_SCROLL[0] + FILTER_SCROLL[2]
                && mouseY >= FILTER_SCROLL[1] && mouseY < FILTER_SCROLL[1] + FILTER_SCROLL[3];
        if (overFilters && filterScroll != null && filterScroll.scrollBy(-(int) Math.signum(scrollY))) {
            return true;
        }
        return super.panelMouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void setZoomFromMouse(double mouseX) {
        double usable = SLIDER_W - KNOB_W;
        double rel = (mouseX - SLIDER_X - KNOB_W / 2.0D) / usable;
        zoom = Mth.clamp(rel, 0.0D, 1.0D);
    }

    /** The filter target whose (visible) row is under the cursor, or null. */
    private ResonatorTarget filterRowAt(double mouseX, double mouseY) {
        if (mouseX < FILTER_LIST[0] || mouseX >= FILTER_LIST[0] + FILTER_LIST[2]
                || mouseY < FILTER_LIST[1] || mouseY >= FILTER_LIST[1] + VISIBLE_FILTER_ROWS * FILTER_ROW_H) {
            return null;
        }
        int index = filterOffset + (int) (mouseY - FILTER_LIST[1]) / FILTER_ROW_H;
        ResonatorTarget[] targets = ResonatorTarget.values();
        return index < targets.length ? targets[index] : null;
    }

    private boolean isInSlider(double mouseX, double mouseY) {
        return mouseX >= SLIDER_X && mouseX <= SLIDER_X + SLIDER_W
                && mouseY >= SLIDER_TRACK_Y - KNOB_H / 2.0 - 1 && mouseY <= SLIDER_TRACK_Y + KNOB_H / 2.0 + 2;
    }

    private boolean isInRadar(double mouseX, double mouseY) {
        double dx = mouseX - RADAR_CX;
        double dy = mouseY - RADAR_CY;
        return dx * dx + dy * dy <= (double) RADAR_RADIUS * RADAR_RADIUS;
    }

    // --- Rendering ------------------------------------------------------------------------------------------

    @Override
    public void containerTick() {
        super.containerTick();
        // Auto-recover if a response never arrives, and keep the scan button disabled while scanning.
        if (scanning && Util.getMillis() - scanStartMillis > SCAN_TIMEOUT_MS) {
            scanning = false;
        }
        if (scanButton != null) {
            scanButton.active = !scanning;
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        graphics.blit(BACKGROUND, 0, 0, 0, 0, PANEL_WIDTH, PANEL_HEIGHT, SHEET_SIZE, SHEET_SIZE);
        RenderSystem.disableBlend();
    }

    @Override
    protected void renderPanelForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.hovered = null;
        renderPins(graphics, mouseX, mouseY);
        renderFilterRows(graphics, mouseX, mouseY);
        renderCellReadout(graphics);
        renderZoom(graphics, mouseX, mouseY);
        renderReadout(graphics);
        if (scanning) {
            renderLoadingBar(graphics);
        }
    }

    private void renderFilterRows(GuiGraphics graphics, int mouseX, int mouseY) {
        ResonatorTarget[] targets = ResonatorTarget.values();
        ResonatorTarget hoveredRow = filterRowAt(mouseX, mouseY);
        float maxScale = PanelText.snap(graphics, 0.75F);
        for (int row = 0; row < VISIBLE_FILTER_ROWS && filterOffset + row < targets.length; row++) {
            ResonatorTarget target = targets[filterOffset + row];
            int rowX = FILTER_LIST[0];
            int rowY = FILTER_LIST[1] + row * FILTER_ROW_H;
            boolean hover = target == hoveredRow;
            if (hover) graphics.fill(rowX, rowY, rowX + FILTER_LIST[2], rowY + FILTER_ROW_H, COLOR_ROW_HOVER);

            drawIcon(graphics, ResonatorPinIcons.generic(target), rowX + ROW_ICON_X, rowY + (FILTER_ROW_H - 16) / 2, 16);

            // Label wraps onto up to two lines, shrinking further if it still doesn't fit.
            int labelColour = target.hasWorldgen() ? COLOR_LABEL : COLOR_DIM;
            drawFitted(graphics, Component.literal(targetLabel(target)), rowX + ROW_LABEL_X, rowY + 1,
                    ROW_LABEL_W, FILTER_ROW_H - 2, labelColour, maxScale, 2);

            int boxX = rowX + FILTER_LIST[2] - CHECK_SIZE - 2;
            int boxY = rowY + (FILTER_ROW_H - CHECK_SIZE) / 2;
            drawCheckbox(graphics, boxX, boxY, filter[target.ordinal()], hover);
        }
    }

    /** Installed Prism Cell's grade and remaining charge (or "No Cell"), in the box above the filter list. */
    private void renderCellReadout(GuiGraphics graphics) {
        float small = PanelText.snap(graphics, 0.75F);
        if (!cellPresent) {
            Component none = Component.translatable("gui.resourceful_refinement.resonator.cell_none");
            drawFitted(graphics, none, CELL_LABEL[0] + 1, CELL_LABEL[1], CELL_LABEL[2] - 2, CELL_LABEL[3], COLOR_CELL_EMPTY, small, 1);
            return;
        }

        PrismCellTier tier = PrismCellTier.byIndex(cellTier);
        Component name = Component.translatable("gui.resourceful_refinement.resonator.cell_" + tier.id());
        drawFitted(graphics, name, CELL_LABEL[0] + 1, CELL_LABEL[1], CELL_LABEL[2] - 2, CELL_LABEL[3], COLOR_LABEL, small, 1);

        Component charge = Component.literal(tier.isCreative() ? "∞" : Integer.toString(cellCharge));
        drawCentredInBox(graphics, charge, CELL_VALUE, COLOR_CELL_BAR, small);

        float frac = tier.isCreative() ? 1.0F : (cellMaxCharge > 0 ? (float) cellCharge / cellMaxCharge : 0.0F);
        int fillW = Math.round(CELL_BAR[2] * Mth.clamp(frac, 0.0F, 1.0F));
        graphics.fill(CELL_BAR[0], CELL_BAR[1], CELL_BAR[0] + fillW, CELL_BAR[1] + CELL_BAR[3], COLOR_CELL_BAR);
    }

    private void renderZoom(GuiGraphics graphics, int mouseX, int mouseY) {
        float small = PanelText.snap(graphics, 0.75F);
        drawFitted(graphics, Component.translatable("gui.resourceful_refinement.resonator.zoom"),
                ZOOM_LABEL[0] + 2, ZOOM_LABEL[1], ZOOM_LABEL[2] - 4, ZOOM_LABEL[3], COLOR_LABEL, small, 1);
        drawCentredInBox(graphics, Component.literal((int) Math.round(viewBlocks()) + "m"), ZOOM_VALUE, COLOR_LABEL, small);

        int knobX = SLIDER_X + (int) Math.round(zoom * (SLIDER_W - KNOB_W));
        int knobY = SLIDER_TRACK_Y + 1 - KNOB_H / 2;
        boolean hot = draggingZoom || (mouseX >= knobX && mouseX < knobX + KNOB_W && mouseY >= knobY && mouseY < knobY + KNOB_H);
        RenderSystem.enableBlend();
        (hot ? CommonSqrButtonTextures.BRASS_BUTTON_HIGHLIGHTED : CommonSqrButtonTextures.BRASS_BUTTON)
                .renderNineSlice(graphics, knobX, knobY, KNOB_W, KNOB_H);
        RenderSystem.disableBlend();
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

    private void renderPins(GuiGraphics graphics, int mouseX, int mouseY) {
        int resX = menu.getBlockPos().getX();
        int resZ = menu.getBlockPos().getZ();
        double viewBlocks = viewBlocks();
        int half = PIN_SIZE / 2;

        // Clip pins to the radar screen.
        enablePanelScissor(graphics, RADAR[0], RADAR[1], RADAR[0] + RADAR[2], RADAR[1] + RADAR[3]);
        ScannedPoi newHover = null;
        for (ScannedPoi poi : results) {
            if (!filter[poi.target().ordinal()]) {
                continue;
            }
            double dx = poi.x() - resX;
            double dz = poi.z() - resZ;
            if (Math.sqrt(dx * dx + dz * dz) > viewBlocks) {
                continue;
            }
            int px = RADAR_CX + (int) Math.round(dx / viewBlocks * RADAR_RADIUS);
            int py = RADAR_CY + (int) Math.round(dz / viewBlocks * RADAR_RADIUS);
            drawPin(graphics, ResonatorPinIcons.iconFor(poi), px, py, poi);
            if (mouseX >= px - half && mouseX <= px + half && mouseY >= py - half && mouseY <= py + half) {
                newHover = poi;
            }
        }
        graphics.disableScissor();

        this.hovered = newHover;
    }

    private void renderReadout(GuiGraphics graphics) {
        float small = PanelText.snap(graphics, 0.75F);
        int x = READOUT[0] + 3;
        int w = READOUT[2] - 6;
        if (pingStatusActive()) {
            drawFitted(graphics, Component.literal(pingStatus), x, READOUT[1] + 2, w, READOUT[3] - 3, pingStatusColor, small, 2);
        } else if (hovered != null) {
            drawIcon(graphics, ResonatorPinIcons.iconFor(hovered), READOUT[0] + 1, READOUT[1] + 1, 16);
            int textX = READOUT[0] + 19;
            int textW = READOUT[0] + READOUT[2] - 2 - textX;
            String typeLine = hovered.hasVariant() ? prettify(hovered.variant()) : targetLabel(hovered.target());
            drawFitted(graphics, Component.literal("X: " + hovered.x() + "   Z: " + hovered.z()),
                    textX, READOUT[1] + 2, textW, 7, COLOR_READOUT_TEXT, small, 1);
            drawFitted(graphics, Component.literal(typeLine), textX, READOUT[1] + 10, textW, 7, COLOR_READOUT_TEXT, small, 1);
        } else {
            Component hint = Component.translatable(results.isEmpty()
                    ? "gui.resourceful_refinement.resonator.press_scan"
                    : "gui.resourceful_refinement.resonator.hover_hint");
            drawFitted(graphics, hint, x, READOUT[1] + 2, w, READOUT[3] - 3, COLOR_HINT_TEXT, small, 2);
        }
    }

    /**
     * Thin indeterminate progress bar along the bottom edge of the readout: a soft green segment sweeps
     * left-to-right and pulses in brightness, shown only while a scan is in flight.
     */
    private void renderLoadingBar(GuiGraphics graphics) {
        int barX = READOUT[0];
        int barW = READOUT[2];
        int barY = READOUT[1] + READOUT[3] - 1;

        long t = Util.getMillis();
        double phase = (t % 1300L) / 1300.0D;
        float pulse = 0.65f + 0.35f * (float) Math.sin(t / 170.0D);
        int segW = Math.max(16, barW / 3);
        int segStart = barX - segW + (int) Math.round(phase * (barW + segW));
        for (int i = 0; i < segW; i++) {
            int x = segStart + i;
            if (x < barX || x >= barX + barW) {
                continue;
            }
            double falloff = Math.sin((double) i / segW * Math.PI);
            int alpha = (int) (Mth.clamp(falloff * pulse, 0.0D, 1.0D) * 255.0D);
            graphics.fill(x, barY, x + 1, barY + 1, (alpha << 24) | LOADING_RGB);
        }
    }

    // --- Drawing helpers ------------------------------------------------------------------------------------

    /**
     * Draws text inside a box: word-wrapped onto at most {@code maxLines} lines at {@code maxScale}, shrinking until
     * it fits both the width and height. Vertically centred in the box.
     */
    private void drawFitted(GuiGraphics graphics, Component text, int x, int y, int width, int height, int colour,
                            float maxScale, int maxLines) {
        float scale = maxScale;
        List<FormattedCharSequence> lines = List.of();
        for (int attempt = 0; attempt < 8; attempt++) {
            lines = font.split(text, Math.max(1, (int) (width / scale)));
            boolean fitsLines = lines.size() <= maxLines;
            final float trial = scale;
            boolean fitsWidth = lines.stream().allMatch(l -> font.width(l) * trial <= width);
            float lineStep = Math.max(1, Math.round(font.lineHeight * scale));
            boolean fitsHeight = (lines.size() - 1) * lineStep + 7 * scale <= height;
            if (fitsLines && fitsWidth && fitsHeight) break;
            scale = PanelText.snapDown(graphics, scale * 0.85F);
        }
        int lineStep = Math.max(1, Math.round(font.lineHeight * scale));
        int shown = Math.min(lines.size(), maxLines);
        float blockHeight = (shown - 1) * lineStep + 7 * scale;
        int top = Math.round(y + (height - blockHeight) / 2f);
        for (int i = 0; i < shown; i++) {
            PanelText.draw(graphics, font, lines.get(i), x, top + i * lineStep, colour, false, scale);
        }
    }

    private void drawCentredInBox(GuiGraphics graphics, Component text, int[] box, int colour, float maxScale) {
        float scale = font.width(text) * maxScale > box[2] - 2
                ? PanelText.snapDown(graphics, (box[2] - 2f) / font.width(text)) : maxScale;
        PanelText.drawCentered(graphics, font, text, box[0] + box[2] / 2f,
                Math.round(box[1] + box[3] / 2f - 7 * scale / 2f), colour, false, scale);
    }

    private void drawCheckbox(GuiGraphics graphics, int x, int y, boolean checked, boolean hover) {
        RenderSystem.enableBlend();
        CommonSqrButtonTextures frame = hover ? CommonSqrButtonTextures.GREY_SLATE_BUTTON_HIGHLIGHTED
                : checked ? CommonSqrButtonTextures.GREY_SLATE_BUTTON : CommonSqrButtonTextures.GREY_SLATE_BUTTON_TOGGLED;
        frame.renderNineSlice(graphics, x, y, CHECK_SIZE, CHECK_SIZE);
        RenderSystem.disableBlend();
        if (checked) {
            // Pixel tick mark sized for an 11px box.
            int c = 0xFFFFFFFF;
            graphics.fill(x + 2, y + 5, x + 3, y + 7, c);
            graphics.fill(x + 3, y + 6, x + 4, y + 8, c);
            graphics.fill(x + 4, y + 7, x + 5, y + 9, c);
            graphics.fill(x + 5, y + 6, x + 6, y + 8, c);
            graphics.fill(x + 6, y + 5, x + 7, y + 7, c);
            graphics.fill(x + 7, y + 4, x + 8, y + 6, c);
            graphics.fill(x + 8, y + 3, x + 9, y + 5, c);
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

    /** Draws a 16×16 radar-pin texture at the given top-left position, scaled to {@code size}. */
    private void drawIcon(GuiGraphics graphics, ResourceLocation texture, int x, int y, int size) {
        RenderSystem.enableBlend();
        graphics.blit(texture, x, y, size, size, 0.0F, 0.0F, 16, 16, 16, 16);
        RenderSystem.disableBlend();
    }

    /** Draws a pin centred at (px, py), applying the shrink-grow press pulse if this pin was just clicked. */
    private void drawPin(GuiGraphics graphics, ResourceLocation texture, int px, int py, ScannedPoi poi) {
        float scale = 1.0F;
        if (poi == pulsingPoi) {
            long elapsed = Util.getMillis() - pulseStartMillis;
            if (elapsed >= PULSE_MS) {
                pulsingPoi = null;
            } else {
                double progress = elapsed / (double) PULSE_MS;
                scale = (float) (1.0D - PULSE_DEPTH * Math.sin(progress * Math.PI));
            }
        }
        int half = PIN_SIZE / 2;
        graphics.pose().pushPose();
        graphics.pose().translate(px, py, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        drawIcon(graphics, texture, -half, -half, PIN_SIZE);
        graphics.pose().popPose();
    }
}
