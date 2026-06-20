package com.resourceful_refinement.content.glare;

public enum GlareComparison {
    GREATER_THAN_OR_EQUAL("≥") {
        @Override public boolean test(int actual, int threshold) { return actual >= threshold; }
    },
    LESS_THAN_OR_EQUAL("≤") {
        @Override public boolean test(int actual, int threshold) { return actual <= threshold; }
    },
    GREATER_THAN(">") {
        @Override public boolean test(int actual, int threshold) { return actual > threshold; }
    },
    LESS_THAN("<") {
        @Override public boolean test(int actual, int threshold) { return actual < threshold; }
    },
    EQUAL("=") {
        @Override public boolean test(int actual, int threshold) { return actual == threshold; }
    },
    NOT_EQUAL("≠") {
        @Override public boolean test(int actual, int threshold) { return actual != threshold; }
    };

    private final String symbol;

    GlareComparison(String symbol) {
        this.symbol = symbol;
    }

    public String symbol() {
        return symbol;
    }

    public abstract boolean test(int actual, int threshold);

    public static GlareComparison byOrdinal(int ordinal) {
        GlareComparison[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : GREATER_THAN_OR_EQUAL;
    }
}
