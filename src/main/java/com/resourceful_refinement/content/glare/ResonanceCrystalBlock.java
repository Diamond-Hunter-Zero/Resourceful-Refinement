package com.resourceful_refinement.content.glare;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ResonanceCrystalBlock extends Block {
    public static final MapCodec<ResonanceCrystalBlock> CODEC = simpleCodec(properties -> new ResonanceCrystalBlock(properties, false));
    private final boolean artificial;

    private static final VoxelShape CRYSTAL_SHAPE = Block.box(5, 0, 5, 11, 14, 11);


    public ResonanceCrystalBlock(Properties properties, boolean artificial) {
        super(properties);
        this.artificial = artificial;
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return CRYSTAL_SHAPE;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);

        // If artificial, detonate when placed in the End
        if (artificial && level instanceof ServerLevel server && server.dimension() == Level.END && !oldState.is(state.getBlock())) {
            server.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6.0F, Level.ExplosionInteraction.BLOCK);
        }
    }
}
