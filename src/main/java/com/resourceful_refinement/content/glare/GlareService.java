package com.resourceful_refinement.content.glare;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class GlareService {
    private GlareService() {}

    public static void onNodeLoaded(ServerLevel level, IGlareNode node) {
        GlareSavedData.get(level).registerNode(level, node);
    }

    public static void onNodeRemoved(ServerLevel level, DimensionalNodePos pos) {
        GlareSavedData.get(level).unregisterLoadedNode(level, pos);
    }

    public static void updateNodeState(ServerLevel level, IGlareNode node) {
        GlareSavedData.get(level).updateNodeState(level, node);
    }

    public static Optional<GlareSavedData.NodeRecord> getNode(Level level, DimensionalNodePos pos) {
        if (!(level instanceof ServerLevel server)) {
            return Optional.empty();
        }
        return GlareSavedData.get(server).getNode(pos);
    }

    public static Optional<GlareSavedData.NetworkRecord> getNetwork(Level level, UUID networkId) {
        if (!(level instanceof ServerLevel server)) {
            return Optional.empty();
        }
        return GlareSavedData.get(server).getNetwork(networkId);
    }

    public static List<GlareLink> getLinks(Level level, DimensionalNodePos pos) {
        if (!(level instanceof ServerLevel server)) {
            return List.of();
        }
        return GlareSavedData.get(server).getCountedLinksFor(pos);
    }

    public static List<GlareLink> getAllLinks(Level level, DimensionalNodePos pos) {
        if (!(level instanceof ServerLevel server)) {
            return List.of();
        }
        return GlareSavedData.get(server).getLinksFor(pos);
    }

    public static boolean canAcceptLink(Level level, DimensionalNodePos pos) {
        return level instanceof ServerLevel server && GlareSavedData.get(server).canAcceptLink(pos);
    }

    public static int removeAllLinks(ServerLevel level, DimensionalNodePos pos) {
        return GlareSavedData.get(level).removeAllLinks(level, pos);
    }

    public static List<GlareSavedData.LinkRenderRecord> getRenderableLinks(ServerPlayer player) {
        return GlareSavedData.get((ServerLevel) player.level()).getRenderableLinksFor(player);
    }

    public static GlareSavedData.LinkResult tryLink(ServerLevel level, DimensionalNodePos from, DimensionalNodePos to) {
        return GlareSavedData.get(level).tryAddLink(level, from, to);
    }

    public static GlareSavedData.LinkResult trySocketLink(ServerLevel level, DimensionalNodePos from, DimensionalNodePos to) {
        return GlareSavedData.get(level).tryAddSocketLink(level, from, to);
    }

    public static boolean removeLink(ServerLevel level, DimensionalNodePos from, DimensionalNodePos to) {
        return GlareSavedData.get(level).removeLink(from, to);
    }

    public static boolean tryResetNetwork(ServerLevel level, UUID networkId) {
        return GlareSavedData.get(level).tryResetNetwork(level, networkId);
    }

    public static void reconcileLoadedChunk(ServerLevel level, BlockPos chunkOrigin) {
        GlareSavedData.get(level).reconcileLoadedChunk(level, chunkOrigin);
    }

    public static void validateLoadedLinks(ServerLevel level) {
        GlareSavedData.get(level).validateLoadedLinks(level);
    }

    public static int validateLoadedLinks(ServerLevel level, int maxChecks) {
        return GlareSavedData.get(level).validateLoadedLinks(level, maxChecks);
    }

    public static GlareSavedData.Diagnostics diagnostics(ServerLevel level) {
        return GlareSavedData.get(level).diagnostics(level);
    }

    public static void forceRebuild(ServerLevel level) {
        GlareSavedData.get(level).forceRebuild(level);
    }

    public static Optional<IGlareNode> ensureLiveNodeRegistered(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel server)) {
            return Optional.empty();
        }
        if (!server.isLoaded(pos)) return Optional.empty();
        BlockEntity be = server.getBlockEntity(pos);
        if (!(be instanceof IGlareNode node)) {
            return Optional.empty();
        }
        GlareSavedData.get(server).registerNode(server, node);
        return Optional.of(node);
    }

    public static void markChunkUnloaded(ServerLevel level, BlockPos chunkOrigin) {
        GlareSavedData.get(level).markChunkUnloaded(level, chunkOrigin);
    }

    public static boolean tryAddTarget(ItemStack stack, Level level, BlockPos pos, Player player) {
        if (level.isClientSide) {
            return true;
        }
        Optional<IGlareNode> liveNode = ensureLiveNodeRegistered(level, pos);
        if (liveNode.isEmpty()) {
            return false;
        }
        GlareTargetsData targets = stack.getOrDefault(com.resourceful_refinement.registry.ModDataComponents.GLARE_TARGETS.get(), GlareTargetsData.EMPTY);
        DimensionalNodePos target = liveNode.get().getGlareNodePos();
        stack.set(com.resourceful_refinement.registry.ModDataComponents.GLARE_TARGETS.get(), targets.withAdded(target));
        player.displayClientMessage(Component.translatable("message.resourceful_refinement.glare.target_added", target.toShortString()), true);
        return true;
    }

    public static void clearTargets(ItemStack stack, Player player) {
        stack.remove(com.resourceful_refinement.registry.ModDataComponents.GLARE_TARGETS.get());
        player.displayClientMessage(Component.translatable("message.resourceful_refinement.glare.targets_cleared"), true);
    }
}
