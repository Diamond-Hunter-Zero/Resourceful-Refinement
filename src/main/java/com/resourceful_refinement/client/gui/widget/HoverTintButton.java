package com.resourceful_refinement.client.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/** Invisible hit target over button art baked into a panel; tints the area while hovered or focused. */
public class HoverTintButton extends AbstractButton {
    private final Runnable onPress;
    private final int tint;

    public HoverTintButton(int x, int y, int width, int height, Component narration, int tint, Runnable onPress) {
        super(x, y, width, height, narration);
        this.onPress = onPress;
        this.tint = tint;
    }

    @Override public void onPress() { onPress.run(); }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (isHovered() || isFocused()) graphics.fill(getX(), getY(), getX() + width, getY() + height, tint);
    }

    @Override protected void updateWidgetNarration(NarrationElementOutput output) { defaultButtonNarrationText(output); }
}
