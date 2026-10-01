package com.resourceful_refinement.content.glare.relay;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.resourceful_refinement.content.glare.GlareNodeBlock;
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

public class GlareRelayRenderer extends SafeBlockEntityRenderer<GlareRelayBlockEntity> {

    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "textures/block/glare/glare_relay.png");

    private final GlareRelayModel model;

    public GlareRelayRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new GlareRelayModel(context.bakeLayer(GlareRelayModel.LAYER_LOCATION));
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public void renderSafe(GlareRelayBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
                           int light, int overlay) {

        BlockState state = be.getBlockState();
        VertexConsumer casingBuffer = buffer.getBuffer(RenderType.entityCutout(TEXTURE));

        ms.pushPose();

        ms.translate(0.5, 1.5, 0.5);
        ms.scale(-1, -1, 1);


        // --- Render Block Model ---
        float time = be.getLevel().getGameTime() + partialTicks;
        float effectiveGameTime = time % (20*PRISM_ROTATE_DURATION);
        model.render(ms, casingBuffer, light, overlay);
        ms.popPose();

        // --- Prism Rendering ---
        SuperByteBuffer prism = CachedBuffers.partialFacing(ModPartialModels.GLARE_PRISM_SMALL, state, Direction.NORTH);

        prism.light(light);
        prism.rotateCentered((float) ((effectiveGameTime/(20*PRISM_ROTATE_DURATION)) * 2f * Math.PI), Direction.Axis.Y);
        prism.rotateCentered((float) ((effectiveGameTime/(20*PRISM_ROTATE_DURATION)) * 2f * Math.PI), Direction.Axis.Z);
        prism.color(be.getSyncedGlareNetworkSnapshot().overloaded() ?  ModPartialModels.PRISM_OFFLINE_COLOUR : ModPartialModels.PRISM_DEFAULT_COLOUR);
        prism.renderInto(ms, buffer.getBuffer(net.minecraft.client.renderer.Sheets.translucentCullBlockSheet()));

    }
}
