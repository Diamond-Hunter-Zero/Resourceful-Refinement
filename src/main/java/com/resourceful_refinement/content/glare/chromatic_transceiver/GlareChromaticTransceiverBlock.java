package com.resourceful_refinement.content.glare.chromatic_transceiver;

import com.mojang.serialization.MapCodec;
import com.resourceful_refinement.content.glare.GlareNodeBlock;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;
import java.util.Map;

public class GlareChromaticTransceiverBlock extends GlareNodeBlock implements com.simibubi.create.content.equipment.wrench.IWrenchable {
    public static final MapCodec<GlareChromaticTransceiverBlock> CODEC = simpleCodec(GlareChromaticTransceiverBlock::new);

    private static final Map<Direction, VoxelShape> SHAPES = Util.make(new EnumMap<>(Direction.class), map -> {
        map.put(Direction.NORTH, Shapes.or(
                Block.box(0, 0, 0, 16, 16, 4),
                Block.box(1, 1, 4, 15, 15, 15)));
        map.put(Direction.EAST, Shapes.or(
                Block.box(12, 0, 0, 16, 16, 16),
                Block.box(1, 1, 1, 12, 15, 15)));
        map.put(Direction.SOUTH, Shapes.or(
                Block.box(0, 0, 12, 16, 16, 16),
                Block.box(1, 1, 1, 15, 15, 12)));
        map.put(Direction.WEST, Shapes.or(
                Block.box(0, 0, 0, 4, 16, 16),
                Block.box(4, 1, 1, 15, 15, 15)));
        map.put(Direction.UP, Shapes.or(
                Block.box(0, 12, 0, 16, 16, 16),
                Block.box(1, 1, 1, 15, 12, 15)));
        map.put(Direction.DOWN, Shapes.or(
                Block.box(0, 0, 0, 16, 4, 16),
                Block.box(1, 4, 1, 15, 15, 15)));
    });

    public GlareChromaticTransceiverBlock(Properties properties) {
        super(properties, GlareChromaticTransceiverBlockEntity::new);
    }

    @Override
    protected MapCodec<? extends GlareNodeBlock> codec() {
        return CODEC;
    }

    // Fully directional (pillar) placement across all six faces, matching the Kinetic Receiver:
    // FACING points toward the player, so the front faces the player.
    @Override
    protected DirectionProperty getFacingProperty() {
        return BlockStateProperties.FACING;
    }

    @Override
    protected Direction getPlacementFacing(BlockPlaceContext context) {
        return context.getNearestLookingDirection().getOpposite();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(BlockStateProperties.FACING));
    }

    @Override
    protected net.minecraft.world.ItemInteractionResult useItemOn(net.minecraft.world.item.ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
        // Let Create's wrench rotate the block (WrenchItem#useOn) instead of falling through to the GUI.
        if (stack.is(com.simibubi.create.AllItems.WRENCH.get())) {
            return net.minecraft.world.ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof GlareChromaticTransceiverBlockEntity transceiver) {
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(transceiver, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useWithoutItem(state, level, pos, player, hit);
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, net.minecraft.core.Direction direction) {
        return level.getBlockEntity(pos) instanceof GlareChromaticTransceiverBlockEntity transceiver && transceiver.isOutputPowered() ? 15 : 0;
    }
}
