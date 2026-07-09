package com.resourceful_refinement.content.bucket_excavator.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

/**
 * @param sourceBlock The block being mined by the excavator
 * @param mineralType The block being stored in a MineralDepositBlock
 *                    or {@code null} / {@link net.minecraft.world.level.block.Blocks#AIR} if the source block is not a mineral deposit.
 */
public record ExcavationRecipeInput(Block sourceBlock, @Nullable Block mineralType) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return sourceBlock == null || sourceBlock.defaultBlockState().isAir();
    }
}
