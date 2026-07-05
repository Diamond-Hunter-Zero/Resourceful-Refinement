package com.resourceful_refinement.content.conveyor;

import com.simibubi.create.content.contraptions.AssemblyException;
import com.simibubi.create.content.contraptions.bearing.BearingContraption;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class ConveyorRotatorContraption extends BearingContraption {
    public ConveyorRotatorContraption() {
        super(false, Direction.UP);
    }

    public ConveyorRotatorContraption(Direction facing) {
        super(false, facing);
    }

    @Override
    public boolean assemble(Level level, BlockPos pos) throws AssemblyException {
        BlockPos sourcePos = pos.above();
        if (!level.isLoaded(sourcePos) || level.getBlockState(sourcePos).isAir()) {
            return false;
        }

        anchor = sourcePos;
        bounds = new AABB(BlockPos.ZERO);
        addBlock(level, sourcePos, capture(level, sourcePos));
        startMoving(level);
        expandBoundsAroundAxis(Direction.Axis.Y);
        return !blocks.isEmpty();
    }

    @Override
    protected boolean isAnchoringBlockAt(BlockPos pos) {
        return pos.equals(BlockPos.ZERO.below());
    }
}
