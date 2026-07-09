package com.resourceful_refinement.content.pug;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class LaunchpadProxyBlock extends BaseEntityBlock {
    public static final MapCodec<LaunchpadProxyBlock> CODEC = simpleCodec(LaunchpadProxyBlock::new);

    public LaunchpadProxyBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LaunchpadProxyBlockEntity(pos, state);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && !state.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof LaunchpadProxyBlockEntity proxy) {
            LaunchpadControllerBlockEntity controller = proxy.getController(level);
            if (controller != null && !controller.isDisassembling()) {
                level.destroyBlock(controller.getBlockPos(), true);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
