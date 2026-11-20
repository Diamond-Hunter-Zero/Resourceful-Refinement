package com.resourceful_refinement.content.tesla_coil;

import com.mojang.serialization.MapCodec;
import com.resourceful_refinement.registry.ModBlockEntities;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.createmod.catnip.math.VoxelShaper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Tesla Coil: a 6-way kinetic block whose FACING is its local up (the head) and which takes shaft input on its local
 * down face. Placement is inherited from {@link DirectionalKineticBlock}, so placing it against a shaft points the head
 * away from that shaft. Everything visible is drawn by {@link TeslaCoilRenderer}, so the head can spin with the network.
 */
public class TeslaCoilBlock extends DirectionalKineticBlock implements IBE<TeslaCoilBlockEntity> {
    public static final MapCodec<TeslaCoilBlock> CODEC = simpleCodec(TeslaCoilBlock::new);

    /** Neck on the shaft face, dome above it; modelled for FACING = UP. */
    private static final VoxelShaper SHAPES = VoxelShaper.forDirectional(Shapes.or(
            Block.box(5, 0, 5, 11, 5, 11),
            Block.box(2, 5, 2, 14, 14, 14)), Direction.UP);

    public TeslaCoilBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends DirectionalKineticBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return IBE.super.newBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face == state.getValue(FACING).getOpposite();
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public Class<TeslaCoilBlockEntity> getBlockEntityClass() {
        return TeslaCoilBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends TeslaCoilBlockEntity> getBlockEntityType() {
        return ModBlockEntities.TESLA_COIL_BE.get();
    }
}
