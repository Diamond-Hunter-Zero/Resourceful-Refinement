package com.resourceful_refinement.registry;

import com.simibubi.create.api.stress.BlockStressValues;
import net.minecraft.world.level.block.Block;

import java.util.function.DoubleSupplier;

public class ModStressValues {

    public static final double REFINERY_STRESS = 12;
    public static final double FRACKING_STRESS = 16;
    public static final double SIEVE_STRESS = 4;
    public static final double FORGE_STRESS = 8;


    public static void register() {
        // Register stresses
        registerImpact(ModBlocks.REFINERY_KINETIC_PROXY.get(), () -> REFINERY_STRESS);
        registerImpact(ModBlocks.FRACKING_PUMP_OUTLET.get(), () -> FRACKING_STRESS);
        registerImpact(ModBlocks.MECHANICAL_SIEVE.get(), () -> SIEVE_STRESS);
        registerImpact(ModBlocks.MECHANICAL_FORGE_MOULD.get(), () -> FORGE_STRESS);

        // Register generation capacities
        registerCapacity(ModBlocks.COMBUSTION_CHAMBER.get(), () -> 10);
    }

    private static void registerImpact(Block block, DoubleSupplier impact) {
        BlockStressValues.IMPACTS.register(block, impact);
    }

    private static void registerCapacity(Block block, DoubleSupplier capacity) {
        BlockStressValues.CAPACITIES.register(block, capacity);
    }
}
