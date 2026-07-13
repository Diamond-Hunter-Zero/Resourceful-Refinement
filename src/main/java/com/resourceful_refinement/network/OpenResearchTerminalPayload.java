package com.resourceful_refinement.network;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.client.research.ClientResearchTerminalData;
import com.resourceful_refinement.client.research.ResearchTreeScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

public record OpenResearchTerminalPayload(ResearchTerminalStatePayload state) implements CustomPacketPayload {
    public static final Type<OpenResearchTerminalPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "open_research_terminal"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenResearchTerminalPayload> STREAM_CODEC =
            StreamCodec.of(OpenResearchTerminalPayload::write, OpenResearchTerminalPayload::read);

    private static void write(RegistryFriendlyByteBuf buf, OpenResearchTerminalPayload payload) {
        ResearchTerminalStatePayload.writeFields(buf, payload.state);
    }

    private static OpenResearchTerminalPayload read(RegistryFriendlyByteBuf buf) {
        return new OpenResearchTerminalPayload(ResearchTerminalStatePayload.readFields(buf));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(OpenResearchTerminalPayload payload) {
        ClientResearchTerminalData.open(payload.state);
        PacketDistributor.sendToServer(new RequestResearchTreeDataPayload());
        Minecraft.getInstance().setScreen(new ResearchTreeScreen(true));
    }
}
