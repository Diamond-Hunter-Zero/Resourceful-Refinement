package com.resourceful_refinement.content.glare.terminal;

import com.mojang.serialization.MapCodec;
import com.resourceful_refinement.content.glare.GlareNodeBlock;
import com.resourceful_refinement.content.glare.GlareNodeBlockItem;
import com.resourceful_refinement.registry.ModBlockEntities;
import com.simibubi.create.AllItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class TelemetryTerminalBlock extends GlareNodeBlock {
    public static final MapCodec<TelemetryTerminalBlock> CODEC = simpleCodec(TelemetryTerminalBlock::new);

    public TelemetryTerminalBlock(Properties properties) {
        super(properties, TelemetryTerminalBlockEntity::new);
    }

    @Override protected MapCodec<? extends GlareNodeBlock> codec() { return CODEC; }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.getItem() instanceof GlareNodeBlockItem) return super.useItemOn(stack, state, level, pos, player, hand, hit);
        if (level.getBlockEntity(pos) instanceof TelemetryTerminalBlockEntity terminal && stack.is(AllItems.WRENCH.get())) {
            if (!level.isClientSide) {
                terminal.cycleMode();
                player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                        "message.resourceful_refinement.telemetry_terminal.mode", terminal.getMode().name()), true);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof TelemetryTerminalBlockEntity terminal)) return super.useWithoutItem(state, level, pos, player, hit);
        if (player.isShiftKeyDown()) return InteractionResult.PASS;
        if (!level.isClientSide) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(terminal, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, net.minecraft.world.level.block.Block block,
            BlockPos neighbourPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, neighbourPos, movedByPiston);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof TelemetryTerminalBlockEntity terminal) terminal.onNeighborChanged();
    }

    @Override protected boolean isSignalSource(BlockState state) { return true; }
    @Override protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, net.minecraft.core.Direction direction) {
        return level.getBlockEntity(pos) instanceof TelemetryTerminalBlockEntity terminal && terminal.isPulsing() ? 15 : 0;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.GLARE_TELEMETRY_TERMINAL_BE.get()) return null;
        return (tickerLevel, pos, tickerState, blockEntity) -> ((TelemetryTerminalBlockEntity) blockEntity).tick();
    }
}
