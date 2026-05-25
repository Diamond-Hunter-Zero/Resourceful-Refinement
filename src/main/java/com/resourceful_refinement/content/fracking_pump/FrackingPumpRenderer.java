package com.resourceful_refinement.content.fracking_pump;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.forge_mould.*;
import com.resourceful_refinement.registry.ModPartialModels;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import static com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer.getAngleForBe;

public class FrackingPumpRenderer extends SafeBlockEntityRenderer<FrackingPumpOutletBlockEntity> {

    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "textures/block/fracking_pump.png");

    private final FrackingPumpOutletModel outletModel;
    private final FrackingPumpBaseModel baseModel;
    private final FrackingPumpShaftModel shaftModel;
    private final FrackingPumpTopModel topModel;
    private final FrackingPumpCounterweightModel counterweightModel;

    private long ponderTimer = 0;

    public FrackingPumpRenderer(BlockEntityRendererProvider.Context context) {
        this.outletModel = new FrackingPumpOutletModel(context.bakeLayer(FrackingPumpLayers.OUTLET));
        this.baseModel = new FrackingPumpBaseModel(context.bakeLayer(FrackingPumpLayers.BASE));
        this.shaftModel = new FrackingPumpShaftModel(context.bakeLayer(FrackingPumpLayers.SHAFT));
        this.topModel = new FrackingPumpTopModel(context.bakeLayer(FrackingPumpLayers.TOP));
        this.counterweightModel = new FrackingPumpCounterweightModel(context.bakeLayer(FrackingPumpLayers.COUNTERWEIGHT));
    }



    @Override
    protected void renderSafe(FrackingPumpOutletBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        BlockState state = be.getBlockState();
        Direction facing = state.getValue(FrackingPumpOutletBlock.FACING);

        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutout(TEXTURE));

        if (!be.isAssembled() && be.renderFalsePylon <= 0) {
            ms.pushPose();
            ms.translate(0.5, 1.5, 0.5);
            ms.scale(1.0F, -1.0F, -1.0F);
            ms.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180 - facing.toYRot()));
            outletModel.render(ms, vc, light, overlay);
            ms.popPose();
            
            // --- Shaft Kinetic Rendering (when disassembled) ---
            renderKineticShaft(be, state, ms, buffer, light);
            return;
        }

        // --- Assembled Multiblock Rendering ---
        long time = be.getLevel().getGameTime();
        int rpmThreshold = be.getRequiredRPM();

        int poleHeight = be.getPoleHeight();
        if (be.renderFalsePylon > 0)
            poleHeight = be.renderFalsePylon;
        int ringHeight = be.getRingHeight();
        if (be.renderFalsePylon > 0)
            ringHeight = be.renderFalsePylon/2;


        if (Math.abs(be.getSpeed()) >= rpmThreshold) {
            long timeMs = (long) ((time + partialTicks) * 50);
            if (be.renderFalsePylon > 0)
            {
                ponderTimer += 1;
                timeMs = ponderTimer * 20;
            }

            baseModel.animatePistons(timeMs);
        } else {
            baseModel.animatePistons(0);
        }

        ms.pushPose();
        ms.translate(0.5, 1.5, 0.5);
        ms.scale(1.0F, -1.0F, -1.0F);
        ms.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180 - facing.toYRot()));

        baseModel.renderToBuffer(ms, vc, light, overlay, 0xFFFFFFFF);

        // Mast (Shaft model)
        // The pole starts 3 blocks above the base. Because Y is inverted, it's -3.
        for (int i = 0; i < poleHeight; i++) {
            ms.pushPose();
            ms.translate(0, -3 - i, 0);
            shaftModel.render(ms, vc, light, overlay);
            ms.popPose();
        }

        // Cap
        ms.pushPose();
        ms.translate(0, -3 - poleHeight, 0);
        topModel.render(ms, vc, light, overlay);
        ms.popPose();

        // Counterweight
        float dropProgress = 0;
        if (Math.abs(be.getSpeed()) >= rpmThreshold) {
            float tickProgress = (time % FrackingPumpOutletBlockEntity.DROP_CYCLE_DURATION) + partialTicks;
            if (be.renderFalsePylon > 0)
                tickProgress = (ponderTimer * 0.5f) % (int)FrackingPumpOutletBlockEntity.DROP_CYCLE_DURATION;

            float normalized = tickProgress / FrackingPumpOutletBlockEntity.DROP_CYCLE_DURATION;
            // Cycle: rapid drop (0 to 0.1), slow rise (0.1 to 1.0)
            if (normalized < FrackingPumpOutletBlockEntity.DROP_NORMALISED_DURATION) {
                dropProgress = normalized / FrackingPumpOutletBlockEntity.DROP_NORMALISED_DURATION;
            } else if (normalized < FrackingPumpOutletBlockEntity.DROP_NORMALISED_DURATION + FrackingPumpOutletBlockEntity.DROP_PAUSE_NORMALISED_DURATION) {
                dropProgress = 1;
            } else {
                dropProgress = 1.0f - ((normalized - FrackingPumpOutletBlockEntity.DROP_NORMALISED_DURATION - FrackingPumpOutletBlockEntity.DROP_PAUSE_NORMALISED_DURATION) / (1 - FrackingPumpOutletBlockEntity.DROP_NORMALISED_DURATION - FrackingPumpOutletBlockEntity.DROP_PAUSE_NORMALISED_DURATION));
            }
        }
        
        float dropDistance = poleHeight - ringHeight; // It can drop until it hits the casing
        if (dropDistance < 0) dropDistance = 0;
        
        float counterweightYOffset = dropProgress * dropDistance;

        ms.pushPose();
        // Start at top (which is -3 - poleHeight + ringHeight because it hangs from the cap)
        ms.translate(0, -3 - poleHeight + counterweightYOffset, 0);
        
        for (int i = 0; i < ringHeight; i++) {
            ms.pushPose();
            // Render layers downwards. Since Y is inverted, downwards is positive Y.
            ms.translate(0, i + 1, 0); 
            counterweightModel.render(ms, vc, light, overlay);
            ms.popPose();
        }
        ms.popPose();

        ms.popPose();

        // --- Shaft Kinetic Rendering (when assembled) ---
        renderKineticShaft(be, state, ms, buffer, light);
    }

    private void renderKineticShaft(FrackingPumpOutletBlockEntity be, BlockState state, PoseStack ms, MultiBufferSource buffer, int light) {
        Direction.Axis shaftAxis = getRotationAxisOf(be);
        SuperByteBuffer shaft = CachedBuffers.partial(shaftAxis == Direction.Axis.X ? 
            ModPartialModels.SHAFT_X :
            ModPartialModels.SHAFT_Z, state);
        
        ms.pushPose();
        float shaftAngle = getAngleForBe(be, be.getBlockPos(), shaftAxis);
        shaft.rotateCentered(shaftAngle, shaftAxis)
             .light(light)
             .renderInto(ms, buffer.getBuffer(RenderType.solid()));
        ms.popPose();
    }

    protected Direction.Axis getRotationAxisOf(FrackingPumpOutletBlockEntity be) {
        return be.getBlockState().getValue(FrackingPumpOutletBlock.FACING).getClockWise().getAxis();
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
