package com.resourceful_refinement.content.glare;

import com.mojang.serialization.MapCodec;
import com.resourceful_refinement.registry.ModBlockEntities;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GlareEmitterDishBlock extends HorizontalKineticBlock implements IBE<GlareEmitterDishBlockEntity> {
    public static final MapCodec<GlareEmitterDishBlock> CODEC = simpleCodec(GlareEmitterDishBlock::new);
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 16, 16);

    public GlareEmitterDishBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends HorizontalKineticBlock> codec() {
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

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == getRotationAxis(state);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(HORIZONTAL_FACING).getAxis();
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.getItem() instanceof RelayWrenchItem wrench) {
            wrench.interactWithNode(stack, level, pos, player);
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (Block.byItem(stack.getItem()) instanceof StainedGlassBlock glass) {
            if (level.isClientSide) {
                return ItemInteractionResult.SUCCESS;
            }
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof GlareEmitterDishBlockEntity emitter) {
                DyeColor colour = glass.getColor();
                emitter.setColour(colour);
                player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.resourceful_refinement.glare.colour_set", colour.getName()), true);
                return ItemInteractionResult.SUCCESS;
            }
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
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof GlareEmitterDishBlockEntity emitter) {
            emitter.refreshEmitterState();
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level instanceof net.minecraft.server.level.ServerLevel server) {
            GlareService.onNodeRemoved(server, GlareNodePos.of(server, pos));
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public Class<GlareEmitterDishBlockEntity> getBlockEntityClass() {
        return GlareEmitterDishBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends GlareEmitterDishBlockEntity> getBlockEntityType() {
        return ModBlockEntities.GLARE_EMITTER_DISH_BE.get();
    }
}
