package com.resourceful_refinement.registry;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.casting_depot.CastingDepotBlock;
import com.resourceful_refinement.content.forge_mould.MechanicalForgeMouldBlock;
import com.resourceful_refinement.content.fracking_pump.FrackingPumpOutletBlock;
import com.resourceful_refinement.content.fracking_pump.FrackingPumpProxyBlock;
import com.resourceful_refinement.content.gel_splatter.GelSplatterBlock;
import com.resourceful_refinement.content.paint_nozzle.PaintNozzleBlock;
import com.resourceful_refinement.content.plushie.PlushieBlock;
import com.resourceful_refinement.content.refill_station.FluidRefillStationBlock;
import com.resourceful_refinement.content.refinery.BlenderBladeBlock;
import com.resourceful_refinement.content.refinery.RefineryAccessPortBlock;
import com.resourceful_refinement.content.refinery.RefineryProxyBlock;
import com.resourceful_refinement.content.refinery.RefineryKineticProxyBlock;
import com.resourceful_refinement.content.sieve.MechanicalFluidSieveBlock;
import com.resourceful_refinement.content.geyser.GeyserBlock;
import com.simibubi.create.AllBlocks;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ResourcefulRefinementMain.MOD_ID);

    // -------------------------------------------------------------------------
    // Mechanical Blocks
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

    public static final DeferredBlock<CastingDepotBlock> CASTING_DEPOT = BLOCKS.register("casting_depot",
            () -> new CastingDepotBlock(BlockBehaviour.Properties.ofFullCopy(AllBlocks.DEPOT.get()).requiresCorrectToolForDrops().noOcclusion()));

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
