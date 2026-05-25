package com.resourceful_refinement.content.casting_depot;

import com.resourceful_refinement.content.casting_depot.rendering.CastingDepotItemRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class CastingDepotItem extends BlockItem {
    public CastingDepotItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return CastingDepotItemRenderer.INSTANCE;
            }
        });
    }
}
