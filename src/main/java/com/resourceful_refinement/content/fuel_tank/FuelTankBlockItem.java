package com.resourceful_refinement.content.fuel_tank;

import com.resourceful_refinement.utilities.FluidGelTooltipHelper;
import com.resourceful_refinement.registry.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FuelTankBlockItem extends BlockItem {

    public FuelTankBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        SimpleFluidContent content = stack.get(ModDataComponents.FUEL_TANK_FLUID.get());
        if (content != null && !content.isEmpty()) {
            FluidGelTooltipHelper.addItemFluidLines(tooltip, content.copy(), FuelTankBlockEntity.TANK_CAPACITY, 0xb9b9b9);
        } else {
            CustomData customData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
            if (customData != null && customData.contains("Tank")) {
                FluidTank tank = new FluidTank(FuelTankBlockEntity.TANK_CAPACITY);
                tank.readFromNBT(context.registries(), customData.copyTag().getCompound("Tank"));
                FluidStack fluid = tank.getFluid();
                if (!fluid.isEmpty()) {
                    FluidGelTooltipHelper.addItemFluidLines(tooltip, fluid, FuelTankBlockEntity.TANK_CAPACITY, 0xb9b9b9);
                }
            }
        }
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, @Nullable Player player, ItemStack stack, BlockState state) {
        boolean result = super.updateCustomBlockEntityTag(pos, level, player, stack, state);
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof FuelTankBlockEntity fuelTank) {
                SimpleFluidContent content = stack.get(ModDataComponents.FUEL_TANK_FLUID.get());
                if (content != null && !content.isEmpty()) {
                    fuelTank.tank.setFluid(content.copy());
                }
            }
        }
        return result;
    }

    public static class FuelTankItemFluidHandler extends FluidHandlerItemStack {
        public FuelTankItemFluidHandler(ItemStack container) {
            super(ModDataComponents.FUEL_TANK_FLUID, container, FuelTankBlockEntity.TANK_CAPACITY);
        }
    }
}
