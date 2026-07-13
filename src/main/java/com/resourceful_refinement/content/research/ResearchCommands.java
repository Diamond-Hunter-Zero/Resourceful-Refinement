package com.resourceful_refinement.content.research;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.api.research.ResearchApi;
import com.resourceful_refinement.api.research.UnlockResult;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@EventBusSubscriber(modid = ResourcefulRefinementMain.MOD_ID)
public final class ResearchCommands {
    public static final String COMMAND_ROOT = "researchSystem";

    private ResearchCommands() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(COMMAND_ROOT)
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("grant")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(nodeArgument()
                                        .executes(context -> grant(context.getSource(),
                                                EntityArgument.getPlayers(context, "targets"),
                                                getNodeId(context, "node"), false))
                                        .then(Commands.argument("cascade", BoolArgumentType.bool())
                                                .executes(context -> grant(context.getSource(),
                                                        EntityArgument.getPlayers(context, "targets"),
                                                        getNodeId(context, "node"),
                                                        BoolArgumentType.getBool(context, "cascade")))))))
                .then(Commands.literal("revoke")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(nodeArgument()
                                        .executes(context -> revoke(context.getSource(),
                                                EntityArgument.getPlayers(context, "targets"),
                                                getNodeId(context, "node"), false))
                                        .then(Commands.argument("cascade", BoolArgumentType.bool())
                                                .executes(context -> revoke(context.getSource(),
                                                        EntityArgument.getPlayers(context, "targets"),
                                                        getNodeId(context, "node"),
                                                        BoolArgumentType.getBool(context, "cascade")))))))
                .then(Commands.literal("grantGlobal")
                        .then(nodeArgument()
                                .executes(context -> grantGlobal(context.getSource(), getNodeId(context, "node"), false))
                                .then(Commands.argument("cascade", BoolArgumentType.bool())
                                        .executes(context -> grantGlobal(context.getSource(), getNodeId(context, "node"),
                                                BoolArgumentType.getBool(context, "cascade"))))))
                .then(Commands.literal("revokeGlobal")
                        .then(nodeArgument()
                                .executes(context -> revokeGlobal(context.getSource(), getNodeId(context, "node"), false))
                                .then(Commands.argument("cascade", BoolArgumentType.bool())
                                        .executes(context -> revokeGlobal(context.getSource(), getNodeId(context, "node"),
                                                BoolArgumentType.getBool(context, "cascade"))))))
                .then(Commands.literal("status")
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(context -> status(context.getSource(),
                                        EntityArgument.getPlayer(context, "target"), Optional.empty()))
                                .then(nodeArgument()
                                        .executes(context -> status(context.getSource(),
                                                EntityArgument.getPlayer(context, "target"),
                                                Optional.of(getNodeId(context, "node")))))))
                .then(Commands.literal("list")
                        .executes(context -> list(context.getSource())))
                .then(Commands.literal("locks")
                        .then(recipeArgument()
                                .executes(context -> locks(context.getSource(), getRecipeId(context, "recipe")))))
                .then(Commands.literal("requirements")
                        .then(Commands.literal("clear")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .then(nodeArgument()
                                                .executes(context -> clearRequirements(context.getSource(),
                                                        EntityArgument.getPlayers(context, "targets"),
                                                        getNodeId(context, "node"))))))
                        .then(Commands.literal("fulfill")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .then(nodeArgument()
                                                .executes(context -> fulfillRequirements(context.getSource(),
                                                        EntityArgument.getPlayers(context, "targets"),
                                                        getNodeId(context, "node"))))))));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> nodeArgument() {
        return Commands.argument("node", ResourceLocationArgument.id())
                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                        ResearchApi.getNodeIds().stream().map(ResourceLocation::toString).sorted().toList(), builder));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> recipeArgument() {
        return Commands.argument("recipe", ResourceLocationArgument.id())
                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                        ResearchApi.getKnownLockedRecipeIds(context.getSource().getServer()).stream()
                                .map(ResourceLocation::toString)
                                .sorted()
                                .toList(), builder));
    }

    private static ResourceLocation getNodeId(CommandContext<CommandSourceStack> context, String name) {
        return ResourceLocationArgument.getId(context, name);
    }

    private static ResourceLocation getRecipeId(CommandContext<CommandSourceStack> context, String name) {
        return ResourceLocationArgument.getId(context, name);
    }

    private static int grant(CommandSourceStack source, Collection<ServerPlayer> targets, ResourceLocation nodeId,
                             boolean cascade) {
        List<UnlockResult> results = new ArrayList<>();
        for (ServerPlayer player : targets) {
            results.add(ResearchApi.grant(player, nodeId, cascade));
        }
        return reportBulkResult(source, "Granted", targets.size(), results, cascade);
    }

    private static int revoke(CommandSourceStack source, Collection<ServerPlayer> targets, ResourceLocation nodeId,
                              boolean cascade) {
        List<UnlockResult> results = new ArrayList<>();
        for (ServerPlayer player : targets) {
            results.add(ResearchApi.revoke(player, nodeId, cascade));
        }
        return reportBulkResult(source, "Revoked", targets.size(), results, cascade);
    }

    private static int grantGlobal(CommandSourceStack source, ResourceLocation nodeId, boolean cascade) {
        UnlockResult result = ResearchApi.grantGlobal(source.getServer(), nodeId, cascade);
        return reportSingleResult(source, "Granted globally", result, cascade);
    }

    private static int revokeGlobal(CommandSourceStack source, ResourceLocation nodeId, boolean cascade) {
        UnlockResult result = ResearchApi.revokeGlobal(source.getServer(), nodeId, cascade);
        return reportSingleResult(source, "Revoked globally", result, cascade);
    }

    private static int status(CommandSourceStack source, ServerPlayer target, Optional<ResourceLocation> nodeId) {
        if (!ResearchApi.isEnabled()) {
            source.sendFailure(Component.literal("Research System is disabled"));
            return 0;
        }
        if (nodeId.isPresent()) {
            ResourceLocation id = nodeId.get();
            if (ResearchApi.getNode(id).isEmpty()) {
                source.sendFailure(Component.literal("Unknown research node: " + id));
                return 0;
            }
            boolean unlocked = ResearchApi.hasUnlocked(target, id);
            source.sendSuccess(() -> Component.literal("Research node " + id + " is "
                    + (unlocked ? "unlocked" : "locked") + " for " + target.getGameProfile().getName()), false);
            return unlocked ? 1 : 0;
        }

        List<ResourceLocation> unlockedNodes = ResearchApi.getNodeIds().stream()
                .sorted(Comparator.comparing(ResourceLocation::toString))
                .filter(id -> ResearchApi.hasUnlocked(target, id))
                .toList();
        source.sendSuccess(() -> Component.literal(target.getGameProfile().getName() + " has unlocked "
                + unlockedNodes.size() + "/" + ResearchApi.getNodeIds().size() + " research nodes"), false);
        if (!unlockedNodes.isEmpty()) {
            source.sendSuccess(() -> Component.literal("Unlocked: " + joinIds(unlockedNodes)), false);
        }
        return unlockedNodes.size();
    }

    private static int list(CommandSourceStack source) {
        List<ResourceLocation> nodes = ResearchApi.getNodeIds().stream()
                .sorted(Comparator.comparing(ResourceLocation::toString))
                .toList();
        source.sendSuccess(() -> Component.literal("Research nodes: " + nodes.size()), false);
        if (!nodes.isEmpty()) {
            source.sendSuccess(() -> Component.literal(joinIds(nodes)), false);
        }
        return nodes.size();
    }

    private static int locks(CommandSourceStack source, ResourceLocation recipeId) {
        List<ResourceLocation> nodeIds = ResearchApi.getLockingNodes(source.getServer(), recipeId);
        if (nodeIds.isEmpty()) {
            source.sendSuccess(() -> Component.literal("Recipe " + recipeId + " is not locked by research"), false);
            return 0;
        }
        boolean usable = ResearchApi.canUseRecipeOnServer(source.getServer(), recipeId);
        source.sendSuccess(() -> Component.literal("Recipe " + recipeId + " is locked by " + joinIds(nodeIds)
                + " and is currently " + (usable ? "unlocked" : "locked") + " at server scope"), false);
        return usable ? 1 : 0;
    }

    private static int clearRequirements(CommandSourceStack source, Collection<ServerPlayer> targets,
                                         ResourceLocation nodeId) {
        if (ResearchApi.getNode(nodeId).isEmpty()) {
            source.sendFailure(Component.literal("Unknown research node: " + nodeId));
            return 0;
        }
        int changed = 0;
        for (ServerPlayer player : targets) {
            if (ResearchApi.clearProgress(source.getServer(), player.getUUID(), nodeId)) {
                changed++;
            }
        }
        int finalChanged = changed;
        source.sendSuccess(() -> Component.literal("Cleared requirement progress for " + nodeId + " on "
                + finalChanged + "/" + targets.size() + " player(s)"), true);
        return changed;
    }

    private static int fulfillRequirements(CommandSourceStack source, Collection<ServerPlayer> targets,
                                           ResourceLocation nodeId) {
        if (ResearchApi.getNode(nodeId).isEmpty()) {
            source.sendFailure(Component.literal("Unknown research node: " + nodeId));
            return 0;
        }
        int unlocked = 0;
        for (ServerPlayer player : targets) {
            if (ResearchApi.fulfillProgress(source.getServer(), player.getUUID(), nodeId).unlocked()) {
                unlocked++;
            }
        }
        int finalUnlocked = unlocked;
        source.sendSuccess(() -> Component.literal("Fulfilled requirement progress for " + nodeId + "; unlocked for "
                + finalUnlocked + "/" + targets.size() + " player(s)"), true);
        return unlocked;
    }

    private static int reportBulkResult(CommandSourceStack source, String verb, int targetCount,
                                        List<UnlockResult> results, boolean cascade) {
        Optional<UnlockResult.Status> blockingStatus = results.stream()
                .map(UnlockResult::status)
                .filter(status -> status == UnlockResult.Status.DISABLED || status == UnlockResult.Status.UNKNOWN_NODE)
                .findFirst();
        if (blockingStatus.isPresent()) {
            sendFailure(source, results.getFirst());
            return 0;
        }
        long changedPlayers = results.stream().filter(UnlockResult::changed).count();
        int changedNodes = results.stream().mapToInt(result -> result.changedNodes().size()).sum();
        ResourceLocation nodeId = results.isEmpty() ? null : results.getFirst().requestedNode();
        source.sendSuccess(() -> Component.literal(verb + " " + nodeId + " for " + targetCount
                + " player(s); changed " + changedNodes + " node unlock(s) across " + changedPlayers
                + " player(s)" + cascadeSuffix(cascade, results)), true);
        return changedNodes;
    }

    private static int reportSingleResult(CommandSourceStack source, String verb, UnlockResult result, boolean cascade) {
        if (result.status() == UnlockResult.Status.DISABLED || result.status() == UnlockResult.Status.UNKNOWN_NODE) {
            sendFailure(source, result);
            return 0;
        }
        source.sendSuccess(() -> Component.literal(verb + " " + result.requestedNode() + "; changed "
                + result.changedNodes().size() + " node unlock(s)" + cascadeSuffix(cascade, List.of(result))), true);
        return result.changedNodes().size();
    }

    private static void sendFailure(CommandSourceStack source, UnlockResult result) {
        String message = switch (result.status()) {
            case DISABLED -> "Research System is disabled";
            case UNKNOWN_NODE -> "Unknown research node: " + result.requestedNode();
            case ALREADY_UNLOCKED -> "Research node already unlocked: " + result.requestedNode();
            case ALREADY_REVOKED -> "Research node already revoked: " + result.requestedNode();
            case UNLOCKED, REVOKED -> result.status().name().toLowerCase(Locale.ROOT) + ": " + result.requestedNode();
        };
        source.sendFailure(Component.literal(message));
    }

    private static String cascadeSuffix(boolean cascade, List<UnlockResult> results) {
        if (!cascade) return "";
        int uniqueNodes = results.stream()
                .flatMap(result -> result.changedNodes().stream())
                .collect(java.util.stream.Collectors.toSet())
                .size();
        return "; cascade changed " + uniqueNodes + " unique node(s)";
    }

    private static String joinIds(List<ResourceLocation> ids) {
        return ids.stream().map(ResourceLocation::toString).collect(java.util.stream.Collectors.joining(", "));
    }
}
