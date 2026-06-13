package com.resourceful_refinement.content.combustion_chamber;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.fracking_pump.FrackingPumpOutletBlock;
import com.resourceful_refinement.content.fracking_pump.FrackingPumpOutletBlockEntity;
import com.resourceful_refinement.content.refill_station.FluidRefillStationBlock;
import com.resourceful_refinement.content.refill_station.FluidRefillStationBlockEntity;
import com.resourceful_refinement.content.refill_station.FluidRefillStationCasingModel;
import com.resourceful_refinement.content.refill_station.FluidRefillStationLayers;
import com.resourceful_refinement.registry.ModPartialModels;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

import static com.resourceful_refinement.content.combustion_chamber.CombustionChamberBlock.FACING;
import static com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer.getAngleForBe;

public class CombustionChamberRenderer extends SafeBlockEntityRenderer<CombustionChamberBlockEntity> {

    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "textures/block/combustion_chamber.png");

    private final CombustionChamberModel casing;

    public CombustionChamberRenderer(BlockEntityRendererProvider.Context context) {
        this.casing = new CombustionChamberModel(context.bakeLayer(CombustionChamberModel.LAYER_LOCATION));
    }

    @Override
    protected void renderSafe(CombustionChamberBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
            int light, int overlay) {
        BlockState state = be.getBlockState();
        Direction facing = state.getValue(FACING);

        VertexConsumer casingBuffer = buffer.getBuffer(RenderType.entityCutout(TEXTURE));

        ms.pushPose();
        ms.translate(0.5, 1.5, 0.5);
        ms.scale(1, -1, -1);
        ms.mulPose(Axis.YP.rotationDegrees(facing.toYRot()));
        casing.render(ms, casingBuffer, light, overlay);

        // --- Render Animations ---
        if (Math.abs(be.getSpeed()) >= 0) {
            long timeMs = (long) ((be.getLevel().getGameTime() + partialTicks) * 50);
            casing.animatePistons(timeMs);
        } else {
            casing.animatePistons(0);
        }
        ms.popPose();


        // --- Shaft Kinetic Rendering ---
        renderKineticShaft(be, state, ms, buffer, light);
    }

    private void renderKineticShaft(CombustionChamberBlockEntity be, BlockState state, PoseStack ms, MultiBufferSource buffer, int light) {
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

    protected Direction.Axis getRotationAxisOf(CombustionChamberBlockEntity be) {
        return be.getBlockState().getValue(CombustionChamberBlock.FACING).getAxis();
    }
}
