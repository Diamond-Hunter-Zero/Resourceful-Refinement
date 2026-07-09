package com.resourceful_refinement.content.glare.common;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

public class TrioAddressSlot extends Trio {

    private float yHeight;
    private float zDepth;
    private float xRot;

    public TrioAddressSlot(int index, float yHeight, float zDepth, float xRot)
    { super(index);
        this.yHeight = yHeight;
        this.zDepth = zDepth;
        this.xRot = xRot;
    }

    @Override public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
        Vec3 location = VecHelper.voxelSpace(3.5F + index * 4.5F, yHeight, zDepth);
        return rotateHorizontally(state, location);
    }

    @Override public void rotate(LevelAccessor level, BlockPos pos, BlockState state, PoseStack pose) {
        float yRot = AngleHelper.horizontalAngle(state.getValue(BlockStateProperties.HORIZONTAL_FACING));
        TransformStack.of(pose).rotateYDegrees(yRot).rotateXDegrees(xRot);
    }

    @Override public float getScale() { return .45F; }
}
