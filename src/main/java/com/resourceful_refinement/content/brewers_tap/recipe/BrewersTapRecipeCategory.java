package com.resourceful_refinement.content.brewers_tap.recipe;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.brewers_tap.FlavourType;
import com.resourceful_refinement.content.distillery.DistilleryModel;
import com.resourceful_refinement.content.distillery.recipe.DistilleryRecipe;
import com.resourceful_refinement.registry.ModBlocks;
import com.simibubi.create.AllBlocks;
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
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.*;

public class BrewersTapRecipeCategory implements IRecipeCategory<BrewersTapRecipe> {

    public static final RecipeType<BrewersTapRecipe> TYPE =
            RecipeType.create(ResourcefulRefinementMain.MOD_ID, "brewers_tap", BrewersTapRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final ResourceLocation background_texture = ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "textures/gui/jei/jei_brewers_tap_backdrop.png");
    private final ResourceLocation flavour_tab_texture = ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "textures/gui/flavour_tab.png");


    public BrewersTapRecipeCategory(IGuiHelper guiHelper) {
        // Define your background (usually a 176xSomething texture)
        this.background = guiHelper.createDrawable(background_texture, 0, 0, 175, 82);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.BREWERS_TAP.get()));
    }

    @Override
    public RecipeType<BrewersTapRecipe> getRecipeType() { return TYPE; }

    @Override
    public Component getTitle() { return Component.translatable("Tapped Drinks"); }

    @Override
    public IDrawable getBackground() { return background; }

    @Override
    public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BrewersTapRecipe recipe, IFocusGroup focuses) {

        int centreXPos = (this.background.getWidth() / 2);

        // Input item
        if (!recipe.getIngredients().isEmpty() && !recipe.getIngredients().getFirst().isEmpty())
            builder.addSlot(RecipeIngredientRole.INPUT, 22, 29).addItemStacks(Arrays.asList(recipe.getIngredients().getFirst().getItems()));

        // Input fluid
        if (!recipe.getFluidIngredients().isEmpty() && recipe.getFluidIngredients().getFirst().amount() > 0)
            builder.addSlot(RecipeIngredientRole.INPUT, 22, 51).addFluidStack(recipe.getFluidIngredients().getFirst().getFluids()[0].getFluid());

        // Output item
        if (!recipe.getRollableResults().isEmpty() && !recipe.getRollableResults().getFirst().getStack().isEmpty())
            builder.addSlot(RecipeIngredientRole.OUTPUT, centreXPos-7, 54).addItemStack(recipe.getRollableResults().getFirst().getStack());

        // Flavour tags
        int tagCount = recipe.getFlavourTags().size();
        for (int i = 0; i  < tagCount; i++)
        {
            FlavourType flavour = FlavourType.unsafeFromTagName(recipe.getFlavourTags().get(i));
            if (flavour == FlavourType.UNKNOWN)
                continue;

            Ingredient flavourIngredient = Ingredient.of(flavour.getItemTag());
            builder.addSlot(RecipeIngredientRole.CATALYST, this.background.getWidth() - 64, 6 + 19*i)
                    .addIngredients(flavourIngredient);
        }
    }

    @Override
    public void draw(BrewersTapRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;

        int centreXPos = (this.background.getWidth() / 2); // Horizontal position of your choice
        int centreYPos = (this.background.getHeight() / 2); // Subtract 8 (half of 16px item) to center

        // Render Processing Time (e.g., "200 ticks")
        String timeText = String.format("%.0f", recipe.getProcessingDuration()/20f) + "s";
        int timeWidth = font.width(timeText);
        guiGraphics.drawString(font, timeText, 17, 8, 0xFFF5F5F5, false);

        // Render fluid stack amounts
        int inputAmount = recipe.getFluidIngredients().getFirst().amount();
        String inputAmountText = inputAmount + "mB";
        int inputAmountWidth = font.width(inputAmountText);
        guiGraphics.drawString(font, inputAmountText, 30 - inputAmountWidth/2f, this.background.getHeight()-12, 0xFF808080, false);

        // Render tap block
        ItemStack tankBlock = new ItemStack(AllBlocks.FLUID_TANK.get());
        ItemStack tapBlock = new ItemStack(ModBlocks.BREWERS_TAP.get());

        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(-centreXPos+16, -centreYPos+6, 0);
        pose.scale(1.85f, 1.85f, 1.85f);
        guiGraphics.renderFakeItem(tankBlock, centreXPos-11, centreYPos - 20);
        pose.translate(0, 0, 60);
        pose.scale(0.75f, 0.75f, 0.75f);
        guiGraphics.renderFakeItem(tapBlock, centreXPos+27, centreYPos - 5);
        pose.popPose();

        // Render tag details
        int tagCount = recipe.getFlavourTags().size();
        for (int i = 0; i  < tagCount; i++)
        {
            guiGraphics.blit(flavour_tab_texture, this.background.getWidth() - 65, 5 + 19*i, 0,0, 72, 18, 72, 18);
            FlavourType flavour = FlavourType.unsafeFromTagName(recipe.getFlavourTags().get(i));
            guiGraphics.drawString(font, flavour.getDisplayName(), this.background.getWidth() - 44, 10 + 19*i, flavour.getColor(), false);
        }
    }

}
