package com.resourceful_refinement.content.advanced_pump;

import com.resourceful_refinement.registry.ModBlockEntities;
import com.simibubi.create.content.fluids.pump.PumpBlock;
import com.simibubi.create.content.fluids.pump.PumpBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;

public class AdvancedPumpBlock extends PumpBlock {

    public static final BooleanProperty POWERED = BooleanProperty.create("powered");
    private static final VoxelShape X_FACE_SUPPORT_SHAPE = Shapes.or(
            Block.box(0, 0, 0, 1, 16, 16),
            Block.box(15, 0, 0, 16, 16, 16));
    private static final VoxelShape Z_FACE_SUPPORT_SHAPE = Shapes.or(
            Block.box(0, 0, 0, 16, 16, 1),
            Block.box(0, 0, 15, 16, 16, 16));

    public AdvancedPumpBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(POWERED, false));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AdvancedPumpBlockEntity(ModBlockEntities.ADVANCED_PUMP_BE.get(), pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state == null ? null : state.setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block otherBlock, BlockPos neighborPos,
                                boolean isMoving) {
        super.neighborChanged(state, level, pos, otherBlock, neighborPos, isMoving);

        if (level.isClientSide)
            return;

        if (level.getBlockEntity(pos) instanceof AdvancedPumpBlockEntity pump)
            pump.updateRedstonePower();
    }

    public static void updatePoweredState(Level level, BlockPos pos, BlockState state, boolean powered) {
        if (!state.hasProperty(POWERED) || state.getValue(POWERED) == powered)
            return;
        level.setBlock(pos, state.setValue(POWERED, powered), Block.UPDATE_CLIENTS);
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        if (!state.hasProperty(BlockStateProperties.FACING)) {
            return Shapes.empty();
        }

        Direction facing = state.getValue(BlockStateProperties.FACING);
        if (facing.getAxis().isVertical() || facing.getAxis() == Direction.Axis.Z) {
            return X_FACE_SUPPORT_SHAPE;
        }
        return Z_FACE_SUPPORT_SHAPE;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);

        if (level.isClientSide)
            return;

        if (level.getBlockEntity(pos) instanceof AdvancedPumpBlockEntity pump)
            pump.updateRedstonePower();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Class<PumpBlockEntity> getBlockEntityClass() {
        return (Class) AdvancedPumpBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends PumpBlockEntity> getBlockEntityType() {
        return ModBlockEntities.ADVANCED_PUMP_BE.get();
    }
}
