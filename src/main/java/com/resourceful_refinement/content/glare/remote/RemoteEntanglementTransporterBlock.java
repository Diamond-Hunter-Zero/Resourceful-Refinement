package com.resourceful_refinement.content.glare.remote;

import com.mojang.serialization.MapCodec;
import com.resourceful_refinement.content.glare.GlareNodeBlock;
import com.resourceful_refinement.content.glare.GlareNodeBlockItem;
import com.resourceful_refinement.content.glare.DimensionalNodePos;
import com.resourceful_refinement.content.glare.GlareService;
import com.resourceful_refinement.registry.ModBlockEntities;
import com.simibubi.create.AllItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class RemoteEntanglementTransporterBlock extends GlareNodeBlock {
    public static final MapCodec<RemoteEntanglementTransporterBlock> CODEC = simpleCodec(RemoteEntanglementTransporterBlock::new);
    public static final EnumProperty<TransporterMode> MODE = EnumProperty.create("mode", TransporterMode.class);
    /** True while all three proxies are owned by this controller; swaps the standalone base model for the full assembly. */
    public static final BooleanProperty ASSEMBLED = BooleanProperty.create("assembled");
    /** Block event fired on both endpoints after a successful teleport; clients answer with a lightning strike. */
    public static final int EVENT_TELEPORTED = 1;

    public RemoteEntanglementTransporterBlock(Properties properties) {
        super(properties, RemoteEntanglementTransporterBlockEntity::new);
        registerDefaultState(defaultBlockState().setValue(MODE, TransporterMode.AUTO).setValue(ASSEMBLED, false));
    }

    @Override protected MapCodec<? extends GlareNodeBlock> codec() { return CODEC; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(MODE, ASSEMBLED);
    }

    @Override public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof RemoteEntanglementTransporterBlockEntity transporter) {
            boolean assembled = transporter.tryAssemble();
            if (!assembled && placer instanceof Player player) {
                player.displayClientMessage(Component.translatable("message.resourceful_refinement.remote_transporter.incomplete"), true);
            }
        }
    }

    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.getItem() instanceof GlareNodeBlockItem) return super.useItemOn(stack, state, level, pos, player, hand, hit);
        if (stack.is(AllItems.WRENCH.get())) {
            if (!level.isClientSide) cycleMode(level, pos, state, player);
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    /** Advances the transporter's mode; shared by wrenching the controller and any of its proxies. */
    public static void cycleMode(Level level, BlockPos pos, BlockState state, Player player) {
        TransporterMode next = state.getValue(MODE).next();
        level.setBlock(pos, state.setValue(MODE, next), Block.UPDATE_ALL);
        if (level.getBlockEntity(pos) instanceof RemoteEntanglementTransporterBlockEntity transporter) transporter.onModeChanged();
        player.displayClientMessage(Component.translatable("message.resourceful_refinement.remote_transporter.mode", next.getSerializedName()), true);
    }

    @Override public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(ASSEMBLED, false);
    }

    @Override protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        if (id != EVENT_TELEPORTED) return super.triggerEvent(state, level, pos, id, param);
        // Returning true on the server is what forwards the event to tracking clients.
        if (level.isClientSide) RemoteEntanglementTransporterClientFx.playTeleportStrike(level, pos, state.getValue(FACING));
        return true;
    }

    @Override public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof RemoteEntanglementTransporterBlockEntity controller) {
            controller.removeAssemblyProxies();
        }
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel server) {
            GlareService.onNodeRemoved(server, DimensionalNodePos.of(server, pos));
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Nullable
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.REMOTE_ENTANGLEMENT_TRANSPORTER_BE.get()) return null;
        return (tickerLevel, pos, tickerState, blockEntity) -> ((RemoteEntanglementTransporterBlockEntity) blockEntity).tick();
    }
}
