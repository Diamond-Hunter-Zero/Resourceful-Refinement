package com.resourceful_refinement.content.refill_station;

import com.resourceful_refinement.content.gel_tracking.GelTrackingService;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.SingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;

public class FluidRefillStationDisplaySource extends SingleLineDisplaySource {

    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        BlockEntity blockEntity = context.getSourceBlockEntity();
        if (!(blockEntity instanceof FluidRefillStationBlockEntity station)) {
            return EMPTY_LINE;
        }

        if (station.hasTrackingId()) {
            int gelCount = GelTrackingService.getGelCountForStation(station);
            return Component.literal(station.getTrackingId() + ": " + gelCount);
        }

        FluidStack fluid = station.tank.getFluid();
        if (fluid.isEmpty()) {
            return Component.literal("empty");
        }

        return Component.literal(fluid.getAmount() + "mb ")
                .append(fluid.getHoverName());
    }

    @Override
    protected String getTranslationKey() {
        return "fluid_refill_station";
    }

    @Override
    protected boolean allowsLabeling(DisplayLinkContext context) {
        return false;
    }
}
