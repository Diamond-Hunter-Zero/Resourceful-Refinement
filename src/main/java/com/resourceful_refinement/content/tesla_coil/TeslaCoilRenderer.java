package com.resourceful_refinement.content.tesla_coil;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.client.fx.lightning.LightningRenderer;
import com.resourceful_refinement.client.fx.lightning.LightningStyle;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import static com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer.getAngleForBe;

/**
 * Draws the Tesla Coil head (spinning with the network about FACING), a ring model on each exposed shaft below it,
 * the coil-to-coil arc and the charge strikes onto a held belt/depot item.
 */
public class TeslaCoilRenderer extends SafeBlockEntityRenderer<TeslaCoilBlockEntity> {

    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "textures/block/tesla_coil/tesla_coil.png");
    public static final ResourceLocation RING_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "textures/block/tesla_coil/tesla_coil_ring.png");

    private static final float ARC_FADE_OUT_TICKS = 4.0F;
    private static final float CHARGE_FADE_TICKS = 2.0F;
    /** How far the charging beam's midpoint is pulled from the straight head-to-head line toward the item (0..1). */
    private static final double CHARGE_BEAM_SAG = 0.5D;

    private final TeslaCoilHeadModel headModel;
    private final TeslaCoilRingModel ringModel;

    public TeslaCoilRenderer(BlockEntityRendererProvider.Context context) {
        this.headModel = new TeslaCoilHeadModel(context.bakeLayer(TeslaCoilHeadModel.LAYER_LOCATION));
        this.ringModel = new TeslaCoilRingModel(context.bakeLayer(TeslaCoilRingModel.LAYER_LOCATION));
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    @Override
    public boolean shouldRenderOffScreen(TeslaCoilBlockEntity be) {
        return true;
    }

    @Override
    protected void renderSafe(TeslaCoilBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
                              int light, int overlay) {
        Level level = be.getLevel();
        if (level == null) {
            return;
        }
        Direction facing = be.getFacing();
        BlockPos pos = be.getBlockPos();

        // Positive-axis kinetic angle, flipped for negative facings since we spin about FACING itself.
        float axisSign = facing.getAxisDirection().getStep();

        float headAngle = getAngleForBe(be, pos, facing.getAxis()) * axisSign;
        VertexConsumer headBuffer = buffer.getBuffer(RenderType.entityCutout(TEXTURE));
        ms.pushPose();
        orient(ms, facing, headAngle);
        headModel.render(ms, headBuffer, light, overlay);
        ms.popPose();

        List<BlockPos> shafts = TeslaCoilBlockEntity.findRingShafts(level, pos, facing);
        if (!shafts.isEmpty()) {
            VertexConsumer ringBuffer = buffer.getBuffer(RenderType.entityCutout(RING_TEXTURE));
            for (BlockPos shaftPos : shafts) {
                BlockPos offset = shaftPos.subtract(pos);
                float ringAngle = getAngleForBe(be, shaftPos, facing.getAxis()) * axisSign;
                ms.pushPose();
                ms.translate(offset.getX(), offset.getY(), offset.getZ());
                orient(ms, facing, ringAngle);
                ringModel.render(ms, ringBuffer, LevelRenderer.getLightColor(level, shaftPos), overlay);
                ms.popPose();
            }
        }

        float now = level.getGameTime() + partialTicks;
        Vec3 origin = Vec3.atLowerCornerOf(pos);
        Vec3 head = be.getHeadCentre().subtract(origin);
        renderArc(be, ms, buffer, head, origin, now);
        renderChargeStrike(be, ms, buffer, head, origin, partialTicks, now);
    }

    /**
     * Maps the model's upright entity-model space onto the block: rotates local up onto {@code facing} about the block
     * centre and spins about it by {@code spin} radians.
     */
    private static void orient(PoseStack ms, Direction facing, float spin) {
        ms.translate(0.5, 0.5, 0.5);
        ms.mulPose(facing.getRotation());
        ms.mulPose(Axis.YP.rotation(spin));
        ms.translate(0.0, 1.0, 0.0);
        ms.scale(1.0F, -1.0F, -1.0F);
    }

    private static void renderArc(TeslaCoilBlockEntity be, PoseStack ms, MultiBufferSource buffer, Vec3 head,
                                  Vec3 origin, float now) {
        Vec3 target = be.getArcTarget();
        if (target == null) {
            return;
        }
        float age = now - be.getArcStartTick();
        if (age < 0.0F || age >= TeslaCoilBlockEntity.ARC_DURATION + ARC_FADE_OUT_TICKS) {
            return;
        }
        float alpha = age <= TeslaCoilBlockEntity.ARC_DURATION ? 1.0F
                : 1.0F - (age - TeslaCoilBlockEntity.ARC_DURATION) / ARC_FADE_OUT_TICKS;
        LightningRenderer.render(ms, buffer, head, target.subtract(origin), LightningStyle.ELECTRIC,
                be.getBlockPos().asLong() ^ be.getArcStartTick(), now, alpha);
    }

    /**
     * The charging beam between a coil pair: two arcs meeting at a midpoint pulled down from the straight line between
     * the heads to just above the held item, so the bolt sags onto it. Drawn by the controller coil only.
     */
    private static void renderChargeStrike(TeslaCoilBlockEntity be, PoseStack ms, MultiBufferSource buffer, Vec3 head,
                                           Vec3 origin, float partialTicks, float now) {
        Vec3 target = be.getProcessingTarget();
        BlockPos partnerPos = be.getChargePartner();
        float elapsed = be.getChargeElapsed(partialTicks);
        if (target == null || partnerPos == null || elapsed < 0.0F) {
            return;
        }
        float cycle = elapsed % (TeslaCoilBlockEntity.CHARGE_STRIKE_TICKS + TeslaCoilBlockEntity.CHARGE_PAUSE_TICKS);
        if (cycle >= TeslaCoilBlockEntity.CHARGE_STRIKE_TICKS) {
            return;
        }
        float alpha = Math.min(1.0F, (TeslaCoilBlockEntity.CHARGE_STRIKE_TICKS - cycle) / CHARGE_FADE_TICKS);

        Vec3 partnerHead = be.getLevel() != null && be.getLevel().getBlockEntity(partnerPos) instanceof TeslaCoilBlockEntity partner
                ? partner.getHeadCentre()
                : Vec3.atCenterOf(partnerPos);
        Vec3 from = head;
        Vec3 to = partnerHead.subtract(origin);
        Vec3 straightMid = from.add(to).scale(0.5D);
        Vec3 mid = straightMid.lerp(target.subtract(origin), CHARGE_BEAM_SAG);

        long seed = be.getBlockPos().asLong() * 31L + 7L;
        LightningRenderer.render(ms, buffer, from, mid, LightningStyle.PROCESSING_ARC, seed, now, alpha);
        LightningRenderer.render(ms, buffer, mid, to, LightningStyle.PROCESSING_ARC, seed ^ 0x5DEECE66DL, now, alpha);
    }
}
