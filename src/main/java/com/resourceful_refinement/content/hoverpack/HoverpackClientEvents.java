package com.resourceful_refinement.content.hoverpack;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.network.HoverpackLaunchPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Game-bus client input for the Hoverpack. Each tick, drains the Launch key's queued presses and, if the local player
 * is wearing a Hoverpack, relays a launch request to the server (which validates charge and applies the impulse).
 */
@EventBusSubscriber(modid = ResourcefulRefinementMain.MOD_ID, value = Dist.CLIENT)
public final class HoverpackClientEvents {

    private HoverpackClientEvents() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        boolean wearing = player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof HoverpackItem;
        boolean launched = false;
        while (HoverpackClientSetup.LAUNCH_KEY.consumeClick()) {
            if (wearing) {
                launched = true;
            }
        }
        if (launched) {
            PacketDistributor.sendToServer(HoverpackLaunchPayload.INSTANCE);
        }
    }
}
