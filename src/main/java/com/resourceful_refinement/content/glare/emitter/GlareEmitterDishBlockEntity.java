package com.resourceful_refinement.content.glare.emitter;

import com.resourceful_refinement.config.ServerConfig;
import com.resourceful_refinement.content.glare.*;
import com.resourceful_refinement.registry.ModBlockEntities;
import com.resourceful_refinement.registry.ModBlocks;
import com.resourceful_refinement.content.gui.GlareNetworkSnapshot;
import com.resourceful_refinement.content.gui.GlareNetworkSnapshotProvider;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
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

public class GlareEmitterDishBlockEntity extends KineticBlockEntity implements IGlareNode, IGlareEmitter, GlareNetworkSnapshotProvider {
    public static final int MAX_LINK_COUNT = 1;

    private DyeColor colour = DyeColor.WHITE;
    private boolean redstonePowered;
    private boolean hasCrystal;
    private int lastPushedLux = -1;
    private DyeColor lastPushedColour = null;
    private UUID networkId;
    private int syncedLinkCount;
    private int syncedLuxCapacity;
    private int syncedLuxAllocated;
    private boolean syncedOverloaded;
    private final int[] syncedColourCharges = new int[DyeColor.values().length];
    private int[] syncedLuxHistory = new int[0];

    public GlareEmitterDishBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.GLARE_EMITTER_DISH_BE.get(), pos, blockState);
    }

    public GlareEmitterDishBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel server) {
            GlareService.onNodeLoaded(server, this);
            refreshEmitterState();
            refreshSyncedGlareSummary(server);
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

    @Override
    public void tick() {
        super.tick();
        if (level instanceof ServerLevel && level.getGameTime() % 20 == 0) {
            refreshEmitterState();
        }
    }

    @Override
    public void onSpeedChanged(float previousSpeed) {
        super.onSpeedChanged(previousSpeed);
        refreshEmitterState();
    }

    public void refreshEmitterState() {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        boolean newRedstone = level.hasNeighborSignal(worldPosition);
        boolean newCrystal = level.getBlockState(worldPosition.below()).is(ModBlocks.RESONANCE_CRYSTAL.get())
                || level.getBlockState(worldPosition.below()).is(ModBlocks.ARTIFICIAL_RESONANCE_CRYSTAL.get());
        boolean localChanged = newRedstone != redstonePowered || newCrystal != hasCrystal;
        redstonePowered = newRedstone;
        hasCrystal = newCrystal;

        // Only push to the whole network when this emitter's actual contribution (Lux + colour) changes,
        // rather than every second. Local goggle state (crystal/signal) still refreshes when it changes.
        int effectiveLux = isGlareEmitterEnabled() ? Math.max(0, getProducedLux()) : 0;
        boolean outputChanged = effectiveLux != lastPushedLux || colour != lastPushedColour;
        if (outputChanged) {
            lastPushedLux = effectiveLux;
            lastPushedColour = colour;
            GlareService.updateNodeState(server, this);
        }
        if (localChanged || outputChanged) {
            refreshSyncedGlareSummary(server);
            syncToClient();
        }
    }

    public boolean isOnCrystal() {return hasCrystal; }

    public void setColour(DyeColor colour) {
        this.colour = colour;
        refreshEmitterState();
    }

    public DyeColor getColour() {
        return colour;
    }

    @Override
    public int getProducedLux() {
        return ServerConfig.GLARE_EMITTER_LUX.getAsInt();
    }

    @Override
    public DyeColor getLuxColourCharge() {
        return colour;
    }

    @Override
    public boolean isGlareEmitterEnabled() {
        return hasCrystal && !redstonePowered && Math.abs(getSpeed()) >= ServerConfig.GLARE_EMITTER_RPM.getAsInt() && !isOverStressed();
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

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putString("Colour", colour.getName());
        tag.putBoolean("Powered", redstonePowered);
        tag.putBoolean("HasCrystal", hasCrystal);
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
        colour = DyeColor.byName(tag.getString("Colour"), DyeColor.WHITE);
        redstonePowered = tag.getBoolean("Powered");
        hasCrystal = tag.getBoolean("HasCrystal");
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
                syncedOverloaded ? GlareOperationStatus.OVERLOADED : GlareOperationStatus.ONLINE, syncedOverloaded);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        tooltip.add(Component.literal("     GLARE Emitter:"));
        tooltip.add(Component.literal("Links: " + syncedLinkCount + "/" + MAX_LINK_COUNT));
        tooltip.add(Component.literal("Lux Output: " + (isGlareEmitterEnabled() ? ServerConfig.GLARE_EMITTER_LUX.getAsInt() : 0) + "/" + ServerConfig.GLARE_EMITTER_LUX.getAsInt()));
        tooltip.add(Component.literal("Required RPM: " + ServerConfig.GLARE_EMITTER_RPM.getAsInt()));
        tooltip.add(Component.literal("Crystal: " + (hasCrystal ? "present" : "missing")));
        tooltip.add(Component.literal("Signal: " + (redstonePowered ? "disabled" : "clear")));
        if (networkId != null) {
            tooltip.add(Component.literal("Network Lux: " + syncedLuxAllocated + "/" + syncedLuxCapacity));
            tooltip.add(Component.literal(syncedOverloaded ? "Status: Overloaded" : "Status: Online"));
            String colourSummary = colourChargeSummary();
            if (!colourSummary.isEmpty()) {
                tooltip.add(Component.literal("Colour Charge: " + colourSummary));
            }
        }
        return true;
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
            builder.append(colours[i].getName().replace('_', ' ')).append(": ").append(charge);
        }
        return builder.toString();
    }
}
