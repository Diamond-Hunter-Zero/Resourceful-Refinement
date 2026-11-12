package com.resourceful_refinement.content.gui;

import com.resourceful_refinement.client.gui.ScaledContainerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;

public class PowerTerminalScreen extends ScaledContainerScreen<PowerTerminalMenu> {
    private PowerTerminal terminal;

    public PowerTerminalScreen(PowerTerminalMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, PowerTerminal.WIDTH, PowerTerminal.HEIGHT);
        inventoryLabelY = 1000;
        titleLabelY = 1000;
    }

    @Override
    protected void init() {
        super.init();
        terminal = new PowerTerminal(leftPos, topPos, menu.getBlockPos(), this::snapshot);
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
    protected boolean panelMouseClicked(double mouseX, double mouseY, int button) {
        return terminal != null && terminal.mouseClicked(mouseX, mouseY, button)
                || super.panelMouseClicked(mouseX, mouseY, button);
    }
}
