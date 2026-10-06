package com.resourceful_refinement.network;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.resonator.ping.ClientPingBeacon;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * S2C: the authoritative Ping Beacon state, broadcast to every client whenever it changes (deploy / replace /
 * expire) and sent to players on join. When {@code active} is false the other fields are placeholders.
 */
public record PingBeaconSyncPayload(boolean active, ResourceLocation dimension, BlockPos pos, int remainingTicks)
        implements CustomPacketPayload {

    public static final Type<PingBeaconSyncPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "ping_beacon_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PingBeaconSyncPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeBoolean(payload.active);
                buf.writeResourceLocation(payload.dimension);
                buf.writeBlockPos(payload.pos);
                buf.writeVarInt(payload.remainingTicks);
            },
            buf -> new PingBeaconSyncPayload(buf.readBoolean(), buf.readResourceLocation(), buf.readBlockPos(),
                    buf.readVarInt()));

    public static PingBeaconSyncPayload inactive() {
        return new PingBeaconSyncPayload(false, ResourceLocation.withDefaultNamespace("overworld"), BlockPos.ZERO, 0);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(PingBeaconSyncPayload payload) {
        ClientPingBeacon.accept(payload);
    }
}
