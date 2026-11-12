package com.resourceful_refinement.client.fx.lightning;

import com.resourceful_refinement.ResourcefulRefinementMain;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderBuffersEvent;

/**
 * Gives {@link LightningRenderType#LIGHTNING} its own fixed buffer in the level's buffer source. Without one it shares
 * the single "everything else" buffer, which is flushed (one draw call) every time a renderer switches render type;
 * a block entity renderer drawing its model then an arc, for many block entities, would issue a draw per arc.
 * With a fixed buffer, all lightning from every block entity accumulates and draws once when the level flushes.
 */
@EventBusSubscriber(modid = ResourcefulRefinementMain.MOD_ID, value = Dist.CLIENT)
public final class LightningClientSetup {

    private LightningClientSetup() {}

    @SubscribeEvent
    public static void onRegisterRenderBuffers(RegisterRenderBuffersEvent event) {
        event.registerRenderBuffer(LightningRenderType.LIGHTNING);
    }
}
