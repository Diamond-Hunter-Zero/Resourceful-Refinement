package com.resourceful_refinement.network;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.resonator.ResourceResonatorScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * S2C: the outcome of a {@link PlacePingBeaconPayload} request, sent only to the requesting player. On failure the
 * {@code messageKey} names a lang entry the Resonator GUI shows in its details panel; on success it is empty and the
 * GUI clears any prior error.
 */
public record PingBeaconResultPayload(boolean success, String messageKey) implements CustomPacketPayload {

    public static final Type<PingBeaconResultPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "ping_beacon_result"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PingBeaconResultPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeBoolean(payload.success);
                buf.writeUtf(payload.messageKey);
            },
            buf -> new PingBeaconResultPayload(buf.readBoolean(), buf.readUtf()));

    public static PingBeaconResultPayload ok() {
        return new PingBeaconResultPayload(true, "");
    }

    public static PingBeaconResultPayload failure(String shortKey) {
        return new PingBeaconResultPayload(false, "message.resourceful_refinement.resonator." + shortKey);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(PingBeaconResultPayload payload) {
        if (Minecraft.getInstance().screen instanceof ResourceResonatorScreen screen) {
            screen.acceptPingResult(payload.success, payload.messageKey);
        }
    }
}
