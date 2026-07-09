package com.resourceful_refinement.utilities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public record RegionExtents(BlockPos min, BlockPos max)
{
    public static RegionExtents GetFaceExtendedRegion(BlockPos targetPos, Direction direction, int xSize, int ySize, int zSize) {
        // 1. Calculate local starting offsets to center the box on the face.
        int startX = -((xSize - 1) / 2);
        int startY = -((ySize - 1) / 2);

        // 2. Transform the two extreme local corners into world-space positions.
        // Local Z starts at 1 so the region is perfectly flush right outside the front face.
        BlockPos localMinCorner = transformLocalToGlobal(targetPos, direction, startX, startY, 1);
        BlockPos localMaxCorner = transformLocalToGlobal(targetPos, direction, startX + xSize - 1, startY + ySize - 1, zSize);

        // 3. Normalize the coordinates into absolute minimum and maximum structural extents.
        BlockPos min = new BlockPos(
                Math.min(localMinCorner.getX(), localMaxCorner.getX()),
                Math.min(localMinCorner.getY(), localMaxCorner.getY()),
                Math.min(localMinCorner.getZ(), localMaxCorner.getZ())
        );

        BlockPos max = new BlockPos(
                Math.max(localMinCorner.getX(), localMaxCorner.getX()),
                Math.max(localMinCorner.getY(), localMaxCorner.getY()),
                Math.max(localMinCorner.getZ(), localMaxCorner.getZ())
        );

        return new RegionExtents(min, max);
    }

    /// Map local (X, Y, Z) vectors to absolute global BlockPos offsets.
    private static BlockPos transformLocalToGlobal(BlockPos target, Direction dir, int localX, int localY, int localZ) {
        Direction localXDir;
        Direction localYDir;

        if (dir.getAxis().isHorizontal()) {
            // Horizontal: Local Z is the facing direction, Local Y is Up, Local X is the side-normal
            localXDir = dir.getClockWise();
            localYDir = Direction.UP;
        } else {
            // Vertical (UP/DOWN): Local Z is Up/Down, we project the local plane onto North/East
            localXDir = Direction.EAST;
            localYDir = Direction.NORTH;
        }

        return target
                .relative(dir, localZ)
                .relative(localXDir, localX)
                .relative(localYDir, localY);
    }
}
