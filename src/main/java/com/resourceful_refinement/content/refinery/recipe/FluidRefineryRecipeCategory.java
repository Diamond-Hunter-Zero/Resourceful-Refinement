package com.resourceful_refinement.content.refinery.recipe;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.forge_mould.recipe.MechanicalForgeMouldRecipe;
import com.resourceful_refinement.content.fracking_pump.*;
import com.resourceful_refinement.content.refinery.rendering.*;
import com.resourceful_refinement.registry.ModBlocks;
import com.simibubi.create.content.processing.recipe.HeatCondition;
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
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.earlydisplay.ElementShader;

public class FluidRefineryRecipeCategory implements IRecipeCategory<FluidRefineryRecipe> {

    public static final RecipeType<FluidRefineryRecipe> TYPE =
            RecipeType.create(ResourcefulRefinementMain.MOD_ID, "fluid_refinery", FluidRefineryRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final ResourceLocation background_texture = ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "textures/gui/jei/jei_refinery_backdrop.png");

    private RefineryBaseModel base;
    private RefineryMiddleModel middle;
    private RefineryTopModel top;
    private RefineryBlenderModel blender;

    private float renderTime = 0;

    public FluidRefineryRecipeCategory(IGuiHelper guiHelper) {
        // Define your background (usually a 176xSomething texture)
        this.background = guiHelper.createDrawable(background_texture, 0, 0, 176, 82);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.REFINERY_ACCESS_PORT.get()));
    }

    @Override
    public RecipeType<FluidRefineryRecipe> getRecipeType() { return TYPE; }

    @Override
    public Component getTitle() { return Component.translatable("Fluid Refinery"); }

    @Override
    public IDrawable getBackground() { return background; }

    @Override
    public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FluidRefineryRecipe recipe, IFocusGroup focuses) {
        // Define where items go. Note: 1.21.1 uses RecipeHolder for recipes.

        int centreXPos = (this.background.getWidth() / 2)-8; // Horizontal position of your choice
        int centreYPos = (this.background.getHeight() / 2); // Vertical centre

        if (!recipe.getIngredients().isEmpty())
        {
            if (recipe.getCombinedIngredients().size() > 0 && !recipe.getCombinedIngredients().get(0).ingredient().isEmpty())
                builder.addSlot(RecipeIngredientRole.INPUT, centreXPos-60, 39).addItemStack(recipe.getCombinedIngredients().get(0).getItems()[0]);
            if (recipe.getCombinedIngredients().size() > 1 && !recipe.getCombinedIngredients().get(1).ingredient().isEmpty())
                builder.addSlot(RecipeIngredientRole.INPUT, centreXPos+60, 39).addItemStack(recipe.getCombinedIngredients().get(1).getItems()[0]);
        }

        if (!recipe.getFluidIngredients().isEmpty())
        {
            if (recipe.getFluidIngredients().size() > 0)
                builder.addSlot(RecipeIngredientRole.INPUT, centreXPos-60, 4).addFluidStack(recipe.getFluidIngredients().get(0).getFluids()[0].getFluid());
            if (recipe.getFluidIngredients().size() > 1)
                builder.addSlot(RecipeIngredientRole.INPUT, centreXPos+60, 4).addFluidStack(recipe.getFluidIngredients().get(1).getFluids()[0].getFluid());
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, centreXPos + 32, this.background.getHeight() - 19).addFluidStack(recipe.getFluidResults().getFirst().getFluid());
    }

    @Override
    public void draw(FluidRefineryRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;

        int centreXPos = (this.background.getWidth() / 2); // Horizontal position of your choice
        int centreYPos = (this.background.getHeight() / 2); // Vertical centre

        // Render Processing Time (e.g., "200 ticks")
        String timeText = recipe.getProcessingDuration() + "t";
        int timeWidth = font.width(timeText);
        guiGraphics.drawString(font, timeText, centreXPos + 52, this.background.getHeight() - 15 -font.lineHeight/2, 0xFFE3E3E3, false);

        // Heating warning
        String heatText = recipe.getRequiredHeat().getSerializedName();
        guiGraphics.drawString(font, heatText, 4, this.background.getHeight() - 5 -font.lineHeight, recipe.getRequiredHeat().getColor(), false);

        // Render Fluid amounts
        if (!recipe.getFluidIngredients().isEmpty())
        {
            if (recipe.getFluidIngredients().size() > 0)
            {
                String fluidText1 = recipe.getFluidIngredients().getFirst().amount() + "mB";
                int fluid1Width = font.width(fluidText1);
                guiGraphics.drawString(font, fluidText1, centreXPos-60 - fluid1Width/2, 8 + 17, 0xFF808080, false);
            }
            if (recipe.getFluidIngredients().size() > 1)
            {
                String fluidText2 = recipe.getFluidIngredients().get(1).amount() + "mB";
                int fluid2Width = font.width(fluidText2);
                guiGraphics.drawString(font, fluidText2, centreXPos+60 - fluid2Width/2, 8 + 17, 0xFF808080, false);
            }
        }

        String fluidOutputText = recipe.getFluidResults().getFirst().getAmount() + "mB";
        guiGraphics.drawString(font, fluidOutputText, centreXPos + 43, this.background.getHeight() - 5 -font.lineHeight/2, 0xFFE3E3E3, false);

        // Render refinery
        prepareModels();

        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(centreXPos, centreYPos-54, 36);
        pose.scale(-15f, 15f, 15f);
        pose.mulPose(Axis.YP.rotationDegrees(200f));
        pose.mulPose(Axis.XP.rotationDegrees(15f));
        pose.mulPose(Axis.ZP.rotationDegrees(5f));

        MultiBufferSource.BufferSource bufferSource = guiGraphics.bufferSource();
        renderRefineryStructure(pose, bufferSource);
        bufferSource.endBatch();

        pose.popPose();
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, FluidRefineryRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        // 1. Coordinates for our vertically centered block (from previous step)
        int xPos = (this.background.getWidth() / 2) - 32;
        int yPos = (this.background.getHeight() / 2) -46;

        // 2. Check if mouse is over the block
        if (mouseX >= xPos && mouseX <= xPos + 64 && mouseY >= yPos && mouseY <= yPos + 64) {
            // Add the block name (the bold/color formatting is handled by the block's getName())
            tooltip.add(Component.literal("Fluid Refinery Assembly"));
        }
    }

    private void prepareModels() {
        if (this.base != null) return;

        // Access the global EntityModelSet
        EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();

        // Bake the layers using LayerDefinitions
        this.base = new RefineryBaseModel(modelSet.bakeLayer(RefineryLayers.BASE));
        this.middle = new RefineryMiddleModel(modelSet.bakeLayer(RefineryLayers.MIDDLE));
        this.top = new RefineryTopModel(modelSet.bakeLayer(RefineryLayers.TOP));
        this.blender = new RefineryBlenderModel(modelSet.bakeLayer(RefineryLayers.BLENDER));
    }

    private void renderRefineryStructure(PoseStack ms, MultiBufferSource buffer)
    {
        var vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(FluidRefineryRenderer.TEXTURE));
        for (int i = 0; i < 3; i++) {
            ms.pushPose();
            ms.translate(0, 3 - i, 0);
            if (i == 0)
            {
                base.render(ms, vertexConsumer, 0xF000F0, OverlayTexture.NO_OVERLAY);
            }
            else if (i == 2)
            {
                top.render(ms, vertexConsumer, 0xF000F0, OverlayTexture.NO_OVERLAY);
            }
            else
            {
                middle.render(ms, vertexConsumer, 0xF000F0, OverlayTexture.NO_OVERLAY);
                ms.mulPose(Axis.YP.rotationDegrees(renderTime));
                renderTime = (renderTime + 1)%360;
                blender.render(ms, vertexConsumer, 0xF000F0, OverlayTexture.NO_OVERLAY);
            }
            ms.popPose();
        }
    }

}
