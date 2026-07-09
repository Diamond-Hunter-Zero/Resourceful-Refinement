package com.resourceful_refinement.network;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.glare.GlareAddress;
import com.resourceful_refinement.content.pug.LaunchCondition;
import com.resourceful_refinement.content.pug.LaunchpadConfiguration;
import com.resourceful_refinement.content.pug.LaunchpadControllerBlockEntity;
import com.resourceful_refinement.content.pug.LaunchpadMode;
import com.resourceful_refinement.content.pug.LaunchpadMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record ConfigureLaunchpadPayload(BlockPos pos, LaunchpadMode mode, GlareAddress localAddress,
        GlareAddress destinationAddress, LaunchCondition launchCondition, int timerSeconds)
        implements CustomPacketPayload {
    public static final Type<ConfigureLaunchpadPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "configure_launchpad"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigureLaunchpadPayload> STREAM_CODEC = StreamCodec.of(
            ConfigureLaunchpadPayload::write, ConfigureLaunchpadPayload::read);

    private static void write(RegistryFriendlyByteBuf buf, ConfigureLaunchpadPayload payload) {
        buf.writeBlockPos(payload.pos);
        buf.writeEnum(payload.mode);
        payload.localAddress.write(buf);
        payload.destinationAddress.write(buf);
        buf.writeEnum(payload.launchCondition);
        buf.writeVarInt(payload.timerSeconds);
    }

    private static ConfigureLaunchpadPayload read(RegistryFriendlyByteBuf buf) {
        return new ConfigureLaunchpadPayload(buf.readBlockPos(), buf.readEnum(LaunchpadMode.class),
                GlareAddress.read(buf), GlareAddress.read(buf), buf.readEnum(LaunchCondition.class), buf.readVarInt());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ConfigureLaunchpadPayload payload, ServerPlayer player) {
        if (!(player.containerMenu instanceof LaunchpadMenu menu) || !menu.getBlockPos().equals(payload.pos)
                || payload.timerSeconds < 0 || payload.timerSeconds > LaunchpadConfiguration.MAX_TIMER_SECONDS
                || !validAddress(payload.localAddress) || !validAddress(payload.destinationAddress)) return;
        LaunchpadControllerBlockEntity controller = ServerPayloadGuard.loadedNearbyBlockEntity(player, payload.pos,
                LaunchpadControllerBlockEntity.class);
        if (controller == null) return;
        controller.applyConfiguration(new LaunchpadConfiguration(payload.mode, payload.localAddress,
                payload.destinationAddress, payload.launchCondition, payload.timerSeconds));
        LaunchpadStatePayload.broadcast(player.serverLevel(), controller);
    }

    private static boolean validAddress(GlareAddress address) {
        return BuiltInRegistries.ITEM.containsKey(address.first())
                && BuiltInRegistries.ITEM.containsKey(address.second())
                && BuiltInRegistries.ITEM.containsKey(address.third());
    }
}
