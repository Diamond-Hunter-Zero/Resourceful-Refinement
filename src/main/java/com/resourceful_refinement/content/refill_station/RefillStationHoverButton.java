package com.resourceful_refinement.content.refill_station;

import net.createmod.catnip.gui.widget.AbstractSimiWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Invisible hit target over a baked GUI icon; draws a hover overlay texture when highlighted.
 */
public class RefillStationHoverButton extends AbstractSimiWidget {

    private final RefillStationGuiTextures hoverTexture;

    public RefillStationHoverButton(int x, int y, RefillStationGuiTextures hoverTexture, Runnable onClick) {
        super(x, y, hoverTexture.getWidth(), hoverTexture.getHeight(), Component.empty());
        this.hoverTexture = hoverTexture;
        withCallback(onClick);
    }

    @Override
    protected void doRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!visible) {
            return;
        }
        if (isHovered || isFocused()) {
            hoverTexture.renderButton(graphics, getX(), getY());
        }
    }
}
