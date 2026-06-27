package com.resourceful_refinement.content.glare.remote;

import com.resourceful_refinement.content.glare.GlareAddress;
import com.resourceful_refinement.content.glare.DimensionalNodePos;
import com.resourceful_refinement.content.glare.GlareOperationStatus;
import com.resourceful_refinement.content.glare.GlareSavedData;
import com.resourceful_refinement.content.glare.GlareService;
import com.resourceful_refinement.content.glare.IGlareNode;
import com.resourceful_refinement.content.glare.IGlareReceiver;
import com.resourceful_refinement.content.glare.common.TrioAddressSlot;
import com.resourceful_refinement.content.glare.terminal.TelemetryAddressBehaviour;
import com.resourceful_refinement.content.glare.common.Trio;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.logistics.depot.DepotBehaviour;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Depot transport is deliberately atomic: a source stack is cleared only after a loaded receiver has accepted the
 * whole stack. Persisted endpoint records are used for discovery, never as a substitute for a live inventory.
 */
public class RemoteEntanglerDepotBlockEntity extends DepotBlockEntity implements IGlareNode, IGlareReceiver,
        IRemoteEntanglementEndpoint, IHaveGoggleInformation {
    public static final int REQUIRED_LUX = 4;
    public static final int CHARGE_TICKS = 40;
    public static final int COOLDOWN_TICKS = 160;

    private List<TelemetryAddressBehaviour> addressSlots;
    private UUID networkId;
    private GlareOperationStatus operationStatus = GlareOperationStatus.ONLINE;
    private RemoteMachineState machineState = RemoteMachineState.IDLE;
    private int stateTicks;
    private String lastFailure = "";

    public RemoteEntanglerDepotBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        List<TrioAddressSlot> transforms = Trio.makeSlots((index)-> new TrioAddressSlot(index, 8f, 15.9f, 0f));
        addressSlots = new ArrayList<>(3);
        for (int slot = 0; slot < 3; slot++) {
            TelemetryAddressBehaviour behaviour = new TelemetryAddressBehaviour(this, transforms.get(slot), slot);
            behaviour.withCallback(ignored -> onAddressChanged());
            addressSlots.add(behaviour);
            behaviours.add(behaviour);
        }
    }

    @Override public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel server) GlareService.onNodeLoaded(server, this);
    }

    @Override public void tick() {
        super.tick();
        if (!(level instanceof ServerLevel server)) return;

        boolean canRun = RemoteEntanglementUtil.isOperational(server, getGlareNodePos())
                && RemoteEntanglementUtil.isChilled(server, worldPosition);
        if (machineState == RemoteMachineState.COOLDOWN) {
            if (canRun && ++stateTicks >= COOLDOWN_TICKS) setMachineState(RemoteMachineState.IDLE);
            return;
        }
        if (!getRemoteMode().canSend() || !getRemoteAddress().isComplete()) {
            if (machineState == RemoteMachineState.CHARGING) setMachineState(RemoteMachineState.IDLE);
            return;
        }
        if (machineState == RemoteMachineState.IDLE) {
            if (getHeldItem().isEmpty()) return;
            setMachineState(RemoteMachineState.CHARGING);
        }
        if (canRun && ++stateTicks >= CHARGE_TICKS) {
            attemptTransfer(server);
            setMachineState(RemoteMachineState.COOLDOWN);
        }
    }

    private void attemptTransfer(ServerLevel server) {
        ItemStack held = getHeldItem();
        if (held.isEmpty()) {
            lastFailure = "The item stack was removed before entanglement completed";
            notifyUpdate();
            return;
        }
        for (GlareSavedData.NodeRecord candidate : RemoteEntanglementService.findCandidates(server, getGlareNodePos(),
                RemoteEndpointKind.DEPOT, getRemoteAddress())) {
            if (!candidate.pos.levelKey().equals(server.dimension()) || !server.isLoaded(candidate.pos.pos())) continue;
            if (!(server.getBlockEntity(candidate.pos.pos()) instanceof RemoteEntanglerDepotBlockEntity receiver)) continue;
            if (!receiver.getRemoteMode().canReceive() || !receiver.getHeldItem().isEmpty()) continue;
            receiver.setHeldItem(held.copy());
            getDepotBehaviour().removeHeldItem();
            receiver.notifyUpdate();
            notifyUpdate();
            lastFailure = "";
            return;
        }
        lastFailure = "No loaded, empty receiving depot matches this address";
        notifyUpdate();
    }

    public void onModeChanged() {
        setMachineState(RemoteMachineState.IDLE);
        pushRemoteState();
    }

    private void onAddressChanged() {
        setMachineState(RemoteMachineState.IDLE);
        pushRemoteState();
    }

    private void setMachineState(RemoteMachineState state) {
        machineState = state;
        stateTicks = 0;
        setChanged();
        sendData();
    }

    private void pushRemoteState() {
        if (level instanceof ServerLevel server) GlareService.updateNodeState(server, this);
        setChanged();
        sendData();
    }

    public DepotBehaviour getDepotBehaviour() { return getBehaviour(DepotBehaviour.TYPE); }
    public IItemHandler getItemHandler() { return getDepotBehaviour().itemHandler; }

    @Override public int getMaxGlareLinks() { return 1; }
    @Override public DimensionalNodePos getGlareNodePos() { return DimensionalNodePos.of(level, worldPosition); }
    @Override public void onGlareNetworkChanged(ServerLevel level, UUID id) { networkId = id; sendData(); }
    @Override public void onGlareLinksChanged(ServerLevel level) { sendData(); }
    @Override public int getAllocatedLux() { return REQUIRED_LUX; }
    @Override public GlareOperationStatus getGlareOperationStatus() { return operationStatus; }
    @Override public void setGlareOperationStatus(GlareOperationStatus status) { operationStatus = status; pushRemoteState(); }
    @Override public void applyGlareOperationStatusFromNetwork(GlareOperationStatus status) { operationStatus = status; }
    @Override public RemoteEndpointKind getRemoteEndpointKind() { return RemoteEndpointKind.DEPOT; }
    @Override public GlareAddress getRemoteAddress() {
        return GlareAddress.of(addressSlots.get(0).getFilter().getItem(), addressSlots.get(1).getFilter().getItem(),
                addressSlots.get(2).getFilter().getItem());
    }
    @Override public RemoteEntanglementMode getRemoteMode() {
        return getBlockState().getValue(RemoteEntanglerDepotBlock.RECEIVING)
                ? RemoteEntanglementMode.DEPOT_RECEIVE : RemoteEntanglementMode.DEPOT_SEND;
    }

    @Override protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        if (networkId != null) tag.putUUID("GlareNetwork", networkId);
        tag.putString("OperationStatus", operationStatus.name());
        tag.putString("MachineState", machineState.name());
        tag.putInt("StateTicks", stateTicks);
        tag.putString("LastFailure", lastFailure);
    }

    @Override protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        networkId = tag.hasUUID("GlareNetwork") ? tag.getUUID("GlareNetwork") : null;
        try { operationStatus = GlareOperationStatus.valueOf(tag.getString("OperationStatus")); }
        catch (IllegalArgumentException ignored) { operationStatus = GlareOperationStatus.ONLINE; }
        try { machineState = RemoteMachineState.valueOf(tag.getString("MachineState")); }
        catch (IllegalArgumentException ignored) { machineState = RemoteMachineState.IDLE; }
        stateTicks = Math.max(0, tag.getInt("StateTicks"));
        lastFailure = tag.getString("LastFailure");
    }

    @Override public boolean addToGoggleTooltip(List<Component> tooltip, boolean sneaking) {
        tooltip.add(Component.literal("     Remote Entangler Depot"));
        tooltip.add(Component.literal("Mode: " + (getRemoteMode().canReceive() ? "Receive" : "Send")));
        tooltip.add(Component.literal("State: " + machineState.name()));
        if (!lastFailure.isEmpty()) tooltip.add(Component.literal(lastFailure));
        return true;
    }
}
