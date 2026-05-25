package com.resourceful_refinement.content.refinery.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class RefineryBaseModel {
    private final ModelPart root;

    public RefineryBaseModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition Base_Layer = partdefinition.addOrReplaceChild("Base Layer", CubeListBuilder.create().texOffs(0, 0).addBox(-23.0F, -15.0F, -23.0F, 46.0F, 15.0F, 46.0F, new CubeDeformation(0.0F))
                .texOffs(177, 62).addBox(10.0F, -16.01F, -24.0F, 14.0F, 16.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(178, 179).addBox(-7.0F, -15.0F, -24.0F, 14.0F, 14.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 183).addBox(-10.0F, -16.0F, -23.0F, 20.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(204, 156).addBox(-4.0F, -16.0F, -4.0F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(214, 121).addBox(-4.0F, -23.0F, -23.0F, 8.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition Edge_Rim_r1 = Base_Layer.addOrReplaceChild("Edge Rim_r1", CubeListBuilder.create().texOffs(0, 183).addBox(-10.0F, -1.0F, -1.0F, 20.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-22.0F, -15.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition Edge_Rim_r2 = Base_Layer.addOrReplaceChild("Edge_Rim_r2", CubeListBuilder.create().texOffs(0, 183).addBox(-10.0F, -1.0F, -1.0F, 20.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(22.0F, -15.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition Edge_Rim_r3 = Base_Layer.addOrReplaceChild("Edge_Rim_r3", CubeListBuilder.create().texOffs(0, 183).addBox(-10.0F, -1.0F, -1.0F, 20.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -15.0F, 22.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition Burner_Corner_r1 = Base_Layer.addOrReplaceChild("Burner Corner_r1", CubeListBuilder.create().texOffs(177, 62).addBox(-13.0F, -16.01F, -1.0F, 14.0F, 16.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-23.0F, 0.0F, 23.0F, -3.1416F, 0.0F, 3.1416F));

        PartDefinition Burner_Corner_r2 = Base_Layer.addOrReplaceChild("Burner_Corner_r2", CubeListBuilder.create().texOffs(177, 62).addBox(-13.0F, -16.01F, -1.0F, 14.0F, 16.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-23.0F, 0.0F, -23.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition Burner_Corner_r3 = Base_Layer.addOrReplaceChild("Burner_Corner_r3", CubeListBuilder.create().texOffs(177, 62).addBox(-13.0F, -16.01F, -1.0F, 14.0F, 16.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(23.0F, 0.0F, 23.0F, 0.0F, -1.5708F, 0.0F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    public void render(PoseStack poseStack, VertexConsumer buffer, int light, int overlay) {
        root.render(poseStack, buffer, light, overlay);
    }
}