package com.resourceful_refinement.content.plushie;

import com.resourceful_refinement.content.geyser.GeyserItemRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.List;
import java.util.function.Consumer;

public class PlushieItem extends BlockItem {
    public PlushieItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return PlushieItemRenderer.INSTANCE;
            }
        });
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        // Add your custom translation key
        tooltipComponents.add(Component.literal("§7An adorably mass-marketable vulpine"));
        tooltipComponents.add(Component.literal("§7companion for your engineering endeavours"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
