package com.resourceful_refinement.content.glare.chromatic_transceiver;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.glare.kinetic_receiver.GlareKineticReceiverBlock;
import com.resourceful_refinement.content.glare.kinetic_receiver.GlareKineticReceiverBlockEntity;
import com.resourceful_refinement.content.glare.kinetic_receiver.GlareKineticReceiverModel;
import com.resourceful_refinement.registry.ModPartialModels;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;

import static com.resourceful_refinement.registry.ModPartialModels.PRISM_ROTATE_DURATION;
import static com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer.getAngleForBe;
import static com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer.kineticRotationTransform;

public class GlareChromaticTransceiverRenderer extends SafeBlockEntityRenderer<GlareChromaticTransceiverBlockEntity> {

    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "textures/block/glare/glare_chromatic_transceiver.png");

    private final GlareChromaticTransceiverModel model;

    public GlareChromaticTransceiverRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new GlareChromaticTransceiverModel(context.bakeLayer(GlareChromaticTransceiverModel.LAYER_LOCATION));
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    protected void renderSafe(GlareChromaticTransceiverBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
                              int light, int overlay) {
        BlockState state = be.getBlockState();
        Direction facing = state.getValue(GlareKineticReceiverBlock.FACING);
        VertexConsumer casingBuffer = buffer.getBuffer(RenderType.entityCutout(TEXTURE));

        ms.pushPose();

        if (facing.getAxis().isVertical())
        {
            boolean isUp = facing == Direction.UP;
            ms.translate(0.5, 0.5, isUp? 1.5 : -0.5);
            ms.scale(1, -1, -1);
            ms.mulPose(Axis.XP.rotationDegrees(isUp? 90 : -90));
        }
        else
        {
            ms.translate(0.5, 1.5, 0.5);
            ms.scale(-1, -1, 1);
            ms.mulPose(Axis.YP.rotationDegrees(facing.toYRot()));
        }


        // --- Render Block Model ---
        float time = be.getLevel().getGameTime() + partialTicks;
        float effectiveGameTime = time % (20*PRISM_ROTATE_DURATION);
        model.render(ms, casingBuffer, light, overlay, activeFilterColor(be, time));
        ms.popPose();

        // --- Prism Rendering ---
        SuperByteBuffer prism = CachedBuffers.partialFacing(ModPartialModels.GLARE_PRISM_SMALL, state);

        prism.light(light);
        prism.rotateCentered((float) ((effectiveGameTime/(20*PRISM_ROTATE_DURATION)) * 2f * Math.PI), Direction.Axis.Y);
        prism.rotateCentered((float) ((effectiveGameTime/(20*PRISM_ROTATE_DURATION)) * 2f * Math.PI), Direction.Axis.Z);
        prism.color(activeFilterColor(be, time));
        prism.renderInto(ms, buffer.getBuffer(net.minecraft.client.renderer.Sheets.translucentCullBlockSheet()));

    }

    /** Colour shown when the transceiver has no active filters. */
    private static final int NO_FILTER_GREY = 0x808080;

    /**
     * Colour for the model based on the transceiver's active filters:
     * none -> static grey, one -> that filter's colour, many -> a looping transition
     * through each active filter's colour in dye order.
     */
    private static int activeFilterColor(GlareChromaticTransceiverBlockEntity be, float time) {
        DyeColor[] dyes = DyeColor.values();
        int[] colors = new int[dyes.length];
        int count = 0;
        for (DyeColor dye : dyes) {
            if (be.getThreshold(dye) >= 0) {
                colors[count++] = dye.getTextureDiffuseColor() & 0xFFFFFF;
            }
        }

        if (count == 0) return NO_FILTER_GREY;
        if (count == 1) return colors[0];

        float segment = 20f * PRISM_ROTATE_DURATION;   // ticks per colour-to-colour transition
        float total = segment * count;                 // full loop, wrapping back to the first colour
        float progress = time % total;
        int index = (int) (progress / segment);
        float delta = (progress - index * segment) / segment;
        return lerpColorByHSB(colors[index], colors[(index + 1) % count], delta);
    }

    /**
     * Like {@link #lerpColorByHue} but interpolates hue, saturation and brightness, so it can
     * transition to/from black, white and greys. Hue takes the shortest path around the wheel;
     * achromatic endpoints (saturation 0) adopt the other endpoint's hue to avoid rainbow sweeps.
     */
    public static int lerpColorByHSB(int hexColorStart, int hexColorEnd, float delta) {
        int r1 = (hexColorStart >> 16) & 0xFF;
        int g1 = (hexColorStart >> 8) & 0xFF;
        int b1 = hexColorStart & 0xFF;

        int r2 = (hexColorEnd >> 16) & 0xFF;
        int g2 = (hexColorEnd >> 8) & 0xFF;
        int b2 = hexColorEnd & 0xFF;

        float[] hsbStart = java.awt.Color.RGBtoHSB(r1, g1, b1, null);
        float[] hsbEnd = java.awt.Color.RGBtoHSB(r2, g2, b2, null);

        float hStart = hsbStart[0], sStart = hsbStart[1], bStart = hsbStart[2];
        float hEnd = hsbEnd[0], sEnd = hsbEnd[1], bEnd = hsbEnd[2];

        // Grey/black/white have meaningless hue; borrow the chromatic endpoint's hue.
        if (sStart <= 0f) hStart = hEnd;
        if (sEnd <= 0f) hEnd = hStart;

        // Shortest path around the hue wheel.
        float dh = hEnd - hStart;
        if (dh > 0.5f) dh -= 1.0f;
        else if (dh < -0.5f) dh += 1.0f;
        float h = hStart + delta * dh;
        h -= (float) Math.floor(h); // wrap into [0, 1)

        float s = Mth.lerp(delta, sStart, sEnd);
        float b = Mth.lerp(delta, bStart, bEnd);

        return java.awt.Color.getHSBColor(h, s, b).getRGB() & 0xFFFFFF;
    }

    public static int lerpColorByHue(int hexColorStart, int hexColorEnd, float delta) {
        // 1. Extract RGB channels
        int r1 = (hexColorStart >> 16) & 0xFF;
        int g1 = (hexColorStart >> 8) & 0xFF;
        int b1 = hexColorStart & 0xFF;

        int r2 = (hexColorEnd >> 16) & 0xFF;
        int g2 = (hexColorEnd >> 8) & 0xFF;
        int b2 = hexColorEnd & 0xFF;

        // 2. Convert to HSB space (H values are normalized between 0.0f and 1.0f)
        float[] hsbStart = java.awt.Color.RGBtoHSB(r1, g1, b1, null);
        float[] hsbEnd = java.awt.Color.RGBtoHSB(r2, g2, b2, null);

        float hStart = hsbStart[0];
        float hEnd = hsbEnd[0];

        // 3. Force the interpolation to move forward through the color wheel
        // If the target hue is numerically less than the start hue, add 1.0 to wrap it
        if (hEnd < hStart) {
            hEnd += 1.0f;
        }

        // 4. Linearly interpolate the adjusted Hue
        float hInterpolated = Mth.lerp(delta, hStart, hEnd);

        // Wrap the result back into the 0.0 to 1.0 range
        hInterpolated = hInterpolated % 1.0f;

        // 5. Keep saturation and brightness fixed to the starting color's values
        float s = hsbStart[1];
        float b = hsbStart[2];

        // 6. Convert back to a clean 24-bit RGB integer
        return java.awt.Color.getHSBColor(hInterpolated, s, b).getRGB() & 0xFFFFFF;
    }

}
