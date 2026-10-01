package com.resourceful_refinement.content.glare.chromatic_transceiver;

import com.mojang.serialization.MapCodec;
import com.resourceful_refinement.content.glare.GlareNodeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

public class GlareChromaticTransceiverBlock extends GlareNodeBlock {
    public static final MapCodec<GlareChromaticTransceiverBlock> CODEC = simpleCodec(GlareChromaticTransceiverBlock::new);

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
