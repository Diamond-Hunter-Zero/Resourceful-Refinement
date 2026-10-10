package com.resourceful_refinement.content.resonator;

import com.resourceful_refinement.content.prism_cell.PrismCell;
import com.resourceful_refinement.content.prism_cell.PrismCellBatteryComponent;
import com.resourceful_refinement.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Block item for the Resource Resonator. Carries any installed Prism Cell across a break/place cycle: the broken block
 * stores its cell in a {@link PrismCellBatteryComponent} on the dropped item (see
 * {@link ResourceResonatorBlock#getDrops}), this item shows that cell in its tooltip, and placing it back restores the
 * cell into the new block entity.
 */
public class ResourceResonatorBlockItem extends BlockItem {

    public ResourceResonatorBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        PrismCellBatteryComponent battery = stack.get(ModDataComponents.PRISM_CELL_BATTERY.get());
        if (battery == null) {
            return;
        }
        if (battery.isCreative()) {
            tooltip.add(Component.translatable("tooltip.resourceful_refinement.prism_cell.installed_creative")
                    .withStyle(ChatFormatting.AQUA));
        } else {
            tooltip.add(Component.translatable("tooltip.resourceful_refinement.prism_cell.installed",
                    battery.charge(), battery.maxCharge()).withStyle(ChatFormatting.AQUA));
        }
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, @Nullable Player player, ItemStack stack, BlockState state) {
        boolean result = super.updateCustomBlockEntityTag(pos, level, player, stack, state);
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ResourceResonatorBlockEntity resonator) {
                PrismCellBatteryComponent battery = stack.get(ModDataComponents.PRISM_CELL_BATTERY.get());
                if (battery != null) {
                    resonator.setPrismCell(PrismCell.fromComponent(battery));
                    resonator.onPrismCellChanged();
                }
            }
        }
        return result;
    }
}
