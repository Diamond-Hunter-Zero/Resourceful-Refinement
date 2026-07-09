package com.resourceful_refinement.content.mechanical_stamper;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public class MechanicalStamperRenderer extends SafeBlockEntityRenderer<MechanicalStamperBlockEntity> {
    private static final ResourceLocation CASING_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "textures/block/copper_casing.png");
    private static final ResourceLocation SHAFT_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "textures/block/shafts/axis.png");
    private static final ResourceLocation HEAD_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "textures/block/brass_block.png");
    private static final float HEAD_HALF_WIDTH = 3.5F / 16F;
    private static final float HEAD_HALF_LENGTH = 3F / 16F;
    private static final float HEAD_REST_OFFSET = 11F / 16F;
    private static final float HEAD_TRAVEL = 11F / 16F;

    private final ItemRenderer itemRenderer;

    public MechanicalStamperRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    protected void renderSafe(MechanicalStamperBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
                              int light, int overlay) {
        Direction facing = be.getFacing();
        renderPlaceholderShaft(be, facing, ms, buffer, light);
        renderPlaceholderHead(be, facing, partialTicks, ms, buffer, light);
        renderHeldItems(be, facing, partialTicks, ms, buffer, light, overlay);
    }

    private void renderPlaceholderShaft(MechanicalStamperBlockEntity be, Direction facing, PoseStack ms,
                                        MultiBufferSource buffer, int light) {
        Direction.Axis axis = be.getBlockState().getBlock() instanceof MechanicalStamperBlock stamperBlock
                ? stamperBlock.getRotationAxis(be.getBlockState())
                : facing.getClockWise().getAxis();
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutout(SHAFT_TEXTURE));
        if (axis == Direction.Axis.X) {
            renderTexturedBox(ms, consumer, 0, 6.5F / 16F, 6.5F / 16F, 1, 9.5F / 16F, 9.5F / 16F,
                    light, OverlayTexture.NO_OVERLAY);
        } else {
            renderTexturedBox(ms, consumer, 6.5F / 16F, 6.5F / 16F, 0, 9.5F / 16F, 9.5F / 16F, 1,
                    light, OverlayTexture.NO_OVERLAY);
        }
    }

    private void renderPlaceholderHead(MechanicalStamperBlockEntity be, Direction facing, float partialTicks, PoseStack ms,
                                       MultiBufferSource buffer, int light) {
        float extension = be.getRenderedHeadOffset(partialTicks);
        double centerX = 0.5 + facing.getStepX() * (HEAD_REST_OFFSET + HEAD_TRAVEL * extension);
        double centerY = 0.5;
        double centerZ = 0.5 + facing.getStepZ() * (HEAD_REST_OFFSET + HEAD_TRAVEL * extension);

        AABB headBox = createFacingBox(facing, centerX, centerY, centerZ, HEAD_HALF_WIDTH, HEAD_HALF_LENGTH);
        renderTexturedBox(ms, buffer.getBuffer(RenderType.entityCutout(HEAD_TEXTURE)),
                (float) headBox.minX, (float) headBox.minY, (float) headBox.minZ,
                (float) headBox.maxX, (float) headBox.maxY, (float) headBox.maxZ,
                light, OverlayTexture.NO_OVERLAY);

        AABB guideBox = createFacingBox(facing, 0.5 + facing.getStepX() * 0.57, 0.5,
                0.5 + facing.getStepZ() * 0.57, 1.5F / 16F, 7.5F / 16F);
        renderTexturedBox(ms, buffer.getBuffer(RenderType.entityCutout(CASING_TEXTURE)),
                (float) guideBox.minX, (float) guideBox.minY, (float) guideBox.minZ,
                (float) guideBox.maxX, (float) guideBox.maxY, (float) guideBox.maxZ,
                light, OverlayTexture.NO_OVERLAY);
    }

    private void renderHeldItems(MechanicalStamperBlockEntity be, Direction facing, float partialTicks, PoseStack ms,
                                 MultiBufferSource buffer, int light, int overlay) {
        ItemStack stamp = be.getStampItem();
        if (!stamp.isEmpty()) {
            renderStampItem(be, stamp, facing, partialTicks, ms, buffer, light, overlay);
        }

        ItemStack medium = be.getFillMedium();
        if (!medium.isEmpty()) {
            renderMediumItem(be, medium, facing, ms, buffer, light, overlay);
        }
    }

    private void renderStampItem(MechanicalStamperBlockEntity be, ItemStack stack, Direction facing, float partialTicks,
                                 PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        float extension = be.getRenderedHeadOffset(partialTicks);
        double offset = HEAD_REST_OFFSET + HEAD_TRAVEL * extension + HEAD_HALF_LENGTH + 0.01F;

        ms.pushPose();
        ms.translate(0.5 + facing.getStepX() * offset, 0.5, 0.5 + facing.getStepZ() * offset);
        rotateTowardFacing(facing, ms);
        ms.scale(0.35F, 0.35F, 0.35F);
        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, ms, buffer, be.getLevel(),
                (int) be.getBlockPos().asLong());
        ms.popPose();
    }

    private void renderMediumItem(MechanicalStamperBlockEntity be, ItemStack stack, Direction facing, PoseStack ms,
                                  MultiBufferSource buffer, int light, int overlay) {
        Direction side = facing.getClockWise();

        ms.pushPose();
        ms.translate(0.5 + side.getStepX() * 0.53, 0.75, 0.5 + side.getStepZ() * 0.53);
        rotateTowardFacing(side, ms);
        ms.scale(0.3F, 0.3F, 0.3F);
        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, ms, buffer, be.getLevel(),
                (int) be.getBlockPos().asLong() + 1);
        ms.popPose();
    }

    private static AABB createFacingBox(Direction facing, double centerX, double centerY, double centerZ,
                                        float halfWidth, float halfLength) {
        double minY = centerY - halfWidth;
        double maxY = centerY + halfWidth;
        if (facing.getAxis() == Direction.Axis.X) {
            return new AABB(centerX - halfLength, minY, centerZ - halfWidth,
                    centerX + halfLength, maxY, centerZ + halfWidth);
        }
        return new AABB(centerX - halfWidth, minY, centerZ - halfLength,
                centerX + halfWidth, maxY, centerZ + halfLength);
    }

    private static void rotateTowardFacing(Direction facing, PoseStack ms) {
        ms.mulPose(Axis.YP.rotationDegrees(180 - facing.toYRot()));
        ms.mulPose(Axis.XP.rotationDegrees(90));
    }

    private static void renderTexturedBox(PoseStack ms, VertexConsumer consumer,
                                          float x1, float y1, float z1,
                                          float x2, float y2, float z2,
                                          int light, int overlay) {
        PoseStack.Pose pose = ms.last();
        int packedLight = light == 0 ? LightTexture.FULL_BRIGHT : light;

        face(consumer, pose, x1, y1, z1, x1, y2, z1, x2, y2, z1, x2, y1, z1, 0, 0, -1, packedLight, overlay);
        face(consumer, pose, x1, y1, z2, x2, y1, z2, x2, y2, z2, x1, y2, z2, 0, 0, 1, packedLight, overlay);
        face(consumer, pose, x1, y1, z1, x1, y1, z2, x1, y2, z2, x1, y2, z1, -1, 0, 0, packedLight, overlay);
        face(consumer, pose, x2, y1, z1, x2, y2, z1, x2, y2, z2, x2, y1, z2, 1, 0, 0, packedLight, overlay);
        face(consumer, pose, x1, y2, z1, x1, y2, z2, x2, y2, z2, x2, y2, z1, 0, 1, 0, packedLight, overlay);
        face(consumer, pose, x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2, 0, -1, 0, packedLight, overlay);
    }

    private static void face(VertexConsumer consumer, PoseStack.Pose pose,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float x3, float y3, float z3,
                             float x4, float y4, float z4,
                             float nx, float ny, float nz,
                             int light, int overlay) {
        vertex(consumer, pose, x1, y1, z1, 0, 1, nx, ny, nz, light, overlay);
        vertex(consumer, pose, x2, y2, z2, 0, 0, nx, ny, nz, light, overlay);
        vertex(consumer, pose, x3, y3, z3, 1, 0, nx, ny, nz, light, overlay);
        vertex(consumer, pose, x4, y4, z4, 1, 1, nx, ny, nz, light, overlay);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose,
                               float x, float y, float z,
                               float u, float v,
                               float nx, float ny, float nz,
                               int light, int overlay) {
        consumer.addVertex(pose, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }

    @Override
    public AABB getRenderBoundingBox(MechanicalStamperBlockEntity be) {
        return new AABB(be.getBlockPos()).inflate(2);
    }

    @Override
    public int getViewDistance() {
        return 128;
    }
}
