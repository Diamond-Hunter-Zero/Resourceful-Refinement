package com.resourceful_refinement.content.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.client.gui.PanelText;
import com.resourceful_refinement.network.ToggleGlareNetworkPayload;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Locale;
import java.util.function.Supplier;

/**
 * Shared Power Terminal panel: a power switch that overloads / restores the GLARE network, and a Lux usage history
 * graph. Drawn 1:1 on {@code power_terminal_gui.png} ({@link #WIDTH} x {@link #HEIGHT} texels), so host screens
 * should place it in a scaled panel space (see {@code ScaledContainerScreen}).
 */
public class PowerTerminal {
    public static final int WIDTH = 176;
    public static final int HEIGHT = 219;

    private static final ResourceLocation BACKGROUND = texture("power_terminal_gui");
    private static final ResourceLocation SWITCH_ON = texture("power_switch_on");
    private static final ResourceLocation SWITCH_OFF = texture("power_switch_off");
    private static final ResourceLocation SWITCH_OFF_DARK = texture("power_switch_off_dark");
    private static final int SHEET_SIZE = 256;
    private static final int SWITCH_SIZE = 64;

    // Regions of the panel art, {x, y, width, height}
    private static final int[] SWITCH = {56, 14, SWITCH_SIZE, SWITCH_SIZE};
    private static final int[] LABEL_BOX = {38, 90, 100, 14};
    private static final int[] GRAPH = {13, 128, 150, 60};
    private static final int[] READOUT_BOX = {38, 196, 100, 14};

    private static final int TEXT = 0xFF9A9A9A;
    private static final int LUX_TEXT = 0xFFA5E8F4;
    private static final int WARNING = 0xFFE00028;

    private final Supplier<? extends GlareNetworkGuiData> data;
    private final BlockPos ownerPos;
    private final LuxHistoryGraph graph;
    private int x;
    private int y;

    public PowerTerminal(int x, int y, BlockPos ownerPos, Supplier<? extends GlareNetworkGuiData> data) {
        this.ownerPos = ownerPos;
        this.data = data;
        this.graph = new LuxHistoryGraph(0, 0, GRAPH[2], GRAPH[3], data).withoutBackground();
        setPosition(x, y);
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
        graph.setBounds(x + GRAPH[0], y + GRAPH[1], GRAPH[2], GRAPH[3]);
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "textures/gui/" + name + ".png");
    }

    public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY) {
        GlareNetworkGuiData snapshot = data.get();
        if (snapshot == null) snapshot = GlareNetworkSnapshot.EMPTY;
        boolean overloaded = snapshot.overloaded();
        boolean hovered = isInSwitch(mouseX, mouseY);

        RenderSystem.enableBlend();
        graphics.blit(BACKGROUND, x, y, 0, 0, WIDTH, HEIGHT, SHEET_SIZE, SHEET_SIZE);
        // Off/overloaded uses the dark sprite, brightening to the lit "off" sprite on hover to invite a restore.
        ResourceLocation switchSprite = !overloaded ? SWITCH_ON : hovered ? SWITCH_OFF : SWITCH_OFF_DARK;
        graphics.blit(switchSprite, x + SWITCH[0], y + SWITCH[1], 0, 0, SWITCH_SIZE, SWITCH_SIZE, SWITCH_SIZE, SWITCH_SIZE);
        RenderSystem.disableBlend();
        if (hovered && !overloaded) {
            graphics.fill(x + SWITCH[0], y + SWITCH[1], x + SWITCH[0] + SWITCH_SIZE, y + SWITCH[1] + SWITCH_SIZE, 0x22FFFFFF);
        }

        float scale = PanelText.snap(graphics, 1f);
        Component label = Component.literal(overloaded ? "Network Overloaded!" : "Network Online");
        int labelColour = hovered ? LUX_TEXT : overloaded ? WARNING : TEXT;
        drawInBox(graphics, font, label, LABEL_BOX, labelColour, scale);

        graph.render(graphics);

        Component readout = Component.literal(String.format(Locale.ROOT, "%02d / %02d  LUX",
                Math.max(0, snapshot.currentLux()), Math.max(0, snapshot.maxLux())));
        drawInBox(graphics, font, readout, READOUT_BOX, LUX_TEXT, scale);
    }

    private void drawInBox(GuiGraphics graphics, Font font, Component text, int[] box, int colour, float desiredScale) {
        float scale = font.width(text) * desiredScale > box[2] - 4
                ? PanelText.snapDown(graphics, (box[2] - 4f) / font.width(text)) : desiredScale;
        PanelText.drawCentered(graphics, font, text, x + box[0] + box[2] / 2f,
                Math.round(y + box[1] + box[3] / 2f - 7 * scale / 2f), colour, false, scale);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || !isInSwitch(mouseX, mouseY)) return false;
        PacketDistributor.sendToServer(new ToggleGlareNetworkPayload(ownerPos));
        return true;
    }

    private boolean isInSwitch(double mouseX, double mouseY) {
        return mouseX >= x + SWITCH[0] && mouseX < x + SWITCH[0] + SWITCH[2]
                && mouseY >= y + SWITCH[1] && mouseY < y + SWITCH[1] + SWITCH[3];
    }
}
