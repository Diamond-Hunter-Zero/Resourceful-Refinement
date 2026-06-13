package com.resourceful_refinement.registry;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.casting_depot.CastingDepotItem;
import com.resourceful_refinement.content.combustion_chamber.CombustionChamberItem;
import com.resourceful_refinement.content.forge_mould.MechanicalForgeMouldItem;
import com.resourceful_refinement.content.fracking_pump.FrackingPumpOutletItem;
import com.resourceful_refinement.content.geyser.GeyserItem;
import com.resourceful_refinement.content.moulds.MouldItem;
import com.resourceful_refinement.content.plushie.PlushieItem;
import com.resourceful_refinement.content.refill_station.FluidRefillStationItem;
import com.resourceful_refinement.content.sieve.MechanicalSieveItem;
import com.resourceful_refinement.content.plunger.PlungerItem;
import com.resourceful_refinement.content.refinery.BlenderBladeItem;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;


public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ResourcefulRefinementMain.MOD_ID);

    // -------------------------------------------------------------------------
    // Mechanical Blocks
    // -------------------------------------------------------------------------
    public static final DeferredItem<BlenderBladeItem> BLENDER_BLADE = ITEMS.register("blender_blade",
            () -> new BlenderBladeItem(ModBlocks.BLENDER_BLADE.get(), new Item.Properties()));

    public static final DeferredItem<BlockItem> REFINERY_ACCESS_PORT = ITEMS.registerSimpleBlockItem("refinery_access_port", ModBlocks.REFINERY_ACCESS_PORT);
    // REFINERY_PROXY has no player-obtainable item — it is only placed programmatically during assembly.

    public static final DeferredItem<BlockItem> MECHANICAL_SIEVE_ITEM = ITEMS.register("mechanical_sieve",
            ()-> new MechanicalSieveItem(ModBlocks.MECHANICAL_SIEVE.get(), new Item.Properties()));

    public static final DeferredItem<MechanicalForgeMouldItem> MECHANICAL_FORGE_MOULD_ITEM = ITEMS.register("mechanical_forge_mould",
            () -> new MechanicalForgeMouldItem(ModBlocks.MECHANICAL_FORGE_MOULD.get(), new Item.Properties()));

    public static final DeferredItem<BlockItem> CASTING_DEPOT_ITEM = ITEMS.register("casting_depot",
            ()-> new CastingDepotItem(ModBlocks.CASTING_DEPOT.get(), new Item.Properties()));

    public static final DeferredItem<FrackingPumpOutletItem> FRACKING_PUMP_OUTLET_ITEM = ITEMS.register("fracking_pump_outlet",
            () -> new FrackingPumpOutletItem(ModBlocks.FRACKING_PUMP_OUTLET.get(), new Item.Properties()));

    public static final DeferredItem<GeyserItem> GEYSER_ITEM = ITEMS.register("geyser_block",
            () -> new GeyserItem(ModBlocks.GEYSER.get(), new Item.Properties()));

    public static final DeferredItem<PlushieItem> PLUSHIE_ITEM = ITEMS.register("fox_plushie",
            () -> new PlushieItem(ModBlocks.PLUSHIE.get(), new Item.Properties()));

    public static final DeferredItem<BlockItem> PAINT_NOZZLE_ITEM = ITEMS.registerSimpleBlockItem("paint_nozzle", ModBlocks.PAINT_NOZZLE);

    public static final DeferredItem<FluidRefillStationItem> FLUID_REFILL_STATION_ITEM = ITEMS.register("fluid_refill_station",
            () -> new FluidRefillStationItem(ModBlocks.FLUID_REFILL_STATION.get(), new Item.Properties()));

    public static final DeferredItem<BlockItem> DISTILLERY_ITEM = ITEMS.registerSimpleBlockItem("distillery", ModBlocks.DISTILLERY);

    public static final DeferredItem<BlockItem> RADIATOR_PIPE_ITEM = ITEMS.registerSimpleBlockItem("radiator_pipe", ModBlocks.RADIATOR_PIPE);

    public static final DeferredItem<CombustionChamberItem> COMBUSTION_CHAMBER_ITEM = ITEMS.register("combustion_chamber",
            () -> new CombustionChamberItem(ModBlocks.COMBUSTION_CHAMBER.get(), new Item.Properties()));


    // -------------------------------------------------------------------------
    // Gel Items
    // -------------------------------------------------------------------------

    public static final DeferredItem<BlockItem> GEL_SPLATTER_ITEM = ITEMS.registerSimpleBlockItem("gel_splatter", ModBlocks.GEL_SPLATTER);
    public static final DeferredItem<BlockItem> GEL_SPLATTER_STICKY_ITEM = ITEMS.registerSimpleBlockItem("gel_splatter_sticky", ModBlocks.GEL_SPLATTER_STICKY);
    public static final DeferredItem<BlockItem> GEL_SPLATTER_SLIPPERY_ITEM = ITEMS.registerSimpleBlockItem("gel_splatter_slippery", ModBlocks.GEL_SPLATTER_SLIPPERY);
    public static final DeferredItem<BlockItem> GEL_SPLATTER_MOLTEN_ITEM = ITEMS.registerSimpleBlockItem("gel_splatter_molten", ModBlocks.GEL_SPLATTER_MOLTEN);
    public static final DeferredItem<BlockItem> GEL_SPLATTER_BOUNCY_ITEM = ITEMS.registerSimpleBlockItem("gel_splatter_bouncy", ModBlocks.GEL_SPLATTER_BOUNCY);

    // -------------------------------------------------------------------------
    // Misc. Items
    // -------------------------------------------------------------------------

    public static final DeferredItem<Item> FERROUS_CRYSTAL = ITEMS.registerItem("ferrous_crystal", Item::new, new Item.Properties());
    public static final DeferredItem<Item> FLUX_DUST = ITEMS.registerItem("flux_dust", Item::new, new Item.Properties());

    public static final DeferredItem<Item> DURASTEEL_INGOT = ITEMS.registerItem("durasteel_ingot", Item::new, new Item.Properties());
    public static final DeferredItem<Item> DURASTEEL_SHEET = ITEMS.registerItem("durasteel_sheet", Item::new, new Item.Properties());

    public static final DeferredItem<MouldItem> INGOT_MOULD = ITEMS.registerItem("ingot_mould",
            MouldItem::new);
    public static final DeferredItem<MouldItem> SHAFT_MOULD = ITEMS.registerItem("shaft_mould",
            MouldItem::new);

    public static final DeferredItem<com.resourceful_refinement.content.hosegun.HosegunItem> HOSEGUN = ITEMS.register("hosegun",
            () -> new com.resourceful_refinement.content.hosegun.HosegunItem(new Item.Properties()));

    public static final DeferredItem<PlungerItem> PLUNGER = ITEMS.register("plunger",
            () -> new PlungerItem(PlungerItem.createProperties()));

    public static final DeferredItem<Item> PAINT_BLOB = ITEMS.registerItem("paint_blob", Item::new, new Item.Properties());


    // -------------------------------------------------------------------------
    // Foods?...
    // -------------------------------------------------------------------------

    public static final FoodProperties GLUE_POT_FOOD = new FoodProperties.Builder()
            .nutrition(0)                             // Hunger restored
            .saturationModifier(0.3F)                 // Saturation multiplier
            .alwaysEdible()                           // Allow eating even when full
            .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 200, 0), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.WEAKNESS, 200, 1), 1.0F)
            .build();

    public static final DeferredItem<Item> GLUE_POT = ITEMS.registerItem("glue_pot", Item::new, new Item.Properties()
            .food(GLUE_POT_FOOD));

    // -------------------------------------------------------------------------
    // Buckets (Handled by FluidEntry)
    // -------------------------------------------------------------------------
}
