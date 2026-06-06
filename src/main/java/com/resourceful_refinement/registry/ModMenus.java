package com.resourceful_refinement.registry;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.refill_station.FluidRefillStationMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, ResourcefulRefinementMain.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<FluidRefillStationMenu>> FLUID_REFILL_STATION =
            MENUS.register("fluid_refill_station",
                    () -> IMenuTypeExtension.create(FluidRefillStationMenu::fromNetwork));
}
