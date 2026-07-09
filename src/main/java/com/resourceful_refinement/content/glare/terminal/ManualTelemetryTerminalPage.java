package com.resourceful_refinement.content.glare.terminal;

import com.resourceful_refinement.client.gui.widget.VerticalScrollBar;
import com.resourceful_refinement.content.glare.GlareMessage;
import com.resourceful_refinement.network.TelemetryTerminalActionPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.UUID;

public class ManualTelemetryTerminalPage implements TelemetryTerminalPage {
    private final TelemetryTerminalScreen screen;
    private boolean compose;
    private ComposeTelemetryTerminalPage composePage;
    private VerticalScrollBar inboxScroll;
    private Button inboxButton;
    private int inboxOffset;
    private int selectedIndex;

    public ManualTelemetryTerminalPage(TelemetryTerminalScreen screen) {
        this.screen = screen;
        compose = screen.snapshot().manualComposeOpen();
    }

    @Override public void init() {
        int x = screen.contentLeft();
        int y = screen.contentTop();
        int count = screen.snapshot().inbox().size();
        inboxButton = screen.addPageWidget(Button.builder(inboxLabel(count), b -> setCompose(false)).bounds(x + 2, y + 1, 100, 19).build());
        screen.addPageWidget(Button.builder(Component.translatable("gui.resourceful_refinement.telemetry_terminal.compose"), b -> setCompose(true))
                .bounds(x + 104, y + 1, 100, 19).build());
        if (compose) {
            composePage = new ComposeTelemetryTerminalPage(screen, false, 24);
            composePage.init();
        } else {
            inboxScroll = screen.addPageWidget(new VerticalScrollBar(x + 93, y + 26, 6, 158,
                    count, 6, inboxOffset, offset -> inboxOffset = offset));
            screen.addPageWidget(Button.builder(Component.translatable("gui.resourceful_refinement.telemetry_terminal.discard"), b -> discardSelected())
                    .bounds(x + 238, y + 164, 61, 18).build());
        }
    }

    private void setCompose(boolean compose) {
        if (this.compose == compose) return;
        if (composePage != null) composePage.flush();
        screen.send(new TelemetryTerminalActionPayload(screen.getMenu().getBlockPos(), TelemetryTerminalActionPayload.Action.SET_MANUAL_VIEW,
                TelemetryTerminalMode.MANUAL, com.resourceful_refinement.content.glare.GlareAddress.empty(), "", compose, new UUID(0, 0)));
    }

    private void discardSelected() {
        List<GlareMessage> inbox = screen.snapshot().inbox();
        if (selectedIndex < 0 || selectedIndex >= inbox.size()) return;
        screen.send(new TelemetryTerminalActionPayload(screen.getMenu().getBlockPos(), TelemetryTerminalActionPayload.Action.DISCARD_MESSAGE,
                TelemetryTerminalMode.MANUAL, com.resourceful_refinement.content.glare.GlareAddress.empty(), "", false, inbox.get(selectedIndex).id()));
    }

    @Override public void snapshotUpdated(TelemetryTerminalSnapshot snapshot) {
        if (inboxButton != null) inboxButton.setMessage(inboxLabel(snapshot.inbox().size()));
        if (snapshot.manualComposeOpen() != compose) {
            compose = snapshot.manualComposeOpen();
            screen.rebuildCurrentPage();
            return;
        }
        selectedIndex = Math.max(0, Math.min(selectedIndex, snapshot.inbox().size() - 1));
        if (composePage != null) composePage.snapshotUpdated(snapshot);
    }

    private static Component inboxLabel(int count) {
        return count > 0
                ? Component.translatable("gui.resourceful_refinement.telemetry_terminal.inbox_count", count)
                : Component.translatable("gui.resourceful_refinement.telemetry_terminal.inbox");
    }

    @Override public void tick() { if (composePage != null) composePage.tick(); }
    @Override public void flush() { if (composePage != null) composePage.flush(); }
    @Override public void removed() { if (composePage != null) composePage.removed(); }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (composePage != null) {
            composePage.render(graphics, mouseX, mouseY, partialTick);
            return;
        }
        int x = screen.contentLeft();
        int y = screen.contentTop();
        List<GlareMessage> inbox = screen.snapshot().inbox();
        for (int row = 0; row < 6 && inboxOffset + row < inbox.size(); row++) {
            int index = inboxOffset + row;
            GlareMessage message = inbox.get(index);
            int rowY = y + 27 + row * 26;
            int colour = index == selectedIndex ? 0x607FA3AD : (mouseX >= x && mouseX < x + 91 && mouseY >= rowY && mouseY < rowY + 24 ? 0x40FFFFFF : 0x20101010);
            graphics.fill(x, rowY, x + 91, rowY + 24, colour);
            TelemetryAddressRenderer.render(graphics, message.from(), x + 3, rowY + 1, 18, 0.75f);
            String preview = message.body().replace('\n', ' ');
            if (preview.length() > 12) preview = preview.substring(0, 12) + "...";
            graphics.drawString(screen.getMinecraft().font, preview, x + 3, rowY + 15, 0xFF9FA8AC, false);
        }
        if (inbox.isEmpty()) {
            graphics.drawCenteredString(screen.getMinecraft().font, Component.translatable("gui.resourceful_refinement.telemetry_terminal.empty_inbox"), x + 190, y + 92, 0xFF9FA8AC);
            return;
        }
        GlareMessage selected = inbox.get(Math.min(selectedIndex, inbox.size() - 1));
        int panelX = x + 105;
        TelemetryAddressRenderer.render(graphics, selected.from(), panelX + 4, y + 29, 20, 1);
        long day = selected.gameTime() / 24000L + 1;
        long minute = (selected.gameTime() % 24000L) * 60L / 1000L;
        String time = "Day " + day + " " + String.format("%02d:%02d", (minute / 60 + 6) % 24, minute % 60);
        graphics.drawString(screen.getMinecraft().font, time, x + 299 - screen.getMinecraft().font.width(time), y + 33, 0xFF9FA8AC, false);
        int textY = y + 53;
        for (var line : screen.getMinecraft().font.split(Component.literal(selected.body()), 188)) {
            if (textY > y + 155) break;
            graphics.drawString(screen.getMinecraft().font, line, panelX + 4, textY, 0xFFE1E4E5, false);
            textY += screen.getMinecraft().font.lineHeight;
        }
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (composePage != null) return composePage.mouseClicked(mouseX, mouseY, button);
        int x = screen.contentLeft();
        int y = screen.contentTop();
        if (button == 0 && mouseX >= x && mouseX < x + 91 && mouseY >= y + 27 && mouseY < y + 183) {
            int row = (int) (mouseY - (y + 27)) / 26;
            if (inboxOffset + row < screen.snapshot().inbox().size()) {
                selectedIndex = inboxOffset + row;
                return true;
            }
        }
        return false;
    }

    @Override public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (composePage != null) return composePage.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        int x = screen.contentLeft();
        int y = screen.contentTop();
        return mouseX >= x && mouseX < x + 101 && mouseY >= y + 24 && mouseY < y + 187
                && inboxScroll != null && inboxScroll.scrollBy(-(int) Math.signum(scrollY));
    }
}
