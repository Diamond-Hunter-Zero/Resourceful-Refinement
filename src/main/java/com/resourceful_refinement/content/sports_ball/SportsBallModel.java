package com.resourceful_refinement.content.sports_ball;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;


public class SportsBallModel {

    public SportsBallModel(ModelPart root) {
        this.bb_main = root.getChild("bb_main");
    }

    private final ModelPart bb_main;

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition partdefinition = mesh.getRoot();

        PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -10.0F, -4.5F, 9.0F, 9.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(0, 37).addBox(-3.5F, -9.0F, -5.5F, 7.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(37, 0).addBox(-3.5F, -9.0F, 4.5F, 7.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(29, 19).addBox(4.5F, -9.0F, -3.5F, 1.0F, 7.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(29, 34).addBox(-5.5F, -9.0F, -3.5F, 1.0F, 7.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(0, 19).addBox(-3.5F, -11.0F, -3.5F, 7.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(0, 28).addBox(-3.5F, -1.0F, -3.5F, 7.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    public void render(PoseStack poseStack, VertexConsumer buffer, int light, int overlay) {
        bb_main.render(poseStack, buffer, light, overlay);
    }
}
