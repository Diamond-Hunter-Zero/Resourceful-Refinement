package com.resourceful_refinement.network;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.refill_station.FluidRefillStationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

public record SetRefillStationTrackingIdPayload(BlockPos pos, String trackingId) implements CustomPacketPayload {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "set_refill_station_tracking_id");

    public static final Type<SetRefillStationTrackingIdPayload> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, SetRefillStationTrackingIdPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, SetRefillStationTrackingIdPayload::pos,
                    ByteBufCodecs.STRING_UTF8, SetRefillStationTrackingIdPayload::trackingId,
                    SetRefillStationTrackingIdPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SetRefillStationTrackingIdPayload payload, ServerPlayer player) {
        if (!player.level().isLoaded(payload.pos())) {
            return;
        }

        BlockEntity be = player.level().getBlockEntity(payload.pos());
        if (!(be instanceof FluidRefillStationBlockEntity station)) {
            return;
        }

        if (!station.isWithinUsableDistance(player)) {
            return;
        }

        station.setTrackingId(payload.trackingId());
    }
}
