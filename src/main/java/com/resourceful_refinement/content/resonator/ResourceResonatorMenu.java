package com.resourceful_refinement.content.resonator;

import com.resourceful_refinement.config.ServerConfig;
import com.resourceful_refinement.content.prism_cell.PrismCell;
import com.resourceful_refinement.content.resonator.scan.ResonatorTarget;
import com.resourceful_refinement.content.resonator.scan.ScannedPoi;
import com.resourceful_refinement.registry.ModBlocks;
import com.resourceful_refinement.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Container menu for the Resource Resonator radar GUI. Carries no inventory slots — it is a config/readout menu.
 * The current filter flags and the cached scan results are synced to the client once at open time (via
 * {@link #writeClientSideData}); fresh scan results after that arrive through a dedicated S2C payload.
 */
public class ResourceResonatorMenu extends AbstractContainerMenu {

    private final BlockPos blockPos;
    private final ContainerLevelAccess access;
    private final boolean[] initialFilter;
    private final List<ScannedPoi> initialResults;
    private final int scanRadiusChunks;
    private final boolean cellPresent;
    private final int cellTier;
    private final int cellCharge;
    private final int cellMaxCharge;
    private final int pingChargeCost;

    public ResourceResonatorMenu(int id, Inventory inventory, ResourceResonatorBlockEntity blockEntity) {
        this(id, inventory, blockEntity, blockEntity.getBlockPos(), blockEntity.copyFilter(),
                blockEntity.snapshotResults(), blockEntity.getEffectiveScanRadiusChunks(),
                blockEntity.getPrismCell(), ServerConfig.RESONATOR_PING_CHARGE_COST.get());
    }

    private ResourceResonatorMenu(int id, Inventory inventory, ResourceResonatorBlockEntity blockEntity,
            BlockPos pos, boolean[] filter, List<ScannedPoi> results, int scanRadiusChunks, PrismCell cell,
            int pingChargeCost) {
        super(ModMenus.RESOURCE_RESONATOR.get(), id);
        this.blockPos = pos;
        this.access = blockEntity != null
                ? ContainerLevelAccess.create(blockEntity.getLevel(), pos)
                : ContainerLevelAccess.NULL;
        this.initialFilter = normaliseFilter(filter);
        this.initialResults = results;
        this.scanRadiusChunks = scanRadiusChunks;
        this.cellPresent = cell != null;
        this.cellTier = cell != null ? cell.tier().index() : 0;
        this.cellCharge = cell != null ? cell.currentCharge() : 0;
        this.cellMaxCharge = cell != null ? cell.maxCharge() : 0;
        this.pingChargeCost = pingChargeCost;
    }

    private ResourceResonatorMenu(int id, Inventory inventory, BlockPos pos, boolean[] filter,
            List<ScannedPoi> results, int scanRadiusChunks,
            boolean cellPresent, int cellTier, int cellCharge, int cellMaxCharge, int pingChargeCost) {
        super(ModMenus.RESOURCE_RESONATOR.get(), id);
        this.blockPos = pos;
        this.access = ContainerLevelAccess.NULL;
        this.initialFilter = normaliseFilter(filter);
        this.initialResults = results;
        this.scanRadiusChunks = scanRadiusChunks;
        this.cellPresent = cellPresent;
        this.cellTier = cellTier;
        this.cellCharge = cellCharge;
        this.cellMaxCharge = cellMaxCharge;
        this.pingChargeCost = pingChargeCost;
    }

    public static ResourceResonatorMenu fromNetwork(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        boolean[] filter = readFilter(buf);
        List<ScannedPoi> results = ScannedPoi.LIST_STREAM_CODEC.decode(buf);
        int scanRadiusChunks = buf.readVarInt();
        boolean cellPresent = buf.readBoolean();
        int cellTier = buf.readVarInt();
        int cellCharge = buf.readVarInt();
        int cellMaxCharge = buf.readVarInt();
        int pingChargeCost = buf.readVarInt();
        return new ResourceResonatorMenu(id, inventory, pos, filter, results, scanRadiusChunks,
                cellPresent, cellTier, cellCharge, cellMaxCharge, pingChargeCost);
    }

    public static void writeClientSideData(RegistryFriendlyByteBuf buf, ResourceResonatorBlockEntity blockEntity) {
        buf.writeBlockPos(blockEntity.getBlockPos());
        writeFilter(buf, blockEntity.copyFilter());
        ScannedPoi.LIST_STREAM_CODEC.encode(buf, blockEntity.snapshotResults());
        buf.writeVarInt(blockEntity.getEffectiveScanRadiusChunks());
        PrismCell cell = blockEntity.getPrismCell();
        buf.writeBoolean(cell != null);
        buf.writeVarInt(cell != null ? cell.tier().index() : 0);
        buf.writeVarInt(cell != null ? cell.currentCharge() : 0);
        buf.writeVarInt(cell != null ? cell.maxCharge() : 0);
        buf.writeVarInt(ServerConfig.RESONATOR_PING_CHARGE_COST.get());
    }

    private static void writeFilter(RegistryFriendlyByteBuf buf, boolean[] filter) {
        int count = ResonatorTarget.count();
        for (int i = 0; i < count; i++) {
            buf.writeBoolean(i < filter.length && filter[i]);
        }
    }

    private static boolean[] readFilter(RegistryFriendlyByteBuf buf) {
        int count = ResonatorTarget.count();
        boolean[] filter = new boolean[count];
        for (int i = 0; i < count; i++) {
            filter[i] = buf.readBoolean();
        }
        return filter;
    }

    private static boolean[] normaliseFilter(boolean[] filter) {
        boolean[] normalised = new boolean[ResonatorTarget.count()];
        for (int i = 0; i < normalised.length && i < filter.length; i++) {
            normalised[i] = filter[i];
        }
        return normalised;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public boolean isEnabled(ResonatorTarget target) {
        return initialFilter[target.ordinal()];
    }

    public boolean[] getInitialFilter() {
        return initialFilter;
    }

    public List<ScannedPoi> getInitialResults() {
        return initialResults;
    }

    /** Configured scan radius in chunks, synced from the server; the radar's maximum zoom-out is derived from this. */
    public int getScanRadiusChunks() {
        return scanRadiusChunks;
    }

    public boolean isCellPresent() {
        return cellPresent;
    }

    public int getCellTier() {
        return cellTier;
    }

    public int getCellCharge() {
        return cellCharge;
    }

    public int getCellMaxCharge() {
        return cellMaxCharge;
    }

    public int getPingChargeCost() {
        return pingChargeCost;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.RESOURCE_RESONATOR.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
