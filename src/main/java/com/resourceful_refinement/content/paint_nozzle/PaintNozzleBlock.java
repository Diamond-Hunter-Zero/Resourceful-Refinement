package com.resourceful_refinement.content.paint_nozzle;

import com.mojang.serialization.MapCodec;
import com.resourceful_refinement.registry.ModBlockEntities;
import com.simibubi.create.AllItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class PaintNozzleBlock extends DirectionalBlock implements EntityBlock {

    public static final MapCodec<PaintNozzleBlock> CODEC = simpleCodec(PaintNozzleBlock::new);
    public static final BooleanProperty VALVE_OPEN = BooleanProperty.create("valve_open");

    private static final VoxelShape NORTH_AABB = Block.box(3, 3, 3, 13, 13, 16);
    private static final VoxelShape EAST_AABB = Block.box(0, 3, 3, 13, 13, 13);
    private static final VoxelShape UP_AABB = Block.box(3, 0, 3, 13, 13, 13);
    private static final VoxelShape SOUTH_AABB = Block.box(3, 3, 0, 13, 13, 13);
    private static final VoxelShape WEST_AABB = Block.box(3, 3, 3, 16, 13, 13);
    private static final VoxelShape DOWN_AABB = Block.box(3, 3, 3, 13, 16, 13);

    public PaintNozzleBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(VALVE_OPEN, false));
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
        builder.add(VALVE_OPEN);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    /** Pipe connection face (model-local south / back of the nozzle body). */
    public static Direction getPipeFace(BlockState state) {
        return state.getValue(FACING).getOpposite();
    }

    /** Direction gel-blobs are fired from the nozzle. */
    public static Direction getSprayDirection(BlockState state) {
        return state.getValue(FACING);
    }

    public static boolean isValveOpen(BlockState state) {
        return state.getValue(VALVE_OPEN);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case DOWN -> DOWN_AABB;
            case UP -> UP_AABB;
            case NORTH -> NORTH_AABB;
            case SOUTH -> SOUTH_AABB;
            case WEST -> WEST_AABB;
            case EAST -> EAST_AABB;
        };
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PaintNozzleBlockEntity(ModBlockEntities.PAINT_NOZZLE_BE.get(), pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if (be instanceof PaintNozzleBlockEntity nozzle) {
                PaintNozzleBlockEntity.serverTick(lvl, pos, st, nozzle);
            }
        };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        boolean open = !isValveOpen(state);
        BlockState updated = state.setValue(VALVE_OPEN, open);
        level.setBlock(pos, updated, Block.UPDATE_ALL);
        level.playSound(null, pos, open ? SoundEvents.IRON_TRAPDOOR_OPEN : SoundEvents.IRON_TRAPDOOR_CLOSE,
                SoundSource.BLOCKS, 0.6F, 1.1F);

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof PaintNozzleBlockEntity nozzle) {
            nozzle.onValveStateChanged(open);
        }

        return InteractionResult.CONSUME;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(AllItems.WRENCH.get())) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof PaintNozzleBlockEntity nozzle)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!level.isClientSide) {
            nozzle.cycleFlowSpeed();
            level.playSound(null, pos, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 0.35F, 1.4F + level.random.nextFloat() * 0.15F);
        }

        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof PaintNozzleBlockEntity nozzle) {
                nozzle.invalidateCapabilities();
            }
        }
    }
}
