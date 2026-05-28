package com.resourceful_refinement.content.hosegun;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Hosegun item model. {@code Paint_Cylinder*} parts are placeholders — fluid tanks are drawn separately
 * as tinted, fill-scaled volumes in {@link HosegunItemRenderer}.
 */
public class HosegunModel {

    /** Scaling factor for fluid boxes as stored fluid amount changes. */
    public static final float TANK_FILL_SCALE = 0.666F;

    /** Local Z of the rear of the tank; fluid scale is anchored here so the forward end shrinks. */
    public static final float TANK_BACK_Z = 0.225F;

    private final ModelPart handle;
    private final ModelPart paintCylinderRight;
    private final ModelPart paintCylinderLeft;
    private final ModelPart paintFillRight;
    private final ModelPart paintFillLeft;
    private final ModelPart mainBody;

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition Handle = partdefinition.addOrReplaceChild("Handle", CubeListBuilder.create().texOffs(31, 13).addBox(-3.0F, -1.0F, 5.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(48, 51).addBox(-4.0F, -1.0F, -1.0F, 1.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(21, 53).addBox(3.0F, -1.0F, -1.0F, 1.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 19.0F, 5.0F, 0.2618F, 0.0F, 0.0F));

        PartDefinition paint_fill = partdefinition.addOrReplaceChild("paint_fill", CubeListBuilder.create().texOffs(0, 55).addBox(-1.25F, -1.25F, -0.05F, 2.5F, 2.5F, 8.5F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, 21.5F, 2.3F));

        PartDefinition Paint_Cylinder = partdefinition.addOrReplaceChild("Paint_Cylinder", CubeListBuilder.create().texOffs(29, 16).addBox(-1.5F, -1.5F, -4.5F, 3.0F, 3.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, 21.5F, 6.5F, -0.1745F, 0.0F, 0.0F));

        PartDefinition paint_fill2 = partdefinition.addOrReplaceChild("paint_fill2", CubeListBuilder.create().texOffs(0, 55).mirror().addBox(-9.25F, -1.25F, -0.05F, 2.5F, 2.5F, 8.5F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(4.0F, 21.5F, 2.3F));

        PartDefinition Paint_Cylinder2 = partdefinition.addOrReplaceChild("Paint_Cylinder2", CubeListBuilder.create().texOffs(29, 16).mirror().addBox(-1.5F, -1.5F, -4.5F, 3.0F, 3.0F, 9.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-4.0F, 21.5F, 6.5F, -0.1745F, 0.0F, 0.0F));

        PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(48, 42).addBox(-3.0F, -6.0F, -9.0F, 6.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 44).addBox(-2.0F, -5.0F, -7.0F, 4.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 16).addBox(-3.0F, -6.0F, -1.0F, 6.0F, 6.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(25, 42).addBox(-1.5F, -7.0F, -2.0F, 3.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(54, 13).addBox(2.0F, -4.0F, -5.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition cube_r1 = bb_main.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-2.895F, -3.0F, -4.5F, 5.99F, 6.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1F, -1.5F, 10.0F, -0.3927F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    public HosegunModel(ModelPart root) {
        this.handle = root.getChild("Handle");
        this.paintCylinderRight = root.getChild("Paint_Cylinder");
        this.paintCylinderLeft = root.getChild("Paint_Cylinder2");
        this.paintFillRight = root.getChild("paint_fill");
        this.paintFillLeft = root.getChild("paint_fill2");
        this.mainBody = root.getChild("bb_main");
    }

    public void renderBody(PoseStack poseStack, VertexConsumer buffer, int light, int overlay) {
        handle.render(poseStack, buffer, light, overlay);
        mainBody.render(poseStack, buffer, light, overlay);
        paintCylinderRight.render(poseStack, buffer, light, overlay);
        paintCylinderLeft.render(poseStack, buffer, light, overlay);
    }

    public void renderFluidTanks(PoseStack poseStack, VertexConsumer buffer, int light, int overlay, float fillRatio, int tintArgb) {
        if (fillRatio <= 0.0F) {
            return;
        }
        renderScaledTank(poseStack, buffer, light, overlay, paintFillRight, false, fillRatio, tintArgb);
        renderScaledTank(poseStack, buffer, light, overlay, paintFillLeft, true, fillRatio, tintArgb);
    }

    private static void renderScaledTank(
            PoseStack poseStack,
            VertexConsumer buffer,
            int light,
            int overlay,
            ModelPart tankPart,
            boolean isleft,
            float fillRatio,
            int tintArgb
    ) {
        poseStack.pushPose();

        poseStack.mulPose(Axis.XP.rotationDegrees(-10));

        poseStack.translate(0, -0.085F, TANK_BACK_Z + (1f - fillRatio)*TANK_FILL_SCALE);
        poseStack.scale(1.0F, 1.0F, fillRatio);

        tankPart.render(poseStack, buffer, light, overlay, tintArgb);
        poseStack.popPose();
    }
}
