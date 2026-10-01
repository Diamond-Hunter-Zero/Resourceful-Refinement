package com.resourceful_refinement.content.glare.kinetic_receiver;

import com.resourceful_refinement.config.ServerConfig;
import com.resourceful_refinement.content.glare.*;
import com.resourceful_refinement.content.gui.GlareNetworkSnapshot;
import com.resourceful_refinement.content.gui.GlareNetworkSnapshotProvider;
import com.resourceful_refinement.registry.ModBlockEntities;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * A GLARE receiver that acts as a Create generator: it allocates Lux from its network and, while the
 * network can power it (online and not overloaded), emits rotation on its local-back face. The output
 * RPM, total stress capacity and Lux draw are all driven by {@code ServerConfig}.
 */
public class GlareKineticReceiverBlockEntity extends GeneratingKineticBlockEntity
        implements IGlareNode, IGlareReceiver, IHaveGoggleInformation, GlareNetworkSnapshotProvider {

    public static final int MAX_LINK_COUNT = 1;

    private GlareOperationStatus status = GlareOperationStatus.ONLINE;
    private UUID networkId;
    private int syncedLinkCount;
    private int syncedLuxCapacity;
    private int syncedLuxAllocated;
    private boolean syncedOverloaded;
    private final int[] syncedColourCharges = new int[DyeColor.values().length];
    private int[] syncedLuxHistory = new int[0];

    public GlareKineticReceiverBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.GLARE_KINETIC_RECEIVER_BE.get(), pos, blockState);
    }

    public GlareKineticReceiverBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    // -------------------------------------------------------------------------
    //  GLARE node plumbing
    // -------------------------------------------------------------------------
    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel server) {
            GlareService.onNodeLoaded(server, this);
            refreshSyncedGlareSummary(server);
            updateGeneratedRotation();
        }
    }

    @Override
    public int getMaxGlareLinks() {
        return MAX_LINK_COUNT;
    }

    @Override
    public DimensionalNodePos getGlareNodePos() {
        return DimensionalNodePos.of(level, worldPosition);
    }

    public UUID getNetworkId() {
        return networkId;
    }

    @Override
    public void onGlareNetworkChanged(ServerLevel level, UUID networkId) {
        this.networkId = networkId;
        refreshSyncedGlareSummary(level);
        updateGeneratedRotation();
        syncToClient();
    }

    @Override
    public void onGlareLinksChanged(ServerLevel level) {
        refreshSyncedGlareSummary(level);
        updateGeneratedRotation();
        syncToClient();
    }

    private void refreshSyncedGlareSummary(ServerLevel server) {
        DimensionalNodePos pos = DimensionalNodePos.of(server, worldPosition);
        syncedLinkCount = GlareService.getLinks(server, pos).size();
        syncedLuxCapacity = 0;
        syncedLuxAllocated = 0;
        syncedOverloaded = false;
        Arrays.fill(syncedColourCharges, 0);
        syncedLuxHistory = new int[0];
        if (networkId != null) {
            GlareService.getNetwork(server, networkId).ifPresent(network -> {
                syncedLuxCapacity = network.luxCapacity;
                syncedLuxAllocated = network.luxAllocated;
                syncedOverloaded = network.overloaded;
                syncedLuxHistory = network.luxHistory.stream().mapToInt(Integer::intValue).toArray();
                for (var entry : network.colourCharges.entrySet()) {
                    syncedColourCharges[entry.getKey().ordinal()] = entry.getValue();
                }
            });
        }
    }

    private void syncToClient() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    // -------------------------------------------------------------------------
    //  Receiver behaviour
    // -------------------------------------------------------------------------
    @Override
    public int getAllocatedLux() {
        return ServerConfig.KINETIC_RECEIVER_LUX.getAsInt();
    }

    @Override
    public GlareOperationStatus getGlareOperationStatus() {
        return status;
    }

    @Override
    public void setGlareOperationStatus(GlareOperationStatus status) {
        this.status = status;
        if (level instanceof ServerLevel server) {
            GlareService.updateNodeState(server, this);
        }
        updateGeneratedRotation();
        syncToClient();
    }

    @Override
    public void applyGlareOperationStatusFromNetwork(GlareOperationStatus status) {
        if (this.status == status) {
            return;
        }
        this.status = status;
        updateGeneratedRotation();
        syncToClient();
    }

    /** Powered only while linked to a network that is online (not overloaded). */
    public boolean isReceiverPowered() {
        return networkId != null && status == GlareOperationStatus.ONLINE;
    }

    // -------------------------------------------------------------------------
    //  Kinetic generation
    // -------------------------------------------------------------------------
    @Override
    public float getGeneratedSpeed() {
        return isReceiverPowered() ? ServerConfig.KINETIC_RECEIVER_RPM.getAsInt() : 0f;
    }

    @Override
    public float calculateAddedStressCapacity() {
        if (!isReceiverPowered()) {
            return 0f;
        }
        return ServerConfig.KINETIC_RECEIVER_CAPACITY.getAsInt();
    }

    @Override
    public void tick() {
        super.tick();
        // Guard against any missed network callback leaving the applied speed out of sync.
        if (level != null && !level.isClientSide && getGeneratedSpeed() != getSpeed()) {
            updateGeneratedRotation();
        }
    }

    // -------------------------------------------------------------------------
    //  Persistence & networking
    // -------------------------------------------------------------------------
    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putString("Status", status.name());
        if (networkId != null) {
            tag.putUUID("GlareNetwork", networkId);
        }
        tag.putInt("GlareLinkCount", syncedLinkCount);
        tag.putInt("GlareLuxCapacity", syncedLuxCapacity);
        tag.putInt("GlareLuxAllocated", syncedLuxAllocated);
        tag.putBoolean("GlareOverloaded", syncedOverloaded);
        tag.putIntArray("GlareColourCharges", syncedColourCharges);
        tag.putIntArray("GlareLuxHistory", syncedLuxHistory);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        try {
            status = GlareOperationStatus.valueOf(tag.getString("Status"));
        } catch (IllegalArgumentException ignored) {
            status = GlareOperationStatus.ONLINE;
        }
        networkId = tag.hasUUID("GlareNetwork") ? tag.getUUID("GlareNetwork") : null;
        syncedLinkCount = tag.getInt("GlareLinkCount");
        syncedLuxCapacity = tag.getInt("GlareLuxCapacity");
        syncedLuxAllocated = tag.getInt("GlareLuxAllocated");
        syncedOverloaded = tag.getBoolean("GlareOverloaded");
        int[] colourCharges = tag.getIntArray("GlareColourCharges");
        Arrays.fill(syncedColourCharges, 0);
        System.arraycopy(colourCharges, 0, syncedColourCharges, 0, Math.min(colourCharges.length, syncedColourCharges.length));
        syncedLuxHistory = tag.getIntArray("GlareLuxHistory");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        write(tag, registries, true);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public GlareNetworkSnapshot getSyncedGlareNetworkSnapshot() {
        return GlareNetworkSnapshot.of(networkId != null, syncedLuxAllocated, syncedLuxCapacity, syncedLuxHistory,
                syncedOverloaded ? GlareOperationStatus.OVERLOADED : status, syncedOverloaded);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        tooltip.add(Component.literal("     GLARE Kinetic Receiver:"));
        tooltip.add(Component.literal("Links: " + syncedLinkCount + "/" + MAX_LINK_COUNT));
        tooltip.add(Component.literal("Lux Draw: " + getAllocatedLux()));
        if (isReceiverPowered()) {
            int rpm = ServerConfig.KINETIC_RECEIVER_RPM.getAsInt();
            tooltip.add(Component.literal("§7Generating §b" + String.format("%,d", ServerConfig.KINETIC_RECEIVER_CAPACITY.getAsInt() * rpm)
                    + "su §8at " + rpm + " RPM"));
        } else if (networkId != null) {
            tooltip.add(Component.literal(syncedOverloaded ? "Status: Overloaded" : "Status: " + status.name()));
        } else {
            tooltip.add(Component.literal("Status: Unlinked"));
        }
        return true;
    }
}
