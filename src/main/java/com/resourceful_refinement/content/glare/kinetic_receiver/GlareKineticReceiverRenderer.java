package com.resourceful_refinement.content.glare.kinetic_receiver;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.resourceful_refinement.ResourcefulRefinementMain;
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
import static com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer.getAngleForBe;
import static com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer.kineticRotationTransform;

public class GlareKineticReceiverRenderer extends SafeBlockEntityRenderer<GlareKineticReceiverBlockEntity> {

    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "textures/block/glare/glare_relay.png");

    private final GlareKineticReceiverModel model;

    public GlareKineticReceiverRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new GlareKineticReceiverModel(context.bakeLayer(GlareKineticReceiverModel.LAYER_LOCATION));
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    protected void renderSafe(GlareKineticReceiverBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
                              int light, int overlay) {
        BlockState state = be.getBlockState();
        Direction facing = state.getValue(GlareKineticReceiverBlock.FACING);
        float speed = Math.abs(be.getSpeed());
        VertexConsumer casingBuffer = buffer.getBuffer(RenderType.entityCutout(TEXTURE));

        ms.pushPose();

        if (facing.getAxis().isVertical())
        {
            boolean isUp = facing == Direction.UP;
            ms.translate(0.5, 0.5, isUp? 1.5 : -0.5);
            ms.scale(1, -1, -1);
            ms.mulPose(Axis.XP.rotationDegrees(isUp? 90 : -90));
        }
        else
        {
            ms.translate(0.5, 1.5, 0.5);
            ms.scale(-1, -1, 1);
            ms.mulPose(Axis.YP.rotationDegrees(facing.toYRot()));
        }


        // --- Render Block Model ---
        model.render(ms, casingBuffer, light, overlay);
        ms.popPose();

        // --- Prism Rendering ---
        float effectiveGameTime = (be.getLevel().getGameTime() + partialTicks) % (20*PRISM_ROTATE_DURATION);
        SuperByteBuffer prism = CachedBuffers.partialFacing(ModPartialModels.GLARE_PRISM_SMALL, state);

        prism.light(light);
        prism.rotateCentered((float) ((effectiveGameTime/(20*PRISM_ROTATE_DURATION)) * 2f * Math.PI), Direction.Axis.Y);
        prism.rotateCentered((float) ((effectiveGameTime/(20*PRISM_ROTATE_DURATION)) * 2f * Math.PI), Direction.Axis.Z);
        prism.color(be.isReceiverPowered() ? ModPartialModels.PRISM_DEFAULT_COLOUR : ModPartialModels.PRISM_OFFLINE_COLOUR);
        prism.renderInto(ms, buffer.getBuffer(net.minecraft.client.renderer.Sheets.translucentCullBlockSheet()));

        // --- Shaft Kinetic Rendering ---
        SuperByteBuffer cog = CachedBuffers.partialFacing(ModPartialModels.GLARE_COG_SHAFT, state, facing);
        kineticRotationTransform(cog, be, facing.getAxis(),
                getAngleForBe(be, be.getBlockPos(), facing.getAxis()), light)
                .renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));
    }

}
