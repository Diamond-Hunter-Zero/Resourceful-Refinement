package com.resourceful_refinement.content.glare;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.config.ServerConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Locale;

@EventBusSubscriber(modid = ResourcefulRefinementMain.MOD_ID)
public final class GlareCommands {
    private GlareCommands() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("rrglare")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("stats").executes(context -> stats(context.getSource())))
                .then(Commands.literal("inspect")
                        .executes(context -> inspectTarget(context.getSource()))
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(context -> inspect(context.getSource(), BlockPosArgument.getBlockPos(context, "pos")))))
                .then(Commands.literal("validate")
                        .executes(context -> validate(context.getSource(), ServerConfig.GLARE_LOS_CHECKS_PER_TICK.get()))
                        .then(Commands.argument("budget", IntegerArgumentType.integer(1, 100_000))
                                .executes(context -> validate(context.getSource(), IntegerArgumentType.getInteger(context, "budget")))))
                .then(Commands.literal("rebuild").executes(context -> rebuild(context.getSource()))));
    }

    private static int stats(CommandSourceStack source) {
        GlareSavedData.Diagnostics stats = GlareService.diagnostics(source.getLevel());
        source.sendSuccess(() -> Component.literal("GLARE: " + stats.persistedNodes() + " persisted nodes ("
                + stats.loadedNodesInDimension() + " loaded here), " + stats.networks() + " networks, "
                + stats.links() + " links [" + stats.validLinks() + " valid, " + stats.blockedLinks()
                + " blocked, " + stats.unknownLinks() + " unknown]"), false);
        source.sendSuccess(() -> Component.literal("LoS budget: " + ServerConfig.GLARE_LOS_CHECKS_PER_TICK.get()
                + "/tick; debug logging: " + ServerConfig.GLARE_DEBUG_LOGGING.get()), false);
        return stats.persistedNodes();
    }

    private static int inspectTarget(CommandSourceStack source) throws CommandSyntaxException {
        HitResult hit = source.getPlayerOrException().pick(64.0D, 0.0F, false);
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK) {
            source.sendFailure(Component.literal("No block targeted within 64 blocks"));
            return 0;
        }
        return inspect(source, blockHit.getBlockPos());
    }

    private static int inspect(CommandSourceStack source, BlockPos pos) {
        ServerLevel level = source.getLevel();
        GlareNodePos nodePos = GlareNodePos.of(level, pos);
        GlareSavedData.NodeRecord node = GlareService.getNode(level, nodePos).orElse(null);
        if (node == null) {
            source.sendFailure(Component.literal("No persisted GLARE node at " + pos.toShortString()));
            return 0;
        }
        boolean live = level.isLoaded(pos) && level.getBlockEntity(pos) instanceof IGlareNode;
        source.sendSuccess(() -> Component.literal("GLARE node " + nodePos.toShortString() + " ["
                + (live ? "loaded" : "persisted/unloaded") + "]"), false);
        source.sendSuccess(() -> Component.literal("Network: " + (node.networkId == null ? "none" : node.networkId)
                + "; links: " + node.lastKnownLinks.size() + "/" + node.maxLinks + "; Lux: +"
                + node.luxProduced + " -" + node.luxAllocated + "; status: " + node.status), false);
        for (GlareLink link : GlareService.getLinks(level, nodePos)) {
            source.sendSuccess(() -> Component.literal("  -> " + link.other(nodePos).toShortString() + " ["
                    + GlareSavedData.get(level).getLinkValidity(link).name().toLowerCase(Locale.ROOT) + "]"), false);
        }
        return 1;
    }

    private static int validate(CommandSourceStack source, int budget) {
        int validated = GlareService.validateLoadedLinks(source.getLevel(), budget);
        source.sendSuccess(() -> Component.literal("Validated " + validated + " loaded GLARE links (examined up to "
                + budget + ")"), false);
        return validated;
    }

    private static int rebuild(CommandSourceStack source) {
        GlareService.forceRebuild(source.getLevel());
        source.sendSuccess(() -> Component.literal("Rebuilt persisted GLARE graph and refreshed loaded endpoints"), true);
        return 1;
    }
}
