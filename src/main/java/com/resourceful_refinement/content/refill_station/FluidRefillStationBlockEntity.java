package com.resourceful_refinement.content.refill_station;

import com.resourceful_refinement.content.gel_splatter.FluidGelTooltipHelper;
import com.resourceful_refinement.content.gel_tracking.GelTrackingService;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.List;

import static com.resourceful_refinement.content.hosegun.HosegunItem.BOUND_TEXT_COLOUR;

public class FluidRefillStationBlockEntity extends BlockEntity implements MenuProvider, IHaveGoggleInformation {

    public static final int TANK_CAPACITY = 1000;
    public static final int MAX_TRACKING_ID_LENGTH = 32;

    public final FluidTank tank = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            syncToClient();
        }
    };

    private String trackingId = "";
    private boolean wasRedstonePowered;

    public FluidRefillStationBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public String getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(String trackingId) {
        String sanitised = sanitiseTrackingId(trackingId);
        if (sanitised.equals(this.trackingId)) {
            return;
        }
        String previous = this.trackingId;
        this.trackingId = sanitised;
        if (level instanceof ServerLevel server) {
            GelTrackingService.onStationTrackingIdChanged(server, worldPosition, previous, sanitised);
        }
        syncToClient();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            wasRedstonePowered = level.hasNeighborSignal(worldPosition);
            syncPoweredBlockState(wasRedstonePowered);
        }
        if (level instanceof ServerLevel server && hasTrackingId()) {
            GelTrackingService.onStationLoaded(server, worldPosition, trackingId);
        }
    }

    @Override
    public void setRemoved() {
        if (level instanceof ServerLevel server) {
            GelTrackingService.onStationRemoved(server, worldPosition);
        }
        super.setRemoved();
    }

    public static String sanitiseTrackingId(String raw) {
        if (raw == null) {
            return "";
        }
        String trimmed = raw.trim();
        if (trimmed.length() > MAX_TRACKING_ID_LENGTH) {
            trimmed = trimmed.substring(0, MAX_TRACKING_ID_LENGTH);
        }
        StringBuilder builder = new StringBuilder(trimmed.length());
        for (int i = 0; i < trimmed.length(); i++) {
            char character = trimmed.charAt(i);
            if (character >= 32 && character != 127) {
                builder.append(character);
            }
        }
        return builder.toString();
    }

    public boolean isWithinUsableDistance(Player player) {
        if (level == null) {
            return false;
        }
        return player.distanceToSqr(
                worldPosition.getX() + 0.5,
                worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.resourceful_refinement.fluid_refill_station");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new FluidRefillStationMenu(containerId, inventory, this);
    }

    @Override
    public void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buf) {
        FluidRefillStationMenu.writeClientSideData(buf, this);
    }

    public boolean hasTrackingId() {
        return trackingId != null && !trackingId.isEmpty();
    }

    public float getFillRatio() {
        if (tank.isEmpty()) {
            return 0.0F;
        }
        return (float) tank.getFluidAmount() / TANK_CAPACITY;
    }

    /** Called when a neighbour block updates; schedules gel purge on redstone rising edge. */
    public void onNeighborChanged() {
        if (level == null || level.isClientSide) {
            return;
        }
        boolean powered = level.hasNeighborSignal(worldPosition);
        if (powered && !wasRedstonePowered && hasTrackingId() && level instanceof ServerLevel server) {
            if (GelTrackingService.getGelCount(server, trackingId) > 0) {
                GelTrackingService.scheduleGelPurge(server, trackingId);
                server.playSound(null, worldPosition, SoundEvents.CONDUIT_DEACTIVATE, SoundSource.BLOCKS, 0.85F, 1.0F);
            }
        }
        syncPoweredBlockState(powered);
        wasRedstonePowered = powered;
    }

    private void syncPoweredBlockState(boolean powered) {
        if (level == null || level.isClientSide) {
            return;
        }
        BlockState state = getBlockState();
        if (state.getValue(FluidRefillStationBlock.POWERED) == powered) {
            return;
        }
        level.setBlock(worldPosition, state.setValue(FluidRefillStationBlock.POWERED, powered), Block.UPDATE_CLIENTS);
    }

    protected void syncToClient() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Tank", tank.writeToNBT(registries, new CompoundTag()));
        if (hasTrackingId()) {
            tag.putString("TrackingId", trackingId);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Tank")) {
            tank.readFromNBT(registries, tag.getCompound("Tank"));
        }
        trackingId = tag.contains("TrackingId") ? tag.getString("TrackingId") : "";
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {

        tooltip.add(Component.literal("     Fluid Refill Station:"));
        if (hasTrackingId())
            tooltip.add(Component.literal("Tracking: " + getTrackingId()).withColor(BOUND_TEXT_COLOUR));


        FluidStack tankOutputFluid = tank.getFluid();
        if (tankOutputFluid.isEmpty())
            tooltip.add(Component.literal("§6Tank: §8empty"));
        else 
            FluidGelTooltipHelper.addGoggleFluidLines(tooltip, tankOutputFluid, true);
        
        return true;
    }

}
