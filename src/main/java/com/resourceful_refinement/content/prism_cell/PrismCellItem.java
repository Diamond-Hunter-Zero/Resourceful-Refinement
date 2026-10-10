package com.resourceful_refinement.content.prism_cell;

import com.resourceful_refinement.registry.ModDataComponents;
import com.resourceful_refinement.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * A Prism Cell item: an interchangeable, disposable battery. All four grades share this class and the shared
 * {@code item/prism_cell} geometry model; they differ only by texture and by the preset
 * {@link PrismCellBatteryComponent} default (applied in {@link ModItems}). The current charge is shown as a tooltip
 * line and a durability-style bar.
 */
public class PrismCellItem extends Item {

    private final PrismCellTier tier;

    public PrismCellItem(PrismCellTier tier, Properties properties) {
        super(properties);
        this.tier = tier;
    }

    public PrismCellTier tier() {
        return tier;
    }

    /** Maps a grade to its registered item — used when a consumer block rebuilds a cell item on removal. */
    public static Item byTier(PrismCellTier tier) {
        return switch (tier) {
            case CREATIVE -> ModItems.CREATIVE_PRISM_CELL.get();
            case LOW -> ModItems.LOW_GRADE_PRISM_CELL.get();
            case MID -> ModItems.MID_GRADE_PRISM_CELL.get();
            case HIGH -> ModItems.HIGH_GRADE_PRISM_CELL.get();
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        PrismCellBatteryComponent battery = stack.get(ModDataComponents.PRISM_CELL_BATTERY.get());
        if (battery == null) {
            return;
        }
        if (battery.isCreative()) {
            tooltip.add(Component.translatable("tooltip.resourceful_refinement.prism_cell.creative_charge")
                    .withStyle(ChatFormatting.AQUA));
        } else {
            tooltip.add(Component.translatable("tooltip.resourceful_refinement.prism_cell.charge",
                    battery.charge(), battery.maxCharge()).withStyle(ChatFormatting.AQUA));
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        PrismCellBatteryComponent battery = stack.get(ModDataComponents.PRISM_CELL_BATTERY.get());
        return battery != null && !battery.isCreative() && battery.charge() < battery.maxCharge();
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        PrismCellBatteryComponent battery = stack.get(ModDataComponents.PRISM_CELL_BATTERY.get());
        if (battery == null || battery.maxCharge() <= 0) {
            return 0;
        }
        return Mth.clamp(Math.round(13.0F * battery.charge() / battery.maxCharge()), 0, 13);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x3CC8E6; // prism cyan
    }
}
