package com.resourceful_refinement.content.forge_mould;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class ForgeMouldPressModel {
    private final ModelPart root;

    public ForgeMouldPressModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Mould = partdefinition.addOrReplaceChild("Mould", CubeListBuilder.create().texOffs(0, 27).addBox(-7.005F, -2.0F, -5.0F, 14.01F, 2.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(49, 27).addBox(-6.995F, -5.0F, -4.0F, 13.99F, 3.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition Slant_r1 = Mould.addOrReplaceChild("Slant_r1", CubeListBuilder.create().texOffs(53, 17).addBox(-8.0F, -4.0F, 0.0F, 14.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -0.75F, 4.3F, 0.3054F, 0.0F, 0.0F));

		PartDefinition Slant_r2 = Mould.addOrReplaceChild("Slant_r2", CubeListBuilder.create().texOffs(53, 17).addBox(-8.0F, -4.0F, -1.0F, 14.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -0.75F, -4.3F, -0.3054F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    public void render(PoseStack poseStack, VertexConsumer buffer, int light, int overlay) {
        root.render(poseStack, buffer, light, overlay);
    }
}
