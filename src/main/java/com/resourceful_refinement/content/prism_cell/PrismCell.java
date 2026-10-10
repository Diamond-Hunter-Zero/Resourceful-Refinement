package com.resourceful_refinement.content.prism_cell;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Mutable, in-world representation of an installed Prism Cell's charge state. Blocks and items that run a cell keep a
 * {@code PrismCell} member while a cell is installed (populated from a {@link PrismCellBatteryComponent} on insert,
 * and converted back to an item stack on removal).
 *
 * <p>Creative cells ({@link PrismCellTier#CREATIVE}) never drain or fill and always report full charge, mirroring
 * Create's creative motors/tanks.</p>
 */
public final class PrismCell {

    private final PrismCellTier tier;
    private final int maxCharge;
    private int charge;

    /** Set once a {@code destroyOnEmpty} drain empties a non-creative cell; the owner should then discard it. */
    private boolean depleted;

    public PrismCell(PrismCellTier tier, int maxCharge, int charge) {
        this.tier = tier;
        this.maxCharge = Math.max(0, maxCharge);
        this.charge = Mth.clamp(charge, 0, this.maxCharge);
    }

    // --- Construction / conversion --------------------------------------------------------------------------

    public static PrismCell fromComponent(PrismCellBatteryComponent component) {
        return new PrismCell(component.grade(), component.maxCharge(), component.charge());
    }

    public PrismCellBatteryComponent toComponent() {
        return new PrismCellBatteryComponent(tier.index(), maxCharge, currentCharge());
    }

    /** Rebuilds the matching Prism Cell item stack (with this cell's charge preserved) for giving back to a player. */
    public ItemStack toItemStack() {
        ItemStack stack = new ItemStack(PrismCellItem.byTier(tier));
        stack.set(com.resourceful_refinement.registry.ModDataComponents.PRISM_CELL_BATTERY.get(), toComponent());
        return stack;
    }

    // --- Charge operations ----------------------------------------------------------------------------------

    /**
     * Reduces the stored charge by up to {@code amount} and returns the amount actually drained. Creative cells never
     * drain (returns {@code amount}, treated as free power). If {@code destroyOnEmpty} is set and the drain empties a
     * non-creative cell, {@link #isDepleted()} flips true so the owner can discard the cell.
     */
    public int drainCellCharge(int amount, boolean destroyOnEmpty) {
        if (amount <= 0) {
            return 0;
        }
        if (tier.isCreative()) {
            return amount;
        }
        int drained = Math.min(amount, charge);
        charge -= drained;
        if (destroyOnEmpty && charge <= 0) {
            depleted = true;
        }
        return drained;
    }

    /** Adds up to {@code amount} charge, capped at the maximum, and returns the amount actually added (API hook). */
    public int fillCellCharge(int amount) {
        if (amount <= 0 || tier.isCreative()) {
            return 0;
        }
        int filled = Math.min(amount, maxCharge - charge);
        charge += filled;
        return filled;
    }

    /** True if this cell currently holds at least {@code amount} of usable charge (creative cells always do). */
    public boolean hasCharge(int amount) {
        return tier.isCreative() || charge >= amount;
    }

    // --- Accessors ------------------------------------------------------------------------------------------

    public PrismCellTier tier() {
        return tier;
    }

    public int maxCharge() {
        return maxCharge;
    }

    /** Current remaining charge; creative cells always report their maximum. */
    public int currentCharge() {
        return tier.isCreative() ? maxCharge : charge;
    }

    public boolean isEmpty() {
        return !tier.isCreative() && charge <= 0;
    }

    public boolean isDepleted() {
        return depleted;
    }

    // --- NBT persistence (for block entities that store a cell) --------------------------------------------

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Tier", tier.index());
        tag.putInt("MaxCharge", maxCharge);
        tag.putInt("Charge", charge);
        return tag;
    }

    @Nullable
    public static PrismCell load(@Nullable CompoundTag tag) {
        if (tag == null || !tag.contains("Tier")) {
            return null;
        }
        return new PrismCell(PrismCellTier.byIndex(tag.getInt("Tier")), tag.getInt("MaxCharge"), tag.getInt("Charge"));
    }
}
