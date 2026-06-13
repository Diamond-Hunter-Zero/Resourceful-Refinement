package com.resourceful_refinement.content.combustion_chamber;

import com.mojang.serialization.MapCodec;
import com.resourceful_refinement.content.distillery.DistilleryBlockEntity;
import com.resourceful_refinement.registry.ModBlockEntities;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.Nullable;

public class CombustionChamberBlock extends KineticBlock implements IBE<CombustionChamberBlockEntity> {

    public static final MapCodec<CombustionChamberBlock> CODEC = simpleCodec(CombustionChamberBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;


    // -------------------------------------------------------------------------
    // Block Instantiation
    // -------------------------------------------------------------------------
    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new CombustionChamberBlockEntity(ModBlockEntities.COMBUSTION_CHAMBER_BE.get(), blockPos, blockState);
    }

    public CombustionChamberBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends KineticBlock> codec() {
        return CODEC;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public Class<CombustionChamberBlockEntity> getBlockEntityClass() {
        return CombustionChamberBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CombustionChamberBlockEntity> getBlockEntityType() {
        return ModBlockEntities.COMBUSTION_CHAMBER_BE.get();
    }


    // -------------------------------------------------------------------------
    // Placement Updates
    // -------------------------------------------------------------------------
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        withBlockEntityDo(level, pos, CombustionChamberBlockEntity::updateHeatAdjacency);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        withBlockEntityDo(level, pos, CombustionChamberBlockEntity::updateHeatAdjacency);
    }



    // -------------------------------------------------------------------------
    // Spatial Properties
    // -------------------------------------------------------------------------
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face == state.getValue(FACING);
    }
}
