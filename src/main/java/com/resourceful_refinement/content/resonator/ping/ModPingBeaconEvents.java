package com.resourceful_refinement.content.resonator.ping;

import com.resourceful_refinement.ResourcefulRefinementMain;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Server-side game-bus hooks for the Ping Beacon: expiry each tick, and syncing the current beacon to joining players.
 */
@EventBusSubscriber(modid = ResourcefulRefinementMain.MOD_ID)
public final class ModPingBeaconEvents {

    private ModPingBeaconEvents() {}

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        PingBeaconManager.tick(event.getServer());
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PingBeaconManager.syncTo(player);
        }
    }
}
