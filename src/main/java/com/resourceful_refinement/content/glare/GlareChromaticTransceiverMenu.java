package com.resourceful_refinement.content.glare;

import com.resourceful_refinement.registry.ModBlocks;
import com.resourceful_refinement.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class GlareChromaticTransceiverMenu extends AbstractContainerMenu {
    private static final int COLOUR_COUNT = 16;
    private final BlockPos blockPos;
    private final ContainerLevelAccess access;
    private final int[] initialThresholds;
    private final GlareComparison[] initialComparisons;
    private final GlareLogicMode initialMode;
    private final ContainerData colourCharges;

    public GlareChromaticTransceiverMenu(int id, Inventory inventory, GlareChromaticTransceiverBlockEntity blockEntity) {
        this(id, inventory, blockEntity, blockEntity.getLogicMode(), readThresholds(blockEntity), readComparisons(blockEntity), liveColourData(blockEntity));
    }

    private GlareChromaticTransceiverMenu(int id, Inventory inventory, @Nullable GlareChromaticTransceiverBlockEntity blockEntity,
            GlareLogicMode mode, int[] thresholds, GlareComparison[] comparisons, ContainerData colourCharges) {
        super(ModMenus.GLARE_CHROMATIC_TRANSCEIVER.get(), id);
        this.blockPos = blockEntity != null ? blockEntity.getBlockPos() : BlockPos.ZERO;
        this.access = blockEntity != null ? ContainerLevelAccess.create(blockEntity.getLevel(), blockPos) : ContainerLevelAccess.NULL;
        this.initialMode = mode;
        this.initialThresholds = thresholds;
        this.initialComparisons = comparisons;
        this.colourCharges = colourCharges;
        addDataSlots(colourCharges);
    }

    public static GlareChromaticTransceiverMenu fromNetwork(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        GlareLogicMode mode = buf.readEnum(GlareLogicMode.class);
        int[] thresholds = new int[COLOUR_COUNT];
        for (int i = 0; i < COLOUR_COUNT; i++) thresholds[i] = buf.readVarInt() - 1;
        GlareComparison[] comparisons = new GlareComparison[COLOUR_COUNT];
        for (int i = 0; i < COLOUR_COUNT; i++) comparisons[i] = GlareComparison.byOrdinal(buf.readVarInt());
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        GlareChromaticTransceiverBlockEntity transceiver = blockEntity instanceof GlareChromaticTransceiverBlockEntity value ? value : null;
        GlareChromaticTransceiverMenu menu = new GlareChromaticTransceiverMenu(id, inventory, transceiver, mode, thresholds, comparisons, new SimpleContainerData(COLOUR_COUNT));
        menu.blockPosFromNetwork = pos;
        return menu;
    }

    private BlockPos blockPosFromNetwork;

    public static void writeClientSideData(RegistryFriendlyByteBuf buf, GlareChromaticTransceiverBlockEntity blockEntity) {
        buf.writeBlockPos(blockEntity.getBlockPos());
        buf.writeEnum(blockEntity.getLogicMode());
        for (DyeColor colour : DyeColor.values()) buf.writeVarInt(blockEntity.getThreshold(colour) + 1);
        for (DyeColor colour : DyeColor.values()) buf.writeVarInt(blockEntity.getComparison(colour).ordinal());
    }

    private static int[] readThresholds(GlareChromaticTransceiverBlockEntity blockEntity) {
        int[] values = new int[COLOUR_COUNT];
        for (DyeColor colour : DyeColor.values()) values[colour.ordinal()] = blockEntity.getThreshold(colour);
        return values;
    }

    private static GlareComparison[] readComparisons(GlareChromaticTransceiverBlockEntity blockEntity) {
        GlareComparison[] values = new GlareComparison[COLOUR_COUNT];
        for (DyeColor colour : DyeColor.values()) values[colour.ordinal()] = blockEntity.getComparison(colour);
        return values;
    }

    private static ContainerData liveColourData(GlareChromaticTransceiverBlockEntity blockEntity) {
        return new ContainerData() {
            @Override public int get(int index) { return blockEntity.getNetworkColourCharge(DyeColor.values()[index]); }
            @Override public void set(int index, int value) {}
            @Override public int getCount() { return COLOUR_COUNT; }
        };
    }

    public BlockPos getBlockPos() { return blockPosFromNetwork != null ? blockPosFromNetwork : blockPos; }
    public GlareLogicMode getInitialMode() { return initialMode; }
    public int getInitialThreshold(DyeColor colour) { return initialThresholds[colour.ordinal()]; }
    public GlareComparison getInitialComparison(DyeColor colour) { return initialComparisons[colour.ordinal()]; }
    public int getColourCharge(DyeColor colour) { return colourCharges.get(colour.ordinal()); }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.GLARE_CHROMATIC_TRANSCEIVER.get());
    }

    @Override
    public net.minecraft.world.item.ItemStack quickMoveStack(Player player, int index) {
        return net.minecraft.world.item.ItemStack.EMPTY;
    }
}
