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
                )
                .playToServer(
                        ResonatorScanRequestPayload.TYPE,
                        ResonatorScanRequestPayload.STREAM_CODEC,
                        ModNetworking::handleResonatorScanRequest
                )
                .playToServer(
                        SetResonatorFilterPayload.TYPE,
                        SetResonatorFilterPayload.STREAM_CODEC,
                        ModNetworking::handleSetResonatorFilter
                )
                .playToClient(
                        ResonatorScanResultPayload.TYPE,
                        ResonatorScanResultPayload.STREAM_CODEC,
                        ModNetworking::handleResonatorScanResult
                )
                .playToServer(
                        PlacePingBeaconPayload.TYPE,
                        PlacePingBeaconPayload.STREAM_CODEC,
                        ModNetworking::handlePlacePingBeacon
                )
                .playToClient(
                        PingBeaconResultPayload.TYPE,
                        PingBeaconResultPayload.STREAM_CODEC,
                        ModNetworking::handlePingBeaconResult
                )
                .playToClient(
                        PingBeaconSyncPayload.TYPE,
                        PingBeaconSyncPayload.STREAM_CODEC,
                        ModNetworking::handlePingBeaconSync
                )
                .playToServer(
                        HoverpackLaunchPayload.TYPE,
                        HoverpackLaunchPayload.STREAM_CODEC,
                        ModNetworking::handleHoverpackLaunch
                );
    }

    private static void handleHoverpackLaunch(HoverpackLaunchPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                HoverpackLaunchPayload.handle(payload, serverPlayer);
            }
        });
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

    private static void handleResonatorScanRequest(ResonatorScanRequestPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                ResonatorScanRequestPayload.handle(payload, serverPlayer);
            }
        });
    }

    private static void handleSetResonatorFilter(SetResonatorFilterPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                SetResonatorFilterPayload.handle(payload, serverPlayer);
            }
        });
    }

    private static void handleResonatorScanResult(ResonatorScanResultPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ResonatorScanResultPayload.handleClient(payload));
    }

    private static void handlePlacePingBeacon(PlacePingBeaconPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                PlacePingBeaconPayload.handle(payload, serverPlayer);
            }
        });
    }

    private static void handlePingBeaconResult(PingBeaconResultPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> PingBeaconResultPayload.handleClient(payload));
    }

    private static void handlePingBeaconSync(PingBeaconSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> PingBeaconSyncPayload.handleClient(payload));
    }
}
