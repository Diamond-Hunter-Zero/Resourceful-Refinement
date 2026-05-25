package com.resourceful_refinement.content.forge_mould;

import com.resourceful_refinement.content.casting_depot.rendering.CastingDepotItemRenderer;
import com.resourceful_refinement.registry.ModItems;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class MechanicalForgeMouldItem extends BlockItem {
    public MechanicalForgeMouldItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return ForgeMouldItemRenderer.INSTANCE;
            }
        });
    }

    @Override
    public BlockPlaceContext updatePlacementContext(BlockPlaceContext context) {
        if (context.getClickedFace() != Direction.UP || (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()))
            return super.updatePlacementContext(context);

        BlockPos clickedPos = context.getClickedPos();
        // The block we are looking at is clickedPos.below()
        BlockPos basePos = clickedPos.below();
        
        // If the block we are placing ON has an item handler, we want to leave a gap
        if (context.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, basePos, Direction.UP) != null) {
            BlockPos targetPos = clickedPos.above();
            if (context.getLevel().getBlockState(targetPos).canBeReplaced(context)) {
                return BlockPlaceContext.at(context, targetPos, Direction.UP);
            }
        }

        return super.updatePlacementContext(context);
    }
}
