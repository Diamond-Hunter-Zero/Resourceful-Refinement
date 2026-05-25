package com.resourceful_refinement.content.fracking_pump.recipe;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.fracking_pump.*;
import com.resourceful_refinement.content.sieve.recipe.MechanicalSieveRecipe;
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
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class FrackingPumpRecipeCategory implements IRecipeCategory<FrackingPumpRecipe> {

    public static final RecipeType<FrackingPumpRecipe> TYPE =
            RecipeType.create(ResourcefulRefinementMain.MOD_ID, "fracking_pump", FrackingPumpRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final ResourceLocation background_texture = ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "textures/gui/jei/jei_fracking_backdrop.png");

    private FrackingPumpBaseModel baseModel;
    private FrackingPumpShaftModel shaftModel;
    private FrackingPumpTopModel topModel;
    private FrackingPumpCounterweightModel counterweightModel;


    public FrackingPumpRecipeCategory(IGuiHelper guiHelper) {
        // Define your background (usually a 176xSomething texture)
        this.background = guiHelper.createDrawable(background_texture, 0, 0, 175, 82);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.FRACKING_PUMP_OUTLET.get()));
    }

    @Override
    public RecipeType<FrackingPumpRecipe> getRecipeType() { return TYPE; }

    @Override
    public Component getTitle() { return Component.translatable("Fracking"); }

    @Override
    public IDrawable getBackground() { return background; }

    @Override
    public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FrackingPumpRecipe recipe, IFocusGroup focuses) {
        // Define where items go
        builder.addSlot(RecipeIngredientRole.INPUT, 27, 32).addFluidStack(recipe.getFluidIngredients().getFirst().getFluids()[0].getFluid());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 133, 32).addFluidStack(recipe.getFluidResults().getFirst().getFluid());

        if (recipe.requiresGeyserFluid())
        {
            builder.addSlot(RecipeIngredientRole.INPUT, this.background.getWidth() / 2 - 20, this.background.getHeight() - 19).addItemStack(recipe.getSourceBlock().asItem().getDefaultInstance());
            builder.addSlot(RecipeIngredientRole.INPUT, this.background.getWidth() / 2 + 4, this.background.getHeight() - 19).addFluidStack(recipe.getSourceFluid());
        }
        else
            builder.addSlot(RecipeIngredientRole.INPUT, this.background.getWidth() / 2 - 8, this.background.getHeight() - 19).addItemStack(recipe.getSourceBlock().asItem().getDefaultInstance());
    }

    @Override
    public void draw(FrackingPumpRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;

        int centreXPos = (this.background.getWidth() / 2); // Horizontal position of your choice
        int centreYPos = (this.background.getHeight() / 2); // Subtract 8 (half of 16px item) to center

        // 1. Render Processing Time (e.g., "200 ticks")
        String timeText = recipe.getProcessingDuration() + "t";
        int timeWidth = font.width(timeText);
        // Draw text at x=100, y=40 (adjust based on your background)
        guiGraphics.drawString(font, timeText, 5, this.background.getHeight() - 10, 0xFF808080, false);

        // Render fluid stack amounts
        int inputAmount = recipe.getFluidIngredients().getFirst().amount();
        String inputAmountText = inputAmount + "mB";
        int inputAmountWidth = font.width(inputAmountText);
        guiGraphics.drawString(font, inputAmountText, 27 + 8 - inputAmountWidth/2f, centreYPos + 12, 0xFF808080, false);

        int outputAmount = recipe.getFluidResults().getFirst().getAmount();
        String outputAmountText = outputAmount + "mB";
        int outputAmountWidth = font.width(outputAmountText);
        guiGraphics.drawString(font, outputAmountText, 133 + 8 - outputAmountWidth/2f, centreYPos + 12, 0xFF808080, false);

        // Render pump structure model
        prepareModels();

        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(centreXPos, centreYPos-2, 6);
        pose.scale(-8f, 8f, 8f);
        pose.mulPose(Axis.YP.rotationDegrees(225f));
        pose.mulPose(Axis.XP.rotationDegrees(15f));
        pose.mulPose(Axis.ZP.rotationDegrees(15f));

        MultiBufferSource.BufferSource bufferSource = guiGraphics.bufferSource();
        renderPumpStructure(pose, bufferSource);
        bufferSource.endBatch();
        pose.popPose();
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, FrackingPumpRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        // 1. Coordinates for our vertically centered block (from previous step)
        int xPos = (this.background.getWidth() / 2 - 16);
        int yPos = (this.background.getHeight() / 2 - 38);

        // 2. Check if mouse is over the block
        if (mouseX >= xPos && mouseX <= xPos + 32 && mouseY >= yPos && mouseY <= yPos + 52) {
            tooltip.add(Component.literal("Fracking Pylon"));
        }
    }

    private void prepareModels() {
        if (this.baseModel != null) return;

        // Access the global EntityModelSet
        EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();

        // Bake the layers using your LayerDefinitions
        this.baseModel = new FrackingPumpBaseModel(modelSet.bakeLayer(FrackingPumpLayers.BASE));
        this.shaftModel = new FrackingPumpShaftModel(modelSet.bakeLayer(FrackingPumpLayers.SHAFT));
        this.topModel = new FrackingPumpTopModel(modelSet.bakeLayer(FrackingPumpLayers.TOP));
        this.counterweightModel = new FrackingPumpCounterweightModel(modelSet.bakeLayer(FrackingPumpLayers.COUNTERWEIGHT));
    }

    private void renderPumpStructure(PoseStack ms, MultiBufferSource buffer)
    {
        var vertexConsumer = buffer.getBuffer(baseModel.renderType(FrackingPumpRenderer.TEXTURE));
        // Base
        baseModel.renderToBuffer(ms, vertexConsumer, 0xF000F0, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        // Shafts & top
        for (int i = 0; i < 3; i++) {
            ms.pushPose();
            ms.translate(0, -3 - i, 0);
            if (i == 2)
            {
                topModel.render(ms, vertexConsumer, 0xF000F0, OverlayTexture.NO_OVERLAY);
                ms.popPose();
                break;
            }
            else if (i == 1)
            {
                counterweightModel.render(ms, vertexConsumer, 0xF000F0, OverlayTexture.NO_OVERLAY);
            }
            shaftModel.render(ms, vertexConsumer, 0xF000F0, OverlayTexture.NO_OVERLAY);
            ms.popPose();
        }
    }
}
