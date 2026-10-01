package com.resourceful_refinement.content.mineral_deposit;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.resourceful_refinement.ResourcefulRefinementMain;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class MineralDepositModel extends HierarchicalModel<Entity> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "mineral_deposit"),
            "main");

    private final ModelPart root;

    private final ModelPart top_face;
    private final ModelPart east_face;
    private final ModelPart west_face;
    private final ModelPart south_face;
    private final ModelPart north_face;

	public MineralDepositModel(ModelPart root) {

        this.root = root;

        this.top_face = root.getChild("top_face");
        this.east_face = root.getChild("east_face");
        this.west_face = root.getChild("west_face");
        this.south_face = root.getChild("south_face");
        this.north_face = root.getChild("north_face");
	}

    @Override
    public ModelPart root() {
        return root;
    }

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition top_face = partdefinition.addOrReplaceChild("top_face", CubeListBuilder.create(), PartPose.offset(0.0F, 8.0F, 0.0F));

        PartDefinition small_chip_c_r1 = top_face.addOrReplaceChild("small_chip_c_r1", CubeListBuilder.create().texOffs(6, 11).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.0F, 0.0F, -6.0F, -0.1892F, -1.0428F, 0.0357F));

        PartDefinition small_chip_b_r1 = top_face.addOrReplaceChild("small_chip_b_r1", CubeListBuilder.create().texOffs(1, 7).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0F, 13.0F, -3.0F, 1.6689F, -1.3101F, -1.794F));

        PartDefinition small_chip_b_r2 = top_face.addOrReplaceChild("small_chip_b_r2", CubeListBuilder.create().texOffs(1, 7).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.0F, 13.0F, 3.0F, 1.6689F, -1.3101F, -1.794F));

        PartDefinition small_chip_b_r3 = top_face.addOrReplaceChild("small_chip_b_r3", CubeListBuilder.create().texOffs(1, 7).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, 0.0F, -5.0F, 2.7011F, 1.3617F, 2.3715F));

        PartDefinition small_chip_a_r1 = top_face.addOrReplaceChild("small_chip_a_r1", CubeListBuilder.create().texOffs(8, 1).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, 0.0F, 5.0F, -0.0465F, -0.2577F, 0.1806F));

        PartDefinition medium_chip_b_r1 = top_face.addOrReplaceChild("medium_chip_b_r1", CubeListBuilder.create().texOffs(2, 9).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3379F, 0.6505F, 2.0833F, -0.2002F, 0.8735F, -0.0897F));

        PartDefinition chip_b_r1 = top_face.addOrReplaceChild("chip_b_r1", CubeListBuilder.create().texOffs(0, 8).addBox(-1.0F, -4.0F, -3.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 3.5F, -2.0F, -0.0873F, -0.3491F, -0.6109F));

        PartDefinition chip_a_r1 = top_face.addOrReplaceChild("chip_a_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -4.0F, -3.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5F, 3.5F, 3.5F, -1.0472F, 0.2618F, -0.4363F));

        PartDefinition east_face = partdefinition.addOrReplaceChild("east_face", CubeListBuilder.create(), PartPose.offset(-8.0F, 24.0F, 0.0F));

        PartDefinition small_chip_a_r2 = east_face.addOrReplaceChild("small_chip_a_r2", CubeListBuilder.create().texOffs(8, 1).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -11.5F, -1.0F, -1.7413F, 0.2195F, -3.1261F));

        PartDefinition small_chip_a_r3 = east_face.addOrReplaceChild("small_chip_a_r3", CubeListBuilder.create().texOffs(8, 1).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, -8.5F, 6.0F, -1.8332F, 1.353F, -0.3742F));

        PartDefinition small_chip_c_r2 = east_face.addOrReplaceChild("small_chip_c_r2", CubeListBuilder.create().texOffs(6, 11).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, -14.0F, -5.0F, -1.8913F, 0.543F, -0.4905F));

        PartDefinition chip_a_r2 = east_face.addOrReplaceChild("chip_a_r2", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -4.0F, -3.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -5.5F, -1.5F, 1.5112F, -0.4326F, -1.3889F));

        PartDefinition chip_b_r2 = east_face.addOrReplaceChild("chip_b_r2", CubeListBuilder.create().texOffs(0, 8).addBox(-1.0F, -4.0F, -3.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.4F, -11.5F, 3.0F, -2.9118F, 0.2399F, -1.4271F));

        PartDefinition west_face = partdefinition.addOrReplaceChild("west_face", CubeListBuilder.create(), PartPose.offsetAndRotation(8.0F, 24.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition small_chip_a_r4 = west_face.addOrReplaceChild("small_chip_a_r4", CubeListBuilder.create().texOffs(8, 1).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -11.5F, -1.0F, -1.7413F, 0.2195F, -3.1261F));

        PartDefinition small_chip_a_r5 = west_face.addOrReplaceChild("small_chip_a_r5", CubeListBuilder.create().texOffs(8, 1).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, -8.5F, 6.0F, -1.8332F, 1.353F, -0.3742F));

        PartDefinition small_chip_c_r3 = west_face.addOrReplaceChild("small_chip_c_r3", CubeListBuilder.create().texOffs(6, 11).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, -14.0F, -5.0F, -1.8913F, 0.543F, -0.4905F));

        PartDefinition chip_a_r3 = west_face.addOrReplaceChild("chip_a_r3", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -4.0F, -3.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -4.5F, -1.5F, 1.5112F, -0.4326F, -1.3889F));

        PartDefinition chip_b_r3 = west_face.addOrReplaceChild("chip_b_r3", CubeListBuilder.create().texOffs(0, 8).addBox(-1.0F, -4.0F, -3.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.4F, -11.5F, 3.0F, -2.9118F, 0.2399F, -1.4271F));

        PartDefinition south_face = partdefinition.addOrReplaceChild("south_face", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 8.0F));

        PartDefinition small_chip_b_r4 = south_face.addOrReplaceChild("small_chip_b_r4", CubeListBuilder.create().texOffs(1, 7).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -14.0F, 0.0F, 0.0256F, 0.1318F, -1.8268F));

        PartDefinition small_chip_a_r6 = south_face.addOrReplaceChild("small_chip_a_r6", CubeListBuilder.create().texOffs(8, 1).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, -6.0F, -0.5F, 1.2294F, 1.1386F, -1.3893F));

        PartDefinition medium_chip_b_r2 = south_face.addOrReplaceChild("medium_chip_b_r2", CubeListBuilder.create().texOffs(2, 9).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.3379F, -3.3495F, -0.9167F, -2.1795F, -1.2492F, 0.6181F));

        PartDefinition medium_chip_a_r1 = south_face.addOrReplaceChild("medium_chip_a_r1", CubeListBuilder.create().texOffs(2, 2).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.6943F, -11.3981F, -0.8037F, 1.5506F, -1.2937F, 2.931F));

        PartDefinition medium_chip_a_r2 = south_face.addOrReplaceChild("medium_chip_a_r2", CubeListBuilder.create().texOffs(2, 2).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.6943F, -4.3981F, -0.8037F, 2.8798F, 1.4399F, -1.7453F));

        PartDefinition chip_a_r4 = south_face.addOrReplaceChild("chip_a_r4", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.8758F, -9.7342F, -1.5687F, -3.1081F, -0.1588F, -1.3639F));

        PartDefinition north_face = partdefinition.addOrReplaceChild("north_face", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 24.0F, -8.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition small_chip_b_r5 = north_face.addOrReplaceChild("small_chip_b_r5", CubeListBuilder.create().texOffs(1, 7).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -14.0F, 0.0F, 0.0256F, 0.1318F, -1.8268F));

        PartDefinition small_chip_a_r7 = north_face.addOrReplaceChild("small_chip_a_r7", CubeListBuilder.create().texOffs(8, 1).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, -6.0F, -0.5F, 1.2294F, 1.1386F, -1.3893F));

        PartDefinition medium_chip_b_r3 = north_face.addOrReplaceChild("medium_chip_b_r3", CubeListBuilder.create().texOffs(2, 9).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.3379F, -3.3495F, -0.9167F, -2.1795F, -1.2492F, 0.6181F));

        PartDefinition medium_chip_a_r3 = north_face.addOrReplaceChild("medium_chip_a_r3", CubeListBuilder.create().texOffs(2, 2).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.6943F, -11.3981F, -0.8037F, 1.5506F, -1.2937F, 2.931F));

        PartDefinition medium_chip_a_r4 = north_face.addOrReplaceChild("medium_chip_a_r4", CubeListBuilder.create().texOffs(2, 2).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.6943F, -4.3981F, -0.8037F, 2.8798F, 1.4399F, -1.7453F));

        PartDefinition chip_a_r5 = north_face.addOrReplaceChild("chip_a_r5", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.8758F, -9.7342F, -1.5687F, -3.1081F, -0.1588F, -1.3639F));


        return LayerDefinition.create(meshdefinition, 16, 16);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

    public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay);
    }
}