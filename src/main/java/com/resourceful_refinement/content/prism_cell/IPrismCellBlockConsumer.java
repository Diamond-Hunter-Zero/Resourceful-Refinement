package com.resourceful_refinement.content.prism_cell;

import com.resourceful_refinement.registry.ModDataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Implemented by block entities that run on a Prism Cell. The block delegates its interaction handling to the
 * {@link #tryInsertCell} / {@link #tryExtractCell} defaults here, which realise the exchangeable-battery contract
 * from the design doc:
 *
 * <ul>
 *   <li>Right-clicking an empty consumer while holding a cell installs that cell (and consumes one from the stack).</li>
 *   <li>Crouch-right-clicking a loaded consumer empty-handed removes the cell back into the player's inventory.</li>
 * </ul>
 *
 * <p>The implementor owns the actual {@link PrismCell} storage (typically a persisted block-entity field) and its
 * charge consumption; these defaults only handle the insert/remove exchange.</p>
 */
public interface IPrismCellBlockConsumer {

    @Nullable
    PrismCell getPrismCell();

    void setPrismCell(@Nullable PrismCell cell);

    /** Marks the owning block entity dirty after a cell exchange so the change is saved. */
    void onPrismCellChanged();

    default boolean hasPrismCell() {
        return getPrismCell() != null;
    }

    /**
     * Attempts to install the cell the player is holding into this (currently empty) consumer. On success the stack is
     * reduced by one (unless the player is in creative) and the cell data is copied into storage. Returns {@code false}
     * if there is already a cell installed or the held stack is not a Prism Cell.
     */
    default boolean tryInsertCell(Player player, ItemStack held) {
        if (hasPrismCell()) {
            return false;
        }
        PrismCellBatteryComponent battery = held.get(ModDataComponents.PRISM_CELL_BATTERY.get());
        if (battery == null) {
            return false;
        }
        setPrismCell(PrismCell.fromComponent(battery));
        if (!player.isCreative()) {
            held.shrink(1);
        }
        onPrismCellChanged();
        return true;
    }

    /**
     * Removes the installed cell (if any) and returns it to the player as a Prism Cell item, dropping it in-world if
     * their inventory is full. Returns {@code false} if no cell is installed.
     */
    default boolean tryExtractCell(Player player) {
        PrismCell cell = getPrismCell();
        if (cell == null) {
            return false;
        }
        setPrismCell(null);
        onPrismCellChanged();
        ItemStack stack = cell.toItemStack();
        if (!player.addItem(stack)) {
            player.drop(stack, false);
        }
        return true;
    }
}
