package com.resourceful_refinement.content.conveyor;

import com.mojang.serialization.MapCodec;
import com.resourceful_refinement.registry.ModBlockEntities;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
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
import org.jetbrains.annotations.Nullable;

public class ConveyorRotatorBlock extends HorizontalKineticBlock implements IBE<ConveyorRotatorBlockEntity> {
    public static final MapCodec<ConveyorRotatorBlock> CODEC = simpleCodec(ConveyorRotatorBlock::new);

    public ConveyorRotatorBlock(Properties properties) {
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
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!state.is(oldState.getBlock())) {
            withBlockEntityDo(level, pos, be -> be.setOutputDirection(state.getValue(HORIZONTAL_FACING)));
        }
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return Direction.Axis.Y;
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == Direction.Axis.Y;
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        InteractionResult result = super.onWrenched(state, context);
        if (result.consumesAction() && !context.getLevel().isClientSide) {
            BlockPos pos = context.getClickedPos();
            BlockState updatedState = context.getLevel().getBlockState(pos);
            if (updatedState.is(this)) {
                withBlockEntityDo(context.getLevel(), pos,
                        be -> be.setOutputDirection(updatedState.getValue(HORIZONTAL_FACING)));
            }
        }
        return result;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ConveyorRotatorBlockEntity(ModBlockEntities.CONVEYOR_ROTATOR_BE.get(), pos, state);
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
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hitResult) {
        if (!stack.isEmpty() || hand != InteractionHand.MAIN_HAND) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide) {
            return getBlockEntityOptional(level, pos)
                    .filter(ConveyorRotatorBlockEntity::hasRotationSession)
                    .map(be -> ItemInteractionResult.SUCCESS)
                    .orElse(ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof ConveyorRotatorBlockEntity rotator && rotator.finishRotationByPlayer()) {
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public Class<ConveyorRotatorBlockEntity> getBlockEntityClass() {
        return ConveyorRotatorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ConveyorRotatorBlockEntity> getBlockEntityType() {
        return ModBlockEntities.CONVEYOR_ROTATOR_BE.get();
    }
}
