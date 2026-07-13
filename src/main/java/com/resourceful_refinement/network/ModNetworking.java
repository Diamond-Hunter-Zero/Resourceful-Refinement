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
                .playToClient(
                        ResearchCraftingLockPayload.TYPE,
                        ResearchCraftingLockPayload.STREAM_CODEC,
                        ModNetworking::handleResearchCraftingLock
                )
                .playToServer(
                        RequestResearchTreeDataPayload.TYPE,
                        RequestResearchTreeDataPayload.STREAM_CODEC,
                        ModNetworking::handleRequestResearchTreeData
                )
                .playToClient(
                        ResearchTreeSyncPayload.TYPE,
                        ResearchTreeSyncPayload.STREAM_CODEC,
                        ModNetworking::handleResearchTreeSync
                )
                .playToClient(
                        OpenResearchTerminalPayload.TYPE,
                        OpenResearchTerminalPayload.STREAM_CODEC,
                        ModNetworking::handleOpenResearchTerminal
                )
                .playToClient(
                        ResearchTerminalStatePayload.TYPE,
                        ResearchTerminalStatePayload.STREAM_CODEC,
                        ModNetworking::handleResearchTerminalState
                )
                .playToClient(
                        ResearchUnlockToastPayload.TYPE,
                        ResearchUnlockToastPayload.STREAM_CODEC,
                        ModNetworking::handleResearchUnlockToast
                )
                .playToServer(
                        ResearchTerminalActionPayload.TYPE,
                        ResearchTerminalActionPayload.STREAM_CODEC,
                        ModNetworking::handleResearchTerminalAction
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

    private static void handleResearchCraftingLock(ResearchCraftingLockPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ResearchCraftingLockPayload.handleClient(payload));
    }

    private static void handleRequestResearchTreeData(RequestResearchTreeDataPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                RequestResearchTreeDataPayload.handle(payload, serverPlayer);
            }
        });
    }

    private static void handleResearchTreeSync(ResearchTreeSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ResearchTreeSyncPayload.handleClient(payload));
    }

    private static void handleOpenResearchTerminal(OpenResearchTerminalPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> OpenResearchTerminalPayload.handleClient(payload));
    }

    private static void handleResearchTerminalState(ResearchTerminalStatePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ResearchTerminalStatePayload.handleClient(payload));
    }

    private static void handleResearchUnlockToast(ResearchUnlockToastPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ResearchUnlockToastPayload.handleClient(payload));
    }

    private static void handleResearchTerminalAction(ResearchTerminalActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                ResearchTerminalActionPayload.handle(payload, serverPlayer);
            }
        });
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
