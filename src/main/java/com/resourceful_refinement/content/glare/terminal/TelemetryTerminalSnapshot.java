package com.resourceful_refinement.content.glare.terminal;

import com.resourceful_refinement.content.glare.GlareAddress;
import com.resourceful_refinement.content.glare.GlareMessage;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

/** Complete immutable menu state used both when opening the menu and when synchronizing concurrent viewers. */
public record TelemetryTerminalSnapshot(int revision, TelemetryTerminalMode mode, boolean manualComposeOpen,
        GlareAddress ownAddress, List<GlareAddress> contacts, GlareAddress manualDestination, String manualBody,
        GlareAddress autoSendDestination, String autoSendBody, boolean displayLinkActive, List<String> receiveFilters,
        String receiveFilterDraft, boolean discardMatchingMessages, TelemetryTerminalResult lastResult, List<GlareMessage> inbox) {
    public static final int MAX_CONTACTS = 32;
    public static final int MAX_FILTERS = 32;
    public static final int MAX_FILTER_LENGTH = 64;
    public static final int MAX_SYNCED_MESSAGES = 256;

    public TelemetryTerminalSnapshot {
        contacts = List.copyOf(contacts);
        receiveFilters = List.copyOf(receiveFilters);
        inbox = List.copyOf(inbox);
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(revision);
        buf.writeEnum(mode);
        buf.writeBoolean(manualComposeOpen);
        writeAddress(buf, ownAddress);
        buf.writeVarInt(contacts.size());
        contacts.forEach(address -> writeAddress(buf, address));
        writeAddress(buf, manualDestination);
        buf.writeUtf(manualBody, 512);
        writeAddress(buf, autoSendDestination);
        buf.writeUtf(autoSendBody, 512);
        buf.writeBoolean(displayLinkActive);
        buf.writeVarInt(receiveFilters.size());
        receiveFilters.forEach(filter -> buf.writeUtf(filter, MAX_FILTER_LENGTH));
        buf.writeUtf(receiveFilterDraft, MAX_FILTER_LENGTH);
        buf.writeBoolean(discardMatchingMessages);
        buf.writeEnum(lastResult);
        int syncedMessages = Math.min(inbox.size(), MAX_SYNCED_MESSAGES);
        buf.writeVarInt(syncedMessages);
        for (GlareMessage message : inbox.subList(inbox.size() - syncedMessages, inbox.size())) {
            buf.writeUUID(message.id());
            writeAddress(buf, message.from());
            writeAddress(buf, message.to());
            buf.writeUtf(message.body(), 512);
            buf.writeLong(message.gameTime());
        }
    }

    public static TelemetryTerminalSnapshot read(RegistryFriendlyByteBuf buf) {
        int revision = buf.readVarInt();
        TelemetryTerminalMode mode = buf.readEnum(TelemetryTerminalMode.class);
        boolean compose = buf.readBoolean();
        GlareAddress own = readAddress(buf);
        int contactCount = Math.min(buf.readVarInt(), MAX_CONTACTS);
        List<GlareAddress> contacts = new ArrayList<>(contactCount);
        for (int i = 0; i < contactCount; i++) contacts.add(readAddress(buf));
        GlareAddress manualDestination = readAddress(buf);
        String manualBody = buf.readUtf(512);
        GlareAddress autoDestination = readAddress(buf);
        String autoBody = buf.readUtf(512);
        boolean displayLinkActive = buf.readBoolean();
        int filterCount = Math.min(buf.readVarInt(), MAX_FILTERS);
        List<String> filters = new ArrayList<>(filterCount);
        for (int i = 0; i < filterCount; i++) filters.add(buf.readUtf(MAX_FILTER_LENGTH));
        String filterDraft = buf.readUtf(MAX_FILTER_LENGTH);
        boolean discard = buf.readBoolean();
        TelemetryTerminalResult result = buf.readEnum(TelemetryTerminalResult.class);
        int inboxCount = Math.min(buf.readVarInt(), MAX_SYNCED_MESSAGES);
        List<GlareMessage> inbox = new ArrayList<>(inboxCount);
        for (int i = 0; i < inboxCount; i++) {
            inbox.add(new GlareMessage(buf.readUUID(), readAddress(buf), readAddress(buf), buf.readUtf(512), buf.readLong()));
        }
        return new TelemetryTerminalSnapshot(revision, mode, compose, own, contacts, manualDestination, manualBody,
                autoDestination, autoBody, displayLinkActive, filters, filterDraft, discard, result, inbox);
    }

    private static void writeAddress(RegistryFriendlyByteBuf buf, GlareAddress address) {
        address.write(buf);
    }

    private static GlareAddress readAddress(RegistryFriendlyByteBuf buf) {
        return GlareAddress.read(buf);
    }
}
