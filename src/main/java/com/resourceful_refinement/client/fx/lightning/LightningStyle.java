package com.resourceful_refinement.client.fx.lightning;

import java.util.ArrayList;
import java.util.List;

/**
 * Immutable look-and-behaviour description for a procedural lightning bolt. Shape parameters feed
 * {@link LightningBoltGenerator}; layer and timing parameters feed {@link LightningRenderer}.
 *
 * @param layers           glow layers drawn back to front, typically wide-and-dim to thin-and-white
 * @param segmentLength    target length (blocks) of the finest zig-zag segment; smaller = more detail
 * @param jaggedness       sideways kick of each subdivision as a fraction of the segment length (0.2 calm .. 0.6 wild)
 * @param maxOffset        cap (blocks) on any single sideways kick, so long beams stay beam-like rather than wandering
 * @param branchDensity    expected forks per block of main bolt
 * @param branchLength     fork length as a fraction of the bolt length still remaining past the fork point
 * @param maxBranchLength  hard cap (blocks) on fork length
 * @param maxBranchDepth   fork recursion depth (0 = no forks, 1 = forks, 2 = forks of forks)
 * @param strands          number of independent main strands between the endpoints (extra strands are dimmer)
 * @param restrikeInterval ticks between re-randomising the shape; fractional values are fine (0.75 = ~27 Hz)
 * @param flicker          random per-restrike brightness dip (0 = steady, 1 = can blink fully out)
 * @param afterimage       brightness of the previous shape, faded out over one interval (0 = off)
 * @param endpointGlow     radius (blocks) of the soft glow disc drawn at each endpoint (0 = off)
 */
public record LightningStyle(
        List<Layer> layers,
        float segmentLength,
        float jaggedness,
        float maxOffset,
        float branchDensity,
        float branchLength,
        float maxBranchLength,
        int maxBranchDepth,
        int strands,
        float restrikeInterval,
        float flicker,
        float afterimage,
        float endpointGlow) {

    /**
     * One camera-facing ribbon pass along every strand.
     *
     * @param halfWidth  ribbon half-width in blocks at full intensity
     * @param color      0xAARRGGBB; alpha is the layer's peak opacity along the centre line
     * @param edgeAlpha  opacity at the ribbon edge relative to the centre (0 = soft glow falloff, 1 = hard-edged)
     */
    public record Layer(float halfWidth, int color, float edgeAlpha) {}

    /** Bright white-hot core with a cyan/blue halo. Suits weapon beams and long arcs. */
    public static final LightningStyle ELECTRIC = builder()
            .layer(0.22F, 0x3C2A6CFF, 0.0F)
            .layer(0.075F, 0x9A5CD8FF, 0.0F)
            .layer(0.022F, 0xFFF4FCFF, 0.55F)
            .build();

    /** Short, twitchy, thin arc for block-entity crackle between nearby points. */
    public static final LightningStyle ARC = builder()
            .layer(0.12F, 0x403C8CFF, 0.0F)
            .layer(0.04F, 0xA070E0FF, 0.0F)
            .layer(0.014F, 0xFFF4FCFF, 0.6F)
            .segmentLength(0.12F)
            .jaggedness(0.5F)
            .maxOffset(0.35F)
            .branchDensity(0.8F)
            .branchLength(0.45F)
            .maxBranchLength(0.8F)
            .maxBranchDepth(1)
            .restrikeInterval(0.75F)
            .flicker(0.5F)
            .endpointGlow(0.12F)
            .build();

    /** Shorter cyan/blue beam, for use with Railgun. */
    public static final LightningStyle RAILGUN_ARC = builder()
            .layer(0.12F, 0x3C2A6CFF, 0.0F)
            .layer(0.04F, 0x9A5CD8FF, 0.0F)
            .layer(0.014F, 0xFFF4FCFF, 0.55F)
            .segmentLength(0.3F)
            .jaggedness(0.25F)
            .maxOffset(0.25F)
            .branchDensity(0.65F)
            .branchLength(0.35F)
            .maxBranchLength(0.6F)
            .maxBranchDepth(1)
            .restrikeInterval(1F)
            .flicker(0.3F)
            .endpointGlow(0.1F)
            .build();

    /** Short, twitchy, yellow arc for Tesla recipe processing. */
    public static final LightningStyle PROCESSING_ARC = builder()
            .layer(0.12F, 0x3Cffcb5f, 0.0F)
            .layer(0.04F, 0x9Affe873, 0.0F)
            .layer(0.014F, 0xFFf8ffc0, 0.6F)
            .segmentLength(0.12F)
            .jaggedness(0.5F)
            .maxOffset(0.35F)
            .branchDensity(0.8F)
            .branchLength(0.45F)
            .maxBranchLength(0.8F)
            .maxBranchDepth(1)
            .restrikeInterval(0.75F)
            .flicker(0.5F)
            .endpointGlow(0.12F)
            .build();

    /** Calm, low-segment green beam: few gentle kicks, for simple joint/link visuals. */
    public static final LightningStyle JOINT = builder()
            .layer(0.12F, 0x403BD66B, 0.0F)
            .layer(0.04F, 0xA09BE066, 0.0F)
            .layer(0.014F, 0xFFE7FF9F, 0.6F)
            .segmentLength(0.4F)
            .jaggedness(0.2F)
            .maxOffset(0.2F)
            .branchDensity(0.6F)
            .branchLength(0.3F)
            .maxBranchLength(0.6F)
            .maxBranchDepth(1)
            .restrikeInterval(1.25F)
            .flicker(0.25F)
            .endpointGlow(0.1F)
            .build();

    /** Hostile red-violet variant of {@link #ELECTRIC}. */
    public static final LightningStyle CRIMSON = ELECTRIC.toBuilder()
            .clearLayers()
            .layer(0.22F, 0x3CFF2A5A, 0.0F)
            .layer(0.075F, 0x9AFF5C9A, 0.0F)
            .layer(0.022F, 0xFFFFF0F6, 0.55F)
            .build();

    public LightningStyle {
        layers = List.copyOf(layers);
        if (layers.isEmpty()) {
            throw new IllegalArgumentException("LightningStyle needs at least one layer");
        }
        segmentLength = Math.max(0.02F, segmentLength);
        strands = Math.max(1, strands);
        maxBranchDepth = Math.max(0, maxBranchDepth);
        restrikeInterval = Math.max(0.05F, restrikeInterval);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        Builder b = new Builder();
        b.layers.addAll(layers);
        b.segmentLength = segmentLength;
        b.jaggedness = jaggedness;
        b.maxOffset = maxOffset;
        b.branchDensity = branchDensity;
        b.branchLength = branchLength;
        b.maxBranchLength = maxBranchLength;
        b.maxBranchDepth = maxBranchDepth;
        b.strands = strands;
        b.restrikeInterval = restrikeInterval;
        b.flicker = flicker;
        b.afterimage = afterimage;
        b.endpointGlow = endpointGlow;
        return b;
    }

    public static final class Builder {
        private final List<Layer> layers = new ArrayList<>();
        private float segmentLength = 0.16F;
        private float jaggedness = 0.42F;
        private float maxOffset = 0.55F;
        private float branchDensity = 0.3F;
        private float branchLength = 0.22F;
        private float maxBranchLength = 2.5F;
        private int maxBranchDepth = 2;
        private int strands = 1;
        private float restrikeInterval = 1.5F;
        private float flicker = 0.35F;
        private float afterimage = 0.4F;
        private float endpointGlow = 0.3F;

        private Builder() {}

        public Builder layer(float halfWidth, int color, float edgeAlpha) {
            layers.add(new Layer(halfWidth, color, edgeAlpha));
            return this;
        }

        public Builder clearLayers() {
            layers.clear();
            return this;
        }

        public Builder segmentLength(float v) { segmentLength = v; return this; }
        public Builder jaggedness(float v) { jaggedness = v; return this; }
        public Builder maxOffset(float v) { maxOffset = v; return this; }
        public Builder branchDensity(float v) { branchDensity = v; return this; }
        public Builder branchLength(float v) { branchLength = v; return this; }
        public Builder maxBranchLength(float v) { maxBranchLength = v; return this; }
        public Builder maxBranchDepth(int v) { maxBranchDepth = v; return this; }
        public Builder strands(int v) { strands = v; return this; }
        public Builder restrikeInterval(float v) { restrikeInterval = v; return this; }
        public Builder flicker(float v) { flicker = v; return this; }
        public Builder afterimage(float v) { afterimage = v; return this; }
        public Builder endpointGlow(float v) { endpointGlow = v; return this; }

        public LightningStyle build() {
            return new LightningStyle(layers, segmentLength, jaggedness, maxOffset, branchDensity, branchLength,
                    maxBranchLength, maxBranchDepth, strands, restrikeInterval, flicker, afterimage, endpointGlow);
        }
    }
}
