package com.resourceful_refinement.content.hoverpack;

import com.mojang.blaze3d.platform.InputConstants;
import com.resourceful_refinement.ResourcefulRefinementMain;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

/**
 * Mod-bus client setup for the Hoverpack: registers the Launch keybinding and adds the wings render layer to the player
 * renderers. Runtime input handling lives in {@link HoverpackClientEvents}.
 */
@EventBusSubscriber(modid = ResourcefulRefinementMain.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class HoverpackClientSetup {

    public static final String KEY_CATEGORY = "key.categories.resourceful_refinement";

    /** The "Launch" key — triggers a Hoverpack launch while worn. Default: R. */
    public static final KeyMapping LAUNCH_KEY = new KeyMapping(
            "key.resourceful_refinement.launch",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            KEY_CATEGORY);

    private HoverpackClientSetup() {}

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(LAUNCH_KEY);
    }

    @SubscribeEvent
    public static void addPlayerLayers(EntityRenderersEvent.AddLayers event) {
        HoverpackWingsLayer.register(event);
    }
}
