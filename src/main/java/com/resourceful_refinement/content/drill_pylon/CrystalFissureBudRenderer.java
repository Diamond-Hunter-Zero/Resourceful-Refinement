package com.resourceful_refinement.content.drill_pylon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.resourceful_refinement.content.drill_pylon.tint.FissureTintProfile;
import com.resourceful_refinement.content.drill_pylon.tint.FissureTintProfiles;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Renders the Crystal Fissure Bud every frame with a whole-block tint that lerps through its
 * {@link FissureTintProfile} using client game-time. The block is {@code ENTITYBLOCK_ANIMATED} so the
 * static chunk mesh does not draw it; this renderer draws the baked model instead, multiplying its
 * (all tint-indexed) faces by the current cycle colour.
 */
public class CrystalFissureBudRenderer implements BlockEntityRenderer<CrystalFissureBudBlockEntity> {
    private final BlockRenderDispatcher blockRenderer;

    public CrystalFissureBudRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(CrystalFissureBudBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Level level = be.getLevel();
        if (level == null) {
            return;
        }

        BlockState state = be.getBlockState();
        BakedModel model = blockRenderer.getBlockModel(state);

        FissureTintProfile profile = FissureTintProfiles.get(be.getSourceItem(), level.registryAccess());
        int color = profile.colorAt(level.getGameTime() + partialTick);
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;

        // Use translucent explicitly: ItemBlockRenderTypes.getRenderType(...) returns the block's
        // chunk-render-layer map value (SOLID by default here), which renders opaque. The model's JSON
        // render_type only drives chunk rendering, which ENTITYBLOCK_ANIMATED skips, so the BER must
        // pick the blending render type itself for the tintable texture's alpha to show.
        VertexConsumer consumer = buffer.getBuffer(RenderType.translucent());
        blockRenderer.getModelRenderer().renderModel(
                pose.last(), consumer, state, model, r, g, b, packedLight, packedOverlay);
    }
}
