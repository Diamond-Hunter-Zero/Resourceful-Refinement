package com.resourceful_refinement.network;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.railgun.RailgunClient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * S2C: entity {@code entityId} just fired a railgun from its main or off hand. Sent to the shooter and everyone
 * tracking them; each client draws the beam itself, tracing the shooter's reticle up to {@code range} blocks.
 */
public record RailgunFirePayload(int entityId, boolean mainHand, float range) implements CustomPacketPayload {

    public static final Type<RailgunFirePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "railgun_fire"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RailgunFirePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, RailgunFirePayload::entityId,
            ByteBufCodecs.BOOL, RailgunFirePayload::mainHand,
            ByteBufCodecs.FLOAT, RailgunFirePayload::range,
            RailgunFirePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(RailgunFirePayload payload) {
        RailgunClient.onFire(payload);
    }
}
