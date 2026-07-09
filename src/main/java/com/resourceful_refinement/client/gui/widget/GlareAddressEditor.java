package com.resourceful_refinement.client.gui.widget;

import com.resourceful_refinement.content.glare.GlareAddress;
import com.resourceful_refinement.content.glare.terminal.TelemetryItemPickerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

/** Shared, non-consuming three-item GLARE address editor used by terminal and launchpad screens. */
public class GlareAddressEditor extends AbstractWidget {
    private final Screen parent;
    private final Consumer<GlareAddress> responder;
    private GlareAddress address;
    private int slotSize = 1;
    private int spacing = 1;

    public GlareAddressEditor(Screen parent, int x, int y, int slotSize, int spacing, GlareAddress address, Consumer<GlareAddress> responder) {
        super(x, y, spacing * 3 - 3, slotSize, Component.empty());
        this.parent = parent;
        this.address = address;
        this.responder = responder;
        this.slotSize = slotSize;
        this.spacing = spacing;
    }

    public GlareAddress getAddress() {
        return address;
    }

    public void setAddress(GlareAddress address) {
        this.address = address;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ResourceLocation[] ids = {address.first(), address.second(), address.third()};
        for (int slot = 0; slot < 3; slot++) {
            int slotX = getX() + slot * spacing;
            boolean hovered = mouseX >= slotX && mouseX < slotX + slotSize
                    && mouseY >= getY() && mouseY < getY() + slotSize;

            if (isHovered)
                graphics.fill(slotX, getY(), slotX + slotSize, getY() + slotSize, 0xff4d5459);


            /*graphics.fill(slotX, getY(), slotX + slotSize, getY() + slotSize,
                    hovered ? 0xFF69757A : 0xFF171B1E);
            graphics.renderOutline(slotX, getY(), slotSize, slotSize, active ? 0xFF899499 : 0xFF4D5559);*/

            Item item = BuiltInRegistries.ITEM.get(ids[slot]);
            graphics.renderItem(new ItemStack(item), slotX, getY());
            if (hovered && item != net.minecraft.world.item.Items.AIR) {
                graphics.renderTooltip(Minecraft.getInstance().font, new ItemStack(item), mouseX, mouseY);
            }
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (!active) return;
        int slot = (int) (mouseX - getX()) / spacing;
        if (slot < 0 || slot > 2) return;
        Minecraft.getInstance().setScreen(new TelemetryItemPickerScreen(parent, item -> setPart(slot, item)));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (active && visible && isMouseOver(mouseX, mouseY) && button == 1) {
            int slot = (int) (mouseX - getX()) / spacing;
            if (slot >= 0 && slot <= 2) {
                setPart(slot, net.minecraft.world.item.Items.AIR);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void setPart(int slot, Item item) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        address = switch (slot) {
            case 0 -> new GlareAddress(id, address.second(), address.third());
            case 1 -> new GlareAddress(address.first(), id, address.third());
            default -> new GlareAddress(address.first(), address.second(), id);
        };
        responder.accept(address);
    }

    @Override
    protected void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
