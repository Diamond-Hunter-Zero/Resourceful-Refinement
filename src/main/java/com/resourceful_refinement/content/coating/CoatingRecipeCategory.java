package com.resourceful_refinement.content.coating;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.forge_mould.recipe.CoatingRecipe;
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
import net.minecraft.world.item.Items;

public class CoatingRecipeCategory implements IRecipeCategory<CoatingRecipe> {

    public static final RecipeType<CoatingRecipe> TYPE =
            RecipeType.create(ResourcefulRefinementMain.MOD_ID, "coating", CoatingRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final ResourceLocation background_texture = ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "textures/gui/jei/jei_coating_backdrop.png");

    public CoatingRecipeCategory(IGuiHelper guiHelper) {
        // Define your background (usually a 176xSomething texture)
        this.background = guiHelper.createDrawable(background_texture, 0, 0, 175, 82);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.CASTING_DEPOT.get()));
    }

    @Override
    public RecipeType<CoatingRecipe> getRecipeType() { return TYPE; }

    @Override
    public Component getTitle() { return Component.translatable("Fluid Coating"); }

    @Override
    public IDrawable getBackground() { return background; }

    @Override
    public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CoatingRecipe recipe, IFocusGroup focuses) {
        // Define where items go. Note: 1.21.1 uses RecipeHolder for recipes.
        // If 'recipe' is a RecipeHolder, use recipe.value() to get the actual recipe class.
        if (!recipe.getItemIngredient().isEmpty())
            builder.addSlot(RecipeIngredientRole.INPUT, 61, 9).addIngredients(recipe.getItemIngredient());
        builder.addSlot(RecipeIngredientRole.INPUT, 88, 9).addFluidStack(recipe.getFluidIngredient().getFluids()[0].getFluid());
    }

    @Override
    public void draw(CoatingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;

        int centreXPos = (this.background.getWidth() / 2); // Horizontal position of your choice
        int centreYPos = (this.background.getHeight() / 2); // Subtract 8 (half of 16px item) to center

        // Render Processing Time (e.g., "200 ticks")
        String timeText = recipe.getProcessingDuration() + "t";
        int timeWidth = font.width(timeText);
        guiGraphics.drawString(font, timeText, this.background.getWidth()-34, 13  - font.lineHeight/2, 0xFFF5F5F5, false);

        // Render Fluid amount
        String fluidText = recipe.getFluidIngredient().amount() + "mB";
        int fluidWidth = font.width(fluidText);
        guiGraphics.drawString(font, fluidText, 88 + 8 - fluidWidth/2, 30, 0xFF808080, false);

        // Render coating effect text
        String coatingTitle = recipe.getCoatingType().getSerializedName();
        String coatingDesc = recipe.getCoatingType().getDescription();
        String coatingDurability = "+" + recipe.getCoatingType().getMaxDurability() + " durability";

        int textYPos = this.background.getHeight() - font.lineHeight - 4;

        guiGraphics.drawString(font, coatingTitle, this.background.getWidth() - 128, textYPos - (font.lineHeight + 2) * 2, recipe.getCoatingType().getColor(), false);
        guiGraphics.drawString(font, coatingDesc, this.background.getWidth() - 128, textYPos - (font.lineHeight + 2) * 1, 0xFFFFFFFF, false);
        guiGraphics.drawString(font, coatingDurability, this.background.getWidth() - 128, textYPos, 0xFFFFFFFF, false);

        // Forge block
        ItemStack forgeBlock = new ItemStack(ModBlocks.MECHANICAL_FORGE_MOULD.get());
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(-2 * centreXPos + 20, -centreYPos, 0);
        pose.scale(2.0f, 2.0f, 2.0f);
        guiGraphics.renderFakeItem(forgeBlock, centreXPos-8, centreYPos - 20);
        pose.popPose();

        // Render casting depot
        ItemStack blockStack = new ItemStack(ModBlocks.CASTING_DEPOT.get());
        ItemStack pickaxeItem = new ItemStack(Items.DIAMOND_PICKAXE);

        pose.pushPose();
        pose.translate(-1*centreXPos - 10, -10, 0);
        pose.scale(2f, 2f, 2f);
        guiGraphics.renderFakeItem(blockStack, (int)(centreXPos/1.5f - 8), (int)(centreYPos/1.5f + 2));
        pose.popPose();

        // Render item on depot
        //guiGraphics.renderFakeItem(pickaxeItem, 0, 0);

        pose.pushPose();
        pose.translate(12, centreYPos-7, 0);
        guiGraphics.renderFakeItem(pickaxeItem, 0, 0);
        pose.popPose();
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, CoatingRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        int xPos = 12;
        int yPos = (this.background.getHeight() / 2) - 7;

        if (mouseX >= xPos && mouseX <= xPos + 16 && mouseY >= yPos && mouseY <= yPos + 16) {
            // Add the block name (the bold/color formatting is handled by the block's getName())
            tooltip.add(Component.literal("Any tool"));
        }
    }
}
