package com.resourceful_refinement.content.forge_mould;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.resourceful_refinement.registry.ModPartialModels;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.resources.ResourceLocation;
import com.resourceful_refinement.ResourcefulRefinementMain;

import static com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer.getAngleForBe;

public class ForgeMouldRenderer extends SafeBlockEntityRenderer<MechanicalForgeMouldBlockEntity> {

    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "textures/block/forge_mould.png");

    private final ForgeMouldCasingModel casing;
    private final ForgeMouldPressModel press;
    private final ForgeMouldTubeModel tube;

    public ForgeMouldRenderer(BlockEntityRendererProvider.Context context) {
        this.casing = new ForgeMouldCasingModel(context.bakeLayer(ForgeMouldLayers.CASING));
        this.press = new ForgeMouldPressModel(context.bakeLayer(ForgeMouldLayers.PRESS));
        this.tube = new ForgeMouldTubeModel(context.bakeLayer(ForgeMouldLayers.TUBE));
    }

    @Override
    protected void renderSafe(MechanicalForgeMouldBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        BlockState state = be.getBlockState();
        Direction facing = state.getValue(MechanicalForgeMouldBlock.FACING);

        // --- Shaft Rendering ---
        Direction.Axis shaftAxis = getRotationAxisOf(be);
        SuperByteBuffer shaft = CachedBuffers.partial(shaftAxis == Direction.Axis.X ? 
            com.resourceful_refinement.registry.ModPartialModels.SHAFT_X : 
            com.resourceful_refinement.registry.ModPartialModels.SHAFT_Z, state);
        
        ms.pushPose();
        float shaftAngle = getAngleForBe(be, be.getBlockPos(), shaftAxis);
        shaft.rotateCentered(shaftAngle, shaftAxis)
             .light(light)
             .renderInto(ms, buffer.getBuffer(RenderType.solid()));
        ms.popPose();

        // --- Forge Mould Rendering ---
        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutout(TEXTURE));

        ms.pushPose();
        ms.translate(0.5, 1.5, 0.5);
        ms.scale(1, -1, -1);

        // Rotate models based on block facing
        float angle = facing.toYRot();
        ms.mulPose(Axis.YP.rotationDegrees(angle));

        // Render Casing
        casing.render(ms, vc, light, overlay);

        // Calculate extension (0 to 18 pixels)
        float extension = be.extensionProgress;


        if (be.runFalseAnimation)
        {
            switch (be.falseAnimState) {
                case IDLE -> {
                    be.falseAnimState = MechanicalForgeMouldBlockEntity.RunningState.EXTENDING;
                    be.falseAnimationExtension = 0f;
                }
                case EXTENDING -> {
                    be.falseAnimationExtension += 0.02f;
                    extension = be.falseAnimationExtension;
                    if (be.falseAnimationExtension >= 1f)
                    {
                        be.falseAnimState = MechanicalForgeMouldBlockEntity.RunningState.IMPACTING;
                        be.falseAnimationExtension = 0f;
                    }
                }
                case IMPACTING -> {
                    extension = 1f;
                    be.falseAnimationExtension += 0.0125f;
                    if (be.falseAnimationExtension >= 1f)
                    {
                        be.falseAnimState = MechanicalForgeMouldBlockEntity.RunningState.RETRACTING;
                        be.falseAnimationExtension = 0f;
                    }
                }
                case RETRACTING -> {
                    be.falseAnimationExtension += 0.02f;
                    extension = 1f - be.falseAnimationExtension;
                    if (be.falseAnimationExtension >= 1f)
                    {
                        be.falseAnimState = MechanicalForgeMouldBlockEntity.RunningState.IDLE;
                        be.falseAnimationExtension = 0f;
                        be.runFalseAnimation = false;
                    }
                }
            }
        }

        float yOffset = extension * 18f;

        // Render Press
        // Start at model Y=2px and move down
        // Note that a positive Y in this frame is 'downwards'
        ms.pushPose();
        ms.translate(0, (1 + yOffset)/16f, 0);
        press.render(ms, vc, light, overlay);
        ms.popPose();

        // Render Tubes
        // Stacking tubes from the top of the block down to the press
        if (extension > 0) {
            // Tube height is 10 pixels. 
            // Model renders 14-24 pixels down from its pivot.
            int numTubes = (int) Math.ceil(yOffset / 10f);
            for (int i = 0; i < numTubes; i++) {
                ms.pushPose();
                // First tube ends at 8 + 10 = 18. Pivot = 18 - 24 = -6.
                // Each subsequent tube is 10 pixels further down.
                float tubeOffset = (yOffset - 1f - 10f * i)/16f;
                ms.translate(0, tubeOffset, 0);
                
                // If it's the last tube, we might want it to follow the press exactly
                // but for now, simple stacking is easier.
                tube.render(ms, vc, light, overlay);
                ms.popPose();
            }
        }

        ms.popPose();

        // Render filter slot
        FilteringRenderer.renderOnBlockEntity(be, partialTicks, ms, buffer, light, overlay);
    }

    protected Direction.Axis getRotationAxisOf(MechanicalForgeMouldBlockEntity be) {
        return be.getBlockState().getValue(MechanicalForgeMouldBlock.FACING).getClockWise().getAxis();
    }

    @Override
    public int getViewDistance() {
        return 128;
    }
}
