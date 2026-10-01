package com.resourceful_refinement.network;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.resonator.ResourceResonatorBlockEntity;
import com.resourceful_refinement.content.resonator.scan.ResonatorTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * C2S: the player toggled the scan filter. Persists the new per-target enabled flags on the Resonator.
 */
public record SetResonatorFilterPayload(BlockPos pos, boolean[] filter) implements CustomPacketPayload {

    public static final Type<SetResonatorFilterPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "set_resonator_filter"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetResonatorFilterPayload> STREAM_CODEC = StreamCodec.of(
            SetResonatorFilterPayload::write, SetResonatorFilterPayload::read);

    private static void write(RegistryFriendlyByteBuf buf, SetResonatorFilterPayload payload) {
        buf.writeBlockPos(payload.pos);
        int count = ResonatorTarget.count();
        for (int i = 0; i < count; i++) {
            buf.writeBoolean(i < payload.filter.length && payload.filter[i]);
        }
    }

    private static SetResonatorFilterPayload read(RegistryFriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        int count = ResonatorTarget.count();
        boolean[] filter = new boolean[count];
        for (int i = 0; i < count; i++) {
            filter[i] = buf.readBoolean();
        }
        return new SetResonatorFilterPayload(pos, filter);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SetResonatorFilterPayload payload, ServerPlayer player) {
        if (payload.filter.length != ResonatorTarget.count()) {
            return;
        }
        ResourceResonatorBlockEntity resonator = ServerPayloadGuard.loadedNearbyBlockEntity(
                player, payload.pos, ResourceResonatorBlockEntity.class);
        if (resonator == null) {
            return;
        }
        resonator.applyFilter(payload.filter);
    }
}
