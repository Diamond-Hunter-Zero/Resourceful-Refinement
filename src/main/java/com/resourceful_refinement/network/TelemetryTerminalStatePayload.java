package com.resourceful_refinement.network;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.glare.terminal.TelemetryTerminalBlockEntity;
import com.resourceful_refinement.content.glare.terminal.TelemetryTerminalMenu;
import com.resourceful_refinement.content.glare.terminal.TelemetryTerminalScreen;
import com.resourceful_refinement.content.glare.terminal.TelemetryTerminalSnapshot;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.network.PacketDistributor;

public record TelemetryTerminalStatePayload(BlockPos pos, TelemetryTerminalSnapshot snapshot) implements CustomPacketPayload {
    public static final Type<TelemetryTerminalStatePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "telemetry_terminal_state"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TelemetryTerminalStatePayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> { buf.writeBlockPos(payload.pos); payload.snapshot.write(buf); },
            buf -> new TelemetryTerminalStatePayload(buf.readBlockPos(), TelemetryTerminalSnapshot.read(buf)));

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void broadcast(ServerLevel level, TelemetryTerminalBlockEntity terminal) {
        TelemetryTerminalStatePayload payload = new TelemetryTerminalStatePayload(terminal.getBlockPos(), terminal.snapshot());
        for (var player : level.getServer().getPlayerList().getPlayers()) {
            if (player.containerMenu instanceof TelemetryTerminalMenu menu && menu.getBlockPos().equals(terminal.getBlockPos())
                    && player.level().dimension().equals(level.dimension())) {
                PacketDistributor.sendToPlayer(player, payload);
            }
        }
    }

    public static void handleClient(TelemetryTerminalStatePayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof TelemetryTerminalScreen screen && screen.getMenu().getBlockPos().equals(payload.pos)) {
            screen.applySnapshot(payload.snapshot);
        }
    }
}
