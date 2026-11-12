package com.resourceful_refinement.content.glare.terminal;

import com.resourceful_refinement.ResourcefulRefinementMain;
import net.createmod.catnip.gui.TextureSheetSegment;
import net.createmod.catnip.gui.element.ScreenElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Panel backgrounds for the Telemetry Terminal. Both sheets are 512x512 with the panel in the top-left corner;
 * all terminal layout coordinates are texels of these sheets (see {@link TelemetryTerminalLayout}).
 */
public enum TelemetryTerminalGuiTextures implements ScreenElement, TextureSheetSegment {

    /** Manual (Inbox / Compose) and Auto-Send. */
    SEND_PANEL("telemetry_terminal_send_gui"),
    /** Auto-Receive filter list. */
    FILTER_PANEL("telemetry_terminal_filter_gui");

    public static final int PANEL_WIDTH = 351;
    public static final int PANEL_HEIGHT = 216;
    private static final int SHEET_SIZE = 512;

    public final ResourceLocation location;

    TelemetryTerminalGuiTextures(String location) {
        this.location = ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "textures/gui/" + location + ".png");
    }

    @Override public ResourceLocation getLocation() { return location; }
    @Override public int getStartX() { return 0; }
    @Override public int getStartY() { return 0; }
    @Override public int getWidth() { return PANEL_WIDTH; }
    @Override public int getHeight() { return PANEL_HEIGHT; }

    @OnlyIn(Dist.CLIENT)
    public void render(GuiGraphics graphics, int x, int y) {
        // Texture size must be passed explicitly: the default 256x256 assumption makes UVs wrap and tile the sheet.
        graphics.blit(location, x, y, 0, 0, PANEL_WIDTH, PANEL_HEIGHT, SHEET_SIZE, SHEET_SIZE);
    }
}
