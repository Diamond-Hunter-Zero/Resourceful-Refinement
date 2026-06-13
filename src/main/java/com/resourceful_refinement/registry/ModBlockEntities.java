package com.resourceful_refinement.registry;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.casting_depot.CastingDepotBlockEntity;
import com.resourceful_refinement.content.combustion_chamber.CombustionChamberBlockEntity;
import com.resourceful_refinement.content.distillery.DistilleryBlockEntity;
import com.resourceful_refinement.content.fracking_pump.FrackingPumpOutletBlockEntity;
import com.resourceful_refinement.content.fracking_pump.FrackingPumpProxyBlockEntity;
import com.resourceful_refinement.content.paint_nozzle.PaintNozzleBlockEntity;
import com.resourceful_refinement.content.plushie.PlushieBlockEntity;
import com.resourceful_refinement.content.radiator.RadiatorBlockEntity;
import com.resourceful_refinement.content.refinery.BlenderBladeBlockEntity;
import com.resourceful_refinement.content.refinery.RefineryAccessPortBlockEntity;
import com.resourceful_refinement.content.refinery.RefineryProxyBlockEntity;
import com.resourceful_refinement.content.refinery.RefineryKineticProxyBlockEntity;
import com.resourceful_refinement.content.sieve.MechanicalFluidSieveBlockEntity;
import com.resourceful_refinement.content.forge_mould.MechanicalForgeMouldBlockEntity;
import com.resourceful_refinement.content.geyser.GeyserBlockEntity;
import com.resourceful_refinement.content.refill_station.FluidRefillStationBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ResourcefulRefinementMain.MOD_ID);

    public static final Supplier<BlockEntityType<BlenderBladeBlockEntity>> BLENDER_BLADE = BLOCK_ENTITIES.register("blender_blade",
            () -> BlockEntityType.Builder.of((pos, state) -> new BlenderBladeBlockEntity(ModBlockEntities.BLENDER_BLADE.get(), pos, state), ModBlocks.BLENDER_BLADE.get()).build(null));

    public static final Supplier<BlockEntityType<RefineryAccessPortBlockEntity>> REFINERY_ACCESS_PORT = BLOCK_ENTITIES.register("refinery_access_port",
            () -> BlockEntityType.Builder.of((pos, state) -> new RefineryAccessPortBlockEntity(ModBlockEntities.REFINERY_ACCESS_PORT.get(), pos, state), ModBlocks.REFINERY_ACCESS_PORT.get()).build(null));

    public static final Supplier<BlockEntityType<RefineryProxyBlockEntity>> REFINERY_PROXY = BLOCK_ENTITIES.register("refinery_proxy",
            () -> BlockEntityType.Builder.of((pos, state) -> new RefineryProxyBlockEntity(ModBlockEntities.REFINERY_PROXY.get(), pos, state), ModBlocks.REFINERY_PROXY.get()).build(null));

    public static final Supplier<BlockEntityType<RefineryKineticProxyBlockEntity>> REFINERY_KINETIC_PROXY = BLOCK_ENTITIES.register("refinery_kinetic_proxy",
            () -> BlockEntityType.Builder.of((pos, state) -> new RefineryKineticProxyBlockEntity(ModBlockEntities.REFINERY_KINETIC_PROXY.get(), pos, state), ModBlocks.REFINERY_KINETIC_PROXY.get()).build(null));

    public static final Supplier<BlockEntityType<MechanicalFluidSieveBlockEntity>> MECHANICAL_SIEVE_BE = BLOCK_ENTITIES.register("mechanical_sieve",
            () -> BlockEntityType.Builder.of((pos, state) -> new MechanicalFluidSieveBlockEntity(ModBlockEntities.MECHANICAL_SIEVE_BE.get(), pos, state), ModBlocks.MECHANICAL_SIEVE.get()).build(null));

    public static final Supplier<BlockEntityType<MechanicalForgeMouldBlockEntity>> MECHANICAL_FORGE_MOULD_BE = BLOCK_ENTITIES.register("mechanical_forge_mould",
            () -> BlockEntityType.Builder.of((pos, state) -> new MechanicalForgeMouldBlockEntity(ModBlockEntities.MECHANICAL_FORGE_MOULD_BE.get(), pos, state), ModBlocks.MECHANICAL_FORGE_MOULD.get()).build(null));

    public static final Supplier<BlockEntityType<CastingDepotBlockEntity>> CASTING_DEPOT_BE = BLOCK_ENTITIES.register("casting_depot",
            () -> BlockEntityType.Builder.of((pos, state) -> new CastingDepotBlockEntity(ModBlockEntities.CASTING_DEPOT_BE.get(), pos, state), ModBlocks.CASTING_DEPOT.get()).build(null));

    public static final Supplier<BlockEntityType<FrackingPumpOutletBlockEntity>> FRACKING_PUMP_OUTLET_BE = BLOCK_ENTITIES.register("fracking_pump_outlet",
            () -> BlockEntityType.Builder.of((pos, state) -> new FrackingPumpOutletBlockEntity(ModBlockEntities.FRACKING_PUMP_OUTLET_BE.get(), pos, state), ModBlocks.FRACKING_PUMP_OUTLET.get()).build(null));

    public static final Supplier<BlockEntityType<FrackingPumpProxyBlockEntity>> FRACKING_PUMP_PROXY_BE = BLOCK_ENTITIES.register("fracking_pump_proxy",
            () -> BlockEntityType.Builder.of((pos, state) -> new FrackingPumpProxyBlockEntity(ModBlockEntities.FRACKING_PUMP_PROXY_BE.get(), pos, state), ModBlocks.FRACKING_PUMP_PROXY.get()).build(null));

    public static final Supplier<BlockEntityType<GeyserBlockEntity>> GEYSER_BE = BLOCK_ENTITIES.register("geyser_block",
            () -> BlockEntityType.Builder.of((pos, state) -> new GeyserBlockEntity(ModBlockEntities.GEYSER_BE.get(), pos, state), ModBlocks.GEYSER.get()).build(null));

    public static final Supplier<BlockEntityType<PlushieBlockEntity>> PLUSHIE_BE = BLOCK_ENTITIES.register("fox_plushie",
            () -> BlockEntityType.Builder.of((pos, state) -> new PlushieBlockEntity(ModBlockEntities.PLUSHIE_BE.get(), pos, state), ModBlocks.PLUSHIE.get()).build(null));

    public static final Supplier<BlockEntityType<PaintNozzleBlockEntity>> PAINT_NOZZLE_BE = BLOCK_ENTITIES.register("paint_nozzle",
            () -> BlockEntityType.Builder.of((pos, state) -> new PaintNozzleBlockEntity(ModBlockEntities.PAINT_NOZZLE_BE.get(), pos, state), ModBlocks.PAINT_NOZZLE.get()).build(null));

    public static final Supplier<BlockEntityType<FluidRefillStationBlockEntity>> FLUID_REFILL_STATION_BE = BLOCK_ENTITIES.register("fluid_refill_station",
            () -> BlockEntityType.Builder.of(
                    (pos, state) -> new FluidRefillStationBlockEntity(ModBlockEntities.FLUID_REFILL_STATION_BE.get(), pos, state),
                    ModBlocks.FLUID_REFILL_STATION.get()
            ).build(null));

    public static final Supplier<BlockEntityType<DistilleryBlockEntity>> DISTILLERY_BE = BLOCK_ENTITIES.register("distillery",
            () -> BlockEntityType.Builder.of((pos, state) -> new DistilleryBlockEntity(ModBlockEntities.DISTILLERY_BE.get(), pos, state), ModBlocks.DISTILLERY.get()).build(null));

    public static final Supplier<BlockEntityType<RadiatorBlockEntity>> RADIATOR_PIPE_BE = BLOCK_ENTITIES.register("radiator_pipe",
            () -> BlockEntityType.Builder.of((pos, state) -> new RadiatorBlockEntity(ModBlockEntities.RADIATOR_PIPE_BE.get(), pos, state), ModBlocks.RADIATOR_PIPE.get()).build(null));

    public static final Supplier<BlockEntityType<CombustionChamberBlockEntity>> COMBUSTION_CHAMBER_BE = BLOCK_ENTITIES.register("combustion_chamber",
            () -> BlockEntityType.Builder.of((pos, state) -> new CombustionChamberBlockEntity(ModBlockEntities.COMBUSTION_CHAMBER_BE.get(), pos, state), ModBlocks.COMBUSTION_CHAMBER.get()).build(null));




    /** Shared by {@code gel_splatter}, {@code gel_splatter_sticky}, and {@code gel_splatter_slippery}. */
    public static final Supplier<BlockEntityType<com.resourceful_refinement.content.gel_splatter.GelSplatterBlockEntity>> GEL_SPLATTER_BE =
            BLOCK_ENTITIES.register("gel_splatter",
                    () -> BlockEntityType.Builder.of(
                            com.resourceful_refinement.content.gel_splatter.GelSplatterBlockEntity::new,
                            ModBlocks.GEL_SPLATTER.get(),
                            ModBlocks.GEL_SPLATTER_SLIPPERY.get(),
                            ModBlocks.GEL_SPLATTER_STICKY.get(),
                            ModBlocks.GEL_SPLATTER_MOLTEN.get(),
                            ModBlocks.GEL_SPLATTER_BOUNCY.get()
                    ).build(null));
}
