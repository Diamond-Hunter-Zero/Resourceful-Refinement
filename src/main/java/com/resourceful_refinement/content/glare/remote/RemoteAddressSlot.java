package com.resourceful_refinement.content.glare.remote;

import com.mojang.blaze3d.vertex.PoseStack;
import com.resourceful_refinement.content.glare.terminal.Trio;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

/** Trio value boxes arranged left-to-right on a block's horizontal front face. */
public class RemoteAddressSlot extends Trio {
    public RemoteAddressSlot(int index) {
        super(index);
    }

    @Override
    public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
        // ValueBoxTransform's horizontal convention starts on the south face and rotates to FACING.
        // Keeping rendering and hit testing on the same transform is important for the transporter,
        // whose non-interactable casing sits immediately in front of this controller face.
        Vec3 southFace = VecHelper.voxelSpace(3.5F + index * 4.5F, 12F, 15.9F);
        return rotateHorizontally(state, southFace);
    }

    @Override
    public void rotate(LevelAccessor level, BlockPos pos, BlockState state, PoseStack pose) {
        float yRot = AngleHelper.horizontalAngle(state.getValue(BlockStateProperties.HORIZONTAL_FACING)) + 180F;
        TransformStack.of(pose).rotateYDegrees(yRot);
    }

    @Override public float getScale() { return .45F; }
}
