package com.resourceful_refinement.content.fracking_pump;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class FrackingPumpShaftModel {
    private final ModelPart root;

    public FrackingPumpShaftModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Shaft = partdefinition.addOrReplaceChild("Shaft", CubeListBuilder.create().texOffs(0, 99).addBox(-6.0F, -16.0F, -6.0F, 12.0F, 16.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(41, 132).addBox(6.0F, -16.0F, -7.0F, 1.0F, 16.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition Shaft_Ridge_r1 = Shaft.addOrReplaceChild("Shaft Ridge_r1", CubeListBuilder.create().texOffs(41, 132).addBox(-1.0F, -16.0F, -7.0F, 1.0F, 16.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.0F, 0.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

		return LayerDefinition.create(meshdefinition, 256, 256);
    }

    public void render(PoseStack poseStack, VertexConsumer buffer, int light, int overlay) {
        root.render(poseStack, buffer, light, overlay);
    }
}
