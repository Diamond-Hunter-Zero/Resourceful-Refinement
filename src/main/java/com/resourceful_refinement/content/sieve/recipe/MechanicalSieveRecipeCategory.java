package com.resourceful_refinement.content.sieve.recipe;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.forge_mould.recipe.MechanicalForgeMouldRecipe;
import com.resourceful_refinement.registry.ModBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class MechanicalSieveRecipeCategory implements IRecipeCategory<MechanicalSieveRecipe> {

    public static final RecipeType<MechanicalSieveRecipe> TYPE =
            RecipeType.create(ResourcefulRefinementMain.MOD_ID, "mechanical_sieve", MechanicalSieveRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final ResourceLocation background_texture = ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "textures/gui/jei/jei_sieve_backdrop.png");

    public MechanicalSieveRecipeCategory(IGuiHelper guiHelper) {
        // Define your background (usually a 176xSomething texture)
        this.background = guiHelper.createDrawable(background_texture, 0, 0, 175, 82);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.MECHANICAL_SIEVE.get()));
    }

    @Override
    public RecipeType<MechanicalSieveRecipe> getRecipeType() { return TYPE; }

    @Override
    public Component getTitle() { return Component.translatable("Fluid Sieving"); }

    @Override
    public IDrawable getBackground() { return background; }

    @Override
    public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MechanicalSieveRecipe recipe, IFocusGroup focuses) {
        // Define where items go. Note: 1.21.1 uses RecipeHolder for recipes.
        // If 'recipe' is a RecipeHolder, use recipe.value() to get the actual recipe class.
        if (!recipe.getRollableResults().isEmpty())
            builder.addSlot(RecipeIngredientRole.OUTPUT, 124, 12).addItemStack(recipe.getRollableResults().getFirst().getStack());
        builder.addSlot(RecipeIngredientRole.INPUT, 36, 12).addFluidStack(recipe.getFluidIngredients().getFirst().getFluids()[0].getFluid());
        if (!recipe.getFluidResults().isEmpty())
            builder.addSlot(RecipeIngredientRole.OUTPUT, 124, 52).addFluidStack(recipe.getFluidResults().getFirst().getFluid());
    }

    @Override
    public void draw(MechanicalSieveRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;

        int centreXPos = (this.background.getWidth() / 2); // Horizontal position of your choice
        int centreYPos = (this.background.getHeight() / 2); // Subtract 8 (half of 16px item) to center

        // 1. Render Processing Time (e.g., "200 ticks")
        String timeText = recipe.getProcessingDuration() + "t";
        int timeWidth = font.width(timeText);
        // Draw text at x=100, y=40 (adjust based on your background)
        guiGraphics.drawString(font, timeText, 15, centreYPos + 31, 0xFFF5F5F5, false);

        // Render fluid stack amounts
        int inputAmount = recipe.getFluidIngredients().getFirst().amount();
        String inputAmountText = inputAmount + "mB";
        int inputAmountWidth = font.width(inputAmountText);
        guiGraphics.drawString(font, inputAmountText, 36 + 8 - inputAmountWidth/2f, 12 + 20, 0xFF808080, false);

        if (!recipe.getFluidResults().isEmpty())
        {
            int outputAmount = recipe.getFluidResults().getFirst().getAmount();
            String outputAmountText = outputAmount + "mB";
            int outputAmountWidth = font.width(outputAmountText);
            guiGraphics.drawString(font, outputAmountText, 124 + 8 - outputAmountWidth/2f, 52 + 20, 0xFF808080, false);
        }

        // Render drop chance
        if (!recipe.getRollableResults().isEmpty())
        {
            int chance = (int)(recipe.getRollableResults().getFirst().getChance() * 100f);
            String chanceText = chance + "%";
            int chanceWidth = font.width(chanceText);
            guiGraphics.drawString(font, chanceText, this.background.getWidth() - 32, centreYPos - 24, 0xFF808080, false);
        }

        // Render block model
        ItemStack forgeBlock = new ItemStack(ModBlocks.MECHANICAL_SIEVE.get());
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();

        pose.translate(-centreXPos, -centreYPos-2, 0);
        pose.scale(2.0f, 2.0f, 2.0f);
        guiGraphics.renderFakeItem(forgeBlock, centreXPos-8, centreYPos-8);
        pose.popPose();
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, MechanicalSieveRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        // 1. Coordinates for our vertically centered block (from previous step)
        int xPos = (this.background.getWidth() / 2) - 8;
        int yPos = (this.background.getHeight() / 2) + 7;

        // 2. Check if mouse is over the block
        if (mouseX >= xPos && mouseX <= xPos + 16 && mouseY >= yPos && mouseY <= yPos + 16) {
            // Add the block name (the bold/color formatting is handled by the block's getName())
            tooltip.add(ModBlocks.CASTING_DEPOT.get().getName());
        }
    }

}
