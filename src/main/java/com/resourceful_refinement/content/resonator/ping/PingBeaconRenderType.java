package com.resourceful_refinement.content.resonator.ping;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * A render type for the Ping Beacon beam: vanilla's beacon-beam setup (same shader, texture, translucency and
 * depth test) with back-face culling disabled so the square column renders from any angle. The beam is depth-tested,
 * so terrain in front of it occludes it exactly like a real beacon beam. Extends {@link RenderType} only to reach
 * the protected state shards and {@code create}; it is never instantiated.
 */
public final class PingBeaconRenderType extends RenderType {

    public static final ResourceLocation BEAM_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/entity/beacon_beam.png");

    public static final RenderType PING_BEAM = create(
            "resourceful_refinement_ping_beacon_beam",
            DefaultVertexFormat.BLOCK,
            VertexFormat.Mode.QUADS,
            1536,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_BEACON_BEAM_SHADER)
                    .setTextureState(new TextureStateShard(BEAM_TEXTURE, false, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setWriteMaskState(COLOR_WRITE)
                    .setCullState(NO_CULL)
                    .createCompositeState(false));

    private PingBeaconRenderType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
            boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
        throw new UnsupportedOperationException("Constant holder only");
    }
}
