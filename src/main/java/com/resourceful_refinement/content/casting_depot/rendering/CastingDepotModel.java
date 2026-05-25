package com.resourceful_refinement.content.casting_depot.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class CastingDepotModel {

	private final ModelPart root;

	public CastingDepotModel(ModelPart root) {
		this.root = root;
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 0).addBox(-16.0F, -11.0F, 0.0F, 16.0F, 11.0F, 16.0F, new CubeDeformation(0.0F))
                //.texOffs(0, 46).addBox(-15.05F, -13.0F, 0.95F, 14.1F, 2.0F, 14.1F, new CubeDeformation(0.0F))
                .texOffs(37, 28).addBox(-15.0F, -13.0F, 1.0F, 14.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(37, 34).addBox(-3.0F, -13.0F, 4.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 38).addBox(-4.0F, -13.0F, 4.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 38).addBox(-4.0F, -13.0F, 11.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 38).addBox(-13.0F, -13.0F, 11.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 38).addBox(-13.0F, -13.0F, 4.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 28).addBox(-13.0F, -12.0F, 4.0F, 10.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, 24.0F, -8.0F));

        PartDefinition Mould_Side_Edge_r1 = bone.addOrReplaceChild("Mould Side Edge_r1", CubeListBuilder.create().texOffs(37, 34).addBox(-1.0F, -1.0F, -4.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-14.0F, -12.0F, 8.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition Mould_Edge_r1 = bone.addOrReplaceChild("Mould Edge_r1", CubeListBuilder.create().texOffs(37, 28).addBox(-7.0F, -1.0F, -1.5F, 14.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.0F, -12.0F, 13.5F, 0.0F, 3.1416F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
	}

    public void render(PoseStack poseStack, VertexConsumer buffer, int light, int overlay) {
        root.render(poseStack, buffer, light, overlay);
    }
}