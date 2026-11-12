package com.resourceful_refinement.content.glare.terminal;

import com.resourceful_refinement.client.gui.PanelText;
import com.resourceful_refinement.client.gui.widget.ThreeStateButton;
import com.resourceful_refinement.client.gui.widget.VerticalScrollBar;
import com.resourceful_refinement.content.gui.CommonSqrButtonTextures;
import com.resourceful_refinement.content.gui.CommonSqrButtonTextures.ButtonStyle;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.function.IntConsumer;

import static com.resourceful_refinement.content.glare.terminal.TelemetryTerminalLayout.*;

/** Factory and drawing helpers shared by the terminal pages, so every page uses the same skins and type sizes. */
final class TelemetryTerminalWidgets {
    private TelemetryTerminalWidgets() {}

    static Font font() { return Minecraft.getInstance().font; }

    /** Inbox / Compose view toggle: icon above a tiny label. */
    static ThreeStateButton viewToggle(int[] rect, String key, CommonSqrButtonTextures icon, Runnable onPress) {
        return new ThreeStateButton(rect[0], rect[1], rect[2], rect[3], Component.translatable(key), ButtonStyle.GREY_BORDERLESS, onPress)
                .icon(icon, SCALE_TINY).layout(ThreeStateButton.Layout.ICON_ABOVE_LABEL).label(SCALE_TINY, false);
    }

    /** Footer action, e.g. "Send âœ‰" or "Discard ðŸ—‘": label followed by icon. */
    static ThreeStateButton actionButton(int[] rect, String key, CommonSqrButtonTextures icon, Runnable onPress) {
        return new ThreeStateButton(rect[0], rect[1], rect[2], rect[3], Component.translatable(key), ButtonStyle.GREY_BORDERLESS, onPress)
                .icon(icon, SCALE_SMALL).layout(ThreeStateButton.Layout.LABEL_THEN_ICON).label(SCALE_SMALL, false);
    }

    static ThreeStateButton iconButton(int[] rect, CommonSqrButtonTextures icon, float iconScale, Runnable onPress) {
        return new ThreeStateButton(rect[0], rect[1], rect[2], rect[3], Component.empty(), ButtonStyle.GREY_BORDERLESS, onPress)
                .icon(icon, iconScale);
    }

    static ThreeStateButton slateButton(int x, int y, int width, int height, Component label, float scale, Runnable onPress) {
        return new ThreeStateButton(x, y, width, height, label, ButtonStyle.GREY_SLATE, onPress).label(scale, false);
    }

    static VerticalScrollBar scrollBar(int[] rect, int total, int visible, int offset, IntConsumer onChanged) {
        return new VerticalScrollBar(rect[0], rect[1], rect[2], rect[3], total, visible, offset, onChanged)
                .withStyle(false, SCROLL_THUMB, SCROLL_THUMB_HOVER, 1);
    }

    /** Centred caption in the strip above the left-hand list ("12 Messages", "3 Contacts"). */
    static void listHeader(GuiGraphics graphics, Component text) {
        PanelText.drawCentered(graphics, font(), text, LIST_HEADER[0] + LIST_HEADER[2] / 2f, LIST_HEADER[1] + 2,
                TEXT_DIM, false, PanelText.snap(graphics, 1f));
    }

    /** Two-line "Sender / Address:" style label to the left of the address slots. */
    static void addressLabel(GuiGraphics graphics, String keyPrefix) {
        float scale = PanelText.snap(graphics, SCALE_SMALL);
        PanelText.draw(graphics, font(), Component.translatable(keyPrefix + "_line_1"), ADDRESS_LABEL_X, ADDRESS_LABEL_Y, TEXT_LABEL, false, scale);
        PanelText.draw(graphics, font(), Component.translatable(keyPrefix + "_line_2"), ADDRESS_LABEL_X, ADDRESS_LABEL_Y + ADDRESS_LABEL_LINE, TEXT_LABEL, false, scale);
    }

    /** Centred small text in the top-right info box (timestamp, characters remaining). */
    static void infoText(GuiGraphics graphics, Component text, int colour) {
        float scale = PanelText.snap(graphics, SCALE_SMALL);
        PanelText.drawCentered(graphics, font(), text, INFO_BOX[0] + INFO_BOX[2] / 2f,
                Math.round(INFO_BOX[1] + INFO_BOX[3] / 2f - 7 * scale / 2f), colour, false, scale);
    }

    /** Up to two lines of small text in the footer status bar. */
    static void statusText(GuiGraphics graphics, Component text, int colour) {
        if (text.getString().isEmpty()) return;
        PanelText.drawWrapped(graphics, font(), text, STATUS_BAR[0] + 3, STATUS_BAR[1] + 2, STATUS_BAR[2] - 6, STATUS_BAR[3] - 2,
                colour, PanelText.snap(graphics, SCALE_SMALL));
    }

    static void fill(GuiGraphics graphics, int x, int y, int width, int height, int colour) {
        graphics.fill(x, y, x + width, y + height, colour);
    }

    /** Small slate "x" used for per-row delete affordances that only appear on hover. */
    static void deleteBox(GuiGraphics graphics, int x, int y, int size, boolean hovered) {
        RenderSystem.enableBlend();
        (hovered ? CommonSqrButtonTextures.GREY_SLATE_BUTTON_HIGHLIGHTED : CommonSqrButtonTextures.GREY_SLATE_BUTTON)
                .renderNineSlice(graphics, x, y, size, size);
        RenderSystem.disableBlend();
        PanelText.drawCentered(graphics, font(), Component.literal("x"), x + size / 2f, y + (size - 6) / 2f - 1,
                0xFFFFFFFF, false, 1f);
    }

    static boolean inRect(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
