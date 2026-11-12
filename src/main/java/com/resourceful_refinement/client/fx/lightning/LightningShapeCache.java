package com.resourceful_refinement.client.fx.lightning;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Small LRU cache of bolt shapes, so a shape is generated once per re-strike instead of once per frame.
 * <p>
 * Shapes are stored in a <b>canonical frame</b>: starting at the origin and running along +X for a quantised length.
 * The renderer rotates and stretches them onto the real endpoints each frame, so a beam whose endpoints move (a held
 * weapon, a swinging part) still hits the cache. Two quantisations keep keys stable while things move:
 * <ul>
 *   <li>length in ~2% steps; the renderer stretches the remaining &le;1% along the axis, which is invisible;</li>
 *   <li>level-of-detail segment length in powers of two of the style's own, which is also the only granularity
 *       at which subdivision actually changes.</li>
 * </ul>
 * Render-thread only.
 */
final class LightningShapeCache {

    private static final int MAX_ENTRIES = 128;
    private static final double LENGTH_STEP = Math.log(1.02D);
    private static final double LOG_2 = Math.log(2.0D);
    private static final double MIN_LENGTH = 1.0E-3D;

    private static final Map<Key, List<LightningBoltGenerator.Strand>> CACHE =
            new LinkedHashMap<>(MAX_ENTRIES, 0.75F, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<Key, List<LightningBoltGenerator.Strand>> eldest) {
                    return size() > MAX_ENTRIES;
                }
            };

    /** Styles compared by identity: presets are constants, and record equality would hash every field per lookup. */
    private record Key(long seed, LightningStyle style, int lengthBucket, int lodLevel) {
        @Override
        public boolean equals(Object o) {
            return o instanceof Key k && k.seed == seed && k.style == style
                    && k.lengthBucket == lengthBucket && k.lodLevel == lodLevel;
        }

        @Override
        public int hashCode() {
            int h = Long.hashCode(seed);
            h = h * 31 + System.identityHashCode(style);
            h = h * 31 + lengthBucket;
            return h * 31 + lodLevel;
        }
    }

    /** Canonical shape plus the length it was generated at, so the caller can stretch it to the real length. */
    record Shape(List<LightningBoltGenerator.Strand> strands, double canonicalLength) {}

    private LightningShapeCache() {}

    /**
     * @param length        real distance between the endpoints
     * @param segmentLength requested finest segment length (level of detail); at least the style's own
     */
    static Shape get(long seed, LightningStyle style, double length, float segmentLength) {
        length = Math.max(length, MIN_LENGTH);
        int lengthBucket = (int) Math.round(Math.log(length) / LENGTH_STEP);
        double canonicalLength = Math.exp(lengthBucket * LENGTH_STEP);

        double lodRatio = segmentLength / style.segmentLength();
        int lodLevel = lodRatio <= 1.0D ? 0 : (int) Math.round(Math.log(lodRatio) / LOG_2);
        float lodSegment = style.segmentLength() * (1 << Math.min(lodLevel, 16));

        Key key = new Key(seed, style, lengthBucket, lodLevel);
        List<LightningBoltGenerator.Strand> strands = CACHE.get(key);
        if (strands == null) {
            strands = LightningBoltGenerator.generate(0.0D, 0.0D, 0.0D, canonicalLength, 0.0D, 0.0D,
                    style, seed, lodSegment, true);
            CACHE.put(key, strands);
        }
        return new Shape(strands, canonicalLength);
    }

    static void clear() {
        CACHE.clear();
    }
}
