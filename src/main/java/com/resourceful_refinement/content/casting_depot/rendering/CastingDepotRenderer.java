package com.resourceful_refinement.content.casting_depot.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.casting_depot.CastingDepotBlockEntity;
import com.simibubi.create.content.logistics.depot.DepotRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.resources.ResourceLocation;

public class CastingDepotRenderer extends SafeBlockEntityRenderer<CastingDepotBlockEntity> {

    // Define the texture path for your depot
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "textures/block/casting_depot.png"
    );

    private final CastingDepotModel model;

    public CastingDepotRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new CastingDepotModel(context.bakeLayer(CastingDepotLayers.CASTING_DEPOT));
    }

    @Override
    protected void renderSafe(CastingDepotBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        BlockState state = be.getBlockState();

        // --- 1. Render the Custom Depot Model ---
        ms.pushPose();

        // standard Java Model translation: center at 0.5, 1.5, 0.5 and flip Y/Z
        ms.translate(0.5, 1.5, 0.5);
        ms.scale(1, -1, -1);

        // Rotate the model based on the block's orientation if it has a FACING property
        if (state.hasProperty(HorizontalDirectionalBlock.FACING)) {
            float angle = state.getValue(HorizontalDirectionalBlock.FACING).toYRot();
            ms.mulPose(Axis.YP.rotationDegrees(angle));
        }

        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutout(TEXTURE));
        model.render(ms, vc, light, overlay);

        ms.popPose();

        // --- 2. Render the items sitting on the depot ---
        // We call Create's standard DepotRenderer to handle the floating item logic
        ms.pushPose();
        ms.translate(0, -0.06, 0);
        DepotRenderer.renderItemsOf(be, partialTicks, ms, buffer, light, overlay, be.GetDepotBehaviour());
        ms.popPose();
    }

    @Override
    public int getViewDistance() {
        return 128;
    }
}