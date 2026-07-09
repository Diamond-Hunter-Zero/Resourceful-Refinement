package com.resourceful_refinement.content.glare;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
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
