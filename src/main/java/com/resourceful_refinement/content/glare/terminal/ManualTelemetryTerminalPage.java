package com.resourceful_refinement.content.glare.terminal;

import com.resourceful_refinement.client.gui.PanelText;
import com.resourceful_refinement.client.gui.widget.ThreeStateButton;
import com.resourceful_refinement.client.gui.widget.VerticalScrollBar;
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

/** Manual mode: an Inbox view and a Compose view, switched by the toggle pair at the top of the left column. */
public class ManualTelemetryTerminalPage implements TelemetryTerminalPage {
    private static final int VISIBLE_MESSAGES = 5;
    private static final int MESSAGE_ROW_HEIGHT = LIST[3] / VISIBLE_MESSAGES;
    private static final int PREVIEW_ADDRESS_SPACING = 14;

    private final TelemetryTerminalScreen screen;
    private boolean compose;
    private ComposeTelemetryTerminalPage composePage;
    private VerticalScrollBar inboxScroll;
    private ThreeStateButton discardButton;
    private int inboxOffset;
    private int selectedIndex;

    public ManualTelemetryTerminalPage(TelemetryTerminalScreen screen) {
        this.screen = screen;
        compose = screen.snapshot().manualComposeOpen();
    }

    @Override public void init() {
        ThreeStateButton inbox = screen.addPageWidget(viewToggle(INBOX_BUTTON,
                "gui.resourceful_refinement.telemetry_terminal.inbox", CommonSqrButtonTextures.MAIL_ICON, () -> setCompose(false)));
        ThreeStateButton write = screen.addPageWidget(viewToggle(COMPOSE_BUTTON,
                "gui.resourceful_refinement.telemetry_terminal.compose_short", CommonSqrButtonTextures.WRITE_ICON, () -> setCompose(true)));
        inbox.setToggled(!compose);
        write.setToggled(compose);

        if (compose) {
            composePage = new ComposeTelemetryTerminalPage(screen, false);
            composePage.init();
        } else {
            int count = screen.snapshot().inbox().size();
            inboxScroll = screen.addPageWidget(scrollBar(LIST_SCROLL, count, VISIBLE_MESSAGES, inboxOffset, offset -> inboxOffset = offset));
            discardButton = screen.addPageWidget(actionButton(ACTION_BUTTON,
                    "gui.resourceful_refinement.telemetry_terminal.discard", CommonSqrButtonTextures.TRASH_ICON, this::discardSelected));
            discardButton.active = count > 0;
        }
    }

    private void setCompose(boolean compose) {
        if (this.compose == compose) return;
        if (composePage != null) composePage.flush();
        screen.send(new TelemetryTerminalActionPayload(screen.getMenu().getBlockPos(), TelemetryTerminalActionPayload.Action.SET_MANUAL_VIEW,
                TelemetryTerminalMode.MANUAL, GlareAddress.empty(), "", compose, new UUID(0, 0)));
    }

    private void discardSelected() {
        List<GlareMessage> inbox = screen.snapshot().inbox();
        if (selectedIndex < 0 || selectedIndex >= inbox.size()) return;
        screen.send(new TelemetryTerminalActionPayload(screen.getMenu().getBlockPos(), TelemetryTerminalActionPayload.Action.DISCARD_MESSAGE,
                TelemetryTerminalMode.MANUAL, GlareAddress.empty(), "", false, inbox.get(selectedIndex).id()));
    }

    @Override public void snapshotUpdated(TelemetryTerminalSnapshot snapshot) {
        if (snapshot.manualComposeOpen() != compose) {
            compose = snapshot.manualComposeOpen();
            screen.rebuildCurrentPage();
            return;
        }
        if (composePage != null) {
            composePage.snapshotUpdated(snapshot);
            return;
        }
        int count = snapshot.inbox().size();
        selectedIndex = Math.max(0, Math.min(selectedIndex, count - 1));
        if (inboxScroll != null) inboxScroll.setTotalItems(count);
        if (discardButton != null) discardButton.active = count > 0;
    }

    @Override public void tick() { if (composePage != null) composePage.tick(); }
    @Override public void flush() { if (composePage != null) composePage.flush(); }
    @Override public void removed() { if (composePage != null) composePage.removed(); }

    @Override public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (composePage != null) composePage.renderBackground(graphics, mouseX, mouseY, partialTick);
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (composePage != null) {
            composePage.render(graphics, mouseX, mouseY, partialTick);
            return;
        }
        List<GlareMessage> inbox = screen.snapshot().inbox();
        listHeader(graphics, Component.translatable("gui.resourceful_refinement.telemetry_terminal.message_count", inbox.size()));
        addressLabel(graphics, "gui.resourceful_refinement.telemetry_terminal.sender_address");

        float small = PanelText.snap(graphics, SCALE_SMALL);
        for (int row = 0; row < VISIBLE_MESSAGES && inboxOffset + row < inbox.size(); row++) {
            int index = inboxOffset + row;
            GlareMessage message = inbox.get(index);
            int rowY = LIST[1] + row * MESSAGE_ROW_HEIGHT;
            boolean hovered = inRect(mouseX, mouseY, LIST[0], rowY, LIST[2], MESSAGE_ROW_HEIGHT);
            if (index == selectedIndex) fill(graphics, LIST[0], rowY, LIST[2], MESSAGE_ROW_HEIGHT, ROW_SELECTED);
            else if (hovered) fill(graphics, LIST[0], rowY, LIST[2], MESSAGE_ROW_HEIGHT, ROW_HOVER);
            TelemetryAddressRenderer.render(graphics, message.from(), LIST[0] + 3, rowY + 2, PREVIEW_ADDRESS_SPACING, SCALE_SMALL);
            String preview = font().plainSubstrByWidth(message.body().replace('\n', ' '), (int) ((LIST[2] - 6) / small));
            PanelText.draw(graphics, font(), Component.literal(preview), LIST[0] + 3, rowY + 15, TEXT_DIM, false, small);
        }

        if (inbox.isEmpty()) {
            PanelText.drawCentered(graphics, font(), Component.translatable("gui.resourceful_refinement.telemetry_terminal.empty_inbox"),
                    MESSAGE_AREA[0] + MESSAGE_AREA[2] / 2f, MESSAGE_AREA[1] + MESSAGE_AREA[3] / 2f - 4, TEXT_DIM, false, 1f);
            return;
        }
        GlareMessage selected = inbox.get(Math.min(selectedIndex, inbox.size() - 1));
        TelemetryAddressRenderer.render(graphics, selected.from(), ADDRESS_SLOTS_X, ADDRESS_SLOTS_Y, ADDRESS_SPACING, 1f);
        infoText(graphics, Component.literal(formatTime(selected.gameTime())), TEXT_DIM);
        PanelText.drawWrapped(graphics, font(), Component.literal(selected.body()), MESSAGE_AREA[0] + 4, MESSAGE_AREA[1] + 4,
                MESSAGE_AREA[2] - 8, MESSAGE_AREA[3] - 6, TEXT_BRIGHT, PanelText.snap(graphics, 1f));
    }

    private static String formatTime(long gameTime) {
        long day = gameTime / 24000L + 1;
        long minute = (gameTime % 24000L) * 60L / 1000L;
        return "Day " + day + "  " + String.format("%02d:%02d", (minute / 60 + 6) % 24, minute % 60);
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (composePage != null) return composePage.mouseClicked(mouseX, mouseY, button);
        if (button == 0 && TelemetryTerminalLayout.inside(LIST, mouseX, mouseY)) {
            int row = (int) (mouseY - LIST[1]) / MESSAGE_ROW_HEIGHT;
            if (row < VISIBLE_MESSAGES && inboxOffset + row < screen.snapshot().inbox().size()) {
                selectedIndex = inboxOffset + row;
                return true;
            }
        }
        return false;
    }

    @Override public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (composePage != null) return composePage.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        boolean overList = inRect(mouseX, mouseY, LIST[0], LIST[1], LIST_SCROLL[0] + LIST_SCROLL[2] - LIST[0], LIST[3]);
        return overList && inboxScroll != null && inboxScroll.scrollBy(-(int) Math.signum(scrollY));
    }
}
