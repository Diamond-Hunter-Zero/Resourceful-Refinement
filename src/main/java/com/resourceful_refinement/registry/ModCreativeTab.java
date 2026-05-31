package com.resourceful_refinement.registry;

import com.resourceful_refinement.ResourcefulRefinementMain;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ResourcefulRefinementMain.MOD_ID);

    public static boolean isFluidBucketItem(Item item) {
        for (FluidEntry entry : ModFluids.ENTRIES) {
            if (entry.bucket.get() == item) {
                return true;
            }
        }
        return false;
    }

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB = CREATIVE_MODE_TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.resourceful_refinement.main"))
            .icon(() -> new ItemStack(ModItems.MECHANICAL_SIEVE_ITEM.get()))
            .displayItems((parameters, output) -> {
                for (DeferredHolder<Item, ? extends Item> item : ModItems.ITEMS.getEntries()) {
                    if (!isFluidBucketItem(item.get())) {
                        output.accept(item.get());
                    }
                }
            }).build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> FLUIDS_TAB = CREATIVE_MODE_TABS.register("fluids", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.resourceful_refinement.fluids"))
            .icon(() -> new ItemStack(ModFluids.RED_PAINT.bucket.get()))
            .displayItems((parameters, output) -> {
                for (FluidEntry entry : ModFluids.ENTRIES) {
                    output.accept(entry.bucket.get());
                }
            }).build());
}
