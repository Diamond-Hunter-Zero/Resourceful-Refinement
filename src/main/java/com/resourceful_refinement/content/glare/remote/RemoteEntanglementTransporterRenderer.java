package com.resourceful_refinement.content.glare.remote;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.resourceful_refinement.registry.ModPartialModels;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * The static assembly is a baked block model. This renderer adds the parts that change every frame: the glow overlay,
 * tinted by operational state and pulsed while online, and the Liquid Chorus level inside the tank.
 */
public class RemoteEntanglementTransporterRenderer extends SmartBlockEntityRenderer<RemoteEntanglementTransporterBlockEntity> {
    private static final int OFFLINE_COLOUR = 0x3A3A3A;
    private static final int AUTO_COLOUR = 0xF07CFF;
    private static final int SEND_COLOUR = 0x5CFF7A;
    private static final int RECEIVE_COLOUR = 0xFF4A4A;
    private static final double PULSE_PERIOD_TICKS = 40.0;
    private static final float PULSE_MIN = 0.55F;

    // Tank interior in model space (north-facing), inset half a pixel inside the tank casing [2,16,6]-[14,29,14].
    private static final float TANK_MIN_X = 2.5F / 16F, TANK_MAX_X = 13.5F / 16F;
    private static final float TANK_MIN_Z = 6.5F / 16F, TANK_MAX_Z = 13.5F / 16F;
    private static final float TANK_MIN_Y = 16.5F / 16F, TANK_MAX_Y = 28.5F / 16F;

    public RemoteEntanglementTransporterRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override protected void renderSafe(RemoteEntanglementTransporterBlockEntity be, float partialTicks, PoseStack pose,
            MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, pose, buffer, light, overlay);
        if (!be.isRemoteEndpointAssembled() || be.getLevel() == null) return;

        Direction facing = be.getBlockState().getValue(RemoteEntanglementTransporterBlock.FACING);
        pose.pushPose();
        // Matches the blockstate's y rotations, which turn the north-facing model clockwise.
        pose.translate(0.5, 0, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(180F - facing.toYRot()));
        pose.translate(-0.5, 0, -0.5);

        CachedBuffers.partial(ModPartialModels.REMOTE_TRANSPORTER_GLOW, be.getBlockState())
                .light(LightTexture.FULL_BRIGHT)
                .color(glowColour(be, partialTicks))
                .renderInto(pose, buffer.getBuffer(RenderType.cutout()));

        renderTankFluid(be, pose, buffer, LevelRenderer.getLightColor(be.getLevel(), be.tankPos()));
        pose.popPose();
    }

    private static int glowColour(RemoteEntanglementTransporterBlockEntity be, float partialTicks) {
        TransporterMode mode = be.getBlockState().getValue(RemoteEntanglementTransporterBlock.MODE);
        // "Offline" tracks send requirements (fuel, chill, network); a Receive-Only transporter never sends, so it stays red.
        if (be.isOffline() && mode != TransporterMode.RECEIVE_ONLY) return 0xFF000000 | OFFLINE_COLOUR;
        int base = switch (mode) {
            case AUTO -> AUTO_COLOUR;
            case SEND_ONLY -> SEND_COLOUR;
            case RECEIVE_ONLY -> RECEIVE_COLOUR;
        };
        double time = be.getLevel().getGameTime() + partialTicks;
        float wave = (float) (0.5 + 0.5 * Math.sin(time * Math.PI * 2 / PULSE_PERIOD_TICKS));
        float brightness = PULSE_MIN + (1F - PULSE_MIN) * wave;
        int r = Math.round(((base >> 16) & 0xFF) * brightness);
        int g = Math.round(((base >> 8) & 0xFF) * brightness);
        int b = Math.round((base & 0xFF) * brightness);
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    private static void renderTankFluid(RemoteEntanglementTransporterBlockEntity be, PoseStack pose,
            MultiBufferSource buffer, int light) {
        FluidStack stack = be.getTank().getFluid();
        if (stack.isEmpty()) return;
        float fill = Math.min(1F, stack.getAmount() / (float) be.getTank().getCapacity());
        float top = TANK_MIN_Y + (TANK_MAX_Y - TANK_MIN_Y) * fill;
        if (top <= TANK_MIN_Y + 0.001F) return;

        IClientFluidTypeExtensions props = IClientFluidTypeExtensions.of(stack.getFluid());
        int colour = props.getTintColor(stack);
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(props.getStillTexture(stack));
        renderBox(pose, buffer.getBuffer(RenderType.translucent()), TANK_MIN_X, TANK_MIN_Y, TANK_MIN_Z,
                TANK_MAX_X, top, TANK_MAX_Z, colour, light, sprite);
    }

    /** Axis-aligned fluid cuboid with each face's UVs scaled to its size, so the sprite isn't stretched. */
    private static void renderBox(PoseStack pose, VertexConsumer consumer, float x1, float y1, float z1,
            float x2, float y2, float z2, int colour, int light, TextureAtlasSprite sprite) {
        PoseStack.Pose last = pose.last();
        int a = (colour >> 24) & 0xFF, r = (colour >> 16) & 0xFF, g = (colour >> 8) & 0xFF, b = colour & 0xFF;
        if (a == 0) a = 0xFF;
        // Horizontal faces sample x/z, vertical faces sample their width and the fill height (y) from the sprite.
        float ux1 = sprite.getU(x1), ux2 = sprite.getU(x2), uz1 = sprite.getU(z1), uz2 = sprite.getU(z2);
        float vz1 = sprite.getV(z1), vz2 = sprite.getV(z2);
        float vy1 = sprite.getV(1F - (y2 - y1)), vy2 = sprite.getV(1F);
        int[] c = {r, g, b, a};

        // Top (+Y) and bottom (-Y)
        quad(consumer, last, c, light, 0, 1, 0,
                x1, y2, z1, ux1, vz1, x1, y2, z2, ux1, vz2, x2, y2, z2, ux2, vz2, x2, y2, z1, ux2, vz1);
        quad(consumer, last, c, light, 0, -1, 0,
                x1, y1, z1, ux1, vz1, x2, y1, z1, ux2, vz1, x2, y1, z2, ux2, vz2, x1, y1, z2, ux1, vz2);
        // North (-Z) and south (+Z)
        quad(consumer, last, c, light, 0, 0, -1,
                x1, y1, z1, ux1, vy2, x1, y2, z1, ux1, vy1, x2, y2, z1, ux2, vy1, x2, y1, z1, ux2, vy2);
        quad(consumer, last, c, light, 0, 0, 1,
                x1, y1, z2, ux1, vy2, x2, y1, z2, ux2, vy2, x2, y2, z2, ux2, vy1, x1, y2, z2, ux1, vy1);
        // West (-X) and east (+X)
        quad(consumer, last, c, light, -1, 0, 0,
                x1, y1, z1, uz1, vy2, x1, y1, z2, uz2, vy2, x1, y2, z2, uz2, vy1, x1, y2, z1, uz1, vy1);
        quad(consumer, last, c, light, 1, 0, 0,
                x2, y1, z1, uz1, vy2, x2, y2, z1, uz1, vy1, x2, y2, z2, uz2, vy1, x2, y1, z2, uz2, vy2);
    }

    private static void quad(VertexConsumer consumer, PoseStack.Pose pose, int[] c, int light, float nx, float ny, float nz,
            float xa, float ya, float za, float ua, float va, float xb, float yb, float zb, float ub, float vb,
            float xc, float yc, float zc, float uc, float vc, float xd, float yd, float zd, float ud, float vd) {
        vertex(consumer, pose, c, light, nx, ny, nz, xa, ya, za, ua, va);
        vertex(consumer, pose, c, light, nx, ny, nz, xb, yb, zb, ub, vb);
        vertex(consumer, pose, c, light, nx, ny, nz, xc, yc, zc, uc, vc);
        vertex(consumer, pose, c, light, nx, ny, nz, xd, yd, zd, ud, vd);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, int[] c, int light,
            float nx, float ny, float nz, float x, float y, float z, float u, float v) {
        consumer.addVertex(pose, x, y, z).setColor(c[0], c[1], c[2], c[3]).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
    }

    @Override public boolean shouldRenderOffScreen(RemoteEntanglementTransporterBlockEntity be) { return true; }
    @Override public int getViewDistance() { return 256; }
}
