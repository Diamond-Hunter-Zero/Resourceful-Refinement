package com.resourceful_refinement.content.glare.terminal;

import com.resourceful_refinement.registry.ModBlocks;
import com.resourceful_refinement.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TelemetryTerminalMenu extends AbstractContainerMenu {
    private final BlockPos blockPos;
    private final ContainerLevelAccess access;
    private TelemetryTerminalSnapshot snapshot;

    public TelemetryTerminalMenu(int id, Inventory inventory, TelemetryTerminalBlockEntity terminal) {
        this(id, inventory, terminal.getBlockPos(), terminal.snapshot(), ContainerLevelAccess.create(terminal.getLevel(), terminal.getBlockPos()));
    }

    private TelemetryTerminalMenu(int id, Inventory inventory, BlockPos pos, TelemetryTerminalSnapshot snapshot, ContainerLevelAccess access) {
        super(ModMenus.GLARE_TELEMETRY_TERMINAL.get(), id);
        this.blockPos = pos;
        this.snapshot = snapshot;
        this.access = access;
    }

    public static TelemetryTerminalMenu fromNetwork(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        TelemetryTerminalSnapshot snapshot = TelemetryTerminalSnapshot.read(buf);
        BlockEntity be = inventory.player.level().getBlockEntity(pos);
        ContainerLevelAccess access = be instanceof TelemetryTerminalBlockEntity terminal
                ? ContainerLevelAccess.create(terminal.getLevel(), pos) : ContainerLevelAccess.NULL;
        return new TelemetryTerminalMenu(id, inventory, pos, snapshot, access);
    }

    public static void writeClientSideData(RegistryFriendlyByteBuf buf, TelemetryTerminalBlockEntity terminal) {
        buf.writeBlockPos(terminal.getBlockPos());
        terminal.snapshot().write(buf);
    }

    public BlockPos getBlockPos() { return blockPos; }
    public TelemetryTerminalSnapshot getSnapshot() { return snapshot; }
    public void applySnapshot(TelemetryTerminalSnapshot snapshot) {
        if (snapshot.revision() >= this.snapshot.revision()) this.snapshot = snapshot;
    }

    @Override public boolean stillValid(Player player) { return stillValid(access, player, ModBlocks.GLARE_TELEMETRY_TERMINAL.get()); }
    @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
}
