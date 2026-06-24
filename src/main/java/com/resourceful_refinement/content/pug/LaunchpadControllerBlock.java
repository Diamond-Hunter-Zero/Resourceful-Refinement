package com.resourceful_refinement.content.pug;

import com.mojang.serialization.MapCodec;
import com.resourceful_refinement.registry.ModBlockEntities;
import com.simibubi.create.AllItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class LaunchpadControllerBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<LaunchpadControllerBlock> CODEC = simpleCodec(LaunchpadControllerBlock::new);
    public static final BooleanProperty ASSEMBLED = BooleanProperty.create("assembled");

    public LaunchpadControllerBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(ASSEMBLED, false));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ASSEMBLED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(ASSEMBLED, false);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LaunchpadControllerBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof LaunchpadControllerBlockEntity controller
                && !controller.tryAssemble() && placer instanceof Player player) {
            player.displayClientMessage(Component.translatable(
                    "message.resourceful_refinement.launchpad.incomplete"), true);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof LaunchpadControllerBlockEntity controller)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!controller.isAssembled()) {
            boolean assembled = controller.tryAssemble();
            player.displayClientMessage(Component.translatable(assembled
                    ? "message.resourceful_refinement.launchpad.assembled"
                    : "message.resourceful_refinement.launchpad.incomplete"), true);
            return InteractionResult.CONSUME;
        }
        player.openMenu(controller, pos);
        return InteractionResult.CONSUME;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(AllItems.WRENCH.get())) {
            return super.useItemOn(stack, state, level, pos, player, hand, hit);
        }
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof LaunchpadControllerBlockEntity controller) {
            LaunchpadMode mode = controller.toggleMode();
            player.displayClientMessage(Component.translatable(
                    "message.resourceful_refinement.launchpad.mode", mode.name().toLowerCase()), true);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
            BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof LaunchpadControllerBlockEntity controller) {
            controller.updateRedstoneState(level.hasNeighborSignal(pos));
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof LaunchpadControllerBlockEntity controller) {
            controller.onControllerRemoved();
            controller.dropInventory();
            controller.removeAssemblyProxies();
        }
        if (!state.is(newState.getBlock()) && level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            PugService.unregister(serverLevel, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.LAUNCHPAD_CONTROLLER_BE.get()) return null;
        return (tickerLevel, pos, tickerState, blockEntity) ->
                ((LaunchpadControllerBlockEntity) blockEntity).serverTick();
    }
}
