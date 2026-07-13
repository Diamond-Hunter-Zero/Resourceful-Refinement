package com.resourceful_refinement.network;

import com.resourceful_refinement.ResourcefulRefinementMain;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public record RequestResearchTreeDataPayload() implements CustomPacketPayload {
    public static final Type<RequestResearchTreeDataPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "request_research_tree_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestResearchTreeDataPayload> STREAM_CODEC =
            StreamCodec.of(RequestResearchTreeDataPayload::write, RequestResearchTreeDataPayload::read);

    private static void write(RegistryFriendlyByteBuf buf, RequestResearchTreeDataPayload payload) {}

    private static RequestResearchTreeDataPayload read(RegistryFriendlyByteBuf buf) {
        return new RequestResearchTreeDataPayload();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RequestResearchTreeDataPayload payload, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, ResearchTreeSyncPayload.capture(player));
    }
}
