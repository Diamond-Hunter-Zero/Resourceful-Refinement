package com.resourceful_refinement.content.hoverpack;

import com.resourceful_refinement.config.ServerConfig;
import com.resourceful_refinement.content.prism_cell.PrismCell;
import com.resourceful_refinement.content.prism_cell.PrismCellBatteryComponent;
import com.resourceful_refinement.content.prism_cell.PrismCellTier;
import com.resourceful_refinement.registry.ModDataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Server-side logic for a Hoverpack launch (triggered by the Launch key, relayed via
 * {@link com.resourceful_refinement.network.HoverpackLaunchPayload}). Validates that the player wears a charged
 * Hoverpack, applies the launch impulse, and drains the installed Prism Cell.
 */
public final class HoverpackLaunchHandler {

    private HoverpackLaunchHandler() {}

    /**
     * Attempts a launch for the given player. No-op (returns silently) if the player is not wearing a Hoverpack, has no
     * installed cell, or the cell lacks sufficient charge.
     */
    public static void tryLaunch(ServerPlayer player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(chest.getItem() instanceof HoverpackItem)) {
            return;
        }
        PrismCellBatteryComponent battery = chest.get(ModDataComponents.PRISM_CELL_BATTERY.get());
        if (battery == null) {
            return;
        }

        int cost = ServerConfig.HOVERPACK_LAUNCH_CHARGE_COST.get();
        PrismCell cell = PrismCell.fromComponent(battery);
        if (!cell.hasCharge(cost)) {
            return;
        }

        double mult = thrustMultiplier(cell.tier());
        applyImpulse(player, mult);

        // Drain the cell; a drain that empties a non-creative cell destroys it.
        cell.drainCellCharge(cost, true);
        if (cell.isDepleted()) {
            chest.remove(ModDataComponents.PRISM_CELL_BATTERY.get());
        } else {
            chest.set(ModDataComponents.PRISM_CELL_BATTERY.get(), cell.toComponent());
        }

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 1.0F, 1.0F);
        player.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(chest.getItem()));
    }

    /** Thrust multiplier by grade: +step cumulatively per grade above Low (Low ×1, Mid ×1.25, High/Creative ×1.5625). */
    private static double thrustMultiplier(PrismCellTier tier) {
        double step = ServerConfig.HOVERPACK_GRADE_THRUST_STEP.get();
        int steps = switch (tier) {
            case LOW -> 0;
            case MID -> 1;
            case HIGH, CREATIVE -> 2;
        };
        return Math.pow(1.0 + step, steps);
    }

    private static void applyImpulse(ServerPlayer player, double mult) {
        Vec3 look = player.getLookAngle();
        Vec3 delta = player.getDeltaMovement();

        if (player.isFallFlying()) {
            // Already gliding: firework-rocket style forward boost (vanilla FireworkRocketEntity formula), grade-scaled.
            double scale = ServerConfig.HOVERPACK_LAUNCH_FORWARD_AIR.get() * mult;
            Vec3 boost = new Vec3(
                    look.x * 0.1 + (look.x * 1.5 - delta.x) * 0.5,
                    look.y * 0.1 + (look.y * 1.5 - delta.y) * 0.5,
                    look.z * 0.1 + (look.z * 1.5 - delta.z) * 0.5
            ).scale(scale);
            player.setDeltaMovement(delta.add(boost));
        } else {
            // From the ground (or mid-air without gliding): strong vertical impulse + minor forward, then auto-glide.
            double vertical = ServerConfig.HOVERPACK_LAUNCH_VERTICAL.get() * mult;
            double forward = ServerConfig.HOVERPACK_LAUNCH_FORWARD_GROUND.get() * mult;
            player.setDeltaMovement(delta.x + look.x * forward, vertical, delta.z + look.z * forward);
            player.startFallFlying();
        }

        player.hasImpulse = true;
        // Force the server to send the new velocity to the client (which is otherwise movement-authoritative).
        player.hurtMarked = true;
    }
}
