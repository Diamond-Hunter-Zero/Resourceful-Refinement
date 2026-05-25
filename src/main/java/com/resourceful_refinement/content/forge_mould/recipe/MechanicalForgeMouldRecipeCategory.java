package com.resourceful_refinement.content.forge_mould.recipe;

import com.mojang.blaze3d.vertex.PoseStack;
import com.resourceful_refinement.ResourcefulRefinementMain;
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
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;

public class MechanicalForgeMouldRecipeCategory implements IRecipeCategory<MechanicalForgeMouldRecipe> {

    public static final RecipeType<MechanicalForgeMouldRecipe> TYPE =
            RecipeType.create(ResourcefulRefinementMain.MOD_ID, "mechanical_forge_mould", MechanicalForgeMouldRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final ResourceLocation background_texture = ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "textures/gui/jei/jei_forge_backdrop.png");
    private final String CASTING_WARNING = "Requires Casting Depot";

    public MechanicalForgeMouldRecipeCategory(IGuiHelper guiHelper) {
        // Define your background (usually a 176xSomething texture)
        this.background = guiHelper.createDrawable(background_texture, 0, 0, 175, 82);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.MECHANICAL_FORGE_MOULD.get()));
    }

    @Override
    public RecipeType<MechanicalForgeMouldRecipe> getRecipeType() { return TYPE; }

    @Override
    public Component getTitle() { return Component.translatable("Fluid Forging"); }

    @Override
    public IDrawable getBackground() { return background; }

    @Override
    public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MechanicalForgeMouldRecipe recipe, IFocusGroup focuses) {
        // Define where items go. Note: 1.21.1 uses RecipeHolder for recipes.
        // If 'recipe' is a RecipeHolder, use recipe.value() to get the actual recipe class.
        if (!recipe.getIngredients().isEmpty())
            builder.addSlot(RecipeIngredientRole.INPUT, 39, 32).addIngredients(recipe.getIngredients().getFirst());
        builder.addSlot(RecipeIngredientRole.INPUT, 15, 32).addFluidStack(recipe.getFluidIngredients().getFirst().getFluids()[0].getFluid());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 139, 33).addItemStack(recipe.getResultItem(null));
    }

    @Override
    public void draw(MechanicalForgeMouldRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;

        int centreXPos = (this.background.getWidth() / 2); // Horizontal position of your choice
        int centreYPos = (this.background.getHeight() / 2); // Subtract 8 (half of 16px item) to center

        // Render Processing Time (e.g., "200 ticks")
        String timeText = recipe.getProcessingDuration() + "t";
        int timeWidth = font.width(timeText);
        guiGraphics.drawString(font, timeText, centreXPos + 20, centreYPos - 28, 0xFF808080, false);

        // Render Fluid amount
        String fluidText = recipe.getFluidIngredients().getFirst().amount() + "mB";
        int fluidWidth = font.width(fluidText);
        guiGraphics.drawString(font, fluidText, 15 + 8 - fluidWidth/2, 32 + 8 + 12, 0xFF808080, false);

        // Forge block
        ItemStack forgeBlock = new ItemStack(ModBlocks.MECHANICAL_FORGE_MOULD.get());
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(-centreXPos, -centreYPos, 0);
        pose.scale(2.0f, 2.0f, 2.0f);
        guiGraphics.renderFakeItem(forgeBlock, centreXPos-8, centreYPos - 20);
        pose.popPose();

        // Render casting requirement
        if (recipe.isCasting())
        {
            // 1. Get the block you want to display (e.g., the machine or a required catalyst)
            ItemStack blockStack = new ItemStack(ModBlocks.CASTING_DEPOT.get());

            // 2. Calculate vertical center
            // background.getHeight() returns the height you defined in the constructor
            //centreXPos -= 8;
            //centreYPos += 7;

            // 3. Render the block
            pose.pushPose();
            pose.translate(0, 0, 0);
            pose.scale(1.5f, 1.5f, 1.5f);
            guiGraphics.renderFakeItem(blockStack, (int)(centreXPos/1.5f - 8), (int)(centreYPos/1.5f + 2));
            pose.popPose();

            int castingWidth = font.width(CASTING_WARNING);
            // Draw text at x=100, y=40 (adjust based on your background)
            guiGraphics.drawString(font, CASTING_WARNING, centreXPos - (castingWidth/2f) + 8, centreYPos + 30, 0xFF808080, false);
        }
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, MechanicalForgeMouldRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        if (!recipe.isCasting()) return;

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
