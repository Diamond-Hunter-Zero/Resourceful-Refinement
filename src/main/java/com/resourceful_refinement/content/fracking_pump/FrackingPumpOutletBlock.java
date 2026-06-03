package com.resourceful_refinement.content.fracking_pump;

import com.mojang.serialization.MapCodec;
import com.resourceful_refinement.registry.ModBlockEntities;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.Nullable;

public class FrackingPumpOutletBlock extends KineticBlock implements IBE<FrackingPumpOutletBlockEntity> {

    public static final MapCodec<FrackingPumpOutletBlock> CODEC = simpleCodec(FrackingPumpOutletBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public FrackingPumpOutletBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends KineticBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FrackingPumpOutletBlockEntity(ModBlockEntities.FRACKING_PUMP_OUTLET_BE.get(), pos, state);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        // Face the player when placed
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof FrackingPumpOutletBlockEntity outlet && outlet.isAssembled()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player.isShiftKeyDown()) return InteractionResult.PASS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof FrackingPumpOutletBlockEntity outlet)) return InteractionResult.PASS;

        if (outlet.isAssembled()) return InteractionResult.PASS;

        if (level.isClientSide) return InteractionResult.SUCCESS;

        FrackingPumpOutletBlockEntity.AssemblyResult result = outlet.tryAssemble();
        if (result.success()) {
            player.displayClientMessage(Component.literal("[Fracking Pump] Structure assembled!"), false);
        } else if (!result.reason().isEmpty()) {
            player.displayClientMessage(Component.literal("[Fracking Pump] Assembly failed: " + result.reason()), true);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.hasBlockEntity() && (!state.is(newState.getBlock()) || !newState.hasBlockEntity())) {
            withBlockEntityDo(level, pos, FrackingPumpOutletBlockEntity::disassemble);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    /**
     * Converts a horizontal direction from model-local space into world space.
     * Geometry and {@link FrackingPumpRenderer} are authored with {@link Direction#SOUTH} as the
     * identity orientation (BER rotation {@code 180 - facing.toYRot()}).
     */
    public static Direction modelLocalToWorld(Direction local, Direction facing) {
        if (local.getAxis() == Direction.Axis.Y) {
            return local;
        }
        return switch (facing) {
            case SOUTH -> local;
            case NORTH -> local.getOpposite();
            case WEST -> local.getCounterClockWise();
            case EAST -> local.getClockWise();
            default -> local;
        };
    }

    /** Inverse of {@link #modelLocalToWorld(Direction, Direction)} for capability side queries. */
    public static Direction worldToModelLocal(Direction world, Direction facing) {
        if (world.getAxis() == Direction.Axis.Y) {
            return world;
        }
        return switch (facing) {
            case SOUTH -> world;
            case NORTH -> world.getOpposite();
            case WEST -> world.getClockWise();
            case EAST -> world.getCounterClockWise();
            default -> world;
        };
    }

    /** Fluid intake on the model's local north face. */
    public static boolean isFluidInputSide(Direction worldSide, Direction facing) {
        return worldToModelLocal(worldSide, facing) == Direction.NORTH;
    }

    /** Fluid output on the model's local south face. */
    public static boolean isFluidOutputSide(Direction worldSide, Direction facing) {
        return worldToModelLocal(worldSide, facing) == Direction.SOUTH;
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getClockWise().getAxis();
    }

    @Override
    public boolean hasShaftTowards(net.minecraft.world.level.LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == getRotationAxis(state);
    }

    @Override
    public Class<FrackingPumpOutletBlockEntity> getBlockEntityClass() {
        return FrackingPumpOutletBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends FrackingPumpOutletBlockEntity> getBlockEntityType() {
        return ModBlockEntities.FRACKING_PUMP_OUTLET_BE.get();
    }
}