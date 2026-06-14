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

    // Misc Fluids
    public static final FluidEntry LIQUID_GLUE = register("liquid_glue", 0xDEDEB8, FluidGroup.ALLOYED);
    public static final FluidEntry COOLANT = register("coolant", 0xD9F4FA, FluidGroup.ALLOYED);

    // Paint Fluids (v0.2)
    public static final FluidEntry WHITE_PAINT = register("white_paint", 0xF9FFFE, FluidGroup.PAINT);
    public static final FluidEntry ORANGE_PAINT = register("orange_paint", 0xF9801D, FluidGroup.PAINT);
    public static final FluidEntry MAGENTA_PAINT = register("magenta_paint", 0xC74EBD, FluidGroup.PAINT);
    public static final FluidEntry LIGHT_BLUE_PAINT = register("light_blue_paint", 0x3AB3DA, FluidGroup.PAINT);
    public static final FluidEntry YELLOW_PAINT = register("yellow_paint", 0xFED83D, FluidGroup.PAINT);
    public static final FluidEntry LIME_PAINT = register("lime_paint", 0x80C71F, FluidGroup.PAINT);
    public static final FluidEntry PINK_PAINT = register("pink_paint", 0xF38BAA, FluidGroup.PAINT);
    public static final FluidEntry GRAY_PAINT = register("gray_paint", 0x474F52, FluidGroup.PAINT);
    public static final FluidEntry LIGHT_GRAY_PAINT = register("light_gray_paint", 0x9D9D97, FluidGroup.PAINT);
    public static final FluidEntry CYAN_PAINT = register("cyan_paint", 0x169C9C, FluidGroup.PAINT);
    public static final FluidEntry PURPLE_PAINT = register("purple_paint", 0x893293, FluidGroup.PAINT);
    public static final FluidEntry BLUE_PAINT = register("blue_paint", 0x3C44AA, FluidGroup.PAINT);
    public static final FluidEntry BROWN_PAINT = register("brown_paint", 0x835432, FluidGroup.PAINT);
    public static final FluidEntry GREEN_PAINT = register("green_paint", 0x5E7C16, FluidGroup.PAINT);
    public static final FluidEntry RED_PAINT = register("red_paint", 0xB02E26, FluidGroup.PAINT);
    public static final FluidEntry BLACK_PAINT = register("black_paint", 0x1D1D21, FluidGroup.PAINT);

    /** Paint fluid buckets use {@link FluidEntry#PAINT_FLUID_BUCKET_MODEL} instead of the vanilla bucket underlay. */
    /*public static boolean isPaintFluid(FluidEntry entry) {
        return entry != null && entry.group == FluidGroup.PAINT;
    }*/
}
