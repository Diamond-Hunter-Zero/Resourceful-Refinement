package com.resourceful_refinement.content.sieve;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class MechanicalSieveCogModel {

	private final ModelPart root;

	public MechanicalSieveCogModel(ModelPart root) {
		this.root = root;
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition Cog = partdefinition.addOrReplaceChild("Cog", CubeListBuilder.create(), PartPose.offset(0.0F, 22.0F, 0.0F));

        PartDefinition cogwheel = Cog.addOrReplaceChild("cogwheel", CubeListBuilder.create().texOffs(0, 50).addBox(-1.5F, -9.0F, -2.0F, 3.0F, 18.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 19).addBox(-9.0F, -1.5F, -2.0F, 18.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition cube_r1 = cogwheel.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 27).addBox(-9.0F, -1.5F, -5.0F, 18.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(15, 50).addBox(-1.5F, -9.0F, -5.0F, 3.0F, 18.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition group = Cog.addOrReplaceChild("group", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition cube_r2 = group.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(49, 62).addBox(-2.5F, -7.1F, -4.95F, 5.0F, 14.2F, 3.9F, new CubeDeformation(0.0F))
                .texOffs(37, 35).addBox(-7.1F, -2.5F, -4.95F, 14.2F, 5.0F, 3.9F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.0F, 0.0F, -0.3927F));

        PartDefinition cube_r3 = group.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(45, 19).addBox(-7.1F, -2.5F, -4.95F, 14.2F, 5.0F, 3.9F, new CubeDeformation(0.0F))
                .texOffs(30, 62).addBox(-2.5F, -7.1F, -4.95F, 5.0F, 14.2F, 3.9F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.0F, 0.0F, 0.3927F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

    public void render(PoseStack poseStack, VertexConsumer buffer, int light, int overlay) {
        root.render(poseStack, buffer, light, overlay);
    }
}