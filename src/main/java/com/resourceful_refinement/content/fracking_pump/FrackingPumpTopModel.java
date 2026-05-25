package com.resourceful_refinement.content.fracking_pump;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class FrackingPumpTopModel {
    private final ModelPart root;

    public FrackingPumpTopModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Top = partdefinition.addOrReplaceChild("Top", CubeListBuilder.create().texOffs(1, 164).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 8.0F, 16.0F, new CubeDeformation(0.0F))
		.texOffs(0, 40).addBox(-18.0F, -11.0F, -4.0F, 36.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition Strut_r1 = Top.addOrReplaceChild("Strut_r1", CubeListBuilder.create().texOffs(144, 22).mirror().addBox(-3.0F, 6.5F, -1.0F, 12.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-12.0F, -11.5F, 0.0F, 0.0F, 0.0F, 0.4363F));

		PartDefinition Strut_r2 = Top.addOrReplaceChild("Strut_r2", CubeListBuilder.create().texOffs(144, 22).addBox(-9.0F, 6.5F, -1.0F, 12.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(12.0F, -11.5F, 0.0F, 0.0F, 0.0F, -0.4363F));

		return LayerDefinition.create(meshdefinition, 256, 256);
    }

    public void render(PoseStack poseStack, VertexConsumer buffer, int light, int overlay) {
        root.render(poseStack, buffer, light, overlay);
    }
}
