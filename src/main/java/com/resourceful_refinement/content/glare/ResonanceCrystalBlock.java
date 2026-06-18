package com.resourceful_refinement.content.glare;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ResonanceCrystalBlock extends Block {
    public static final MapCodec<ResonanceCrystalBlock> CODEC = simpleCodec(properties -> new ResonanceCrystalBlock(properties, false));
    private final boolean artificial;

    public ResonanceCrystalBlock(Properties properties, boolean artificial) {
        super(properties);
        this.artificial = artificial;
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (artificial && level instanceof ServerLevel server && server.dimension() == Level.END && !oldState.is(state.getBlock())) {
            server.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6.0F, Level.ExplosionInteraction.BLOCK);
        }
    }
}
