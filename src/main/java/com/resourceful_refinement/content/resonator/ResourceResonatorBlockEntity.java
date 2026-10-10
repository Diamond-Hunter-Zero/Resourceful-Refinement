package com.resourceful_refinement.content.resonator;

import com.resourceful_refinement.config.ServerConfig;
import com.resourceful_refinement.content.prism_cell.IPrismCellBlockConsumer;
import com.resourceful_refinement.content.prism_cell.PrismCell;
import com.resourceful_refinement.content.prism_cell.PrismCellTier;
import com.resourceful_refinement.content.resonator.scan.ResonatorTarget;
import com.resourceful_refinement.content.resonator.scan.ResourceScanService;
import com.resourceful_refinement.content.resonator.scan.ScannedPoi;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * Block entity for the Resource Resonator. Holds the per-type scan filter (persisted) and the most recent scan
 * result set (in-memory only — persistence across sessions is not required per the design). Acts as the
 * {@link MenuProvider} for the radar GUI.
 */
public class ResourceResonatorBlockEntity extends BlockEntity implements MenuProvider, IPrismCellBlockConsumer {

    private static final String TAG_FILTER_MASK = "FilterMask";
    private static final String TAG_PRISM_CELL = "PrismCell";

    /** One enabled flag per {@link ResonatorTarget}, indexed by ordinal. All enabled by default. */
    private final boolean[] filter = new boolean[ResonatorTarget.count()];

    /** Last scan output; rebuilt on every scan, synced to the client menu on open and after each scan. */
    private List<ScannedPoi> lastResults = List.of();

    /** Installed Prism Cell that powers the Ping Beacon and sets the scan radius; {@code null} when none is loaded. */
    @Nullable
    private PrismCell prismCell;

    public ResourceResonatorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        Arrays.fill(filter, true);
    }

    // --- Prism Cell (IPrismCellBlockConsumer) ---------------------------------------------------------------

    @Nullable
    @Override
    public PrismCell getPrismCell() {
        return prismCell;
    }

    @Override
    public void setPrismCell(@Nullable PrismCell cell) {
        this.prismCell = cell;
    }

    @Override
    public void onPrismCellChanged() {
        setChanged();
    }

    /**
     * Consumes the Ping Beacon charge cost from the installed cell, destroying the cell if the draw empties it.
     * Callers must have already confirmed a cell is present with enough charge (via {@link #getPrismCell()}).
     */
    public void consumePingCharge(int cost) {
        if (prismCell == null) {
            return;
        }
        prismCell.drainCellCharge(cost, true);
        if (prismCell.isDepleted()) {
            prismCell = null;
        }
        setChanged();
    }

    /** Scan radius (chunks) for the installed cell's grade, or the no-cell base radius when empty. */
    public int getEffectiveScanRadiusChunks() {
        if (prismCell == null) {
            return ServerConfig.RESONATOR_SCAN_RADIUS_BASE_CHUNKS.get();
        }
        PrismCellTier tier = prismCell.tier();
        return switch (tier) {
            case LOW -> ServerConfig.RESONATOR_SCAN_RADIUS_LOW_CHUNKS.get();
            case MID -> ServerConfig.RESONATOR_SCAN_RADIUS_MID_CHUNKS.get();
            case HIGH, CREATIVE -> ServerConfig.RESONATOR_SCAN_RADIUS_HIGH_CHUNKS.get();
        };
    }

    // --- Filter state ---------------------------------------------------------------------------------------

    public boolean isEnabled(ResonatorTarget target) {
        return filter[target.ordinal()];
    }

    public void setEnabled(ResonatorTarget target, boolean enabled) {
        if (filter[target.ordinal()] != enabled) {
            filter[target.ordinal()] = enabled;
            setChanged();
        }
    }

    // --- Scanning -------------------------------------------------------------------------------------------

    /**
     * Runs a seed-based scan for <em>all</em> target types, caches and returns the results. Server-side only.
     *
     * <p>The scan deliberately ignores the GUI filter: it always predicts every generation type so the client holds
     * the complete result set and can show/hide types instantly by toggling the filter, without re-scanning. The
     * filter is purely a display control (applied client-side when drawing pins).</p>
     */
    public List<ScannedPoi> runScan(ServerLevel level) {
        int radius = getEffectiveScanRadiusChunks();
        List<ScannedPoi> results = ResourceScanService.scan(level, getBlockPos(), radius,
                EnumSet.allOf(ResonatorTarget.class));
        this.lastResults = results;
        return results;
    }

    public List<ScannedPoi> getLastResults() {
        return lastResults;
    }

    public void setLastResults(List<ScannedPoi> results) {
        this.lastResults = results != null ? results : List.of();
    }

    // --- Persistence (filter only) --------------------------------------------------------------------------

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        int mask = 0;
        for (int i = 0; i < filter.length; i++) {
            if (filter[i]) {
                mask |= (1 << i);
            }
        }
        tag.putInt(TAG_FILTER_MASK, mask);
        if (prismCell != null) {
            tag.put(TAG_PRISM_CELL, prismCell.save());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains(TAG_FILTER_MASK)) {
            int mask = tag.getInt(TAG_FILTER_MASK);
            for (int i = 0; i < filter.length; i++) {
                filter[i] = (mask & (1 << i)) != 0;
            }
        }
        this.prismCell = tag.contains(TAG_PRISM_CELL) ? PrismCell.load(tag.getCompound(TAG_PRISM_CELL)) : null;
    }

    // --- MenuProvider ---------------------------------------------------------------------------------------

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.resourceful_refinement.resource_resonator");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ResourceResonatorMenu(id, inventory, this);
    }

    @Override
    public void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buf) {
        ResourceResonatorMenu.writeClientSideData(buf, this);
    }

    /** Collects the current filter flags for sync, in ordinal order. */
    public boolean[] copyFilter() {
        return Arrays.copyOf(filter, filter.length);
    }

    public void applyFilter(boolean[] incoming) {
        boolean changed = false;
        for (int i = 0; i < filter.length && i < incoming.length; i++) {
            if (filter[i] != incoming[i]) {
                filter[i] = incoming[i];
                changed = true;
            }
        }
        if (changed) {
            setChanged();
        }
    }

    /** Defensive copy helper for building sync payloads off the cached results. */
    public List<ScannedPoi> snapshotResults() {
        return new ArrayList<>(lastResults);
    }
}
