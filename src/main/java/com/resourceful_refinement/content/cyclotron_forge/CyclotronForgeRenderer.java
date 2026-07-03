package com.resourceful_refinement.content.cyclotron_forge;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.milking_station.MilkingStationModel;
import com.resourceful_refinement.registry.ModBlocks;
import com.resourceful_refinement.registry.ModPartialModels;
import com.simibubi.create.AllBlocks;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import static com.resourceful_refinement.content.combustion_chamber.CombustionChamberBlock.FACING;
import static com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer.getAngleForBe;
import static com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer.getRotationAxisOf;

public class CyclotronForgeRenderer implements BlockEntityRenderer<CyclotronControllerBlockEntity> {

    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "textures/block/cyclotron/cyclotron_forge.png");
    public static final ResourceLocation COIL_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "textures/block/cyclotron/cyclotron_coil.png");
    public static final ResourceLocation COIL_WINDOW_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "textures/block/cyclotron/cyclotron_coil_window.png");

    private final CyclotronFrontModel frontCap;
    private final CyclotronBackModel backCap;
    private final CyclotronCoilModel coilModel;

    private final float CyclotronGlowHue = getNormalizedHue(0x78ecff);

    public CyclotronForgeRenderer(BlockEntityRendererProvider.Context context) {
        this.frontCap = new CyclotronFrontModel(context.bakeLayer(CyclotronFrontModel.LAYER_LOCATION));
        this.backCap = new CyclotronBackModel(context.bakeLayer(CyclotronBackModel.LAYER_LOCATION));
        this.coilModel = new CyclotronCoilModel(context.bakeLayer(CyclotronCoilModel.LAYER_LOCATION));
    }

    @Override
    public void render(CyclotronControllerBlockEntity be, float partialTick, PoseStack poseStack,
            MultiBufferSource bufferSource, int light, int overlay) {
        if (!be.isAssembled()) {
            renderBlock(ModBlocks.CYCLOTRON_CONTROLLER.get().defaultBlockState(), poseStack, bufferSource, light, 0, 0, 0);
            return;
        }

        // Procedural model placement
        BlockState state = be.getBlockState();
        Direction facing = state.getValue(FACING);

        VertexConsumer casingBuffer = bufferSource.getBuffer(RenderType.entityCutout(TEXTURE));
        VertexConsumer coilBuffer = bufferSource.getBuffer(RenderType.entityCutout(COIL_TEXTURE));
        VertexConsumer coilWindowBuffer = bufferSource.getBuffer(RenderType.entityCutout(COIL_WINDOW_TEXTURE));

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.scale(1, -1, -1);
        poseStack.mulPose(Axis.YP.rotationDegrees(facing.toYRot()));
        frontCap.render(poseStack, casingBuffer, light, overlay);

        for (int i = 1; i <= be.getCoilLength(); i++)
        {
            poseStack.translate(0, 0, 1);
            if (be.isProcessing())
            {
                long timeMs = (long) ((be.getLevel().getGameTime() + partialTick))*50;
                float saturation = ((float) Math.cos((timeMs%3000)*Math.PI * 2/3000f) + 1f) * 0.5f;

                if (i % 2 == 1)
                    coilModel.renderWithInteriorTint(poseStack, coilWindowBuffer, light, overlay, CyclotronGlowHue, saturation, saturation*0.8f + 0.2f);
                else
                    coilModel.renderWithInteriorTint(poseStack, coilBuffer, light, overlay, CyclotronGlowHue, saturation, saturation*0.8f + 0.2f);
            }
            else
            {
                if (i % 2 == 1)
                    coilModel.renderWithInteriorTint(poseStack, coilWindowBuffer, light, overlay, CyclotronGlowHue, 0, 0.2f);
                else
                    coilModel.renderWithInteriorTint(poseStack, coilBuffer, light, overlay, CyclotronGlowHue, 0, 0.2f);
            }
        }
        poseStack.translate(0, 0, 1);
        backCap.render(poseStack, casingBuffer, light, overlay);

        // --- Shaft Kinetic Rendering ---
        CyclotronKineticProxyBlockEntity kineticProxy = be.getKineticProxy();
        if (kineticProxy != null) {
            poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
            poseStack.translate(-0.5, -0.5, -0.5);
            Direction.Axis shaftAxis = getRotationAxisOf(kineticProxy);
            float shaftAngle = getAngleForBe(kineticProxy, be.getBlockPos(), shaftAxis);
            SuperByteBuffer shaft = CachedBuffers.partial(shaftAxis == Direction.Axis.X ?
                    ModPartialModels.SHAFT_X :
                    ModPartialModels.SHAFT_Z, state);
            if (shaftAxis == Direction.Axis.Z)
                shaftAngle *= -1;

            shaft.rotateCentered(shaftAngle, shaftAxis)
                    .light(light)
                    .renderInto(poseStack, bufferSource.getBuffer(RenderType.solid()));
        }

        poseStack.popPose();
    }

    public static float getNormalizedHue(int hexColor) {
        // 1. Extract the RGB components from the hexadecimal int
        float r = ((hexColor >> 16) & 0xFF) / 255f;
        float g = ((hexColor >> 8) & 0xFF) / 255f;
        float b = (hexColor & 0xFF) / 255f;

        // 2. Find the minimum and maximum channel values
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;

        // 3. Calculate hue
        if (delta == 0f) {
            return 0f; // Undefined hue (grayscale)
        }

        float hue = 0f;
        if (max == r) {
            hue = (g - b) / delta;
        } else if (max == g) {
            hue = 2f + (b - r) / delta;
        } else if (max == b) {
            hue = 4f + (r - g) / delta;
        }

        // 4. Convert to 0.0 to 1.0 range
        hue /= 6f;
        if (hue < 0f) {
            hue += 1f;
        }

        return hue;
    }

    private static void renderBlock(BlockState state, PoseStack poseStack, MultiBufferSource bufferSource, int light,
            int x, int y, int z) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state, poseStack, bufferSource, light,
                OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(CyclotronControllerBlockEntity be) {
        if (!be.isAssembled()) return new AABB(be.getBlockPos());
        Direction facing = be.getFacing();
        BlockPos controller = be.getBlockPos();
        BlockPos inputCenter = controller.relative(facing.getOpposite(), be.getCoilLength() + 1);
        BlockPos p1 = controller.relative(facing.getClockWise(), 1).below();
        BlockPos p2 = inputCenter.relative(facing.getCounterClockWise(), 1).above();
        return new AABB(controller).minmax(new AABB(p1)).minmax(new AABB(p2)).inflate(1.0);
    }

    @Override
    public boolean shouldRenderOffScreen(CyclotronControllerBlockEntity be) {
        return be.isAssembled();
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
