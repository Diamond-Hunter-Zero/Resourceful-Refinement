package com.resourceful_refinement.client.gui.widget;

import com.resourceful_refinement.client.gui.PanelText;
import com.resourceful_refinement.content.gui.CommonSqrButtonTextures;
import com.resourceful_refinement.content.gui.CommonSqrButtonTextures.ButtonStyle;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.Nullable;

/**
 * Nine-slice button with three visual states: default, highlighted (hovered/focused) and toggled. A toggled button
 * represents the current selection (e.g. the open tab) and ignores clicks.
 * <p>
 * Content is an optional label and an optional 16x16 icon, arranged by {@link Layout}; both are drawn at their own
 * scale so small labels can sit under icons as in the panel mock-ups.
 */
public class ThreeStateButton extends AbstractButton {
    public enum Layout { CENTRED, ICON_ABOVE_LABEL, LABEL_THEN_ICON }

    private final ButtonStyle style;
    private final Runnable onPress;
    @Nullable private CommonSqrButtonTextures icon;
    private Layout layout = Layout.CENTRED;
    private float labelScale = 1f;
    private float iconScale = 1f;
    private boolean labelShadow;
    private int labelColour = 0xFFFFFFFF;
    private int highlightedLabelColour = 0xFFFFFFFF;
    private int toggledLabelColour = 0xFFFFFFFF;
    private int iconColour = 0xFFFFFFFF;
    private int toggledIconColour = 0xFFFFFFFF;
    private boolean toggled;
    private boolean snapLabelDown;

    public ThreeStateButton(int x, int y, int width, int height, Component label, ButtonStyle style, Runnable onPress) {
        super(x, y, width, height, label);
        this.style = style;
        this.onPress = onPress;
    }

    public ThreeStateButton icon(CommonSqrButtonTextures icon, float scale) {
        this.icon = icon;
        this.iconScale = scale;
        return this;
    }

    public ThreeStateButton layout(Layout layout) {
        this.layout = layout;
        return this;
    }

    public ThreeStateButton label(float scale, boolean shadow) {
        this.labelScale = scale;
        this.labelShadow = shadow;
        return this;
    }

    public ThreeStateButton labelColours(int normal, int highlighted, int toggled) {
        this.labelColour = normal;
        this.highlightedLabelColour = highlighted;
        this.toggledLabelColour = toggled;
        return this;
    }

    public ThreeStateButton iconColours(int normal, int toggled) {
        this.iconColour = normal;
        this.toggledIconColour = toggled;
        return this;
    }

    /** Snap the label scale downwards (never larger than requested), e.g. for labels sized to fit exactly. */
    public ThreeStateButton snapLabelDown() {
        this.snapLabelDown = true;
        return this;
    }

    public ThreeStateButton labelScale(float scale) {
        this.labelScale = scale;
        return this;
    }

    public boolean isToggled() { return toggled; }

    public ThreeStateButton setToggled(boolean toggled) {
        this.toggled = toggled;
        return this;
    }

    @Override
    public void onPress() {
        if (!toggled) onPress.run();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return !toggled && super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Focus only highlights during keyboard navigation; a mouse click leaves the button focused but shouldn't stay lit.
        boolean highlighted = active && !toggled && (isHovered()
                || (isFocused() && Minecraft.getInstance().getLastInputType().isKeyboard()));
        CommonSqrButtonTextures frame = style.forState(toggled, highlighted);
        RenderSystem.enableBlend();
        if (frame != null) frame.renderNineSlice(graphics, getX(), getY(), width, height);

        Font font = Minecraft.getInstance().font;
        Component label = getMessage();
        boolean hasLabel = !label.getString().isEmpty();
        float textScale = !hasLabel ? 0 : snapLabelDown ? PanelText.snapDown(graphics, labelScale) : PanelText.snap(graphics, labelScale);
        float iconSize = icon == null ? 0 : 16 * iconScale;
        float maxTextWidth = width - 4 - (layout == Layout.LABEL_THEN_ICON ? iconSize + 2 : 0);
        if (hasLabel && font.width(label) * textScale > maxTextWidth) {
            textScale = PanelText.snapDown(graphics, maxTextWidth / font.width(label));
        }
        int textColour = !active ? 0xFF7A7A7A : toggled ? toggledLabelColour : highlighted ? highlightedLabelColour : labelColour;
        int tint = toggled ? toggledIconColour : iconColour;

        float centreX = getX() + width / 2f;
        float centreY = getY() + height / 2f;
        float textWidth = hasLabel ? font.width(label) * textScale : 0;
        // Cap height of the default font is 7px, so centre on that rather than the 9px line height.
        float textHeight = 7 * textScale;

        switch (layout) {
            case ICON_ABOVE_LABEL -> {
                float gap = 1;
                float total = iconSize + (hasLabel ? gap + textHeight : 0);
                float top = Math.round(centreY - total / 2f);
                if (icon != null) drawIcon(graphics, Math.round(centreX - iconSize / 2f), top, tint);
                if (hasLabel) PanelText.draw(graphics, font, label, Math.round(centreX - textWidth / 2f),
                        top + iconSize + gap, textColour, labelShadow, textScale);
            }
            case LABEL_THEN_ICON -> {
                float gap = 2;
                float total = textWidth + (icon != null ? gap + iconSize : 0);
                float left = Math.round(centreX - total / 2f);
                // Bias up half a unit: the frames' 1px dark bottom edge makes true centre read low.
                if (hasLabel) PanelText.draw(graphics, font, label, left, (float) Math.floor(centreY - textHeight / 2f - 0.5f),
                        textColour, labelShadow, textScale);
                if (icon != null) drawIcon(graphics, left + textWidth + gap, Math.round(centreY - iconSize / 2f), tint);
            }
            default -> {
                if (icon != null) drawIcon(graphics, Math.round(centreX - iconSize / 2f), Math.round(centreY - iconSize / 2f), tint);
                if (hasLabel) PanelText.draw(graphics, font, label, Math.round(centreX - textWidth / 2f),
                        Math.round(centreY - textHeight / 2f), textColour, labelShadow, textScale);
            }
        }
        RenderSystem.disableBlend();
    }

    private void drawIcon(GuiGraphics graphics, float x, float y, int argb) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(iconScale, iconScale, 1);
        graphics.setColor(((argb >> 16) & 0xFF) / 255f, ((argb >> 8) & 0xFF) / 255f, (argb & 0xFF) / 255f, ((argb >>> 24) & 0xFF) / 255f);
        icon.render(graphics, 0, 0);
        graphics.setColor(1, 1, 1, 1);
        graphics.pose().popPose();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
