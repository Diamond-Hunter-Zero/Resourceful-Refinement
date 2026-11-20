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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
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

    /** Cell model height is 10px; at this scale it fills the core cavity between the pillars (block px 4..10). */
    private static final float CELL_SCALE = 0.6F;
    private static final float CAVITY_FLOOR = 4F / 16F;

    /** Draws the installed Prism Cell's own item model upright in the core cavity, seated like a battery. */
    private static void renderInstalledCell(ResourceResonatorBlockEntity be, PoseStack ms, MultiBufferSource buffer,
                                            int light, int overlay) {
        var cell = be.getPrismCell();
        if (cell == null) return;
        ms.pushPose();
        // ItemDisplayContext.NONE centres the 16px model on the origin, so lift by half the scaled block.
        ms.translate(0.5F, CAVITY_FLOOR + 0.5F * CELL_SCALE, 0.5F);
        ms.scale(CELL_SCALE, CELL_SCALE, CELL_SCALE);
        Minecraft.getInstance().getItemRenderer().renderStatic(cell.toItemStack(), ItemDisplayContext.NONE,
                light, overlay, ms, buffer, be.getLevel(), (int) be.getBlockPos().asLong());
        ms.popPose();
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

        renderInstalledCell(be, ms, buffer, light, overlay);

    }
}
