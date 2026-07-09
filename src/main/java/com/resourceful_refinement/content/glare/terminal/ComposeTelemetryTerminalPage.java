package com.resourceful_refinement.content.glare.terminal;

import com.resourceful_refinement.client.gui.widget.VerticalScrollBar;
import com.resourceful_refinement.client.gui.widget.WrappedTextArea;
import com.resourceful_refinement.client.gui.widget.GlareAddressEditor;
import com.resourceful_refinement.content.glare.GlareAddress;
import com.resourceful_refinement.network.TelemetryTerminalActionPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.UUID;

/** Shared compose editor used by Manual and Auto-Send pages. */
class ComposeTelemetryTerminalPage implements TelemetryTerminalPage {
    private final TelemetryTerminalScreen screen;
    private final boolean automatic;
    private final int topOffset;
    private GlareAddress destination;
    private GlareAddressEditor addressEditor;
    private WrappedTextArea bodyEditor;
    private VerticalScrollBar contactsScroll;
    private int contactOffset;
    private boolean dirty;
    private int saveDelay;

    ComposeTelemetryTerminalPage(TelemetryTerminalScreen screen, boolean automatic, int topOffset) {
        this.screen = screen;
        this.automatic = automatic;
        this.topOffset = topOffset;
        TelemetryTerminalSnapshot snapshot = screen.snapshot();
        destination = automatic ? snapshot.autoSendDestination() : snapshot.manualDestination();
    }

    @Override public void init() {
        int x = screen.contentLeft();
        int y = screen.contentTop() + topOffset;
        addressEditor = screen.addPageWidget(new GlareAddressEditor(screen, x + 86, y + 2, 22, 25, destination,
                this::setDestination));
        screen.addPageWidget(Button.builder(Component.translatable("gui.resourceful_refinement.telemetry_terminal.save_contact"), b -> saveContact())
                .bounds(x + 164, y + 2, 48, 22).build());
        bodyEditor = screen.addPageWidget(new WrappedTextArea(screen.getMinecraft().font, x + 84, y + 29, 210, 117,
                512, value -> { dirty = true; saveDelay = 10; }));
        bodyEditor.setValue(automatic ? screen.snapshot().autoSendBody() : screen.snapshot().manualBody());
        if (automatic && screen.snapshot().displayLinkActive()) bodyEditor.active = false;
        screen.addPageWidget(Button.builder(Component.translatable("gui.resourceful_refinement.telemetry_terminal.clear"), b -> {
            bodyEditor.setValue(""); dirty = true; saveDelay = 0; flush();
        }).bounds(x + 84, y + 149, 48, 18).build());
        if (!automatic) {
            screen.addPageWidget(Button.builder(Component.translatable("gui.resourceful_refinement.telemetry_terminal.send"), b -> {
                flush();
                screen.send(TelemetryTerminalActionPayload.simple(screen.getMenu().getBlockPos(), TelemetryTerminalActionPayload.Action.SEND_MANUAL));
            }).bounds(x + 238, y + 149, 56, 18).build());
        }
        contactsScroll = screen.addPageWidget(new VerticalScrollBar(x + 72, y + 24, 6, 126,
                screen.snapshot().contacts().size(), 6, contactOffset, offset -> contactOffset = offset));
    }

    private void setDestination(GlareAddress address) {
        destination = address;
        dirty = true;
        saveDelay = 0;
        flush();
    }

    private void saveContact() {
        flush();
        screen.send(action(TelemetryTerminalActionPayload.Action.ADD_CONTACT, destination, "", false, new UUID(0, 0)));
    }

    private TelemetryTerminalActionPayload action(TelemetryTerminalActionPayload.Action action, GlareAddress address,
            String text, boolean flag, UUID id) {
        return new TelemetryTerminalActionPayload(screen.getMenu().getBlockPos(), action, screen.snapshot().mode(), address, text, flag, id);
    }

    @Override public void tick() {
        if (dirty && saveDelay-- <= 0) flush();
    }

    @Override public void flush() {
        if (!dirty || bodyEditor == null) return;
        screen.send(action(automatic ? TelemetryTerminalActionPayload.Action.SAVE_AUTO_DRAFT : TelemetryTerminalActionPayload.Action.SAVE_MANUAL_DRAFT,
                destination, bodyEditor.getValue(), false, new UUID(0, 0)));
        dirty = false;
    }

    @Override public void snapshotUpdated(TelemetryTerminalSnapshot snapshot) {
        if (!dirty) {
            destination = automatic ? snapshot.autoSendDestination() : snapshot.manualDestination();
            if (addressEditor != null) addressEditor.setAddress(destination);
            if (bodyEditor != null) bodyEditor.setValue(automatic ? snapshot.autoSendBody() : snapshot.manualBody());
        }
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int x = screen.contentLeft();
        int y = screen.contentTop() + topOffset;
        graphics.drawString(screen.getMinecraft().font, Component.translatable("gui.resourceful_refinement.telemetry_terminal.contacts"), x + 4, y + 5, 0xFFE2E5E6, false);
        graphics.drawString(screen.getMinecraft().font, Component.translatable("gui.resourceful_refinement.telemetry_terminal.to"), x + 84, y + 8, 0xFFB8C0C3, false);
        if (automatic && screen.snapshot().displayLinkActive()) {
            graphics.drawString(screen.getMinecraft().font, Component.translatable("gui.resourceful_refinement.telemetry_terminal.display_link_readout"), x + 84, y + 27, 0xFF9FC4D0, false);
        }
        List<GlareAddress> contacts = screen.snapshot().contacts();
        for (int row = 0; row < 6 && contactOffset + row < contacts.size(); row++) {
            int rowY = y + 27 + row * 21;
            boolean hovered = mouseX >= x + 2 && mouseX < x + 70 && mouseY >= rowY && mouseY < rowY + 19;
            if (hovered) graphics.fill(x + 1, rowY - 1, x + 70, rowY + 19, 0x40FFFFFF);
            TelemetryAddressRenderer.render(graphics, contacts.get(contactOffset + row), x + 5, rowY, 20, 0.75f);
        }
        Component status = switch (screen.snapshot().lastResult()) {
            case MESSAGE_SENT -> Component.translatable(automatic
                    ? "gui.resourceful_refinement.telemetry_terminal.last_sent" : "gui.resourceful_refinement.telemetry_terminal.sent");
            case ADDRESS_NOT_FOUND -> Component.translatable("gui.resourceful_refinement.telemetry_terminal.address_not_found");
            case INVALID_CONFIGURATION -> Component.translatable("gui.resourceful_refinement.telemetry_terminal.incomplete");
            default -> Component.empty();
        };
        graphics.drawString(screen.getMinecraft().font, status, x + 138, y + 154, 0xFFB7C2C6, false);
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;
        int x = screen.contentLeft();
        int y = screen.contentTop() + topOffset;
        if (mouseX >= x + 2 && mouseX < x + 70 && mouseY >= y + 27 && mouseY < y + 153) {
            int row = (int) (mouseY - (y + 27)) / 21;
            List<GlareAddress> contacts = screen.snapshot().contacts();
            if (contactOffset + row < contacts.size()) {
                destination = contacts.get(contactOffset + row);
                dirty = true;
                saveDelay = 0;
                flush();
                return true;
            }
        }
        return false;
    }

    @Override public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int x = screen.contentLeft();
        int y = screen.contentTop() + topOffset;
        return mouseX >= x && mouseX < x + 80 && mouseY >= y + 20 && mouseY < y + 155
                && contactsScroll != null && contactsScroll.scrollBy(-(int) Math.signum(scrollY));
    }

    @Override public void removed() { flush(); }
}
