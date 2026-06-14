package com.resourceful_refinement.content.milking_station;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.resourceful_refinement.ResourcefulRefinementMain;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MilkingStationRenderer extends SafeBlockEntityRenderer<MilkingStationBlockEntity> {

    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "textures/block/milking_station.png");

    private final MilkingStationModel model;

    public MilkingStationRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new MilkingStationModel(context.bakeLayer(MilkingStationModel.LAYER_LOCATION));
    }

    @Override
    protected void renderSafe(MilkingStationBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
            int light, int overlay) {
        BlockState state = be.getBlockState();
        Direction facing = state.getValue(MilkingStationBlock.FACING);
        float speed = Math.abs(be.getSpeed());
        float time = be.getLevel() == null ? 0 : be.getLevel().getGameTime() + partialTicks;
        float armAngle = speed == 0 ? 0 : Mth.sin(time * speed / 24f) * 0.2f;

        model.animateArm(armAngle);

        VertexConsumer casingBuffer = buffer.getBuffer(RenderType.entityCutout(TEXTURE));

        ms.pushPose();
        ms.translate(0.5, 1.5, 0.5);
        ms.scale(1, -1, -1);
        ms.mulPose(Axis.YP.rotationDegrees(facing.toYRot()));
        model.render(ms, casingBuffer, light, overlay);
        ms.popPose();

        renderCapturedEntity(be, partialTicks, ms, buffer, light, facing);
    }

    private void renderCapturedEntity(MilkingStationBlockEntity be, float partialTicks, PoseStack ms,
            MultiBufferSource buffer, int light, Direction facing) {
        Entity entity = be.getPreviewEntity();
        if (entity == null) {
            return;
        }

        float yaw = facing.toYRot() + 180f;
        entity.yRotO = yaw;
        entity.xRotO = 0;
        entity.setYRot(yaw);
        entity.setYHeadRot(yaw);
        entity.setYBodyRot(yaw);
        entity.setXRot(0);

        if (entity instanceof LivingEntity living) {
            living.yHeadRotO = yaw;
            living.yBodyRotO = yaw;
            living.yHeadRot = yaw;
            living.yBodyRot = yaw;
            living.walkAnimation.setSpeed(0);
            living.walkAnimation.update(0, 1);
        }

        ms.pushPose();
        ms.translate(0.5, 1, 0.5);

        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        dispatcher.render(entity, 0, 0, 0, yaw, partialTicks, ms, buffer, light);
        ms.popPose();
    }

    @Override
    public int getViewDistance() {
        return 128;
    }
}
