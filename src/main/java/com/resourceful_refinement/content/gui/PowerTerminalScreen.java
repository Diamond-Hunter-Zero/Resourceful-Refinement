package com.resourceful_refinement.content.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;

public class PowerTerminalScreen extends AbstractContainerScreen<PowerTerminalMenu> {
    private PowerTerminal terminal;

    public PowerTerminalScreen(PowerTerminalMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 192;
        imageHeight = 176;
        inventoryLabelY = 1000;
        titleLabelY = 1000;
    }

    @Override
    protected void init() {
        super.init();
        terminal = new PowerTerminal(leftPos, topPos, imageWidth, imageHeight, menu.getBlockPos(), this::snapshot);
    }

    private GlareNetworkGuiData snapshot() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return GlareNetworkSnapshot.EMPTY;
        }
        BlockEntity blockEntity = minecraft.level.getBlockEntity(menu.getBlockPos());
        return blockEntity instanceof GlareNetworkSnapshotProvider provider
                ? provider.getSyncedGlareNetworkSnapshot() : GlareNetworkSnapshot.EMPTY;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        if (terminal != null) {
            terminal.render(graphics, font, mouseX, mouseY);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return terminal != null && terminal.mouseClicked(mouseX, mouseY, button)
                || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {}
}
