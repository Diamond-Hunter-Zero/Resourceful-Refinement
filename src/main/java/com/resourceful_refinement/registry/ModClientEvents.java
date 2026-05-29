package com.resourceful_refinement.registry;

import com.resourceful_refinement.content.gel_splatter.GelFluidTintColors;
import com.resourceful_refinement.content.gel_splatter.GelFluidTintColorsClient;
import com.resourceful_refinement.content.gel_splatter.GelSplatterBlock;
import com.resourceful_refinement.content.gel_splatter.GelSplatterBlockEntity;
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
            GelFluidTintColorsClient.install();
            GelFluidTintColors.clearCompatCache();
            // Force blocks to use the Translucent texture blend pass
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.GEL_SPLATTER.get(), RenderType.solid());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.GEL_SPLATTER_SLIPPERY.get(), RenderType.solid());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.GEL_SPLATTER_STICKY.get(), RenderType.solid());
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
        event.getBlockColors().register(GelSplatterBlock::RegisterRendererTint, ModBlocks.GEL_SPLATTER.get());
        event.getBlockColors().register(GelSplatterBlock::RegisterRendererTint, ModBlocks.GEL_SPLATTER_STICKY.get());
        event.getBlockColors().register(GelSplatterBlock::RegisterRendererTint, ModBlocks.GEL_SPLATTER_SLIPPERY.get());
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        int defaultTint = GelFluidTintColors.getGelTint(GelSplatterBlockEntity.getDefaultFluid());
        event.getItemColors().register(
                (stack, tintIndex) -> tintIndex == 0 ? defaultTint : 0xFFFFFFFF,
                ModItems.GEL_SPLATTER_ITEM.get(),
                ModItems.GEL_SPLATTER_STICKY_ITEM.get(),
                ModItems.GEL_SPLATTER_SLIPPERY_ITEM.get()
        );
    }
}
