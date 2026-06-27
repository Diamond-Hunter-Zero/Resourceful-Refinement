package com.resourceful_refinement.content.glare;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/** SmartBlockEntity counterpart to {@link GlareNodeBlockEntity}, for GLARE nodes that use Create behaviours. */
public abstract class GlareSmartNodeBlockEntity extends SmartBlockEntity implements IGlareNode, IHaveGoggleInformation {
    private final int maxLinks;
    private UUID networkId;
    private int syncedLinkCount;
    private int syncedLuxCapacity;
    private int syncedLuxAllocated;
    private boolean syncedOverloaded;
    private GlareOperationStatus syncedOperationStatus = GlareOperationStatus.ONLINE;
    private final int[] syncedColourCharges = new int[DyeColor.values().length];

    protected GlareSmartNodeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int maxLinks) {
        super(type, pos, state);
        this.maxLinks = maxLinks;
    }

    @Override public int getMaxGlareLinks() { return maxLinks; }
    @Override public DimensionalNodePos getGlareNodePos() { return DimensionalNodePos.of(level, worldPosition); }
    public UUID getNetworkId() { return networkId; }

    @Override public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel server) {
            GlareService.onNodeLoaded(server, this);
            refreshGlareSummary(server);
        }
    }

    @Override public void onGlareNetworkChanged(ServerLevel level, UUID networkId) {
        this.networkId = networkId;
        refreshGlareSummary(level);
        sendData();
    }

    @Override public void onGlareLinksChanged(ServerLevel level) {
        refreshGlareSummary(level);
        sendData();
    }

    protected void pushGlareState() {
        if (level instanceof ServerLevel server) GlareService.updateNodeState(server, this);
    }

    private void refreshGlareSummary(ServerLevel server) {
        syncedLinkCount = GlareService.getLinks(server, DimensionalNodePos.of(server, worldPosition)).size();
        syncedLuxCapacity = 0;
        syncedLuxAllocated = 0;
        syncedOverloaded = false;
        syncedOperationStatus = this instanceof IGlareReceiver receiver ? receiver.getGlareOperationStatus() : GlareOperationStatus.ONLINE;
        Arrays.fill(syncedColourCharges, 0);
        if (networkId != null) GlareService.getNetwork(server, networkId).ifPresent(network -> {
            syncedLuxCapacity = network.luxCapacity;
            syncedLuxAllocated = network.luxAllocated;
            syncedOverloaded = network.overloaded;
            network.colourCharges.forEach((colour, count) -> syncedColourCharges[colour.ordinal()] = count);
        });
    }

    @Override protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        if (networkId != null) tag.putUUID("GlareNetwork", networkId);
        tag.putInt("GlareLinkCount", syncedLinkCount);
        tag.putInt("GlareLuxCapacity", syncedLuxCapacity);
        tag.putInt("GlareLuxAllocated", syncedLuxAllocated);
        tag.putBoolean("GlareOverloaded", syncedOverloaded);
        tag.putString("GlareOperationStatus", syncedOperationStatus.name());
        tag.putIntArray("GlareColourCharges", syncedColourCharges);
    }

    @Override protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        networkId = tag.hasUUID("GlareNetwork") ? tag.getUUID("GlareNetwork") : null;
        syncedLinkCount = tag.getInt("GlareLinkCount");
        syncedLuxCapacity = tag.getInt("GlareLuxCapacity");
        syncedLuxAllocated = tag.getInt("GlareLuxAllocated");
        syncedOverloaded = tag.getBoolean("GlareOverloaded");
        try { syncedOperationStatus = GlareOperationStatus.valueOf(tag.getString("GlareOperationStatus")); }
        catch (IllegalArgumentException ignored) { syncedOperationStatus = GlareOperationStatus.ONLINE; }
        Arrays.fill(syncedColourCharges, 0);
        int[] charges = tag.getIntArray("GlareColourCharges");
        System.arraycopy(charges, 0, syncedColourCharges, 0, Math.min(charges.length, syncedColourCharges.length));
    }

    @Override public boolean addToGoggleTooltip(List<Component> tooltip, boolean sneaking) {
        tooltip.add(Component.literal("     GLARE Node:"));
        tooltip.add(Component.literal("Links: " + syncedLinkCount + "/" + maxLinks));
        if (networkId != null) {
            tooltip.add(Component.literal("Lux: " + syncedLuxAllocated + "/" + syncedLuxCapacity));
            tooltip.add(Component.literal(syncedOverloaded ? "Status: Overloaded" : "Status: Online"));
        }
        return true;
    }
}
