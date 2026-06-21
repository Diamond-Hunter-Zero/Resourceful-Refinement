package com.resourceful_refinement.content.glare.terminal;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class TelemetryAddressSlot extends Trio {
    public TelemetryAddressSlot(int index) { super(index); }

    @Override public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
        Vec3 location = VecHelper.voxelSpace(3.5F + index * 4.5F, 15.9F, 8F);
        return rotateHorizontally(state, location);
    }

    @Override public void rotate(LevelAccessor level, BlockPos pos, BlockState state, PoseStack pose) {
        float yRot = AngleHelper.horizontalAngle(state.getValue(TelemetryTerminalBlock.FACING));
        TransformStack.of(pose).rotateYDegrees(yRot).rotateXDegrees(90);
    }

    @Override public float getScale() { return .45F; }
}
