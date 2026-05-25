package com.resourceful_refinement.content.sieve;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class MechanicalSieveCasingBottomModel {

	private final ModelPart root;

	public MechanicalSieveCasingBottomModel(ModelPart root) {
		this.root = root;
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -2.0F, -8.0F, 16.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
		.texOffs(0, 35).addBox(-8.0F, -14.0F, -8.0F, 16.0F, 12.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(67, 45).addBox(-6.0F, -16.0F, -9.5F, 12.0F, 15.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition Casing_Base_r1 = bb_main.addOrReplaceChild("Casing_Base_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -2.0F, -8.0F, 16.0F, 2.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -16.0F, 0.0F, 0.0F, 0.0F, -3.1416F));
		
		return LayerDefinition.create(meshdefinition, 128, 128);
	}

    public void render(PoseStack poseStack, VertexConsumer buffer, int light, int overlay) {
        root.render(poseStack, buffer, light, overlay);
    }
}
