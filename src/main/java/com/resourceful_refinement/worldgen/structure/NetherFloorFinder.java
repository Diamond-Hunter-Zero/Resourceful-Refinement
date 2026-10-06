package com.resourceful_refinement.worldgen.structure;

import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.OptionalInt;

/**
 * Finds a Nether cave floor to anchor a structure on, without generating chunks. In the Nether the
 * {@code WORLD_SURFACE_WG} heightmap resolves to the column ceiling, so structures that must sit on the
 * walkable floor scan the generation noise column for the highest air-over-solid pocket instead.
 *
 * <p>Shared by {@link NetherSurfaceJigsawStructure} (geysers) and the configurable crystal-node structure.</p>
 */
public final class NetherFloorFinder {
    public static final int SCAN_TOP = 115;
    public static final int SCAN_BOTTOM = 32;

    private NetherFloorFinder() {
    }

    /**
     * The Y of the air block directly above the highest solid, non-fluid floor in {@code (x, z)}'s noise
     * column between {@link #SCAN_BOTTOM} and {@link #SCAN_TOP}, or empty if no such floor exists.
     */
    public static OptionalInt findFloorY(Structure.GenerationContext context, int x, int z) {
        NoiseColumn column = context.chunkGenerator().getBaseColumn(x, z, context.heightAccessor(), context.randomState());
        for (int y = SCAN_TOP; y > SCAN_BOTTOM; y--) {
            BlockState current = column.getBlock(y);
            BlockState below = column.getBlock(y - 1);
            if (current.isAir() && !below.isAir() && below.getFluidState().isEmpty()) {
                return OptionalInt.of(y);
            }
        }
        return OptionalInt.empty();
    }
}
