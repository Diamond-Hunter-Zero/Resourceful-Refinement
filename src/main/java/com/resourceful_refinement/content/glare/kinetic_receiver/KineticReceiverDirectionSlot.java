package com.resourceful_refinement.content.glare.kinetic_receiver;

import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Places Create's value box on the four side faces of the Kinetic Receiver's base plate (the 4px slab on the
 * shaft side, opposite FACING), centred on the plate's thickness. Works for all six FACING orientations.
 */
public class KineticReceiverDirectionSlot extends ValueBoxTransform.Sided {
    /** Base plate thickness in pixels; the box is centred halfway into it from the back face. */
    private static final double BASE_THICKNESS = 4;
    /** Distance of the box origin from the face plane in pixels (negative = inset), matching Create plus a tiny nudge. */
    private static final double FACE_OFFSET = -0.35;

    @Override
    protected boolean isSideActive(BlockState state, Direction direction) {
        return direction.getAxis() != state.getValue(DirectionalKineticBlock.FACING).getAxis();
    }

    @Override
    public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
        Direction back = state.getValue(DirectionalKineticBlock.FACING).getOpposite();
        // Centre of the side face, then slid toward the back onto the base plate. Create's value box geometry
        // already stands proud of its origin, so the origin sits at Create's usual 0.5px inset (as in
        // CenteredSideValueBoxTransform), backed off by a tiny 0.1px nudge so the box reads flush with the face.
        Vec3 centre = VecHelper.getCenterOf(BlockPos.ZERO);
        Vec3 outward = Vec3.atLowerCornerOf(direction.getNormal()).scale(0.5 + FACE_OFFSET / 16);
        Vec3 towardBase = Vec3.atLowerCornerOf(back.getNormal()).scale((8 - BASE_THICKNESS / 2) / 16);
        return centre.add(outward).add(towardBase);
    }

    @Override
    protected Vec3 getSouthLocation() {
        return VecHelper.voxelSpace(8, 8, 16 + FACE_OFFSET);
    }

    @Override
    public float getScale() {
        // The plate is only 4px deep; shrink the box so it doesn't overhang onto the dish frame.
        return 0.325f;
    }
}
