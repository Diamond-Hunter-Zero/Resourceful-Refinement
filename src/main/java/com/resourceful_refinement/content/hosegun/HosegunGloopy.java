package com.resourceful_refinement.content.hosegun;

import com.resourceful_refinement.registry.ModDataComponents;
import com.resourceful_refinement.registry.ModItems;
import net.minecraft.world.item.ItemStack;

/** Gloopy variant flag on a {@link HosegunItem} stack (applied via glue pot, removed in a mixing basin). */
public final class HosegunGloopy {

    private HosegunGloopy() {}

    public static boolean isGloopy(ItemStack stack) {
        return stack.is(ModItems.HOSEGUN.get())
                && Boolean.TRUE.equals(stack.get(ModDataComponents.HOSEGUN_GLOOPY.get()));
    }

    public static void setGloopy(ItemStack stack) {
        if (stack.is(ModItems.HOSEGUN.get())) {
            stack.set(ModDataComponents.HOSEGUN_GLOOPY.get(), true);
        }
    }

    public static void clearGloopy(ItemStack stack) {
        stack.remove(ModDataComponents.HOSEGUN_GLOOPY.get());
    }
}
