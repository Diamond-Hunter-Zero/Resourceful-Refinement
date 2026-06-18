package com.resourceful_refinement.content.glare.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.resourceful_refinement.content.glare.GlareSavedData;
import com.resourceful_refinement.network.GlareLinkSyncPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;

public final class GlareClientLinks {
    private static final ResourceLocation BEACON_BEAM = ResourceLocation.withDefaultNamespace("textures/entity/beacon_beam.png");
    private static final ResourceLocation GUARDIAN_BEAM = ResourceLocation.withDefaultNamespace("textures/entity/guardian_beam.png");
    private static final double MAX_RENDER_DISTANCE_SQ = 1024.0D * 1024.0D;
    private static final float VALID_WIDTH = 0.155F;
    private static final float BLOCKED_WIDTH = 0.155F;
    private static final float TILE_LENGTH = 1F;

    private static ResourceLocation dimension = ResourceLocation.withDefaultNamespace("overworld");
    private static List<GlareLinkSyncPayload.Link> links = List.of();

    private GlareClientLinks() {}

    public static void handleSync(GlareLinkSyncPayload payload) {
        dimension = payload.dimension();
        links = List.copyOf(payload.links());
    }

    public static void render(RenderLevelStageEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || !minecraft.level.dimension().location().equals(dimension) || links.isEmpty()) {
            return;
        }
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
            return;
        }

        float time = (float) (minecraft.level.getGameTime() + event.getPartialTick().getGameTimeDeltaTicks());
        Vec3 camera = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffer = minecraft.renderBuffers().bufferSource();

        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);
        for (GlareLinkSyncPayload.Link link : links) {
            Vec3 a = Vec3.atCenterOf(link.a());
            Vec3 b = Vec3.atCenterOf(link.b());
            if (!isNearCamera(camera, link.a(), link.b())) {
                continue;
            }
            renderBeam(poseStack, buffer, a, b, link.validity(), time);
        }
        poseStack.popPose();

        buffer.endBatch(RenderType.beaconBeam(BEACON_BEAM, true));
        buffer.endBatch(RenderType.entityCutoutNoCull(GUARDIAN_BEAM));
    }

    private static boolean isNearCamera(Vec3 camera, BlockPos a, BlockPos b) {
        return camera.distanceToSqr(Vec3.atCenterOf(a)) <= MAX_RENDER_DISTANCE_SQ
                || camera.distanceToSqr(Vec3.atCenterOf(b)) <= MAX_RENDER_DISTANCE_SQ;
    }

    private static void renderBeam(PoseStack poseStack, MultiBufferSource buffer, Vec3 start, Vec3 end, GlareSavedData.LinkValidity validity, float time) {
        Vec3 delta = end.subtract(start);
        double length = delta.length();
        if (length <= 0.001D) {
            return;
        }

        Vec3 direction = delta.normalize();
        Vec3 reference = Math.abs(direction.y) > 0.95D ? new Vec3(1.0D, 0.0D, 0.0D) : new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 right = direction.cross(reference).normalize();
        Vec3 up = right.cross(direction).normalize();

        boolean blocked = validity == GlareSavedData.LinkValidity.BLOCKED;
        float width = blocked ? BLOCKED_WIDTH : VALID_WIDTH;
        VertexConsumer consumer = buffer.getBuffer(blocked ? RenderType.entityCutoutNoCull(GUARDIAN_BEAM) : RenderType.beaconBeam(BEACON_BEAM, true));

        int segments = Math.max(1, (int) Math.ceil(length / TILE_LENGTH));
        float scrollSpeed = 0.035F;
        float scroll = -time * scrollSpeed;

        for (int segment = 0; segment < segments; segment++) {
            double startT = segment / (double) segments;
            double endT = (segment + 1) / (double) segments;
            Vec3 segmentStart = start.lerp(end, startT);
            Vec3 segmentEnd = start.lerp(end, endT);
            float vStart = (float) (length * startT / TILE_LENGTH) + scroll;
            float vEnd = (float) (length * endT / TILE_LENGTH) + scroll;

            if (blocked) {
                drawRibbon(poseStack.last().pose(), consumer, segmentStart, segmentEnd, right.scale(width), right, vStart, vEnd, 170, 210, 255, 255);
                drawRibbon(poseStack.last().pose(), consumer, segmentStart, segmentEnd, up.scale(width), up, vStart, vEnd, 170, 210, 255, 255);
            } else {
                drawTubeSide(poseStack.last().pose(), consumer, segmentStart, segmentEnd, right, up, width, vStart, vEnd, 120, 245, 255, 190);
                drawTubeSide(poseStack.last().pose(), consumer, segmentStart, segmentEnd, right.reverse(), up.reverse(), width, vStart, vEnd, 120, 245, 255, 190);
                drawTubeSide(poseStack.last().pose(), consumer, segmentStart, segmentEnd, up, right.reverse(), width, vStart, vEnd, 255, 255, 255, 150);
                drawTubeSide(poseStack.last().pose(), consumer, segmentStart, segmentEnd, up.reverse(), right, width, vStart, vEnd, 255, 255, 255, 150);
            }
        }
    }

    private static void drawTubeSide(Matrix4f matrix, VertexConsumer consumer, Vec3 start, Vec3 end, Vec3 faceNormal, Vec3 tangent, float width, float vStart, float vEnd, int red, int green, int blue, int alpha) {
        Vec3 centerOffset = faceNormal.scale(width);
        Vec3 halfWidth = tangent.scale(width);
        Vec3 a = start.add(centerOffset).add(halfWidth);
        Vec3 b = start.add(centerOffset).subtract(halfWidth);
        Vec3 c = end.add(centerOffset).subtract(halfWidth);
        Vec3 d = end.add(centerOffset).add(halfWidth);
        vertex(matrix, consumer, a, 0.0F, vStart, faceNormal, red, green, blue, alpha);
        vertex(matrix, consumer, b, 1.0F, vStart, faceNormal, red, green, blue, alpha);
        vertex(matrix, consumer, c, 1.0F, vEnd, faceNormal, red, green, blue, alpha);
        vertex(matrix, consumer, d, 0.0F, vEnd, faceNormal, red, green, blue, alpha);
    }

    private static void drawRibbon(Matrix4f matrix, VertexConsumer consumer, Vec3 start, Vec3 end, Vec3 halfWidth, Vec3 normal, float vStart, float vEnd, int red, int green, int blue, int alpha) {
        Vec3 a = start.subtract(halfWidth);
        Vec3 b = start.add(halfWidth);
        Vec3 c = end.add(halfWidth);
        Vec3 d = end.subtract(halfWidth);
        vertex(matrix, consumer, a, 0.0F, vStart, normal, red, green, blue, alpha);
        vertex(matrix, consumer, b, 1.0F, vStart, normal, red, green, blue, alpha);
        vertex(matrix, consumer, c, 1.0F, vEnd, normal, red, green, blue, alpha);
        vertex(matrix, consumer, d, 0.0F, vEnd, normal, red, green, blue, alpha);
    }

    private static void vertex(Matrix4f matrix, VertexConsumer consumer, Vec3 pos, float u, float v, Vec3 normal, int red, int green, int blue, int alpha) {
        Vector3f normalVector = normal.toVector3f();
        consumer.addVertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z)
                .setColor(red, green, blue, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(normalVector.x(), normalVector.y(), normalVector.z());
    }
}
