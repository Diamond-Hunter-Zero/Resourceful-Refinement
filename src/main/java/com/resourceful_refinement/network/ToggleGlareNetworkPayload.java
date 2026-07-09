package com.resourceful_refinement.network;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.glare.GlareSavedData;
import com.resourceful_refinement.content.glare.GlareService;
import com.resourceful_refinement.content.glare.IGlareNode;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.UUID;

public record ToggleGlareNetworkPayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<ToggleGlareNetworkPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "toggle_glare_network"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleGlareNetworkPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeBlockPos(payload.pos),
            buf -> new ToggleGlareNetworkPayload(buf.readBlockPos()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ToggleGlareNetworkPayload payload, ServerPlayer player) {
        BlockEntity blockEntity = ServerPayloadGuard.loadedNearbyBlockEntity(player, payload.pos, BlockEntity.class);
        if (!(blockEntity instanceof IGlareNode node) || !(player.level() instanceof ServerLevel server)) {
            return;
        }
        GlareService.ensureLiveNodeRegistered(server, payload.pos);
        GlareSavedData.NodeRecord record = GlareService.getNode(server, node.getGlareNodePos()).orElse(null);
        UUID networkId = record == null ? null : record.networkId;
        if (networkId == null) {
            return;
        }
        GlareSavedData.NetworkRecord network = GlareService.getNetwork(server, networkId).orElse(null);
        if (network == null) {
            return;
        }
        if (network.overloaded) {
            GlareService.tryResetNetwork(server, networkId);
        } else {
            GlareService.forceOverloadNetwork(server, networkId);
        }
    }
}
