package com.resourceful_refinement.content.distillery.recipe;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.distillery.DistilleryBlock;
import com.resourceful_refinement.content.distillery.DistilleryModel;
import com.resourceful_refinement.content.radiator.RadiatorModel;
import com.resourceful_refinement.content.sieve.recipe.MechanicalSieveRecipe;
import com.resourceful_refinement.registry.ModBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
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
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class DistilleryRecipeCategory implements IRecipeCategory<DistilleryRecipe> {

    public static final RecipeType<DistilleryRecipe> TYPE =
            RecipeType.create(ResourcefulRefinementMain.MOD_ID, "distillery", DistilleryRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final ResourceLocation background_texture = ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "textures/gui/jei/jei_distillery_backdrop.png");
    private DistilleryModel distilleryModel;

    public DistilleryRecipeCategory(IGuiHelper guiHelper) {
        // Define your background (usually a 176xSomething texture)
        this.background = guiHelper.createDrawable(background_texture, 0, 0, 175, 82);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.DISTILLERY.get()));
    }

    @Override
    public RecipeType<DistilleryRecipe> getRecipeType() { return TYPE; }

    @Override
    public Component getTitle() { return Component.translatable("Distilling"); }

    @Override
    public IDrawable getBackground() { return background; }

    @Override
    public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, DistilleryRecipe recipe, IFocusGroup focuses) {
        // Define where items go. Note: 1.21.1 uses RecipeHolder for recipes.
        // If 'recipe' is a RecipeHolder, use recipe.value() to get the actual recipe class.
        if (!recipe.getCombinedIngredients().isEmpty() && !recipe.getCombinedIngredients().getFirst().ingredient().isEmpty())
            builder.addSlot(RecipeIngredientRole.INPUT, 43, 32).addItemStack(recipe.getCombinedIngredients().getFirst().getItems()[0]);

        builder.addSlot(RecipeIngredientRole.INPUT, 17, 32).addFluidStack(recipe.getFluidIngredients().getFirst().getFluids()[0].getFluid());
        if (!recipe.getFluidResults().isEmpty())
            builder.addSlot(RecipeIngredientRole.OUTPUT, this.background.getWidth() - 31, 32).addFluidStack(recipe.getFluidResults().getFirst().getFluid());
    }

    @Override
    public void draw(DistilleryRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;

        int centreXPos = (this.background.getWidth() / 2); // Horizontal position of your choice
        int centreYPos = (this.background.getHeight() / 2); // Subtract 8 (half of 16px item) to center

        // 1. Render Processing Time (e.g., "200 ticks")
        String timeText = String.format("%.0f", recipe.getProcessingDuration()/20f) + "s";
        int timeWidth = font.width(timeText);
        guiGraphics.drawString(font, timeText, 18, 10, 0xFFF5F5F5, false);

        // Render fluid stack amounts
        int inputAmount = recipe.getFluidIngredients().getFirst().amount();
        String inputAmountText = inputAmount + "mB";
        int inputAmountWidth = font.width(inputAmountText);
        guiGraphics.drawString(font, inputAmountText, 24 - inputAmountWidth/2f, centreYPos + 10, 0xFF808080, false);

        if (!recipe.getFluidResults().isEmpty())
        {
            int outputAmount = recipe.getFluidResults().getFirst().getAmount();
            String outputAmountText = outputAmount + "mB";
            int outputAmountWidth = font.width(outputAmountText);
            guiGraphics.drawString(font, outputAmountText, this.background.getWidth() - 23 - outputAmountWidth/2f, centreYPos + 10, 0xFF808080, false);
        }

        // Heating warning
        String heatText = recipe.getRequiredHeatCondition().getSerializedName();
        guiGraphics.drawString(font, heatText, this.background.getWidth() - 54, this.background.getHeight() - 7 -font.lineHeight, recipe.getRequiredHeatCondition().getColor(), false);


        // Height warning
        int maxHeight = recipe.getRequiredHeight();
        String heightText = maxHeight + " Blocks";
        guiGraphics.drawString(font, heightText, this.background.getWidth() - 47, 9, 0xFFF5F5F5, false);


        // Render block model
        float distilleryScale = (44f + 6f*(maxHeight-2f))/maxHeight;
        guiGraphics.pose().pushPose();

        // Render block model
        prepareModels();
        VertexConsumer vertexConsumer;
        int modelType = 1;
        guiGraphics.pose().translate(centreXPos-4, centreYPos+(9 + 3*(maxHeight-2)), 20);
        guiGraphics.pose().scale(-distilleryScale, distilleryScale, distilleryScale);
        guiGraphics.pose().mulPose(Axis.XP.rotationDegrees(-20));
        guiGraphics.pose().mulPose(Axis.YP.rotationDegrees(225));
        guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(0));

        for (int i = 1; i <= maxHeight; i++)
        {
            if (i == 1)
                modelType = 1;
            else if (i == maxHeight)
                modelType = 3;
            else
                modelType = 2;

            vertexConsumer = guiGraphics.bufferSource().getBuffer(RenderType.entityTranslucent(DistilleryModel.GetTextureForModelType(modelType)));
            guiGraphics.pose().translate(0, -1, 0);
            distilleryModel.render(guiGraphics.pose(),
                    vertexConsumer,
                    0xFFFFFF,
                    OverlayTexture.NO_OVERLAY);
        }
        guiGraphics.pose().popPose();

    }

    private void prepareModels() {
        if (this.distilleryModel != null) return;

        // Access the global EntityModelSet
        EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();

        // Bake the layers using LayerDefinitions
        this.distilleryModel = new DistilleryModel(modelSet.bakeLayer(DistilleryModel.DISTILLERY_MODEL_LAYER));
    }
}
