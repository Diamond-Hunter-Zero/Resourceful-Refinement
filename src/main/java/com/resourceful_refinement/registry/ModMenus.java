package com.resourceful_refinement.registry;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.refill_station.FluidRefillStationMenu;
import com.resourceful_refinement.content.glare.GlareChromaticTransceiverMenu;
import com.resourceful_refinement.content.glare.terminal.TelemetryTerminalMenu;
import com.resourceful_refinement.content.pug.LaunchpadMenu;
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

    public static final DeferredHolder<MenuType<?>, MenuType<GlareChromaticTransceiverMenu>> GLARE_CHROMATIC_TRANSCEIVER =
            MENUS.register("glare_chromatic_transceiver",
                    () -> IMenuTypeExtension.create(GlareChromaticTransceiverMenu::fromNetwork));

    public static final DeferredHolder<MenuType<?>, MenuType<TelemetryTerminalMenu>> GLARE_TELEMETRY_TERMINAL =
            MENUS.register("glare_telemetry_terminal", () -> IMenuTypeExtension.create(TelemetryTerminalMenu::fromNetwork));

    public static final DeferredHolder<MenuType<?>, MenuType<LaunchpadMenu>> LAUNCHPAD =
            MENUS.register("launchpad", () -> IMenuTypeExtension.create(LaunchpadMenu::fromNetwork));
}
