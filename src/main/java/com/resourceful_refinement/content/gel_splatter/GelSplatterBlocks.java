package com.resourceful_refinement.content.gel_splatter;

import com.resourceful_refinement.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

/**
 * Helpers for the gel splatter block family ({@code gel_splatter}, {@code gel_splatter_sticky},
 * {@code gel_splatter_slippery}).
 */
public final class GelSplatterBlocks {

    private GelSplatterBlocks() {}

    public static boolean is(Block block) {
        return block instanceof GelSplatterBlock;
    }

    public static boolean is(BlockState state) {
        return state != null && is(state.getBlock());
    }

    /** {@link GelType#GOOEY Gooey} (sticky) and {@link GelType#SPEEDY Speedy} use dedicated block variants. */
    public static Block getBlockForGelType(GelType type) {
        return switch (type) {
            case GOOEY -> ModBlocks.GEL_SPLATTER_STICKY.get();
            case SPEEDY -> ModBlocks.GEL_SPLATTER_SLIPPERY.get();
            case MOLTEN -> ModBlocks.GEL_SPLATTER_MOLTEN.get();
            case BOUNCY -> ModBlocks.GEL_SPLATTER_BOUNCY.get();
            default -> ModBlocks.GEL_SPLATTER.get();
        };
    }

    public static Block getBlockForFluid(Fluid fluid) {
        return getBlockForGelType(GelPropertiesManager.getGelType(fluid));
    }

    public static BlockState defaultStateForFluid(Fluid fluid) {
        return getBlockForFluid(fluid).defaultBlockState();
    }

    /** Preserves multiface attachments when switching between gel splatter variants. */
    public static BlockState withVariantForFluid(BlockState current, Fluid fluid) {
        Block target = getBlockForFluid(fluid);
        if (current.getBlock() == target) {
            return current;
        }
        BlockState result = target.defaultBlockState();
        for (Direction direction : Direction.values()) {
            result = result.setValue(
                    GelSplatterBlock.getFaceProperty(direction),
                    current.getValue(GelSplatterBlock.getFaceProperty(direction)));
        }
        return result;
    }
}
