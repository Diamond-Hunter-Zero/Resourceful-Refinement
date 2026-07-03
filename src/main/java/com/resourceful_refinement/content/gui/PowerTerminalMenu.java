package com.resourceful_refinement.content.gui;

import com.resourceful_refinement.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class PowerTerminalMenu extends AbstractContainerMenu {
    private final BlockPos blockPos;
    private final BlockPos validationPos;

    public PowerTerminalMenu(int id, Inventory inventory, BlockPos blockPos) {
        this(id, inventory, blockPos, blockPos);
    }

    public PowerTerminalMenu(int id, Inventory inventory, BlockPos blockPos, BlockPos validationPos) {
        super(ModMenus.POWER_TERMINAL.get(), id);
        this.blockPos = blockPos;
        this.validationPos = validationPos;
    }

    public static PowerTerminalMenu fromNetwork(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        return new PowerTerminalMenu(id, inventory, buf.readBlockPos());
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.level().isLoaded(validationPos) && player.distanceToSqr(validationPos.getCenter()) <= 64.0D;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
