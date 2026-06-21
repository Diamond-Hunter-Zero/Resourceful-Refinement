package com.resourceful_refinement.content.radiator;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.config.ServerConfig;
import com.resourceful_refinement.content.forge_mould.recipe.MechanicalForgeMouldRecipe;
import com.resourceful_refinement.content.refinery.rendering.*;
import com.resourceful_refinement.registry.ModBlocks;
import com.resourceful_refinement.utilities.heating.ExtendedHeatCondition;
import com.resourceful_refinement.utilities.heating.HeatUtilities;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class RadiatorVirtualHeatingCategory implements IRecipeCategory<RadiatorVirtualHeatingRecord> {

    public static final RecipeType<RadiatorVirtualHeatingRecord> TYPE =
            RecipeType.create(ResourcefulRefinementMain.MOD_ID, "radiator_heat_conversion", RadiatorVirtualHeatingRecord.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final ResourceLocation background_texture = ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "textures/gui/jei/jei_radiator_backdrop.png");
    private RadiatorModel radiatorModel;

    public RadiatorVirtualHeatingCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(background_texture, 0, 0, 175, 82);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.RADIATOR_PIPE.get()));
    }

    @Override
    public RecipeType<RadiatorVirtualHeatingRecord> getRecipeType() { return TYPE; }

    @Override
    public Component getTitle() { return Component.translatable("Radiator Heating"); }

    @Override
    public IDrawable getBackground() { return background; }

    @Override
    public IDrawable getIcon() { return icon; }



    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RadiatorVirtualHeatingRecord recipe, IFocusGroup focuses) {
        // 1. Fetch all fluids associated with the tag
        List<FluidStack> matchingFluids = getFluidsFromTag(recipe.fluidTag());

        // 2. Build the Input Slot (The Fluid)
        // By passing a List of FluidStacks, JEI automatically handles the visual cycling!
        int centreYPos = (this.background.getHeight() / 2);
        builder.addSlot(RecipeIngredientRole.INPUT, 11, centreYPos-13)
                .addIngredients(NeoForgeTypes.FLUID_STACK, matchingFluids)
                .setFluidRenderer(1000, false, 24, 24);
    }

    @Override
    public void draw(RadiatorVirtualHeatingRecord recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;

        int centreXPos = (this.background.getWidth() / 2); // Horizontal position of your choice
        int centreYPos = (this.background.getHeight() / 2); // Subtract 8 (half of 16px item) to center

        // Render Consumption Rate
        String timeText = String.format("%.0f", recipe.consumptionRate()*20f) + " mb/s";

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 0);
        guiGraphics.pose().scale(0.75f, 0.75f, 1.0f);
        guiGraphics.drawString(font, "Consumption:", centreXPos+12, centreYPos - 29, 0xcfc7c7, false);
        guiGraphics.pose().popPose();

        guiGraphics.drawString(font, timeText, this.background.getWidth() - font.width(timeText)-12, centreYPos - 33, 0xFFF5F5F5, false);

        // Heating warning
        String heatText = recipe.resultingHeat().getSerializedName();
        guiGraphics.drawString(font, heatText, this.background.getWidth() - 71, this.background.getHeight() - 5 -font.lineHeight, recipe.resultingHeat().getColor(), false);

        // Render radiator block model
        prepareModels();
        int radiatorScale = 30;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(centreXPos+16, centreYPos-7, 100);
        guiGraphics.pose().mulPose(Axis.XP.rotationDegrees(-30));
        guiGraphics.pose().mulPose(Axis.YP.rotationDegrees(30));
        guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(90));
        guiGraphics.pose().scale(radiatorScale, radiatorScale, radiatorScale);

        var vertexConsumer = guiGraphics.bufferSource().getBuffer(RenderType.entityTranslucent(RadiatorModel.GetTextureForHeatEnergy(recipe.resultingHeat().getBlazeHeatEnergy())));
        radiatorModel.render(guiGraphics.pose(),
                vertexConsumer,
                0xF000F0,
                OverlayTexture.NO_OVERLAY);

        guiGraphics.pose().popPose();

    }


    // -------------------------------------------------------------------------
    // Recipe Utilities
    // -------------------------------------------------------------------------
    private List<FluidStack> getFluidsFromTag(TagKey<Fluid> tag) {
        List<FluidStack> fluids = new ArrayList<>();

        // Fetch from the built-in registry
        Iterable<Holder<Fluid>> holders = BuiltInRegistries.FLUID.getTagOrEmpty(tag);
        for (Holder<Fluid> holder : holders) {
            // Create a stack of 1000mB for each matching fluid
            fluids.add(new FluidStack(holder.value(), 1000));
        }

        return fluids;
    }

    public static void RegisterConversionRecipes(IRecipeRegistration registration)
    {
        List<RadiatorVirtualHeatingRecord> recipes = List.of(
                new RadiatorVirtualHeatingRecord(HeatUtilities.CHILLED_FLUID_TAG, ExtendedHeatCondition.CHILLED, ServerConfig.CHILLED_COOLANT_CONSUMPTION.get()),
                new RadiatorVirtualHeatingRecord(HeatUtilities.COOLED_FLUID_TAG, ExtendedHeatCondition.COOLED, ServerConfig.COOLED_COOLANT_CONSUMPTION.get()),
                new RadiatorVirtualHeatingRecord(HeatUtilities.PASSIVE_FLUID_TAG, ExtendedHeatCondition.PASSIVE, ServerConfig.PASSIVE_COOLANT_CONSUMPTION.get()),
                new RadiatorVirtualHeatingRecord(HeatUtilities.HEATED_FLUID_TAG, ExtendedHeatCondition.HEATED, ServerConfig.HEATED_COOLANT_CONSUMPTION.get()),
                new RadiatorVirtualHeatingRecord(HeatUtilities.SUPERHEATED_FLUID_TAG, ExtendedHeatCondition.SUPERHEATED, ServerConfig.SUPERHEATED_COOLANT_CONSUMPTION.get())
                // Add other custom tags here
        );

        registration.addRecipes(RadiatorVirtualHeatingCategory.TYPE, recipes);
    }

    private void prepareModels() {
        if (this.radiatorModel != null) return;

        // Access the global EntityModelSet
        EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();

        // Bake the layers using LayerDefinitions
        this.radiatorModel = new RadiatorModel(modelSet.bakeLayer(RadiatorModel.RADIATOR_MODEL_LAYER));
    }
}
