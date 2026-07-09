package com.resourceful_refinement.registry;

import com.simibubi.create.api.stress.BlockStressValues;
import net.minecraft.world.level.block.Block;

import java.util.function.DoubleSupplier;

public class ModStressValues {

    public static final double REFINERY_STRESS = 12;
    public static final double FRACKING_STRESS = 16;
    public static final double SIEVE_STRESS = 4;
    public static final double FORGE_STRESS = 8;
    public static final double MECHANICAL_STAMPER_STRESS = 4;
    public static final double ADVANCED_PUMP_STRESS = 8;
    public static final double MILKING_STATION_STRESS = 4;
    public static final double CONVEYOR_ROTATOR_STRESS = 2;
    public static final double GLARE_EMITTER_STRESS = 8;
    public static final double BUCKET_EXCAVATOR_STRESS = 16;
    public static final double DRILL_PYLON_STRESS = 16;
    public static final double CYCLOTRON_STRESS = 32;


    public static void register() {
        // Register stresses
        registerImpact(ModBlocks.REFINERY_KINETIC_PROXY.get(), () -> REFINERY_STRESS);
        registerImpact(ModBlocks.FRACKING_PUMP_OUTLET.get(), () -> FRACKING_STRESS);
        registerImpact(ModBlocks.MECHANICAL_SIEVE.get(), () -> SIEVE_STRESS);
        registerImpact(ModBlocks.MECHANICAL_FORGE_MOULD.get(), () -> FORGE_STRESS);
        registerImpact(ModBlocks.MECHANICAL_STAMPER.get(), () -> MECHANICAL_STAMPER_STRESS);
        registerImpact(ModBlocks.ADVANCED_PUMP.get(), () -> ADVANCED_PUMP_STRESS);
        registerImpact(ModBlocks.MILKING_STATION.get(), () -> MILKING_STATION_STRESS);
        registerImpact(ModBlocks.BUCKET_EXCAVATOR.get(), () -> BUCKET_EXCAVATOR_STRESS);
        registerImpact(ModBlocks.DRILL_PYLON_HEAD.get(), () -> DRILL_PYLON_STRESS);
        registerImpact(ModBlocks.DRILL_PYLON_KINETIC_PROXY.get(), () -> DRILL_PYLON_STRESS);
        registerImpact(ModBlocks.CYCLOTRON_KINETIC_PROXY.get(), () -> CYCLOTRON_STRESS);
        registerImpact(ModBlocks.CONVEYOR_ROTATOR.get(), () -> CONVEYOR_ROTATOR_STRESS);

        // Register generation capacities
        registerCapacity(ModBlocks.COMBUSTION_CHAMBER.get(), () -> 10);
        registerImpact(ModBlocks.GLARE_EMITTER_DISH.get(), () -> GLARE_EMITTER_STRESS);
    }

    private static void registerImpact(Block block, DoubleSupplier impact) {
        BlockStressValues.IMPACTS.register(block, impact);
    }

    private static void registerCapacity(Block block, DoubleSupplier capacity) {
        BlockStressValues.CAPACITIES.register(block, capacity);
    }
}
