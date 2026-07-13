package com.resourceful_refinement.client.research;

import com.mojang.blaze3d.platform.InputConstants;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.network.RequestResearchTreeDataPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public final class ResearchKeyMappings {
    public static final KeyMapping OPEN_RESEARCH_TREE = new KeyMapping(
            "key." + ResourcefulRefinementMain.MOD_ID + ".research_tree",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            "key.categories." + ResourcefulRefinementMain.MOD_ID);

    private ResearchKeyMappings() {}

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(OPEN_RESEARCH_TREE);
    }

    public static void tick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        while (OPEN_RESEARCH_TREE.consumeClick()) {
            if (minecraft.player == null || minecraft.level == null || minecraft.screen instanceof ResearchTreeScreen) {
                continue;
            }
            PacketDistributor.sendToServer(new RequestResearchTreeDataPayload());
            minecraft.setScreen(new ResearchTreeScreen());
        }
    }
}
