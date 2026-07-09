package com.resourceful_refinement.content.pug;

import com.resourceful_refinement.client.gui.widget.GlareAddressEditor;
import com.resourceful_refinement.content.gui.CommonSqrButtonTextures;
import com.resourceful_refinement.content.gui.SqrHoverButton;
import com.resourceful_refinement.content.refill_station.RefillStationGuiTextures;
import com.resourceful_refinement.network.ConfigureLaunchpadPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class LaunchpadScreen extends AbstractContainerScreen<LaunchpadMenu> {
    public static final int WIDTH = LaunchControllerGuiTextures.PANEL_WIDTH;
    public static final int HEIGHT = LaunchControllerGuiTextures.PANEL_HEIGHT;
    private LaunchpadConfiguration draft;
    private GlareAddressEditor localAddressEditor;
    private GlareAddressEditor destinationAddressEditor;
    private Button sendModeButton;
    private Button receiveModeButton;
    private Button conditionButton;
    private EditBox timerInput;
    private boolean applyingSnapshot;

    public LaunchpadScreen(LaunchpadMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = WIDTH;
        imageHeight = HEIGHT;
        inventoryLabelY = 1_000;
        titleLabelY = 1_000;
        draft = menu.getSnapshot().configuration();
    }

    @Override
    protected void init() {
        super.init();

        // Send/Receive tabs
        sendModeButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.resourceful_refinement.launchpad.send"), button -> setMode(LaunchpadMode.SEND))
                .bounds(leftPos + 8, topPos, 76, 12).build());
        receiveModeButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.resourceful_refinement.launchpad.receive"), button -> setMode(LaunchpadMode.RECEIVE))
                .bounds(leftPos + 86, topPos, 76, 12).build());

        // Close button
        addRenderableWidget(new SqrHoverButton(
                leftPos + WIDTH - 24,
                topPos + 3,
                CommonSqrButtonTextures.HOVER_CLOSE,
                this::onClose));

        /*localAddressEditor = addRenderableWidget(new GlareAddressEditor(this, leftPos + 14, topPos + 47,
                draft.localAddress(), address -> {
                    draft = new LaunchpadConfiguration(draft.mode(), address, draft.destinationAddress(),
                            draft.launchCondition(), draft.timerSeconds());
                    sendConfiguration();
                }));*/

        destinationAddressEditor = addRenderableWidget(new GlareAddressEditor(this, leftPos + 7, topPos + 31, 16, 20,
                draft.destinationAddress(), address -> {
                    draft = new LaunchpadConfiguration(draft.mode(), draft.localAddress(), address,
                            draft.launchCondition(), draft.timerSeconds());
                    sendConfiguration();
                }));

        conditionButton = addRenderableWidget(Button.builder(conditionLabel(), button -> cycleCondition())
                .bounds(leftPos + 240, topPos + 62, 100, 16).build());
        conditionButton.setTooltip(Tooltip.create(
                Component.translatable("gui.resourceful_refinement.launchpad.condition.tooltip")));
        timerInput = new EditBox(font, leftPos + 240, topPos + 88, 94, 18,
                Component.translatable("gui.resourceful_refinement.launchpad.timer"));
        timerInput.setMaxLength(4);
        timerInput.setFilter(value -> value.isEmpty() || value.chars().allMatch(Character::isDigit));
        timerInput.setValue(Integer.toString(draft.timerSeconds()));
        timerInput.setResponder(this::timerChanged);
        addRenderableWidget(timerInput);
        updateWidgetState();
    }

    private void setMode(LaunchpadMode mode) {
        if (draft.mode() == mode) return;
        draft = new LaunchpadConfiguration(mode, draft.localAddress(), draft.destinationAddress(),
                draft.launchCondition(), draft.timerSeconds());
        updateWidgetState();
        sendConfiguration();
    }

    private void cycleCondition() {
        LaunchCondition[] conditions = LaunchCondition.values();
        LaunchCondition next = conditions[(draft.launchCondition().ordinal() + 1) % conditions.length];
        draft = new LaunchpadConfiguration(draft.mode(), draft.localAddress(), draft.destinationAddress(), next,
                draft.timerSeconds());
        updateWidgetState();
        sendConfiguration();
    }

    private void timerChanged(String value) {
        if (applyingSnapshot || value.isEmpty()) return;
        try {
            int seconds = Mth.clamp(Integer.parseInt(value), 0, LaunchpadConfiguration.MAX_TIMER_SECONDS);
            if (seconds == draft.timerSeconds()) return;
            draft = new LaunchpadConfiguration(draft.mode(), draft.localAddress(), draft.destinationAddress(),
                    draft.launchCondition(), seconds);
            sendConfiguration();
        } catch (NumberFormatException ignored) {
        }
    }

    private void sendConfiguration() {
        PacketDistributor.sendToServer(new ConfigureLaunchpadPayload(menu.getBlockPos(), draft.mode(),
                draft.localAddress(), draft.destinationAddress(), draft.launchCondition(), draft.timerSeconds()));
    }

    private Component conditionLabel() {
        return Component.translatable("gui.resourceful_refinement.launchpad.condition."
                + draft.launchCondition().name().toLowerCase(java.util.Locale.ROOT));
    }

    private void updateWidgetState() {
        boolean sending = draft.mode().canSend();
        sendModeButton.active = !sending;
        receiveModeButton.active = sending;
        destinationAddressEditor.visible = sending;
        conditionButton.visible = sending;
        conditionButton.setMessage(conditionLabel());
        timerInput.visible = sending && draft.launchCondition() == LaunchCondition.TIMER;
    }

    public void applySnapshot(LaunchpadSnapshot snapshot) {
        menu.applySnapshot(snapshot);
        if (menu.getSnapshot() != snapshot) return;
        applyingSnapshot = true;
        draft = snapshot.configuration();
        if (localAddressEditor != null) localAddressEditor.setAddress(draft.localAddress());
        if (destinationAddressEditor != null) destinationAddressEditor.setAddress(draft.destinationAddress());
        if (timerInput != null && !timerInput.isFocused()) timerInput.setValue(Integer.toString(draft.timerSeconds()));
        updateWidgetState();
        applyingSnapshot = false;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        for (var slot : menu.slots) {
            graphics.fill(leftPos + slot.x - 1, topPos + slot.y - 1,
                    leftPos + slot.x + 17, topPos + slot.y + 17, 0xFF15191B);
            graphics.renderOutline(leftPos + slot.x - 1, topPos + slot.y - 1, 18, 18, 0xFF687277);
        }

        if (draft.mode().canSend()) renderSendBackground(graphics);
        else renderReceiveBackground(graphics);
    }

    private void renderSendBackground(GuiGraphics graphics) {

        graphics.pose().pushPose();
        graphics.pose().translate(leftPos, topPos-1, 0);
        graphics.pose().scale(2f, 2f, 1.0f);
        graphics.blit(LaunchControllerGuiTextures.SEND_PANEL.location,0,0,
                0,0, LaunchControllerGuiTextures.PANEL_WIDTH, LaunchControllerGuiTextures.PANEL_HEIGHT);
        graphics.pose().popPose();

        LaunchpadSnapshot snapshot = menu.getSnapshot();
        int barX = leftPos + 78;
        int barY = topPos + 31;
        int barWidth = 120;

        // Fuel guage
        int filled = snapshot.fuelCapacityMb() <= 0 ? 0
                : Mth.clamp(snapshot.fuelAmountMb() * barWidth / snapshot.fuelCapacityMb(), 0, barWidth);
        graphics.fill(barX, barY, barX + filled, barY + 16, 0xFFa9121c);
        if (snapshot.fuelRequiredMb() > 0 && snapshot.fuelCapacityMb() > 0) {
            int marker = Mth.clamp(snapshot.fuelRequiredMb() * barWidth / snapshot.fuelCapacityMb(), 0, barWidth - 1);
            graphics.fill(barX + marker, barY - 2, barX + marker + 2, barY + 14, 0xFFffb73e);
        }

    }

    private void renderReceiveBackground(GuiGraphics graphics) {

        graphics.pose().pushPose();
        graphics.pose().translate(leftPos, topPos-1, 0);
        graphics.pose().scale(2f, 2f, 1.0f);
        graphics.blit(LaunchControllerGuiTextures.RECEIVE_PANEL.location,0,0,
                0,0, LaunchControllerGuiTextures.PANEL_WIDTH, LaunchControllerGuiTextures.PANEL_HEIGHT);
        graphics.pose().popPose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        if (draft.mode().canSend()) renderSendDetails(graphics);
        else renderReceiveDetails(graphics);
        renderTooltip(graphics, mouseX, mouseY);
    }

    private void renderSendDetails(GuiGraphics graphics) {
        LaunchpadSnapshot snapshot = menu.getSnapshot();
        /*graphics.drawString(font, Component.translatable("gui.resourceful_refinement.launchpad.destination"),
                leftPos + 98, topPos + 35, 0xFFCDD3D5, false);*/

        graphics.drawString(font, Component.translatable(snapshot.fuelAmountMb() + " mb"),
                leftPos + 110, topPos + 21, 0xFF767676, false);

        graphics.drawString(font, Component.translatable("gui.resourceful_refinement.launchpad.fuel_requirement_1"),
                leftPos + 203, topPos + 30, 0xFF171717, false);
        graphics.drawString(font, Component.translatable("gui.resourceful_refinement.launchpad.fuel_requirement_2"),
                leftPos + 203, topPos + 40, 0xFF171717, false);

        graphics.drawString(font, snapshot.fuelRequiredMb() + " mb",
                leftPos + 277, topPos + 35, 0xFF767676, false);

        int y = topPos + 165;
        int x = leftPos + 180;
        if (snapshot.failures().isEmpty()) {
            graphics.drawString(font, Component.translatable("gui.resourceful_refinement.launchpad.ready"),
                    x, y, 0xFF7CD992, false);
        } else {
            for (int index = 0; index < Math.min(3, snapshot.failures().size()); index++) {
                LaunchpadFailureReason reason = snapshot.failures().get(index);
                graphics.drawString(font, Component.translatable("gui.resourceful_refinement.launchpad.failure."
                                + reason.name().toLowerCase(java.util.Locale.ROOT)),
                        x, y + index * 10, 0xFF954043, false);
            }
        }
    }

    private void renderReceiveDetails(GuiGraphics graphics) {

        // Render progress tracker
        LaunchpadSnapshot snapshot = menu.getSnapshot();
        int x = leftPos + 84;
        int lineStart = x;
        int lineWidth = 216;
        for (LaunchpadSnapshot.InboundFlight flight : snapshot.inboundFlights()) {
            int markerX = lineStart + 2 + Math.round(flight.progress() * (lineWidth-3));
            int color = flight.state() == PugFlightState.QUEUED ? 0xFFE09A62 : 0xFFB9D7E2;
            graphics.fill(markerX-2, topPos + 29, markerX+3, topPos + 36, color);
        }
        //graphics.fill(lineStart, topPos + 29, lineStart + lineWidth, topPos + 36, 0xFFB9D7E2);

        graphics.drawString(font, Component.translatable("gui.resourceful_refinement.launchpad.inbound",
                        snapshot.inboundFlights().size(), snapshot.queuedCount()),
                leftPos + 178, topPos + 64, 0xFFCDD3D5, false);
        graphics.drawString(font, Component.translatable("gui.resourceful_refinement.launchpad.queued",
                        snapshot.inboundFlights().size(), snapshot.queuedCount()),
                leftPos + 178, topPos + 82, 0xFFCDD3D5, false);
        Component occupied = snapshot.claimedFlightId() == null
                ? Component.translatable("gui.resourceful_refinement.launchpad.pad_free")
                : Component.translatable("gui.resourceful_refinement.launchpad.unloading", snapshot.landedCargoItems());
        graphics.drawString(font, occupied, leftPos + 178, topPos + 98, 0xFFB8C7CC, false);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }
}
