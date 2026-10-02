package com.resourceful_refinement.content.resonator;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.glare.relay.GlareRelayBlockEntity;
import com.resourceful_refinement.content.glare.relay.GlareRelayModel;
import com.resourceful_refinement.registry.ModPartialModels;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import static com.resourceful_refinement.registry.ModPartialModels.PRISM_ROTATE_DURATION;

public class ResourceResonatorRenderer extends SafeBlockEntityRenderer<ResourceResonatorBlockEntity> {

    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "textures/block/resource_resonator.png");

    private final ResourceResonatorModel model;

    public ResourceResonatorRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new ResourceResonatorModel(context.bakeLayer(ResourceResonatorModel.LAYER_LOCATION));
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public void renderSafe(ResourceResonatorBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
                           int light, int overlay) {

        BlockState state = be.getBlockState();
        VertexConsumer casingBuffer = buffer.getBuffer(RenderType.entityCutout(TEXTURE));

        ms.pushPose();

        ms.translate(0.5, 1.5, 0.5);
        ms.scale(-1, -1, 1);


        // --- Render Block Model ---
        model.render(ms, casingBuffer, light, overlay);
        ms.popPose();

    }
}
