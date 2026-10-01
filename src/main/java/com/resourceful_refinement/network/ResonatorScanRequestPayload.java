package com.resourceful_refinement.network;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.resonator.ResourceResonatorBlockEntity;
import com.resourceful_refinement.content.resonator.scan.ScannedPoi;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/**
 * C2S: the player pressed "Scan". The server runs a seed-based scan from the Resonator's position and replies with
 * a {@link ResonatorScanResultPayload} to the requesting player only.
 */
public record ResonatorScanRequestPayload(BlockPos pos) implements CustomPacketPayload {

    public static final Type<ResonatorScanRequestPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "resonator_scan_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ResonatorScanRequestPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeBlockPos(payload.pos),
            buf -> new ResonatorScanRequestPayload(buf.readBlockPos()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ResonatorScanRequestPayload payload, ServerPlayer player) {
        ResourceResonatorBlockEntity resonator = ServerPayloadGuard.loadedNearbyBlockEntity(
                player, payload.pos, ResourceResonatorBlockEntity.class);
        if (resonator == null || !(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        List<ScannedPoi> results = resonator.runScan(serverLevel);
        PacketDistributor.sendToPlayer(player, new ResonatorScanResultPayload(payload.pos, results));
    }
}
