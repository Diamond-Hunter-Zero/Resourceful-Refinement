package com.resourceful_refinement.content.pug;

import com.google.common.collect.Lists;
import com.resourceful_refinement.client.gui.PanelText;
import com.resourceful_refinement.client.gui.ScaledContainerScreen;
import com.resourceful_refinement.client.gui.widget.GlareAddressEditor;
import com.resourceful_refinement.client.gui.widget.HoverTintButton;
import com.resourceful_refinement.client.gui.widget.ThreeStateButton;
import com.resourceful_refinement.client.gui.widget.ValueBoardSlider;
import com.resourceful_refinement.content.glare.GlareAddress;
import com.resourceful_refinement.content.glare.terminal.TelemetryAddressRenderer;
import com.resourceful_refinement.content.gui.CommonSqrButtonTextures.ButtonStyle;
import com.resourceful_refinement.network.ConfigureLaunchpadPayload;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.gui.widget.SelectionScrollInput;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Launchpad controller UI, laid out on the durasteel panel art and scaled to the window by
 * {@link ScaledContainerScreen}.
 * <p>
 * Coordinates below are panel-art texels shifted up by one ({@link #ART_Y}): the art is drawn one pixel high so that
 * its slot wells line up with the menu's slot positions.
 */
public class LaunchpadScreen extends ScaledContainerScreen<LaunchpadMenu> {
    public static final int WIDTH = LaunchControllerGuiTextures.PANEL_WIDTH;
    public static final int HEIGHT = LaunchControllerGuiTextures.PANEL_HEIGHT;
    private static final int ART_Y = -1;

    // Header
    private static final int[] SEND_TAB = {4, 2, 90, 16};
    private static final int[] RECEIVE_TAB = {96, 2, 90, 16};
    private static final int[] CLOSE = {327, 3, 18, 18};

    // Shared
    private static final int ADDRESS_X = 7, ADDRESS_Y = 35, ADDRESS_SPACING = 20;
    private static final int[] LOCAL_ADDRESS_TITLE = {7, 26, 56, 8};

    // Send panel
    private static final int[] FUEL_VALUE = {102, 26, 61, 8};
    private static final int[] FUEL_GAUGE = {79, 36, 118, 14};
    private static final int[] FUEL_INFO = {204, 33, 120, 20};
    private static final int[] CONDITION_FIELD = {232, 62, 114, 14};
    private static final int CONDITION_LABEL_X = 238, CONDITION_LABEL_Y = 65;
    private static final int[] TIMER_BOARD = {236, 84, 107, 24};
    private static final int TIMER_TRACK_START = 241, TIMER_TRACK_END = 337;
    private static final int TIMER_SECONDS_ROW_Y = 91, TIMER_MINUTES_ROW_Y = 102;
    private static final int[] TIMER_LABELS = {177, 83, 51, 26};
    private static final int[] STATUS = {177, 162, 164, 17};

    // Receive panel
    private static final int FLIGHT_TRACK_START = 83, FLIGHT_TRACK_END = 301, FLIGHT_TRACK_Y = 46;
    private static final net.minecraft.resources.ResourceLocation PUG_ICON = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
            com.resourceful_refinement.ResourcefulRefinementMain.MOD_ID, "textures/gui/pug_rocket_icon.png");
    private static final int PUG_ICON_SIZE = 9;
    /** Gap between the bottom of the PUG icon and the arrow track. */
    private static final int PUG_ICON_LIFT = 4;
    private static final int RECEIVE_TEXT_X = 179, RECEIVE_TEXT_RIGHT = 343;
    private static final int[][] RECEIVE_ROWS = {{59, 16}, {76, 17}, {94, 15}};

    // Colours
    private static final int TEXT_LABEL = 0xFF9A9A9A;
    private static final int TEXT_VALUE = 0xFFCDD3D5;
    private static final int TEXT_OK = 0xFF7CD992;
    private static final int TEXT_ERROR = 0xFFC2454A;
    private static final int TEXT_FUEL_VALUE = 0xFFE8A35A;

    private LaunchpadConfiguration draft;
    private GlareAddressEditor destinationAddressEditor;
    private ThreeStateButton sendModeButton;
    private ThreeStateButton receiveModeButton;
    private SelectionScrollInput conditionInput;
    private Label conditionLabel;
    private ValueBoardSlider timerBoard;

    public LaunchpadScreen(LaunchpadMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, WIDTH, HEIGHT);
        inventoryLabelY = 1_000;
        titleLabelY = 1_000;
        draft = menu.getSnapshot().configuration();
    }

    @Override
    protected void init() {
        super.init();

        // Mode tabs share one label size: the largest that fits the wider label.
        Component send = Component.translatable("gui.resourceful_refinement.launchpad.send");
        Component receive = Component.translatable("gui.resourceful_refinement.launchpad.receive");
        float tabScale = Math.min(1.25f, (SEND_TAB[2] - 8f) / Math.max(font.width(send), font.width(receive)));
        sendModeButton = addRenderableWidget(new ThreeStateButton(SEND_TAB[0], SEND_TAB[1], SEND_TAB[2], SEND_TAB[3],
                send, ButtonStyle.DURASTEEL, () -> setMode(LaunchpadMode.SEND)).label(tabScale, true).snapLabelDown().labelOffset(-1));
        receiveModeButton = addRenderableWidget(new ThreeStateButton(RECEIVE_TAB[0], RECEIVE_TAB[1], RECEIVE_TAB[2], RECEIVE_TAB[3],
                receive, ButtonStyle.DURASTEEL, () -> setMode(LaunchpadMode.RECEIVE)).label(tabScale, true).snapLabelDown().labelOffset(-1));

        addRenderableWidget(new HoverTintButton(CLOSE[0], CLOSE[1], CLOSE[2], CLOSE[3],
                Component.translatable("gui.resourceful_refinement.telemetry_terminal.close"), 0x40FFFFFF, this::onClose));

        destinationAddressEditor = addRenderableWidget(new GlareAddressEditor(this, ADDRESS_X, ADDRESS_Y, 16, ADDRESS_SPACING,
                draft.destinationAddress(), address -> updateDraft(draft.mode(), address, draft.launchCondition(), draft.timerSeconds()))
                .withHoverColour(0xFF3A3F44));

        // Launch condition: Create's scrollable selection field, writing into a Create label over the baked field art.
        List<Component> options = new ArrayList<>();
        for (LaunchCondition condition : LaunchCondition.values()) options.add(conditionText(condition));
        conditionLabel = addRenderableWidget(new Label(CONDITION_LABEL_X, CONDITION_LABEL_Y, Component.empty()).withShadow());
        conditionLabel.text = conditionText(draft.launchCondition());
        // Create draws its own tooltip inside the widget's render (here, inside the scaled panel transform); suppress
        // it so only the deferred, correctly-placed copy below is shown.
        conditionInput = new SelectionScrollInput(CONDITION_FIELD[0], CONDITION_FIELD[1], CONDITION_FIELD[2], CONDITION_FIELD[3]) {
            @Override
            protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {}
        };
        conditionInput.forOptions(options);
        conditionInput.titled(Component.translatable("gui.resourceful_refinement.launchpad.condition.title"));
        conditionInput.writingTo(conditionLabel);
        conditionInput.calling(index -> updateDraft(draft.mode(), draft.destinationAddress(),
                LaunchCondition.values()[index], draft.timerSeconds()));
        conditionInput.setState(draft.launchCondition().ordinal());
        addRenderableWidget(conditionInput);

        // Launch timer: Create-style Seconds / Minutes value board over the baked board art.
        timerBoard = addRenderableWidget(new ValueBoardSlider(TIMER_BOARD[0], TIMER_BOARD[1], TIMER_BOARD[2], TIMER_BOARD[3],
                List.of(new ValueBoardSlider.Row(TIMER_SECONDS_ROW_Y, 1, 0, 60),
                        new ValueBoardSlider.Row(TIMER_MINUTES_ROW_Y, 60, 1, 60)),
                TIMER_TRACK_START, TIMER_TRACK_END, LaunchpadConfiguration.MAX_TIMER_SECONDS, draft.timerSeconds(),
                LaunchpadScreen::formatTimer,
                seconds -> updateDraft(draft.mode(), draft.destinationAddress(), draft.launchCondition(), seconds)));

        updateWidgetState();
    }

    private static Component formatTimer(int seconds) {
        return Component.literal(String.format(Locale.ROOT, "%02d:%02d", seconds / 60, seconds % 60));
    }

    private static Component conditionText(LaunchCondition condition) {
        return Component.translatable("gui.resourceful_refinement.launchpad.condition." + condition.name().toLowerCase(Locale.ROOT));
    }

    private void setMode(LaunchpadMode mode) {
        if (draft.mode() == mode) return;
        updateDraft(mode, draft.destinationAddress(), draft.launchCondition(), draft.timerSeconds());
    }

    private void updateDraft(LaunchpadMode mode, GlareAddress destination, LaunchCondition condition, int timerSeconds) {
        draft = new LaunchpadConfiguration(mode, draft.localAddress(), destination, condition,
                Mth.clamp(timerSeconds, 0, LaunchpadConfiguration.MAX_TIMER_SECONDS));
        updateWidgetState();
        PacketDistributor.sendToServer(new ConfigureLaunchpadPayload(menu.getBlockPos(), draft.mode(),
                draft.localAddress(), draft.destinationAddress(), draft.launchCondition(), draft.timerSeconds()));
    }

    private void updateWidgetState() {
        boolean sending = draft.mode().canSend();
        sendModeButton.setToggled(sending);
        receiveModeButton.setToggled(!sending);
        destinationAddressEditor.visible = sending;
        conditionInput.visible = sending;
        conditionLabel.visible = sending;
        timerBoard.visible = sending;
        timerBoard.active = draft.launchCondition() == LaunchCondition.TIMER;
    }

    public void applySnapshot(LaunchpadSnapshot snapshot) {
        menu.applySnapshot(snapshot);
        if (menu.getSnapshot() != snapshot) return;
        draft = snapshot.configuration();
        if (destinationAddressEditor != null) destinationAddressEditor.setAddress(draft.destinationAddress());
        if (conditionInput != null && conditionInput.getState() != draft.launchCondition().ordinal()) {
            conditionInput.setState(draft.launchCondition().ordinal());
            conditionLabel.text = conditionText(draft.launchCondition());
        }
        if (timerBoard != null) timerBoard.setValue(draft.timerSeconds());
        if (sendModeButton != null) updateWidgetState();
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if (conditionInput != null) conditionInput.tick();
    }

    @Override
    protected boolean panelMouseClicked(double mouseX, double mouseY, int button) {
        // Clicking the selection field also cycles it (shift-click cycles backwards), alongside scroll-to-select.
        if (button == 0 && conditionInput.visible && conditionInput.isMouseOver(mouseX, mouseY)) {
            int count = LaunchCondition.values().length;
            conditionInput.setState((conditionInput.getState() + (hasShiftDown() ? count - 1 : 1)) % count);
            conditionInput.onChanged();
            return true;
        }
        return super.panelMouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected boolean panelMouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (timerBoard.isDragging() && timerBoard.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;
        return super.panelMouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    protected boolean panelMouseReleased(double mouseX, double mouseY, int button) {
        if (timerBoard.isDragging() && timerBoard.mouseReleased(mouseX, mouseY, button)) return true;
        return super.panelMouseReleased(mouseX, mouseY, button);
    }

    // --- Rendering ------------------------------------------------------------------------------------------

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        boolean sending = draft.mode().canSend();
        (sending ? LaunchControllerGuiTextures.SEND_PANEL : LaunchControllerGuiTextures.RECEIVE_PANEL)
                .render(graphics, leftPos, topPos + ART_Y);
        if (sending) {
            renderFuelGauge(graphics);
            if (draft.launchCondition() != LaunchCondition.TIMER) {
                // The timer board only applies to timed launches; dim it otherwise.
                dim(graphics, TIMER_LABELS);
                dim(graphics, TIMER_BOARD);
            }
        }
    }

    private static void dim(GuiGraphics graphics, int[] box) {
        graphics.fill(box[0], box[1], box[0] + box[2], box[1] + box[3], 0x99202020);
    }

    /**
     * Segmented fuel cells: a warm amber-to-red gradient split into the 4px columns of the gauge art, with a lit
     * leading edge, and a marker showing how much fuel the current destination needs (red while short of it).
     */
    private void renderFuelGauge(GuiGraphics graphics) {
        LaunchpadSnapshot snapshot = menu.getSnapshot();
        int x = FUEL_GAUGE[0], y = FUEL_GAUGE[1], w = FUEL_GAUGE[2], h = FUEL_GAUGE[3];
        int filled = snapshot.fuelCapacityMb() <= 0 ? 0
                : Mth.clamp(Math.round(snapshot.fuelAmountMb() * w / (float) snapshot.fuelCapacityMb()), 0, w);
        if (filled > 0) {
            int bandTop = Math.max(1, h / 5);
            int bandBottom = Math.max(1, h / 4);
            graphics.fill(x, y, x + filled, y + bandTop, 0xFFFFB45A);                         // highlight
            graphics.fill(x, y + bandTop, x + filled, y + h - bandBottom, 0xFFE0602A);        // body
            graphics.fill(x, y + h - bandBottom, x + filled, y + h, 0xFF9C2A14);              // shade
            for (int cx = x + 3; cx < x + filled; cx += 4) {
                graphics.fill(cx, y, cx + 1, y + h, 0x55000000);                              // cell separators
            }
            if (filled < w) graphics.fill(x + filled - 1, y, x + filled, y + h, 0xFFFFE2A0);  // lit leading edge
        }
        int required = snapshot.fuelRequiredMb();
        if (required > 0 && snapshot.fuelCapacityMb() > 0) {
            int marker = x + Mth.clamp(Math.round(required * w / (float) snapshot.fuelCapacityMb()), 0, w - 1);
            int colour = snapshot.fuelAmountMb() >= required ? 0xFFFFF2B0 : 0xFFFF4A3A;
            graphics.fill(marker, y - 1, marker + 1, y + h + 1, colour);
            graphics.fill(marker - 1, y - 2, marker + 2, y - 1, colour);
            graphics.fill(marker - 1, y + h + 1, marker + 2, y + h + 2, colour);
        }
    }

    @Override
    protected void renderPanelForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (draft.mode().canSend()) renderSendDetails(graphics, mouseX, mouseY);
        else renderReceiveDetails(graphics);
    }

    private void renderSendDetails(GuiGraphics graphics, int mouseX, int mouseY) {
        LaunchpadSnapshot snapshot = menu.getSnapshot();
        float small = PanelText.snap(graphics, 0.75f);
        drawInBox(graphics, Component.literal(snapshot.fuelAmountMb() + " mb"), FUEL_VALUE, TEXT_FUEL_VALUE, small);

        // "Fuel to reach destination:" with the requirement on the line below.
        PanelText.drawFittedLines(graphics, font, List.of(
                        Component.translatable("gui.resourceful_refinement.launchpad.fuel_requirement_1")
                                .append(" ").append(Component.translatable("gui.resourceful_refinement.launchpad.fuel_requirement_2")),
                        Component.literal(snapshot.fuelRequiredMb() + " mb")),
                FUEL_INFO[0], FUEL_INFO[1], FUEL_INFO[2], FUEL_INFO[3], TEXT_LABEL, 0.75f);

        // Status / failure readout, shrunk to fit its well however many reasons there are.
        if (snapshot.failures().isEmpty()) {
            PanelText.drawFittedLines(graphics, font, List.of(Component.translatable("gui.resourceful_refinement.launchpad.ready")),
                    STATUS[0], STATUS[1], STATUS[2], STATUS[3], TEXT_OK, 1f);
        } else {
            List<Component> failures = new ArrayList<>();
            for (LaunchpadFailureReason reason : snapshot.failures()) {
                failures.add(Component.translatable("gui.resourceful_refinement.launchpad.failure." + reason.name().toLowerCase(Locale.ROOT)));
            }
            PanelText.drawFittedLines(graphics, font, failures, STATUS[0], STATUS[1], STATUS[2], STATUS[3], TEXT_ERROR, 1f);
        }

        // Create's scroll inputs carry their own option-list tooltip; show it at the real cursor.
        if (conditionInput.isMouseOver(mouseX, mouseY) && !conditionInput.getToolTip().isEmpty()) {
            setTooltipForNextRenderPass(Lists.transform(conditionInput.getToolTip(), Component::getVisualOrderText));
        }
    }

    private void renderReceiveDetails(GuiGraphics graphics) {
        LaunchpadSnapshot snapshot = menu.getSnapshot();
        drawInBox(graphics, Component.translatable("gui.resourceful_refinement.launchpad.local_address_title"),
                LOCAL_ADDRESS_TITLE, 0xFF6E6E6E, PanelText.snap(graphics, 0.75f));
        TelemetryAddressRenderer.render(graphics, draft.localAddress(), ADDRESS_X, ADDRESS_Y, ADDRESS_SPACING, 1f);

        // Inbound flights ride along just above the baked arrow track; queued flights are tinted amber.
        PugFlightState claimedState = null;
        for (LaunchpadSnapshot.InboundFlight flight : snapshot.inboundFlights()) {
            if (flight.id().equals(snapshot.claimedFlightId())) claimedState = flight.state();
            int markerX = FLIGHT_TRACK_START + Math.round(flight.progress() * (FLIGHT_TRACK_END - FLIGHT_TRACK_START));
            int iconX = markerX - PUG_ICON_SIZE / 2;
            int iconY = FLIGHT_TRACK_Y - PUG_ICON_SIZE - PUG_ICON_LIFT;
            com.mojang.blaze3d.systems.RenderSystem.enableBlend();
            if (flight.state() == PugFlightState.QUEUED) graphics.setColor(1.0F, 0.68F, 0.42F, 1.0F);
            graphics.blit(PUG_ICON, iconX, iconY, 0, 0, PUG_ICON_SIZE, PUG_ICON_SIZE, PUG_ICON_SIZE, PUG_ICON_SIZE);
            graphics.setColor(1, 1, 1, 1);
            com.mojang.blaze3d.systems.RenderSystem.disableBlend();
        }

        // The claimed PUG is still on its way down until it lands and starts unloading.
        Component occupied = snapshot.claimedFlightId() == null
                ? Component.translatable("gui.resourceful_refinement.launchpad.pad_free")
                : claimedState == PugFlightState.DESCENDING
                        ? Component.translatable("gui.resourceful_refinement.launchpad.descending")
                        : Component.translatable("gui.resourceful_refinement.launchpad.unloading", snapshot.landedCargoItems());
        Component[] rows = {
                Component.translatable("gui.resourceful_refinement.launchpad.inbound", snapshot.inboundFlights().size(), snapshot.queuedCount()),
                Component.translatable("gui.resourceful_refinement.launchpad.queued", snapshot.queuedCount()),
                occupied};
        for (int i = 0; i < rows.length; i++) {
            int[] row = RECEIVE_ROWS[i];
            PanelText.drawFittedLines(graphics, font, List.of(rows[i]), RECEIVE_TEXT_X, row[0],
                    RECEIVE_TEXT_RIGHT - RECEIVE_TEXT_X, row[1], i == 2 ? 0xFFB8C7CC : TEXT_VALUE, 1f);
        }
    }

    private void drawInBox(GuiGraphics graphics, Component text, int[] box, int colour, float maxScale) {
        float scale = font.width(text) * maxScale > box[2]
                ? PanelText.snapDown(graphics, box[2] / (float) font.width(text)) : maxScale;
        PanelText.draw(graphics, font, text, box[0], Math.round(box[1] + box[3] / 2f - 7 * scale / 2f), colour, false, scale);
    }
}
