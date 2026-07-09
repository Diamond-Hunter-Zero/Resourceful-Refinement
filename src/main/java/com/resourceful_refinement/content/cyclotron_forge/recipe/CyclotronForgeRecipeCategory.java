package com.resourceful_refinement.content.cyclotron_forge.recipe;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.registry.ModBlocks;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.Arrays;
import java.util.List;

public class CyclotronForgeRecipeCategory implements IRecipeCategory<CyclotronForgeRecipe> {
    public static final RecipeType<CyclotronForgeRecipe> TYPE =
            RecipeType.create(ResourcefulRefinementMain.MOD_ID, "cyclotron_forge", CyclotronForgeRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public CyclotronForgeRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(176, 92);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.CYCLOTRON_CONTROLLER.get()));
    }

    @Override
    public RecipeType<CyclotronForgeRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Cyclotron Forge");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CyclotronForgeRecipe recipe, IFocusGroup focuses) {
        List<SizedIngredient> itemInputs = recipe.getCombinedIngredients();
        addItemInput(builder, itemInputs, 0, 12, 18);
        addItemInput(builder, itemInputs, 1, 12, 50);

        List<SizedFluidIngredient> fluidInputs = recipe.getFluidIngredients();
        addFluidInput(builder, fluidInputs, 0, 42, 18);
        addFluidInput(builder, fluidInputs, 1, 42, 50);

        List<ProcessingOutput> itemOutputs = recipe.getRollableResults();
        for (int i = 0; i < Math.min(itemOutputs.size(), 4); i++) {
            int x = 124 + (i % 2) * 22;
            int y = 18 + (i / 2) * 32;
            builder.addSlot(RecipeIngredientRole.OUTPUT, x, y).addItemStack(itemOutputs.get(i).getStack());
        }

        List<FluidStack> fluidOutputs = recipe.getFluidResults();
        for (int i = 0; i < Math.min(fluidOutputs.size(), 2); i++) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 94, 18 + i * 32).addFluidStack(fluidOutputs.get(i).getFluid());
        }
    }

    @Override
    public void draw(CyclotronForgeRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics,
            double mouseX, double mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;

        guiGraphics.drawString(font, "Items", 10, 6, 0xFF666666, false);
        guiGraphics.drawString(font, "Fluids", 40, 6, 0xFF666666, false);
        guiGraphics.drawString(font, "Out", 103, 6, 0xFF666666, false);

        String timeText = String.format("%.0fs", recipe.getProcessingDuration() / 20f);
        guiGraphics.drawString(font, timeText, 72, 19, 0xFF666666, false);
        guiGraphics.drawString(font, recipe.getCoilLength() + " coil", 68, 34, 0xFF666666, false);
        guiGraphics.drawString(font, recipe.getMinRpm() + " rpm", 68, 49, 0xFF666666, false);

        int luxPeak = recipe.getLuxCurve().stream().mapToInt(Integer::intValue).max().orElse(0);
        if (luxPeak > 0) {
            guiGraphics.drawString(font, luxPeak + " lux", 68, 64, 0xFF666666, false);
        }

        drawFluidAmounts(recipe, guiGraphics, font);
    }

    private static void addItemInput(IRecipeLayoutBuilder builder, List<SizedIngredient> inputs, int index, int x, int y) {
        if (inputs.size() <= index || inputs.get(index).ingredient().isEmpty()) return;
        builder.addSlot(RecipeIngredientRole.INPUT, x, y).addItemStacks(stacksFor(inputs.get(index)));
    }

    private static void addFluidInput(IRecipeLayoutBuilder builder, List<SizedFluidIngredient> inputs, int index, int x, int y) {
        if (inputs.size() <= index) return;
        SizedFluidIngredient ingredient = inputs.get(index);
        if (ingredient.getFluids().length == 0) return;
        builder.addSlot(RecipeIngredientRole.INPUT, x, y).addFluidStack(ingredient.getFluids()[0].getFluid());
    }

    private static List<ItemStack> stacksFor(SizedIngredient ingredient) {
        return Arrays.stream(ingredient.getItems())
                .map(ItemStack::copy)
                .peek(stack -> stack.setCount(ingredient.count()))
                .toList();
    }

    private static void drawFluidAmounts(CyclotronForgeRecipe recipe, GuiGraphics guiGraphics, Font font) {
        List<SizedFluidIngredient> fluidInputs = recipe.getFluidIngredients();
        for (int i = 0; i < Math.min(fluidInputs.size(), 2); i++) {
            String text = fluidInputs.get(i).amount() + "mB";
            int width = font.width(text);
            guiGraphics.drawString(font, text, 50 - width / 2, 38 + i * 32, 0xFF666666, false);
        }

        List<FluidStack> fluidOutputs = recipe.getFluidResults();
        for (int i = 0; i < Math.min(fluidOutputs.size(), 2); i++) {
            String text = fluidOutputs.get(i).getAmount() + "mB";
            int width = font.width(text);
            guiGraphics.drawString(font, text, 102 - width / 2, 38 + i * 32, 0xFF666666, false);
        }
    }
}
