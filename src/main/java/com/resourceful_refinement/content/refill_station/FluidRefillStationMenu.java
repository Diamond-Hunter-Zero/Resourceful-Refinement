package com.resourceful_refinement.content.refill_station;

import com.resourceful_refinement.registry.ModBlocks;
import com.resourceful_refinement.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class FluidRefillStationMenu extends AbstractContainerMenu {

    private final BlockPos blockPos;
    private final ContainerLevelAccess access;
    @Nullable
    private final FluidRefillStationBlockEntity blockEntity;

    public FluidRefillStationMenu(int containerId, Inventory playerInventory, FluidRefillStationBlockEntity blockEntity) {
        super(ModMenus.FLUID_REFILL_STATION.get(), containerId);
        this.blockEntity = blockEntity;
        this.blockPos = blockEntity.getBlockPos();
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
    }

    public static FluidRefillStationMenu fromNetwork(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        BlockEntity be = playerInventory.player.level().getBlockEntity(pos);
        if (be instanceof FluidRefillStationBlockEntity station) {
            return new FluidRefillStationMenu(containerId, playerInventory, station);
        }
        throw new IllegalStateException("Missing Fluid Refill Station at " + pos);
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public String getInitialTrackingId() {
        return blockEntity != null ? blockEntity.getTrackingId() : "";
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.FLUID_REFILL_STATION.get());
    }

    @Override
    public net.minecraft.world.item.ItemStack quickMoveStack(Player player, int index) {
        return net.minecraft.world.item.ItemStack.EMPTY;
    }
}
