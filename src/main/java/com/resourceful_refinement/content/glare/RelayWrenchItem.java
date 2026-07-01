package com.resourceful_refinement.content.glare;

import com.resourceful_refinement.registry.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

/** Two-click editor for GLARE links. The selected endpoint is persisted and synchronized on the held stack. */
public class RelayWrenchItem extends Item {
    public RelayWrenchItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.FAIL;
        return interactWithNode(context.getItemInHand(), context.getLevel(), context.getClickedPos(), player);
    }

    public InteractionResult interactWithNode(ItemStack stack, Level level, net.minecraft.core.BlockPos pos, Player player) {
        if (!(level.getBlockEntity(pos) instanceof IGlareNode node)) return InteractionResult.PASS;
        if (!node.allowsManualGlareLinks()) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;
        ServerLevel server = (ServerLevel) level;
        GlareService.ensureLiveNodeRegistered(server, pos);
        DimensionalNodePos clicked = node.getGlareNodePos();
        DimensionalNodePos selected = stack.get(ModDataComponents.RELAY_WRENCH_TARGET.get());

        if (player.isShiftKeyDown()) {
            if (clicked.equals(selected)) {
                clearSelection(stack, player);
            } else {
                int removed = GlareService.removeAllLinks(server, clicked);
                player.displayClientMessage(Component.translatable(
                        "message.resourceful_refinement.relay_wrench.links_removed", removed), true);
            }
            return InteractionResult.SUCCESS;
        }

        if (selected == null) {
            if (!GlareService.canAcceptLink(server, clicked)) {
                showAtLimit(player);
                return InteractionResult.SUCCESS;
            }
            stack.set(ModDataComponents.RELAY_WRENCH_TARGET.get(), clicked);
            player.displayClientMessage(Component.translatable("message.resourceful_refinement.relay_wrench.selected",
                    clicked.toShortString()), true);
            return InteractionResult.SUCCESS;
        }

        if (selected.equals(clicked)) {
            clearSelection(stack, player);
            return InteractionResult.SUCCESS;
        }
        if (!GlareService.getNode(server, selected).isPresent()) {
            clearSelection(stack, player);
            player.displayClientMessage(Component.translatable("message.resourceful_refinement.relay_wrench.target_missing"), true);
            return InteractionResult.SUCCESS;
        }
        if (!GlareService.canAcceptLink(server, selected)) {
            stack.remove(ModDataComponents.RELAY_WRENCH_TARGET.get());
            showAtLimit(player);
            return InteractionResult.SUCCESS;
        }
        if (!GlareService.canAcceptLink(server, clicked)) {
            showAtLimit(player);
            return InteractionResult.SUCCESS;
        }

        GlareSavedData.LinkResult result = GlareService.tryLink(server, selected, clicked);
        stack.remove(ModDataComponents.RELAY_WRENCH_TARGET.get());
        if (result == GlareSavedData.LinkResult.CREATED) {
            player.displayClientMessage(Component.translatable("message.resourceful_refinement.relay_wrench.connected"), true);
        } else if (result == GlareSavedData.LinkResult.ALREADY_LINKED) {
            player.displayClientMessage(Component.translatable("message.resourceful_refinement.relay_wrench.already_linked"), true);
        } else {
            player.displayClientMessage(Component.translatable(
                    "message.resourceful_refinement.glare.link_failed." + result.name().toLowerCase(java.util.Locale.ROOT)), true);
        }
        return InteractionResult.SUCCESS;
    }

    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown() && stack.has(ModDataComponents.RELAY_WRENCH_TARGET.get())) {
            if (!level.isClientSide) clearSelection(stack, player);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        return InteractionResultHolder.pass(stack);
    }

    private static void clearSelection(ItemStack stack, Player player) {
        stack.remove(ModDataComponents.RELAY_WRENCH_TARGET.get());
        player.displayClientMessage(Component.translatable("message.resourceful_refinement.relay_wrench.cancelled"), true);
    }

    private static void showAtLimit(Player player) {
        player.displayClientMessage(Component.translatable("message.resourceful_refinement.relay_wrench.at_limit"), true);
    }

    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        DimensionalNodePos selected = stack.get(ModDataComponents.RELAY_WRENCH_TARGET.get());
        if (selected != null) tooltip.add(Component.translatable("tooltip.resourceful_refinement.relay_wrench.selected",
                selected.toShortString()));
    }
}
