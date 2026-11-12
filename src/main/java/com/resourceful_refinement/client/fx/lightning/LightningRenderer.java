package com.resourceful_refinement.client.fx.lightning;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;


/**
 * Draws a procedural lightning bolt between two points as camera-facing ribbons.
 * <p>
 * <b>Usage.</b> Call {@link #render} from anything that has a {@link PoseStack} and {@link MultiBufferSource}:
 * <ul>
 *   <li>a block entity renderer — pass endpoints in block-local space (e.g. {@code new Vec3(0.5, 1.2, 0.5)});</li>
 *   <li>an entity renderer — pass endpoints relative to the entity;</li>
 *   <li>a {@code RenderLevelStageEvent} handler — translate the pose to the bolt's start minus the camera position
 *       and pass endpoints relative to that (see {@link ClientLightning}), then {@code endBatch} the
 *       {@link LightningRenderType#LIGHTNING} type.</li>
 * </ul>
 * The camera position is recovered from the pose matrix itself, so ribbons face the viewer in any of those spaces.
 * <p>
 * <b>Animation.</b> The shape is a pure function of {@code (seed, floor(time / restrikeInterval))}: hold the seed
 * constant per emitter (e.g. {@code blockPos.asLong()}) and feed game time, and the bolt re-strikes on its own
 * schedule with nothing to store or tick. Endpoints may move every frame; the shape stretches with them.
 * <p>
 * <b>Look.</b> Each {@link LightningStyle.Layer} is a ribbon with a bright centre line fading to its edges, drawn
 * additively, so stacking a wide dim blue layer, a medium cyan one and a thin white one gives a hot core with a halo
 * and no texture. Forks are thinner, dimmer and taper to a tip.
 */
public final class LightningRenderer {

    /**
     * Rough angular size of one screen pixel (radians) at normal FOV/1080p. Ribbons never get thinner than this at
     * their distance, and lose brightness instead, so distant bolts stay as a faint unbroken line rather than
     * shimmering in and out between pixels.
     */
    private static final float MIN_ANGULAR_HALF_WIDTH = 0.0011F;
    private static final float TAPER_WIDTH = 0.85F;
    private static final float TAPER_ALPHA = 0.6F;
    private static final int GLOW_DISC_SEGMENTS = 12;
    private static final long FLICKER_SALT = 0x5DEECE66DL;
    /**
     * Finest segment length per block of camera distance (~7 px on screen). Beyond the distance where this exceeds
     * the style's own segment length, bolts subdivide less: the same path with fewer, invisible micro-kinks.
     */
    private static final float LOD_SEGMENT_PER_DISTANCE = 0.008F;

    // Render-thread scratch, reused across frames and grown on demand so steady-state rendering allocates nothing
    // per strand. render() is never re-entered, so sharing is safe.
    private static final Matrix4f INVERSE_POSE = new Matrix4f();
    private static final Vector3f CAMERA = new Vector3f();
    private static double[] worldPoints = new double[3 * 257];
    private static float[] sides = new float[3 * 257];
    private static float[] distances = new float[257];

    private LightningRenderer() {}

    /**
     * Draws one animated bolt.
     *
     * @param poseStack the current pose; endpoints are interpreted in its local space
     * @param buffer    buffer source; geometry goes to {@link LightningRenderType#LIGHTNING}
     * @param from      start point (local space)
     * @param to        end point (local space)
     * @param style     look and behaviour
     * @param seed      stable per-emitter seed; different emitters should use different seeds
     * @param timeTicks animation clock in ticks, e.g. {@code level.getGameTime() + partialTick}
     * @param alpha     overall opacity multiplier (0..1) for fade in/out
     */
    public static void render(PoseStack poseStack, MultiBufferSource buffer, Vec3 from, Vec3 to,
            LightningStyle style, long seed, float timeTicks, float alpha) {
        if (alpha <= 0.0F) {
            return;
        }
        Matrix4f matrix = poseStack.last().pose();
        Vector3f camera = INVERSE_POSE.set(matrix).invert().transformPosition(CAMERA.zero());
        VertexConsumer consumer = buffer.getBuffer(LightningRenderType.LIGHTNING);

        double cycles = timeTicks / style.restrikeInterval();
        long bucket = Mth.lfloor(cycles);
        float phase = (float) (cycles - bucket);

        long currentSeed = strikeSeed(seed, bucket);
        float currentBrightness = alpha * flickerFactor(currentSeed, style.flicker());

        if (style.afterimage() > 0.0F) {
            long previousSeed = strikeSeed(seed, bucket - 1);
            float previousBrightness = alpha * style.afterimage() * flickerFactor(previousSeed, style.flicker())
                    * afterimageFade(phase);
            drawBolt(matrix, consumer, camera, from, to, style, previousSeed, previousBrightness, true);
        }
        drawBolt(matrix, consumer, camera, from, to, style, currentSeed, currentBrightness, false);

        if (style.endpointGlow() > 0.0F) {
            drawGlowDiscs(matrix, consumer, camera, from, style, currentBrightness);
            drawGlowDiscs(matrix, consumer, camera, to, style, currentBrightness);
        }
    }

    /** Draws a single static bolt shape (no restrike/flicker/afterimage) — for one-frame effects or custom timing. */
    public static void renderStatic(PoseStack poseStack, MultiBufferSource buffer, Vec3 from, Vec3 to,
            LightningStyle style, long shapeSeed, float brightness) {
        if (brightness <= 0.0F) {
            return;
        }
        Matrix4f matrix = poseStack.last().pose();
        Vector3f camera = INVERSE_POSE.set(matrix).invert().transformPosition(CAMERA.zero());
        VertexConsumer consumer = buffer.getBuffer(LightningRenderType.LIGHTNING);
        drawBolt(matrix, consumer, camera, from, to, style, shapeSeed, brightness, false);
        if (style.endpointGlow() > 0.0F) {
            drawGlowDiscs(matrix, consumer, camera, from, style, brightness);
            drawGlowDiscs(matrix, consumer, camera, to, style, brightness);
        }
    }

    private static void drawBolt(Matrix4f matrix, VertexConsumer consumer, Vector3f camera, Vec3 from, Vec3 to,
            LightningStyle style, long shapeSeed, float brightness, boolean ghost) {
        if (brightness <= 0.004F) {
            return;
        }
        // Level of detail from the bolt's nearest point to the camera, so it only coarsens once all of it is far.
        float distance = (float) distanceToSegment(camera, from, to);
        float pixel = distance * MIN_ANGULAR_HALF_WIDTH;
        LightningStyle.Layer widest = widestLayer(style);
        LightningStyle.Layer thinnest = thinnestLayer(style);

        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double dz = to.z - from.z;
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length < 1.0E-4D) {
            return;
        }

        // The shape is cached in a canonical frame (origin -> +X) and only regenerated on re-strike, so the
        // afterimage normally reuses the shape it drew as the current bolt one interval earlier.
        LightningShapeCache.Shape shape = LightningShapeCache.get(shapeSeed, style, length,
                distance * LOD_SEGMENT_PER_DISTANCE);

        // Orthonormal frame mapping canonical X onto the bolt; Y/Z are any stable perpendiculars.
        double fx = dx / length, fy = dy / length, fz = dz / length;
        double ux, uy, uz;
        if (Math.abs(fy) < 0.99D) {
            ux = fz; uy = 0.0D; uz = -fx;          // cross(forward, worldUp)
        } else {
            ux = 0.0D; uy = -fz; uz = fy;          // cross(forward, worldX)
        }
        double uLen = Math.sqrt(ux * ux + uy * uy + uz * uz);
        ux /= uLen; uy /= uLen; uz /= uLen;
        double wx = fy * uz - fz * uy, wy = fz * ux - fx * uz, wz = fx * uy - fy * ux;
        double stretch = length / shape.canonicalLength();

        for (LightningBoltGenerator.Strand strand : shape.strands()) {
            // The afterimage is a fading echo: forks add little to it, so only main strands are drawn.
            if (ghost && !strand.isMain()) {
                continue;
            }
            int count = strand.count();
            if (count < 2) {
                continue;
            }
            ensureScratch(count);
            double[] local = strand.points();
            for (int i = 0, n = count * 3; i < n; i += 3) {
                double cx = local[i] * stretch, cy = local[i + 1], cz = local[i + 2];
                worldPoints[i] = from.x + fx * cx + ux * cy + wx * cz;
                worldPoints[i + 1] = from.y + fy * cx + uy * cy + wy * cz;
                worldPoints[i + 2] = from.z + fz * cx + uz * cy + wz * cz;
            }
            computeSides(worldPoints, count, camera, sides, distances);
            float intensity = strand.intensity();
            boolean taper = strand.taper();
            if (ghost) {
                // Outer glow + bright core: the core is what makes the echo readable; the middle layer is skipped.
                drawRibbon(matrix, consumer, worldPoints, count, intensity, taper, widest, brightness);
                if (thinnest != widest) {
                    drawRibbon(matrix, consumer, worldPoints, count, intensity, taper, thinnest, brightness);
                }
                continue;
            }
            for (LightningStyle.Layer layer : style.layers()) {
                // Sub-pixel layers would all be widened to the same 1 px line and stacked; skip all but the widest.
                if (layer != widest && layer.halfWidth() < pixel) {
                    continue;
                }
                drawRibbon(matrix, consumer, worldPoints, count, intensity, taper, layer, brightness);
            }
        }
    }

    private static void ensureScratch(int count) {
        if (distances.length < count) {
            int size = Integer.highestOneBit(count - 1) << 1;
            worldPoints = new double[size * 3];
            sides = new float[size * 3];
            distances = new float[size];
        }
    }

    /**
     * Conservative bounding box (in the same space as {@code from}/{@code to}) that contains everything the bolt
     * can draw: the main path's worst-case sideways wander, forks, ribbon width and endpoint glow. Use it for frustum
     * culling, and return it from a block entity renderer's {@code getRenderBoundingBox} (offset to world space) so
     * arcs reaching outside their block aren't culled with it.
     */
    public static AABB renderBounds(Vec3 from, Vec3 to, LightningStyle style) {
        // Midpoint displacement wanders at most the sum of each generation's kick.
        double length = from.distanceTo(to);
        double wander = 0.0D;
        for (double seg = length; seg > style.segmentLength() * 0.5D; seg *= 0.5D) {
            wander += Math.min(seg * style.jaggedness(), style.maxOffset());
        }
        double branches = style.maxBranchDepth() > 0 && style.branchDensity() > 0.0F
                ? style.maxBranchLength() * 1.5D * (1.0D + style.jaggedness()) : 0.0D;
        double width = Math.max(widestLayer(style).halfWidth(), style.endpointGlow());
        return new AABB(from, to).inflate(wander + branches + width);
    }

    private static LightningStyle.Layer thinnestLayer(LightningStyle style) {
        LightningStyle.Layer thinnest = style.layers().getFirst();
        for (LightningStyle.Layer layer : style.layers()) {
            if (layer.halfWidth() < thinnest.halfWidth()) {
                thinnest = layer;
            }
        }
        return thinnest;
    }

    /** Ease-out fade (1 - phase^2): the echo holds most of its brightness, then drops off near the next strike. */
    private static float afterimageFade(float phase) {
        return 1.0F - phase * phase;
    }

    private static LightningStyle.Layer widestLayer(LightningStyle style) {
        LightningStyle.Layer widest = style.layers().getFirst();
        for (LightningStyle.Layer layer : style.layers()) {
            if (layer.halfWidth() > widest.halfWidth()) {
                widest = layer;
            }
        }
        return widest;
    }

    private static double distanceToSegment(Vector3f point, Vec3 a, Vec3 b) {
        double abx = b.x - a.x, aby = b.y - a.y, abz = b.z - a.z;
        double apx = point.x - a.x, apy = point.y - a.y, apz = point.z - a.z;
        double lenSq = abx * abx + aby * aby + abz * abz;
        double t = lenSq < 1.0E-9D ? 0.0D : Mth.clamp((apx * abx + apy * aby + apz * abz) / lenSq, 0.0D, 1.0D);
        double dx = apx - abx * t, dy = apy - aby * t, dz = apz - abz * t;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * For every point, the unit vector across the ribbon: perpendicular to both the local tangent (averaged over the
     * neighbouring segments, so joints share vertices with no gaps) and the direction to the camera.
     */
    private static void computeSides(double[] pts, int count, Vector3f camera, float[] sides, float[] distances) {
        for (int i = 0; i < count; i++) {
            int prev = Math.max(0, i - 1) * 3;
            int next = Math.min(count - 1, i + 1) * 3;
            int o = i * 3;
            double tx = pts[next] - pts[prev];
            double ty = pts[next + 1] - pts[prev + 1];
            double tz = pts[next + 2] - pts[prev + 2];
            double vx = camera.x - pts[o];
            double vy = camera.y - pts[o + 1];
            double vz = camera.z - pts[o + 2];
            distances[i] = (float) Math.sqrt(vx * vx + vy * vy + vz * vz);

            double sx = ty * vz - tz * vy;
            double sy = tz * vx - tx * vz;
            double sz = tx * vy - ty * vx;
            double len = Math.sqrt(sx * sx + sy * sy + sz * sz);
            if (len < 1.0E-9D) {
                // Looking straight down the bolt: any perpendicular to the tangent works.
                sx = -tz;
                sy = 0.0D;
                sz = tx;
                len = Math.sqrt(sx * sx + sz * sz);
                if (len < 1.0E-9D) {
                    sx = 1.0D;
                    len = 1.0D;
                }
            }
            sides[o] = (float) (sx / len);
            sides[o + 1] = (float) (sy / len);
            sides[o + 2] = (float) (sz / len);
        }
    }

    /** One layer along one strand: two quads per segment (edge→centre, centre→edge) so alpha fades across the width. */
    private static void drawRibbon(Matrix4f matrix, VertexConsumer consumer, double[] pts, int count,
            float intensity, boolean taper, LightningStyle.Layer layer, float brightness) {
        int rgb = layer.color() & 0x00FFFFFF;
        float layerAlpha = ((layer.color() >>> 24) & 0xFF) / 255.0F * brightness * intensity;
        float widthScale = (float) Math.sqrt(intensity);

        float prevWidth = 0.0F;
        int prevCenter = 0;
        int prevEdge = 0;
        for (int i = 0; i < count; i++) {
            float t = i / (float) (count - 1);
            float width = layer.halfWidth() * widthScale;
            float alpha = layerAlpha;
            if (taper) {
                width *= 1.0F - TAPER_WIDTH * t;
                alpha *= 1.0F - TAPER_ALPHA * t;
            }
            float minWidth = distances[i] * MIN_ANGULAR_HALF_WIDTH;
            if (width < minWidth) {
                // Keep it a pixel wide but dim it in proportion, so total light stays roughly constant.
                alpha *= width / minWidth;
                width = minWidth;
            }
            int centerColor = argb(alpha, rgb);
            int edgeColor = argb(alpha * layer.edgeAlpha(), rgb);

            if (i > 0) {
                int a = (i - 1) * 3;
                int b = i * 3;
                float ax = (float) pts[a], ay = (float) pts[a + 1], az = (float) pts[a + 2];
                float bx = (float) pts[b], by = (float) pts[b + 1], bz = (float) pts[b + 2];
                float asx = sides[a] * prevWidth, asy = sides[a + 1] * prevWidth, asz = sides[a + 2] * prevWidth;
                float bsx = sides[b] * width, bsy = sides[b + 1] * width, bsz = sides[b + 2] * width;

                // Left half: left edge -> centre.
                consumer.addVertex(matrix, ax - asx, ay - asy, az - asz).setColor(prevEdge);
                consumer.addVertex(matrix, ax, ay, az).setColor(prevCenter);
                consumer.addVertex(matrix, bx, by, bz).setColor(centerColor);
                consumer.addVertex(matrix, bx - bsx, by - bsy, bz - bsz).setColor(edgeColor);
                // Right half: centre -> right edge.
                consumer.addVertex(matrix, ax, ay, az).setColor(prevCenter);
                consumer.addVertex(matrix, ax + asx, ay + asy, az + asz).setColor(prevEdge);
                consumer.addVertex(matrix, bx + bsx, by + bsy, bz + bsz).setColor(edgeColor);
                consumer.addVertex(matrix, bx, by, bz).setColor(centerColor);
            }
            prevWidth = width;
            prevCenter = centerColor;
            prevEdge = edgeColor;
        }
    }

    /** Soft camera-facing glow at an endpoint: one disc per layer, scaled from the endpoint radius by layer width. */
    private static void drawGlowDiscs(Matrix4f matrix, VertexConsumer consumer, Vector3f camera, Vec3 center,
            LightningStyle style, float brightness) {
        if (brightness <= 0.004F) {
            return;
        }
        float vx = (float) (camera.x - center.x);
        float vy = (float) (camera.y - center.y);
        float vz = (float) (camera.z - center.z);
        float vLen = (float) Math.sqrt(vx * vx + vy * vy + vz * vz);
        if (vLen < 1.0E-4F) {
            return;
        }
        vx /= vLen;
        vy /= vLen;
        vz /= vLen;
        // Basis (u, w) spanning the plane facing the camera.
        float ux, uy, uz;
        if (Math.abs(vy) < 0.99F) {
            ux = vz;
            uy = 0.0F;
            uz = -vx;
        } else {
            ux = 1.0F;
            uy = 0.0F;
            uz = 0.0F;
        }
        float uLen = (float) Math.sqrt(ux * ux + uy * uy + uz * uz);
        ux /= uLen;
        uy /= uLen;
        uz /= uLen;
        float wx = vy * uz - vz * uy;
        float wy = vz * ux - vx * uz;
        float wz = vx * uy - vy * ux;

        float cx = (float) center.x, cy = (float) center.y, cz = (float) center.z;
        float widest = style.layers().getFirst().halfWidth();
        for (LightningStyle.Layer layer : style.layers()) {
            float radius = style.endpointGlow() * (widest > 0.0F ? layer.halfWidth() / widest : 1.0F);
            radius = Math.max(radius, layer.halfWidth() * 1.5F);
            float alpha = ((layer.color() >>> 24) & 0xFF) / 255.0F * brightness;
            int centerColor = argb(alpha, layer.color() & 0x00FFFFFF);
            int rimColor = argb(0.0F, layer.color() & 0x00FFFFFF);
            for (int s = 0; s < GLOW_DISC_SEGMENTS; s++) {
                double a0 = s * Math.PI * 2.0D / GLOW_DISC_SEGMENTS;
                double a1 = (s + 1) * Math.PI * 2.0D / GLOW_DISC_SEGMENTS;
                float c0 = (float) Math.cos(a0) * radius, s0 = (float) Math.sin(a0) * radius;
                float c1 = (float) Math.cos(a1) * radius, s1 = (float) Math.sin(a1) * radius;
                // Degenerate quad (centre twice) = a triangle fan wedge.
                consumer.addVertex(matrix, cx, cy, cz).setColor(centerColor);
                consumer.addVertex(matrix, cx + ux * c0 + wx * s0, cy + uy * c0 + wy * s0, cz + uz * c0 + wz * s0)
                        .setColor(rimColor);
                consumer.addVertex(matrix, cx + ux * c1 + wx * s1, cy + uy * c1 + wy * s1, cz + uz * c1 + wz * s1)
                        .setColor(rimColor);
                consumer.addVertex(matrix, cx, cy, cz).setColor(centerColor);
            }
        }
    }

    private static long strikeSeed(long seed, long bucket) {
        return seed ^ (bucket * 0x9E3779B97F4A7C15L);
    }

    private static float flickerFactor(long strikeSeed, float flicker) {
        if (flicker <= 0.0F) {
            return 1.0F;
        }
        // SplitMix64 finaliser: a well-mixed hash of the strike seed, without allocating a generator per frame.
        long z = strikeSeed ^ FLICKER_SALT;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        z ^= z >>> 31;
        float r = (z >>> 40) / (float) (1L << 24);
        return 1.0F - flicker * r * r;
    }

    private static int argb(float alpha, int rgb) {
        int a = Mth.clamp((int) (alpha * 255.0F + 0.5F), 0, 255);
        return (a << 24) | rgb;
    }
}
