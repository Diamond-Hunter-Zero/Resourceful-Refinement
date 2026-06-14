package com.resourceful_refinement.content.fuel_tank;

import com.resourceful_refinement.content.combustion_chamber.CombustionChamberBlockEntity;
import com.resourceful_refinement.content.gel_splatter.FluidGelTooltipHelper;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.List;

public class FuelTankBlockEntity extends BlockEntity implements IHaveGoggleInformation {

    public static final int TANK_CAPACITY = 4000;
    private static final int MAX_PUSH_PER_TICK = 50;

    public final FluidTank tank = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            syncToClient();
        }
    };

    public FuelTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FuelTankBlockEntity be) {
        if (!be.tank.isEmpty()) {
            be.pushToAdjacentCombustionChambers();
        }
    }

    private void pushToAdjacentCombustionChambers() {
        if (level == null || level.isClientSide || tank.isEmpty()) {
            return;
        }

        for (Direction direction : Direction.values()) {
            if (tank.isEmpty()) {
                return;
            }

            BlockPos neighbourPos = worldPosition.relative(direction);
            if (!(level.getBlockEntity(neighbourPos) instanceof CombustionChamberBlockEntity)) {
                continue;
            }

            IFluidHandler target = level.getCapability(Capabilities.FluidHandler.BLOCK, neighbourPos, direction.getOpposite());
            if (target == null) {
                continue;
            }

            FluidStack offer = tank.getFluid().copy();
            offer.setAmount(Math.min(offer.getAmount(), MAX_PUSH_PER_TICK));

            int accepted = target.fill(offer, IFluidHandler.FluidAction.SIMULATE);
            if (accepted <= 0) {
                continue;
            }

            FluidStack drained = tank.drain(accepted, IFluidHandler.FluidAction.EXECUTE);
            if (drained.isEmpty()) {
                continue;
            }

            int filled = target.fill(drained, IFluidHandler.FluidAction.EXECUTE);
            if (filled < drained.getAmount()) {
                FluidStack remainder = drained.copy();
                remainder.setAmount(drained.getAmount() - filled);
                tank.fill(remainder, IFluidHandler.FluidAction.EXECUTE);
            }
        }
    }

    public float getFillRatio() {
        if (tank.isEmpty()) {
            return 0.0F;
        }
        return (float) tank.getFluidAmount() / TANK_CAPACITY;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        tooltip.add(Component.literal("     Fuel Tank:"));

        FluidStack fluid = tank.getFluid();
        if (fluid.isEmpty()) {
            tooltip.add(Component.literal("Tank: \u00a78empty"));
        } else {
            FluidGelTooltipHelper.addGoggleFluidLines(tooltip, fluid, true);
        }

        return true;
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
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Tank")) {
            tank.readFromNBT(registries, tag.getCompound("Tank"));
        }
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
}
