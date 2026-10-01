package com.resourceful_refinement.network;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.resonator.ResourceResonatorScreen;
import com.resourceful_refinement.content.resonator.scan.ScannedPoi;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * S2C: fresh scan results for the Resonator at {@code pos}. Consumed by an open {@link ResourceResonatorScreen}.
 */
public record ResonatorScanResultPayload(BlockPos pos, List<ScannedPoi> results) implements CustomPacketPayload {

    public static final Type<ResonatorScanResultPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "resonator_scan_result"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ResonatorScanResultPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeBlockPos(payload.pos);
                ScannedPoi.LIST_STREAM_CODEC.encode(buf, payload.results);
            },
            buf -> new ResonatorScanResultPayload(buf.readBlockPos(), ScannedPoi.LIST_STREAM_CODEC.decode(buf)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(ResonatorScanResultPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof ResourceResonatorScreen screen
                && screen.getMenu().getBlockPos().equals(payload.pos)) {
            screen.acceptScanResults(payload.results);
        }
    }
}
