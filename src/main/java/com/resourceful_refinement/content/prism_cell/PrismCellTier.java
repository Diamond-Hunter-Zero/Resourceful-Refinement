package com.resourceful_refinement.content.prism_cell;

/**
 * The four Prism Cell grades. Each grade is a distinct item id with its own texture and a preset
 * {@link PrismCellBatteryComponent} (its maximum charge), but all share the same JSON geometry model.
 *
 * <p>The {@link #index} is the value stored in the data component — {@code 0} for the Creative cell and {@code 1-3}
 * for the drainable grades — matching the design doc's tier table. Append new grades to the end so existing indices
 * stay stable.</p>
 */
public enum PrismCellTier {
    /** Creative-only cell: always sits at maximum charge and never drains or fills (like Create's creative sources). */
    CREATIVE(0, 256_000, "creative"),
    LOW(1, 4_000, "low"),
    MID(2, 16_000, "mid"),
    HIGH(3, 64_000, "high");

    private static final PrismCellTier[] BY_INDEX = buildIndex();

    private final int index;
    private final int maxCharge;
    private final String id;

    PrismCellTier(int index, int maxCharge, String id) {
        this.index = index;
        this.maxCharge = maxCharge;
        this.id = id;
    }

    /** Tier index stored in the data component (0 = Creative, 1-3 = drainable grades). */
    public int index() {
        return index;
    }

    /** Preset maximum charge for a freshly created cell of this grade. */
    public int maxCharge() {
        return maxCharge;
    }

    public String id() {
        return id;
    }

    public boolean isCreative() {
        return this == CREATIVE;
    }

    /** Resolves a tier by its stored index, falling back to {@link #LOW} for unknown values (defensive load guard). */
    public static PrismCellTier byIndex(int index) {
        if (index >= 0 && index < BY_INDEX.length && BY_INDEX[index] != null) {
            return BY_INDEX[index];
        }
        return LOW;
    }

    private static PrismCellTier[] buildIndex() {
        int max = 0;
        for (PrismCellTier tier : values()) {
            max = Math.max(max, tier.index);
        }
        PrismCellTier[] lookup = new PrismCellTier[max + 1];
        for (PrismCellTier tier : values()) {
            lookup[tier.index] = tier;
        }
        return lookup;
    }
}
