package com.resourceful_refinement.content.fracking_pump;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class FrackingPumpOutletModel {
    private final ModelPart root;

    public FrackingPumpOutletModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Base = partdefinition.addOrReplaceChild("Base", CubeListBuilder.create().texOffs(53, 84).addBox(-7.0F, -16.0F, -6.0F, 14.0F, 16.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(115, 142).addBox(-8.0F, -16.0F, -8.0F, 16.0F, 16.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition Front_Face_r1 = Base.addOrReplaceChild("Front Face_r1", CubeListBuilder.create().texOffs(152, 142).addBox(-8.0F, -16.0F, -1.0F, 16.0F, 16.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 7.0F, 0.0F, 3.1416F, 0.0F));

		return LayerDefinition.create(meshdefinition, 256, 256);
    }

    public void render(PoseStack poseStack, VertexConsumer buffer, int light, int overlay) {
        root.render(poseStack, buffer, light, overlay);
    }
}
