package com.resourceful_refinement.content.glare.terminal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;


public class TelemetryTerminalRenderer extends SmartBlockEntityRenderer<TelemetryTerminalBlockEntity> {

    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "textures/block/glare/telemetry_terminal.png");

    public static final ResourceLocation POWERED_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "textures/block/glare/telemetry_terminal_powered.png");

    private final TelemetryTerminalModel model;

    public TelemetryTerminalRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.model = new TelemetryTerminalModel(context.bakeLayer(TelemetryTerminalModel.LAYER_LOCATION));
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    @Override
    protected void renderSafe(TelemetryTerminalBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
                              int light, int overlay) {
        // Renders the value-box filter items (the assigned address items) for all behaviours.
        // Without this, the widget outlines still appear (drawn by Create's global ValueBoxHandler)
        // but the items inside them do not render.
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

        BlockState state = be.getBlockState();
        Direction facing = state.getValue(TelemetryTerminalBlock.FACING);
        VertexConsumer casingBuffer = buffer.getBuffer(RenderType.entityCutout(be.isPulsing() ? POWERED_TEXTURE : TEXTURE));

        ms.pushPose();
        ms.translate(0.5, 1.5, 0.5);
        ms.scale(-1, -1, 1);
        ms.mulPose(Axis.YP.rotationDegrees(facing.getOpposite().toYRot()));


        // --- Render Block Model ---
        int lightColour = 0xffdebb;
        if (be.isPulsing())
            lightColour = 0xc91f16;
        else if (be.isSendPulsing())
            lightColour = 0x81ae4d;

        model.render(ms, casingBuffer, light, overlay, lightColour);
        ms.popPose();
    }
}
