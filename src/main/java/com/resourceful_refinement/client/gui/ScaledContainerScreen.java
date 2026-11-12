package com.resourceful_refinement.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * Container screen whose whole contents live in a fixed "panel space" that is scaled to the window by a
 * {@link PixelPanelScaler}, independent of the player's GUI-scale setting.
 * <p>
 * Subclasses lay out widgets, slots and drawing in panel units with the panel's top-left at
 * ({@link #leftPos}, {@link #topPos}) = (0, 0), exactly as they would in an unscaled screen. This class applies the
 * transform when rendering and converts mouse input back into panel space, so vanilla widgets, slots and hit
 * tests work unchanged.
 * <p>
 * Caveats: {@code GuiGraphics.enableScissor} ignores the pose, so avoid scissoring widgets inside the panel; and
 * widget {@code Tooltip}s are positioned in panel space, so prefer {@code setTooltipForNextRenderPass} (which is
 * drawn after this transform is popped, at the real mouse position).
 */
public abstract class ScaledContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    protected final PixelPanelScaler scaler;

    protected ScaledContainerScreen(T menu, Inventory inventory, Component title, int panelWidth, int panelHeight) {
        super(menu, inventory, title);
        imageWidth = panelWidth;
        imageHeight = panelHeight;
        scaler = new PixelPanelScaler(panelWidth, panelHeight);
    }

    @Override
    protected void init() {
        scaler.update();
        super.init();
        leftPos = 0;
        topPos = 0;
    }

    // Report real (GUI-space) bounds to mods such as JEI that position overlays around the container.
    @Override public int getGuiLeft() { return Mth.floor(scaler.originX()); }
    @Override public int getGuiTop() { return Mth.floor(scaler.originY()); }
    @Override public int getXSize() { return Mth.ceil(imageWidth * scaler.scale()); }
    @Override public int getYSize() { return Mth.ceil(imageHeight * scaler.scale()); }

    protected int panelMouseX(double guiX) { return Mth.floor(scaler.toPanelX(guiX)); }
    protected int panelMouseY(double guiY) { return Mth.floor(scaler.toPanelY(guiY)); }

    @Override
    public final void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderTransparentBackground(graphics);
        int panelX = panelMouseX(mouseX);
        int panelY = panelMouseY(mouseY);
        graphics.pose().pushPose();
        graphics.pose().translate(scaler.originX(), scaler.originY(), 0);
        graphics.pose().scale(scaler.scale(), scaler.scale(), 1);
        super.render(graphics, panelX, panelY, partialTick);
        renderPanelForeground(graphics, panelX, panelY, partialTick);
        graphics.pose().popPose();
        // Slot tooltips are drawn unscaled at the real cursor.
        renderTooltip(graphics, mouseX, mouseY);
    }

    /** Called after widgets and slots, still in panel space. */
    protected void renderPanelForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {}

    /** The full-screen dim is drawn in GUI space by {@link #render}; here only the panel art is drawn. */
    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBg(graphics, partialTick, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {}

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(scaler.toPanelX(mouseX), scaler.toPanelY(mouseY));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return panelMouseClicked(scaler.toPanelX(mouseX), scaler.toPanelY(mouseY), button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return panelMouseReleased(scaler.toPanelX(mouseX), scaler.toPanelY(mouseY), button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return panelMouseDragged(scaler.toPanelX(mouseX), scaler.toPanelY(mouseY), button,
                dragX / scaler.scale(), dragY / scaler.scale());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return panelMouseScrolled(scaler.toPanelX(mouseX), scaler.toPanelY(mouseY), scrollX, scrollY);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return super.isMouseOver(scaler.toPanelX(mouseX), scaler.toPanelY(mouseY));
    }

    // Panel-space input hooks. Override these instead of the mouse* methods; call super to reach widgets/slots.
    protected boolean panelMouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    protected boolean panelMouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(mouseX, mouseY, button);
    }

    protected boolean panelMouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    protected boolean panelMouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
}
