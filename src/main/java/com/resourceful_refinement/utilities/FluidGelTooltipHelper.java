package com.resourceful_refinement.utilities;

import com.resourceful_refinement.content.gel_splatter.GelPropertiesManager;
import com.resourceful_refinement.content.gel_splatter.GelType;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;
import java.util.Locale;

/** Shared fluid + gel-type lines for goggle overlays and item tooltips. */
public final class FluidGelTooltipHelper {

    private FluidGelTooltipHelper() {}

    public static void addGoggleFluidLines(List<Component> tooltip, FluidStack fluid, boolean isTank) {
        if (fluid.isEmpty()) {
            return;
        }

        GelType gelType = GelPropertiesManager.getGelType(fluid.getFluid());
        if (isTank)
        {
            tooltip.add(Component.literal("§6Tank: §7" + fluid.getAmount() + "mb"));
            tooltip.add(Component.literal("§7" + fluid.getHoverName().getString() +" §9[" + formatGelType(gelType) + "]"));
        }
        else
        {
            tooltip.add(Component.literal("§7" + fluid.getHoverName().getString() + " §9[" + formatGelType(gelType) + "]"));
            tooltip.add(Component.literal("§7" + fluid.getAmount() + "mb"));
        }
    }

    public static void addItemGelFluidLines(List<Component> tooltip, FluidStack fluid, int capacity, int color, boolean isGloopy) {
        if (fluid.isEmpty()) {
            return;
        }

        GelType gelType = GelPropertiesManager.getGelType(fluid.getFluid());
        if (isGloopy)
            tooltip.add(Component.literal(fluid.getAmount() + "/" + capacity + " mb").withColor(color).append(" §9[Gloop " + formatGelType(gelType) + "]"));
        else
            tooltip.add(Component.literal(fluid.getAmount() + "/" + capacity + " mb").withColor(color).append(" §9[" + formatGelType(gelType) + "]"));
    }

    public static void addItemFluidLines(List<Component> tooltip, FluidStack fluid, int capacity, int color) {
        if (fluid.isEmpty()) {
            return;
        }

        tooltip.add(Component.literal(fluid.getAmount() + "/" + capacity + " mb").withColor(color).append(" §7" + fluid.getHoverName().getString()));
    }

    private static String formatGelType(GelType gelType) {
        String id = gelType.getId();
        if (id.isEmpty()) {
            return id;
        }
        return id.substring(0, 1).toUpperCase(Locale.ROOT) + id.substring(1);
    }
}
