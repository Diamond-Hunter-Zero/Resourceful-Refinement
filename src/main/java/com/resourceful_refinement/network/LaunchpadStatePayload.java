package com.resourceful_refinement.network;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.pug.LaunchpadControllerBlockEntity;
import com.resourceful_refinement.content.pug.LaunchpadMenu;
import com.resourceful_refinement.content.pug.LaunchpadScreen;
import com.resourceful_refinement.content.pug.LaunchpadSnapshot;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;

public record LaunchpadStatePayload(BlockPos pos, LaunchpadSnapshot snapshot) implements CustomPacketPayload {
    public static final Type<LaunchpadStatePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "launchpad_state"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LaunchpadStatePayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeBlockPos(payload.pos);
                payload.snapshot.write(buf);
            },
            buf -> new LaunchpadStatePayload(buf.readBlockPos(), LaunchpadSnapshot.read(buf)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void broadcast(ServerLevel level, LaunchpadControllerBlockEntity controller) {
        var viewers = new ArrayList<net.minecraft.server.level.ServerPlayer>();
        for (var player : level.getServer().getPlayerList().getPlayers()) {
            if (player.containerMenu instanceof LaunchpadMenu menu
                    && menu.getBlockPos().equals(controller.getBlockPos())
                    && player.level().dimension().equals(level.dimension())) {
                viewers.add(player);
            }
        }
        if (viewers.isEmpty()) return;
        LaunchpadStatePayload payload = new LaunchpadStatePayload(controller.getBlockPos(),
                LaunchpadSnapshot.capture(controller));
        viewers.forEach(player -> PacketDistributor.sendToPlayer(player, payload));
    }

    public static void handleClient(LaunchpadStatePayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof LaunchpadScreen screen
                && screen.getMenu().getBlockPos().equals(payload.pos)) {
            screen.applySnapshot(payload.snapshot);
        }
    }
}
