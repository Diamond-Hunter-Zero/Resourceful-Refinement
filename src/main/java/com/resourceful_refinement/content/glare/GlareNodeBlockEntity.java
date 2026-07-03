package com.resourceful_refinement.content.glare;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.resourceful_refinement.content.gui.GlareNetworkSnapshot;
import com.resourceful_refinement.content.gui.GlareNetworkSnapshotProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class GlareNodeBlockEntity extends BlockEntity implements IGlareNode, IHaveGoggleInformation, GlareNetworkSnapshotProvider {
    private final int maxLinks;
    private UUID networkId;

    /// The number of links attached to this node
    private int syncedLinkCount;
    /// The total LUX capacity of the network
    private int syncedLuxCapacity;
    /// The total allocated Lux across the network
    private int syncedLuxAllocated;
    /// The overload state of the network
    private boolean syncedOverloaded;
    private GlareOperationStatus syncedOperationStatus = GlareOperationStatus.ONLINE;
    private final int[] syncedColourCharges = new int[DyeColor.values().length];
    private int[] syncedLuxHistory = new int[0];

    public GlareNodeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState, int maxLinks) {
        super(type, pos, blockState);
        this.maxLinks = maxLinks;
    }

    @Override
    public int getMaxGlareLinks() {
        return maxLinks;
    }

    @Override
    public DimensionalNodePos getGlareNodePos() {
        return DimensionalNodePos.of(level, worldPosition);
    }

    public UUID getNetworkId() {
        return networkId;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel server) {
            GlareService.onNodeLoaded(server, this);
            refreshSyncedGlareSummary(server);
        }
    }

    @Override
    public void onGlareNetworkChanged(ServerLevel level, UUID networkId) {
        this.networkId = networkId;
        refreshSyncedGlareSummary(level);
        syncToClient();
    }

    @Override
    public void onGlareLinksChanged(ServerLevel level) {
        refreshSyncedGlareSummary(level);
        syncToClient();
    }

    private void refreshSyncedGlareSummary(ServerLevel server) {
        DimensionalNodePos pos = DimensionalNodePos.of(server, worldPosition);
        syncedLinkCount = GlareService.getLinks(server, pos).size();
        syncedLuxCapacity = 0;
        syncedLuxAllocated = 0;
        syncedOverloaded = false;
        syncedOperationStatus = this instanceof IGlareReceiver receiver ? receiver.getGlareOperationStatus() : GlareOperationStatus.ONLINE;
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

    protected void syncToClient() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    protected void pushGlareState() {
        if (level instanceof ServerLevel server) {
            GlareService.updateNodeState(server, this);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (networkId != null) {
            tag.putUUID("GlareNetwork", networkId);
        }
        tag.putInt("GlareLinkCount", syncedLinkCount);
        tag.putInt("GlareLuxCapacity", syncedLuxCapacity);
        tag.putInt("GlareLuxAllocated", syncedLuxAllocated);
        tag.putBoolean("GlareOverloaded", syncedOverloaded);
        tag.putString("GlareOperationStatus", syncedOperationStatus.name());
        tag.putIntArray("GlareColourCharges", syncedColourCharges);
        tag.putIntArray("GlareLuxHistory", syncedLuxHistory);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        networkId = tag.hasUUID("GlareNetwork") ? tag.getUUID("GlareNetwork") : null;
        syncedLinkCount = tag.getInt("GlareLinkCount");
        syncedLuxCapacity = tag.getInt("GlareLuxCapacity");
        syncedLuxAllocated = tag.getInt("GlareLuxAllocated");
        syncedOverloaded = tag.getBoolean("GlareOverloaded");
        try {
            syncedOperationStatus = GlareOperationStatus.valueOf(tag.getString("GlareOperationStatus"));
        } catch (IllegalArgumentException ignored) {
            syncedOperationStatus = GlareOperationStatus.ONLINE;
        }
        int[] colourCharges = tag.getIntArray("GlareColourCharges");
        Arrays.fill(syncedColourCharges, 0);
        System.arraycopy(colourCharges, 0, syncedColourCharges, 0, Math.min(colourCharges.length, syncedColourCharges.length));
        syncedLuxHistory = tag.getIntArray("GlareLuxHistory");
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

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        tooltip.add(Component.literal("     GLARE Node:"));
        tooltip.add(Component.literal("Links: " + syncedLinkCount + "/" + maxLinks));
        if (networkId != null) {
            tooltip.add(Component.literal("Lux: " + syncedLuxAllocated + "/" + syncedLuxCapacity));
            tooltip.add(Component.literal(syncedOverloaded ? "Status: Overloaded" : "Status: Online"));
            if (this instanceof IGlareReceiver) {
                tooltip.add(Component.literal("Receiver: " + formatStatus(syncedOperationStatus)));
            }
            String colourSummary = colourChargeSummary();
            if (!colourSummary.isEmpty()) {
                tooltip.add(Component.literal("Colour Charge: " + colourSummary));
            }
        }
        return true;
    }

    protected static String colourName(DyeColor colour) {
        return colour.getName().replace('_', ' ');
    }

    protected int getSyncedColourCharge(DyeColor colour) {
        return syncedColourCharges[colour.ordinal()];
    }

    @Override
    public GlareNetworkSnapshot getSyncedGlareNetworkSnapshot() {
        return GlareNetworkSnapshot.of(networkId != null, syncedLuxAllocated, syncedLuxCapacity, syncedLuxHistory,
                syncedOverloaded ? GlareOperationStatus.OVERLOADED : syncedOperationStatus, syncedOverloaded);
    }

    private String colourChargeSummary() {
        StringBuilder builder = new StringBuilder();
        DyeColor[] colours = DyeColor.values();
        for (int i = 0; i < syncedColourCharges.length && i < colours.length; i++) {
            int charge = syncedColourCharges[i];
            if (charge <= 0) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(", ");
            }
            builder.append(colourName(colours[i])).append(": ").append(charge);
        }
        return builder.toString();
    }

    private static String formatStatus(GlareOperationStatus status) {
        return status.name().toLowerCase(java.util.Locale.ROOT).replace('_', ' ');
    }
}
