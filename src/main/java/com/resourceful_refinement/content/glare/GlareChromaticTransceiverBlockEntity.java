package com.resourceful_refinement.content.glare;

import com.resourceful_refinement.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;

public class GlareChromaticTransceiverBlockEntity extends GlareNodeBlockEntity implements IGlareReceiver, MenuProvider {
    public static final int MAX_LINK_COUNT = 1;
    public static final int DISABLED_FILTER = -1;
    public static final int MAX_THRESHOLD = 1_000_000;

    private final int[] thresholds = new int[DyeColor.values().length];
    private final GlareComparison[] comparisons = new GlareComparison[DyeColor.values().length];
    private GlareLogicMode logicMode = GlareLogicMode.AND;
    private GlareOperationStatus status = GlareOperationStatus.ONLINE;
    private boolean outputPowered;

    public GlareChromaticTransceiverBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GLARE_CHROMATIC_TRANSCEIVER_BE.get(), pos, state, MAX_LINK_COUNT);
        Arrays.fill(thresholds, DISABLED_FILTER);
        Arrays.fill(comparisons, GlareComparison.GREATER_THAN_OR_EQUAL);
    }

    @Override
    public int getAllocatedLux() {
        return 0;
    }

    @Override
    public GlareOperationStatus getGlareOperationStatus() {
        return status;
    }

    @Override
    public void setGlareOperationStatus(GlareOperationStatus status) {
        this.status = status;
        pushGlareState();
        syncToClient();
    }

    @Override
    public void applyGlareOperationStatusFromNetwork(GlareOperationStatus status) {
        this.status = status;
        updateOutput();
    }

    public int getThreshold(DyeColor colour) {
        return thresholds[colour.ordinal()];
    }

    public GlareLogicMode getLogicMode() {
        return logicMode;
    }

    public GlareComparison getComparison(DyeColor colour) {
        return comparisons[colour.ordinal()];
    }

    public boolean isOutputPowered() {
        return outputPowered;
    }

    public int getNetworkColourCharge(DyeColor colour) {
        return getSyncedColourCharge(colour);
    }

    public void applyConfiguration(GlareLogicMode mode, int[] requestedThresholds, int[] requestedComparisons) {
        logicMode = mode;
        for (int i = 0; i < thresholds.length; i++) {
            int value = i < requestedThresholds.length ? requestedThresholds[i] : DISABLED_FILTER;
            thresholds[i] = value < 0 ? DISABLED_FILTER : Math.min(value, MAX_THRESHOLD);
            comparisons[i] = GlareComparison.byOrdinal(i < requestedComparisons.length ? requestedComparisons[i] : 0);
        }
        updateOutput();
        syncToClient();
    }

    @Override
    public void onGlareNetworkChanged(ServerLevel level, java.util.UUID networkId) {
        super.onGlareNetworkChanged(level, networkId);
        updateOutput();
    }

    @Override
    public void onGlareLinksChanged(ServerLevel level) {
        super.onGlareLinksChanged(level);
        updateOutput();
    }

    private void updateOutput() {
        int[] charges = new int[DyeColor.values().length];
        for (DyeColor colour : DyeColor.values()) charges[colour.ordinal()] = getNetworkColourCharge(colour);
        boolean powered = matchesFilters(logicMode, thresholds, comparisons, charges);
        if (powered == outputPowered) {
            return;
        }
        outputPowered = powered;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
            level.updateNeighborsAt(worldPosition.below(), getBlockState().getBlock());
            syncToClient();
        }
    }

    static boolean matchesFilters(GlareLogicMode mode, int[] thresholds, int[] charges) {
        GlareComparison[] defaultComparisons = new GlareComparison[DyeColor.values().length];
        Arrays.fill(defaultComparisons, GlareComparison.GREATER_THAN_OR_EQUAL);
        return matchesFilters(mode, thresholds, defaultComparisons, charges);
    }

    static boolean matchesFilters(GlareLogicMode mode, int[] thresholds, GlareComparison[] comparisons, int[] charges) {
        int enabled = 0;
        int matches = 0;
        for (int i = 0; i < DyeColor.values().length; i++) {
            int threshold = i < thresholds.length ? thresholds[i] : DISABLED_FILTER;
            if (threshold < 0) continue;
            enabled++;
            GlareComparison comparison = i < comparisons.length ? comparisons[i] : GlareComparison.GREATER_THAN_OR_EQUAL;
            if (i < charges.length && comparison.test(charges[i], threshold)) matches++;
        }
        return enabled > 0 && switch (mode) {
            case AND -> matches == enabled;
            case OR -> matches > 0;
            case XOR -> matches == 1;
        };
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.resourceful_refinement.glare_chromatic_transceiver");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new GlareChromaticTransceiverMenu(id, inventory, this);
    }

    @Override
    public void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buf) {
        GlareChromaticTransceiverMenu.writeClientSideData(buf, this);
    }

    public boolean isWithinUsableDistance(Player player) {
        return player.distanceToSqr(worldPosition.getCenter()) <= 64.0;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putIntArray("ColourThresholds", thresholds);
        tag.putIntArray("ColourComparisons", Arrays.stream(comparisons).mapToInt(Enum::ordinal).toArray());
        tag.putString("LogicMode", logicMode.name());
        tag.putString("Status", status.name());
        tag.putBoolean("OutputPowered", outputPowered);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        Arrays.fill(thresholds, DISABLED_FILTER);
        int[] saved = tag.getIntArray("ColourThresholds");
        System.arraycopy(saved, 0, thresholds, 0, Math.min(saved.length, thresholds.length));
        Arrays.fill(comparisons, GlareComparison.GREATER_THAN_OR_EQUAL);
        int[] savedComparisons = tag.getIntArray("ColourComparisons");
        for (int i = 0; i < Math.min(savedComparisons.length, comparisons.length); i++) {
            comparisons[i] = GlareComparison.byOrdinal(savedComparisons[i]);
        }
        try { logicMode = GlareLogicMode.valueOf(tag.getString("LogicMode")); } catch (IllegalArgumentException ignored) {}
        try { status = GlareOperationStatus.valueOf(tag.getString("Status")); } catch (IllegalArgumentException ignored) {}
        outputPowered = tag.getBoolean("OutputPowered");
    }
}
