package com.resourceful_refinement.content.glare.lux;

import com.mojang.serialization.MapCodec;
import com.resourceful_refinement.content.glare.DimensionalNodePos;
import com.resourceful_refinement.content.glare.GlareNodeBlockItem;
import com.resourceful_refinement.content.glare.GlareService;
import com.resourceful_refinement.content.glare.RelayWrenchItem;
import com.resourceful_refinement.content.gui.GlarePowerTerminalOpener;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class LuxTransceiverBlock extends DirectionalBlock implements EntityBlock, com.simibubi.create.content.equipment.wrench.IWrenchable {
    public static final MapCodec<LuxTransceiverBlock> CODEC = simpleCodec(LuxTransceiverBlock::new);
    public static final DirectionProperty FACING = DirectionalBlock.FACING;

    private static final Map<Direction, VoxelShape> SHAPES = Util.make(new EnumMap<>(Direction.class), map -> {
        map.put(Direction.NORTH, Shapes.or(
                Block.box(1, 1, 14, 15, 15, 16),
                Block.box(2, 2, 2, 14, 14, 14)));
        map.put(Direction.EAST, Shapes.or(
                Block.box(0, 1, 1, 2, 15, 15),
                Block.box(2, 2, 2, 14, 14, 14)));
        map.put(Direction.SOUTH, Shapes.or(
                Block.box(1, 1, 0, 15, 15, 2),
                Block.box(2, 2, 2, 14, 14, 14)));
        map.put(Direction.WEST, Shapes.or(
                Block.box(14, 1, 1, 16, 15, 15),
                Block.box(2, 2, 2, 14, 14, 14)));
        map.put(Direction.UP, Shapes.or(
                Block.box(1, 0, 1, 15, 2, 15),
                Block.box(2, 2, 2, 14, 14, 14)));
        map.put(Direction.DOWN, Shapes.or(
                Block.box(1, 14, 1, 15, 16, 15),
                Block.box(2, 2, 2, 14, 14, 14)));
    });


    public LuxTransceiverBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LuxTransceiverBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        // Let Create's wrench rotate the block (WrenchItem#useOn) instead of falling through to the GUI.
        if (stack.is(com.simibubi.create.AllItems.WRENCH.get())) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        if (stack.getItem() instanceof RelayWrenchItem wrench) {
            wrench.interactWithNode(stack, level, pos, player);
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (stack.getItem() instanceof GlareNodeBlockItem) {
            if (level.isClientSide) {
                return ItemInteractionResult.SUCCESS;
            }
            return GlareService.tryAddTarget(stack, level, pos, player) ? ItemInteractionResult.SUCCESS : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return level.getBlockEntity(pos) instanceof LuxTransceiverBlockEntity
                ? GlarePowerTerminalOpener.open(level, pos, player)
                : InteractionResult.PASS;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof LuxTransceiverBlockEntity transceiver) {
            transceiver.refreshSocketLink();
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof LuxTransceiverBlockEntity transceiver) {
            transceiver.refreshSocketLink();
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel server) {
            if (level.getBlockEntity(pos) instanceof LuxTransceiverBlockEntity transceiver) {
                transceiver.clearSocketLink();
            }
            GlareService.onNodeRemoved(server, DimensionalNodePos.of(server, pos));
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
