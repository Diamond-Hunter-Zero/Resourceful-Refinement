package com.resourceful_refinement.content.glare.terminal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.resourceful_refinement.content.glare.GlareAddress;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class TelemetryAddressRenderer {
    private TelemetryAddressRenderer() {}

    /** Draws the three address items left to right; {@code spacing} is in unscaled units, {@code scale} sizes the items. */
    public static void render(GuiGraphics graphics, GlareAddress address, int x, int y, int spacing, float scale) {
        PoseStack ms = graphics.pose();
        ResourceLocation[] parts = {address.first(), address.second(), address.third()};
        for (int i = 0; i < parts.length; i++) {
            ms.pushPose();
            ms.translate(x + spacing * i, y, 0);
            ms.scale(scale, scale, scale);
            graphics.renderItem(new ItemStack(BuiltInRegistries.ITEM.get(parts[i])), 0, 0);
            ms.popPose();
        }
    }
}
