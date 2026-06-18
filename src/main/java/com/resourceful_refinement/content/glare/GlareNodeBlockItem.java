package com.resourceful_refinement.content.glare;

import com.resourceful_refinement.registry.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

public class GlareNodeBlockItem extends BlockItem {
    public GlareNodeBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (player != null && player.isShiftKeyDown()) {
            if (!context.getLevel().isClientSide) {
                GlareService.clearTargets(stack, player);
            }
            return InteractionResult.SUCCESS;
        }
        BlockEntity be = context.getLevel().getBlockEntity(context.getClickedPos());
        if (be instanceof IGlareNode) {
            if (!context.getLevel().isClientSide && player != null) {
                GlareService.tryAddTarget(stack, context.getLevel(), context.getClickedPos(), player);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        ItemStack stack = context.getItemInHand();
        GlareTargetsData targets = stack.getOrDefault(ModDataComponents.GLARE_TARGETS.get(), GlareTargetsData.EMPTY);
        InteractionResult result = super.place(context);
        if (result.consumesAction() && !context.getLevel().isClientSide && context.getLevel() instanceof ServerLevel server && !targets.isEmpty()) {
            BlockPos placedPos = findPlacedNodePos(context, server);
            if (placedPos != null && GlareService.ensureLiveNodeRegistered(server, placedPos).orElse(null) instanceof IGlareNode node) {
                GlareNodePos from = node.getGlareNodePos();
                for (GlareNodePos target : targets.targets()) {
                    GlareSavedData.LinkResult linkResult = GlareService.tryLink(server, from, target);
                    if (context.getPlayer() != null && linkResult != GlareSavedData.LinkResult.CREATED && linkResult != GlareSavedData.LinkResult.ALREADY_LINKED) {
                        context.getPlayer().displayClientMessage(Component.translatable("message.resourceful_refinement.glare.link_failed." + linkResult.name().toLowerCase(java.util.Locale.ROOT)), true);
                    }
                }
                stack.remove(ModDataComponents.GLARE_TARGETS.get());
            }
        }
        return result;
    }

    private static BlockPos findPlacedNodePos(BlockPlaceContext context, ServerLevel server) {
        BlockPos clicked = context.getClickedPos();
        if (server.getBlockEntity(clicked) instanceof IGlareNode) {
            return clicked;
        }
        BlockPos adjacent = clicked.relative(context.getClickedFace());
        if (server.getBlockEntity(adjacent) instanceof IGlareNode) {
            return adjacent;
        }
        return null;
    }
}
