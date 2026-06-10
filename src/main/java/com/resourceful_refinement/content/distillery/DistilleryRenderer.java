package com.resourceful_refinement.content.distillery;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.utilities.FluidBoxRendering;
import com.resourceful_refinement.utilities.heating.HeatUtilities;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

import static com.resourceful_refinement.content.distillery.DistilleryBlock.MODEL_TYPE;

public class DistilleryRenderer implements BlockEntityRenderer<DistilleryBlockEntity> {

    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "textures/block/distillery_tank_connected.png");

    private static final float FLUID_STACK_START_Y = 0.1f;
    private static final float FLUID_STACK_RADII = 0.45f;

    public DistilleryRenderer(BlockEntityRendererProvider.Context context) {
    }


    @Override
    public void render(DistilleryBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        Direction facing = be.getBlockState().getValue(DistilleryBlock.FACING);
        int modelType = be.getBlockState().getValue(MODEL_TYPE);

        ms.pushPose();

        DistilleryBlockEntity controller = be.getController();
        if (controller != null && (modelType == 0 || modelType == 3))
        {
            // Render progress gauge
            float recipeProgress = controller.getProgressionFactor();
            if (recipeProgress > 0)
            {
                ms.pushPose();
                ms.translate(0.5, 0.5, 0.5);
                if (facing.getAxis() == Direction.Axis.X)
                    ms.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
                else
                    ms.mulPose(Axis.YP.rotationDegrees(facing.toYRot()));
                ms.translate(3f/16f,  modelType== 3 ? (-6f/16f) : (-5f/16f), 0.5005f);
                renderColouredQuad(ms, buffer, light, overlay, 2f/16f, recipeProgress * 10f/16f, 0, 0, 255, 150, 0, 100);
                ms.popPose();
            }

            // Render heat gauge
            ms.pushPose();
            ms.translate(0.5, 0.5, 0.5);
            if (facing.getAxis() == Direction.Axis.X)
                ms.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
            else
                ms.mulPose(Axis.YP.rotationDegrees(facing.toYRot()));

            int gaugeRotation = HeatUtilities.GetGaugeRotation(controller.heatLevel);
            ms.translate(-3f/16f, 0, 0.5005f);
            ms.mulPose(Axis.ZP.rotationDegrees(gaugeRotation));
            renderColouredQuad(ms, buffer, light, overlay, 1f/16f, 3.15f/16f, -0.5f/16f, -0.5f/16f, 150, 10, 0, 255);
            ms.popPose();
        }

        // Render fluid stacks
        if (be.isController())
        {
            ms.pushPose();
            ms.translate(0.5, 0, 0.5);
            float height = be.stackSize - 0.2f;
            FluidBoxRendering.renderFluidsForTanks(ms, buffer, light, height, FLUID_STACK_START_Y, FLUID_STACK_RADII, true, be.inputTank, be.outputTank);
            ms.popPose();
        }

        ms.popPose();

        // Render filter slot
        if (be.stackIndex == be.stackSize - 1)
            FilteringRenderer.renderOnBlockEntity(be, partialTicks, ms, buffer, light, overlay);
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    private void renderColouredQuad(PoseStack ms, MultiBufferSource buffer, int light, int overlay, float width, float height, float xOffset, float yOffset, int red, int green, int blue, int alpha)
    {
        VertexConsumer consumer = buffer.getBuffer(RenderType.gui());
        PoseStack.Pose pose = ms.last();
        Matrix4f matrix = pose.pose();

        float x1 = 0f + xOffset, x2 = width + xOffset;
        float y1 = 0f + yOffset, y2 = height + yOffset;
        float z = 0f;

        // Vertex 1: Bottom-Left (0, 0)
        consumer.addVertex(matrix, x1, y1, z)
                .setUv(0f, 0f)
                .setColor(red, green, blue, alpha)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, 0, 0, 1);

        // Vertex 2: Bottom-Right (width, 0)
        consumer.addVertex(matrix, x2, y1, z)
                .setUv(1f, 0f)
                .setColor(red, green, blue, alpha)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, blue, 0, 1);

        // Vertex 3: Top-Right (width, height)
        consumer.addVertex(matrix, x2, y2, z)
                .setUv(1f, 1f)
                .setColor(red, green, blue, alpha)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, 0, 0, 1);

        // Vertex 4: Top-Left (0, height)
        consumer.addVertex(matrix, x1, y2, z)
                .setUv(0f, 1f)
                .setColor(red, green, blue, alpha)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, 0, 0, 1);
    }
}
