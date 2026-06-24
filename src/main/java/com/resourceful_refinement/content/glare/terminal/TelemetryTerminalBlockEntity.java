package com.resourceful_refinement.content.glare.terminal;

import com.resourceful_refinement.content.glare.GlareAddress;
import com.resourceful_refinement.content.glare.GlareMessage;
import com.resourceful_refinement.content.glare.GlareSmartNodeBlockEntity;
import com.resourceful_refinement.content.glare.GlareNodePos;
import com.resourceful_refinement.content.glare.GlareOperationStatus;
import com.resourceful_refinement.content.glare.IGlareReceiver;
import com.resourceful_refinement.content.glare.IGlareTelemetryEndpoint;
import com.resourceful_refinement.content.glare.TelemetryService;
import com.resourceful_refinement.content.glare.common.Trio;
import com.resourceful_refinement.content.glare.common.TrioAddressSlot;
import com.resourceful_refinement.network.TelemetryTerminalStatePayload;
import com.resourceful_refinement.registry.ModBlockEntities;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * Server authority for one terminal. Persistent user configuration lives here; inbox contents remain owned by
 * {@link com.resourceful_refinement.content.glare.GlareSavedData}. Every mutation broadcasts a revisioned snapshot
 * to all open viewers, while loaded-only subscriptions drive automatic receive behavior.
 */
public class TelemetryTerminalBlockEntity extends GlareSmartNodeBlockEntity
        implements IGlareReceiver, IGlareTelemetryEndpoint, MenuProvider {
    public static final int MAX_LINK_COUNT = 1;
    public static final GlareAddress DEFAULT_GUI_ADDRESS = GlareAddress.of(
            Blocks.GRASS_BLOCK.asItem(), Blocks.GRASS_BLOCK.asItem(), Blocks.GRASS_BLOCK.asItem());

    // Persistent address and per-mode user configuration.
    private List<TelemetryAddressBehaviour> addressSlots;
    private final List<GlareAddress> contacts = new ArrayList<>();
    private final List<String> receiveFilters = new ArrayList<>();
    private GlareOperationStatus status = GlareOperationStatus.ONLINE;
    private TelemetryTerminalMode mode = TelemetryTerminalMode.MANUAL;
    private TelemetryTerminalResult lastResult = TelemetryTerminalResult.NONE;
    private GlareAddress manualDestination = DEFAULT_GUI_ADDRESS;
    private GlareAddress autoSendDestination = DEFAULT_GUI_ADDRESS;
    private String manualBody = "";
    private String autoSendBody = "";
    private String displayLinkText = "";
    private final Map<BlockPos, DisplayReadout> displayReadouts = new HashMap<>();
    private String receiveFilterDraft = "";
    private boolean manualComposeOpen;
    private boolean discardMatchingMessages;
    // Runtime automation state. Subscriptions and pulses are deliberately never persisted across unloaded chunks.
    private boolean wasRedstonePowered;
    private int pulseTicks;
    private long lastPulseGameTime = Long.MIN_VALUE;
    private int revision;
    private TelemetryService.Subscription inboxSubscription;

    public TelemetryTerminalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GLARE_TELEMETRY_TERMINAL_BE.get(), pos, state, MAX_LINK_COUNT);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        List<TrioAddressSlot> transforms = Trio.makeSlots((index)-> new TrioAddressSlot(index, 15.9f, 8f, 90f));
        addressSlots = new ArrayList<>(3);
        for (int slot = 0; slot < 3; slot++) {
            TelemetryAddressBehaviour behaviour = new TelemetryAddressBehaviour(this, transforms.get(slot), slot);
            behaviour.withCallback(ignored -> onAddressChanged());
            addressSlots.add(behaviour);
            behaviours.add(behaviour);
        }
    }

    @Override public int getAllocatedLux() { return 0; }
    @Override public GlareOperationStatus getGlareOperationStatus() { return status; }
    @Override public void setGlareOperationStatus(GlareOperationStatus status) { this.status = status; pushGlareState(); sendData(); }
    @Override public void applyGlareOperationStatusFromNetwork(GlareOperationStatus status) { this.status = status; }

    @Override
    public GlareAddress getTelemetryAddress() {
        return GlareAddress.of(getAddressStack(0).getItem(), getAddressStack(1).getItem(), getAddressStack(2).getItem());
    }

    public ItemStack getAddressStack(int slot) { return addressSlots.get(slot).getFilter(); }
    public void setAddressSlot(int slot, ItemStack stack) { addressSlots.get(slot).setFilter(stack); }
    public TelemetryTerminalMode getMode() { return mode; }
    public boolean isPulsing() { return pulseTicks > 0; }
    public long getLastPulseGameTime() { return lastPulseGameTime; }
    public int getRevision() { return revision; }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel server) {
            wasRedstonePowered = server.hasNeighborSignal(worldPosition);
            refreshSubscription(server);
        }
    }

    @Override
    public void remove() {
        closeSubscription();
        super.remove();
    }

    @Override
    public void onGlareNetworkChanged(ServerLevel level, UUID networkId) {
        super.onGlareNetworkChanged(level, networkId);
        refreshSubscription(level);
        broadcastSnapshot();
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide) return;
        if (pulseTicks > 0 && --pulseTicks == 0) {
            updateRedstoneOutput();
        }
        if (level.getGameTime() % 20L == 0L) refreshDisplayLinkText();
    }

    public void onNeighborChanged() {
        if (!(level instanceof ServerLevel server)) return;
        boolean powered = server.hasNeighborSignal(worldPosition);
        if (powered && !wasRedstonePowered && mode == TelemetryTerminalMode.AUTO_SEND) {
            performAutoSend(server);
        }
        wasRedstonePowered = powered;
    }

    public void setMode(TelemetryTerminalMode mode) {
        if (this.mode == mode) return;
        this.mode = mode;
        lastResult = TelemetryTerminalResult.NONE;
        changed(true);
    }

    public void cycleMode() {
        TelemetryTerminalMode[] values = TelemetryTerminalMode.values();
        setMode(values[(mode.ordinal() + 1) % values.length]);
    }

    public void setManualComposeOpen(boolean composeOpen) {
        if (manualComposeOpen == composeOpen) return;
        manualComposeOpen = composeOpen;
        changed(false);
    }

    public void setDraft(boolean automatic, GlareAddress destination, String body) {
        String safeBody = sanitiseBody(body);
        if (automatic) {
            autoSendDestination = destination;
            autoSendBody = safeBody;
        } else {
            manualDestination = destination;
            manualBody = safeBody;
        }
        changed(false);
    }

    public void addContact(GlareAddress address) {
        if (!address.isComplete() || contacts.contains(address) || contacts.size() >= TelemetryTerminalSnapshot.MAX_CONTACTS) return;
        contacts.add(address);
        changed(false);
    }

    public void removeContact(GlareAddress address) {
        if (contacts.remove(address)) changed(false);
    }

    public void addFilter(String raw) {
        String filter = sanitiseFilter(raw);
        if (filter.isEmpty() || receiveFilters.contains(filter) || receiveFilters.size() >= TelemetryTerminalSnapshot.MAX_FILTERS) return;
        receiveFilters.add(filter);
        receiveFilterDraft = "";
        changed(false);
    }

    public void setReceiveFilterDraft(String raw) {
        String draft = raw == null ? "" : raw.substring(0, Math.min(raw.length(), TelemetryTerminalSnapshot.MAX_FILTER_LENGTH));
        if (receiveFilterDraft.equals(draft)) return;
        receiveFilterDraft = draft;
        changed(false);
    }

    public void removeFilter(String raw) {
        if (receiveFilters.remove(sanitiseFilter(raw))) changed(false);
    }

    public void setDiscardMatchingMessages(boolean discard) {
        if (discardMatchingMessages == discard) return;
        discardMatchingMessages = discard;
        changed(false);
    }

    public void sendManual() {
        if (!(level instanceof ServerLevel server)) return;
        lastResult = send(server, manualDestination, manualBody);
        if (lastResult == TelemetryTerminalResult.MESSAGE_SENT) {
            manualDestination = DEFAULT_GUI_ADDRESS;
            manualBody = "";
        }
        changed(false);
    }

    public void discardMessage(UUID messageId) {
        if (!(level instanceof ServerLevel server) || getNetworkId() == null) return;
        TelemetryService.discard(server, getNetworkId(), getTelemetryAddress(), messageId);
        changed(false);
    }

    public void setDisplayLinkText(String text) {
        displayLinkText = sanitiseBody(text);
        changed(false);
    }

    public void acceptDisplayLinkText(BlockPos source, String text) {
        if (!(level instanceof ServerLevel server)) return;
        displayReadouts.put(source.immutable(), new DisplayReadout(sanitiseBody(text), server.getGameTime()));
        refreshDisplayLinkText();
    }

    public TelemetryTerminalSnapshot snapshot() {
        List<GlareMessage> inbox = level instanceof ServerLevel server && getNetworkId() != null
                ? TelemetryService.read(server, getNetworkId(), getTelemetryAddress()) : List.of();
        return new TelemetryTerminalSnapshot(revision, mode, manualComposeOpen, getTelemetryAddress(), contacts,
                manualDestination, manualBody, autoSendDestination, effectiveAutoSendBody(), !displayReadouts.isEmpty(), receiveFilters,
                receiveFilterDraft, discardMatchingMessages, lastResult, inbox);
    }

    private void performAutoSend(ServerLevel server) {
        lastResult = send(server, autoSendDestination, effectiveAutoSendBody());
        changed(false);
    }

    private TelemetryTerminalResult send(ServerLevel server, GlareAddress destination, String body) {
        if (!getTelemetryAddress().isComplete() || !destination.isComplete() || body.isBlank() || getNetworkId() == null) return TelemetryTerminalResult.INVALID_CONFIGURATION;
        if (!TelemetryService.addressExists(server, getNetworkId(), destination)) return TelemetryTerminalResult.ADDRESS_NOT_FOUND;
        return TelemetryService.send(server, getNetworkId(), getTelemetryAddress(), destination, body) == TelemetryService.SendResult.SENT
                ? TelemetryTerminalResult.MESSAGE_SENT : TelemetryTerminalResult.ADDRESS_NOT_FOUND;
    }

    private String effectiveAutoSendBody() {
        return displayLinkText.isEmpty() ? autoSendBody : displayLinkText;
    }

    private void refreshDisplayLinkText() {
        if (!(level instanceof ServerLevel server)) return;
        long cutoff = server.getGameTime() - 100L;
        displayReadouts.values().removeIf(readout -> readout.gameTime < cutoff);
        String combined = displayReadouts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(java.util.Comparator.comparingLong(BlockPos::asLong)))
                .map(entry -> entry.getValue().text())
                .filter(text -> !text.isBlank()).reduce((first, second) -> first + "\n" + second).orElse("");
        combined = sanitiseBody(combined);
        if (!displayLinkText.equals(combined)) {
            displayLinkText = combined;
            changed(false);
        }
    }

    private void onAddressChanged() {
        if (!(level instanceof ServerLevel server)) return;
        pushGlareState();
        refreshSubscription(server);
        changed(false);
    }

    private void refreshSubscription(ServerLevel server) {
        closeSubscription();
        if (!getTelemetryAddress().isComplete()) return;
        inboxSubscription = TelemetryService.subscribe(server, GlareNodePos.of(server, worldPosition), getTelemetryAddress(), this::onInboxUpdate);
    }

    private void closeSubscription() {
        if (inboxSubscription != null) inboxSubscription.close();
        inboxSubscription = null;
    }

    private void onInboxUpdate(TelemetryService.InboxUpdate update) {
        broadcastSnapshot();
        GlareMessage message = update.changedMessage();
        if (mode != TelemetryTerminalMode.AUTO_RECEIVE || update.mutation() != TelemetryService.Mutation.SENT
                || message == null || !matchesFilter(message.body())
                || !(level instanceof ServerLevel server)) return;
        // Defer mutation so every terminal subscribed to this inbox observes the same message before any discard.
        int runAt = server.getServer().getTickCount() + 1;
        server.getServer().tell(new TickTask(runAt, () -> {
            if (isRemoved() || level != server || mode != TelemetryTerminalMode.AUTO_RECEIVE) return;
            pulseTicks = 2;
            lastPulseGameTime = server.getGameTime();
            updateRedstoneOutput();
            if (discardMatchingMessages && getNetworkId() != null) {
                TelemetryService.discard(server, getNetworkId(), getTelemetryAddress(), message.id());
            }
        }));
    }

    private boolean matchesFilter(String body) {
        String normalised = body.toLowerCase(Locale.ROOT);
        return receiveFilters.stream().anyMatch(normalised::contains);
    }

    private void updateRedstoneOutput() {
        if (level == null) return;
        level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        level.updateNeighborsAt(worldPosition.below(), getBlockState().getBlock());
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    private void changed(boolean refreshSubscription) {
        revision++;
        setChanged();
        if (level instanceof ServerLevel server) {
            if (refreshSubscription) refreshSubscription(server);
            sendData();
            broadcastSnapshot();
        }
    }

    private void broadcastSnapshot() {
        if (level instanceof ServerLevel server) TelemetryTerminalStatePayload.broadcast(server, this);
    }

    private static String sanitiseBody(String body) {
        if (body == null) return "";
        return body.substring(0, Math.min(body.length(), GlareMessage.MAX_BODY_LENGTH));
    }

    private static String sanitiseFilter(String filter) {
        if (filter == null) return "";
        String value = filter.strip().toLowerCase(Locale.ROOT);
        return value.substring(0, Math.min(value.length(), TelemetryTerminalSnapshot.MAX_FILTER_LENGTH));
    }

    private record DisplayReadout(String text, long gameTime) {}

    @Override public Component getDisplayName() { return Component.translatable("block.resourceful_refinement.glare_telemetry_terminal"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) { return new TelemetryTerminalMenu(id, inventory, this); }
    @Override public void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buf) { TelemetryTerminalMenu.writeClientSideData(buf, this); }
    public boolean isWithinUsableDistance(Player player) { return player.distanceToSqr(worldPosition.getCenter()) <= 64.0; }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putString("Mode", mode.name());
        tag.putString("Status", status.name());
        tag.putString("LastResult", lastResult.name());
        tag.put("ManualDestination", manualDestination.save());
        tag.putString("ManualBody", manualBody);
        tag.put("AutoDestination", autoSendDestination.save());
        tag.putString("AutoBody", autoSendBody);
        tag.putString("DisplayLinkText", displayLinkText);
        tag.putString("ReceiveFilterDraft", receiveFilterDraft);
        tag.putBoolean("ManualComposeOpen", manualComposeOpen);
        tag.putBoolean("DiscardMatching", discardMatchingMessages);
        tag.putBoolean("WasRedstonePowered", wasRedstonePowered);
        tag.putInt("Revision", revision);
        ListTag contactTags = new ListTag();
        contacts.forEach(address -> contactTags.add(address.save()));
        tag.put("Contacts", contactTags);
        ListTag filterTags = new ListTag();
        for (String filter : receiveFilters) {
            CompoundTag filterTag = new CompoundTag();
            filterTag.putString("Value", filter);
            filterTags.add(filterTag);
        }
        tag.put("ReceiveFilters", filterTags);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        if (tag.contains("AddressInventory", Tag.TAG_COMPOUND) && !tag.contains("TelemetryAddressSlot0", Tag.TAG_COMPOUND)) {
            ItemStackHandler legacyAddress = new ItemStackHandler(3);
            legacyAddress.deserializeNBT(registries, tag.getCompound("AddressInventory"));
            for (int slot = 0; slot < 3; slot++) {
                CompoundTag migratedRoot = new CompoundTag();
                CompoundTag migratedSlot = new CompoundTag();
                migratedSlot.put("Filter", legacyAddress.getStackInSlot(slot).copyWithCount(1).saveOptional(registries));
                migratedSlot.putInt("FilterAmount", 64);
                migratedSlot.putBoolean("UpTo", true);
                migratedRoot.put("TelemetryAddressSlot" + slot, migratedSlot);
                addressSlots.get(slot).read(migratedRoot, registries, clientPacket);
            }
        }
        try { mode = TelemetryTerminalMode.valueOf(tag.getString("Mode")); } catch (IllegalArgumentException ignored) {}
        try { status = GlareOperationStatus.valueOf(tag.getString("Status")); } catch (IllegalArgumentException ignored) {}
        try { lastResult = TelemetryTerminalResult.valueOf(tag.getString("LastResult")); } catch (IllegalArgumentException ignored) {}
        if (tag.contains("ManualDestination", Tag.TAG_COMPOUND)) manualDestination = GlareAddress.load(tag.getCompound("ManualDestination"));
        if (tag.contains("AutoDestination", Tag.TAG_COMPOUND)) autoSendDestination = GlareAddress.load(tag.getCompound("AutoDestination"));
        if (manualDestination.equals(GlareAddress.empty())) manualDestination = DEFAULT_GUI_ADDRESS;
        if (autoSendDestination.equals(GlareAddress.empty())) autoSendDestination = DEFAULT_GUI_ADDRESS;
        manualBody = sanitiseBody(tag.getString("ManualBody"));
        autoSendBody = sanitiseBody(tag.getString("AutoBody"));
        displayLinkText = sanitiseBody(tag.getString("DisplayLinkText"));
        receiveFilterDraft = tag.getString("ReceiveFilterDraft");
        if (receiveFilterDraft.length() > TelemetryTerminalSnapshot.MAX_FILTER_LENGTH) {
            receiveFilterDraft = receiveFilterDraft.substring(0, TelemetryTerminalSnapshot.MAX_FILTER_LENGTH);
        }
        manualComposeOpen = tag.getBoolean("ManualComposeOpen");
        discardMatchingMessages = tag.getBoolean("DiscardMatching");
        wasRedstonePowered = tag.getBoolean("WasRedstonePowered");
        revision = tag.getInt("Revision");
        contacts.clear();
        ListTag contactTags = tag.getList("Contacts", Tag.TAG_COMPOUND);
        for (int i = 0; i < Math.min(contactTags.size(), TelemetryTerminalSnapshot.MAX_CONTACTS); i++) contacts.add(GlareAddress.load(contactTags.getCompound(i)));
        receiveFilters.clear();
        ListTag filterTags = tag.getList("ReceiveFilters", Tag.TAG_COMPOUND);
        for (int i = 0; i < Math.min(filterTags.size(), TelemetryTerminalSnapshot.MAX_FILTERS); i++) {
            String filter = sanitiseFilter(filterTags.getCompound(i).getString("Value"));
            if (!filter.isEmpty() && !receiveFilters.contains(filter)) receiveFilters.add(filter);
        }
    }
}
