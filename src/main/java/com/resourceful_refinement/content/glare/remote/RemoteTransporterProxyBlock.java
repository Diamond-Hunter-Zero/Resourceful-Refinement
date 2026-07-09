package com.resourceful_refinement.content.glare.remote;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class RemoteTransporterProxyBlock extends BaseEntityBlock {
    public static final MapCodec<RemoteTransporterProxyBlock> CODEC = simpleCodec(RemoteTransporterProxyBlock::new);
    private final boolean solid;

    public RemoteTransporterProxyBlock(Properties properties) {
        this(properties, true);
    }

    public RemoteTransporterProxyBlock(Properties properties, boolean solid) {
        super(properties);
        this.solid = solid;
    }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }
    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return solid ? Shapes.block() : Shapes.empty();
    }
    @Override public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return solid ? Shapes.block() : Shapes.empty();
    }
    @Override public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return solid ? Shapes.block() : Shapes.empty();
    }
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RemoteTransporterProxyBlockEntity(pos, state);
    }

    @Override public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && !state.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof RemoteTransporterProxyBlockEntity proxy) {
            RemoteEntanglementTransporterBlockEntity controller = proxy.getController(level);
            if (controller != null && !controller.isDisassembling()) {
                level.destroyBlock(controller.getBlockPos(), true);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
