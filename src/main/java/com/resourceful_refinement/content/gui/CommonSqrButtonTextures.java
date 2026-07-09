package com.resourceful_refinement.content.gui;

import com.resourceful_refinement.ResourcefulRefinementMain;
import net.createmod.catnip.gui.TextureSheetSegment;
import net.createmod.catnip.gui.element.ScreenElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Common GUI sprites for buttons and elements in RR screens
 */
public enum CommonSqrButtonTextures implements ScreenElement, TextureSheetSegment {

    HOVER_CLEAR("gui_trash_select", 0, 0, 18, 18),
    HOVER_SAVE("gui_confirm_select", 0, 0, 18, 18),
    HOVER_CLOSE("gui_cancel_header_select", 0, 0, 18, 18);


    public final ResourceLocation location;
    private final int width;
    private final int height;
    private final int startX;
    private final int startY;

    CommonSqrButtonTextures(String location, int startX, int startY, int width, int height) {
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
        graphics.blit(location, x, y, startX, startY, width, height, 18, 18);
    }
}
