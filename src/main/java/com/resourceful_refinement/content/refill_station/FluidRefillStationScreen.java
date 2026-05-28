package com.resourceful_refinement.content.refill_station;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.resourceful_refinement.network.SetRefillStationTrackingIdPayload;
import com.resourceful_refinement.registry.ModBlocks;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * Create-style configuration screen: blurred world background, custom panel texture, and block preview.
 */
public class FluidRefillStationScreen extends AbstractSimiScreen implements MenuAccess<FluidRefillStationMenu> {

    private static final int TITLE_COLOR = 0x592424;
    private static final int LABEL_COLOR = 0xB8B8B8;
    private static final int FOOTER_HINT_COLOR = 0x6E6E6E;

    private static final int INPUT_X = 16;
    private static final int INPUT_Y = 47;
    private static final int INPUT_WIDTH = RefillStationGuiTextures.PANEL_WIDTH - 32;
    private static final int INPUT_HEIGHT = 18;

    private final FluidRefillStationMenu menu;
    private EditBox trackingIdField;

    public FluidRefillStationScreen(FluidRefillStationMenu menu, Inventory inventory, Component title) {
        super(title);
        this.menu = menu;
    }

    @Override
    public FluidRefillStationMenu getMenu() {
        return menu;
    }

    @Override
    protected void init() {
        setWindowSize(RefillStationGuiTextures.PANEL_WIDTH, RefillStationGuiTextures.PANEL_HEIGHT);
        setWindowOffset(-20, 0);
        super.init();

        int fieldX = guiLeft + INPUT_X;
        int fieldY = guiTop + INPUT_Y;

        trackingIdField = new EditBox(font, fieldX, fieldY, INPUT_WIDTH, INPUT_HEIGHT, Component.empty());
        trackingIdField.setMaxLength(FluidRefillStationBlockEntity.MAX_TRACKING_ID_LENGTH);
        trackingIdField.setValue(menu.getInitialTrackingId());
        trackingIdField.setHint(Component.translatable("gui.resourceful_refinement.fluid_refill_station.tracking_id_hint"));
        trackingIdField.setBordered(false);
        trackingIdField.setTextColor(0xFFFFFF);
        trackingIdField.setTextColorUneditable(0xFFFFFF);
        addRenderableWidget(trackingIdField);
        setInitialFocus(trackingIdField);

        addRenderableWidget(new RefillStationHoverButton(
                guiLeft + RefillStationGuiTextures.PANEL_WIDTH - 33,
                guiTop + 4,
                RefillStationGuiTextures.HOVER_CLOSE,
                this::onClose));

        addRenderableWidget(new RefillStationHoverButton(
                guiLeft + RefillStationGuiTextures.PANEL_WIDTH - 59,
                guiTop + RefillStationGuiTextures.PANEL_HEIGHT - 24,
                RefillStationGuiTextures.HOVER_CLEAR,
                () -> trackingIdField.setValue("")));

        addRenderableWidget(new RefillStationHoverButton(
                guiLeft + RefillStationGuiTextures.PANEL_WIDTH - 33,
                guiTop + RefillStationGuiTextures.PANEL_HEIGHT - 24,
                RefillStationGuiTextures.HOVER_SAVE,
                this::saveAndClose));
    }

    private void saveAndClose() {
        PacketDistributor.sendToServer(new SetRefillStationTrackingIdPayload(menu.getBlockPos(), trackingIdField.getValue()));
        onClose();
    }

    @Override
    public void onClose() {
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.closeContainer();
        }
        super.onClose();
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
    protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int x = guiLeft;
        int y = guiTop;

        RefillStationGuiTextures.PANEL.render(graphics, x, y);

        graphics.drawString(font, title, x + 8, y + 4, TITLE_COLOR, false);

        Component label = Component.translatable("gui.resourceful_refinement.fluid_refill_station.tracking_id_label");
        graphics.drawString(font, label, x + 12, y + 29, LABEL_COLOR, false);

        Component footerHint = Component.translatable("gui.resourceful_refinement.fluid_refill_station.footer_hint");
        graphics.drawString(font, footerHint, x + 8, y + RefillStationGuiTextures.PANEL_HEIGHT - 24, FOOTER_HINT_COLOR, false);

        Component footerHint2 = Component.translatable("gui.resourceful_refinement.fluid_refill_station.footer_hint2");
        graphics.drawString(font, footerHint2, x + 8, y + RefillStationGuiTextures.PANEL_HEIGHT - 14, FOOTER_HINT_COLOR, false);


        renderBlockPreview(graphics, x, y);
    }

    private void renderBlockPreview(GuiGraphics graphics, int guiX, int guiY) {
        BlockPos pos = menu.getBlockPos();
        Level level = minecraft.level;
        if (level == null) {
            return;
        }

        RenderSystem.enableDepthTest();

        var pose = graphics.pose();
        pose.pushPose();
        TransformStack.of(pose)
                .pushPose()
                .translate((guiX + RefillStationGuiTextures.PANEL_WIDTH + 4), (guiY + RefillStationGuiTextures.PANEL_HEIGHT-34), 5)
                .scale(2.5f);
        GuiGameElement.of(ModBlocks.FLUID_REFILL_STATION).render(graphics);
        pose.popPose();
    }
}
