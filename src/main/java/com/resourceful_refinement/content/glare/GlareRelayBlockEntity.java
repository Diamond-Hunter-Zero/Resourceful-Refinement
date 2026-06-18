package com.resourceful_refinement.content.glare;

import com.resourceful_refinement.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class GlareRelayBlockEntity extends GlareNodeBlockEntity {

    public static final int MAX_LINK_COUNT = 8;

    public GlareRelayBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.GLARE_RELAY_BE.get(), pos, blockState, MAX_LINK_COUNT);
    }
}
