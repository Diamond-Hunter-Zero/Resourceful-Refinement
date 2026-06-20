package com.resourceful_refinement.network;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.glare.GlareChromaticTransceiverBlockEntity;
import com.resourceful_refinement.content.glare.GlareLogicMode;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record ConfigureGlareTransceiverPayload(BlockPos pos, GlareLogicMode mode, int[] thresholds, int[] comparisons) implements CustomPacketPayload {
    public static final Type<ConfigureGlareTransceiverPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "configure_glare_transceiver"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigureGlareTransceiverPayload> STREAM_CODEC = StreamCodec.of(
            ConfigureGlareTransceiverPayload::write, ConfigureGlareTransceiverPayload::read);

    private static void write(RegistryFriendlyByteBuf buf, ConfigureGlareTransceiverPayload payload) {
        buf.writeBlockPos(payload.pos);
        buf.writeEnum(payload.mode);
        for (int i = 0; i < 16; i++) buf.writeVarInt((i < payload.thresholds.length ? payload.thresholds[i] : -1) + 1);
        for (int i = 0; i < 16; i++) buf.writeVarInt(i < payload.comparisons.length ? payload.comparisons[i] : 0);
    }

    private static ConfigureGlareTransceiverPayload read(RegistryFriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        GlareLogicMode mode = buf.readEnum(GlareLogicMode.class);
        int[] thresholds = new int[16];
        for (int i = 0; i < thresholds.length; i++) thresholds[i] = buf.readVarInt() - 1;
        int[] comparisons = new int[16];
        for (int i = 0; i < comparisons.length; i++) comparisons[i] = buf.readVarInt();
        return new ConfigureGlareTransceiverPayload(pos, mode, thresholds, comparisons);
    }

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(ConfigureGlareTransceiverPayload payload, ServerPlayer player) {
        if (!player.level().isLoaded(payload.pos) || payload.thresholds.length != 16 || payload.comparisons.length != 16) return;
        if (!(player.level().getBlockEntity(payload.pos) instanceof GlareChromaticTransceiverBlockEntity transceiver)) return;
        if (!transceiver.isWithinUsableDistance(player)) return;
        transceiver.applyConfiguration(payload.mode, payload.thresholds, payload.comparisons);
    }
}
