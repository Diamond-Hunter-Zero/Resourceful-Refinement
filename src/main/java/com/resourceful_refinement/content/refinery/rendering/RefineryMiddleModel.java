package com.resourceful_refinement.content.refinery.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class RefineryMiddleModel {
    private final ModelPart root;

    public RefineryMiddleModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition Middle_Layer = partdefinition.addOrReplaceChild("Middle_Layer", CubeListBuilder.create().texOffs(45, 183).addBox(20.0F, -8.0F, -23.0F, 3.0F, 16.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(110, 201).addBox(-22.0F, -8.0F, 22.0F, 44.0F, 16.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(110, 201).addBox(-22.0F, -8.0F, -22.0F, 44.0F, 16.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 16.0F, 0.0F));

        PartDefinition Outer_Glass_Wall_r1 = Middle_Layer.addOrReplaceChild("Outer_Glass_Wall_r1", CubeListBuilder.create().texOffs(110, 201).addBox(-22.0F, -16.0F, 1.0F, 44.0F, 16.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-23.0F, 8.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition Inner_Glass_Wall_r1 = Middle_Layer.addOrReplaceChild("Inner_Glass_Wall_r1", CubeListBuilder.create().texOffs(110, 201).addBox(-22.0F, -16.0F, 1.0F, 44.0F, 16.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(21.0F, 8.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition Outer_Glass_Wall_r2 = Middle_Layer.addOrReplaceChild("Outer_Glass_Wall_r2", CubeListBuilder.create().texOffs(110, 201).addBox(-22.0F, -16.0F, 1.0F, 44.0F, 16.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 8.0F, 23.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition Inner_Glass_Wall_r2 = Middle_Layer.addOrReplaceChild("Inner_Glass_Wall_r2", CubeListBuilder.create().texOffs(110, 201).addBox(-22.0F, -16.0F, 1.0F, 44.0F, 16.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 8.0F, -21.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition Outer_Glass_Wall_r3 = Middle_Layer.addOrReplaceChild("Outer_Glass_Wall_r3", CubeListBuilder.create().texOffs(110, 201).addBox(-22.0F, -16.0F, 1.0F, 44.0F, 16.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(23.0F, 8.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition Inner_Glass_Wall_r3 = Middle_Layer.addOrReplaceChild("Inner_Glass_Wall_r3", CubeListBuilder.create().texOffs(110, 201).addBox(-22.0F, -16.0F, 1.0F, 44.0F, 16.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-21.0F, 8.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition Frame_Pillar_4_r1 = Middle_Layer.addOrReplaceChild("Frame_Pillar_4_r1", CubeListBuilder.create().texOffs(45, 183).addBox(-2.0F, -16.0F, -1.0F, 3.0F, 16.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(21.0F, 8.0F, 21.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition Frame_Pillar_3_r1 = Middle_Layer.addOrReplaceChild("Frame_Pillar_3_r1", CubeListBuilder.create().texOffs(45, 183).addBox(-2.0F, -16.0F, -1.0F, 3.0F, 16.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-22.0F, 8.0F, 22.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition Frame_Pillar_2_r1 = Middle_Layer.addOrReplaceChild("Frame_Pillar_2_r1", CubeListBuilder.create().texOffs(45, 183).addBox(-2.0F, -16.0F, -1.0F, 3.0F, 16.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-21.0F, 8.0F, -21.0F, 0.0F, -1.5708F, 0.0F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    public void render(PoseStack poseStack, VertexConsumer buffer, int light, int overlay) {
        root.render(poseStack, buffer, light, overlay);
    }
}
