package com.resourceful_refinement.content.pug;

import com.resourceful_refinement.ResourcefulRefinementMain;
import net.createmod.catnip.gui.TextureSheetSegment;
import net.createmod.catnip.gui.element.ScreenElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;


public enum LaunchControllerGuiTextures implements ScreenElement, TextureSheetSegment {

    SEND_PANEL("launchpad_send_gui", 0, 0, 351, 187),
    RECEIVE_PANEL("launchpad_receive_gui", 0, 0, 351, 187);

    public static final int PANEL_WIDTH = SEND_PANEL.width;
    public static final int PANEL_HEIGHT = SEND_PANEL.height;

    public final ResourceLocation location;
    private final int width;
    private final int height;
    private final int startX;
    private final int startY;

    LaunchControllerGuiTextures(String location, int startX, int startY, int width, int height) {
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
        graphics.blit(location, x, y, startX, startY, width, height, 512, 512);
    }
}
