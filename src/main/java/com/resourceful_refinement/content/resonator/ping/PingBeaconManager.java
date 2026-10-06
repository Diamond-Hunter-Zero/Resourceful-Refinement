package com.resourceful_refinement.content.resonator.ping;

import com.resourceful_refinement.network.PingBeaconSyncPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

/**
 * Authoritative server-side state for the single active Ping Beacon.
 *
 * <p>At most one beacon exists at a time — deploying a new one supersedes any existing one. The beacon is transient
 * (30s, not persisted across restarts): it lives only as in-memory state keyed to the running server, is broadcast
 * to all clients for rendering, and is cleared automatically when it expires (ticked each server tick) or when
 * replaced. Clients are told about it via {@link PingBeaconSyncPayload}; the server owns timing.</p>
 */
public final class PingBeaconManager {

    /** Lifetime of a deployed Ping Beacon, in ticks (30 seconds). */
    public static final int DURATION_TICKS = 20 * 30;

    private static boolean active;
    @Nullable private static ResourceKey<Level> dimension;
    private static BlockPos pos = BlockPos.ZERO;
    private static long expiryGameTime;

    private PingBeaconManager() {}

    /** Deploys a beacon at {@code target} in {@code level}, replacing any existing one, and syncs to all clients. */
    public static void place(ServerLevel level, BlockPos target) {
        active = true;
        dimension = level.dimension();
        pos = target.immutable();
        expiryGameTime = gameTime(level.getServer()) + DURATION_TICKS;
        broadcast(level.getServer());
    }

    /** Clears the active beacon (if any) and syncs the removal to all clients. */
    public static void clear(MinecraftServer server) {
        if (!active) {
            return;
        }
        active = false;
        dimension = null;
        broadcast(server);
    }

    /** Server-tick hook: expires the beacon once its lifetime elapses. */
    public static void tick(MinecraftServer server) {
        if (active && gameTime(server) >= expiryGameTime) {
            clear(server);
        }
    }

    /** Sends the current beacon state to a single player (used when a player joins). */
    public static void syncTo(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server != null) {
            PacketDistributor.sendToPlayer(player, snapshot(server));
        }
    }

    private static void broadcast(MinecraftServer server) {
        PacketDistributor.sendToAllPlayers(snapshot(server));
    }

    private static PingBeaconSyncPayload snapshot(MinecraftServer server) {
        if (!active || dimension == null) {
            return PingBeaconSyncPayload.inactive();
        }
        int remaining = (int) Math.max(0L, expiryGameTime - gameTime(server));
        return new PingBeaconSyncPayload(true, dimension.location(), pos, remaining);
    }

    /** Canonical timeline for expiry — the overworld's game time advances every server tick. */
    private static long gameTime(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld != null ? overworld.getGameTime() : server.getTickCount();
    }
}
