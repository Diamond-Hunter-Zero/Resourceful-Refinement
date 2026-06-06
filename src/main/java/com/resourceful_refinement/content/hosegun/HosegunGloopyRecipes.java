package com.resourceful_refinement.content.hosegun;

import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.registry.ModDataComponents;
import com.resourceful_refinement.registry.ModItems;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;

/** Datapack mixing recipe {@link #UNGLOOP_HOSEGUN} converts a gloopy hosegun back to a normal one. */
public final class HosegunGloopyRecipes {

    public static final ResourceLocation UNGLOOP_HOSEGUN = ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "mixing/ungloop_hosegun");

    private static final ThreadLocal<ItemStack> CAPTURED_INPUT = new ThreadLocal<>();

    private HosegunGloopyRecipes() {}

    public static boolean isUngloopRecipe(Recipe<?> recipe, Level level) {
        return level.getRecipeManager()
                .byKey(UNGLOOP_HOSEGUN)
                .map(holder -> holder.value() == recipe)
                .orElse(false);
    }

    public static void captureGloopyInput(BasinBlockEntity basin) {
        ItemStack captured = findGloopyHosegun(basin);
        if (!captured.isEmpty()) {
            CAPTURED_INPUT.set(captured.copy());
        }
    }

    public static void finishUngloop(BasinBlockEntity basin) {
        try {
            ItemStack captured = CAPTURED_INPUT.get();
            restoreHosegunState(basin, captured);
        } finally {
            CAPTURED_INPUT.remove();
        }
    }

    @Nullable
    private static ItemStack findGloopyHosegun(BasinBlockEntity basin) {
        Level level = basin.getLevel();
        if (level == null) {
            return null;
        }

        IItemHandler items = level.getCapability(Capabilities.ItemHandler.BLOCK, basin.getBlockPos(), null);
        if (items == null) {
            return null;
        }

        for (int slot = 0; slot < items.getSlots(); slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (HosegunGloopy.isGloopy(stack)) {
                return stack;
            }
        }
        return null;
    }

    private static void restoreHosegunState(BasinBlockEntity basin, @Nullable ItemStack captured) {
        Level level = basin.getLevel();
        if (level == null) {
            return;
        }

        IItemHandler items = level.getCapability(Capabilities.ItemHandler.BLOCK, basin.getBlockPos(), null);
        if (items == null) {
            return;
        }

        for (int slot = 0; slot < items.getSlots(); slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (!stack.is(ModItems.HOSEGUN.get())) {
                continue;
            }

            HosegunGloopy.clearGloopy(stack);

            if (captured == null || captured.isEmpty()) {
                continue;
            }

            var fluidContent = captured.get(ModDataComponents.HOSEGUN_FLUID.get());
            if (fluidContent != null) {
                stack.set(ModDataComponents.HOSEGUN_FLUID.get(), fluidContent);
            } else {
                stack.remove(ModDataComponents.HOSEGUN_FLUID.get());
            }

            String trackingId = captured.get(ModDataComponents.HOSEGUN_TRACKING_ID.get());
            if (trackingId != null && !trackingId.isEmpty()) {
                HosegunTracking.setTrackingId(stack, trackingId);
            } else {
                HosegunTracking.clearTrackingId(stack);
            }
        }
    }
}
