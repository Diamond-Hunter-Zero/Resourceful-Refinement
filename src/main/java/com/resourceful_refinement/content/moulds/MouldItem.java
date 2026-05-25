package com.resourceful_refinement.content.moulds;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class MouldItem extends Item {
    public MouldItem(Properties properties, float breakChance) {
        super(properties);
        this.breakChance = breakChance;
    }

    private float breakChance = 0;

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        // Add your custom translation key
        tooltipComponents.add(Component.literal("§7" + ((int)(breakChance * 100)) + "% chance to break when"));
        tooltipComponents.add(Component.literal("§7used at a Mechanical Forge"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
