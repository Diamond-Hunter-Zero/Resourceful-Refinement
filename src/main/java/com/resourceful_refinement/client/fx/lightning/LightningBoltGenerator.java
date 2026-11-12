package com.resourceful_refinement.client.fx.lightning;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

/**
 * Builds the polyline geometry of a lightning bolt by recursive midpoint displacement: start with the straight
 * segment between the endpoints, then repeatedly split every segment at its midpoint and kick that midpoint sideways
 * (perpendicular to the segment) by a random amount proportional to the segment's length. Each pass halves the
 * segment length and the kick, which gives the self-similar, angular zig-zag of real lightning. Endpoints are never
 * moved, so a bolt always lands exactly on its anchors.
 * <p>
 * Forks are grown from random points of a finished strand with the same algorithm, aimed roughly along the parent's
 * local direction, then recursed up to {@link LightningStyle#maxBranchDepth()}.
 * <p>
 * The output depends only on the endpoints, style and seed, so callers can regenerate every frame with the same seed
 * and get a stable shape, then change the seed to "re-strike".
 */
public final class LightningBoltGenerator {

    /** Hard cap on subdivision passes (2^8 = 256 segments per strand). */
    private static final int MAX_GENERATIONS = 8;
    private static final float MIN_BRANCH_LENGTH = 0.25F;
    /** Smallest sideways kick as a fraction of the maximum, so subdivisions never come out nearly straight. */
    private static final double MIN_KICK = 0.2D;
    private static final float BRANCH_INTENSITY = 0.5F;
    private static final float EXTRA_STRAND_INTENSITY = 0.4F;
    /** How far off the parent's heading a fork may aim (added random unit vector scale). */
    private static final double BRANCH_SPREAD = 0.7D;

    private LightningBoltGenerator() {}

    /**
     * One continuous polyline of the bolt.
     *
     * @param points    packed xyz, {@code count * 3} long
     * @param count     number of points
     * @param intensity brightness/width multiplier (1 for the main strand, lower for forks)
     * @param taper     whether width should shrink towards the last point (forks fade to a tip)
     */
    public record Strand(double[] points, int count, float intensity, boolean taper) {
        /** Main strands run endpoint to endpoint; forks are the tapered ones. */
        public boolean isMain() {
            return !taper;
        }
    }

    public static List<Strand> generate(double ax, double ay, double az, double bx, double by, double bz,
            LightningStyle style, long seed) {
        return generate(ax, ay, az, bx, by, bz, style, seed, style.segmentLength(), true);
    }

    /**
     * @param segmentLength finest segment length to subdivide to; pass more than {@link LightningStyle#segmentLength()}
     *                      for a coarser level of detail. Coarse levels consume the random stream first, so a coarser
     *                      bolt follows the same overall path as the detailed one, just with fewer small kinks
     * @param branches      whether to grow forks
     */
    public static List<Strand> generate(double ax, double ay, double az, double bx, double by, double bz,
            LightningStyle style, long seed, float segmentLength, boolean branches) {
        SplittableRandom random = new SplittableRandom(seed);
        float segLen = Math.max(style.segmentLength(), segmentLength);
        List<Strand> out = new ArrayList<>();
        for (int s = 0; s < style.strands(); s++) {
            float intensity = s == 0 ? 1.0F : EXTRA_STRAND_INTENSITY;
            Strand main = subdivide(ax, ay, az, bx, by, bz, style, segLen, style.maxOffset(), intensity, false, random);
            out.add(main);
            if (branches) {
                growBranches(main, style, segLen, style.branchDensity(), 1, random, out);
            }
        }
        return out;
    }

    private static Strand subdivide(double ax, double ay, double az, double bx, double by, double bz,
            LightningStyle style, float segmentLength, float maxOffset, float intensity, boolean taper,
            SplittableRandom random) {
        double length = Math.sqrt(sq(bx - ax) + sq(by - ay) + sq(bz - az));
        int generations = 0;
        if (length > segmentLength) {
            generations = (int) Math.ceil(Math.log(length / segmentLength) / Math.log(2.0D));
            generations = Math.min(generations, MAX_GENERATIONS);
        }

        // Subdivide in place: allocate the final (2^g + 1)-point array once, pin the endpoints, then fill midpoints
        // coarse-to-fine by halving the stride. Within each pass segments are visited left to right, so the random
        // stream is consumed in the same order as a pass-by-pass rebuild would.
        int count = (1 << generations) + 1;
        double[] pts = new double[count * 3];
        pts[0] = ax;
        pts[1] = ay;
        pts[2] = az;
        int end = (count - 1) * 3;
        pts[end] = bx;
        pts[end + 1] = by;
        pts[end + 2] = bz;
        double segLength = length;
        double[] perp = new double[3];

        for (int stride = count - 1; stride > 1; stride >>= 1) {
            int half = stride >> 1;
            double kick = Math.min(segLength * style.jaggedness(), maxOffset);
            for (int i = 0; i < count - 1; i += stride) {
                int p = i * 3;
                int q = (i + stride) * 3;
                int m = (i + half) * 3;
                double dx = pts[q] - pts[p];
                double dy = pts[q + 1] - pts[p + 1];
                double dz = pts[q + 2] - pts[p + 2];
                randomPerpendicular(dx, dy, dz, random, perp);
                // Biased away from zero so nearly every joint is a visible kink rather than a straight run.
                double amount = (MIN_KICK + (1.0D - MIN_KICK) * random.nextDouble()) * kick
                        * (random.nextBoolean() ? 1.0D : -1.0D);
                pts[m] = (pts[p] + pts[q]) * 0.5D + perp[0] * amount;
                pts[m + 1] = (pts[p + 1] + pts[q + 1]) * 0.5D + perp[1] * amount;
                pts[m + 2] = (pts[p + 2] + pts[q + 2]) * 0.5D + perp[2] * amount;
            }
            segLength *= 0.5D;
        }
        return new Strand(pts, count, intensity, taper);
    }

    private static void growBranches(Strand parent, LightningStyle style, float segmentLength, float density, int depth,
            SplittableRandom random, List<Strand> out) {
        if (depth > style.maxBranchDepth() || density <= 0.0F || parent.count() < 3) {
            return;
        }
        double[] pts = parent.points();
        int count = parent.count();

        // Cumulative arc length so a fork knows how much bolt remains past its root.
        double[] arc = new double[count];
        for (int i = 1; i < count; i++) {
            int p = (i - 1) * 3;
            int q = i * 3;
            arc[i] = arc[i - 1] + Math.sqrt(sq(pts[q] - pts[p]) + sq(pts[q + 1] - pts[p + 1]) + sq(pts[q + 2] - pts[p + 2]));
        }
        double total = arc[count - 1];
        if (total <= 1.0E-4D) {
            return;
        }
        double perPoint = density * (total / (count - 1));
        double[] dir = new double[3];

        // Skip the first and last few points so forks don't sprout from the anchors themselves.
        int margin = Math.max(1, count / 16);
        for (int i = margin; i < count - margin; i++) {
            if (random.nextDouble() >= perPoint) {
                continue;
            }
            double remaining = total - arc[i];
            double length = Math.min(remaining * style.branchLength() * (0.5D + random.nextDouble()),
                    style.maxBranchLength() / depth);
            if (length < MIN_BRANCH_LENGTH) {
                continue;
            }

            // Heading: parent's local direction a few points ahead, bent by a random unit vector.
            int o = i * 3;
            int ahead = Math.min(count - 1, i + Math.max(2, count / 12)) * 3;
            double hx = pts[ahead] - pts[o];
            double hy = pts[ahead + 1] - pts[o + 1];
            double hz = pts[ahead + 2] - pts[o + 2];
            double hLen = Math.sqrt(hx * hx + hy * hy + hz * hz);
            if (hLen < 1.0E-6D) {
                continue;
            }
            randomUnit(random, dir);
            hx = hx / hLen + dir[0] * BRANCH_SPREAD;
            hy = hy / hLen + dir[1] * BRANCH_SPREAD;
            hz = hz / hLen + dir[2] * BRANCH_SPREAD;
            hLen = Math.sqrt(hx * hx + hy * hy + hz * hz);
            if (hLen < 1.0E-6D) {
                continue;
            }
            double scale = length / hLen;

            float intensity = parent.intensity() * BRANCH_INTENSITY;
            Strand branch = subdivide(pts[o], pts[o + 1], pts[o + 2],
                    pts[o] + hx * scale, pts[o + 1] + hy * scale, pts[o + 2] + hz * scale,
                    style, segmentLength, style.maxOffset() * 0.5F, intensity, true, random);
            out.add(branch);
            growBranches(branch, style, segmentLength, density * 0.6F, depth + 1, random, out);
        }
    }

    /** Writes a random unit vector perpendicular to (dx, dy, dz) into {@code out}. */
    private static void randomPerpendicular(double dx, double dy, double dz, SplittableRandom random, double[] out) {
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1.0E-9D) {
            randomUnit(random, out);
            return;
        }
        dx /= len;
        dy /= len;
        dz /= len;
        for (int attempt = 0; attempt < 4; attempt++) {
            randomUnit(random, out);
            double dot = out[0] * dx + out[1] * dy + out[2] * dz;
            double px = out[0] - dx * dot;
            double py = out[1] - dy * dot;
            double pz = out[2] - dz * dot;
            double pLen = Math.sqrt(px * px + py * py + pz * pz);
            if (pLen > 1.0E-3D) {
                out[0] = px / pLen;
                out[1] = py / pLen;
                out[2] = pz / pLen;
                return;
            }
        }
        // Degenerate (random vector parallel to the segment every time): any fixed perpendicular will do.
        double px = Math.abs(dy) < 0.9D ? -dz : 0.0D;
        double py = Math.abs(dy) < 0.9D ? 0.0D : dz;
        double pz = Math.abs(dy) < 0.9D ? dx : -dy;
        double pLen = Math.max(1.0E-9D, Math.sqrt(px * px + py * py + pz * pz));
        out[0] = px / pLen;
        out[1] = py / pLen;
        out[2] = pz / pLen;
    }

    private static void randomUnit(SplittableRandom random, double[] out) {
        double z = random.nextDouble() * 2.0D - 1.0D;
        double theta = random.nextDouble() * Math.PI * 2.0D;
        double r = Math.sqrt(1.0D - z * z);
        out[0] = r * Math.cos(theta);
        out[1] = r * Math.sin(theta);
        out[2] = z;
    }

    private static double sq(double v) {
        return v * v;
    }
}
