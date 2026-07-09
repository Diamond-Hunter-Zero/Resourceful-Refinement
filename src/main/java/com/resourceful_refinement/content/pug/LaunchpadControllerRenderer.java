package com.resourceful_refinement.content.pug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.resourceful_refinement.content.glare.remote.RemoteEntanglerDepotBlockEntity;
import com.simibubi.create.content.logistics.depot.DepotRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class LaunchpadControllerRenderer extends SmartBlockEntityRenderer<LaunchpadControllerBlockEntity> {
    public LaunchpadControllerRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override protected void renderSafe(LaunchpadControllerBlockEntity be, float partialTicks, PoseStack pose,
            MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, pose, buffer, light, overlay);
    }
}
