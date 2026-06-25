package com.resourceful_refinement.content.brewers_tap;

import com.mojang.serialization.MapCodec;
import com.resourceful_refinement.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BrewersTapBlock extends HorizontalDirectionalBlock implements EntityBlock {

    public static final MapCodec<BrewersTapBlock> CODEC = simpleCodec(BrewersTapBlock::new);
    public static final BooleanProperty VALVE_OPEN = BooleanProperty.create("valve_open");

    private static final VoxelShape NORTH_AABB = Shapes.or(Block.box(5, 7, 9, 11, 13, 16), Block.box(6, 4, 5, 10, 12, 9));
    private static final VoxelShape EAST_AABB = Shapes.or(Block.box(0, 7, 5, 7, 13, 11), Block.box(7, 4, 6, 11, 12, 10));
    private static final VoxelShape SOUTH_AABB = Shapes.or(Block.box(5, 7, 0, 11, 13, 7), Block.box(6, 4, 7, 10, 12, 11));
    private static final VoxelShape WEST_AABB = Shapes.or(Block.box(9, 7, 5, 16, 13, 11), Block.box(5, 4, 6, 9, 12, 10));

    public BrewersTapBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(VALVE_OPEN, false));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
        builder.add(VALVE_OPEN);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    /** Pipe connection face (model-local south / back of the nozzle body). */
    public static Direction getPipeFace(BlockState state) {
        return state.getValue(FACING).getOpposite();
    }


    public static boolean isValveOpen(BlockState state) {
        return state.getValue(VALVE_OPEN);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> SOUTH_AABB;
            case WEST -> WEST_AABB;
            case EAST -> EAST_AABB;
            default -> NORTH_AABB;
        };
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BrewersTapBlockEntity(ModBlockEntities.BREWERS_TAP_BE.get(), pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if (be instanceof BrewersTapBlockEntity tap) {
                BrewersTapBlockEntity.serverTick(lvl, pos, st, tap);
            }
        };
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hitResult) {
        if (stack.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!(level.getBlockEntity(pos) instanceof BrewersTapBlockEntity tap)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }

        // Check if held item has a flavour tag. If not, toggle valve state
        if (!stack.is(FlavourType.ALL_FLAVOURS_ITEM_TAG))
        {
            toggleValve(state, level, pos, player, hitResult);
            return ItemInteractionResult.SUCCESS;
        }

        // Insert new itemstack, and switch out previous stack if it exists
        ItemStack stored = tap.flavourInv.getStackInSlot(0).copy();
        ItemStack held = stack.copy();
        tap.flavourInv.setStackInSlot(0, held);

        // Switch held item (clears player's hand if previous flavour item was empty)
        player.setItemInHand(hand, stored);
        player.getInventory().setChanged();

        level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.45F, 1.4F);
        tap.onFlavourItemChanged();
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        // Remove held item if shifting while empty-handed
        if (player.isShiftKeyDown() && level.getBlockEntity(pos) instanceof BrewersTapBlockEntity tap) {
            ItemStack stored = tap.flavourInv.getStackInSlot(0).copy();
            if (stored.isEmpty()) {
                return InteractionResult.PASS;
            }

            tap.flavourInv.setStackInSlot(0, ItemStack.EMPTY);
            if (!player.addItem(stored)) {
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stored);
            }
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.45F, 0.8F);
            tap.onFlavourItemChanged();
            return InteractionResult.CONSUME;
        }

        // Toggle the valve
        return toggleValve(state, level, pos, player, hit);
    }

    private InteractionResult toggleValve(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit)
    {
        boolean open = !isValveOpen(state);
        BlockState updated = state.setValue(VALVE_OPEN, open);
        level.setBlock(pos, updated, Block.UPDATE_ALL);
        level.playSound(null, pos, open ? SoundEvents.WOODEN_TRAPDOOR_OPEN : SoundEvents.WOODEN_TRAPDOOR_CLOSE,
                SoundSource.BLOCKS, 0.5F, 0.9F);

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof BrewersTapBlockEntity tap) {
            tap.onValveStateChanged(open);
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        // Drop contents
        if (!state.is(newState.getBlock()) && !level.isClientSide
                && level.getBlockEntity(pos) instanceof BrewersTapBlockEntity tap) {
            ItemStack stored = tap.flavourInv.getStackInSlot(0);
            if (!stored.isEmpty()) {
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stored.copy());
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

}
