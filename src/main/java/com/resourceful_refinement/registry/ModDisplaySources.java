package com.resourceful_refinement.registry;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.refill_station.FluidRefillStationDisplaySource;
import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.api.registry.CreateRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDisplaySources {

    public static final DeferredRegister<DisplaySource> REGISTER =
            DeferredRegister.create(CreateRegistries.DISPLAY_SOURCE, ResourcefulRefinementMain.MOD_ID);

    public static final DeferredHolder<DisplaySource, FluidRefillStationDisplaySource> FLUID_REFILL_STATION =
            REGISTER.register("fluid_refill_station", FluidRefillStationDisplaySource::new);

    public static void init(IEventBus modEventBus) {
        REGISTER.register(modEventBus);
        modEventBus.addListener(ModDisplaySources::commonSetup);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> DisplaySource.BY_BLOCK_ENTITY.add(
                ModBlockEntities.FLUID_REFILL_STATION_BE.get(),
                FLUID_REFILL_STATION.get()));
    }
}
