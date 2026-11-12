package com.resourceful_refinement.content.railgun;

import com.resourceful_refinement.config.ServerConfig;
import com.resourceful_refinement.content.prism_cell.PrismCell;
import com.resourceful_refinement.network.RailgunFirePayload;
import com.resourceful_refinement.registry.ModDamageTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server side of a railgun shot: spends the charge, then keeps the beam alive for {@link RailgunItem#FIRE_TICKS},
 * re-tracing it along the shooter's current reticle and damaging everything on it every
 * {@link RailgunItem#DAMAGE_INTERVAL_TICKS}. Clients draw the beam themselves from a {@link RailgunFirePayload},
 * tracing the same line locally so it follows the reticle smoothly. Server thread only.
 */
public final class RailgunFiring {

    private record ActiveBeam(Level level, InteractionHand hand, long startTick, double range) {}

    private static final Map<UUID, ActiveBeam> ACTIVE = new HashMap<>();

    private RailgunFiring() {}

    /** Called when a charge completes. Spends the cell's charge and starts the beam, or fizzles if it can't. */
    public static void fire(ServerPlayer player, ItemStack stack, InteractionHand hand) {
        if (!(stack.getItem() instanceof RailgunItem railgun)) {
            return;
        }
        ServerLevel level = player.serverLevel();
        int cost = ServerConfig.RAILGUN_CHARGE_COST.get();
        PrismCell cell = railgun.getPrismCell(stack);
        if (cell == null || !cell.hasCharge(cost)) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.6F, 1.6F);
            return;
        }
        // Same policy as the other cell consumers: a shot that empties a cell uses it up.
        cell.drainCellCharge(cost, true);
        railgun.setPrismCell(stack, cell.isDepleted() ? null : cell);

        double range = ServerConfig.RAILGUN_RANGE.get();
        ActiveBeam beam = new ActiveBeam(level, hand, level.getGameTime(), range);
        ACTIVE.put(player.getUUID(), beam);
        damagePulse(player, beam);
        // No charging during the beam. The cooldown syncs to the client, which blocks use() there too; with use
        // still held, vanilla retries every few ticks, so charging resumes on its own once the beam ends.
        player.getCooldowns().addCooldown(railgun, RailgunItem.FIRE_TICKS);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 1.0F, 1.5F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.FIREWORK_ROCKET_LARGE_BLAST, SoundSource.PLAYERS, 0.7F, 0.7F);
        player.awardStat(Stats.ITEM_USED.get(railgun));

        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                new RailgunFirePayload(player.getId(), hand == InteractionHand.MAIN_HAND, (float) range));
    }

    /** Per-tick upkeep for a player's live beam (from the player tick, after any shot fired this tick). */
    public static void tick(ServerPlayer player) {
        ActiveBeam beam = ACTIVE.get(player.getUUID());
        if (beam == null) {
            return;
        }
        long age = beam.level().getGameTime() - beam.startTick();
        if (age < 0 || age >= RailgunItem.FIRE_TICKS || !player.isAlive() || player.level() != beam.level()
                || !(player.getItemInHand(beam.hand()).getItem() instanceof RailgunItem)) {
            ACTIVE.remove(player.getUUID());
            return;
        }
        // Age 0 is the shot itself, already pulsed in fire().
        if (age > 0 && age % RailgunItem.DAMAGE_INTERVAL_TICKS == 0) {
            damagePulse(player, beam);
        }
    }

    public static void forget(UUID playerId) {
        ACTIVE.remove(playerId);
    }

    private static void damagePulse(ServerPlayer player, ActiveBeam beam) {
        ServerLevel level = player.serverLevel();
        Vec3 start = RailgunTargeting.muzzle(player, beam.hand(), 1.0F);
        Vec3 end = RailgunTargeting.beamEnd(player, beam.range(), 1.0F);
        float damage = ServerConfig.RAILGUN_DAMAGE.get().floatValue();
        for (LivingEntity target : RailgunTargeting.entitiesOnBeam(player, start, end)) {
            // Pulses are 5 ticks apart, inside the 10-tick hurt cooldown; clear it so every pulse lands.
            target.invulnerableTime = 0;
            if (target.hurt(ModDamageTypes.railgun(level, player), damage)) {
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY(0.5D), target.getZ(),
                        8, target.getBbWidth() * 0.4D, target.getBbHeight() * 0.3D, target.getBbWidth() * 0.4D, 0.15D);
            }
        }
    }
}
