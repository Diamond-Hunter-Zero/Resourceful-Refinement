package com.resourceful_refinement.content.glare.chromatic_transceiver;

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

public class GlareChromaticTransceiverModel extends HierarchicalModel<Entity> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "glare_chromatic_transceiver"),
            "main");

    private final ModelPart root;

    private final ModelPart frame;
    private final ModelPart lower_frame;
    private final ModelPart mid_frame;
    private final ModelPart chromatic_lights;

    public GlareChromaticTransceiverModel(ModelPart root) {
        this.root = root;

        this.frame = root.getChild("frame");
        this.lower_frame = this.frame.getChild("lower_frame");
        this.mid_frame = this.frame.getChild("mid_frame");
        this.chromatic_lights = root.getChild("chromatic_lights");
    }

    @Override
    public ModelPart root() {
        return root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition frame = partdefinition.addOrReplaceChild("frame", CubeListBuilder.create().texOffs(72, 91).addBox(-7.0F, -14.9998F, -7.0F, 14.0F, 11.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 16.0F, 8.0F, 1.5708F, 0.0F, 0.0F));


        PartDefinition lower_frame = frame.addOrReplaceChild("lower_frame", CubeListBuilder.create().texOffs(17, 47).addBox(4.0F, -1.0F, 4.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(34, 49).addBox(-4.0F, -1.0F, 5.0F, 8.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(34, 49).addBox(-4.0F, -4.0F, 5.0F, 8.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(23, 92).addBox(-5.0F, 0.0F, -5.0F, 10.0F, 0.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(23, 92).addBox(-5.0F, -1.0F, -5.0F, 10.0F, 0.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition corner_node_r1 = lower_frame.addOrReplaceChild("corner_node_r1", CubeListBuilder.create().texOffs(17, 47).addBox(4.0F, -1.0F, 4.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(34, 49).addBox(-4.0F, -1.0F, 5.0F, 8.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -3.1416F, 0.0F, 3.1416F));

        PartDefinition frame_arm_r1 = lower_frame.addOrReplaceChild("frame_arm_r1", CubeListBuilder.create().texOffs(34, 49).addBox(-4.0F, -1.0F, 5.0F, 8.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.0F, 0.0F, -3.1416F, 0.0F, 3.1416F));

        PartDefinition frame_arm_r2 = lower_frame.addOrReplaceChild("frame_arm_r2", CubeListBuilder.create().texOffs(34, 49).addBox(-4.0F, -1.0F, 5.0F, 8.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(34, 49).addBox(-4.0F, 2.0F, 5.0F, 8.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(17, 47).addBox(4.0F, 2.0F, 4.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition corner_node_r2 = lower_frame.addOrReplaceChild("corner_node_r2", CubeListBuilder.create().texOffs(17, 47).addBox(4.0F, -1.0F, 4.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(34, 49).addBox(-4.0F, -4.0F, 5.0F, 8.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(34, 49).addBox(-4.0F, -1.0F, 5.0F, 8.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition mid_frame = frame.addOrReplaceChild("mid_frame", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -3.0F, 6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(17, 47).addBox(4.0F, -4.0F, 4.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(61, 62).addBox(-7.0F, -3.1F, -7.0F, 14.0F, 0.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition frame_joiner_r1 = mid_frame.addOrReplaceChild("frame_joiner_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -3.0F, 6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(17, 47).addBox(4.0F, -4.0F, 4.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition frame_joiner_r2 = mid_frame.addOrReplaceChild("frame_joiner_r2", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -3.0F, 6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(17, 47).addBox(4.0F, -4.0F, 4.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -3.1416F, 0.0F, 3.1416F));

        PartDefinition frame_joiner_r3 = mid_frame.addOrReplaceChild("frame_joiner_r3", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -3.0F, 6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(17, 47).addBox(4.0F, -4.0F, 4.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));


        PartDefinition chromatic_lights = partdefinition.addOrReplaceChild("chromatic_lights", CubeListBuilder.create().texOffs(0, 103).addBox(-6.0F, -3.0F, -10.0F, 12.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 14.0F, 8.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition side_light_r1 = chromatic_lights.addOrReplaceChild("side_light_r1", CubeListBuilder.create().texOffs(0, 103).addBox(-6.0F, -2.0F, -8.0F, 12.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, -2.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition side_light_r2 = chromatic_lights.addOrReplaceChild("side_light_r2", CubeListBuilder.create().texOffs(0, 103).addBox(-6.0F, -2.0F, -8.0F, 12.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, -2.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition side_light_r3 = chromatic_lights.addOrReplaceChild("side_light_r3", CubeListBuilder.create().texOffs(0, 103).addBox(-6.0F, -2.0F, -8.0F, 12.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, -2.0F, 0.0F, 1.5708F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }


    public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int colour) {
        frame.render(poseStack, vertexConsumer, packedLight, packedOverlay);
        chromatic_lights.render(poseStack, vertexConsumer, packedLight, packedOverlay, colour);
    }

}
