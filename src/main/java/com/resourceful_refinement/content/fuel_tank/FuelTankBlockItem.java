package com.resourceful_refinement.content.fuel_tank;

import com.resourceful_refinement.content.gel_splatter.FluidGelTooltipHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FuelTankBlockItem extends BlockItem {

    public FuelTankBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        CustomData customData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (customData != null && customData.contains("Tank")) {
            FluidTank tank = new FluidTank(FuelTankBlockEntity.TANK_CAPACITY);
            tank.readFromNBT(context.registries(), customData.copyTag().getCompound("Tank"));
            FluidStack fluid = tank.getFluid();
            if (!fluid.isEmpty()) {
                FluidGelTooltipHelper.addItemFluidLines(tooltip, fluid, FuelTankBlockEntity.TANK_CAPACITY, 0xFFFFFF, false);
            }
        }
    }

    public static class FuelTankItemFluidHandler implements IFluidHandlerItem {
        private final ItemStack container;

        public FuelTankItemFluidHandler(ItemStack container) {
            this.container = container;
        }

        @Override
        public @NotNull ItemStack getContainer() {
            return container;
        }

        private FluidTank getTank() {
            FluidTank tank = new FluidTank(FuelTankBlockEntity.TANK_CAPACITY);
            CustomData customData = container.get(DataComponents.BLOCK_ENTITY_DATA);
            if (customData != null && customData.contains("Tank")) {
                tank.readFromNBT(null, customData.copyTag().getCompound("Tank"));
            }
            return tank;
        }

        private void saveTank(FluidTank tank) {
            if (tank.isEmpty()) {
                CustomData customData = container.get(DataComponents.BLOCK_ENTITY_DATA);
                if (customData != null) {
                    CompoundTag tag = customData.copyTag();
                    tag.remove("Tank");
                    if (tag.isEmpty()) {
                        container.remove(DataComponents.BLOCK_ENTITY_DATA);
                    } else {
                        container.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(tag));
                    }
                }
            } else {
                CompoundTag tag = new CompoundTag();
                CustomData customData = container.get(DataComponents.BLOCK_ENTITY_DATA);
                if (customData != null) {
                    tag = customData.copyTag();
                }
                tag.put("Tank", tank.writeToNBT(null, new CompoundTag()));
                container.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(tag));
            }
        }

        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tankIdx) {
            return getTank().getFluid();
        }

        @Override
        public int getTankCapacity(int tankIdx) {
            return FuelTankBlockEntity.TANK_CAPACITY;
        }

        @Override
        public boolean isFluidValid(int tankIdx, @NotNull FluidStack stack) {
            return true;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty()) return 0;
            FluidTank tank = getTank();
            int filled = tank.fill(resource, action);
            if (filled > 0 && action.execute()) {
                saveTank(tank);
            }
            return filled;
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource.isEmpty()) return FluidStack.EMPTY;
            FluidTank tank = getTank();
            FluidStack drained = tank.drain(resource, action);
            if (!drained.isEmpty() && action.execute()) {
                saveTank(tank);
            }
            return drained;
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            if (maxDrain <= 0) return FluidStack.EMPTY;
            FluidTank tank = getTank();
            FluidStack drained = tank.drain(maxDrain, action);
            if (!drained.isEmpty() && action.execute()) {
                saveTank(tank);
            }
            return drained;
        }
    }
}
