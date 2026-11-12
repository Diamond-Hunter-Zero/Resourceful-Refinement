package com.resourceful_refinement.content.railgun;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.registry.ModItems;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(modid = ResourcefulRefinementMain.MOD_ID, value = Dist.CLIENT)
public final class RailgunClientEvents {

    private RailgunClientEvents() {}

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(RailgunItemRenderer.LAYER, RailgunModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(RailgunClientExtensions.INSTANCE, ModItems.RAILGUN.get());
    }

    /** Lowest priority so nothing can cancel the render after we've pushed (which would skip the matching Post). */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        RailgunClient.pushRendered(event.getEntity());
    }

    @SubscribeEvent
    public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
        RailgunClient.popRendered(event.getEntity());
    }

    @SubscribeEvent
    public static void onRenderFrame(RenderFrameEvent.Pre event) {
        RailgunClient.beginFrame();
    }

    /** Final FOVs (after other mods), used to line the first-person beam up with the on-screen barrel. */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        RailgunClient.captureFov(event.getFOV(), event.usedConfiguredFov());
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        RailgunClient.clientTick();
    }
}
