package com.resourceful_refinement.content.research_terminal;

import com.mojang.serialization.MapCodec;
import com.resourceful_refinement.network.OpenResearchTerminalPayload;
import com.resourceful_refinement.network.ResearchTerminalStatePayload;
import com.resourceful_refinement.network.ResearchTreeSyncPayload;
import com.resourceful_refinement.registry.ModBlockEntities;
import com.simibubi.create.content.logistics.depot.DepotBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public class ResearchTerminalBlock extends DepotBlock {
    public static final MapCodec<ResearchTerminalBlock> CODEC = simpleCodec(ResearchTerminalBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public ResearchTerminalBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends DepotBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context)
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ResearchTerminalBlockEntity(ModBlockEntities.RESEARCH_TERMINAL_BE.get(), pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.RESEARCH_TERMINAL_BE.get()) {
            return null;
        }
        return (tickerLevel, pos, tickerState, blockEntity) -> ((ResearchTerminalBlockEntity) blockEntity).tick();
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable net.minecraft.world.entity.LivingEntity placer,
            ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof ResearchTerminalBlockEntity terminal) {
            terminal.assignOwner(placer instanceof Player player ? player : null);
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (hit.getDirection() == Direction.UP) {
            return super.useItemOn(stack, state, level, pos, player, hand, hit);
        }
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof ResearchTerminalBlockEntity terminal) {
            PacketDistributor.sendToPlayer(serverPlayer,
                    new OpenResearchTerminalPayload(ResearchTerminalStatePayload.capture(terminal)));
            PacketDistributor.sendToPlayer(serverPlayer, ResearchTreeSyncPayload.capture(serverPlayer));
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
}
