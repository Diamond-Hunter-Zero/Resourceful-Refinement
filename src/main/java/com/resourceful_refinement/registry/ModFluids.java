package com.resourceful_refinement.registry;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.fluids.base.FluidGroup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

public class ModFluids {
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, ResourcefulRefinementMain.MOD_ID);
    public static final List<FluidEntry> ENTRIES = new ArrayList<>();

    private static FluidEntry register(String name, int color, FluidGroup group) {
        FluidEntry entry = new FluidEntry(name, color, group);
        ENTRIES.add(entry);
        return entry;
    }

    // Raw Molten Minerals
    public static final FluidEntry MOLTEN_CRIMSITE = register("molten_crimsite", 0xCC3333, FluidGroup.RAW);
    public static final FluidEntry MOLTEN_VERIDIUM = register("molten_veridium", 0x296E52, FluidGroup.RAW);
    public static final FluidEntry MOLTEN_OCHRUM = register("molten_ochrum", 0xD9B568, FluidGroup.RAW);
    public static final FluidEntry MOLTEN_ASURINE = register("molten_asurine", 0x4D8CA8, FluidGroup.RAW);
    public static final FluidEntry MOLTEN_SCORCHIA = register("molten_scorchia", 0x333333, FluidGroup.RAW);


    // Catalysed Fluids
    public static final FluidEntry CATALYSED_IRON = register("catalysed_iron", 0x94645A, FluidGroup.CATALYSED);
    public static final FluidEntry CATALYSED_COPPER = register("catalysed_copper", 0xB87333, FluidGroup.CATALYSED);
    public static final FluidEntry CATALYSED_GOLD = register("catalysed_gold", 0xFFD700, FluidGroup.CATALYSED);
    public static final FluidEntry CATALYSED_ZINC = register("catalysed_zinc", 0x7BA699, FluidGroup.CATALYSED);
    public static final FluidEntry CATALYSED_REDSTONE = register("catalysed_redstone", 0xFF0000, FluidGroup.CATALYSED);
    public static final FluidEntry CATALYSED_SPARKPOWDER = register("catalysed_sparkpowder", 0xF5E7A2, FluidGroup.CATALYSED);

    // Alloyed Fluids
    public static final FluidEntry SILICA_SUBSTRATE = register("silica_substrate", 0xA5A8B0, FluidGroup.ALLOYED);
    public static final FluidEntry MOLTEN_ANDESITE_BLEND = register("molten_andesite_blend", 0x7C7D80, FluidGroup.ALLOYED);
    public static final FluidEntry MOLTEN_BRASS_BLEND = register("molten_brass_blend", 0xD4AF37, FluidGroup.ALLOYED);
    public static final FluidEntry MOLTEN_NETHERITE_BLEND = register("molten_netherite_blend", 0x402B22, FluidGroup.ALLOYED);
    public static final FluidEntry DURASTEEL_ALLOY = register("durasteel_alloy", 0x2F4F4F, FluidGroup.ALLOYED);

    // Purified Fluids
    public static final FluidEntry PURIFIED_IRON = register("purified_iron", 0xD8D8D8, FluidGroup.PURIFIED);
    public static final FluidEntry PURIFIED_COPPER = register("purified_copper", 0xDE7514, FluidGroup.PURIFIED);
    public static final FluidEntry PURIFIED_GOLD = register("purified_gold", 0xFFD700, FluidGroup.PURIFIED);
    public static final FluidEntry PURIFIED_ZINC = register("purified_zinc", 0x91B8B4, FluidGroup.PURIFIED);
    public static final FluidEntry PURIFIED_DURASTEEL = register("purified_durasteel", 0x364744, FluidGroup.PURIFIED);

    // Carborax Fluids
    public static final FluidEntry UNREFINED_CARBORAX = register("unrefined_carborax", 0x29243B, FluidGroup.CARBORAX);
    public static final FluidEntry CATALYSED_CARBORAX = register("catalysed_carborax", 0x342152, FluidGroup.CARBORAX);
    public static final FluidEntry OVERCHARGED_CARBORAX = register("overcharged_carborax", 0x461B66, FluidGroup.CARBORAX);
    public static final FluidEntry CARBORAX_DIESEL = register("carborax_diesel", 0x7868A60, FluidGroup.CARBORAX);
}
