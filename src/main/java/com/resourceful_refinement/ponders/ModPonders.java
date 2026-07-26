package com.resourceful_refinement.ponders;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.registry.ModBlocks;
import com.resourceful_refinement.registry.ModFluids;
import com.resourceful_refinement.registry.ModItems;
import com.simibubi.create.AllBlocks;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class ModPonders implements PonderPlugin {

    public static final ResourceLocation RESOURCEFUL_REFINEMENT_CHAPTER = ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "resourceful_refinement");

    @Override
    public String getModId() {
        return ResourcefulRefinementMain.MOD_ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        registerSceneHelper(helper);
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        registerTagsHelper(helper);
    }



    public static void registerSceneHelper(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        System.out.println("[PA Debug] Registering ponders...");

        helper.forComponents(ModBlocks.MECHANICAL_FORGE_MOULD.getId(), ModBlocks.CASTING_DEPOT.getId())
                .addStoryBoard("mechanical_forge_ponder", ForgeAndCastingPonders::mechanicalForgeScene, RESOURCEFUL_REFINEMENT_CHAPTER)
                .addStoryBoard("coating_ponder", ForgeAndCastingPonders::coatingScene, RESOURCEFUL_REFINEMENT_CHAPTER);

        helper.forComponents(ModBlocks.MECHANICAL_SIEVE.getId())
                .addStoryBoard("single_sieve_ponder", SievePonders::mechanicalSieveScene, RESOURCEFUL_REFINEMENT_CHAPTER)
                .addStoryBoard("sieve_stack_ponder", SievePonders::mechanicalSieveStackScene, RESOURCEFUL_REFINEMENT_CHAPTER);

        helper.forComponents(ModBlocks.BLENDER_BLADE.getId())
                .addStoryBoard("blender_blade_ponder", RefineryPonders::blenderBladesScene, RESOURCEFUL_REFINEMENT_CHAPTER);

        helper.forComponents(ModBlocks.REFINERY_ACCESS_PORT.getId(), ModBlocks.BLENDER_BLADE.getId())
                .addStoryBoard("basic_refinery_ponder", RefineryPonders::refineryStructureScene, RESOURCEFUL_REFINEMENT_CHAPTER)
                .addStoryBoard("basic_refinery_ponder", RefineryPonders::refineryCraftingScene, RESOURCEFUL_REFINEMENT_CHAPTER)
                .addStoryBoard("stacked_refinery_ponder", RefineryPonders::refineryStackingScene, RESOURCEFUL_REFINEMENT_CHAPTER);

        helper.forComponents(ModBlocks.FRACKING_PUMP_OUTLET.getId())
                .addStoryBoard("fracking_ponder", FrackingPonders::frackingGeyserScene, RESOURCEFUL_REFINEMENT_CHAPTER)
                .addStoryBoard("large_fracking_ponder", FrackingPonders::frackingBuildScene, RESOURCEFUL_REFINEMENT_CHAPTER)
                .addStoryBoard("large_fracking_ponder", FrackingPonders::frackingCraftingScene, RESOURCEFUL_REFINEMENT_CHAPTER);

        helper.forComponents(ModBlocks.PAINT_NOZZLE.getId())
                .addStoryBoard("paint_nozzle_ponder", GelPonders::paintNozzleScene, RESOURCEFUL_REFINEMENT_CHAPTER)
                .addStoryBoard("gel_splatter_ponder", GelPonders::gelPropertiesScene, RESOURCEFUL_REFINEMENT_CHAPTER);

        helper.forComponents(ModItems.HOSEGUN.getId())
                .addStoryBoard("hosegun_ponder", GelPonders::hosegunBasicsScene, RESOURCEFUL_REFINEMENT_CHAPTER)
                .addStoryBoard("gel_splatter_ponder", GelPonders::gelPropertiesScene, RESOURCEFUL_REFINEMENT_CHAPTER)
                .addStoryBoard("glue_pot_ponder", GelPonders::gloopyHosegunScene, RESOURCEFUL_REFINEMENT_CHAPTER)
                .addStoryBoard("refill_station_tracking_ponder", GelPonders::refillStationTrackingScene, RESOURCEFUL_REFINEMENT_CHAPTER);

        helper.forComponents(ModBlocks.GEL_SPLATTER.getId(), ModBlocks.GEL_SPLATTER_STICKY.getId(),ModBlocks.GEL_SPLATTER_SLIPPERY.getId(),
                        ModBlocks.GEL_SPLATTER_MOLTEN.getId(), ModBlocks.GEL_SPLATTER_BOUNCY.getId())
                .addStoryBoard("gel_splatter_ponder", GelPonders::gelPropertiesScene, RESOURCEFUL_REFINEMENT_CHAPTER)
                .addStoryBoard("glue_pot_ponder", GelPonders::gloopyHosegunScene, RESOURCEFUL_REFINEMENT_CHAPTER)
                .addStoryBoard("refill_station_tracking_ponder", GelPonders::refillStationTrackingScene, RESOURCEFUL_REFINEMENT_CHAPTER);

        helper.forComponents(ModBlocks.FLUID_REFILL_STATION.getId())
                .addStoryBoard("refill_station_basic_ponder", GelPonders::refillStationBasicsScene, RESOURCEFUL_REFINEMENT_CHAPTER)
                .addStoryBoard("refill_station_tracking_ponder", GelPonders::refillStationTrackingScene, RESOURCEFUL_REFINEMENT_CHAPTER);

        helper.forComponents(ModItems.GLUE_POT.getId())
                .addStoryBoard("glue_pot_ponder", GelPonders::gloopyHosegunScene, RESOURCEFUL_REFINEMENT_CHAPTER);

        helper.forComponents(ModItems.RADIATOR_PIPE_ITEM.getId(), ModFluids.COOLANT.bucket.getId())
                .addStoryBoard("radiator_ponder", RadiatorPonders::radiatorBasicsScene, RESOURCEFUL_REFINEMENT_CHAPTER)
                .addStoryBoard("radiator_crafting_ponder", RadiatorPonders::radiatorCraftingScene, RESOURCEFUL_REFINEMENT_CHAPTER);

        helper.forComponents(ModFluids.COOLANT.bucket.getId())
                .addStoryBoard("radiator_ponder", RadiatorPonders::radiatorBasicsScene, RESOURCEFUL_REFINEMENT_CHAPTER);

        helper.forComponents(AllBlocks.BASIN.getId())
                .addStoryBoard("radiator_crafting_ponder", RadiatorPonders::radiatorCraftingScene, RESOURCEFUL_REFINEMENT_CHAPTER);

        helper.forComponents(ModBlocks.DISTILLERY.getId())
                .addStoryBoard("distillery_ponder", DistilleryPonders::distilleryScene, RESOURCEFUL_REFINEMENT_CHAPTER)
                .addStoryBoard("radiator_crafting_ponder", RadiatorPonders::radiatorCraftingScene, RESOURCEFUL_REFINEMENT_CHAPTER);

        helper.forComponents(ModBlocks.COMBUSTION_CHAMBER.getId())
                .addStoryBoard("combustion_chamber_ponder", CombustionChamberPonders::chamberBasicsScene, RESOURCEFUL_REFINEMENT_CHAPTER);

        helper.forComponents(ModBlocks.FUEL_TANK.getId())
                .addStoryBoard("fuel_tank_ponder", CombustionChamberPonders::fuelTankScene, RESOURCEFUL_REFINEMENT_CHAPTER);

        helper.forComponents(ModBlocks.MILKING_STATION.getId())
                .addStoryBoard("milking_station_ponder", MilkingStationPonders::milkingStationScene, RESOURCEFUL_REFINEMENT_CHAPTER);

        helper.forComponents(ModBlocks.ADVANCED_PUMP.getId())
                .addStoryBoard("advanced_pump_ponder", PumpPonders::advancedPumpBasicsScene, RESOURCEFUL_REFINEMENT_CHAPTER);

        helper.forComponents(ModBlocks.BREWERS_TAP.getId(), ModItems.DRINKS_GLASS.getId())
                .addStoryBoard("brewers_tap_ponder", BrewersTapPonders::brewersTapScene, RESOURCEFUL_REFINEMENT_CHAPTER);

        helper.forComponents(ModFluids.LIQUID_CONCRETE.bucket.getId(), ModFluids.POURED_CEMENT.bucket.getId(), AllBlocks.HOSE_PULLEY.getId())
                .addStoryBoard("cement_ponder", FluidPonders::liquidConcreteScene, RESOURCEFUL_REFINEMENT_CHAPTER);

    }

    public static void registerTagsHelper(PonderTagRegistrationHelper<ResourceLocation> helper) {

        helper.registerTag(RESOURCEFUL_REFINEMENT_CHAPTER)
                .addToIndex()
                .title("Resourceful Refinement")
                .description("Advanced processing and refinement for fluids and ores")
                .item(ModBlocks.MECHANICAL_SIEVE.asItem(), true, true)
                .register();

        helper.addToTag(RESOURCEFUL_REFINEMENT_CHAPTER)
                .add(ModBlocks.MECHANICAL_FORGE_MOULD.getId())
                .add(ModBlocks.MECHANICAL_SIEVE.getId())
                .add(ModBlocks.CASTING_DEPOT.getId())
                .add(ModBlocks.REFINERY_ACCESS_PORT.getId())
                .add(ModBlocks.BLENDER_BLADE.getId())
                .add(ModBlocks.FRACKING_PUMP_OUTLET.getId())
                .add(ModBlocks.PAINT_NOZZLE.getId())
                .add(ModBlocks.GEL_SPLATTER_SLIPPERY.getId())
                .add(ModBlocks.GEL_SPLATTER_STICKY.getId())
                .add(ModBlocks.GEL_SPLATTER_MOLTEN.getId())
                .add(ModBlocks.GEL_SPLATTER_BOUNCY.getId())
                .add(ModBlocks.GEL_SPLATTER.getId())
                .add(ModItems.HOSEGUN.getId())
                .add(ModBlocks.FLUID_REFILL_STATION.getId())
                .add(ModBlocks.RADIATOR_PIPE.getId())
                .add(ModFluids.COOLANT.bucket.getId())
                .add(ModBlocks.DISTILLERY.getId())
                .add(ModBlocks.COMBUSTION_CHAMBER.getId())
                .add(ModBlocks.FUEL_TANK.getId())
                .add(ModBlocks.MILKING_STATION.getId())
                .add(ModBlocks.ADVANCED_PUMP.getId())
                .add(ModBlocks.BREWERS_TAP.getId())
                .add(ModFluids.LIQUID_CONCRETE.bucket.getId())
                .add(ModFluids.POURED_CEMENT.bucket.getId());
    }
}
