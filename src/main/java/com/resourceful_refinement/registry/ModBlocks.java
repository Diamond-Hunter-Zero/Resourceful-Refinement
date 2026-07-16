package com.resourceful_refinement.registry;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.advanced_pump.AdvancedPumpBlock;
import com.resourceful_refinement.content.brewers_tap.BrewersTapBlock;
import com.resourceful_refinement.content.bucket_excavator.BucketExcavatorBlock;
import com.resourceful_refinement.content.casting_depot.CastingDepotBlock;
import com.resourceful_refinement.content.combustion_chamber.CombustionChamberBlock;
import com.resourceful_refinement.content.cyclotron_forge.CyclotronControllerBlock;
import com.resourceful_refinement.content.cyclotron_forge.CyclotronKineticProxyBlock;
import com.resourceful_refinement.content.cyclotron_forge.CyclotronProxyBlock;
import com.resourceful_refinement.content.conveyor.ConveyorBeltBlock;
import com.resourceful_refinement.content.conveyor.ConveyorRotatorBlock;
import com.resourceful_refinement.content.conveyor.ConveyorRotatorProxyBlock;
import com.resourceful_refinement.content.distillery.DistilleryBlock;
import com.resourceful_refinement.content.drill_pylon.CrystalFissureBudBlock;
import com.resourceful_refinement.content.drill_pylon.DrillPylonHeadBlock;
import com.resourceful_refinement.content.drill_pylon.DrillPylonKineticProxyBlock;
import com.resourceful_refinement.content.drill_pylon.DrillPylonProxyBlock;
import com.resourceful_refinement.content.forge_mould.MechanicalForgeMouldBlock;
import com.resourceful_refinement.content.fracking_pump.FrackingPumpOutletBlock;
import com.resourceful_refinement.content.fracking_pump.FrackingPumpProxyBlock;
import com.resourceful_refinement.content.fuel_tank.FuelTankBlock;
import com.resourceful_refinement.content.gel_splatter.GelSplatterBlock;
import com.resourceful_refinement.content.mechanical_stamper.MechanicalStamperBlock;
import com.resourceful_refinement.content.glare.GlareEmitterDishBlock;
import com.resourceful_refinement.content.glare.GlareChromaticTransceiverBlock;
import com.resourceful_refinement.content.glare.GlareKineticReceiverBlockEntity;
import com.resourceful_refinement.content.glare.GlareNodeBlock;
import com.resourceful_refinement.content.glare.GlareRelayBlockEntity;
import com.resourceful_refinement.content.glare.ResonanceCrystalBlock;
import com.resourceful_refinement.content.glare.lux.LuxTransceiverBlock;
import com.resourceful_refinement.content.glare.terminal.TelemetryTerminalBlock;
import com.resourceful_refinement.content.glare.remote.RemoteEntanglerDepotBlock;
import com.resourceful_refinement.content.glare.remote.RemoteEntanglementTransporterBlock;
import com.resourceful_refinement.content.glare.remote.RemoteTransporterProxyBlock;
import com.resourceful_refinement.content.milking_station.MilkingStationBlock;
import com.resourceful_refinement.content.manifold.ManifoldBlock;
import com.resourceful_refinement.content.pug.LaunchpadControllerBlock;
import com.resourceful_refinement.content.pug.LaunchpadProxyBlock;
import com.resourceful_refinement.content.paint_nozzle.PaintNozzleBlock;
import com.resourceful_refinement.content.plushie.PlushieBlock;
import com.resourceful_refinement.content.radiator.RadiatorBlock;
import com.resourceful_refinement.content.refill_station.FluidRefillStationBlock;
import com.resourceful_refinement.content.refinery.BlenderBladeBlock;
import com.resourceful_refinement.content.refinery.RefineryAccessPortBlock;
import com.resourceful_refinement.content.refinery.RefineryProxyBlock;
import com.resourceful_refinement.content.refinery.RefineryKineticProxyBlock;
import com.resourceful_refinement.content.research_terminal.ResearchTerminalBlock;
import com.resourceful_refinement.content.sieve.MechanicalFluidSieveBlock;
import com.resourceful_refinement.content.geyser.GeyserBlock;
import com.simibubi.create.AllBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ResourcefulRefinementMain.MOD_ID);

    // -------------------------------------------------------------------------
    // v0.1 - v0.2 Mechanical Blocks
    // -------------------------------------------------------------------------
    public static final DeferredBlock<BlenderBladeBlock> BLENDER_BLADE = BLOCKS.register("blender_blade",
            () -> new BlenderBladeBlock(BlockBehaviour.Properties.of().strength(1.0f).sound(SoundType.METAL).noOcclusion()));

    public static final DeferredBlock<RefineryAccessPortBlock> REFINERY_ACCESS_PORT = BLOCKS.register("refinery_access_port",
            () -> new RefineryAccessPortBlock(BlockBehaviour.Properties.of().pushReaction(PushReaction.BLOCK).strength(2.5f).sound(SoundType.COPPER).requiresCorrectToolForDrops().noOcclusion()));

    public static final DeferredBlock<RefineryProxyBlock> REFINERY_PROXY = BLOCKS.register("refinery_proxy",
            () -> new RefineryProxyBlock(BlockBehaviour.Properties.of()
                    .pushReaction(PushReaction.BLOCK)
                    .strength(2.5f)
                    .sound(SoundType.COPPER)
                    .noOcclusion()
                    .isRedstoneConductor((s, g, p) -> false)));

    public static final DeferredBlock<RefineryKineticProxyBlock> REFINERY_KINETIC_PROXY = BLOCKS.register("refinery_kinetic_proxy",
            () -> new RefineryKineticProxyBlock(BlockBehaviour.Properties.of()
                    .pushReaction(PushReaction.BLOCK)
                    .strength(2.5f)
                    .sound(SoundType.COPPER)
                    .noOcclusion()
                    .isRedstoneConductor((s, g, p) -> false)));

    public static final DeferredBlock<MechanicalFluidSieveBlock> MECHANICAL_SIEVE = BLOCKS.register("mechanical_sieve",
            () -> new MechanicalFluidSieveBlock(BlockBehaviour.Properties.of().strength(2.5f).sound(SoundType.COPPER).requiresCorrectToolForDrops().noOcclusion()));

    public static final DeferredBlock<MechanicalForgeMouldBlock> MECHANICAL_FORGE_MOULD = BLOCKS.register("mechanical_forge_mould",
            () -> new MechanicalForgeMouldBlock(BlockBehaviour.Properties.of().strength(2.5f).sound(SoundType.COPPER).requiresCorrectToolForDrops().noOcclusion()));

    public static final DeferredBlock<MechanicalStamperBlock> MECHANICAL_STAMPER = BLOCKS.register("mechanical_stamper",
            () -> new MechanicalStamperBlock(BlockBehaviour.Properties.of().strength(2.5f).sound(SoundType.COPPER).requiresCorrectToolForDrops().noOcclusion()));

    public static final DeferredBlock<CastingDepotBlock> CASTING_DEPOT = BLOCKS.register("casting_depot",
            () -> new CastingDepotBlock(BlockBehaviour.Properties.ofFullCopy(AllBlocks.DEPOT.get()).requiresCorrectToolForDrops().noOcclusion()));

    public static final DeferredBlock<ResearchTerminalBlock> RESEARCH_TERMINAL = BLOCKS.register("research_terminal",
            () -> new ResearchTerminalBlock(BlockBehaviour.Properties.ofFullCopy(AllBlocks.DEPOT.get()).requiresCorrectToolForDrops().noOcclusion()));

    public static final DeferredBlock<FrackingPumpOutletBlock> FRACKING_PUMP_OUTLET = BLOCKS.register("fracking_pump_outlet",
            () -> new FrackingPumpOutletBlock(BlockBehaviour.Properties.of().pushReaction(PushReaction.BLOCK).strength(2.5f).sound(SoundType.COPPER).requiresCorrectToolForDrops().noOcclusion()));

    public static final DeferredBlock<FrackingPumpProxyBlock> FRACKING_PUMP_PROXY = BLOCKS.register("fracking_pump_proxy",
            () -> new FrackingPumpProxyBlock(BlockBehaviour.Properties.of()
                    .pushReaction(PushReaction.BLOCK)
                    .strength(2.5f)
                    .sound(SoundType.COPPER)
                    .noOcclusion()));

    public static final DeferredBlock<GeyserBlock> GEYSER = BLOCKS.register("geyser_block",
            () -> new GeyserBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                    .pushReaction(PushReaction.BLOCK)
                    .strength(50.0f, 1200.0f) // High mining time and explosion resistance
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final DeferredBlock<PlushieBlock> PLUSHIE = BLOCKS.register("fox_plushie",
            () -> new PlushieBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_WOOL)
                    .noOcclusion()));

    public static final DeferredBlock<PaintNozzleBlock> PAINT_NOZZLE = BLOCKS.register("paint_nozzle",
            () -> new PaintNozzleBlock(BlockBehaviour.Properties.of()
                    .strength(2.5f)
                    .sound(SoundType.COPPER)
                    .noOcclusion()));

    public static final DeferredBlock<FluidRefillStationBlock> FLUID_REFILL_STATION = BLOCKS.register("fluid_refill_station",
            () -> new FluidRefillStationBlock(BlockBehaviour.Properties.of()
                    .strength(2.5f)
                    .sound(SoundType.COPPER)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));


    // -------------------------------------------------------------------------
    // v0.3 Blocks
    // -------------------------------------------------------------------------

    public static final DeferredBlock<DistilleryBlock> DISTILLERY = BLOCKS.register("distillery",
            () -> new DistilleryBlock(BlockBehaviour.Properties.of()
                    .strength(2.5f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final DeferredBlock<RadiatorBlock> RADIATOR_PIPE = BLOCKS.register("radiator_pipe",
            () -> new RadiatorBlock(BlockBehaviour.Properties.of()
                    .strength(1f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final DeferredBlock<CombustionChamberBlock> COMBUSTION_CHAMBER = BLOCKS.register("combustion_chamber",
            () -> new CombustionChamberBlock(BlockBehaviour.Properties.of()
                    .strength(1.5f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final DeferredBlock<FuelTankBlock> FUEL_TANK = BLOCKS.register("fuel_tank",
            () -> new FuelTankBlock(BlockBehaviour.Properties.of()
                    .strength(1.5f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final DeferredBlock<AdvancedPumpBlock> ADVANCED_PUMP = BLOCKS.register("advanced_pump",
            () -> new AdvancedPumpBlock(BlockBehaviour.Properties.ofFullCopy(AllBlocks.MECHANICAL_PUMP.get())
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final DeferredBlock<MilkingStationBlock> MILKING_STATION = BLOCKS.register("milking_station",
            () -> new MilkingStationBlock(BlockBehaviour.Properties.of()
                    .strength(2.5f)
                    .sound(SoundType.COPPER)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final DeferredBlock<BrewersTapBlock> BREWERS_TAP = BLOCKS.register("brewers_tap",
            () -> new BrewersTapBlock(BlockBehaviour.Properties.of()
                    .strength(1.5f)
                    .sound(SoundType.COPPER)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final DeferredBlock<ConveyorBeltBlock> CONVEYOR_BELT = BLOCKS.register("conveyor_belt",
            () -> new ConveyorBeltBlock(BlockBehaviour.Properties.of()
                    .strength(1.0f)
                    .sound(SoundType.WOOL)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final DeferredBlock<ConveyorRotatorBlock> CONVEYOR_ROTATOR = BLOCKS.register("conveyor_rotator",
            () -> new ConveyorRotatorBlock(BlockBehaviour.Properties.of()
                    .strength(2.0f)
                    .sound(SoundType.COPPER)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final DeferredBlock<ConveyorRotatorProxyBlock> CONVEYOR_ROTATOR_PROXY = BLOCKS.register("conveyor_rotator_proxy",
            () -> new ConveyorRotatorProxyBlock(BlockBehaviour.Properties.of()
                    .strength(-1.0f, 3600000.0f)
                    .noLootTable()
                    .noOcclusion()
                    .pushReaction(PushReaction.BLOCK)));

    public static final DeferredBlock<ManifoldBlock> MANIFOLD = BLOCKS.register("manifold_block",
            () -> new ManifoldBlock(BlockBehaviour.Properties.of()
                    .strength(2.0f)
                    .sound(SoundType.COPPER)
                    .requiresCorrectToolForDrops()));


    // -------------------------------------------------------------------------
    // v0.4 Blocks
    // -------------------------------------------------------------------------

    public static final DeferredBlock<BucketExcavatorBlock> BUCKET_EXCAVATOR = BLOCKS.register("bucket_excavator",
            () -> new BucketExcavatorBlock(BlockBehaviour.Properties.of()
                    .strength(2.5f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final DeferredBlock<Block> MINERAL_DEPOSIT = BLOCKS.register("mineral_deposit",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(2f)
                    .sound(SoundType.STONE)));

    public static final DeferredBlock<CrystalFissureBudBlock> CRYSTAL_FISSURE_BUD = BLOCKS.register("crystal_fissure_bud",
            () -> new CrystalFissureBudBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_BLOCK)
                    .pushReaction(PushReaction.BLOCK)
                    .strength(50.0f, 1200.0f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final DeferredBlock<DrillPylonHeadBlock> DRILL_PYLON_HEAD = BLOCKS.register("drill_pylon_head",
            () -> new DrillPylonHeadBlock(BlockBehaviour.Properties.of()
                    .pushReaction(PushReaction.BLOCK)
                    .strength(2.5f)
                    .sound(SoundType.COPPER)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final DeferredBlock<DrillPylonProxyBlock> DRILL_PYLON_PROXY = BLOCKS.register("drill_pylon_proxy",
            () -> new DrillPylonProxyBlock(BlockBehaviour.Properties.of()
                    .pushReaction(PushReaction.BLOCK)
                    .strength(2.5f)
                    .sound(SoundType.COPPER)
                    .noOcclusion()
                    .noLootTable()));

    public static final DeferredBlock<DrillPylonKineticProxyBlock> DRILL_PYLON_KINETIC_PROXY = BLOCKS.register("drill_pylon_kinetic_proxy",
            () -> new DrillPylonKineticProxyBlock(BlockBehaviour.Properties.of()
                    .pushReaction(PushReaction.BLOCK)
                    .strength(2.5f)
                    .sound(SoundType.COPPER)
                    .noOcclusion()
                    .noLootTable()));

    public static final DeferredBlock<Block> HEAVY_PLATE_SHIELDING = BLOCKS.register("heavy_plate_shielding",
            () -> new Block(BlockBehaviour.Properties.of()
                    .pushReaction(PushReaction.BLOCK)
                    .strength(4.0f, 8.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()));

    public static final DeferredBlock<CyclotronControllerBlock> CYCLOTRON_CONTROLLER = BLOCKS.register("cyclotron_controller",
            () -> new CyclotronControllerBlock(BlockBehaviour.Properties.of()
                    .pushReaction(PushReaction.BLOCK)
                    .strength(2.5f)
                    .sound(SoundType.COPPER)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final DeferredBlock<CyclotronProxyBlock> CYCLOTRON_PROXY = BLOCKS.register("cyclotron_proxy",
            () -> new CyclotronProxyBlock(BlockBehaviour.Properties.of()
                    .pushReaction(PushReaction.BLOCK)
                    .strength(2.5f)
                    .sound(SoundType.COPPER)
                    .noOcclusion()
                    .noLootTable()));

    public static final DeferredBlock<CyclotronKineticProxyBlock> CYCLOTRON_KINETIC_PROXY = BLOCKS.register("cyclotron_kinetic_proxy",
            () -> new CyclotronKineticProxyBlock(BlockBehaviour.Properties.of()
                    .pushReaction(PushReaction.BLOCK)
                    .strength(2.5f)
                    .sound(SoundType.COPPER)
                    .noOcclusion()
                    .noLootTable()));


    // -------------------------------------------------------------------------
    // GLARE Blocks
    // -------------------------------------------------------------------------

    public static final DeferredBlock<GlareNodeBlock> GLARE_RELAY = BLOCKS.register("glare_relay",
            () -> new GlareNodeBlock(BlockBehaviour.Properties.of()
                    .strength(2.5f)
                    .sound(SoundType.COPPER)
                    .requiresCorrectToolForDrops()
                    .noOcclusion(), GlareRelayBlockEntity::new));

    public static final DeferredBlock<GlareEmitterDishBlock> GLARE_EMITTER_DISH = BLOCKS.register("glare_emitter_dish",
            () -> new GlareEmitterDishBlock(BlockBehaviour.Properties.of()
                    .strength(2.5f)
                    .sound(SoundType.COPPER)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final DeferredBlock<GlareNodeBlock> GLARE_KINETIC_RECEIVER = BLOCKS.register("glare_kinetic_receiver",
            () -> new GlareNodeBlock(BlockBehaviour.Properties.of()
                    .strength(2.5f)
                    .sound(SoundType.COPPER)
                    .requiresCorrectToolForDrops()
                    .noOcclusion(), GlareKineticReceiverBlockEntity::new));

    public static final DeferredBlock<GlareChromaticTransceiverBlock> GLARE_CHROMATIC_TRANSCEIVER = BLOCKS.register("glare_chromatic_transceiver",
            () -> new GlareChromaticTransceiverBlock(BlockBehaviour.Properties.of()
                    .strength(2.5f)
                    .sound(SoundType.COPPER)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final DeferredBlock<TelemetryTerminalBlock> GLARE_TELEMETRY_TERMINAL = BLOCKS.register("glare_telemetry_terminal",
            () -> new TelemetryTerminalBlock(BlockBehaviour.Properties.of()
                    .strength(2.5f)
                    .sound(SoundType.COPPER)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final DeferredBlock<LuxTransceiverBlock> LUX_TRANSCEIVER = BLOCKS.register("lux_transceiver",
            () -> new LuxTransceiverBlock(BlockBehaviour.Properties.of()
                    .strength(2.5f)
                    .sound(SoundType.COPPER)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final DeferredBlock<RemoteEntanglerDepotBlock> REMOTE_ENTANGLER_DEPOT = BLOCKS.register("remote_entangler_depot",
            () -> new RemoteEntanglerDepotBlock(BlockBehaviour.Properties.ofFullCopy(AllBlocks.DEPOT.get())
                    .requiresCorrectToolForDrops().noOcclusion()));

    public static final DeferredBlock<RemoteEntanglementTransporterBlock> REMOTE_ENTANGLEMENT_TRANSPORTER = BLOCKS.register(
            "remote_entanglement_transporter", () -> new RemoteEntanglementTransporterBlock(BlockBehaviour.Properties.of()
                    .strength(2.5F).sound(SoundType.COPPER).requiresCorrectToolForDrops().noOcclusion()));

    public static final DeferredBlock<RemoteTransporterProxyBlock> REMOTE_ENTANGLEMENT_TRANSPORTER_TANK = BLOCKS.register(
            "remote_entanglement_transporter_tank", () -> new RemoteTransporterProxyBlock(BlockBehaviour.Properties.of()
                    .strength(2.5F).sound(SoundType.COPPER).noOcclusion().noLootTable(), true));

    public static final DeferredBlock<RemoteTransporterProxyBlock> REMOTE_ENTANGLEMENT_TRANSPORTER_CASING = BLOCKS.register(
            "remote_entanglement_transporter_casing", () -> new RemoteTransporterProxyBlock(BlockBehaviour.Properties.of()
                    .strength(2.5F).sound(SoundType.GLASS).noOcclusion().noLootTable(), false));

    public static final DeferredBlock<LaunchpadControllerBlock> LAUNCHPAD_CONTROLLER = BLOCKS.register(
            "launchpad_controller", () -> new LaunchpadControllerBlock(BlockBehaviour.Properties.of()
                    .pushReaction(PushReaction.BLOCK).strength(2.5F).sound(SoundType.COPPER)
                    .requiresCorrectToolForDrops().noOcclusion()));

    public static final DeferredBlock<LaunchpadProxyBlock> LAUNCHPAD_PROXY = BLOCKS.register(
            "launchpad_proxy", () -> new LaunchpadProxyBlock(BlockBehaviour.Properties.of()
                    .pushReaction(PushReaction.BLOCK).strength(2.5F).sound(SoundType.COPPER)
                    .noOcclusion().noLootTable()));

    public static final DeferredBlock<ResonanceCrystalBlock> RESONANCE_CRYSTAL = BLOCKS.register("resonance_crystal",
            () -> new ResonanceCrystalBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_BLOCK)
                    .strength(2.0f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion(), false));

    public static final DeferredBlock<ResonanceCrystalBlock> ARTIFICIAL_RESONANCE_CRYSTAL = BLOCKS.register("artificial_resonance_crystal",
            () -> new ResonanceCrystalBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_BLOCK)
                    .strength(2.0f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion(), true));


    // -------------------------------------------------------------------------
    // Fluid Gel Blocks
    // -------------------------------------------------------------------------

    public static final DeferredBlock<GelSplatterBlock> GEL_SPLATTER = BLOCKS.register("gel_splatter",
            () -> new GelSplatterBlock(BlockBehaviour.Properties.of()
                    .pushReaction(PushReaction.DESTROY)
                    .noCollission()
                    .replaceable()
                    .strength(0.25f)
                    .sound(SoundType.SLIME_BLOCK)
                    .noOcclusion()
                    .noLootTable(),
                    false));

    public static final DeferredBlock<GelSplatterBlock> GEL_SPLATTER_SLIPPERY = BLOCKS.register("gel_splatter_slippery",
            () -> new GelSplatterBlock(BlockBehaviour.Properties.of()
                    .friction(0.9f)
                    .speedFactor(1f)
                    .pushReaction(PushReaction.DESTROY)
                    .noCollission()
                    .replaceable()
                    .strength(0.25f)
                    .sound(SoundType.SLIME_BLOCK)
                    .noOcclusion()
                    .noLootTable(),
                    false));

    public static final DeferredBlock<GelSplatterBlock> GEL_SPLATTER_STICKY = BLOCKS.register("gel_splatter_sticky",
            () -> new GelSplatterBlock(BlockBehaviour.Properties.of()
                    .jumpFactor(0.35f)
                    .speedFactor(0.6f)
                    .pushReaction(PushReaction.DESTROY)
                    .noCollission()
                    .replaceable()
                    .strength(0.25f)
                    .sound(SoundType.HONEY_BLOCK)
                    .noOcclusion()
                    .noLootTable(),
                    false));

    public static final DeferredBlock<GelSplatterBlock> GEL_SPLATTER_MOLTEN = BLOCKS.register("gel_splatter_molten",
            () -> new GelSplatterBlock(BlockBehaviour.Properties.of()
                    .jumpFactor(0.9f)
                    .speedFactor(0.75f)
                    .pushReaction(PushReaction.DESTROY)
                    .noCollission()
                    .replaceable()
                    .strength(0.25f)
                    .sound(SoundType.NETHERRACK)
                    .noOcclusion()
                    .noLootTable(),
                    true));

    public static final DeferredBlock<GelSplatterBlock> GEL_SPLATTER_BOUNCY = BLOCKS.register("gel_splatter_bouncy",
            () -> new GelSplatterBlock(BlockBehaviour.Properties.of()
                    .jumpFactor(1f)
                    .pushReaction(PushReaction.DESTROY)
                    .noCollission()
                    .replaceable()
                    .strength(0.25f)
                    .sound(SoundType.SLIME_BLOCK)
                    .noOcclusion()
                    .noLootTable(),
                    false));


    // -------------------------------------------------------------------------
    // Fluid Blocks (Handled by FluidEntry)
    // -------------------------------------------------------------------------
}
