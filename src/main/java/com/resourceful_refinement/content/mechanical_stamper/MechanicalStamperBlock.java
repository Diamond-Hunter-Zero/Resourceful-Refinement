package com.resourceful_refinement.content.mechanical_stamper;

import com.mojang.serialization.MapCodec;
import com.resourceful_refinement.registry.ModBlockEntities;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class MechanicalStamperBlock extends HorizontalKineticBlock implements IBE<MechanicalStamperBlockEntity> {
    public static final MapCodec<MechanicalStamperBlock> CODEC = simpleCodec(MechanicalStamperBlock::new);

    public MechanicalStamperBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalKineticBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        Direction facing = state.getValue(HORIZONTAL_FACING);
        return facing.getClockWise().getAxis();
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == getRotationAxis(state);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MechanicalStamperBlockEntity(ModBlockEntities.MECHANICAL_STAMPER_BE.get(), pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && !level.isClientSide
                && level.getBlockEntity(pos) instanceof MechanicalStamperBlockEntity stamper) {
            stamper.dropItemContents();
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hitResult) {
        if (hand != InteractionHand.MAIN_HAND) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!(level.getBlockEntity(pos) instanceof MechanicalStamperBlockEntity stamper)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (stamper.state != MechanicalStamperBlockEntity.RunningState.IDLE) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        Direction side = hitResult.getDirection();
        Direction front = state.getValue(HORIZONTAL_FACING);
        Direction back = front.getOpposite();
        if (side == back) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        ItemStackHandler handler = side == front ? stamper.stampInv : stamper.mediumInv;
        if (stack.isEmpty()) {
            if (!level.isClientSide) {
                ItemStack extracted = handler.extractItem(0, handler.getSlotLimit(0), false);
                if (!extracted.isEmpty()) {
                    Block.popResource(level, pos.relative(side), extracted);
                    stamper.sendData();
                    return ItemInteractionResult.SUCCESS;
                }
            }
            return level.isClientSide ? ItemInteractionResult.SUCCESS : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        ItemStack offered = stack.copy();
        if (side == front) {
            offered.setCount(1);
        }

        if (handler.insertItem(0, offered, true).getCount() == offered.getCount()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!level.isClientSide) {
            ItemStack remainder = handler.insertItem(0, offered, false);
            int inserted = offered.getCount() - remainder.getCount();
            if (inserted > 0 && !player.isCreative()) {
                stack.shrink(inserted);
            }
            stamper.sendData();
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public Class<MechanicalStamperBlockEntity> getBlockEntityClass() {
        return MechanicalStamperBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends MechanicalStamperBlockEntity> getBlockEntityType() {
        return ModBlockEntities.MECHANICAL_STAMPER_BE.get();
    }
}
