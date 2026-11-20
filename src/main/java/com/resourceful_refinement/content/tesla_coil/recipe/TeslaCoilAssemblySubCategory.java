package com.resourceful_refinement.content.tesla_coil.recipe;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.resourceful_refinement.registry.ModBlocks;
import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory;
import com.simibubi.create.content.processing.sequenced.SequencedRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

/** The Tesla Coil step column in Create's Sequenced Assembly JEI page: a coil pair and the charge count. */
public class TeslaCoilAssemblySubCategory extends SequencedAssemblySubCategory {

    private final ItemStack coil = new ItemStack(ModBlocks.TESLA_COIL.get());

    public TeslaCoilAssemblySubCategory() {
        super(25);
    }

    @Override
    public void draw(SequencedRecipe<?> recipe, GuiGraphics graphics, double mouseX, double mouseY, int index) {
        PoseStack ms = graphics.pose();
        int centre = getWidth() / 2;
        drawCoil(graphics, centre - 5, 90.0F);
        drawCoil(graphics, centre + 5, -90.0F);

        if (recipe.getRecipe() instanceof TeslaCoilRecipe tesla && tesla.getCharges() > 1) {
            String text = "x" + tesla.getCharges();
            ms.pushPose();
            ms.translate(0, 0, 200);
            graphics.drawString(Minecraft.getInstance().font, text, centre - Minecraft.getInstance().font.width(text) / 2,
                    24, 0xFFF5F5F5, true);
            ms.popPose();
        }
    }

    private void drawCoil(GuiGraphics graphics, int x, float rotation) {
        PoseStack ms = graphics.pose();
        ms.pushPose();
        ms.translate(x, 14, 0);
        ms.mulPose(Axis.ZP.rotationDegrees(rotation));
        ms.scale(0.75F, 0.75F, 0.75F);
        graphics.renderFakeItem(coil, -8, -8);
        ms.popPose();
    }
}
