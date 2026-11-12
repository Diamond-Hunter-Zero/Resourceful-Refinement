package com.resourceful_refinement.client.fx.lightning;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Client-side manager for transient, world-space lightning beams — weapon shots, zaps, one-off strikes — that aren't
 * owned by a block entity or entity renderer. Spawn a beam, optionally keep the handle to move or end it, and it is
 * drawn every frame until it expires.
 * <pre>{@code
 * // One-shot 8-tick zap between two fixed points:
 * ClientLightning.spawn(BeamAnchor.fixed(muzzle), BeamAnchor.fixed(hit), LightningStyle.ELECTRIC, 8);
 *
 * // Continuous beam that tracks moving anchors until discarded:
 * ClientLightning.Beam beam = ClientLightning.spawn(gunTip, crosshairHit, LightningStyle.ELECTRIC, ClientLightning.FOREVER);
 * ...
 * beam.discard();
 * }</pre>
 * Beams are dropped automatically when the client changes level.
 */
public final class ClientLightning {

    /** Pass as a lifetime for beams that live until {@link Beam#discard()}. */
    public static final int FOREVER = -1;
    private static final float FADE_IN_TICKS = 1.0F;
    private static final float FADE_OUT_TICKS = 4.0F;

    private static final List<Beam> BEAMS = new ArrayList<>();

    private ClientLightning() {}

    /** A beam endpoint evaluated every frame, so beams can follow players, entities or moving parts. */
    @FunctionalInterface
    public interface BeamAnchor {
        Vec3 position(float partialTick);

        static BeamAnchor fixed(Vec3 position) {
            return partialTick -> position;
        }
    }

    /** Handle to a live beam. */
    public static final class Beam {
        private final ClientLevel level;
        private final long seed;
        private final long spawnTime;
        private BeamAnchor from;
        private BeamAnchor to;
        private LightningStyle style;
        private int lifetime;
        private float discardAt = Float.NaN;

        private Beam(ClientLevel level, BeamAnchor from, BeamAnchor to, LightningStyle style, int lifetime) {
            this.level = level;
            this.from = from;
            this.to = to;
            this.style = style;
            this.lifetime = lifetime;
            this.seed = ThreadLocalRandom.current().nextLong();
            this.spawnTime = level.getGameTime();
        }

        public void setEndpoints(BeamAnchor from, BeamAnchor to) {
            this.from = from;
            this.to = to;
        }

        public void setStyle(LightningStyle style) {
            this.style = style;
        }

        /** Starts the fade-out now; the beam is removed once it has faded. */
        public void discard() {
            if (Float.isNaN(discardAt)) {
                discardAt = level.getGameTime() - spawnTime;
            }
        }

        public boolean isAlive() {
            return BEAMS.contains(this);
        }

        /** Opacity at the given age, or a negative value once the beam has fully faded out. */
        private float alpha(float age) {
            float end = !Float.isNaN(discardAt) ? discardAt
                    : lifetime == FOREVER ? Float.POSITIVE_INFINITY : lifetime;
            if (age >= end + FADE_OUT_TICKS) {
                return -1.0F;
            }
            float fadeIn = Math.min(1.0F, (age + 1.0F) / FADE_IN_TICKS);
            float fadeOut = age <= end ? 1.0F : 1.0F - (age - end) / FADE_OUT_TICKS;
            return Math.max(0.0F, Math.min(fadeIn, fadeOut));
        }
    }

    public static Beam spawn(BeamAnchor from, BeamAnchor to, LightningStyle style, int lifetimeTicks) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            throw new IllegalStateException("No client level to spawn lightning in");
        }
        Beam beam = new Beam(level, from, to, style, lifetimeTicks);
        BEAMS.add(beam);
        return beam;
    }

    public static Beam spawn(Vec3 from, Vec3 to, LightningStyle style, int lifetimeTicks) {
        return spawn(BeamAnchor.fixed(from), BeamAnchor.fixed(to), style, lifetimeTicks);
    }

    public static void clear() {
        BEAMS.clear();
    }

    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || BEAMS.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            BEAMS.clear();
            return;
        }

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        float now = level.getGameTime() + partialTick;
        Vec3 camera = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffer = minecraft.renderBuffers().bufferSource();

        boolean drew = false;
        for (Iterator<Beam> it = BEAMS.iterator(); it.hasNext(); ) {
            Beam beam = it.next();
            if (beam.level != level) {
                it.remove();
                continue;
            }
            float alpha = beam.alpha(now - beam.spawnTime);
            if (alpha < 0.0F) {
                it.remove();
                continue;
            }
            Vec3 from = beam.from.position(partialTick);
            Vec3 to = beam.to.position(partialTick);
            if (!event.getFrustum().isVisible(LightningRenderer.renderBounds(from, to, beam.style))) {
                continue;
            }

            // Render relative to the start point so vertex floats stay precise far from the world origin.
            poseStack.pushPose();
            poseStack.translate(from.x - camera.x, from.y - camera.y, from.z - camera.z);
            LightningRenderer.render(poseStack, buffer, Vec3.ZERO, to.subtract(from), beam.style, beam.seed, now, alpha);
            poseStack.popPose();
            drew = true;
        }
        if (drew) {
            buffer.endBatch(LightningRenderType.LIGHTNING);
        }
    }
}
