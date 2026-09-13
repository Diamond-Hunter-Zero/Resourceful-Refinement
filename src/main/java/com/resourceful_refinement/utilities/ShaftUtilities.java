package com.resourceful_refinement.utilities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.resourceful_refinement.registry.ModPartialModels;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import static com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer.getAngleForBe;

public class ShaftUtilities {

    /// Returns the appropriate shaft model for an axis or face
    public static PartialModel GetShaftModel(Direction outputFace, boolean isHalfShaft)
    {
        switch (outputFace.getAxis())
        {
            case X -> {
                if (!isHalfShaft) return ModPartialModels.SHAFT_X;
                else if (outputFace == Direction.WEST) return ModPartialModels.SHAFT_X_HALF;
                else return ModPartialModels.SHAFT_X_HALF_MIRROR;
            }
            case Y -> {
                if (!isHalfShaft) return ModPartialModels.SHAFT_VERTICAL;
                else if (outputFace == Direction.UP) return ModPartialModels.SHAFT_VERTICAL_HALF;
                else return ModPartialModels.SHAFT_VERTICAL_HALF_MIRROR;
            }
            case Z -> {
                if (!isHalfShaft) return ModPartialModels.SHAFT_Z;
                else if (outputFace == Direction.NORTH) return ModPartialModels.SHAFT_Z_HALF;
                else return ModPartialModels.SHAFT_Z_HALF_MIRROR;
            }
        }
        return ModPartialModels.SHAFT_Z;
    }

    /// Renders a kinetic shaft via Flywheel. retrieving the appropriate shaft model according to output face. OutputFace should be provided in world-space-frame
    public static void RenderKineticShaft(BlockState state, Direction outputFace, boolean isHalfShaft, float shaftAngle, PoseStack ms, MultiBufferSource buffer, int light)
    {
        SuperByteBuffer shaft = CachedBuffers.partial(GetShaftModel(outputFace, isHalfShaft), state);

        ms.pushPose();
        shaft.rotateCentered(shaftAngle, outputFace.getAxis())
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.solid()));
        ms.popPose();
    }

}
