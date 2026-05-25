package com.resourceful_refinement.registry;

import com.resourceful_refinement.ResourcefulRefinementMain;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ResourcefulRefinementMain.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB = CREATIVE_MODE_TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.resourceful_refinement.main"))
            .icon(() -> new ItemStack(ModItems.MECHANICAL_SIEVE_ITEM.get()))
            .displayItems((parameters, output) -> {

                // Add all registered items automatically
                for (DeferredHolder<net.minecraft.world.item.Item, ? extends net.minecraft.world.item.Item> item : ModItems.ITEMS.getEntries()) {
                    output.accept(item.get());
                }
            }).build());
}
