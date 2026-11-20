package com.resourceful_refinement.content.glare.remote;

import com.resourceful_refinement.client.fx.lightning.ClientLightning;
import com.resourceful_refinement.client.fx.lightning.LightningStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client-only visuals for the Remote Entanglement Transporter. While charging, three thin rings of lightning hover in the
 * 1x2 detection chamber in front of the controller, each scanning up and down out of phase with the others. A
 * successful teleport strikes a vertical bolt from the chamber floor to its ceiling. Only referenced from client-side
 * code paths (synced reads and block events).
 */
public final class RemoteEntanglementTransporterClientFx {
    private static final int RING_COUNT = 3;
    /** Arcs per ring; each is one short, branchless beam. */
    private static final int RING_SEGMENTS = 8;
    /** Just inside the chamber walls (which sit on the block edges). */
    private static final double RING_RADIUS = 0.42;
    /** Scan limits, measured from the chamber floor: just above the pad glow to just below the front glow's top. */
    private static final double SCAN_MIN = 2.0 / 16.0;
    private static final double SCAN_MAX = 29.0 / 16.0;
    private static final double SCAN_PERIOD_TICKS = 40.0;

    /** Strike endpoints: the pad's surface and the underside of the controller's top section. */
    private static final double STRIKE_BOTTOM = 1.1 / 16.0;
    private static final double STRIKE_TOP = 31.0 / 16.0;
    private static final int STRIKE_TICKS = 10;

    /** Same thin, unbranched ring as the Entangler Depot, scaled up slightly for a body-sized chamber. */
    private static final LightningStyle RING = LightningStyle.builder()
            .layer(0.07F, 0x30E070F0, 0.0F)
            .layer(0.026F, 0x90F0A0FF, 0.0F)
            .layer(0.009F, 0xE0FFF0FF, 0.6F)
            .segmentLength(0.12F)
            .jaggedness(0.45F)
            .maxOffset(0.07F)
            .maxBranchDepth(0)
            .restrikeInterval(1.5F)
            .flicker(0.4F)
            .afterimage(0.0F)
            .endpointGlow(0.0F)
            .build();

    private static final Map<BlockPos, List<ClientLightning.Beam>> RINGS = new HashMap<>();

    private RemoteEntanglementTransporterClientFx() {}

    /** Starts or stops the scanning rings for the controller at {@code pos}; repeated calls with the same state are no-ops. */
    public static void setScanning(Level level, BlockPos pos, Direction facing, boolean scanning) {
        BlockPos key = pos.immutable();
        List<ClientLightning.Beam> rings = RINGS.get(key);
        if (rings != null && !rings.isEmpty() && !rings.getFirst().isAlive()) {
            RINGS.remove(key); // cleared elsewhere, e.g. on level change
            rings = null;
        }
        if (!scanning) {
            if (rings != null) rings.forEach(ClientLightning.Beam::discard);
            RINGS.remove(key);
            return;
        }
        if (rings != null || Minecraft.getInstance().level != level) return;

        BlockPos chamber = key.relative(facing);
        List<ClientLightning.Beam> beams = new ArrayList<>(RING_COUNT * RING_SEGMENTS);
        for (int ring = 0; ring < RING_COUNT; ring++) {
            double scanPhase = ring * Math.PI * 2 / RING_COUNT;
            double spin = level.random.nextDouble() * Math.PI * 2;
            for (int i = 0; i < RING_SEGMENTS; i++) {
                double a0 = spin + i * Math.PI * 2 / RING_SEGMENTS;
                double a1 = spin + (i + 1) * Math.PI * 2 / RING_SEGMENTS;
                beams.add(ClientLightning.spawn(t -> ringPoint(chamber, a0, scanPhase, t),
                        t -> ringPoint(chamber, a1, scanPhase, t), RING, ClientLightning.FOREVER));
            }
        }
        RINGS.put(key, beams);
    }

    /** One-shot vertical ELECTRIC bolt through the chamber, fired by the teleport block event on both endpoints. */
    public static void playTeleportStrike(Level level, BlockPos pos, Direction facing) {
        if (Minecraft.getInstance().level != level) return;
        BlockPos chamber = pos.relative(facing);
        Vec3 bottom = Vec3.atBottomCenterOf(chamber).add(0, STRIKE_BOTTOM, 0);
        Vec3 top = Vec3.atBottomCenterOf(chamber).add(0, STRIKE_TOP, 0);
        ClientLightning.spawn(top, bottom, LightningStyle.ELECTRIC, STRIKE_TICKS);
    }

    private static Vec3 ringPoint(BlockPos chamber, double angle, double scanPhase, float partialTick) {
        var level = Minecraft.getInstance().level;
        double time = level == null ? 0 : level.getGameTime() + partialTick;
        double wave = 0.5 + 0.5 * Math.sin(time * Math.PI * 2 / SCAN_PERIOD_TICKS + scanPhase);
        double y = SCAN_MIN + (SCAN_MAX - SCAN_MIN) * wave;
        return new Vec3(chamber.getX() + 0.5 + Math.cos(angle) * RING_RADIUS, chamber.getY() + y,
                chamber.getZ() + 0.5 + Math.sin(angle) * RING_RADIUS);
    }
}
