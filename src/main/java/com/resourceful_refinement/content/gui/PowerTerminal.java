package com.resourceful_refinement.content.gui;

import com.resourceful_refinement.network.ToggleGlareNetworkPayload;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Locale;
import java.util.function.Supplier;

public class PowerTerminal {
    private static final int PANEL = 0xFF1A1A1A;
    private static final int BUTTON_FRAME = 0xFF9B9B9B;
    private static final int BUTTON_ON = 0xFFF51C29;
    private static final int BUTTON_OVERLOADED = 0xFF9E001D;
    private static final int BUTTON_SHADOW = 0xFF5F1112;
    private static final int TEXT = 0xFF9A9A9A;
    private static final int LUX_TEXT = 0xFFA5E8F4;
    private static final int WARNING = 0xFFE00028;

    private final Supplier<? extends GlareNetworkGuiData> data;
    private final BlockPos ownerPos;
    private final LuxHistoryGraph graph;
    private int x;
    private int y;
    private int width;
    private int height;
    private int buttonX;
    private int buttonY;
    private int buttonSize;

    public PowerTerminal(int x, int y, int width, int height, BlockPos ownerPos,
            Supplier<? extends GlareNetworkGuiData> data) {
        this.ownerPos = ownerPos;
        this.data = data;
        this.graph = new LuxHistoryGraph(x, y, width, height, data);
        setBounds(x, y, width, height);
    }

    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = Math.max(90, width);
        this.height = Math.max(118, height);
        buttonSize = Math.max(32, Math.min(this.width / 2, this.height / 3));
        buttonX = this.x + (this.width - buttonSize) / 2;
        buttonY = this.y + 28;
        int graphY = Math.min(this.y + this.height - 58, buttonY + buttonSize + 16);
        graph.setBounds(this.x + 12, graphY, this.width - 24, Math.max(28, this.height - graphY + this.y - 30));
    }

    public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY) {
        GlareNetworkGuiData snapshot = data.get();
        if (snapshot == null) {
            snapshot = GlareNetworkSnapshot.EMPTY;
        }
        graphics.fill(x, y, x + width, y + height, PANEL);

        if (snapshot.overloaded()) {
            drawCentered(graphics, font, Component.literal("OVERLOADED!"), x + width / 2, y + 10, WARNING);
        }

        boolean hovered = isInButton(mouseX, mouseY);
        int frame = hovered ? 0xFFC5C5C5 : BUTTON_FRAME;
        graphics.fill(buttonX - 4, buttonY - 4, buttonX + buttonSize + 4, buttonY + buttonSize + 4, frame);
        graphics.fill(buttonX, buttonY, buttonX + buttonSize, buttonY + buttonSize, snapshot.overloaded() ? BUTTON_OVERLOADED : BUTTON_ON);
        graphics.fill(buttonX, buttonY + buttonSize * 2 / 3, buttonX + buttonSize, buttonY + buttonSize - 7, BUTTON_ON);
        graphics.fill(buttonX, buttonY + buttonSize - 7, buttonX + buttonSize, buttonY + buttonSize, BUTTON_SHADOW);
        drawPowerGlyph(graphics, buttonX, buttonY, buttonSize, snapshot.overloaded());

        Component buttonLabel = Component.literal(snapshot.overloaded() ? "Restore Network" : "Overload Network");
        drawCentered(graphics, font, buttonLabel, x + width / 2, buttonY + buttonSize + 7, hovered ? LUX_TEXT : TEXT);

        graph.render(graphics);
        Component readout = Component.literal(String.format(Locale.ROOT, "%02d / %02d  LUX",
                Math.max(0, snapshot.currentLux()), Math.max(0, snapshot.maxLux())));
        drawCentered(graphics, font, readout, x + width / 2, y + height - 18, LUX_TEXT);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || !isInButton(mouseX, mouseY)) {
            return false;
        }
        PacketDistributor.sendToServer(new ToggleGlareNetworkPayload(ownerPos));
        return true;
    }

    private boolean isInButton(double mouseX, double mouseY) {
        return mouseX >= buttonX - 4 && mouseX < buttonX + buttonSize + 4
                && mouseY >= buttonY - 4 && mouseY < buttonY + buttonSize + 4;
    }

    private static void drawPowerGlyph(GuiGraphics graphics, int x, int y, int size, boolean overloaded) {
        int color = overloaded ? 0xFF5A0012 : 0xFF82060B;
        int centerX = x + size / 2;
        int centerY = y + size / 3;
        int radius = Math.max(8, size / 5);
        graphics.fill(centerX - 2, centerY + radius / 2, centerX + 2, centerY + radius + size / 5, color);
        graphics.fill(centerX - radius, centerY - 2, centerX - radius + 4, centerY + radius, color);
        graphics.fill(centerX + radius - 4, centerY - 2, centerX + radius, centerY + radius, color);
        graphics.fill(centerX - radius / 2, centerY - radius, centerX + radius / 2, centerY - radius + 4, color);
    }

    private static void drawCentered(GuiGraphics graphics, Font font, Component text, int centerX, int y, int color) {
        graphics.drawString(font, text, centerX - font.width(text) / 2, y, color, false);
    }
}
