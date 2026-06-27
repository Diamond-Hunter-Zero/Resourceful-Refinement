package com.resourceful_refinement.content.glare;

import java.util.Objects;

public record GlareLink(DimensionalNodePos a, DimensionalNodePos b) {
    public GlareLink {
        if (a.equals(b)) {
            throw new IllegalArgumentException("GLARE links require two distinct nodes");
        }
        int order = compare(a, b);
        if (order > 0) {
            DimensionalNodePos swap = a;
            a = b;
            b = swap;
        }
    }

    public boolean contains(DimensionalNodePos pos) {
        return a.equals(pos) || b.equals(pos);
    }

    public DimensionalNodePos other(DimensionalNodePos pos) {
        if (a.equals(pos)) {
            return b;
        }
        if (b.equals(pos)) {
            return a;
        }
        throw new IllegalArgumentException("Position is not part of this link");
    }

    private static int compare(DimensionalNodePos left, DimensionalNodePos right) {
        int level = left.levelKey().location().compareTo(right.levelKey().location());
        if (level != 0) {
            return level;
        }
        int x = Integer.compare(left.pos().getX(), right.pos().getX());
        if (x != 0) {
            return x;
        }
        int y = Integer.compare(left.pos().getY(), right.pos().getY());
        if (y != 0) {
            return y;
        }
        return Integer.compare(left.pos().getZ(), right.pos().getZ());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof GlareLink other && a.equals(other.a) && b.equals(other.b);
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b);
    }
}
