package com.resourceful_refinement.content.coating;

import com.resourceful_refinement.registry.ModDataComponents;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.IItemDecorator;

public class CoatingItemDecorator implements IItemDecorator {
    @Override
    public boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
        if (stack.has(ModDataComponents.COATING_DATA.get())) {
            CoatingData data = stack.get(ModDataComponents.COATING_DATA.get());
            if (data != null) {
                // Draw a secondary bar above the default one
                int integrity = data.integrity();
                int max = data.type().getMaxDurability();
                int color = data.type().getColor();

                float fill = Math.max(0.0F, (float) integrity / (float) max);
                int barWidth = Math.round(13.0F * fill);
                
                // Usually vanilla durability bar is at yOffset + 13. We'll draw ours at yOffset + 11 if item is damaged
                int yPos = yOffset + 13;
                if (stack.isDamaged())
                    yPos -= 2;
                
                // Use a PoseStack translation to move the rendering to the front
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0, 0, 200);

                // Background
                guiGraphics.fill(xOffset + 2, yPos, xOffset + 15, yPos + 2, 0xFF000000);
                // Foreground
                guiGraphics.fill(xOffset + 2, yPos, xOffset + 2 + barWidth, yPos + 1, color | 0xFF000000);

                guiGraphics.pose().popPose();
            }
        }
        return false; // Return false so vanilla decorators still run
    }
}
