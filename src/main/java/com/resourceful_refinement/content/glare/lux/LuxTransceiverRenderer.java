package com.resourceful_refinement.content.glare.lux;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class LuxTransceiverRenderer implements BlockEntityRenderer<LuxTransceiverBlockEntity> {
    public LuxTransceiverRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(LuxTransceiverBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
    }
}
