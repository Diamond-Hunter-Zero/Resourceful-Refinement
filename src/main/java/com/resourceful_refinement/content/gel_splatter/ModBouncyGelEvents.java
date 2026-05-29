package com.resourceful_refinement.content.gel_splatter;

import com.resourceful_refinement.ResourcefulRefinementMain;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = ResourcefulRefinementMain.MOD_ID)
public final class ModBouncyGelEvents {

    private ModBouncyGelEvents() {}

    /** Snapshot velocity at tick start — before move() collision scrubs horizontal speed on landing. */
    @SubscribeEvent
    public static void onEntityTickPre(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }
        if (entity.level().isClientSide()) {
            return;
        }
        var velocity = entity.getDeltaMovement();
        if (!entity.onGround() || velocity.horizontalDistanceSqr() > 2.5E-3D) {
            BouncyGelMomentumHelper.trackAirborne(entity, velocity);
        }
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        BouncyGelMomentumHelper.clear(event.getEntity());
    }
}
