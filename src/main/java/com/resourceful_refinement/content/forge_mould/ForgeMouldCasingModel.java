package com.resourceful_refinement.content.forge_mould;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class ForgeMouldCasingModel {
    private final ModelPart root;

    public ForgeMouldCasingModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Frame = partdefinition.addOrReplaceChild("Frame", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -16.0F, -6.0F, 14.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(0, 40).addBox(-8.0F, -16.0F, -8.0F, 16.0F, 16.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(37, 40).addBox(-8.0F, -16.0F, 6.0F, 16.0F, 16.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 61).addBox(7.0F, -4.0F, -6.0F, 1.0F, 4.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(27, 61).addBox(7.0F, -16.0F, -6.0F, 1.0F, 4.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(0, 61).mirror().addBox(-8.0F, -4.0F, -6.0F, 1.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(27, 61).mirror().addBox(-8.0F, -16.0F, -6.0F, 1.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    public void render(PoseStack poseStack, VertexConsumer buffer, int light, int overlay) {
        root.render(poseStack, buffer, light, overlay);
    }
}
