package com.resourceful_refinement.content.gui;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class GlarePowerTerminalOpener {
    private static final Component TITLE = Component.translatable("gui.resourceful_refinement.power_terminal");

    private GlarePowerTerminalOpener() {}

    public static InteractionResult open(Level level, BlockPos pos, Player player) {
        return open(level, pos, pos, player);
    }

    public static InteractionResult open(Level level, BlockPos terminalPos, BlockPos validationPos, Player player) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new SimpleMenuProvider(
                    (id, inventory, menuPlayer) -> new PowerTerminalMenu(id, inventory, terminalPos, validationPos), TITLE), terminalPos);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }
}
