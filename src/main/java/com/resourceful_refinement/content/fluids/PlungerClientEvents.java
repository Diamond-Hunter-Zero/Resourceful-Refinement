package com.resourceful_refinement.content.fluids;

import com.resourceful_refinement.ResourcefulRefinementMain;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = ResourcefulRefinementMain.MOD_ID, value = Dist.CLIENT)
public class PlungerClientEvents {

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(PlungerItemRenderer.LAYER, PlungerModel::createBodyLayer);
    }
}
