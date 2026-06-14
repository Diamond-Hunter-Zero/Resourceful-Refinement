package com.resourceful_refinement.content.radiator;

import com.mojang.serialization.MapCodec;
import com.resourceful_refinement.registry.ModBlockEntities;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.block.WrenchableDirectionalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.TickPriority;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RadiatorBlock extends WrenchableDirectionalBlock implements EntityBlock, SimpleWaterloggedBlock {

    public static final MapCodec<RadiatorBlock> CODEC = simpleCodec(RadiatorBlock::new);
    public static final IntegerProperty HEAT_STATE = IntegerProperty.create("heat_state", 0, 5);

    private static final VoxelShape Z_AXIS_AABB = Block.box(2, 2, 0, 14, 14, 16);
    private static final VoxelShape X_AXIS_AABB = Block.box(0, 2, 2, 16, 14, 14);
    private static final VoxelShape Y_AXIS_AABB = Block.box(2, 0, 2, 14, 16, 14);

    public RadiatorBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.UP)
                .setValue(HEAT_STATE, 2)
                .setValue(BlockStateProperties.WATERLOGGED, false)
                .setValue(BlazeBurnerBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.NONE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.WATERLOGGED);
        super.createBlockStateDefinition(builder);
        builder.add(HEAT_STATE);
        builder.add(BlazeBurnerBlock.HEAT_LEVEL);
    }

    @Override
    protected @NotNull MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RadiatorBlockEntity(ModBlockEntities.RADIATOR_PIPE_BE.get(), pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if (be instanceof RadiatorBlockEntity radiator) {
                RadiatorBlockEntity.serverTick(lvl, pos, st, radiator);
            }
        };
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction.Axis radiatorAxis = state.getValue(FACING).getAxis();

        if (radiatorAxis == Direction.Axis.X)
            return X_AXIS_AABB;
        else if (radiatorAxis == Direction.Axis.Z)
            return Z_AXIS_AABB;

        return Y_AXIS_AABB;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false)
                : Fluids.EMPTY.defaultFluidState();
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState, LevelAccessor world,
                                  BlockPos pos, BlockPos neighbourPos) {
        if (state.getValue(BlockStateProperties.WATERLOGGED))
            world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        return state;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        // Default to looking direction (facing)
        Direction facing = context.getNearestLookingDirection();
        if (context.getPlayer() != null && !context.getPlayer().isShiftKeyDown()) {
            facing = facing.getOpposite();
        }
        return defaultBlockState()
                .setValue(FACING, facing)
                .setValue(BlockStateProperties.WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (level.isClientSide) return;
        if (state.getBlock() != oldState.getBlock()) {
            // Force vanilla neighbor update
            level.updateNeighborsAt(pos, this);
            // Force Create to rebuild the pipe network graph around this block
            FluidPropagator.propagateChangedPipe(level, pos, state);
            // Also schedule a tick on each neighbouring pipe so they re-evaluate
            for (Direction dir : Direction.values()) {
                BlockPos neighbourPos = pos.relative(dir);
                BlockState neighbourState = level.getBlockState(neighbourPos);
                FluidTransportBehaviour neighbourPipe = FluidPropagator.getPipe(level, neighbourPos);
                if (neighbourPipe != null) {
                    level.scheduleTick(neighbourPos, neighbourState.getBlock(), 1, TickPriority.HIGH);
                }
            }
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block otherBlock,
                                BlockPos neighborPos, boolean isMoving) {
        DebugPackets.sendNeighborsUpdatePacket(level, pos);
        if (level.isClientSide) return;
        level.scheduleTick(pos, this, 1, TickPriority.HIGH);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource r) {
        FluidPropagator.propagateChangedPipe(level, pos, state);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide() && state.getBlock() != newState.getBlock()) {
            level.updateNeighborsAt(pos, this);
            FluidPropagator.propagateChangedPipe(level, pos, state);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

}
