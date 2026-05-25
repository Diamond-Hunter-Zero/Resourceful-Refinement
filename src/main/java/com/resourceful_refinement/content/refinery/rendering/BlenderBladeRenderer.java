package com.resourceful_refinement.content.refinery.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.refinery.BlenderBladeBlockEntity;
import com.resourceful_refinement.registry.ModPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;

public class BlenderBladeRenderer extends KineticBlockEntityRenderer<BlenderBladeBlockEntity> {
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "textures/block/fluid_refinery.png");

    private final RefineryBlenderModel blender;

    public BlenderBladeRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.blender = new RefineryBlenderModel(context.bakeLayer(RefineryLayers.BLENDER));
    }

    @Override
    protected void renderSafe(BlenderBladeBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        if (be.getLevel() == null) return;

        Direction.Axis axis = be.getBlockState().getValue(RotatedPillarKineticBlock.AXIS);
        float angle = getAngleForBe(be, be.getBlockPos(), axis);

        // --- Render Rotating Shaft ---
        /*PartialModel shaftModel;
        if (axis == Direction.Axis.X) {
            shaftModel = ModPartialModels.SHAFT_X;
        } else if (axis == Direction.Axis.Z) {
            shaftModel = ModPartialModels.SHAFT_Z;
        } else {
            shaftModel = ModPartialModels.SHAFT_VERTICAL;
        }

        SuperByteBuffer shaft = CachedBuffers.partial(shaftModel, be.getBlockState());
        kineticRotationTransform(shaft, be, axis, angle, light)
                .renderInto(ms, buffer.getBuffer(RenderType.solid()));*/

        // --- Render Rotating Blender Blades ---
        ms.pushPose();
        ms.translate(0.5, 0.5, 0.5);

        // Apply rotation around the block's rotation axis
        float angleDegrees = (float) Math.toDegrees(angle);
            //angleDegrees -= 4.2f * Math.abs(be.getSpeed());

        if (axis == Direction.Axis.X) {
            ms.mulPose(Axis.XP.rotationDegrees(angleDegrees));
            ms.mulPose(Axis.ZP.rotationDegrees(90));
        } else if (axis == Direction.Axis.Z) {
            ms.mulPose(Axis.ZP.rotationDegrees(angleDegrees));
            ms.mulPose(Axis.XP.rotationDegrees(90));
        } else {
            ms.mulPose(Axis.YP.rotationDegrees(angleDegrees));
        }

        // Align with Blockbench coordinate system and center the blender model part
        ms.scale(-1.0F, -1.0F, 1.0F);
        ms.translate(0, -1.0, 0);

        blender.render(ms, buffer.getBuffer(RenderType.entityTranslucent(TEXTURE)), light, overlay);
        ms.popPose();
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

}
