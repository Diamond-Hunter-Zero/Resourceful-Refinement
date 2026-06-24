package com.resourceful_refinement.content.gui;

import net.createmod.catnip.gui.widget.AbstractSimiWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Invisible hit target over a baked GUI icon; draws a hover overlay texture when highlighted.
 */
public class SqrHoverButton extends AbstractSimiWidget {

    private final CommonSqrButtonTextures commonSqrButtonTextures;

    public SqrHoverButton(int x, int y, CommonSqrButtonTextures commonSqrButtonTextures, Runnable onClick) {
        super(x, y, commonSqrButtonTextures.getWidth(), commonSqrButtonTextures.getHeight(), Component.empty());
        this.commonSqrButtonTextures = commonSqrButtonTextures;
        withCallback(onClick);
    }

    @Override
    protected void doRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!visible) {
            return;
        }
        if (isHovered || isFocused()) {
            commonSqrButtonTextures.render(graphics, getX(), getY());
        }
    }
}
