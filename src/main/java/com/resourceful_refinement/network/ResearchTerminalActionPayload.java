package com.resourceful_refinement.network;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.research_terminal.ResearchTerminalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public record ResearchTerminalActionPayload(BlockPos pos, Action action, @Nullable ResourceLocation nodeId)
        implements CustomPacketPayload {
    public static final Type<ResearchTerminalActionPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "research_terminal_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ResearchTerminalActionPayload> STREAM_CODEC =
            StreamCodec.of(ResearchTerminalActionPayload::write, ResearchTerminalActionPayload::read);

    public static ResearchTerminalActionPayload claim(BlockPos pos) {
        return new ResearchTerminalActionPayload(pos, Action.CLAIM_OWNER, null);
    }

    public static ResearchTerminalActionPayload setTarget(BlockPos pos, ResourceLocation nodeId) {
        return new ResearchTerminalActionPayload(pos, Action.SET_TARGET, nodeId);
    }

    public static ResearchTerminalActionPayload clearTarget(BlockPos pos) {
        return new ResearchTerminalActionPayload(pos, Action.CLEAR_TARGET, null);
    }

    private static void write(RegistryFriendlyByteBuf buf, ResearchTerminalActionPayload payload) {
        buf.writeBlockPos(payload.pos);
        buf.writeEnum(payload.action);
        ResearchTerminalStatePayload.writeNullableResourceLocation(buf, payload.nodeId);
    }

    private static ResearchTerminalActionPayload read(RegistryFriendlyByteBuf buf) {
        return new ResearchTerminalActionPayload(buf.readBlockPos(), buf.readEnum(Action.class),
                ResearchTerminalStatePayload.readNullableResourceLocation(buf));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ResearchTerminalActionPayload payload, ServerPlayer player) {
        ResearchTerminalBlockEntity terminal = ServerPayloadGuard.loadedNearbyBlockEntity(player, payload.pos,
                ResearchTerminalBlockEntity.class);
        if (terminal == null || !(player.level() instanceof ServerLevel server)) {
            return;
        }

        switch (payload.action) {
            case CLAIM_OWNER -> {
                if (!terminal.hasOwner()) {
                    terminal.assignOwner(player);
                }
            }
            case SET_TARGET -> {
                if (payload.nodeId != null && player.getUUID().equals(terminal.getOwnerUuid())
                        && terminal.canTargetNode(server, payload.nodeId)) {
                    terminal.setTargetNodeId(payload.nodeId);
                }
            }
            case CLEAR_TARGET -> {
                if (player.getUUID().equals(terminal.getOwnerUuid())) {
                    terminal.setTargetNodeId(null);
                }
            }
        }

        PacketDistributor.sendToPlayer(player, ResearchTerminalStatePayload.capture(terminal));
        PacketDistributor.sendToPlayer(player, ResearchTreeSyncPayload.capture(player));
    }

    public enum Action {
        CLAIM_OWNER,
        SET_TARGET,
        CLEAR_TARGET
    }
}
