package com.resourceful_refinement.registry;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.brewers_tap.recipe.BrewersTapRecipe;
import com.resourceful_refinement.content.brewers_tap.recipe.BrewersTapRecipeCategory;
import com.resourceful_refinement.content.coating.CoatingRecipeCategory;
import com.resourceful_refinement.content.cyclotron_forge.recipe.CyclotronForgeRecipe;
import com.resourceful_refinement.content.cyclotron_forge.recipe.CyclotronForgeRecipeCategory;
import com.resourceful_refinement.content.distillery.recipe.DistilleryRecipe;
import com.resourceful_refinement.content.distillery.recipe.DistilleryRecipeCategory;
import com.resourceful_refinement.content.drill_pylon.recipe.DrillPylonRecipe;
import com.resourceful_refinement.content.drill_pylon.recipe.DrillPylonRecipeCategory;
import com.resourceful_refinement.content.forge_mould.recipe.CoatingRecipe;
import com.resourceful_refinement.content.forge_mould.recipe.MechanicalForgeMouldRecipe;
import com.resourceful_refinement.content.forge_mould.recipe.MechanicalForgeMouldRecipeCategory;
import com.resourceful_refinement.content.fracking_pump.recipe.FrackingPumpRecipe;
import com.resourceful_refinement.content.fracking_pump.recipe.FrackingPumpRecipeCategory;
import com.resourceful_refinement.content.milking_station.recipe.MilkingStationRecipe;
import com.resourceful_refinement.content.milking_station.recipe.MilkingStationRecipeCategory;
import com.resourceful_refinement.content.radiator.RadiatorVirtualHeatingCategory;
import com.resourceful_refinement.content.refinery.recipe.FluidRefineryRecipe;
import com.resourceful_refinement.content.refinery.recipe.FluidRefineryRecipeCategory;
import com.resourceful_refinement.content.refinery.rendering.RefineryBaseModel;
import com.resourceful_refinement.content.sieve.recipe.MechanicalSieveRecipe;
import com.resourceful_refinement.content.sieve.recipe.MechanicalSieveRecipeCategory;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.List;

@JeiPlugin
public class ModJeiPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new MechanicalForgeMouldRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new MechanicalSieveRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new FrackingPumpRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new CoatingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new FluidRefineryRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new RadiatorVirtualHeatingCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new DistilleryRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new MilkingStationRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new BrewersTapRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new DrillPylonRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new CyclotronForgeRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager rm = Minecraft.getInstance().level.getRecipeManager();

        // Fetch all recipes of your custom type
        // Forging
        List<MechanicalForgeMouldRecipe> forgeRecipes = rm.getAllRecipesFor(ModRecipeTypes.MECHANICAL_FORGE_MOULD_TYPE.get())
                .stream()
                .map(RecipeHolder::value)
                .toList();

        registration.addRecipes(MechanicalForgeMouldRecipeCategory.TYPE, forgeRecipes);

        // Sieving
        List<MechanicalSieveRecipe> sieveRecipes = rm.getAllRecipesFor(ModRecipeTypes.MECHANICAL_SIEVE_TYPE.get())
                .stream()
                .map(RecipeHolder::value)
                .toList();

        registration.addRecipes(MechanicalSieveRecipeCategory.TYPE, sieveRecipes);

        // Fracking
        List<FrackingPumpRecipe> frackingRecipes = rm.getAllRecipesFor(ModRecipeTypes.FRACKING_PUMP_TYPE.get())
                .stream()
                .map(RecipeHolder::value)
                .toList();

        registration.addRecipes(FrackingPumpRecipeCategory.TYPE, frackingRecipes);

        // Coating
        List<CoatingRecipe> coatingRecipes = rm.getAllRecipesFor(ModRecipeTypes.COATING_TYPE.get())
                .stream()
                .map(RecipeHolder::value)
                .toList();

        registration.addRecipes(CoatingRecipeCategory.TYPE, coatingRecipes);

        // Fluid Refinery
        List<FluidRefineryRecipe> refineryRecipes = rm.getAllRecipesFor(ModRecipeTypes.FLUID_REFINERY_TYPE.get())
                .stream()
                .map(RecipeHolder::value)
                .toList();

        registration.addRecipes(FluidRefineryRecipeCategory.TYPE, refineryRecipes);

        // Radiator Heat Conversion
        RadiatorVirtualHeatingCategory.RegisterConversionRecipes(registration);

        // Distillery
        List<DistilleryRecipe> distilleryRecipes = rm.getAllRecipesFor(ModRecipeTypes.DISTILLERY_TYPE.get())
                .stream()
                .map(RecipeHolder::value)
                .toList();

        registration.addRecipes(DistilleryRecipeCategory.TYPE, distilleryRecipes);

        // Milking Station
        List<MilkingStationRecipe> milkingRecipes = rm.getAllRecipesFor(ModRecipeTypes.MILKING_STATION_TYPE.get())
                .stream()
                .map(RecipeHolder::value)
                .toList();

        registration.addRecipes(MilkingStationRecipeCategory.TYPE, milkingRecipes);

        // Brewer's Tap
        List<BrewersTapRecipe> brewersTapRecipes = rm.getAllRecipesFor(ModRecipeTypes.BREWERS_TAP_TYPE.get())
                .stream()
                .map(RecipeHolder::value)
                .toList();

        registration.addRecipes(BrewersTapRecipeCategory.TYPE, brewersTapRecipes);

        // Drill Pylon
        List<DrillPylonRecipe> drillPylonRecipes = rm.getAllRecipesFor(ModRecipeTypes.DRILL_PYLON_TYPE.get())
                .stream()
                .map(RecipeHolder::value)
                .toList();

        registration.addRecipes(DrillPylonRecipeCategory.TYPE, drillPylonRecipes);

        // Cyclotron Forge
        List<CyclotronForgeRecipe> cyclotronRecipes = rm.getAllRecipesFor(ModRecipeTypes.CYCLOTRON_FORGE_TYPE.get())
                .stream()
                .map(RecipeHolder::value)
                .toList();

        registration.addRecipes(CyclotronForgeRecipeCategory.TYPE, cyclotronRecipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        // This adds your machine block icon next to the recipe category name
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MECHANICAL_FORGE_MOULD.get()), MechanicalForgeMouldRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CASTING_DEPOT.get()), MechanicalForgeMouldRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MECHANICAL_SIEVE.get()), MechanicalSieveRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.FRACKING_PUMP_OUTLET.get()), FrackingPumpRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.GEYSER.get()), FrackingPumpRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MECHANICAL_FORGE_MOULD.get()), CoatingRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CASTING_DEPOT.get()), CoatingRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.REFINERY_ACCESS_PORT.get()), FluidRefineryRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.RADIATOR_PIPE.get()), RadiatorVirtualHeatingCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.DISTILLERY.get()), DistilleryRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MILKING_STATION.get()), MilkingStationRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.BREWERS_TAP.get()), BrewersTapRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.DRILL_PYLON_HEAD.get()), DrillPylonRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CRYSTAL_FISSURE_BUD.get()), DrillPylonRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CYCLOTRON_CONTROLLER.get()), CyclotronForgeRecipeCategory.TYPE);
    }
}
