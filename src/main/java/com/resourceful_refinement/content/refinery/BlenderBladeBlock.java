package com.resourceful_refinement.content.refinery;

import com.resourceful_refinement.registry.ModBlockEntities;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BlenderBladeBlock extends RotatedPillarKineticBlock implements IBE<BlenderBladeBlockEntity> {

    public static final MapCodec<BlenderBladeBlock> CODEC = simpleCodec(BlenderBladeBlock::new);

    protected static final VoxelShape X_AXIS_AABB = Block.box(0, 5, 5, 16, 11, 11);
    protected static final VoxelShape Y_AXIS_AABB = Block.box(5, 0, 5, 11, 16, 11);
    protected static final VoxelShape Z_AXIS_AABB = Block.box(5, 5, 0, 11, 11, 16);

    public BlenderBladeBlock(Properties properties) {
        super(properties);
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
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        Direction.Axis axis = state.getValue(AXIS);
        return switch (axis) {
            case X -> X_AXIS_AABB;
            case Z -> Z_AXIS_AABB;
            default -> Y_AXIS_AABB;
        };
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.getValue(AXIS);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(AXIS);
    }

    @Override
    public Class<BlenderBladeBlockEntity> getBlockEntityClass() {
        return BlenderBladeBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends BlenderBladeBlockEntity> getBlockEntityType() {
        return ModBlockEntities.BLENDER_BLADE.get();
    }
}
