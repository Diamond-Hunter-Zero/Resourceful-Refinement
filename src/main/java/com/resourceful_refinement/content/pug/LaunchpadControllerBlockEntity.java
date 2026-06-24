package com.resourceful_refinement.content.pug;

import com.resourceful_refinement.config.ServerConfig;
import com.resourceful_refinement.content.glare.GlareAddress;
import com.resourceful_refinement.content.glare.common.Trio;
import com.resourceful_refinement.content.glare.common.TrioAddressSlot;
import com.resourceful_refinement.content.glare.terminal.TelemetryAddressBehaviour;
import com.resourceful_refinement.registry.ModBlockEntities;
import com.resourceful_refinement.registry.ModBlocks;
import com.resourceful_refinement.network.LaunchpadStatePayload;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

    public class LaunchpadControllerBlockEntity extends SmartBlockEntity implements MenuProvider {
    public static final int INVENTORY_SLOTS = 12;
    public static final int CARGO_SLOTS = 6;
    public static final int SKY_CHECK_INTERVAL = 20;

    public final ItemStackHandler inventory = new ItemStackHandler(INVENTORY_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            syncData();
        }
    };

    public final FluidTank fuelTank = new FluidTank(ServerConfig.PUG_TANK_CAPACITY_MB.get()) {
        @Override
        public boolean isFluidValid(FluidStack stack) {
            return !stack.isEmpty() && stack.is(PugTags.CARBORAX_FUEL);
        }

        @Override
        protected void onContentsChanged() {
            syncData();
        }
    };

    private final IFluidHandler fuelInsertionHandler = new FuelInsertionHandler();
    private final IItemHandler itemAutomationHandler = new ArrivalAwareItemHandler();

    private List<TelemetryAddressBehaviour> addressSlots;
    private LaunchpadConfiguration configuration = LaunchpadConfiguration.defaults();
    private boolean assembled;
    private boolean disassembling;
    private boolean needsAssemblyValidation = true;
    private boolean skyClear;
    private int skyCheckTicks = SKY_CHECK_INTERVAL;
    private int roundRobinCursor;
    private boolean wasRedstonePowered;
    private boolean pendingRedstoneLaunch;
    private int timerTicksElapsed;
    private boolean inventoryDropped;
    private UUID dockedPugId;
    private UUID claimedFlightId;
    private int missingDockedPugTicks;
    private int missingClaimedPugTicks;

    public LaunchpadControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LAUNCHPAD_CONTROLLER_BE.get(), pos, state);
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

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            wasRedstonePowered = level.hasNeighborSignal(worldPosition);
            needsAssemblyValidation = true;
            skyCheckTicks = SKY_CHECK_INTERVAL;
        }
    }

    public void serverTick() {
        if (level == null || level.isClientSide) return;
        if (needsAssemblyValidation) {
            needsAssemblyValidation = false;
            setAssembled(validateAssembly());
        } else if (assembled && level.getGameTime() % SKY_CHECK_INTERVAL == 0 && !validateAssembly()) {
            removeAssemblyProxies();
        }

        updateRedstoneState(level.hasNeighborSignal(worldPosition));
        tickLaunchTimer();
        if (++skyCheckTicks >= SKY_CHECK_INTERVAL) {
            skyCheckTicks = 0;
            boolean current = scanClearSky();
            if (current != skyClear) {
                skyClear = current;
                syncData();
                PugService.syncController(this);
            }
        }
        PugFlightService.tickController(this);
        if (level.getGameTime() % 20L == 0L && level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            LaunchpadStatePayload.broadcast(serverLevel, this);
        }
    }

    public LaunchpadConfiguration getConfiguration() {
        return configuration;
    }

    public void applyConfiguration(LaunchpadConfiguration configuration) {
        boolean resetTriggerState = this.configuration.mode() != configuration.mode()
                || this.configuration.launchCondition() != configuration.launchCondition()
                || this.configuration.timerSeconds() != configuration.timerSeconds();
        this.configuration = configuration;
        if (resetTriggerState) {
            timerTicksElapsed = 0;
            pendingRedstoneLaunch = false;
        }
        syncData();
        if (assembled) PugService.syncController(this);
    }

    public LaunchpadMode toggleMode() {
        LaunchpadMode next = configuration.mode() == LaunchpadMode.SEND
                ? LaunchpadMode.RECEIVE : LaunchpadMode.SEND;
        applyConfiguration(new LaunchpadConfiguration(next, configuration.localAddress(),
                configuration.destinationAddress(), configuration.launchCondition(), configuration.timerSeconds()));
        return next;
    }

    public boolean tryAssemble() {
        if (level == null || level.isClientSide) return false;
        Direction facing = getFacing();
        List<LaunchpadStructure.ProxyPosition> positions = LaunchpadStructure.proxyPositions(worldPosition, facing);
        for (LaunchpadStructure.ProxyPosition proxy : positions) {
            if (isOwnedProxy(proxy)) continue;
            if (!level.getBlockState(proxy.pos()).canBeReplaced()) {
                setAssembled(false);
                return false;
            }
        }

        List<BlockPos> newlyPlaced = new ArrayList<>();
        for (LaunchpadStructure.ProxyPosition proxy : positions) {
            if (isOwnedProxy(proxy)) continue;
            if (!level.setBlock(proxy.pos(), ModBlocks.LAUNCHPAD_PROXY.get().defaultBlockState(), Block.UPDATE_ALL)
                    || !(level.getBlockEntity(proxy.pos()) instanceof LaunchpadProxyBlockEntity proxyBlockEntity)) {
                rollbackNewProxies(newlyPlaced);
                setAssembled(false);
                return false;
            }
            proxyBlockEntity.setControllerData(worldPosition, proxy.lateral(), proxy.depth());
            level.invalidateCapabilities(proxy.pos());
            newlyPlaced.add(proxy.pos());
        }

        boolean valid = validateAssembly();
        if (!valid) rollbackNewProxies(newlyPlaced);
        setAssembled(valid);
        skyClear = scanClearSky();
        skyCheckTicks = 0;
        syncData();
        return valid;
    }

    private void rollbackNewProxies(List<BlockPos> positions) {
        disassembling = true;
        try {
            for (BlockPos pos : positions) level.removeBlock(pos, false);
        } finally {
            disassembling = false;
        }
    }

    public boolean validateAssembly() {
        if (level == null) return false;
        for (LaunchpadStructure.ProxyPosition proxy : LaunchpadStructure.proxyPositions(worldPosition, getFacing())) {
            if (!isOwnedProxy(proxy)) return false;
        }
        return true;
    }

    private boolean isOwnedProxy(LaunchpadStructure.ProxyPosition expected) {
        if (level == null || !level.getBlockState(expected.pos()).is(ModBlocks.LAUNCHPAD_PROXY.get())) return false;
        return level.getBlockEntity(expected.pos()) instanceof LaunchpadProxyBlockEntity proxy
                && proxy.belongsTo(worldPosition, expected.lateral(), expected.depth());
    }

    public void removeAssemblyProxies() {
        if (level == null || disassembling) return;
        disassembling = true;
        try {
            for (LaunchpadStructure.ProxyPosition expected : LaunchpadStructure.proxyPositions(worldPosition, getFacing())) {
                if (isOwnedProxy(expected)) {
                    level.removeBlock(expected.pos(), false);
                    level.invalidateCapabilities(expected.pos());
                }
            }
            setAssembled(false);
        } finally {
            disassembling = false;
        }
    }

    public boolean isDisassembling() {
        return disassembling;
    }

    public boolean isAssembled() {
        return assembled;
    }

    private void setAssembled(boolean assembled) {
        boolean changed = this.assembled != assembled;
        this.assembled = assembled;
        if (level != null) {
            BlockState state = level.getBlockState(worldPosition);
            if (state.is(ModBlocks.LAUNCHPAD_CONTROLLER.get())
                    && state.getValue(LaunchpadControllerBlock.ASSEMBLED) != assembled) {
                level.setBlock(worldPosition, state.setValue(LaunchpadControllerBlock.ASSEMBLED, assembled),
                        Block.UPDATE_CLIENTS);
            }
        }
        if (changed) syncData();
        if (level instanceof net.minecraft.server.level.ServerLevel) PugService.syncController(this);
    }

    public boolean hasClearSky() {
        skyClear = scanClearSky();
        skyCheckTicks = 0;
        return skyClear;
    }

    public boolean isSkyClearCached() {
        return skyClear;
    }

    private boolean scanClearSky() {
        if (level == null || !assembled) return false;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (BlockPos footprintPos : LaunchpadStructure.footprint(worldPosition, getFacing())) {
            for (int y = footprintPos.getY() + 1; y < level.getMaxBuildHeight(); y++) {
                cursor.set(footprintPos.getX(), y, footprintPos.getZ());
                if (!level.getBlockState(cursor).isAir()) return false;
            }
        }
        return true;
    }

    public void updateRedstoneState(boolean powered) {
        if (powered && !wasRedstonePowered) pendingRedstoneLaunch = true;
        if (powered != wasRedstonePowered) {
            wasRedstonePowered = powered;
            syncData();
        }
    }

    public boolean hasPendingRedstoneLaunch() {
        return pendingRedstoneLaunch;
    }

    public boolean consumePendingRedstoneLaunch() {
        if (!pendingRedstoneLaunch) return false;
        pendingRedstoneLaunch = false;
        syncData();
        return true;
    }

    private void tickLaunchTimer() {
        if (!assembled || !configuration.mode().canSend()
                || configuration.launchCondition() != LaunchCondition.TIMER || !hasCargo()) {
            if (timerTicksElapsed != 0) {
                timerTicksElapsed = 0;
                syncData();
            }
            return;
        }
        int target = configuration.timerSeconds() * 20;
        if (timerTicksElapsed < target) {
            timerTicksElapsed++;
            if (timerTicksElapsed == target) syncData();
        }
    }

    public boolean isTimerReady() {
        return configuration.launchCondition() == LaunchCondition.TIMER
                && timerTicksElapsed >= configuration.timerSeconds() * 20;
    }

    public void resetLaunchTimer() {
        if (timerTicksElapsed == 0) return;
        timerTicksElapsed = 0;
        syncData();
    }

    public int getTimerTicksElapsed() {
        return timerTicksElapsed;
    }

    public boolean hasCargo() {
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            if (!inventory.getStackInSlot(slot).isEmpty()) return true;
        }
        return false;
    }

    public int getRoundRobinCursor() {
        return roundRobinCursor;
    }

    public void setRoundRobinCursor(int cursor) {
        roundRobinCursor = Math.floorMod(cursor, INVENTORY_SLOTS);
        syncData();
    }

    public @Nullable IItemHandler getItemHandlerForProxy(LaunchpadProxyBlockEntity proxy, @Nullable Direction side) {
        if (!assembled || side == null || !side.getAxis().isHorizontal() || !proxy.belongsTo(worldPosition)) return null;
        if (proxy.isRearCenter()) return null;
        return itemAutomationHandler;
    }

    public @Nullable IFluidHandler getFuelHandlerForProxy(LaunchpadProxyBlockEntity proxy, @Nullable Direction side) {
        if (!assembled || side == null || !proxy.belongsTo(worldPosition) || !proxy.isRearCenter()) return null;
        return side == LaunchpadStructure.rearFace(getFacing()) ? fuelInsertionHandler : null;
    }

    public FluidTank getFuelTank() {
        return fuelTank;
    }

    public PugEntity getDockedPug() {
        if (!(level instanceof net.minecraft.server.level.ServerLevel serverLevel) || dockedPugId == null) return null;
        return serverLevel.getEntity(dockedPugId) instanceof PugEntity pug
                && pug.getFlightState() == PugFlightState.DOCKED ? pug : null;
    }

    public UUID getDockedPugId() {
        return dockedPugId;
    }

    void setDockedPug(PugEntity pug) {
        dockedPugId = pug.getUUID();
        missingDockedPugTicks = 0;
        syncData();
    }

    void clearDockedPug(UUID expectedId) {
        if (dockedPugId == null || expectedId != null && !dockedPugId.equals(expectedId)) return;
        dockedPugId = null;
        missingDockedPugTicks = 0;
        syncData();
    }

    int incrementMissingDockedPugTicks() {
        return ++missingDockedPugTicks;
    }

    void resetMissingDockedPugTicks() {
        missingDockedPugTicks = 0;
    }

    public UUID getClaimedFlightId() {
        return claimedFlightId;
    }

    public PugEntity getClaimedPug() {
        if (!(level instanceof net.minecraft.server.level.ServerLevel serverLevel) || claimedFlightId == null) return null;
        return serverLevel.getEntity(claimedFlightId) instanceof PugEntity pug
                && (pug.getFlightState() == PugFlightState.DESCENDING
                || pug.getFlightState() == PugFlightState.UNLOADING) ? pug : null;
    }

    public boolean tryClaim(UUID flightId) {
        if (claimedFlightId != null && !claimedFlightId.equals(flightId)) return false;
        if (claimedFlightId == null) {
            claimedFlightId = flightId;
            missingClaimedPugTicks = 0;
            syncData();
        }
        return true;
    }

    public void releaseClaim(UUID flightId) {
        if (claimedFlightId == null || flightId != null && !claimedFlightId.equals(flightId)) return;
        claimedFlightId = null;
        missingClaimedPugTicks = 0;
        syncData();
    }

    int incrementMissingClaimedPugTicks() {
        return ++missingClaimedPugTicks;
    }

    void resetMissingClaimedPugTicks() {
        missingClaimedPugTicks = 0;
    }

    public LaunchAttemptResult attemptLaunch() {
        return PugFlightService.attemptLaunch(this);
    }

    public void onControllerRemoved() {
        PugEntity docked = getDockedPug();
        if (docked != null) docked.discard();
        dockedPugId = null;
    }

    public void dropInventory() {
        if (inventoryDropped || level == null || level.isClientSide) return;
        inventoryDropped = true;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
                        worldPosition.getZ() + 0.5, stack.copy());
                inventory.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    private Direction getFacing() {
        BlockState state = getBlockState();
        return state.hasProperty(LaunchpadControllerBlock.FACING)
                ? state.getValue(LaunchpadControllerBlock.FACING) : Direction.NORTH;
    }

    private void syncData() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public GlareAddress getRemoteAddress() {
        return GlareAddress.of(addressSlots.get(0).getFilter().getItem(), addressSlots.get(1).getFilter().getItem(),
                addressSlots.get(2).getFilter().getItem());
    }

    private void onAddressChanged() {
        if (!(level instanceof ServerLevel server)) return;

        // On address changed, create enw configuration and sync
        applyConfiguration(new LaunchpadConfiguration(configuration.mode(), getRemoteAddress(),
                configuration.destinationAddress(), configuration.launchCondition(), configuration.timerSeconds()));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.resourceful_refinement.launchpad_controller");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new LaunchpadMenu(containerId, inventory, this);
    }

    @Override
    public void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buf) {
        LaunchpadMenu.writeClientSideData(buf, this);
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putString("Mode", configuration.mode().name());
        tag.put("LocalAddress", configuration.localAddress().save());
        tag.put("DestinationAddress", configuration.destinationAddress().save());
        tag.putString("LaunchCondition", configuration.launchCondition().name());
        tag.putInt("TimerSeconds", configuration.timerSeconds());
        tag.putBoolean("Assembled", assembled);
        tag.putBoolean("SkyClear", skyClear);
        tag.putInt("RoundRobinCursor", roundRobinCursor);
        tag.putBoolean("WasRedstonePowered", wasRedstonePowered);
        tag.putBoolean("PendingRedstoneLaunch", pendingRedstoneLaunch);
        tag.putInt("TimerTicksElapsed", timerTicksElapsed);
        if (dockedPugId != null) tag.putUUID("DockedPug", dockedPugId);
        if (claimedFlightId != null) tag.putUUID("ClaimedFlight", claimedFlightId);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.put("FuelTank", fuelTank.writeToNBT(registries, new CompoundTag()));
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        LaunchpadConfiguration defaults = LaunchpadConfiguration.defaults();
        LaunchpadMode mode = readEnum(tag.getString("Mode"), LaunchpadMode.class, defaults.mode());
        LaunchCondition condition = readEnum(tag.getString("LaunchCondition"), LaunchCondition.class,
                defaults.launchCondition());
        GlareAddress local = tag.contains("LocalAddress") ? GlareAddress.load(tag.getCompound("LocalAddress"))
                : defaults.localAddress();
        GlareAddress destination = tag.contains("DestinationAddress")
                ? GlareAddress.load(tag.getCompound("DestinationAddress")) : defaults.destinationAddress();
        int timer = tag.contains("TimerSeconds")
                ? Math.clamp(tag.getInt("TimerSeconds"), 0, LaunchpadConfiguration.MAX_TIMER_SECONDS)
                : defaults.timerSeconds();
        configuration = new LaunchpadConfiguration(mode, local, destination, condition, timer);
        assembled = tag.getBoolean("Assembled");
        skyClear = tag.getBoolean("SkyClear");
        roundRobinCursor = Math.floorMod(tag.getInt("RoundRobinCursor"), INVENTORY_SLOTS);
        wasRedstonePowered = tag.getBoolean("WasRedstonePowered");
        pendingRedstoneLaunch = tag.getBoolean("PendingRedstoneLaunch");
        timerTicksElapsed = Math.clamp(tag.getInt("TimerTicksElapsed"), 0, timer * 20);
        dockedPugId = tag.hasUUID("DockedPug") ? tag.getUUID("DockedPug") : null;
        claimedFlightId = tag.hasUUID("ClaimedFlight") ? tag.getUUID("ClaimedFlight") : null;
        missingDockedPugTicks = 0;
        missingClaimedPugTicks = 0;
        if (tag.contains("Inventory")) inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        if (tag.contains("FuelTank")) fuelTank.readFromNBT(registries, tag.getCompound("FuelTank"));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private static <E extends Enum<E>> E readEnum(String value, Class<E> type, E fallback) {
        try {
            return Enum.valueOf(type, value);
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }

    private final class FuelInsertionHandler implements IFluidHandler {
        @Override public int getTanks() { return fuelTank.getTanks(); }
        @Override public FluidStack getFluidInTank(int tank) { return fuelTank.getFluidInTank(tank); }
        @Override public int getTankCapacity(int tank) { return fuelTank.getTankCapacity(tank); }
        @Override public boolean isFluidValid(int tank, FluidStack stack) { return fuelTank.isFluidValid(tank, stack); }
        @Override public int fill(FluidStack resource, FluidAction action) { return fuelTank.fill(resource, action); }
        @Override public FluidStack drain(FluidStack resource, FluidAction action) { return FluidStack.EMPTY; }
        @Override public FluidStack drain(int maxDrain, FluidAction action) { return FluidStack.EMPTY; }
    }

    private final class ArrivalAwareItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return INVENTORY_SLOTS + CARGO_SLOTS;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            if (slot < 0 || slot >= getSlots()) return ItemStack.EMPTY;
            if (slot < INVENTORY_SLOTS) return inventory.getStackInSlot(slot);
            PugEntity pug = getClaimedPug();
            return pug != null && pug.getFlightState() == PugFlightState.UNLOADING
                    ? pug.cargo.getStackInSlot(slot - INVENTORY_SLOTS) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot < 0 || slot >= INVENTORY_SLOTS) return stack;
            return inventory.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot < 0 || slot >= getSlots()) return ItemStack.EMPTY;
            if (slot < INVENTORY_SLOTS) return inventory.extractItem(slot, amount, simulate);
            PugEntity pug = getClaimedPug();
            return pug != null && pug.getFlightState() == PugFlightState.UNLOADING
                    ? pug.cargo.extractItem(slot - INVENTORY_SLOTS, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot >= 0 && slot < INVENTORY_SLOTS ? inventory.getSlotLimit(slot) : 64;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot >= 0 && slot < INVENTORY_SLOTS && inventory.isItemValid(slot, stack);
        }
    }
}
