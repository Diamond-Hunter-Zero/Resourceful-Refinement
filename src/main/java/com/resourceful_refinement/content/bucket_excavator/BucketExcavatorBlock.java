package com.resourceful_refinement.content.bucket_excavator;

import com.mojang.serialization.MapCodec;
import com.resourceful_refinement.content.glare.DimensionalNodePos;
import com.resourceful_refinement.registry.ModBlockEntities;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.Nullable;

public class BucketExcavatorBlock extends KineticBlock implements IBE<BucketExcavatorBlockEntity> {

    public static final MapCodec<BucketExcavatorBlock> CODEC = simpleCodec(BucketExcavatorBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public BucketExcavatorBlock(Properties properties) {
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
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }


    // -------------------------------------------------------------------------
    // Block Entity & Rendering
    // -------------------------------------------------------------------------

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BucketExcavatorBlockEntity(ModBlockEntities.BUCKET_EXCAVATOR_BE.get(), pos, state);
    }

    @Override
    public Class<BucketExcavatorBlockEntity> getBlockEntityClass() {
        return BucketExcavatorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends BucketExcavatorBlockEntity> getBlockEntityType() {
        return ModBlockEntities.BUCKET_EXCAVATOR_BE.get();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }


    // -------------------------------------------------------------------------
    // Kinetics
    // -------------------------------------------------------------------------

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getClockWise().getAxis();
    }

    @Override
    public boolean hasShaftTowards(net.minecraft.world.level.LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == getRotationAxis(state);
    }


    // -------------------------------------------------------------------------
    // World Updates
    // -------------------------------------------------------------------------

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (level instanceof ServerLevel server)
        {
            ExcavatorRegionSavedData.ExcavatorRecord newRecord = ExcavatorRegionSavedData.RegisterOrUpdateExcavator(server, DimensionalNodePos.of(server, pos), state.getValue(FACING));
            BucketExcavatorBlockEntity excavatorBe = getBlockEntity(server, pos);
            if (excavatorBe != null)
                excavatorBe .excavationData = newRecord;
        }
    }

    @Override
    public BlockState updateAfterWrenched(BlockState newState, UseOnContext context) {

        if (context.getLevel() instanceof ServerLevel server)
        {
            ExcavatorRegionSavedData.ExcavatorRecord newRecord = ExcavatorRegionSavedData.RegisterOrUpdateExcavator(server, DimensionalNodePos.of(server, context.getClickedPos()), newState.getValue(FACING));
            BucketExcavatorBlockEntity excavatorBe = getBlockEntity(server, context.getClickedPos());
            if (excavatorBe != null)
                excavatorBe .excavationData = newRecord;
        }

        return super.updateAfterWrenched(newState, context);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel server
                && level.getBlockEntity(pos) instanceof BucketExcavatorBlockEntity be) {
            ExcavatorRegionSavedData.RemoveExcavator(server, DimensionalNodePos.of(server, pos));
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

}
