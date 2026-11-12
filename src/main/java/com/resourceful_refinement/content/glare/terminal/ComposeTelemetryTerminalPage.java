package com.resourceful_refinement.content.glare.terminal;

import com.resourceful_refinement.client.gui.widget.GlareAddressEditor;
import com.resourceful_refinement.client.gui.widget.VerticalScrollBar;
import com.resourceful_refinement.client.gui.widget.WrappedTextArea;
import com.resourceful_refinement.content.glare.GlareAddress;
import com.resourceful_refinement.content.glare.GlareMessage;
import com.resourceful_refinement.content.gui.CommonSqrButtonTextures;
import com.resourceful_refinement.network.TelemetryTerminalActionPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.UUID;

import static com.resourceful_refinement.content.glare.terminal.TelemetryTerminalLayout.*;
import static com.resourceful_refinement.content.glare.terminal.TelemetryTerminalWidgets.*;

/**
 * Shared compose editor used by the Manual (Compose view) and Auto-Send pages. Manual compose has a Clear (trash)
 * button and a Send action; Auto-Send has no manual send, so its footer action clears the draft instead.
 */
class ComposeTelemetryTerminalPage implements TelemetryTerminalPage {
    private static final int VISIBLE_CONTACTS = 6;
    private static final int CONTACT_ROW_HEIGHT = LIST[3] / VISIBLE_CONTACTS;
    private static final int CONTACT_ITEM_SPACING = 18;
    private static final int DELETE_SIZE = 10;
    private static final int DELETE_X = LIST[0] + LIST[2] - DELETE_SIZE - 3;

    private final TelemetryTerminalScreen screen;
    private final boolean automatic;
    private GlareAddress destination;
    private GlareAddressEditor addressEditor;
    private WrappedTextArea bodyEditor;
    private VerticalScrollBar contactsScroll;
    private int contactOffset;
    private boolean dirty;
    private int saveDelay;

    ComposeTelemetryTerminalPage(TelemetryTerminalScreen screen, boolean automatic) {
        this.screen = screen;
        this.automatic = automatic;
        TelemetryTerminalSnapshot snapshot = screen.snapshot();
        destination = automatic ? snapshot.autoSendDestination() : snapshot.manualDestination();
    }

    @Override public void init() {
        addressEditor = screen.addPageWidget(new GlareAddressEditor(screen, ADDRESS_SLOTS_X, ADDRESS_SLOTS_Y, 16, ADDRESS_SPACING,
                destination, this::setDestination).withHoverColour(SLOT_HOVER));
        screen.addPageWidget(iconButton(SAVE_BUTTON, CommonSqrButtonTextures.SAVE_ICON, 1f, this::saveContact));

        bodyEditor = screen.addPageWidget(new WrappedTextArea(font(), MESSAGE_AREA[0], MESSAGE_AREA[1], MESSAGE_AREA[2], MESSAGE_AREA[3],
                GlareMessage.MAX_BODY_LENGTH, value -> { dirty = true; saveDelay = 10; }).withStyle(false, TEXT_BRIGHT));
        bodyEditor.setValue(automatic ? screen.snapshot().autoSendBody() : screen.snapshot().manualBody());
        if (automatic && screen.snapshot().displayLinkActive()) bodyEditor.active = false;

        if (automatic) {
            screen.addPageWidget(actionButton(ACTION_BUTTON, "gui.resourceful_refinement.telemetry_terminal.discard",
                    CommonSqrButtonTextures.TRASH_ICON, this::clearBody));
        } else {
            screen.addPageWidget(iconButton(CLEAR_BUTTON, CommonSqrButtonTextures.TRASH_ICON, SCALE_SMALL, this::clearBody));
            screen.addPageWidget(actionButton(ACTION_BUTTON, "gui.resourceful_refinement.telemetry_terminal.send",
                    CommonSqrButtonTextures.SEND_ICON, () -> {
                        flush();
                        screen.send(TelemetryTerminalActionPayload.simple(screen.getMenu().getBlockPos(), TelemetryTerminalActionPayload.Action.SEND_MANUAL));
                    }));
        }
        contactsScroll = screen.addPageWidget(scrollBar(LIST_SCROLL, screen.snapshot().contacts().size(), VISIBLE_CONTACTS,
                contactOffset, offset -> contactOffset = offset));
    }

    private void clearBody() {
        bodyEditor.setValue("");
        dirty = true;
        saveDelay = 0;
        flush();
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
        if (contactsScroll != null) contactsScroll.setTotalItems(snapshot.contacts().size());
        if (!dirty) {
            destination = automatic ? snapshot.autoSendDestination() : snapshot.manualDestination();
            if (addressEditor != null) addressEditor.setAddress(destination);
            if (bodyEditor != null) bodyEditor.setValue(automatic ? snapshot.autoSendBody() : snapshot.manualBody());
        }
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        TelemetryTerminalSnapshot snapshot = screen.snapshot();
        List<GlareAddress> contacts = snapshot.contacts();
        listHeader(graphics, Component.translatable("gui.resourceful_refinement.telemetry_terminal.contact_count", contacts.size()));
        addressLabel(graphics, "gui.resourceful_refinement.telemetry_terminal.target_address");
        infoText(graphics, Component.translatable("gui.resourceful_refinement.telemetry_terminal.characters_remaining",
                bodyEditor == null ? GlareMessage.MAX_BODY_LENGTH : bodyEditor.remaining()), TEXT_DIM);

        for (int row = 0; row < VISIBLE_CONTACTS && contactOffset + row < contacts.size(); row++) {
            int rowY = LIST[1] + row * CONTACT_ROW_HEIGHT;
            boolean hovered = inRect(mouseX, mouseY, LIST[0], rowY, LIST[2], CONTACT_ROW_HEIGHT);
            GlareAddress contact = contacts.get(contactOffset + row);
            if (contact.equals(destination)) fill(graphics, LIST[0], rowY, LIST[2], CONTACT_ROW_HEIGHT, ROW_SELECTED);
            else if (hovered) fill(graphics, LIST[0], rowY, LIST[2], CONTACT_ROW_HEIGHT, ROW_HOVER);
            TelemetryAddressRenderer.render(graphics, contact, LIST[0] + 3, rowY + (CONTACT_ROW_HEIGHT - 16) / 2,
                    CONTACT_ITEM_SPACING, 1f);
            // Hovering a contact reveals a small delete button at its right end.
            if (hovered) {
                int dy = deleteY(rowY);
                deleteBox(graphics, DELETE_X, dy, DELETE_SIZE, inRect(mouseX, mouseY, DELETE_X, dy, DELETE_SIZE, DELETE_SIZE));
            }
        }

        if (automatic && snapshot.displayLinkActive()) {
            statusText(graphics, Component.translatable("gui.resourceful_refinement.telemetry_terminal.display_link_readout"), TEXT_HINT);
            return;
        }
        switch (snapshot.lastResult()) {
            case ADDRESS_NOT_FOUND -> statusText(graphics, Component.translatable("gui.resourceful_refinement.telemetry_terminal.address_not_found"), TEXT_ERROR);
            case INVALID_CONFIGURATION -> statusText(graphics, Component.translatable("gui.resourceful_refinement.telemetry_terminal.incomplete"), TEXT_ERROR);
            case MESSAGE_SENT -> statusText(graphics, Component.translatable(automatic
                    ? "gui.resourceful_refinement.telemetry_terminal.last_sent" : "gui.resourceful_refinement.telemetry_terminal.sent"), TEXT_OK);
            default -> {
                if (automatic) statusText(graphics, Component.translatable("gui.resourceful_refinement.telemetry_terminal.auto_send_hint"), TEXT_HINT);
            }
        }
    }

    private static int deleteY(int rowY) {
        return rowY + (CONTACT_ROW_HEIGHT - DELETE_SIZE) / 2;
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || !TelemetryTerminalLayout.inside(LIST, mouseX, mouseY)) return false;
        int row = (int) (mouseY - LIST[1]) / CONTACT_ROW_HEIGHT;
        List<GlareAddress> contacts = screen.snapshot().contacts();
        if (row >= VISIBLE_CONTACTS || contactOffset + row >= contacts.size()) return false;
        GlareAddress contact = contacts.get(contactOffset + row);
        int rowY = LIST[1] + row * CONTACT_ROW_HEIGHT;
        if (inRect(mouseX, mouseY, DELETE_X, deleteY(rowY), DELETE_SIZE, DELETE_SIZE)) {
            screen.send(action(TelemetryTerminalActionPayload.Action.REMOVE_CONTACT, contact, "", false, new UUID(0, 0)));
            return true;
        }
        if (addressEditor != null) addressEditor.setAddress(contact);
        setDestination(contact);
        return true;
    }

    @Override public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        boolean overList = inRect(mouseX, mouseY, LIST[0], LIST[1], LIST_SCROLL[0] + LIST_SCROLL[2] - LIST[0], LIST[3]);
        return overList && contactsScroll != null && contactsScroll.scrollBy(-(int) Math.signum(scrollY));
    }

    @Override public void removed() { flush(); }
}
