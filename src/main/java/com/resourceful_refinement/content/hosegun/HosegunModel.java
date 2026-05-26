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

    /** Full depth of the tank segment along local Z (model units, 1/16 m per unit). */
    public static final float TANK_DEPTH = 9.0F;

    /** Local Z of the rear of the tank; fluid scale is anchored here so the forward end shrinks. */
    public static final float TANK_BACK_Z = 4.5F;

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

        PartDefinition Paint_Cylinder = partdefinition.addOrReplaceChild("Paint_Cylinder", CubeListBuilder.create().texOffs(29, 16).addBox(-1.5F, -1.5F, -4.5F, 3.0F, 3.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, 21.5F, 6.5F, -0.1745F, 0.0F, 0.0F));

        PartDefinition paint_fill = Paint_Cylinder.addOrReplaceChild("paint_fill", CubeListBuilder.create().texOffs(0, 31).addBox(-1.5F, -1.0F, -8.5F, 2.5F, 2.5F, 8.5F, new CubeDeformation(0.0F)), PartPose.offset(0.25F, -0.25F, 4.25F));

        PartDefinition Paint_Cylinder2 = partdefinition.addOrReplaceChild("Paint_Cylinder2", CubeListBuilder.create().texOffs(29, 16).mirror().addBox(-1.5F, -1.5F, -4.5F, 3.0F, 3.0F, 9.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-4.0F, 21.5F, 6.5F, -0.1745F, 0.0F, 0.0F));

        PartDefinition paint_fill2 = Paint_Cylinder2.addOrReplaceChild("paint_fill2", CubeListBuilder.create().texOffs(0, 31).mirror().addBox(-1.0F, -1.0F, -8.5F, 2.5F, 2.5F, 8.5F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-0.25F, -0.25F, 4.25F));

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
        this.paintFillRight = root.getChild("Paint_Cylinder").getChild("paint_fill");
        this.paintFillLeft = root.getChild("Paint_Cylinder2").getChild("paint_fill2");
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
        tankPart.translateAndRotate(poseStack);
        poseStack.mulPose(Axis.XP.rotationDegrees(-10.0F));

        if (isleft)
        {
            poseStack.translate(0.0F, -0.075F, TANK_BACK_Z / 16.0F + 0.375F);
            poseStack.scale(1.0F, 1.0F, fillRatio);
            poseStack.translate(-0.235F, 1.385F, -TANK_BACK_Z / 16.0F + 0F);
        }
        else
        {
            poseStack.translate(0.0F, -0.075F, TANK_BACK_Z / 16.0F + 0.375F);
            poseStack.scale(1.0F, 1.0F, fillRatio);
            poseStack.translate(0.235F, 1.385F, -TANK_BACK_Z / 16.0F + 0F);
        }

        tankPart.render(poseStack, buffer, light, overlay, tintArgb);
        poseStack.popPose();
    }
}
