package com.resourceful_refinement.network;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.glare.GlareAddress;
import com.resourceful_refinement.content.glare.terminal.TelemetryTerminalBlockEntity;
import com.resourceful_refinement.content.glare.terminal.TelemetryTerminalMenu;
import com.resourceful_refinement.content.glare.terminal.TelemetryTerminalMode;
import com.resourceful_refinement.content.glare.terminal.TelemetryTerminalSnapshot;
import com.resourceful_refinement.content.glare.GlareMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public record TelemetryTerminalActionPayload(BlockPos pos, Action action, TelemetryTerminalMode mode,
        GlareAddress address, String text, boolean flag, UUID messageId) implements CustomPacketPayload {
    public static final Type<TelemetryTerminalActionPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "telemetry_terminal_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TelemetryTerminalActionPayload> STREAM_CODEC = StreamCodec.of(
            TelemetryTerminalActionPayload::write, TelemetryTerminalActionPayload::read);

    public static TelemetryTerminalActionPayload simple(BlockPos pos, Action action) {
        return new TelemetryTerminalActionPayload(pos, action, TelemetryTerminalMode.MANUAL, GlareAddress.empty(), "", false, new UUID(0, 0));
    }

    private static void write(RegistryFriendlyByteBuf buf, TelemetryTerminalActionPayload payload) {
        buf.writeBlockPos(payload.pos);
        buf.writeEnum(payload.action);
        buf.writeEnum(payload.mode);
        buf.writeResourceLocation(payload.address.first());
        buf.writeResourceLocation(payload.address.second());
        buf.writeResourceLocation(payload.address.third());
        buf.writeUtf(payload.text, 512);
        buf.writeBoolean(payload.flag);
        buf.writeUUID(payload.messageId);
    }

    private static TelemetryTerminalActionPayload read(RegistryFriendlyByteBuf buf) {
        return new TelemetryTerminalActionPayload(buf.readBlockPos(), buf.readEnum(Action.class),
                buf.readEnum(TelemetryTerminalMode.class), new GlareAddress(buf.readResourceLocation(), buf.readResourceLocation(), buf.readResourceLocation()),
                buf.readUtf(512), buf.readBoolean(), buf.readUUID());
    }

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(TelemetryTerminalActionPayload payload, ServerPlayer player) {
        if (!(player.containerMenu instanceof TelemetryTerminalMenu menu)
                || !menu.getBlockPos().equals(payload.pos)
                || !validAddress(payload.address) || !validText(payload)) return;
        TelemetryTerminalBlockEntity terminal = ServerPayloadGuard.loadedNearbyBlockEntity(
                player, payload.pos, TelemetryTerminalBlockEntity.class);
        if (terminal == null) return;
        switch (payload.action) {
            case SET_MODE -> terminal.setMode(payload.mode);
            case SET_MANUAL_VIEW -> terminal.setManualComposeOpen(payload.flag);
            case SAVE_MANUAL_DRAFT -> terminal.setDraft(false, payload.address, payload.text);
            case SAVE_AUTO_DRAFT -> terminal.setDraft(true, payload.address, payload.text);
            case ADD_CONTACT -> terminal.addContact(payload.address);
            case REMOVE_CONTACT -> terminal.removeContact(payload.address);
            case ADD_FILTER -> terminal.addFilter(payload.text);
            case SAVE_FILTER_DRAFT -> terminal.setReceiveFilterDraft(payload.text);
            case REMOVE_FILTER -> terminal.removeFilter(payload.text);
            case SET_DISCARD -> terminal.setDiscardMatchingMessages(payload.flag);
            case SEND_MANUAL -> terminal.sendManual();
            case DISCARD_MESSAGE -> terminal.discardMessage(payload.messageId);
        }
    }

    private static boolean validAddress(GlareAddress address) {
        return BuiltInRegistries.ITEM.containsKey(address.first())
                && BuiltInRegistries.ITEM.containsKey(address.second())
                && BuiltInRegistries.ITEM.containsKey(address.third());
    }

    private static boolean validText(TelemetryTerminalActionPayload payload) {
        int max = switch (payload.action) {
            case ADD_FILTER, SAVE_FILTER_DRAFT, REMOVE_FILTER -> TelemetryTerminalSnapshot.MAX_FILTER_LENGTH;
            case SAVE_MANUAL_DRAFT, SAVE_AUTO_DRAFT -> GlareMessage.MAX_BODY_LENGTH;
            default -> 0;
        };
        return payload.text != null && (max > 0 ? payload.text.length() <= max : payload.text.isEmpty());
    }

    public enum Action {
        SET_MODE,
        SET_MANUAL_VIEW,
        SAVE_MANUAL_DRAFT,
        SAVE_AUTO_DRAFT,
        ADD_CONTACT,
        REMOVE_CONTACT,
        ADD_FILTER,
        SAVE_FILTER_DRAFT,
        REMOVE_FILTER,
        SET_DISCARD,
        SEND_MANUAL,
        DISCARD_MESSAGE
    }
}
