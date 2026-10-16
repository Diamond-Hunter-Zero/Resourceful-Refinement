package com.resourceful_refinement.content.prism_cell;

import com.resourceful_refinement.registry.ModDataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Implemented by items that run on a Prism Cell. Unlike {@link IPrismCellBlockConsumer} (which stores its cell in a
 * block-entity field), an item is a shared singleton, so the installed cell lives directly on the consumer
 * <em>stack</em> as the {@code prism_cell_battery} data component ({@link ModDataComponents#PRISM_CELL_BATTERY}) —
 * present means a cell is installed, absent means empty.
 *
 * <p>The insert/remove exchange is bundle-style (realised through {@code Item#overrideOtherStackedOnMe} /
 * {@code Item#overrideStackedOnOther}), matching the "Item Bundle behaviour" in the design doc:</p>
 * <ul>
 *   <li>Right-clicking a Prism Cell (on the cursor) onto an empty consumer in an inventory installs that cell.</li>
 *   <li>Right-clicking a loaded consumer (on the cursor) onto an empty slot removes the cell into that slot.</li>
 * </ul>
 *
 * <p>The implementor still owns its actual charge consumption; these defaults only handle the exchange.</p>
 */
public interface IPrismCellItemConsumer {

    /** True if this consumer stack currently has a cell installed. */
    default boolean hasPrismCell(ItemStack self) {
        return self.has(ModDataComponents.PRISM_CELL_BATTERY.get());
    }

    /** The installed cell's serialised component, or {@code null} if empty. */
    @Nullable
    default PrismCellBatteryComponent getCellComponent(ItemStack self) {
        return self.get(ModDataComponents.PRISM_CELL_BATTERY.get());
    }

    /** The installed cell as a mutable {@link PrismCell}, or {@code null} if empty. */
    @Nullable
    default PrismCell getPrismCell(ItemStack self) {
        PrismCellBatteryComponent component = getCellComponent(self);
        return component == null ? null : PrismCell.fromComponent(component);
    }

    /** Stores (or, with {@code null}, clears) the installed cell on this consumer stack. */
    default void setPrismCell(ItemStack self, @Nullable PrismCell cell) {
        if (cell == null) {
            self.remove(ModDataComponents.PRISM_CELL_BATTERY.get());
        } else {
            self.set(ModDataComponents.PRISM_CELL_BATTERY.get(), cell.toComponent());
        }
    }

    /**
     * Handles a cursor stack being right-clicked onto this consumer while it sits in a slot. If the consumer is empty
     * and the cursor holds a Prism Cell, the cell is installed (one consumed from the cursor). Returns {@code true} if
     * the interaction was consumed.
     */
    default boolean handleOtherStackedOnMe(ItemStack self, ItemStack cursor, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY || hasPrismCell(self) || cursor.isEmpty()) {
            return false;
        }
        PrismCellBatteryComponent battery = cursor.get(ModDataComponents.PRISM_CELL_BATTERY.get());
        if (battery == null) {
            return false;
        }
        self.set(ModDataComponents.PRISM_CELL_BATTERY.get(), battery);
        if (!player.isCreative()) {
            cursor.shrink(1);
        }
        return true;
    }

    /**
     * Handles this consumer (on the cursor) being right-clicked onto another slot. If the consumer has a cell installed
     * and the target slot is empty and accepts the cell item, the cell is removed into that slot. Returns {@code true}
     * if the interaction was consumed.
     */
    default boolean handleStackedOnOther(ItemStack self, Slot target, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY || !hasPrismCell(self) || !target.getItem().isEmpty()) {
            return false;
        }
        PrismCell cell = getPrismCell(self);
        if (cell == null) {
            return false;
        }
        ItemStack cellStack = cell.toItemStack();
        if (!target.mayPlace(cellStack)) {
            return false;
        }
        target.set(cellStack);
        setPrismCell(self, null);
        return true;
    }
}
