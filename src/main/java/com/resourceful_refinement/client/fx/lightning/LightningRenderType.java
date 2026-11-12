package com.resourceful_refinement.client.fx.lightning;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

/**
 * Render type for procedural lightning: untextured {@code POSITION_COLOR} quads drawn with vanilla's lightning shader
 * and additive blending ({@code SRC_ALPHA, ONE}), so overlapping glow layers brighten towards white instead of
 * occluding each other. Depth-tested but never depth-written, so terrain in front hides the bolt while the bolt itself
 * never hides anything. Back-face culling is off because the ribbons are camera-facing strips whose winding flips.
 * <p>
 * The lightning shader also fades output by fog distance, so far-away bolts dissolve into fog instead of glowing
 * through it. Extends {@link RenderType} only to reach the protected state shards; it is never instantiated.
 */
public final class LightningRenderType extends RenderType {

    public static final RenderType LIGHTNING = create(
            "resourceful_refinement_lightning",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            8192,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_LIGHTNING_SHADER)
                    .setTransparencyState(LIGHTNING_TRANSPARENCY)
                    .setWriteMaskState(COLOR_WRITE)
                    .setCullState(NO_CULL)
                    .createCompositeState(false));

    private LightningRenderType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
            boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
        throw new UnsupportedOperationException("Constant holder only");
    }
}
