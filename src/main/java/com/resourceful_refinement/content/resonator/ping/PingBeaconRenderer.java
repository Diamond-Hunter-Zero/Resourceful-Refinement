package com.resourceful_refinement.content.resonator.ping;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

/**
 * Renders the active Ping Beacon as a vanilla-beacon-style beam spanning the full world height at the pinged column.
 * Uses {@link PingBeaconRenderType#PING_BEAM}, so terrain in front occludes it like a real beacon beam.
 */
public final class PingBeaconRenderer {

    private static final float INNER_RADIUS = 0.13F;
    private static final float OUTER_RADIUS = 0.20F;

    // Beam colours as 0xAARRGGBB — warm amber to read as a Resonator ping. The alpha is the base opacity,
    // scaled by the end-of-life fade at render time.
    private static final int INNER_COLOR = 0xDCcf081b;
    private static final int OUTER_COLOR = 0x55ff7086;

    private static final float V_SCALE = 0.125F;
    private static final float SCROLL_SPEED = 0.05F;
    private static final float INNER_SPIN = 0.03F;

    private PingBeaconRenderer() {}

    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || !ClientPingBeacon.isActiveIn(minecraft.level.dimension().location())) {
            return;
        }

        float partialTick = event.getPartialTick().getGameTimeDeltaTicks();
        float fade = ClientPingBeacon.alpha(partialTick);
        if (fade <= 0.0F) {
            return;
        }

        BlockPos pos = ClientPingBeacon.pos();
        float time = (float) minecraft.level.getGameTime() + partialTick;
        float scroll = -time * SCROLL_SPEED;
        int minY = minecraft.level.getMinBuildHeight();
        int maxY = minecraft.level.getMaxBuildHeight();
        float centerX = pos.getX() + 0.5F;
        float centerZ = pos.getZ() + 0.5F;

        Vec3 camera = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffer = minecraft.renderBuffers().bufferSource();

        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);
        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer consumer = buffer.getBuffer(PingBeaconRenderType.PING_BEAM);

        renderColumn(matrix, consumer, centerX, centerZ, minY, maxY, OUTER_RADIUS, 0.0F, scroll,
                applyFade(OUTER_COLOR, fade));
        renderColumn(matrix, consumer, centerX, centerZ, minY, maxY, INNER_RADIUS, time * INNER_SPIN, scroll,
                applyFade(INNER_COLOR, fade));

        poseStack.popPose();
        buffer.endBatch(PingBeaconRenderType.PING_BEAM);
    }

    /** Scales a 0xAARRGGBB colour's alpha channel by {@code fade}, leaving RGB intact. */
    private static int applyFade(int color, float fade) {
        int alpha = (int) (((color >>> 24) & 0xFF) * fade);
        return (alpha << 24) | (color & 0x00FFFFFF);
    }

    /** Draws a 4-sided square beam column of half-width {@code radius}, optionally spun about its vertical axis. */
    private static void renderColumn(Matrix4f matrix, VertexConsumer consumer, float cx, float cz, int y0, int y1,
            float radius, float angle, float scroll, int color) {
        if ((color >>> 24) == 0) {
            return;
        }
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        // Square corners (local), rotated about the column axis.
        float[][] local = {{-radius, -radius}, {radius, -radius}, {radius, radius}, {-radius, radius}};
        float[] cornerX = new float[4];
        float[] cornerZ = new float[4];
        for (int i = 0; i < 4; i++) {
            cornerX[i] = cx + local[i][0] * cos - local[i][1] * sin;
            cornerZ[i] = cz + local[i][0] * sin + local[i][1] * cos;
        }

        float vBottom = y0 * V_SCALE + scroll;
        float vTop = y1 * V_SCALE + scroll;

        for (int i = 0; i < 4; i++) {
            int j = (i + 1) % 4;
            float ax = cornerX[i], az = cornerZ[i];
            float bx = cornerX[j], bz = cornerZ[j];
            // Outward normal of this face (perpendicular to the edge, pointing away from the axis).
            float nx = (ax + bx) * 0.5F - cx;
            float nz = (az + bz) * 0.5F - cz;
            float nLen = (float) Math.sqrt(nx * nx + nz * nz);
            if (nLen > 1.0E-4F) {
                nx /= nLen;
                nz /= nLen;
            }
            vertex(matrix, consumer, ax, y0, az, color, 0.0F, vBottom, nx, nz);
            vertex(matrix, consumer, bx, y0, bz, color, 1.0F, vBottom, nx, nz);
            vertex(matrix, consumer, bx, y1, bz, color, 1.0F, vTop, nx, nz);
            vertex(matrix, consumer, ax, y1, az, color, 0.0F, vTop, nx, nz);
        }
    }

    private static void vertex(Matrix4f matrix, VertexConsumer consumer, float x, float y, float z,
            int color, float u, float v, float nx, float nz) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(nx, 0.0F, nz);
    }
}
