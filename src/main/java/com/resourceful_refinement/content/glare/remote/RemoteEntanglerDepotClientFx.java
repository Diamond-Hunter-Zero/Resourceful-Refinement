package com.resourceful_refinement.content.glare.remote;

import com.resourceful_refinement.client.fx.lightning.ClientLightning;
import com.resourceful_refinement.client.fx.lightning.LightningStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client-only visuals for the Remote Entangler Depot: while a sender is charging, a thin ring of lightning hovers over
 * its top plate, drifting slowly up and down as if scanning the held item. Only referenced from client-side code paths.
 */
public final class RemoteEntanglerDepotClientFx {
    /** Arcs around the ring; each is one short, branchless beam. */
    private static final int RING_SEGMENTS = 6;
    private static final double RING_RADIUS = 0.36;
    /** Centre height of the scan, just above the top plate  where the item sits. */
    private static final double RING_HEIGHT = 14.5 / 16.0;
    /** Half the vertical travel of the scan, in blocks. */
    private static final double SCAN_AMPLITUDE = 1.5 / 16.0;
    private static final double SCAN_PERIOD_TICKS = 40.0;

    /** Thin, unbranched, glow-free arc: cheap to build and subtle at depot scale. */
    private static final LightningStyle RING = LightningStyle.builder()
            .layer(0.06F, 0x3044E0D0, 0.0F)
            .layer(0.022F, 0x9070F0E0, 0.0F)
            .layer(0.008F, 0xE0F0FFFC, 0.6F)
            .segmentLength(0.1F)
            .jaggedness(0.45F)
            .maxOffset(0.06F)
            .maxBranchDepth(0)
            .restrikeInterval(1.5F)
            .flicker(0.4F)
            .afterimage(0.0F)
            .endpointGlow(0.0F)
            .build();

    private static final Map<BlockPos, List<ClientLightning.Beam>> RINGS = new HashMap<>();

    private RemoteEntanglerDepotClientFx() {}

    /** Starts or stops the scanning ring for the depot at {@code pos}; repeated calls with the same state are no-ops. */
    public static void setScanning(Level level, BlockPos pos, boolean scanning) {
        BlockPos key = pos.immutable();
        List<ClientLightning.Beam> ring = RINGS.get(key);
        if (ring != null && !ring.isEmpty() && !ring.getFirst().isAlive()) {
            RINGS.remove(key); // cleared elsewhere, e.g. on level change
            ring = null;
        }
        if (!scanning) {
            if (ring != null) ring.forEach(ClientLightning.Beam::discard);
            RINGS.remove(key);
            return;
        }
        if (ring != null || Minecraft.getInstance().level != level) return;

        double phase = level.random.nextDouble() * Math.PI * 2;
        List<ClientLightning.Beam> beams = new ArrayList<>(RING_SEGMENTS);
        for (int i = 0; i < RING_SEGMENTS; i++) {
            double a0 = phase + i * Math.PI * 2 / RING_SEGMENTS;
            double a1 = phase + (i + 1) * Math.PI * 2 / RING_SEGMENTS;
            beams.add(ClientLightning.spawn(t -> point(key, a0, t), t -> point(key, a1, t), RING, ClientLightning.FOREVER));
        }
        RINGS.put(key, beams);
    }

    private static Vec3 point(BlockPos pos, double angle, float partialTick) {
        var level = Minecraft.getInstance().level;
        double time = level == null ? 0 : level.getGameTime() + partialTick;
        double y = RING_HEIGHT + Math.sin(time * Math.PI * 2 / SCAN_PERIOD_TICKS) * SCAN_AMPLITUDE;
        return new Vec3(pos.getX() + 0.5 + Math.cos(angle) * RING_RADIUS, pos.getY() + y,
                pos.getZ() + 0.5 + Math.sin(angle) * RING_RADIUS);
    }
}
