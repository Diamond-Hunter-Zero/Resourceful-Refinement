package com.resourceful_refinement.content.resonator.ping;

import com.resourceful_refinement.network.PingBeaconSyncPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Client-side mirror of the active Ping Beacon, updated from {@link PingBeaconSyncPayload}. Holds the beacon's
 * dimension and position and estimates its remaining lifetime locally (for the end-of-life fade); the server remains
 * authoritative and will send an explicit deactivation when it truly expires.
 */
public final class ClientPingBeacon {

    /** Ticks over which the beam fades out as it nears expiry. */
    private static final float FADE_TICKS = 20.0F;

    private static boolean active;
    @Nullable private static ResourceLocation dimension;
    private static BlockPos pos = BlockPos.ZERO;
    private static long expiryGameTime;

    private ClientPingBeacon() {}

    public static void accept(PingBeaconSyncPayload payload) {
        active = payload.active();
        if (active) {
            dimension = payload.dimension();
            pos = payload.pos();
            expiryGameTime = clientGameTime() + payload.remainingTicks();
        } else {
            dimension = null;
        }
    }

    public static boolean isActiveIn(ResourceLocation dim) {
        return active && dim != null && dim.equals(dimension);
    }

    public static BlockPos pos() {
        return pos;
    }

    /** Opacity multiplier in [0,1]; ramps down over the final {@link #FADE_TICKS} of the beacon's life. */
    public static float alpha(float partialTick) {
        double remaining = (expiryGameTime - clientGameTime()) - partialTick;
        if (remaining <= 0.0D) {
            return 0.0F;
        }
        return remaining >= FADE_TICKS ? 1.0F : (float) (remaining / FADE_TICKS);
    }

    private static long clientGameTime() {
        Level level = Minecraft.getInstance().level;
        return level != null ? level.getGameTime() : 0L;
    }
}
