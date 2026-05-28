package com.resourceful_refinement.content.refill_station;

import com.mojang.blaze3d.platform.InputConstants;
import com.resourceful_refinement.network.SetRefillStationTrackingIdPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public class FluidRefillStationScreen extends AbstractContainerScreen<FluidRefillStationMenu> {

    private EditBox trackingIdField;

    public FluidRefillStationScreen(FluidRefillStationMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 92;
    }

    @Override
    protected void init() {
        super.init();
        int fieldX = leftPos + 8;
        int fieldY = topPos + 32;

        trackingIdField = new EditBox(font, fieldX, fieldY, 160, 18, Component.translatable("gui.resourceful_refinement.fluid_refill_station.tracking_id"));
        trackingIdField.setMaxLength(FluidRefillStationBlockEntity.MAX_TRACKING_ID_LENGTH);
        trackingIdField.setValue(menu.getInitialTrackingId());
        trackingIdField.setHint(Component.translatable("gui.resourceful_refinement.fluid_refill_station.tracking_id_hint"));
        addRenderableWidget(trackingIdField);
        setInitialFocus(trackingIdField);

        addRenderableWidget(Button.builder(
                        Component.translatable("gui.resourceful_refinement.fluid_refill_station.save"),
                        button -> saveAndClose())
                .bounds(leftPos + 8, topPos + 58, 78, 20)
                .build());

        addRenderableWidget(Button.builder(
                        Component.translatable("gui.resourceful_refinement.fluid_refill_station.clear"),
                        button -> trackingIdField.setValue(""))
                .bounds(leftPos + 90, topPos + 58, 78, 20)
                .build());
    }

    private void saveAndClose() {
        PacketDistributor.sendToServer(new SetRefillStationTrackingIdPayload(menu.getBlockPos(), trackingIdField.getValue()));
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.closeContainer();
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (minecraft == null) {
            return false;
        }

        InputConstants.Key key = InputConstants.getKey(keyCode, scanCode);

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            onClose();
            return true;
        }

        // AbstractContainerScreen routes inventory / other menu keys to closeContainer + other screens.
        if (isBlockedMenuHotkey(key)) {
            return true;
        }

        if (trackingIdField.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            saveAndClose();
            return true;
        }

        if (getFocused() != null && getFocused().keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }

        return false;
    }

    /**
     * Hotkeys that would close this screen or open another GUI (handled by {@link AbstractContainerScreen}
     * or global keybinds). Escape is handled separately above.
     */
    private boolean isBlockedMenuHotkey(InputConstants.Key key) {
        return matchesKey(minecraft.options.keyInventory, key)
                || matchesKey(minecraft.options.keyAdvancements, key)
                || matchesKey(minecraft.options.keyChat, key)
                || matchesKey(minecraft.options.keyCommand, key)
                || matchesKey(minecraft.options.keySocialInteractions, key)
                || matchesKey(minecraft.options.keyPlayerList, key);
    }

    private static boolean matchesKey(KeyMapping mapping, InputConstants.Key key) {
        return mapping.isActiveAndMatches(key);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return trackingIdField.charTyped(codePoint, modifiers);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xF0101010);
        graphics.fill(leftPos + 1, topPos + 1, leftPos + imageWidth - 1, topPos + imageHeight - 1, 0xFF2B2B2B);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        trackingIdField.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 8, 0xE0E0E0, false);
        graphics.drawString(font,
                Component.translatable("gui.resourceful_refinement.fluid_refill_station.tracking_id_label"),
                8, 22, 0xA0A0A0, false);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        // No item slots
    }
}
