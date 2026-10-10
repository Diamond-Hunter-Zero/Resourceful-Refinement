package com.resourceful_refinement.network;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.prism_cell.PrismCell;
import com.resourceful_refinement.content.resonator.ResourceResonatorScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * S2C: the outcome of a {@link PlacePingBeaconPayload} request, sent only to the requesting player. On failure the
 * {@code messageKey} names a lang entry the Resonator GUI shows in its details panel; on success it is empty and the
 * GUI clears any prior error. It also carries the Resonator's installed-cell state after the action so the GUI's
 * cell readout and Ping availability stay in sync (e.g. after a ping drains or destroys the cell).
 */
public record PingBeaconResultPayload(boolean success, String messageKey,
        boolean cellPresent, int cellTier, int cellCharge, int cellMaxCharge) implements CustomPacketPayload {

    public static final Type<PingBeaconResultPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "ping_beacon_result"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PingBeaconResultPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeBoolean(payload.success);
                buf.writeUtf(payload.messageKey);
                buf.writeBoolean(payload.cellPresent);
                buf.writeVarInt(payload.cellTier);
                buf.writeVarInt(payload.cellCharge);
                buf.writeVarInt(payload.cellMaxCharge);
            },
            buf -> new PingBeaconResultPayload(buf.readBoolean(), buf.readUtf(),
                    buf.readBoolean(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt()));

    private static PingBeaconResultPayload of(boolean success, String messageKey, @Nullable PrismCell cell) {
        if (cell == null) {
            return new PingBeaconResultPayload(success, messageKey, false, 0, 0, 0);
        }
        return new PingBeaconResultPayload(success, messageKey, true,
                cell.tier().index(), cell.currentCharge(), cell.maxCharge());
    }

    public static PingBeaconResultPayload ok(@Nullable PrismCell cell) {
        return of(true, "", cell);
    }

    public static PingBeaconResultPayload failure(String shortKey, @Nullable PrismCell cell) {
        return of(false, "message.resourceful_refinement.resonator." + shortKey, cell);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(PingBeaconResultPayload payload) {
        if (Minecraft.getInstance().screen instanceof ResourceResonatorScreen screen) {
            screen.acceptPingResult(payload);
        }
    }
}
