package com.resourceful_refinement.content.fracking_pump;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class FrackingPumpCounterweightModel {
    private final ModelPart root;

    public FrackingPumpCounterweightModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Counterweight = partdefinition.addOrReplaceChild("Counterweight", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition Counterweight_Qrt = Counterweight.addOrReplaceChild("Counterweight_Qrt", CubeListBuilder.create().texOffs(53, 54).addBox(-8.0F, -16.0F, -21.0F, 16.0F, 16.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Bottom_Orthoganal_Edge_r1 = Counterweight_Qrt.addOrReplaceChild("Bottom_Orthoganal_Edge_r1", CubeListBuilder.create().texOffs(49, 113).addBox(-8.0F, -9.0F, -5.0F, 16.0F, 9.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.5F, -17.4F, -0.7854F, 0.0F, 0.0F));

		PartDefinition Top_Orthoganal_Edge_r1 = Counterweight_Qrt.addOrReplaceChild("Top_Orthoganal_Edge_r1", CubeListBuilder.create().texOffs(112, 47).addBox(-8.0F, -9.0F, -5.0F, 16.0F, 9.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -12.5F, -17.4F, -0.7854F, 0.0F, 0.0F));

		PartDefinition Diagonal_Portion_r1 = Counterweight_Qrt.addOrReplaceChild("Diagonal Portion_r1", CubeListBuilder.create().texOffs(89, 21).addBox(-11.1F, -16.0F, 0.0F, 18.0F, 16.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(16.0F, 0.0F, -13.0F, 0.0F, -0.7854F, 0.0F));

		PartDefinition Bottom_Diagonal_Edge_r1 = Counterweight_Qrt.addOrReplaceChild("Bottom Diagonal Edge_r1", CubeListBuilder.create().texOffs(73, 143).addBox(-6.5F, -6.5F, -4.5F, 13.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.9393F, 1.6855F, -9.0607F, 0.7418F, -0.7854F, 0.0F));

		PartDefinition Top_Diagonal_Edge_r1 = Counterweight_Qrt.addOrReplaceChild("Top Diagonal Edge_r1", CubeListBuilder.create().texOffs(113, 67).addBox(-6.5F, -0.5F, -4.5F, 13.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.9393F, -17.7855F, -9.0607F, -0.7418F, -0.7854F, 0.0F));

		PartDefinition Counterweight_Qrt_2 = Counterweight.addOrReplaceChild("Counterweight Qrt 2", CubeListBuilder.create().texOffs(53, 54).addBox(-8.0F, -16.0F, -21.0F, 16.0F, 16.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition Bottom_Orthoganal_Edge_r2 = Counterweight_Qrt_2.addOrReplaceChild("Bottom_Orthoganal_Edge_r2", CubeListBuilder.create().texOffs(49, 113).addBox(-8.0F, -9.0F, -5.0F, 16.0F, 9.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.5F, -17.4F, -0.7854F, 0.0F, 0.0F));

		PartDefinition Top_Orthoganal_Edge_r2 = Counterweight_Qrt_2.addOrReplaceChild("Top_Orthoganal_Edge_r2", CubeListBuilder.create().texOffs(112, 47).addBox(-8.0F, -9.0F, -5.0F, 16.0F, 9.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -12.5F, -17.4F, -0.7854F, 0.0F, 0.0F));

		PartDefinition Diagonal_Portion_r2 = Counterweight_Qrt_2.addOrReplaceChild("Diagonal_Portion_r2", CubeListBuilder.create().texOffs(89, 21).addBox(-11.1F, -16.0F, 0.0F, 18.0F, 16.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(16.0F, 0.0F, -13.0F, 0.0F, -0.7854F, 0.0F));

		PartDefinition Bottom_Diagonal_Edge_r2 = Counterweight_Qrt_2.addOrReplaceChild("Bottom_Diagonal_Edge_r2", CubeListBuilder.create().texOffs(73, 143).addBox(-6.5F, -6.5F, -4.5F, 13.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.9393F, 1.6855F, -9.0607F, 0.7418F, -0.7854F, 0.0F));

		PartDefinition Top_Diagonal_Edge_r2 = Counterweight_Qrt_2.addOrReplaceChild("Top_Diagonal_Edge_r2", CubeListBuilder.create().texOffs(113, 67).addBox(-6.5F, -0.5F, -4.5F, 13.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.9393F, -17.7855F, -9.0607F, -0.7418F, -0.7854F, 0.0F));

		PartDefinition Counterweight_Qrt_3 = Counterweight.addOrReplaceChild("Counterweight Qrt 3", CubeListBuilder.create().texOffs(53, 54).addBox(-8.0F, -16.0F, -21.0F, 16.0F, 16.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition Bottom_Orthoganal_Edge_r3 = Counterweight_Qrt_3.addOrReplaceChild("Bottom_Orthoganal_Edge_r3", CubeListBuilder.create().texOffs(49, 113).addBox(-8.0F, -9.0F, -5.0F, 16.0F, 9.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.5F, -17.4F, -0.7854F, 0.0F, 0.0F));

		PartDefinition Top_Orthoganal_Edge_r3 = Counterweight_Qrt_3.addOrReplaceChild("Top_Orthoganal_Edge_r3", CubeListBuilder.create().texOffs(112, 47).addBox(-8.0F, -9.0F, -5.0F, 16.0F, 9.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -12.5F, -17.4F, -0.7854F, 0.0F, 0.0F));

		PartDefinition Diagonal_Portion_r3 = Counterweight_Qrt_3.addOrReplaceChild("Diagonal_Portion_r3", CubeListBuilder.create().texOffs(89, 21).addBox(-11.1F, -16.0F, 0.0F, 18.0F, 16.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(16.0F, 0.0F, -13.0F, 0.0F, -0.7854F, 0.0F));

		PartDefinition Bottom_Diagonal_Edge_r3 = Counterweight_Qrt_3.addOrReplaceChild("Bottom_Diagonal_Edge_r3", CubeListBuilder.create().texOffs(73, 143).addBox(-6.5F, -6.5F, -4.5F, 13.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.9393F, 1.6855F, -9.0607F, 0.7418F, -0.7854F, 0.0F));

		PartDefinition Top_Diagonal_Edge_r3 = Counterweight_Qrt_3.addOrReplaceChild("Top_Diagonal_Edge_r3", CubeListBuilder.create().texOffs(113, 67).addBox(-6.5F, -0.5F, -4.5F, 13.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.9393F, -17.7855F, -9.0607F, -0.7418F, -0.7854F, 0.0F));

		PartDefinition Counterweight_Qrt_4 = Counterweight.addOrReplaceChild("Counterweight Qrt 4", CubeListBuilder.create().texOffs(53, 54).addBox(-8.0F, -16.0F, -21.0F, 16.0F, 16.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition Bottom_Orthoganal_Edge_r4 = Counterweight_Qrt_4.addOrReplaceChild("Bottom_Orthoganal_Edge_r4", CubeListBuilder.create().texOffs(49, 113).addBox(-8.0F, -9.0F, -5.0F, 16.0F, 9.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.5F, -17.4F, -0.7854F, 0.0F, 0.0F));

		PartDefinition Top_Orthoganal_Edge_r4 = Counterweight_Qrt_4.addOrReplaceChild("Top_Orthoganal_Edge_r4", CubeListBuilder.create().texOffs(112, 47).addBox(-8.0F, -9.0F, -5.0F, 16.0F, 9.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -12.5F, -17.4F, -0.7854F, 0.0F, 0.0F));

		PartDefinition Diagonal_Portion_r4 = Counterweight_Qrt_4.addOrReplaceChild("Diagonal_Portion_r4", CubeListBuilder.create().texOffs(89, 21).addBox(-11.1F, -16.0F, 0.0F, 18.0F, 16.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(16.0F, 0.0F, -13.0F, 0.0F, -0.7854F, 0.0F));

		PartDefinition Bottom_Diagonal_Edge_r4 = Counterweight_Qrt_4.addOrReplaceChild("Bottom_Diagonal_Edge_r4", CubeListBuilder.create().texOffs(73, 143).addBox(-6.5F, -6.5F, -4.5F, 13.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.9393F, 1.6855F, -9.0607F, 0.7418F, -0.7854F, 0.0F));

		PartDefinition Top_Diagonal_Edge_r4 = Counterweight_Qrt_4.addOrReplaceChild("Top_Diagonal_Edge_r4", CubeListBuilder.create().texOffs(113, 67).addBox(-6.5F, -0.5F, -4.5F, 13.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.9393F, -17.7855F, -9.0607F, -0.7418F, -0.7854F, 0.0F));

		return LayerDefinition.create(meshdefinition, 256, 256);
    }

    public void render(PoseStack poseStack, VertexConsumer buffer, int light, int overlay) {
        root.render(poseStack, buffer, light, overlay);
    }
}
