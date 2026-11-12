package com.resourceful_refinement.content.railgun;

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
 * Railgun item model (Blockbench export, 128x128 texture). The core drum and the barrel spin about the shared bore
 * axis while spooling; everything else is static. Rendered by {@link RailgunItemRenderer}.
 */
public class RailgunModel {

    /** Model-space (pixel) height of the bore axis that the drum and barrel spin around. */
    public static final float BORE_AXIS_Y = 21.0F;

    /** Model-space (pixel) position of the muzzle: the front of the barrel, on the bore axis. */
    public static final float MUZZLE_X = 0.0F;
    public static final float MUZZLE_Y = BORE_AXIS_Y;
    public static final float MUZZLE_Z = -16.0F;

    private final ModelPart coreDrum;
    private final ModelPart handle;
    private final ModelPart sideArmamentsLeft;
    private final ModelPart sideArmamentsRight;
    private final ModelPart barrel;
    private final ModelPart body;

    public RailgunModel(ModelPart root) {
        this.coreDrum = root.getChild("core_drum");
        this.handle = root.getChild("handle");
        this.sideArmamentsLeft = root.getChild("side_arments_left");
        this.sideArmamentsRight = root.getChild("side_arments_right");
        this.barrel = root.getChild("barrel");
        this.body = root.getChild("bb_main");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition core_drum = partdefinition.addOrReplaceChild("core_drum", CubeListBuilder.create().texOffs(23, 40).addBox(-3.5F, -1.0F, -1.0F, 7.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 52).addBox(-1.0F, -3.5F, -1.0F, 2.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 21.0F, -1.0F));

        core_drum.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(54, 12).addBox(-1.0F, -3.5F, -2.0F, 2.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, 0.0F, 0.0F, -0.7854F));

        core_drum.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(54, 0).addBox(-1.0F, -3.5F, -2.0F, 2.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition handle = partdefinition.addOrReplaceChild("handle", CubeListBuilder.create(), PartPose.offset(3.5F, 20.5F, 5.0F));

        handle.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(28, 62).addBox(-7.5F, -5.5F, 0.0F, 1.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(50, 58).addBox(-0.5F, -5.5F, 0.0F, 1.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3491F, 0.0F, 0.0F));

        handle.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(50, 37).addBox(-3.0F, -0.5F, -0.5F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.5F, -4.5706F, -0.5994F, -0.2618F, 0.0F, 0.0F));

        PartDefinition side_arments_left = partdefinition.addOrReplaceChild("side_arments_left", CubeListBuilder.create(), PartPose.offset(5.3157F, 21.0F, 1.0652F));

        side_arments_left.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(40, 49).addBox(-1.5F, -1.5F, -3.5F, 3.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.8157F, 1.5F, 7.4348F, 0.0F, 0.0F, -0.2618F));

        side_arments_left.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(27, 28).addBox(3.0F, -1.0F, -8.0F, 2.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.3157F, 0.0F, 1.9348F, 0.0F, -0.4363F, 0.0F));

        side_arments_left.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(28, 56).addBox(2.5F, -0.5F, -4.4F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.1345F, 0.0F));

        PartDefinition side_arments_right = partdefinition.addOrReplaceChild("side_arments_right", CubeListBuilder.create(), PartPose.offset(-5.3157F, 21.0F, 1.0652F));

        side_arments_right.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(50, 28).addBox(-1.5F, -1.5F, -3.5F, 3.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.8157F, 1.5F, 7.4348F, 0.0F, 0.0F, 0.2618F));

        side_arments_right.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(0, 40).addBox(-5.0F, -1.0F, -8.0F, 2.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.3157F, 0.0F, 1.9348F, 0.0F, 0.4363F, 0.0F));

        side_arments_right.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(39, 58).addBox(-3.5F, -0.5F, -4.4F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.1345F, 0.0F));

        partdefinition.addOrReplaceChild("barrel", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.0F, -11.0F, 1.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(0, 14).addBox(-3.0F, 0.0F, -11.0F, 1.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(13, 52).addBox(-3.0F, -2.0F, -11.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(57, 57).addBox(-2.0F, -3.0F, -1.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(18, 52).addBox(2.0F, -2.0F, -11.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(27, 0).addBox(2.0F, 0.0F, -11.0F, 1.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(27, 14).addBox(2.0F, -3.0F, -11.0F, 1.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(13, 56).addBox(-1.0F, -4.0F, -4.0F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(57, 49).addBox(-1.0F, 0.0F, -4.0F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 22.0F, -5.0F));

        partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(46, 40).addBox(-3.0F, -6.0F, -4.0F, 6.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(23, 47).addBox(-3.0F, -6.0F, 2.0F, 6.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 28).addBox(-3.0F, -5.0F, 4.0F, 6.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    /**
     * @param drumLight   packed light for the core drum (brightened while spooled so it glows)
     * @param drumAngle   drum spin about the bore axis, in degrees
     * @param barrelAngle barrel spin about the bore axis, in degrees
     */
    public void render(PoseStack poseStack, VertexConsumer buffer, int light, int overlay,
                       int drumLight, float drumAngle, float barrelAngle) {
        body.render(poseStack, buffer, light, overlay);
        handle.render(poseStack, buffer, light, overlay);
        sideArmamentsLeft.render(poseStack, buffer, light, overlay);
        sideArmamentsRight.render(poseStack, buffer, light, overlay);
        renderSpun(coreDrum, poseStack, buffer, drumLight, overlay, drumAngle);
        renderSpun(barrel, poseStack, buffer, light, overlay, barrelAngle);
    }

    /** Spins a part about the bore axis rather than its own pivot (the barrel's pivot sits 1px below the axis). */
    private static void renderSpun(ModelPart part, PoseStack poseStack, VertexConsumer buffer, int light, int overlay,
                                   float angleDegrees) {
        if (angleDegrees == 0.0F) {
            part.render(poseStack, buffer, light, overlay);
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0F, BORE_AXIS_Y / 16.0F, 0.0F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(angleDegrees));
        poseStack.translate(0.0F, -BORE_AXIS_Y / 16.0F, 0.0F);
        part.render(poseStack, buffer, light, overlay);
        poseStack.popPose();
    }
}
