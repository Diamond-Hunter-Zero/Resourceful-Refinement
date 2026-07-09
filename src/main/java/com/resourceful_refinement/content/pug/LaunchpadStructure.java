package com.resourceful_refinement.content.pug;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.List;

public final class LaunchpadStructure {
    public static final int WIDTH = 3;
    public static final int DEPTH = 3;
    public static final int PROXY_COUNT = 8;

    private LaunchpadStructure() {}

    /** Local depth grows behind the controller; local lateral grows toward facing-clockwise. */
    public static BlockPos position(BlockPos controllerPos, Direction facing, int lateral, int depth) {
        if (!facing.getAxis().isHorizontal()) throw new IllegalArgumentException("Launchpad facing must be horizontal");
        if (lateral < -1 || lateral > 1 || depth < 0 || depth >= DEPTH) {
            throw new IllegalArgumentException("Launchpad local position is outside the 3x3 footprint");
        }
        return controllerPos.relative(facing.getClockWise(), lateral).relative(facing.getOpposite(), depth);
    }

    public static List<ProxyPosition> proxyPositions(BlockPos controllerPos, Direction facing) {
        List<ProxyPosition> positions = new ArrayList<>(PROXY_COUNT);
        for (int depth = 0; depth < DEPTH; depth++) {
            for (int lateral = -1; lateral <= 1; lateral++) {
                if (depth == 0 && lateral == 0) continue;
                positions.add(new ProxyPosition(position(controllerPos, facing, lateral, depth), lateral, depth));
            }
        }
        return List.copyOf(positions);
    }

    public static List<BlockPos> footprint(BlockPos controllerPos, Direction facing) {
        List<BlockPos> positions = new ArrayList<>(WIDTH * DEPTH);
        positions.add(controllerPos.immutable());
        proxyPositions(controllerPos, facing).forEach(proxy -> positions.add(proxy.pos()));
        return List.copyOf(positions);
    }

    public static BlockPos rearCenter(BlockPos controllerPos, Direction facing) {
        return position(controllerPos, facing, 0, DEPTH - 1);
    }

    public static Direction rearFace(Direction facing) {
        return facing.getOpposite();
    }

    public record ProxyPosition(BlockPos pos, int lateral, int depth) {
        public ProxyPosition {
            pos = pos.immutable();
        }

        public boolean isRearCenter() {
            return lateral == 0 && depth == DEPTH - 1;
        }
    }
}
