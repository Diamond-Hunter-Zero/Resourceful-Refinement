package com.resourceful_refinement.data;

import com.resourceful_refinement.ResourcefulRefinementMain;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = ResourcefulRefinementMain.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModDataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        // Register the loot table builder pass
        generator.addProvider(event.includeServer(), new LootTableProvider(
                packOutput,
                Set.of(), // No explicit vanilla overrides needed
                List.of(new LootTableProvider.SubProviderEntry(
                        ModBlockLootProvider::new,
                        LootContextParamSets.BLOCK
                )),
                lookupProvider
        ));
    }
}