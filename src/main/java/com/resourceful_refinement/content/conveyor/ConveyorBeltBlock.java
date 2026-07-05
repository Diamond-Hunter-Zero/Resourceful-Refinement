package com.resourceful_refinement.content.conveyor;

import com.mojang.serialization.MapCodec;
import com.resourceful_refinement.registry.ModBlockEntities;
import com.resourceful_refinement.registry.ModItems;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltPart;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.InteractionResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ConveyorBeltBlock extends HorizontalKineticBlock implements IBE<ConveyorBeltBlockEntity> {
    public static final MapCodec<ConveyorBeltBlock> CODEC = simpleCodec(ConveyorBeltBlock::new);
    public static final EnumProperty<BeltPart> PART = EnumProperty.create("part", BeltPart.class);
    private static boolean destroyingChain;

    public ConveyorBeltBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(HORIZONTAL_FACING, Direction.NORTH)
                .setValue(PART, BeltPart.START));
    }

    @Override
    protected MapCodec<? extends HorizontalKineticBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean areStatesKineticallyEquivalent(BlockState oldState, BlockState newState) {
        return super.areStatesKineticallyEquivalent(oldState, newState)
                && oldState.getValue(PART) == newState.getValue(PART);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(PART);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(HORIZONTAL_FACING).getClockWise().getAxis();
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        if (face.getAxis() != getRotationAxis(state)) {
            return false;
        }
        return getBlockEntityOptional(world, pos).map(ConveyorBeltBlockEntity::hasPulley).orElse(false);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ConveyorBeltBlockEntity(ModBlockEntities.CONVEYOR_BELT_BE.get(), pos, state);
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
    public ItemStack getCloneItemStack(BlockState state, net.minecraft.world.phys.HitResult target, LevelReader level,
                                       BlockPos pos, Player player) {
        return ModItems.CONVEYOR_BELT_ITEM.get().getDefaultInstance();
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> drops = new ArrayList<>(super.getDrops(state, builder));
        BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof ConveyorBeltBlockEntity belt && belt.hasPulley()) {
            drops.addAll(AllBlocks.SHAFT.getDefaultState().getDrops(builder));
        }
        return drops;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!destroyingChain && !state.is(newState.getBlock()) && !isMoving) {
            destroyConnectedChain(level, pos, state);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        return InteractionResult.PASS;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hitResult) {
        if (player.isShiftKeyDown() || !player.mayBuild()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (stack.is(ModItems.CONVEYOR_BELT_ITEM.get())) {
            if (state.getValue(PART) != BeltPart.START && state.getValue(PART) != BeltPart.END) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            if (level.isClientSide) {
                return ItemInteractionResult.SUCCESS;
            }
            return extendBelt(level, pos, state) ? ItemInteractionResult.SUCCESS : ItemInteractionResult.FAIL;
        }

        if (AllBlocks.SHAFT.isIn(stack)) {
            if (state.getValue(PART) != BeltPart.MIDDLE) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            if (level.isClientSide) {
                return ItemInteractionResult.SUCCESS;
            }
            if (!player.isCreative()) {
                stack.shrink(1);
            }
            KineticBlockEntity.switchToBlockState(level, pos, state.setValue(PART, BeltPart.PULLEY));
            initBelt(level, pos);
            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public Class<ConveyorBeltBlockEntity> getBlockEntityClass() {
        return ConveyorBeltBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ConveyorBeltBlockEntity> getBlockEntityType() {
        return ModBlockEntities.CONVEYOR_BELT_BE.get();
    }

    public static boolean isConveyor(BlockState state) {
        return state.getBlock() instanceof ConveyorBeltBlock && state.hasProperty(PART);
    }

    public static void initBelt(Level level, BlockPos pos) {
        if (level == null || !level.isLoaded(pos) || !isConveyor(level.getBlockState(pos))) {
            return;
        }

        BlockPos controllerPos = findControllerPosition(level, pos);
        List<BlockPos> chain = getBeltChain(level, controllerPos);
        if (chain.isEmpty()) {
            return;
        }

        for (int i = 0; i < chain.size(); i++) {
            BlockEntity blockEntity = level.getBlockEntity(chain.get(i));
            if (!(blockEntity instanceof ConveyorBeltBlockEntity belt)) {
                continue;
            }
            belt.setController(controllerPos);
            belt.beltLength = chain.size();
            belt.index = i;
            belt.attachKinetics();
            belt.setChanged();
            belt.sendData();
        }
    }

    public static List<BlockPos> getBeltChain(Level level, BlockPos controllerPos) {
        List<BlockPos> chain = new ArrayList<>();
        if (level == null || !level.isLoaded(controllerPos)) {
            return chain;
        }

        BlockPos cursor = controllerPos;
        int limit = ConveyorBeltItem.MAX_BELT_LENGTH;
        while (level.isLoaded(cursor) && limit-- > 0) {
            BlockState state = level.getBlockState(cursor);
            if (!isConveyor(state)) {
                break;
            }
            chain.add(cursor);
            BlockPos next = nextSegmentPosition(state, cursor, true);
            if (next == null) {
                break;
            }
            cursor = next;
        }
        return chain;
    }

    public static @Nullable BlockPos nextSegmentPosition(BlockState state, BlockPos pos, boolean forward) {
        BeltPart part = state.getValue(PART);
        if ((forward && part == BeltPart.END) || (!forward && part == BeltPart.START)) {
            return null;
        }
        Direction facing = state.getValue(HORIZONTAL_FACING);
        return pos.relative(forward ? facing : facing.getOpposite());
    }

    private static BlockPos findControllerPosition(Level level, BlockPos pos) {
        BlockPos cursor = pos;
        int limit = ConveyorBeltItem.MAX_BELT_LENGTH;
        while (limit-- > 0) {
            BlockState state = level.getBlockState(cursor);
            if (!isConveyor(state)) {
                break;
            }
            BlockPos previous = nextSegmentPosition(state, cursor, false);
            if (previous == null || !isConveyor(level.getBlockState(previous))) {
                return cursor;
            }
            cursor = previous;
        }
        return pos;
    }

    private static boolean extendBelt(Level level, BlockPos pos, BlockState state) {
        BlockPos controllerPos = findControllerPosition(level, pos);
        List<BlockPos> chain = getBeltChain(level, controllerPos);
        if (chain.size() >= ConveyorBeltItem.MAX_BELT_LENGTH) {
            return false;
        }

        BeltPart part = state.getValue(PART);
        Direction facing = state.getValue(HORIZONTAL_FACING);
        boolean extendForward = part == BeltPart.END;
        BlockPos newPos = pos.relative(extendForward ? facing : facing.getOpposite());
        if (!ConveyorBeltItem.canReplaceWithBelt(level, newPos, getRotationAxisForState(state))) {
            return false;
        }

        BeltPart currentPart = part == BeltPart.START || part == BeltPart.END ? BeltPart.MIDDLE : part;
        BeltPart newPart = extendForward ? BeltPart.END : BeltPart.START;
        BlockState newState = state.setValue(PART, newPart);

        KineticBlockEntity.switchToBlockState(level, pos, state.setValue(PART, currentPart));
        KineticBlockEntity.switchToBlockState(level, newPos, newState);
        initBelt(level, extendForward ? controllerPos : newPos);
        level.playSound(null, newPos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 0.5F, 1F);
        return true;
    }

    private static Direction.Axis getRotationAxisForState(BlockState state) {
        return state.getValue(HORIZONTAL_FACING).getClockWise().getAxis();
    }

    private static void destroyConnectedChain(Level level, BlockPos pos, BlockState removedState) {
        if (level.isClientSide) {
            return;
        }
        if (!isConveyor(removedState)) {
            return;
        }

        List<BlockPos> positions = collectChain(level, pos, removedState);
        if (positions.isEmpty()) {
            return;
        }

        destroyingChain = true;
        try {
            for (BlockPos beltPos : positions) {
                if (beltPos.equals(pos)) {
                    continue;
                }
                BlockState state = level.getBlockState(beltPos);
                if (!isConveyor(state)) {
                    continue;
                }
                if (state.getValue(PART) != BeltPart.MIDDLE) {
                    Block.popResource(level, beltPos, AllBlocks.SHAFT.asStack());
                }
                level.levelEvent(2001, beltPos, Block.getId(state));
                KineticBlockEntity.switchToBlockState(level, beltPos, Blocks.AIR.defaultBlockState());
            }
        } finally {
            destroyingChain = false;
        }
    }

    private static List<BlockPos> collectChain(Level level, BlockPos removedPos, BlockState removedState) {
        List<BlockPos> positions = new ArrayList<>();
        List<BlockPos> before = collectChainDirection(level, nextSegmentPosition(removedState, removedPos, false), false);
        for (int i = before.size() - 1; i >= 0; i--) {
            positions.add(before.get(i));
        }
        positions.add(removedPos);
        positions.addAll(collectChainDirection(level, nextSegmentPosition(removedState, removedPos, true), true));
        return positions;
    }

    private static List<BlockPos> collectChainDirection(Level level, @Nullable BlockPos start, boolean forward) {
        List<BlockPos> positions = new ArrayList<>();
        if (start == null) {
            return positions;
        }
        BlockPos cursor = start;
        int limit = ConveyorBeltItem.MAX_BELT_LENGTH;
        while (limit-- > 0) {
            BlockState state = level.getBlockState(cursor);
            if (!isConveyor(state)) {
                break;
            }
            if (!positions.contains(cursor)) {
                positions.add(cursor);
            }
            BlockPos next = nextSegmentPosition(state, cursor, forward);
            if (next == null) {
                break;
            }
            cursor = next;
        }
        return positions;
    }
}
