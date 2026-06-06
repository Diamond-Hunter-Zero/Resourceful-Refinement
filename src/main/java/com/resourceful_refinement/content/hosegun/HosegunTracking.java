package com.resourceful_refinement.content.hosegun;

import com.resourceful_refinement.content.refill_station.FluidRefillStationBlockEntity;
import com.resourceful_refinement.registry.ModDataComponents;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/** Gel-tracking ID stored on a {@link HosegunItem} stack (Phase 4+ gel network). */
public final class HosegunTracking {

    private HosegunTracking() {}

    public static Optional<String> getTrackingId(ItemStack stack) {
        @Nullable String id = stack.get(ModDataComponents.HOSEGUN_TRACKING_ID);
        if (id == null || id.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(id);
    }

    public static boolean isBoundTo(ItemStack stack, String stationTrackingId) {
        return getTrackingId(stack).filter(id -> id.equals(stationTrackingId)).isPresent();
    }

    public static void setTrackingId(ItemStack stack, String trackingId) {
        String sanitised = FluidRefillStationBlockEntity.sanitiseTrackingId(trackingId);
        if (sanitised.isEmpty()) {
            clearTrackingId(stack);
        } else {
            stack.set(ModDataComponents.HOSEGUN_TRACKING_ID, sanitised);
        }
    }

    public static void clearTrackingId(ItemStack stack) {
        stack.remove(ModDataComponents.HOSEGUN_TRACKING_ID);
    }
}
