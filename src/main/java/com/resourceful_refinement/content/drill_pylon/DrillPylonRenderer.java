package com.resourceful_refinement.content.drill_pylon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.utilities.ShaftUtilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import static com.resourceful_refinement.content.drill_pylon.DrillPylonHeadBlock.FACING;
import static com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer.getAngleForBe;

public class DrillPylonRenderer implements BlockEntityRenderer<DrillPylonHeadBlockEntity> {

    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "textures/block/drill_pylon.png");

    private final DrillPylonModel drillModel;
    private final DrillPylonHeadModel standaloneDrillModel;

    public DrillPylonRenderer(BlockEntityRendererProvider.Context context) {
        this.drillModel = new DrillPylonModel(context.bakeLayer(DrillPylonModel.LAYER_LOCATION));
        this.standaloneDrillModel = new DrillPylonHeadModel(context.bakeLayer(DrillPylonHeadModel.LAYER_LOCATION));
    }

    @Override
    public void render(DrillPylonHeadBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) {

        if (!be.isAssembled()) {
            BlockState state = be.getBlockState();
            float angle = getAngleForBe(be, be.getBlockPos(), state.getValue(FACING).getAxis());
            DrillPylonHeadRendering.render(standaloneDrillModel, state, angle, poseStack, bufferSource, light, overlay);
            return;
        }

        float now = (be.getLevel().getGameTime() + partialTick) / 20.0F;
        applyDrillAnimation(be, now);

        // Orientate model towards facing, and render
        BlockState state = be.getBlockState();
        Direction facing = state.getValue(FACING);

        VertexConsumer casingBuffer = bufferSource.getBuffer(RenderType.entityCutout(TEXTURE));
        poseStack.pushPose();

        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.scale(1, -1, -1);
        poseStack.mulPose(Axis.YP.rotationDegrees(facing.toYRot()));
        drillModel.render(poseStack, casingBuffer, light, overlay);

        poseStack.popPose();

        renderInputShafts(be, poseStack, bufferSource);
    }

    /**
     * Advances the head's client-side animation phase from its synced drilling flag, then poses the model.
     * Retracted -> extending (start-up) -> running (looping spin) -> retracting (wind-down) -> retracted.
     * Start-up and wind-down always play to completion, and the spin loop finishes its current revolution
     * before winding down, so the bit never pops between poses.
     */
    private void applyDrillAnimation(DrillPylonHeadBlockEntity be, float now) {
        DrillAnimationPhase phase = be.getAnimPhase();
        float phaseStart = be.getAnimPhaseStart();
        float spinOffset = be.getAnimSpinOffset();
        boolean drilling = be.isDrilling();

        // First frame after load: skip the start-up if the drill is already running
        if (phase == null) {
            phase = drilling ? DrillAnimationPhase.RUNNING : DrillAnimationPhase.RETRACTED;
            phaseStart = now;
        }

        switch (phase) {
            case RETRACTED -> {
                if (drilling) {
                    phase = DrillAnimationPhase.EXTENDING;
                    phaseStart = now;
                }
            }
            case EXTENDING -> {
                if (now - phaseStart >= EXTEND_LENGTH) {
                    // drill_extend ends at -540 degrees; carry that into the following loop
                    spinOffset = wrapRadians(spinOffset + EXTEND_END_YAW);
                    phaseStart += EXTEND_LENGTH;
                    phase = drilling ? DrillAnimationPhase.RUNNING : DrillAnimationPhase.RETRACTING;
                }
            }
            case RUNNING -> {
                if (!drilling) {
                    float cycleEnd = phaseStart + (float) Math.ceil((now - phaseStart) / LOOP_LENGTH) * LOOP_LENGTH;
                    if (now >= cycleEnd) {
                        phase = DrillAnimationPhase.RETRACTING;
                        phaseStart = cycleEnd;
                    }
                }
            }
            case RETRACTING -> {
                if (now - phaseStart >= RETRACT_LENGTH) {
                    phase = DrillAnimationPhase.RETRACTED;
                    phaseStart += RETRACT_LENGTH;
                }
            }
        }
        be.setAnimState(phase, phaseStart, spinOffset);

        float elapsed = now - phaseStart;
        switch (phase) {
            case RETRACTED -> drillModel.applyAnimation(DrillPylonModel.drill_retract, RETRACT_LENGTH, spinOffset);
            case EXTENDING -> drillModel.applyAnimation(DrillPylonModel.drill_extend, elapsed, spinOffset);
            case RUNNING -> drillModel.applyAnimation(DrillPylonModel.drill_spin_loop, elapsed, spinOffset);
            case RETRACTING -> drillModel.applyAnimation(DrillPylonModel.drill_retract, elapsed, spinOffset);
        }
    }

    private static final float EXTEND_END_YAW = (float) Math.toRadians(-540.0);
    private static final float EXTEND_LENGTH = DrillPylonModel.drill_extend.lengthInSeconds();
    private static final float LOOP_LENGTH = DrillPylonModel.drill_spin_loop.lengthInSeconds();
    private static final float RETRACT_LENGTH = DrillPylonModel.drill_retract.lengthInSeconds();

    private static float wrapRadians(float angle) {
        float twoPi = (float) (Math.PI * 2.0);
        return ((angle % twoPi) + twoPi) % twoPi;
    }

    private static void renderBlock(BlockState state, PoseStack poseStack, MultiBufferSource bufferSource, int light, int x, int y, int z) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state, poseStack, bufferSource, light, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    /// Renders a half shaft on the outer face of each of the pylon's two kinetic input proxies
    private void renderInputShafts(DrillPylonHeadBlockEntity be, PoseStack ms, MultiBufferSource buffer) {
        Direction front = be.getFront();
        for (Direction side : new Direction[]{front.getClockWise(), front.getCounterClockWise()}) {
            BlockPos proxyPos = be.getBlockPos().above(DrillPylonHeadBlockEntity.KINETIC_Y).relative(side);
            if (!(be.getLevel().getBlockEntity(proxyPos) instanceof DrillPylonKineticProxyBlockEntity proxy)) continue;

            ms.pushPose();
            ms.translate(proxyPos.getX() - be.getBlockPos().getX(), proxyPos.getY() - be.getBlockPos().getY(), proxyPos.getZ() - be.getBlockPos().getZ());
            ShaftUtilities.RenderKineticShaft(
                    proxy.getBlockState(),
                    side,
                    true,
                    getAngleForBe(proxy, proxyPos, side.getAxis()),
                    ms, buffer, LevelRenderer.getLightColor(be.getLevel(), proxyPos.relative(side)));
            ms.popPose();
        }
    }
}
