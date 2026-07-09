package com.resourceful_refinement.content.glare.remote;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.logistics.depot.DepotRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class RemoteEntanglerDepotRenderer extends SmartBlockEntityRenderer<RemoteEntanglerDepotBlockEntity> {
    public RemoteEntanglerDepotRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override protected void renderSafe(RemoteEntanglerDepotBlockEntity be, float partialTicks, PoseStack pose,
            MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, pose, buffer, light, overlay);
        DepotRenderer.renderItemsOf(be, partialTicks, pose, buffer, light, overlay, be.getDepotBehaviour());
    }
}
