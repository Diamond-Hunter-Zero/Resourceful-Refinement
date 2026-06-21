package com.resourceful_refinement.content.glare.terminal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.resourceful_refinement.content.glare.GlareAddress;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

public final class TelemetryAddressRenderer {
    private TelemetryAddressRenderer() {}

    public static void render(GuiGraphics graphics, GlareAddress address, int x, int y, int spacing, float scale) {

        PoseStack ms = graphics.pose();
        ms.pushPose();
        ms.scale(scale, scale, scale);
        graphics.renderItem(new ItemStack(BuiltInRegistries.ITEM.get(address.first())), (int)(x/scale), (int)(y/scale));
        graphics.renderItem(new ItemStack(BuiltInRegistries.ITEM.get(address.second())), (int)((x + spacing)/scale), (int)(y/scale));
        graphics.renderItem(new ItemStack(BuiltInRegistries.ITEM.get(address.third())), (int)((x + spacing * 2)/scale), (int)(y/scale));
        ms.popPose();
    }
}
