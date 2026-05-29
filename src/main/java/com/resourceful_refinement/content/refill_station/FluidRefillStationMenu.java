package com.resourceful_refinement.content.refill_station;

import com.resourceful_refinement.content.gel_tracking.GelTrackingService;
import com.resourceful_refinement.registry.ModBlocks;
import com.resourceful_refinement.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FluidRefillStationMenu extends AbstractContainerMenu {

    private final BlockPos blockPos;
    private final ContainerLevelAccess access;
    @Nullable
    private final FluidRefillStationBlockEntity blockEntity;
    private final List<String> knownTrackingIds;

    public FluidRefillStationMenu(int containerId, Inventory playerInventory, FluidRefillStationBlockEntity blockEntity) {
        this(containerId, playerInventory, blockEntity, readKnownTrackingIds(blockEntity));
    }

    private FluidRefillStationMenu(int containerId, Inventory playerInventory, FluidRefillStationBlockEntity blockEntity,
            List<String> knownTrackingIds) {
        super(ModMenus.FLUID_REFILL_STATION.get(), containerId);
        this.blockEntity = blockEntity;
        this.blockPos = blockEntity.getBlockPos();
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
        this.knownTrackingIds = knownTrackingIds;
    }

    public static FluidRefillStationMenu fromNetwork(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<String> knownTrackingIds = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            knownTrackingIds.add(buf.readUtf());
        }
        BlockPos pos = buf.readBlockPos();
        BlockEntity be = playerInventory.player.level().getBlockEntity(pos);
        if (be instanceof FluidRefillStationBlockEntity station) {
            return new FluidRefillStationMenu(containerId, playerInventory, station, knownTrackingIds);
        }
        throw new IllegalStateException("Missing Fluid Refill Station at " + pos);
    }

    public static void writeClientSideData(RegistryFriendlyByteBuf buf, FluidRefillStationBlockEntity blockEntity) {
        List<String> ids = readKnownTrackingIds(blockEntity);
        buf.writeVarInt(ids.size());
        for (String id : ids) {
            buf.writeUtf(id);
        }
    }

    private static List<String> readKnownTrackingIds(FluidRefillStationBlockEntity blockEntity) {
        if (blockEntity.getLevel() instanceof ServerLevel server) {
            return GelTrackingService.getAllTrackingIds(server);
        }
        return Collections.emptyList();
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public String getInitialTrackingId() {
        return blockEntity != null ? blockEntity.getTrackingId() : "";
    }

    public List<String> getKnownTrackingIds() {
        return knownTrackingIds;
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
