package com.resourceful_refinement.registry;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.casting_depot.CastingDepotItem;
import com.resourceful_refinement.content.forge_mould.MechanicalForgeMouldItem;
import com.resourceful_refinement.content.fracking_pump.FrackingPumpOutletItem;
import com.resourceful_refinement.content.geyser.GeyserItem;
import com.resourceful_refinement.content.moulds.MouldItem;
import com.resourceful_refinement.content.plushie.PlushieItem;
import com.resourceful_refinement.content.sieve.MechanicalSieveItem;
import com.resourceful_refinement.content.refinery.BlenderBladeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.resourceful_refinement.content.forge_mould.MechanicalForgeMouldBlockEntity.INGOT_MOULD_BREAK_CHANCE;
import static com.resourceful_refinement.content.forge_mould.MechanicalForgeMouldBlockEntity.SHAFT_MOULD_BREAK_CHANCE;

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

    // -------------------------------------------------------------------------
    // Misc. Items
    // -------------------------------------------------------------------------

    public static final DeferredItem<Item> FERROUS_CRYSTAL = ITEMS.registerItem("ferrous_crystal", Item::new, new Item.Properties());
    public static final DeferredItem<Item> FLUX_DUST = ITEMS.registerItem("flux_dust", Item::new, new Item.Properties());

    public static final DeferredItem<Item> DURASTEEL_INGOT = ITEMS.registerItem("durasteel_ingot", Item::new, new Item.Properties());
    public static final DeferredItem<Item> DURASTEEL_SHEET = ITEMS.registerItem("durasteel_sheet", Item::new, new Item.Properties());

    public static final DeferredItem<MouldItem> INGOT_MOULD = ITEMS.registerItem("ingot_mould",
            properties -> new MouldItem(properties, INGOT_MOULD_BREAK_CHANCE));
    public static final DeferredItem<MouldItem> SHAFT_MOULD = ITEMS.registerItem("shaft_mould",
            properties -> new MouldItem(properties, SHAFT_MOULD_BREAK_CHANCE));

    // -------------------------------------------------------------------------
    // Buckets (Handled by FluidEntry)
    // -------------------------------------------------------------------------
}
