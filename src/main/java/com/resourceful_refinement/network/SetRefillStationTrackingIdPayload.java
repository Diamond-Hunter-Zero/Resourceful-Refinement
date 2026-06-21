package com.resourceful_refinement.network;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.refill_station.FluidRefillStationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record SetRefillStationTrackingIdPayload(BlockPos pos, String trackingId) implements CustomPacketPayload {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "set_refill_station_tracking_id");

    public static final Type<SetRefillStationTrackingIdPayload> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, SetRefillStationTrackingIdPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeBlockPos(payload.pos());
                buf.writeUtf(payload.trackingId(), FluidRefillStationBlockEntity.MAX_TRACKING_ID_LENGTH);
            },
            buf -> new SetRefillStationTrackingIdPayload(buf.readBlockPos(),
                    buf.readUtf(FluidRefillStationBlockEntity.MAX_TRACKING_ID_LENGTH)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SetRefillStationTrackingIdPayload payload, ServerPlayer player) {
        FluidRefillStationBlockEntity station = ServerPayloadGuard.loadedNearbyBlockEntity(
                player, payload.pos(), FluidRefillStationBlockEntity.class);
        if (station == null) return;
        station.setTrackingId(payload.trackingId());
    }
}
