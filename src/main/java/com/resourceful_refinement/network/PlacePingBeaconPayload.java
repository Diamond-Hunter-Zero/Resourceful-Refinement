package com.resourceful_refinement.network;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.config.ServerConfig;
import com.resourceful_refinement.content.prism_cell.PrismCell;
import com.resourceful_refinement.content.resonator.ResourceResonatorBlockEntity;
import com.resourceful_refinement.content.resonator.ping.PingBeaconManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * C2S: a request to deploy a Ping Beacon at a radar pin's predicted location. The server validates range (from the
 * Resonator) and that the target chunk is loaded; on success it deploys the beacon (replacing any existing one) and
 * replies with a successful {@link PingBeaconResultPayload}, otherwise with a failure carrying an error message key.
 */
public record PlacePingBeaconPayload(BlockPos resonatorPos, BlockPos targetPos) implements CustomPacketPayload {

    public static final Type<PlacePingBeaconPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "place_ping_beacon"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlacePingBeaconPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeBlockPos(payload.resonatorPos);
                buf.writeBlockPos(payload.targetPos);
            },
            buf -> new PlacePingBeaconPayload(buf.readBlockPos(), buf.readBlockPos()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PlacePingBeaconPayload payload, ServerPlayer player) {
        // The Resonator must be present and in reach (standard anti-spoof guard).
        ResourceResonatorBlockEntity resonator = ServerPayloadGuard.loadedNearbyBlockEntity(
                player, payload.resonatorPos, ResourceResonatorBlockEntity.class);
        if (resonator == null || !(player.level() instanceof ServerLevel level)) {
            return;
        }

        // A charged Prism Cell powers the ping. Without one, or without enough charge, the beacon fails.
        PrismCell cell = resonator.getPrismCell();
        if (cell == null) {
            PacketDistributor.sendToPlayer(player, PingBeaconResultPayload.failure("ping_no_cell", null));
            return;
        }
        int chargeCost = ServerConfig.RESONATOR_PING_CHARGE_COST.get();
        if (!cell.hasCharge(chargeCost)) {
            PacketDistributor.sendToPlayer(player, PingBeaconResultPayload.failure("ping_no_charge", cell));
            return;
        }

        int rangeChunks = ServerConfig.RESONATOR_PING_RANGE_CHUNKS.get();
        long dChunkX = SectionPos.blockToSectionCoord(payload.targetPos.getX())
                - SectionPos.blockToSectionCoord(payload.resonatorPos.getX());
        long dChunkZ = SectionPos.blockToSectionCoord(payload.targetPos.getZ())
                - SectionPos.blockToSectionCoord(payload.resonatorPos.getZ());
        if (dChunkX * dChunkX + dChunkZ * dChunkZ > (long) rangeChunks * rangeChunks) {
            PacketDistributor.sendToPlayer(player, PingBeaconResultPayload.failure("ping_out_of_range", cell));
            return;
        }

        int chunkX = SectionPos.blockToSectionCoord(payload.targetPos.getX());
        int chunkZ = SectionPos.blockToSectionCoord(payload.targetPos.getZ());
        if (!level.hasChunk(chunkX, chunkZ)) {
            PacketDistributor.sendToPlayer(player, PingBeaconResultPayload.failure("ping_not_loaded", cell));
            return;
        }

        // Validation passed — consume charge (destroying the cell if it empties) and deploy the beacon.
        resonator.consumePingCharge(chargeCost);
        PingBeaconManager.place(level, payload.targetPos);
        PacketDistributor.sendToPlayer(player, PingBeaconResultPayload.ok(resonator.getPrismCell()));
    }
}
