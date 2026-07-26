package com.resourceful_refinement.content.combustion_chamber;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.registry.ModPartialModels;
import com.resourceful_refinement.utilities.ShaftUtilities;
import com.resourceful_refinement.utilities.heating.HeatUtilities;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import static com.resourceful_refinement.content.combustion_chamber.CombustionChamberBlock.FACING;
import static com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer.getAngleForBe;

public class CombustionChamberRenderer extends SafeBlockEntityRenderer<CombustionChamberBlockEntity> {

    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "textures/block/combustion_chamber.png");

    public static final ResourceLocation POWERED_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "textures/block/combustion_chamber_powered.png");

    private final CombustionChamberModel casing;

    public CombustionChamberRenderer(BlockEntityRendererProvider.Context context) {
        this.casing = new CombustionChamberModel(context.bakeLayer(CombustionChamberModel.LAYER_LOCATION));
    }

    @Override
    protected void renderSafe(CombustionChamberBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
            int light, int overlay) {
        BlockState state = be.getBlockState();
        Direction facing = state.getValue(FACING);

        VertexConsumer casingBuffer = buffer.getBuffer(RenderType.entityCutout(be.isChainRedstonePowered() ? POWERED_TEXTURE : TEXTURE));

        ms.pushPose();
        ms.translate(0.5, 1.5, 0.5);
        ms.scale(1, -1, -1);
        ms.mulPose(Axis.YP.rotationDegrees(facing.toYRot()));

        // --- Render Animations ---
        float engineSpeed = Math.abs(be.getRenderedEngineSpeed());
        if (engineSpeed > 0) {
            long timeMs = (long) ((be.getLevel().getGameTime() + partialTicks) * 50 * (engineSpeed / 16f));
            casing.animatePistons(timeMs);
        } else {
            casing.animatePistons(125);
        }

        casing.render(ms, casingBuffer, light, overlay);
        ms.popPose();


        // --- Heat Stand Rendering ---
        if (be.getLevel() != null && be.getLevel().getBlockState(be.getBlockPos().below()).is(HeatUtilities.SUPPORTS_HEATER_STAND_BLOCK_TAG))
        {
            ms.pushPose();
            ms.translate(0,-1,0);
            CachedBuffers.partial(ModPartialModels.INDUSTRIAL_HEATER_STAND, be.getBlockState())
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.cutout()));
            ms.popPose();
        }


        // --- Shaft Kinetic Rendering ---
        if (be.isOutputEngine()) {
            renderKineticShaft(be, state, ms, buffer, light);
        }

        // --- Fan Shaft Rendering ---
        if (be.hasIntakeFan() && be.isOutputEngine()) {
            ms.pushPose();
            ms.mulPose(Axis.YP.rotationDegrees(facing.toYRot()));
            ms.translate(0,0, facing.getAxis() == Direction.Axis.X? -1 : 1);
            ms.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
            renderKineticShaft(be, state, ms, buffer, light);
            ms.popPose();
        }
    }

    private void renderKineticShaft(CombustionChamberBlockEntity be, BlockState state, PoseStack ms, MultiBufferSource buffer, int light) {
        Direction shaftFace = be.getBlockState().getValue(CombustionChamberBlock.FACING);
        Direction.Axis shaftAxis = shaftFace.getAxis();

        ShaftUtilities.RenderKineticShaft(
                state,
                shaftFace,
                true,
                getAngleForBe(be, be.getBlockPos(), shaftAxis),
                ms, buffer, light);
    }

    @Override
    public int getViewDistance() {
        return 128;
    }
}
