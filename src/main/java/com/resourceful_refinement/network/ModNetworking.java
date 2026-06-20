package com.resourceful_refinement.network;

import com.resourceful_refinement.ResourcefulRefinementMain;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ModNetworking {

    private ModNetworking() {}

    public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        event.registrar(ResourcefulRefinementMain.MOD_ID)
                .versioned("1")
                .playToClient(
                        GlareLinkSyncPayload.TYPE,
                        GlareLinkSyncPayload.STREAM_CODEC,
                        ModNetworking::handleGlareLinkSync
                )
                .playToServer(
                        SetRefillStationTrackingIdPayload.TYPE,
                        SetRefillStationTrackingIdPayload.STREAM_CODEC,
                        ModNetworking::handleSetTrackingId
                )
                .playToServer(
                        ConfigureGlareTransceiverPayload.TYPE,
                        ConfigureGlareTransceiverPayload.STREAM_CODEC,
                        ModNetworking::handleConfigureTransceiver
                );
    }

    private static void handleSetTrackingId(SetRefillStationTrackingIdPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                SetRefillStationTrackingIdPayload.handle(payload, serverPlayer);
            }
        });
    }

    private static void handleGlareLinkSync(GlareLinkSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> com.resourceful_refinement.content.glare.rendering.GlareClientLinks.handleSync(payload));
    }

    private static void handleConfigureTransceiver(ConfigureGlareTransceiverPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                ConfigureGlareTransceiverPayload.handle(payload, serverPlayer);
            }
        });
    }
}
