package com.resourceful_refinement.client.fx.lightning;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

/**
 * Client-only {@code /rr_lightning} command for previewing the lightning renderer in game:
 * <ul>
 *   <li>{@code /rr_lightning strike [ticks]} — one-shot bolt from your hand to whatever you're looking at;</li>
 *   <li>{@code /rr_lightning between <from> <to> [ticks]} — bolt between two block centres (default 100 ticks);</li>
 *   <li>{@code /rr_lightning hold} — toggle a continuous beam that follows your hand and crosshair (weapon preview);</li>
 *   <li>{@code /rr_lightning style <electric|arc|railgun|tesla_charge|crimson|joint>} — switch the style used by the above;</li>
 *   <li>{@code /rr_lightning clear} — remove all beams.</li>
 * </ul>
 */
public final class LightningTestCommand {

    private static final double RANGE = 48.0D;
    private static final Map<String, LightningStyle> STYLES = Map.of(
            "electric", LightningStyle.ELECTRIC,
            "arc", LightningStyle.ARC,
            "railgun", LightningStyle.RAILGUN_ARC,
            "tesla_charge", LightningStyle.PROCESSING_ARC,
            "crimson", LightningStyle.CRIMSON,
            "joint", LightningStyle.JOINT);

    private static LightningStyle style = LightningStyle.ELECTRIC;
    private static ClientLightning.Beam held;

    private LightningTestCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("rr_lightning")
                .then(Commands.literal("strike")
                        .executes(ctx -> strike(ctx, 12))
                        .then(Commands.argument("ticks", IntegerArgumentType.integer(1, 2000))
                                .executes(ctx -> strike(ctx, IntegerArgumentType.getInteger(ctx, "ticks")))))
                .then(Commands.literal("between")
                        .then(Commands.argument("from", BlockPosArgument.blockPos())
                                .then(Commands.argument("to", BlockPosArgument.blockPos())
                                        .executes(ctx -> between(ctx, 100))
                                        .then(Commands.argument("ticks", IntegerArgumentType.integer(1, 72000))
                                                .executes(ctx -> between(ctx,
                                                        IntegerArgumentType.getInteger(ctx, "ticks")))))))
                .then(Commands.literal("hold").executes(LightningTestCommand::toggleHold))
                .then(Commands.literal("style")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(STYLES.keySet(), builder))
                                .executes(LightningTestCommand::setStyle)))
                .then(Commands.literal("clear").executes(ctx -> {
                    ClientLightning.clear();
                    held = null;
                    return 1;
                })));
    }

    private static int strike(CommandContext<CommandSourceStack> ctx, int ticks) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return 0;
        }
        ClientLightning.spawn(handPosition(player, 1.0F), targetPosition(player, 1.0F), style, ticks);
        return 1;
    }

    /** Bolt between the centres of two block positions. Resolves coordinates directly (works for ~ ~ ~ and ^ ^ ^)
     *  rather than via {@code getLoadedBlockPos}, which needs a server level. */
    private static int between(CommandContext<CommandSourceStack> ctx, int ticks) {
        CommandSourceStack source = ctx.getSource();
        BlockPos from = ctx.getArgument("from", Coordinates.class).getBlockPos(source);
        BlockPos to = ctx.getArgument("to", Coordinates.class).getBlockPos(source);
        ClientLightning.spawn(Vec3.atCenterOf(from), Vec3.atCenterOf(to), style, ticks);
        source.sendSystemMessage(Component.literal("Lightning " + from.toShortString() + " -> "
                + to.toShortString() + " for " + ticks + " ticks"));
        return 1;
    }

    private static int toggleHold(CommandContext<CommandSourceStack> ctx) {
        if (held != null && held.isAlive()) {
            held.discard();
            held = null;
            ctx.getSource().sendSystemMessage(Component.literal("Lightning beam off"));
            return 1;
        }
        held = ClientLightning.spawn(
                partialTick -> {
                    LocalPlayer p = Minecraft.getInstance().player;
                    return p == null ? Vec3.ZERO : handPosition(p, partialTick);
                },
                partialTick -> {
                    LocalPlayer p = Minecraft.getInstance().player;
                    return p == null ? Vec3.ZERO : targetPosition(p, partialTick);
                },
                style, ClientLightning.FOREVER);
        ctx.getSource().sendSystemMessage(Component.literal("Lightning beam on — run again to stop"));
        return 1;
    }

    private static int setStyle(CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        LightningStyle chosen = STYLES.get(name);
        if (chosen == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown style: " + name));
            return 0;
        }
        style = chosen;
        if (held != null && held.isAlive()) {
            held.setStyle(chosen);
        }
        ctx.getSource().sendSystemMessage(Component.literal("Lightning style: " + name));
        return 1;
    }

    /** Approximates the first-person main-hand position: a little forward, down and to the hand's side. */
    private static Vec3 handPosition(LocalPlayer player, float partialTick) {
        Vec3 eye = player.getEyePosition(partialTick);
        Vec3 forward = player.getViewVector(partialTick);
        Vec3 right = forward.cross(new Vec3(0.0D, 1.0D, 0.0D));
        right = right.lengthSqr() < 1.0E-6D ? new Vec3(1.0D, 0.0D, 0.0D) : right.normalize();
        Vec3 up = right.cross(forward).normalize();
        double side = player.getMainArm() == HumanoidArm.RIGHT ? 1.0D : -1.0D;
        return eye.add(forward.scale(0.55D)).add(right.scale(0.32D * side)).add(up.scale(-0.24D));
    }

    private static Vec3 targetPosition(LocalPlayer player, float partialTick) {
        HitResult hit = player.pick(RANGE, partialTick, false);
        if (hit.getType() != HitResult.Type.MISS) {
            return hit.getLocation();
        }
        return player.getEyePosition(partialTick).add(player.getViewVector(partialTick).scale(RANGE));
    }
}
