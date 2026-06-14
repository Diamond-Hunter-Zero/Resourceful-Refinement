package com.resourceful_refinement.content.combustion_chamber;

import com.mojang.serialization.MapCodec;
import com.resourceful_refinement.registry.ModBlockEntities;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CombustionChamberBlock extends KineticBlock implements IBE<CombustionChamberBlockEntity> {

    public static final MapCodec<CombustionChamberBlock> CODEC = simpleCodec(CombustionChamberBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape Z_BOUNDING_AABB = Shapes.or(Block.box(0, 0, 0, 16, 10, 16), Block.box(3, 10, 0, 13, 16, 16));
    private static final VoxelShape X_BOUNDING_AABB = Shapes.or(Block.box(0, 0, 0, 16, 10, 16), Block.box(0, 10, 3, 16, 16, 13));
    private static final VoxelShape SUPPORT_SHAPE = Shapes.block();

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
    // Block Shapes
    // -------------------------------------------------------------------------
    @NotNull
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        if (facing == Direction.NORTH || facing == Direction.SOUTH)
            return Z_BOUNDING_AABB;
        else
            return X_BOUNDING_AABB;
    }

    @NotNull
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        if (facing == Direction.NORTH || facing == Direction.SOUTH)
            return Z_BOUNDING_AABB;
        else
            return X_BOUNDING_AABB;
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        return SUPPORT_SHAPE;
    }

    @Override
    protected boolean isCollisionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }


    // -------------------------------------------------------------------------
    // Placement Updates
    // -------------------------------------------------------------------------
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        withBlockEntityDo(level, pos, be -> {
            be.updateConnectivity();
            be.updateHeatAdjacency();
            be.updateRedstonePower();
        });
        updateChainAt(level, pos.relative(state.getValue(FACING)));
        updateChainAt(level, pos.relative(state.getValue(FACING).getOpposite()));
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        withBlockEntityDo(level, pos, be -> {
            be.updateConnectivity();
            be.updateHeatAdjacency();
            be.updateRedstonePower();
        });
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            Direction facing = state.getValue(FACING);
            updateChainAt(level, pos.relative(facing));
            updateChainAt(level, pos.relative(facing.getOpposite()));
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    private void updateChainAt(Level level, BlockPos pos) {
        if (level.isClientSide) return;
        if (level.getBlockEntity(pos) instanceof CombustionChamberBlockEntity chamber) {
            chamber.updateConnectivity();
            chamber.updateHeatAdjacency();
            chamber.updateRedstonePower();
        }
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
        if (world.getBlockEntity(pos) instanceof CombustionChamberBlockEntity chamber) {
            return chamber.isOutputEngine() && face == state.getValue(FACING);
        }
        return face == state.getValue(FACING);
    }
}
