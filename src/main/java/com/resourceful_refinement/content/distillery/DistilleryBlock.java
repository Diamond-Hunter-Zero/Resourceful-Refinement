package com.resourceful_refinement.content.distillery;

import com.mojang.serialization.MapCodec;
import com.resourceful_refinement.content.sieve.MechanicalFluidSieveBlockEntity;
import com.resourceful_refinement.registry.ModBlockEntities;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.Nullable;

public class DistilleryBlock extends HorizontalDirectionalBlock implements EntityBlock, IBE<DistilleryBlockEntity> {

    public static final MapCodec<DistilleryBlock> CODEC = simpleCodec(DistilleryBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty MODEL_TYPE = IntegerProperty.create("model_type", 0, 3);


    // -------------------------------------------------------------------------
    // Block Instantiation
    // -------------------------------------------------------------------------
    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new DistilleryBlockEntity(ModBlockEntities.DISTILLERY_BE.get(), blockPos, blockState);
    }

    public DistilleryBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
        builder.add(MODEL_TYPE);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }


    // -------------------------------------------------------------------------
    // Placement Updates
    // -------------------------------------------------------------------------
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        withBlockEntityDo(level, pos, DistilleryBlockEntity::updateConnectivity);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            super.onRemove(state, level, pos, newState, isMoving);
            // Updating neighbors will trigger neighborChanged on remaining stack parts
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        withBlockEntityDo(level, pos, DistilleryBlockEntity::updateConnectivity);
    }


    // -------------------------------------------------------------------------
    // Block Entity Getters
    // -------------------------------------------------------------------------
    @Override
    public Class<DistilleryBlockEntity> getBlockEntityClass() {
        return DistilleryBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends DistilleryBlockEntity> getBlockEntityType() {
        return ModBlockEntities.DISTILLERY_BE.get();
    }
}
