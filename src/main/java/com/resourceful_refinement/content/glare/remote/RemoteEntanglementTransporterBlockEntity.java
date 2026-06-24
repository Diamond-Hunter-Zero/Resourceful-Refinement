package com.resourceful_refinement.content.glare.remote;

import com.resourceful_refinement.content.glare.GlareAddress;
import com.resourceful_refinement.content.glare.GlareOperationStatus;
import com.resourceful_refinement.content.glare.GlareSavedData;
import com.resourceful_refinement.content.glare.GlareSmartNodeBlockEntity;
import com.resourceful_refinement.content.glare.IGlareReceiver;
import com.resourceful_refinement.content.glare.common.TrioAddressSlot;
import com.resourceful_refinement.content.glare.terminal.TelemetryAddressBehaviour;
import com.resourceful_refinement.content.glare.common.Trio;
import com.resourceful_refinement.registry.ModBlocks;
import com.resourceful_refinement.registry.ModEffects;
import com.resourceful_refinement.registry.ModFluids;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Controller and server authority for the 1x2x2 transporter assembly. */
public class RemoteEntanglementTransporterBlockEntity extends GlareSmartNodeBlockEntity
        implements IGlareReceiver, IRemoteEntanglementEndpoint {
    public static final int REQUIRED_LUX = 8;
    public static final int CHORUS_COST = 250;
    public static final int TANK_CAPACITY = 1000;
    public static final int CHARGE_TICKS = 40;
    public static final int COOLDOWN_TICKS = 200;
    public static final int SICKNESS_TICKS = 100;

    public final FluidTank tank = new FluidTank(TANK_CAPACITY) {
        @Override public boolean isFluidValid(FluidStack stack) {
            return stack.getFluid() == ModFluids.LIQUID_CHORUS.source.get();
        }

        @Override protected void onContentsChanged() {
            setChanged();
            sendData();
        }
    };

    private List<TelemetryAddressBehaviour> addressSlots;
    private GlareOperationStatus operationStatus = GlareOperationStatus.ONLINE;
    private RemoteMachineState machineState = RemoteMachineState.IDLE;
    private int stateTicks;
    private boolean assembled;
    private boolean disassembling;
    private UUID chargingPlayer;
    private String lastFailure = "";

    public RemoteEntanglementTransporterBlockEntity(BlockPos pos, BlockState state) {
        super(com.resourceful_refinement.registry.ModBlockEntities.REMOTE_ENTANGLEMENT_TRANSPORTER_BE.get(), pos, state, 1);
    }

    @Override public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        List<TrioAddressSlot> transforms = Trio.makeSlots((index)-> new TrioAddressSlot(index, 12f, 15.9f, 0f));
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
        if (level instanceof ServerLevel server) {
            assembled = validateAssembly();
            pushGlareState();
        }
    }

    @Override public void tick() {
        super.tick();
        if (!(level instanceof ServerLevel server)) return;
        if (!assembled || !validateAssembly()) {
            if (assembled) {
                assembled = false;
                pushGlareState();
            }
            resetCharge();
            return;
        }

        boolean poweredAndChilled = RemoteEntanglementUtil.isOperational(server, getGlareNodePos())
                && RemoteEntanglementUtil.isChilled(server, worldPosition, tankPos());
        if (machineState == RemoteMachineState.COOLDOWN) {
            if (poweredAndChilled && ++stateTicks >= COOLDOWN_TICKS) setMachineState(RemoteMachineState.IDLE);
            return;
        }
        if (!getRemoteMode().canSend()) {
            resetCharge();
            return;
        }

        ServerPlayer player;
        if (machineState == RemoteMachineState.IDLE) {
            player = findEligiblePlayer(server);
            if (player == null) return;
            chargingPlayer = player.getUUID();
            setMachineState(RemoteMachineState.CHARGING);
        } else {
            player = chargingPlayer == null ? null : server.getServer().getPlayerList().getPlayer(chargingPlayer);
        }
        if (poweredAndChilled && ++stateTicks >= CHARGE_TICKS) {
            boolean succeeded = false;
            if (!isEligiblePlayer(player)) {
                lastFailure = "The selected player left before entanglement completed";
            } else if (tank.getFluidAmount() < CHORUS_COST) {
                lastFailure = "Not enough Liquid Chorus (150 mB required)";
            } else {
                succeeded = attemptTeleport(server, player);
            }
            if (succeeded) tank.drain(CHORUS_COST, IFluidHandler.FluidAction.EXECUTE);
            setMachineState(RemoteMachineState.COOLDOWN);
            chargingPlayer = null;
        }
    }

    private boolean attemptTeleport(ServerLevel sourceLevel, ServerPlayer player) {
        for (GlareSavedData.NodeRecord candidate : RemoteEntanglementService.findCandidates(sourceLevel,
                getGlareNodePos(), RemoteEndpointKind.TRANSPORTER, getRemoteAddress())) {
            ServerLevel destinationLevel = sourceLevel.getServer().getLevel(candidate.pos.levelKey());
            if (destinationLevel == null) continue;

            // Persisted endpoint metadata is enough to select a chunk. The chunk is then loaded and the live
            // controller is revalidated before any entity movement occurs.
            destinationLevel.getChunk(candidate.pos.pos());
            BlockEntity blockEntity = destinationLevel.getBlockEntity(candidate.pos.pos());
            if (!(blockEntity instanceof RemoteEntanglementTransporterBlockEntity destination)) continue;
            if (!destination.assembled || !destination.getRemoteMode().canReceive() || !destination.validateAssembly()) continue;
            Vec3 target = destination.arrivalPosition();
            AABB movedBounds = player.getBoundingBox().move(target.subtract(player.position()));
            if (!destinationLevel.noCollision(player, movedBounds)) continue;

            player.teleportTo(destinationLevel, target.x, target.y, target.z, player.getYRot(), player.getXRot());
            player.addEffect(new MobEffectInstance(ModEffects.TELEPORT_SICKNESS, SICKNESS_TICKS, 0, false, false, false));
            lastFailure = "";
            sendData();
            return true;
        }
        lastFailure = "No safe receiving transporter matches this address";
        sendData();
        return false;
    }

    private ServerPlayer findEligiblePlayer(ServerLevel server) {
        List<ServerPlayer> players = server.getEntitiesOfClass(ServerPlayer.class, chamberBounds(), this::isEligiblePlayer);
        return players.isEmpty() ? null : players.get(server.random.nextInt(players.size()));
    }

    private boolean isEligiblePlayer(Player player) {
        return player instanceof ServerPlayer && player.isAlive() && chamberBounds().intersects(player.getBoundingBox())
                && !player.hasEffect(ModEffects.TELEPORT_SICKNESS);
    }

    public boolean tryAssemble() {
        if (!(level instanceof ServerLevel server)) return false;
        List<BlockPos> positions = List.of(tankPos(), chamberPos(), chamberPos().above());
        for (BlockPos pos : positions) {
            if (!server.getBlockState(pos).canBeReplaced()) return false;
        }
        placeProxy(tankPos(), ModBlocks.REMOTE_ENTANGLEMENT_TRANSPORTER_TANK.get().defaultBlockState());
        placeProxy(chamberPos(), ModBlocks.REMOTE_ENTANGLEMENT_TRANSPORTER_CASING.get().defaultBlockState());
        placeProxy(chamberPos().above(), ModBlocks.REMOTE_ENTANGLEMENT_TRANSPORTER_CASING.get().defaultBlockState());
        assembled = validateAssembly();
        pushGlareState();
        return assembled;
    }

    private void placeProxy(BlockPos pos, BlockState state) {
        level.setBlock(pos, state, Block.UPDATE_ALL);
        if (level.getBlockEntity(pos) instanceof RemoteTransporterProxyBlockEntity proxy) proxy.setControllerPos(worldPosition);
    }

    public void removeAssemblyProxies() {
        if (level == null || disassembling) return;
        disassembling = true;
        try {
            removeOwnedProxy(tankPos(), ModBlocks.REMOTE_ENTANGLEMENT_TRANSPORTER_TANK.get());
            removeOwnedProxy(chamberPos(), ModBlocks.REMOTE_ENTANGLEMENT_TRANSPORTER_CASING.get());
            removeOwnedProxy(chamberPos().above(), ModBlocks.REMOTE_ENTANGLEMENT_TRANSPORTER_CASING.get());
            assembled = false;
        } finally {
            disassembling = false;
        }
    }

    private void removeOwnedProxy(BlockPos pos, Block expectedBlock) {
        // An incomplete controller may overlap another transporter's expected assembly positions. Never remove
        // a proxy based only on its block type; its persisted owner must be this controller.
        if (isOwnedProxy(pos, expectedBlock)) level.removeBlock(pos, false);
    }

    public boolean validateAssembly() {
        if (level == null) return false;
        return isOwnedProxy(tankPos(), ModBlocks.REMOTE_ENTANGLEMENT_TRANSPORTER_TANK.get())
                && isOwnedProxy(chamberPos(), ModBlocks.REMOTE_ENTANGLEMENT_TRANSPORTER_CASING.get())
                && isOwnedProxy(chamberPos().above(), ModBlocks.REMOTE_ENTANGLEMENT_TRANSPORTER_CASING.get());
    }

    private boolean isOwnedProxy(BlockPos pos, Block block) {
        if (!level.getBlockState(pos).is(block)) return false;
        return level.getBlockEntity(pos) instanceof RemoteTransporterProxyBlockEntity proxy
                && proxy.getControllerPos().equals(worldPosition);
    }

    public boolean isDisassembling() { return disassembling; }
    public FluidTank getTank() { return tank; }

    private BlockPos tankPos() { return worldPosition.above(); }
    private BlockPos chamberPos() { return worldPosition.relative(getBlockState().getValue(RemoteEntanglementTransporterBlock.FACING)); }
    private AABB chamberBounds() {
        BlockPos chamber = chamberPos();
        return new AABB(chamber.getX(), chamber.getY(), chamber.getZ(),
                chamber.getX() + 1D, chamber.getY() + 2D, chamber.getZ() + 1D);
    }
    private Vec3 arrivalPosition() { return Vec3.atBottomCenterOf(chamberPos()).add(0, .01D, 0); }

    public void onModeChanged() { resetCharge(); pushGlareState(); }
    private void onAddressChanged() { resetCharge(); pushGlareState(); }
    private void resetCharge() {
        chargingPlayer = null;
        if (machineState == RemoteMachineState.CHARGING) setMachineState(RemoteMachineState.IDLE);
    }
    private void setMachineState(RemoteMachineState state) {
        machineState = state;
        stateTicks = 0;
        setChanged();
        sendData();
    }

    @Override public int getAllocatedLux() { return REQUIRED_LUX; }
    @Override public GlareOperationStatus getGlareOperationStatus() { return operationStatus; }
    @Override public void setGlareOperationStatus(GlareOperationStatus status) { operationStatus = status; pushGlareState(); sendData(); }
    @Override public void applyGlareOperationStatusFromNetwork(GlareOperationStatus status) { operationStatus = status; }
    @Override public RemoteEndpointKind getRemoteEndpointKind() { return RemoteEndpointKind.TRANSPORTER; }
    @Override public GlareAddress getRemoteAddress() {
        return GlareAddress.of(addressSlots.get(0).getFilter().getItem(), addressSlots.get(1).getFilter().getItem(),
                addressSlots.get(2).getFilter().getItem());
    }
    @Override public RemoteEntanglementMode getRemoteMode() {
        return getBlockState().getValue(RemoteEntanglementTransporterBlock.MODE).remoteMode();
    }
    @Override public boolean isRemoteEndpointAssembled() { return assembled; }

    @Override protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.put("Tank", tank.writeToNBT(registries, new CompoundTag()));
        tag.putBoolean("Assembled", assembled);
        tag.putString("OperationStatus", operationStatus.name());
        tag.putString("MachineState", machineState.name());
        tag.putInt("StateTicks", stateTicks);
        if (chargingPlayer != null) tag.putUUID("ChargingPlayer", chargingPlayer);
        tag.putString("LastFailure", lastFailure);
    }

    @Override protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        if (tag.contains("Tank")) tank.readFromNBT(registries, tag.getCompound("Tank"));
        assembled = tag.getBoolean("Assembled");
        try { operationStatus = GlareOperationStatus.valueOf(tag.getString("OperationStatus")); }
        catch (IllegalArgumentException ignored) { operationStatus = GlareOperationStatus.ONLINE; }
        try { machineState = RemoteMachineState.valueOf(tag.getString("MachineState")); }
        catch (IllegalArgumentException ignored) { machineState = RemoteMachineState.IDLE; }
        stateTicks = Math.max(0, tag.getInt("StateTicks"));
        chargingPlayer = tag.hasUUID("ChargingPlayer") ? tag.getUUID("ChargingPlayer") : null;
        lastFailure = tag.getString("LastFailure");
    }

    @Override public boolean addToGoggleTooltip(List<Component> tooltip, boolean sneaking) {
        super.addToGoggleTooltip(tooltip, sneaking);
        tooltip.add(Component.literal("Mode: " + getBlockState().getValue(RemoteEntanglementTransporterBlock.MODE).getSerializedName()));
        tooltip.add(Component.literal("Liquid Chorus: " + tank.getFluidAmount() + "/" + TANK_CAPACITY + " mB"));
        tooltip.add(Component.literal(assembled ? "Assembly: Complete" : "Assembly: Incomplete"));
        tooltip.add(Component.literal("State: " + machineState.name()));
        if (!lastFailure.isEmpty()) tooltip.add(Component.literal(lastFailure));
        return true;
    }
}
