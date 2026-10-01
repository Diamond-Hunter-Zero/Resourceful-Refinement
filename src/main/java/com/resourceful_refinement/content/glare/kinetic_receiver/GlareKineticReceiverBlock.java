package com.resourceful_refinement.content.glare.kinetic_receiver;

import com.mojang.serialization.MapCodec;
import com.resourceful_refinement.content.glare.*;
import com.resourceful_refinement.content.gui.GlarePowerTerminalOpener;
import com.resourceful_refinement.registry.ModBlockEntities;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GlareKineticReceiverBlock extends DirectionalKineticBlock implements IBE<GlareKineticReceiverBlockEntity> {
    public static final MapCodec<GlareKineticReceiverBlock> CODEC = simpleCodec(GlareKineticReceiverBlock::new);
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 16, 16);

    public GlareKineticReceiverBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends DirectionalKineticBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return IBE.super.newBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    // getStateForPlacement is inherited from DirectionalKineticBlock: FACING points toward the player
    // (across all six directions), so the front faces the player and the shaft is emitted from the back.

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face == state.getValue(FACING).getOpposite();
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
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
        return level.getBlockEntity(pos) instanceof IGlareNode
                ? GlarePowerTerminalOpener.open(level, pos, player)
                : InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel server) {
            GlareService.onNodeRemoved(server, DimensionalNodePos.of(server, pos));
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public Class<GlareKineticReceiverBlockEntity> getBlockEntityClass() {
        return GlareKineticReceiverBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends GlareKineticReceiverBlockEntity> getBlockEntityType() {
        return ModBlockEntities.GLARE_KINETIC_RECEIVER_BE.get();
    }
}
