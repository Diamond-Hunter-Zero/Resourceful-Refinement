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
                )
                .playToServer(
                        TelemetryTerminalActionPayload.TYPE,
                        TelemetryTerminalActionPayload.STREAM_CODEC,
                        ModNetworking::handleTelemetryTerminalAction
                )
                .playToServer(
                        ConfigureLaunchpadPayload.TYPE,
                        ConfigureLaunchpadPayload.STREAM_CODEC,
                        ModNetworking::handleConfigureLaunchpad
                )
                .playToServer(
                        ToggleGlareNetworkPayload.TYPE,
                        ToggleGlareNetworkPayload.STREAM_CODEC,
                        ModNetworking::handleToggleGlareNetwork
                )
                .playToClient(
                        TelemetryTerminalStatePayload.TYPE,
                        TelemetryTerminalStatePayload.STREAM_CODEC,
                        ModNetworking::handleTelemetryTerminalState
                )
                .playToClient(
                        LaunchpadStatePayload.TYPE,
                        LaunchpadStatePayload.STREAM_CODEC,
                        ModNetworking::handleLaunchpadState
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

    private static void handleTelemetryTerminalAction(TelemetryTerminalActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) TelemetryTerminalActionPayload.handle(payload, serverPlayer);
        });
    }

    private static void handleTelemetryTerminalState(TelemetryTerminalStatePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> TelemetryTerminalStatePayload.handleClient(payload));
    }

    private static void handleLaunchpadState(LaunchpadStatePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> LaunchpadStatePayload.handleClient(payload));
    }

    private static void handleConfigureLaunchpad(ConfigureLaunchpadPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                ConfigureLaunchpadPayload.handle(payload, serverPlayer);
            }
        });
    }

    private static void handleToggleGlareNetwork(ToggleGlareNetworkPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                ToggleGlareNetworkPayload.handle(payload, serverPlayer);
            }
        });
    }
}
