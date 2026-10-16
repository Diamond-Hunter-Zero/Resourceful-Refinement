package com.resourceful_refinement.network;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.hoverpack.HoverpackLaunchHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * C2S: the player pressed the Launch key while wearing a Hoverpack. Carries no data — the server resolves the worn
 * Hoverpack and its installed cell from the player (see {@link HoverpackLaunchHandler#tryLaunch}).
 */
public record HoverpackLaunchPayload() implements CustomPacketPayload {

    public static final HoverpackLaunchPayload INSTANCE = new HoverpackLaunchPayload();

    public static final Type<HoverpackLaunchPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "hoverpack_launch"));

    public static final StreamCodec<RegistryFriendlyByteBuf, HoverpackLaunchPayload> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(HoverpackLaunchPayload payload, ServerPlayer player) {
        HoverpackLaunchHandler.tryLaunch(player);
    }
}
