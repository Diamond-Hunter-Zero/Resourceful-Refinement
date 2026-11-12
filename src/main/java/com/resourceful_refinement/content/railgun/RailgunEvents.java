package com.resourceful_refinement.content.railgun;

import com.resourceful_refinement.ResourcefulRefinementMain;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Server-side railgun upkeep: ticks live beams and drops them when their shooter leaves. */
@EventBusSubscriber(modid = ResourcefulRefinementMain.MOD_ID)
public final class RailgunEvents {

    private RailgunEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            RailgunFiring.tick(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        RailgunFiring.forget(event.getEntity().getUUID());
    }
}
