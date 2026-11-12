package com.resourceful_refinement.content.glare.terminal;

import com.resourceful_refinement.client.gui.PanelText;
import com.resourceful_refinement.client.gui.widget.ThreeStateButton;
import com.resourceful_refinement.client.gui.widget.VerticalScrollBar;
import com.resourceful_refinement.content.glare.GlareAddress;
import com.resourceful_refinement.network.TelemetryTerminalActionPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.resourceful_refinement.content.glare.terminal.TelemetryTerminalLayout.*;
import static com.resourceful_refinement.content.glare.terminal.TelemetryTerminalWidgets.*;

public class AutoReceiveTelemetryTerminalPage implements TelemetryTerminalPage {
    private static final int VISIBLE_FILTERS = 5;
    private static final int FILTER_ROW_HEIGHT = FILTER_LIST[3] / VISIBLE_FILTERS;
    private static final int REMOVE_SIZE = 15;
    private static final int ROW_INSET = 2;
    private static final int REMOVE_X = FILTER_LIST[0] + FILTER_LIST[2] - REMOVE_SIZE - ROW_INSET;

    private final TelemetryTerminalScreen screen;
    private EditBox filterInput;
    private VerticalScrollBar scrollBar;
    private int offset;
    private int knownRevision;
    private List<String> knownFilters;
    private boolean knownDiscardMode;
    private boolean draftDirty;
    private int draftDelay;
    private boolean applyingSnapshot;

    public AutoReceiveTelemetryTerminalPage(TelemetryTerminalScreen screen) {
        this.screen = screen;
        knownRevision = screen.snapshot().revision();
        knownFilters = new ArrayList<>(screen.snapshot().receiveFilters());
        knownDiscardMode = screen.snapshot().discardMatchingMessages();
    }

    @Override public void init() {
        boolean discarding = screen.snapshot().discardMatchingMessages();
        Component toggle = Component.translatable(discarding
                ? "gui.resourceful_refinement.telemetry_terminal.discard_messages"
                : "gui.resourceful_refinement.telemetry_terminal.keep_messages");
        screen.addPageWidget(slateButton(DISCARD_TOGGLE[0], DISCARD_TOGGLE[1], DISCARD_TOGGLE[2], DISCARD_TOGGLE[3], toggle, 1f,
                () -> send(TelemetryTerminalActionPayload.Action.SET_DISCARD, "", !screen.snapshot().discardMatchingMessages())));

        List<String> filters = screen.snapshot().receiveFilters();
        scrollBar = screen.addPageWidget(scrollBar(FILTER_SCROLL, filters.size(), VISIBLE_FILTERS, offset, value -> offset = value));

        // Unbordered so the panel's baked input well is the frame; vertically centred within it.
        filterInput = screen.addPageWidget(new EditBox(font(), FILTER_INPUT[0] + 5, FILTER_INPUT[1] + (FILTER_INPUT[3] - 8) / 2,
                FILTER_INPUT[2] - 10, 9, Component.translatable("gui.resourceful_refinement.telemetry_terminal.filter")));
        filterInput.setBordered(false);
        filterInput.setTextColor(TEXT_BRIGHT);
        filterInput.setMaxLength(TelemetryTerminalSnapshot.MAX_FILTER_LENGTH);
        filterInput.setHint(Component.translatable("gui.resourceful_refinement.telemetry_terminal.filter_hint"));
        filterInput.setValue(screen.snapshot().receiveFilterDraft());
        filterInput.setResponder(value -> {
            if (!applyingSnapshot) {
                draftDirty = true;
                draftDelay = 10;
            }
        });
        ThreeStateButton add = screen.addPageWidget(slateButton(ADD_FILTER_BUTTON[0], ADD_FILTER_BUTTON[1], ADD_FILTER_BUTTON[2],
                ADD_FILTER_BUTTON[3], Component.literal("+"), 1.5f, this::addFilter));
        add.active = filters.size() < TelemetryTerminalSnapshot.MAX_FILTERS;
    }

    private void addFilter() {
        String value = filterInput.getValue();
        if (!value.isBlank()) {
            flush();
            send(TelemetryTerminalActionPayload.Action.ADD_FILTER, value, false);
        }
        filterInput.setValue("");
    }

    private void send(TelemetryTerminalActionPayload.Action action, String text, boolean flag) {
        screen.send(new TelemetryTerminalActionPayload(screen.getMenu().getBlockPos(), action, TelemetryTerminalMode.AUTO_RECEIVE,
                GlareAddress.empty(), text, flag, new UUID(0, 0)));
    }

    @Override public void snapshotUpdated(TelemetryTerminalSnapshot snapshot) {
        if (snapshot.revision() != knownRevision) {
            knownRevision = snapshot.revision();
            boolean structuralChange = !knownFilters.equals(snapshot.receiveFilters())
                    || knownDiscardMode != snapshot.discardMatchingMessages();
            knownFilters = new ArrayList<>(snapshot.receiveFilters());
            knownDiscardMode = snapshot.discardMatchingMessages();
            if (!structuralChange) {
                if (!draftDirty && filterInput != null && !filterInput.isFocused()
                        && !filterInput.getValue().equals(snapshot.receiveFilterDraft())) {
                    applyingSnapshot = true;
                    filterInput.setValue(snapshot.receiveFilterDraft());
                    applyingSnapshot = false;
                }
                return;
            }
            offset = Math.min(offset, Math.max(0, snapshot.receiveFilters().size() - VISIBLE_FILTERS));
            screen.rebuildCurrentPage();
        }
    }

    @Override public void tick() {
        if (draftDirty && draftDelay-- <= 0) flush();
    }

    @Override public void flush() {
        if (!draftDirty || filterInput == null) return;
        send(TelemetryTerminalActionPayload.Action.SAVE_FILTER_DRAFT, filterInput.getValue(), false);
        draftDirty = false;
    }

    @Override public void removed() { flush(); }

    @Override public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        List<String> filters = screen.snapshot().receiveFilters();
        for (int row = 0; row < VISIBLE_FILTERS && offset + row < filters.size(); row++) {
            int rowY = FILTER_LIST[1] + row * FILTER_ROW_HEIGHT;
            boolean hovered = inRect(mouseX, mouseY, FILTER_LIST[0], rowY, FILTER_LIST[2], FILTER_ROW_HEIGHT);
            fill(graphics, FILTER_LIST[0] + ROW_INSET, rowY + 1, FILTER_LIST[2] - ROW_INSET * 2, FILTER_ROW_HEIGHT - 2,
                    hovered ? ROW_HOVER : 0xFF262626);
        }
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        float small = PanelText.snap(graphics, SCALE_SMALL);
        PanelText.draw(graphics, font(), Component.translatable("gui.resourceful_refinement.telemetry_terminal.receive_filters"),
                FILTER_HEADER[0] + 5, Math.round(FILTER_HEADER[1] + FILTER_HEADER[3] / 2f - 7 * small / 2f), TEXT_LABEL, false, small);

        List<String> filters = screen.snapshot().receiveFilters();
        float body = PanelText.snap(graphics, 1f);
        int textWidth = FILTER_LIST[2] - REMOVE_SIZE - ROW_INSET * 2 - 10;
        for (int row = 0; row < VISIBLE_FILTERS && offset + row < filters.size(); row++) {
            int rowY = FILTER_LIST[1] + row * FILTER_ROW_HEIGHT;
            String text = font().plainSubstrByWidth(filters.get(offset + row), (int) (textWidth / body));
            PanelText.draw(graphics, font(), Component.literal(text), FILTER_LIST[0] + 6,
                    rowY + (FILTER_ROW_HEIGHT - 7) / 2, TEXT_BRIGHT, false, body);
            int removeY = removeY(rowY);
            deleteBox(graphics, REMOVE_X, removeY, REMOVE_SIZE, inRect(mouseX, mouseY, REMOVE_X, removeY, REMOVE_SIZE, REMOVE_SIZE));
        }
        if (filters.isEmpty()) {
            PanelText.drawCentered(graphics, font(), Component.translatable("gui.resourceful_refinement.telemetry_terminal.no_filters"),
                    FILTER_LIST[0] + FILTER_LIST[2] / 2f, FILTER_LIST[1] + FILTER_LIST[3] / 2f - 4, TEXT_DIM, false, body);
        }
    }

    private static int removeY(int rowY) {
        return rowY + (FILTER_ROW_HEIGHT - REMOVE_SIZE) / 2;
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;
        List<String> filters = screen.snapshot().receiveFilters();
        for (int row = 0; row < VISIBLE_FILTERS && offset + row < filters.size(); row++) {
            int rowY = FILTER_LIST[1] + row * FILTER_ROW_HEIGHT;
            if (inRect(mouseX, mouseY, REMOVE_X, removeY(rowY), REMOVE_SIZE, REMOVE_SIZE)) {
                send(TelemetryTerminalActionPayload.Action.REMOVE_FILTER, filters.get(offset + row), false);
                return true;
            }
        }
        return false;
    }

    @Override public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        boolean overList = inRect(mouseX, mouseY, FILTER_LIST[0], FILTER_LIST[1],
                FILTER_SCROLL[0] + FILTER_SCROLL[2] - FILTER_LIST[0], FILTER_LIST[3]);
        return overList && scrollBar != null && scrollBar.scrollBy(-(int) Math.signum(scrollY));
    }
}
