package com.resourceful_refinement.content.refill_station;

import com.resourceful_refinement.ResourcefulRefinementMain;
import net.createmod.catnip.gui.TextureSheetSegment;
import net.createmod.catnip.gui.element.ScreenElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * GUI sprites for the Fluid Refill Station configuration screen.
 * {@link #PANEL} is the full window background; button icons are baked into the panel art.
 */
public enum RefillStationGuiTextures implements ScreenElement, TextureSheetSegment {

    PANEL("refill_station_gui", 0, 0, 200, 102),

    HOVER_CLEAR("gui_trash_select", 0, 0, 18, 18),
    HOVER_SAVE("gui_confirm_select", 0, 0, 18, 18),
    HOVER_CLOSE("gui_cancel_header_select", 0, 0, 18, 18);

    public static final int PANEL_WIDTH = PANEL.width;
    public static final int PANEL_HEIGHT = PANEL.height;

    public final ResourceLocation location;
    private final int width;
    private final int height;
    private final int startX;
    private final int startY;

    RefillStationGuiTextures(String location, int startX, int startY, int width, int height) {
        this.location = ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "textures/gui/" + location + ".png");
        this.startX = startX;
        this.startY = startY;
        this.width = width;
        this.height = height;
    }

    @Override
    public ResourceLocation getLocation() {
        return location;
    }

    @Override
    public int getStartX() {
        return startX;
    }

    @Override
    public int getStartY() {
        return startY;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @OnlyIn(Dist.CLIENT)
    public void render(GuiGraphics graphics, int x, int y) {
        graphics.blit(location, x, y, startX, startY, width, height, 256, 256);
    }

    @OnlyIn(Dist.CLIENT)
    public void renderButton(GuiGraphics graphics, int x, int y) {
        graphics.blit(location, x, y, startX, startY, width, height, 18, 18);
    }
}
