package com.resourceful_refinement.content.glare.terminal;

import com.resourceful_refinement.client.gui.widget.VerticalScrollBar;
import com.resourceful_refinement.content.glare.GlareAddress;
import com.resourceful_refinement.network.TelemetryTerminalActionPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

public class AutoReceiveTelemetryTerminalPage implements TelemetryTerminalPage {
    private static final int VISIBLE_FILTERS = 7;
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
        int x = screen.contentLeft();
        int y = screen.contentTop();
        Component toggle = Component.translatable(screen.snapshot().discardMatchingMessages()
                ? "gui.resourceful_refinement.telemetry_terminal.discard_messages"
                : "gui.resourceful_refinement.telemetry_terminal.keep_messages");
        screen.addPageWidget(Button.builder(toggle, b -> send(TelemetryTerminalActionPayload.Action.SET_DISCARD, "", !screen.snapshot().discardMatchingMessages()))
                .bounds(x + 91, y + 5, 126, 20).build());
        List<String> filters = screen.snapshot().receiveFilters();
        for (int row = 0; row < VISIBLE_FILTERS && offset + row < filters.size(); row++) {
            String filter = filters.get(offset + row);
            screen.addPageWidget(Button.builder(Component.literal("x"), b -> send(TelemetryTerminalActionPayload.Action.REMOVE_FILTER, filter, false))
                    .bounds(x + 276, y + 34 + row * 20, 18, 18).build());
        }
        scrollBar = screen.addPageWidget(new VerticalScrollBar(x + 299, y + 34, 6, 138,
                filters.size(), VISIBLE_FILTERS, offset, value -> offset = value));
        filterInput = screen.addPageWidget(new EditBox(screen.getMinecraft().font, x + 12, y + 178, 247, 18,
                Component.translatable("gui.resourceful_refinement.telemetry_terminal.filter")));
        filterInput.setMaxLength(TelemetryTerminalSnapshot.MAX_FILTER_LENGTH);
        filterInput.setHint(Component.translatable("gui.resourceful_refinement.telemetry_terminal.filter_hint"));
        filterInput.setValue(screen.snapshot().receiveFilterDraft());
        filterInput.setResponder(value -> {
            if (!applyingSnapshot) {
                draftDirty = true;
                draftDelay = 10;
            }
        });
        screen.addPageWidget(Button.builder(Component.literal("+"), b -> addFilter()).bounds(x + 263, y + 177, 32, 20).build());
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

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int x = screen.contentLeft();
        int y = screen.contentTop();
        List<String> filters = screen.snapshot().receiveFilters();
        graphics.drawCenteredString(screen.getMinecraft().font, Component.translatable("gui.resourceful_refinement.telemetry_terminal.receive_filters"), x + 153, y + 28, 0xFFE1E4E5);
        for (int row = 0; row < VISIBLE_FILTERS && offset + row < filters.size(); row++) {
            int rowY = y + 34 + row * 20;
            graphics.fill(x + 8, rowY, x + 272, rowY + 18, 0xFF202528);
            graphics.drawString(screen.getMinecraft().font, filters.get(offset + row), x + 13, rowY + 5, 0xFFD9DDDF, false);
        }
    }

    @Override public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int x = screen.contentLeft();
        int y = screen.contentTop();
        return mouseX >= x && mouseX < x + 307 && mouseY >= y + 31 && mouseY < y + 174
                && scrollBar != null && scrollBar.scrollBy(-(int) Math.signum(scrollY));
    }
}
