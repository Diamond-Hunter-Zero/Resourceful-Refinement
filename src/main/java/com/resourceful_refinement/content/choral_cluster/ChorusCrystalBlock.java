package com.resourceful_refinement.content.choral_cluster;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ChorusCrystalBlock extends Block {
    public ChorusCrystalBlock(Properties properties) {
        super(properties);
    }

    @Override
    @Deprecated
    public boolean skipRendering(BlockState state, BlockState adjacentState, Direction direction) {
        // If the adjacent block is the exact same block, skip rendering the touching face
        if (adjacentState.is(this)) {
            return true;
        }
        return super.skipRendering(state, adjacentState, direction);
    }
}
