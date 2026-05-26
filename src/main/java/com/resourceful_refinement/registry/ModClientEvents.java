package com.resourceful_refinement.registry;

import com.resourceful_refinement.ponders.ModPonders;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.fluids.FluidType;

import static com.resourceful_refinement.ResourcefulRefinementMain.MOD_ID;

@EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModClientEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Force blocks to use the Translucent texture blend pass
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.GEL_SPLATTER.get(), RenderType.solid());
        });
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        // This loads client extensions or renderers
        PonderIndex.addPlugin(new ModPonders());
    }

    @SubscribeEvent
    public static void registerItemDecorators(net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent event) {
        com.resourceful_refinement.content.coating.CoatingItemDecorator decorator = new com.resourceful_refinement.content.coating.CoatingItemDecorator();
        for (net.minecraft.world.item.Item item : net.minecraft.core.registries.BuiltInRegistries.ITEM) {
            if (item instanceof net.minecraft.world.item.DiggerItem || item instanceof net.minecraft.world.item.SwordItem || item instanceof net.minecraft.world.item.TridentItem) {
                event.register(item, decorator);
            }
        }
    }

    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        Camera camera = event.getCamera();
        FluidState fluidState = camera.getEntity().level().getFluidState(camera.getBlockPosition());
        FluidType currentType = fluidState.getFluidType();

        // Check if the current fluid matches any type registered in your custom entries
        boolean isCustomFluid = ModFluids.ENTRIES.stream()
                .anyMatch(entry -> entry.getFluidType() == currentType);

        // Check if the camera is inside your custom fluid type
        if (isCustomFluid) {
            ModFluids.ENTRIES.stream()
                    .filter(entry -> entry.getFluidType() == currentType)
                    .findFirst()
                    .ifPresent(entry -> {
                        int color = entry.color; // e.g., 0xCC3333
                        event.setRed(((color >> 16) & 0xFF) / 255.0f);
                        event.setGreen(((color >> 8) & 0xFF) / 255.0f);
                        event.setBlue((color & 0xFF) / 255.0f);
                    });
        }
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        Camera camera = event.getCamera();
        FluidState fluidState = camera.getEntity().level().getFluidState(camera.getBlockPosition());
        FluidType currentType = fluidState.getFluidType();

        boolean isCustomFluid = ModFluids.ENTRIES.stream()
                .anyMatch(entry -> entry.getFluidType() == currentType);

        if (isCustomFluid) {
            // Start rendering fog right in front of the camera eyes
            event.setNearPlaneDistance(0.0F);
            // End plane is how far the player can see. Lower = thicker fog (Lava is ~2.0F, Water is ~60.0F)
            event.setFarPlaneDistance(1.75F);

            // Confirms the event should override default vanilla sky fog rendering passes
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.getBlockColors().register((state, level, pos, tintIndex) -> {
            if (level != null && pos != null) {
                net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof com.resourceful_refinement.content.gel_splatter.GelSplatterBlockEntity splatterBe) {
                    net.minecraft.world.level.material.Fluid fluid = splatterBe.getFluid();
                    if (fluid != net.minecraft.world.level.material.Fluids.EMPTY) {
                        // Resolve color from custom fluid registry or fallback to custom logic
                        for (com.resourceful_refinement.registry.FluidEntry entry : ModFluids.ENTRIES) {
                            if (entry.source.get() == fluid) {
                                return entry.color;
                            }
                        }
                        // Fallback to standard color maps for water/lava or general dye tints
                        if (fluid.isSame(net.minecraft.world.level.material.Fluids.WATER) || fluid.isSame(net.minecraft.world.level.material.Fluids.FLOWING_WATER)) {
                            return 0x3F76E4;
                        }
                        if (fluid.isSame(net.minecraft.world.level.material.Fluids.LAVA) || fluid.isSame(net.minecraft.world.level.material.Fluids.FLOWING_LAVA)) {
                            return 0xFF4500;
                        }
                    }
                }
            }
            return 0xFFFFFFFF; // Fallback
        }, ModBlocks.GEL_SPLATTER.get());
    }
}
