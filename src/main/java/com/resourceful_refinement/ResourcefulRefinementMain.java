package com.resourceful_refinement;

import com.mojang.logging.LogUtils;

import com.resourceful_refinement.config.ServerConfig;
import com.resourceful_refinement.content.casting_depot.rendering.CastingDepotLayers;
import com.resourceful_refinement.content.casting_depot.rendering.CastingDepotModel;
import com.resourceful_refinement.content.casting_depot.rendering.CastingDepotRenderer;
import com.resourceful_refinement.content.distillery.DistilleryBlock;
import com.resourceful_refinement.content.distillery.DistilleryBlockEntity;
import com.resourceful_refinement.content.distillery.DistilleryRenderer;
import com.resourceful_refinement.content.fracking_pump.*;
import com.resourceful_refinement.content.plunger.ThrownPlungerRenderer;
import com.resourceful_refinement.content.plushie.PlushieModel;
import com.resourceful_refinement.content.plushie.PlushieRenderer;
import com.resourceful_refinement.content.refinery.rendering.*;
import com.resourceful_refinement.registry.ModBlockEntities;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
import com.resourceful_refinement.registry.*;
import com.resourceful_refinement.content.fluids.base.FluidGroup;
import com.resourceful_refinement.content.gel_splatter.GelPropertiesManager;
import org.slf4j.Logger;

import com.resourceful_refinement.content.refinery.RefineryAccessPortBlockEntity;
import com.resourceful_refinement.content.sieve.MechanicalFluidSieveBlockEntity;
import com.resourceful_refinement.content.forge_mould.*;
import com.resourceful_refinement.content.paint_nozzle.PaintNozzleBlock;
import com.resourceful_refinement.content.refill_station.FluidRefillStationBlock;
import com.resourceful_refinement.content.refill_station.FluidRefillStationLayers;
import com.resourceful_refinement.content.refill_station.FluidRefillStationRenderer;
import com.resourceful_refinement.content.refill_station.FluidRefillStationScreen;
import com.resourceful_refinement.network.ModNetworking;

@Mod(ResourcefulRefinementMain.MOD_ID)
public class ResourcefulRefinementMain {

    public static final String MOD_ID = "resourceful_refinement";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ResourcefulRefinementMain(IEventBus modEventBus, ModContainer modContainer) {

        // Register configs
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);

        // Initialise content
        ModRegistries.init(modEventBus);
        com.resourceful_refinement.worldgen.GeyserOffsetManager.init();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(ModNetworking::registerPayloadHandlers);

        // Register NeoForge event listeners (world load, input)
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.addListener(GelPropertiesManager::onTagsUpdated);

    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ModStressValues::register);
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        // --- Access Port (Controller) ---
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.REFINERY_ACCESS_PORT.get(), (be, side) -> {
            // Front face (where it was placed) is the output
            if (side == be.getBlockState().getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING)) {
                return be.outputTank;
            }
            return null;
        });
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.REFINERY_ACCESS_PORT.get(), (be, side) -> {
            if (side == net.minecraft.core.Direction.DOWN) return be.fuelSlot;
            return null;
        });

        // --- Proxies ---
        // Register for BOTH regular and kinetic proxies
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.REFINERY_PROXY.get(), (be, side) -> {
            RefineryAccessPortBlockEntity controller = be.getController(be.getLevel());
            return controller != null ? controller.getFluidHandlerForProxy(be.getDx(), be.getDy(), be.getDz(), side) : null;
        });
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.REFINERY_PROXY.get(), (be, side) -> {
            RefineryAccessPortBlockEntity controller = be.getController(be.getLevel());
            return controller != null ? controller.getItemHandlerForProxy(be.getDx(), be.getDy(), be.getDz(), side) : null;
        });

        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.REFINERY_KINETIC_PROXY.get(), (be, side) -> {
            RefineryAccessPortBlockEntity controller = be.getController(be.getLevel());
            return controller != null ? controller.getFluidHandlerForProxy(be.getDx(), be.getDy(), be.getDz(), side) : null;
        });
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.REFINERY_KINETIC_PROXY.get(), (be, side) -> {
            RefineryAccessPortBlockEntity controller = be.getController(be.getLevel());
            return controller != null ? controller.getItemHandlerForProxy(be.getDx(), be.getDy(), be.getDz(), side) : null;
        });

        // --- Mechanical Sieve ---
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.MECHANICAL_SIEVE_BE.get(), (be, side) -> {
            MechanicalFluidSieveBlockEntity controller = be.getController();
            if (controller == null) return null;

            // Only top block accepts input from TOP
            if (side == Direction.UP && be.stackIndex == be.stackSize - 1) {
                return controller.inputTank;
            }
            // Only bottom block pushes output from BOTTOM
            if (side == Direction.DOWN && be.stackIndex == 0) {
                return controller.outputTank;
            }
            return null;
        });
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.MECHANICAL_SIEVE_BE.get(), (be, side) -> {
            MechanicalFluidSieveBlockEntity controller = be.getController();
            if (controller == null) return null;

            // Only bottom block provides output from FRONT
            if (be.stackIndex == 0 && be.getBlockState().hasProperty(com.resourceful_refinement.content.sieve.MechanicalFluidSieveBlock.FACING)) {
                if (side == be.getBlockState().getValue(com.resourceful_refinement.content.sieve.MechanicalFluidSieveBlock.FACING)) {
                    return controller.outputInv;
                }
            }
            return null;
        });

        // --- Mechanical Forge Mould ---
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.MECHANICAL_FORGE_MOULD_BE.get(), (be, side) -> {
            if (side == Direction.UP) {
                return be.inputTank;
            }
            return null;
        });
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.MECHANICAL_FORGE_MOULD_BE.get(), (be, side) -> {
            if (be.getBlockState().hasProperty(com.resourceful_refinement.content.forge_mould.MechanicalForgeMouldBlock.FACING)) {
                if (side == be.getBlockState().getValue(com.resourceful_refinement.content.forge_mould.MechanicalForgeMouldBlock.FACING)) {
                    return be.inputInv;
                }
            }
            return null;
        });

        // --- Casting Depot ---
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.CASTING_DEPOT_BE.get(), (be, side) -> be.getItemHandler());

        // --- Fracking Pump ---
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.FRACKING_PUMP_OUTLET_BE.get(), (be, side) -> {
            if (!be.isAssembled()) {
                return null;
            }
            Direction facing = be.getBlockState().getValue(FrackingPumpOutletBlock.FACING);
            if (FrackingPumpOutletBlock.isFluidInputSide(side, facing)) {
                return be.inputTank;
            }
            if (FrackingPumpOutletBlock.isFluidOutputSide(side, facing)) {
                return be.outputTank;
            }
            return null;
        });

        // --- Paint Nozzle ---
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.PAINT_NOZZLE_BE.get(), (be, side) -> {
            if (side == PaintNozzleBlock.getPipeFace(be.getBlockState())) {
                return be.tank;
            }
            return null;
        });

        // --- Fluid Refill Station ---
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.FLUID_REFILL_STATION_BE.get(), (be, side) -> {
            if (!FluidRefillStationBlock.isPipeFace(be.getBlockState(), side)) {
                return null;
            }
            return be.tank;
        });

        // --- Hosegun Item Capability ---
        event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> new com.resourceful_refinement.content.hosegun.HosegunItem.HosegunFluidHandler(stack), ModItems.HOSEGUN.get());

        // --- Distillery ---
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.DISTILLERY_BE.get(), (be, side) -> {
            DistilleryBlockEntity controller = be.getController();
            if (controller == null) return null;

            // Only top block exposes output from TOP
            if (side == Direction.UP && be.stackIndex == be.stackSize - 1) {
                return controller.outputTank;
            }

            // Only bottom block accepts input from non-front sides
            if (!be.getBlockState().hasProperty(DistilleryBlock.FACING)) return null;

            Direction facing = controller.getBlockState().getValue(DistilleryBlock.FACING);
            if ((side != Direction.DOWN && side != Direction.UP && side != facing)
                    && be.stackIndex == 0) {
                return controller.inputTank;
            }
            return null;
        });
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.DISTILLERY_BE.get(), (be, side) -> {
            DistilleryBlockEntity controller = be.getController();
            if (controller == null) return null;

            // Only bottom block accepts input from FRONT
            if (be.stackIndex == 0 && be.getBlockState().hasProperty(DistilleryBlock.FACING)) {
                if (side == be.getBlockState().getValue(DistilleryBlock.FACING)) {
                    return controller.inputInv;
                }
            }
            return null;
        });

        // --- Radiator ---
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.RADIATOR_PIPE_BE.get(),
                (be, direction) -> direction != null ? be.getFluidHandler(direction) : null
        );
    }

    /**
     * Client-side setup
     */
    @EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("[Resourceful Refinement] Client setup ran successfully!");
            event.enqueueWork(() -> {
                for (FluidEntry entry : ModFluids.ENTRIES) {
                    if (entry.group == FluidGroup.RAW
                            || entry.group == FluidGroup.CATALYSED
                            || entry.group == FluidGroup.CARBORAX
                            || entry.group == FluidGroup.PAINT) {
                        ItemBlockRenderTypes.setRenderLayer(entry.block.get(), RenderType.TRANSLUCENT);
                        ItemBlockRenderTypes.setRenderLayer(entry.source.get(), RenderType.TRANSLUCENT);
                        ItemBlockRenderTypes.setRenderLayer(entry.flowing.get(), RenderType.TRANSLUCENT);
                    }
                }
            });
        }

        @SubscribeEvent
        public static void registerMenuScreens(RegisterMenuScreensEvent event) {
            event.register(ModMenus.FLUID_REFILL_STATION.get(), FluidRefillStationScreen::new);
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ModBlockEntities.BLENDER_BLADE.get(), BlenderBladeRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.REFINERY_KINETIC_PROXY.get(), RefineryKineticProxyRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.REFINERY_PROXY.get(), RefineryProxyRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.REFINERY_ACCESS_PORT.get(), FluidRefineryRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.MECHANICAL_SIEVE_BE.get(), com.resourceful_refinement.content.sieve.MechanicalSieveRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.MECHANICAL_FORGE_MOULD_BE.get(), ForgeMouldRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.CASTING_DEPOT_BE.get(), CastingDepotRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.FRACKING_PUMP_OUTLET_BE.get(), FrackingPumpRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.GEYSER_BE.get(), com.resourceful_refinement.content.geyser.GeyserRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.PLUSHIE_BE.get(), com.resourceful_refinement.content.plushie.PlushieRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.FLUID_REFILL_STATION_BE.get(), FluidRefillStationRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.DISTILLERY_BE.get(), DistilleryRenderer::new);

            // Register Projectile Renderer dynamically
            event.registerEntityRenderer(ModEntities.GEL_BLOB.get(), com.resourceful_refinement.content.hosegun.GelBlobEntityRenderer::new);
            event.registerEntityRenderer(ModEntities.THROWN_PLUNGER.get(), ThrownPlungerRenderer::new);
        }

        @SubscribeEvent
        public static void registerItemColors(net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.Item event) {
            for (FluidEntry entry : ModFluids.ENTRIES) {
                event.register((stack, tintIndex) -> tintIndex == 1 ? entry.color : -1, entry.bucket.get());
            }
        }

        @SubscribeEvent
        public static void registerAdditionalModels(net.neoforged.neoforge.client.event.ModelEvent.RegisterAdditional event) {
            event.register(new net.minecraft.client.resources.model.ModelResourceLocation(com.resourceful_refinement.registry.ModPartialModels.SHAFT_X.modelLocation(), "standalone"));
            event.register(new net.minecraft.client.resources.model.ModelResourceLocation(com.resourceful_refinement.registry.ModPartialModels.SHAFT_Z.modelLocation(), "standalone"));
            event.register(new net.minecraft.client.resources.model.ModelResourceLocation(com.resourceful_refinement.registry.ModPartialModels.SHAFT_VERTICAL.modelLocation(), "standalone"));
            event.register(new net.minecraft.client.resources.model.ModelResourceLocation(com.resourceful_refinement.registry.ModPartialModels.GEYSER_CASING.modelLocation(), "standalone"));
            event.register(new net.minecraft.client.resources.model.ModelResourceLocation(ModPartialModels.NETHERRACK_GEYSER_CASING.modelLocation(), "standalone"));
        }

        @SubscribeEvent
        public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
            // Using the createBodyLayer() methods from the classes we prepared
            event.registerLayerDefinition(RefineryLayers.BASE, RefineryBaseModel::createBodyLayer);
            event.registerLayerDefinition(RefineryLayers.MIDDLE, RefineryMiddleModel::createBodyLayer);
            event.registerLayerDefinition(RefineryLayers.TOP, RefineryTopModel::createBodyLayer);
            event.registerLayerDefinition(RefineryLayers.BLENDER, RefineryBlenderModel::createBodyLayer);
            event.registerLayerDefinition(com.resourceful_refinement.content.sieve.MechanicalSieveLayers.CASING, com.resourceful_refinement.content.sieve.MechanicalSieveCasingModel::createBodyLayer);
            event.registerLayerDefinition(com.resourceful_refinement.content.sieve.MechanicalSieveLayers.CASING_BOTTOM, com.resourceful_refinement.content.sieve.MechanicalSieveCasingBottomModel::createBodyLayer);
            event.registerLayerDefinition(com.resourceful_refinement.content.sieve.MechanicalSieveLayers.CASING_MIDDLE, com.resourceful_refinement.content.sieve.MechanicalSieveCasingMiddleModel::createBodyLayer);
            event.registerLayerDefinition(com.resourceful_refinement.content.sieve.MechanicalSieveLayers.CASING_TOP, com.resourceful_refinement.content.sieve.MechanicalSieveCasingTopModel::createBodyLayer);
            event.registerLayerDefinition(com.resourceful_refinement.content.sieve.MechanicalSieveLayers.COG, com.resourceful_refinement.content.sieve.MechanicalSieveCogModel::createBodyLayer);
            event.registerLayerDefinition(ForgeMouldLayers.CASING, ForgeMouldCasingModel::createBodyLayer);
            event.registerLayerDefinition(ForgeMouldLayers.PRESS, ForgeMouldPressModel::createBodyLayer);
            event.registerLayerDefinition(ForgeMouldLayers.TUBE, ForgeMouldTubeModel::createBodyLayer);
            event.registerLayerDefinition(CastingDepotLayers.CASTING_DEPOT, CastingDepotModel::createBodyLayer);
            event.registerLayerDefinition(FrackingPumpLayers.OUTLET, FrackingPumpOutletModel::createBodyLayer);
            event.registerLayerDefinition(FrackingPumpLayers.BASE, FrackingPumpBaseModel::createBodyLayer);
            event.registerLayerDefinition(FrackingPumpLayers.SHAFT, FrackingPumpShaftModel::createBodyLayer);
            event.registerLayerDefinition(FrackingPumpLayers.TOP, FrackingPumpTopModel::createBodyLayer);
            event.registerLayerDefinition(FrackingPumpLayers.COUNTERWEIGHT, FrackingPumpCounterweightModel::createBodyLayer);
            event.registerLayerDefinition(PlushieRenderer.LAYER_LOCATION, PlushieModel::createBodyLayer);
            event.registerLayerDefinition(FluidRefillStationLayers.CASING, FluidRefillStationLayers::createCasingLayer);
        }
    }

    @SubscribeEvent
    public void onLevelLoad(LevelEvent.Load event) {
        /// Classes with a EventBusSubscriber registration need at least one
        /// @SubscribeEvent method to be considered valid.
    }
}
