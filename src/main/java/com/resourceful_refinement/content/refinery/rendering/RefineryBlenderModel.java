package com.resourceful_refinement.content.refinery.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class RefineryBlenderModel {
    private final ModelPart root;

    public RefineryBlenderModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition Blender_Blade_Layer = partdefinition.addOrReplaceChild("Blender Blade Layer", CubeListBuilder.create().texOffs(133, 62).addBox(-2.0F, -8.8F, -2.0F, 4.0F, 16.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(202, 96).addBox(1.0F, -7.8F, 0.0F, 12.0F, 16.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(202, 96).mirror().addBox(-13.0F, -7.8F, 0.0F, 12.0F, 16.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 16.8F, 0.0F));

        PartDefinition Blade_2B_r1 = Blender_Blade_Layer.addOrReplaceChild("Blade 2B_r1", CubeListBuilder.create().texOffs(193, 113).mirror().addBox(-8.0F, -8.0F, 0.0F, 9.0F, 16.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-13.8F, 0.2F, -0.5F, 0.0F, -0.5236F, 0.0F));

        PartDefinition Blade_2_r1 = Blender_Blade_Layer.addOrReplaceChild("Blade 2_r1", CubeListBuilder.create().texOffs(193, 113).addBox(-1.0F, -8.0F, 0.0F, 9.0F, 16.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(13.8F, 0.2F, 0.5F, 0.0F, -0.5236F, 0.0F));

        /*PartDefinition bladeLayer = partdefinition.addOrReplaceChild("Blender Blade Layer",
                CubeListBuilder.create().texOffs(193, 94).addBox(-1.0F, -16.0F, -1.0F, 2.0F, 16.0F, 2.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        bladeLayer.addOrReplaceChild("Blade 2_r1", CubeListBuilder.create().texOffs(193, 113)
                        .addBox(-1.0F, -8.0F, 0.0F, 9.0F, 16.0F, 0.0F),
                PartPose.offsetAndRotation(13.4F, -8.0F, 3.3F, 0.0F, -0.5236F, 0.0F));*/

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    public void render(PoseStack poseStack, VertexConsumer buffer, int light, int overlay) {
        root.render(poseStack, buffer, light, overlay);
    }
}
