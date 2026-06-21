package com.resourceful_refinement.content.glare.remote;

import com.mojang.serialization.MapCodec;
import com.resourceful_refinement.content.glare.GlareNodeBlockItem;
import com.resourceful_refinement.content.glare.GlareNodePos;
import com.resourceful_refinement.content.glare.GlareService;
import com.resourceful_refinement.registry.ModBlockEntities;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.logistics.depot.DepotBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class RemoteEntanglerDepotBlock extends DepotBlock {
    public static final MapCodec<RemoteEntanglerDepotBlock> CODEC = simpleCodec(RemoteEntanglerDepotBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty RECEIVING = BooleanProperty.create("receiving");

    public RemoteEntanglerDepotBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(RECEIVING, false));
    }

    @Override protected MapCodec<? extends DepotBlock> codec() { return CODEC; }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, RECEIVING);
    }

    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(RECEIVING, false);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.getItem() instanceof GlareNodeBlockItem) {
            if (level.isClientSide) return ItemInteractionResult.SUCCESS;
            return GlareService.tryAddTarget(stack, level, pos, player)
                    ? ItemInteractionResult.SUCCESS : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (stack.is(AllItems.WRENCH.get())) {
            if (!level.isClientSide) {
                boolean receiving = !state.getValue(RECEIVING);
                level.setBlock(pos, state.setValue(RECEIVING, receiving), Block.UPDATE_ALL);
                if (level.getBlockEntity(pos) instanceof RemoteEntanglerDepotBlockEntity depot) depot.onModeChanged();
                player.displayClientMessage(Component.translatable(
                        receiving ? "message.resourceful_refinement.remote_depot.receive" : "message.resourceful_refinement.remote_depot.send"), true);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RemoteEntanglerDepotBlockEntity(ModBlockEntities.REMOTE_ENTANGLER_DEPOT_BE.get(), pos, state);
    }

    @Override public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel server) {
            GlareService.onNodeRemoved(server, GlareNodePos.of(server, pos));
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Nullable
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.REMOTE_ENTANGLER_DEPOT_BE.get()) return null;
        return (tickerLevel, pos, tickerState, blockEntity) -> ((RemoteEntanglerDepotBlockEntity) blockEntity).tick();
    }
}
