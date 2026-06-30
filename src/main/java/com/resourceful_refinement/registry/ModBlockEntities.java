package com.resourceful_refinement.registry;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.advanced_pump.AdvancedPumpBlockEntity;
import com.resourceful_refinement.content.brewers_tap.BrewersTapBlockEntity;
import com.resourceful_refinement.content.bucket_excavator.BucketExcavatorBlockEntity;
import com.resourceful_refinement.content.casting_depot.CastingDepotBlockEntity;
import com.resourceful_refinement.content.combustion_chamber.CombustionChamberBlockEntity;
import com.resourceful_refinement.content.distillery.DistilleryBlockEntity;
import com.resourceful_refinement.content.drill_pylon.CrystalFissureBudBlockEntity;
import com.resourceful_refinement.content.drill_pylon.DrillPylonHeadBlockEntity;
import com.resourceful_refinement.content.drill_pylon.DrillPylonKineticProxyBlockEntity;
import com.resourceful_refinement.content.drill_pylon.DrillPylonProxyBlockEntity;
import com.resourceful_refinement.content.fracking_pump.FrackingPumpOutletBlockEntity;
import com.resourceful_refinement.content.fracking_pump.FrackingPumpProxyBlockEntity;
import com.resourceful_refinement.content.fuel_tank.FuelTankBlockEntity;
import com.resourceful_refinement.content.milking_station.MilkingStationBlockEntity;
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
import com.resourceful_refinement.content.glare.GlareEmitterDishBlockEntity;
import com.resourceful_refinement.content.glare.GlareChromaticTransceiverBlockEntity;
import com.resourceful_refinement.content.glare.GlareKineticReceiverBlockEntity;
import com.resourceful_refinement.content.glare.GlareRelayBlockEntity;
import com.resourceful_refinement.content.glare.lux.LuxTransceiverBlockEntity;
import com.resourceful_refinement.content.glare.terminal.TelemetryTerminalBlockEntity;
import com.resourceful_refinement.content.glare.remote.RemoteEntanglerDepotBlockEntity;
import com.resourceful_refinement.content.glare.remote.RemoteEntanglementTransporterBlockEntity;
import com.resourceful_refinement.content.glare.remote.RemoteTransporterProxyBlockEntity;
import com.resourceful_refinement.content.refill_station.FluidRefillStationBlockEntity;
import com.resourceful_refinement.content.pug.LaunchpadControllerBlockEntity;
import com.resourceful_refinement.content.pug.LaunchpadProxyBlockEntity;
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

    public static final Supplier<BlockEntityType<BucketExcavatorBlockEntity>> BUCKET_EXCAVATOR_BE = BLOCK_ENTITIES.register("bucket_excavator",
            () -> BlockEntityType.Builder.of((pos, state) -> new BucketExcavatorBlockEntity(ModBlockEntities.BUCKET_EXCAVATOR_BE.get(), pos, state), ModBlocks.BUCKET_EXCAVATOR.get()).build(null));

    public static final Supplier<BlockEntityType<CrystalFissureBudBlockEntity>> CRYSTAL_FISSURE_BUD_BE = BLOCK_ENTITIES.register("crystal_fissure_bud",
            () -> BlockEntityType.Builder.of(CrystalFissureBudBlockEntity::new, ModBlocks.CRYSTAL_FISSURE_BUD.get()).build(null));

    public static final Supplier<BlockEntityType<DrillPylonHeadBlockEntity>> DRILL_PYLON_HEAD_BE = BLOCK_ENTITIES.register("drill_pylon_head",
            () -> BlockEntityType.Builder.of((pos, state) -> new DrillPylonHeadBlockEntity(ModBlockEntities.DRILL_PYLON_HEAD_BE.get(), pos, state), ModBlocks.DRILL_PYLON_HEAD.get()).build(null));

    public static final Supplier<BlockEntityType<DrillPylonProxyBlockEntity>> DRILL_PYLON_PROXY_BE = BLOCK_ENTITIES.register("drill_pylon_proxy",
            () -> BlockEntityType.Builder.of(DrillPylonProxyBlockEntity::new, ModBlocks.DRILL_PYLON_PROXY.get()).build(null));

    public static final Supplier<BlockEntityType<DrillPylonKineticProxyBlockEntity>> DRILL_PYLON_KINETIC_PROXY_BE = BLOCK_ENTITIES.register("drill_pylon_kinetic_proxy",
            () -> BlockEntityType.Builder.of(DrillPylonKineticProxyBlockEntity::new, ModBlocks.DRILL_PYLON_KINETIC_PROXY.get()).build(null));


    // GLARE blocks

    public static final Supplier<BlockEntityType<GlareRelayBlockEntity>> GLARE_RELAY_BE = BLOCK_ENTITIES.register("glare_relay",
            () -> BlockEntityType.Builder.of(GlareRelayBlockEntity::new, ModBlocks.GLARE_RELAY.get()).build(null));

    public static final Supplier<BlockEntityType<GlareEmitterDishBlockEntity>> GLARE_EMITTER_DISH_BE = BLOCK_ENTITIES.register("glare_emitter_dish",
            () -> BlockEntityType.Builder.of(GlareEmitterDishBlockEntity::new, ModBlocks.GLARE_EMITTER_DISH.get()).build(null));

    public static final Supplier<BlockEntityType<GlareKineticReceiverBlockEntity>> GLARE_KINETIC_RECEIVER_BE = BLOCK_ENTITIES.register("glare_kinetic_receiver",
            () -> BlockEntityType.Builder.of(GlareKineticReceiverBlockEntity::new, ModBlocks.GLARE_KINETIC_RECEIVER.get()).build(null));

    public static final Supplier<BlockEntityType<GlareChromaticTransceiverBlockEntity>> GLARE_CHROMATIC_TRANSCEIVER_BE = BLOCK_ENTITIES.register("glare_chromatic_transceiver",
            () -> BlockEntityType.Builder.of(GlareChromaticTransceiverBlockEntity::new, ModBlocks.GLARE_CHROMATIC_TRANSCEIVER.get()).build(null));

    public static final Supplier<BlockEntityType<TelemetryTerminalBlockEntity>> GLARE_TELEMETRY_TERMINAL_BE = BLOCK_ENTITIES.register("glare_telemetry_terminal",
            () -> BlockEntityType.Builder.of(TelemetryTerminalBlockEntity::new, ModBlocks.GLARE_TELEMETRY_TERMINAL.get()).build(null));

    public static final Supplier<BlockEntityType<LuxTransceiverBlockEntity>> LUX_TRANSCEIVER_BE = BLOCK_ENTITIES.register("lux_transceiver",
            () -> BlockEntityType.Builder.of(LuxTransceiverBlockEntity::new, ModBlocks.LUX_TRANSCEIVER.get()).build(null));

    public static final Supplier<BlockEntityType<RemoteEntanglerDepotBlockEntity>> REMOTE_ENTANGLER_DEPOT_BE = BLOCK_ENTITIES.register(
            "remote_entangler_depot", () -> BlockEntityType.Builder.of(
                    (pos, state) -> new RemoteEntanglerDepotBlockEntity(ModBlockEntities.REMOTE_ENTANGLER_DEPOT_BE.get(), pos, state),
                    ModBlocks.REMOTE_ENTANGLER_DEPOT.get()).build(null));

    public static final Supplier<BlockEntityType<RemoteEntanglementTransporterBlockEntity>> REMOTE_ENTANGLEMENT_TRANSPORTER_BE = BLOCK_ENTITIES.register(
            "remote_entanglement_transporter", () -> BlockEntityType.Builder.of(RemoteEntanglementTransporterBlockEntity::new,
                    ModBlocks.REMOTE_ENTANGLEMENT_TRANSPORTER.get()).build(null));

    public static final Supplier<BlockEntityType<RemoteTransporterProxyBlockEntity>> REMOTE_ENTANGLEMENT_TRANSPORTER_PROXY_BE = BLOCK_ENTITIES.register(
            "remote_entanglement_transporter_proxy", () -> BlockEntityType.Builder.of(RemoteTransporterProxyBlockEntity::new,
                    ModBlocks.REMOTE_ENTANGLEMENT_TRANSPORTER_TANK.get(),
                    ModBlocks.REMOTE_ENTANGLEMENT_TRANSPORTER_CASING.get()).build(null));

    public static final Supplier<BlockEntityType<LaunchpadControllerBlockEntity>> LAUNCHPAD_CONTROLLER_BE = BLOCK_ENTITIES.register(
            "launchpad_controller", () -> BlockEntityType.Builder.of(LaunchpadControllerBlockEntity::new,
                    ModBlocks.LAUNCHPAD_CONTROLLER.get()).build(null));

    public static final Supplier<BlockEntityType<LaunchpadProxyBlockEntity>> LAUNCHPAD_PROXY_BE = BLOCK_ENTITIES.register(
            "launchpad_proxy", () -> BlockEntityType.Builder.of(LaunchpadProxyBlockEntity::new,
                    ModBlocks.LAUNCHPAD_PROXY.get()).build(null));

    public static final Supplier<BlockEntityType<DistilleryBlockEntity>> DISTILLERY_BE = BLOCK_ENTITIES.register("distillery",
            () -> BlockEntityType.Builder.of((pos, state) -> new DistilleryBlockEntity(ModBlockEntities.DISTILLERY_BE.get(), pos, state), ModBlocks.DISTILLERY.get()).build(null));

    public static final Supplier<BlockEntityType<RadiatorBlockEntity>> RADIATOR_PIPE_BE = BLOCK_ENTITIES.register("radiator_pipe",
            () -> BlockEntityType.Builder.of((pos, state) -> new RadiatorBlockEntity(ModBlockEntities.RADIATOR_PIPE_BE.get(), pos, state), ModBlocks.RADIATOR_PIPE.get()).build(null));

    public static final Supplier<BlockEntityType<CombustionChamberBlockEntity>> COMBUSTION_CHAMBER_BE = BLOCK_ENTITIES.register("combustion_chamber",
            () -> BlockEntityType.Builder.of((pos, state) -> new CombustionChamberBlockEntity(ModBlockEntities.COMBUSTION_CHAMBER_BE.get(), pos, state), ModBlocks.COMBUSTION_CHAMBER.get()).build(null));

    public static final Supplier<BlockEntityType<FuelTankBlockEntity>> FUEL_TANK_BE = BLOCK_ENTITIES.register("fuel_tank",
            () -> BlockEntityType.Builder.of((pos, state) -> new FuelTankBlockEntity(ModBlockEntities.FUEL_TANK_BE.get(), pos, state), ModBlocks.FUEL_TANK.get()).build(null));

    public static final Supplier<BlockEntityType<AdvancedPumpBlockEntity>> ADVANCED_PUMP_BE = BLOCK_ENTITIES.register("advanced_pump",
            () -> BlockEntityType.Builder.of((pos, state) -> new AdvancedPumpBlockEntity(ModBlockEntities.ADVANCED_PUMP_BE.get(), pos, state), ModBlocks.ADVANCED_PUMP.get()).build(null));

    public static final Supplier<BlockEntityType<MilkingStationBlockEntity>> MILKING_STATION_BE = BLOCK_ENTITIES.register("milking_station",
            () -> BlockEntityType.Builder.of((pos, state) -> new MilkingStationBlockEntity(ModBlockEntities.MILKING_STATION_BE.get(), pos, state), ModBlocks.MILKING_STATION.get()).build(null));

    public static final Supplier<BlockEntityType<BrewersTapBlockEntity>> BREWERS_TAP_BE = BLOCK_ENTITIES.register("brewers_tap",
            () -> BlockEntityType.Builder.of((pos, state) -> new BrewersTapBlockEntity(ModBlockEntities.BREWERS_TAP_BE.get(), pos, state), ModBlocks.BREWERS_TAP.get()).build(null));



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
