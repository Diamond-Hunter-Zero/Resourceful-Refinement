package com.resourceful_refinement.content.glare.kinetic_receiver;

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

public class GlareKineticReceiverModel extends HierarchicalModel<Entity> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "glare_kinetic_receiver"),
            "main");

    private final ModelPart root;

    private final ModelPart frame;
    private final ModelPart cap_top;
    private final ModelPart pillars;
    private final ModelPart lower_frame;
    private final ModelPart mid_frame;


    public GlareKineticReceiverModel(ModelPart root) {
        this.root = root;

        this.frame = root.getChild("frame");
        this.cap_top = this.frame.getChild("cap_top");
        this.pillars = this.frame.getChild("pillars");
        this.lower_frame = this.frame.getChild("lower_frame");
        this.mid_frame = this.frame.getChild("mid_frame");
    }

    @Override
    public ModelPart root() {
        return root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition frame = partdefinition.addOrReplaceChild("frame", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 16.0F, -8.0F, -1.5708F, 0.0F, 0.0F));

        PartDefinition cap_top = frame.addOrReplaceChild("cap_top", CubeListBuilder.create().texOffs(61, 20).addBox(-4.0F, -1.0002F, -4.0F, 8.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(62, 58).addBox(-4.0F, -1.0002F, 2.0F, 8.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(13, 63).addBox(-4.0F, -1.0002F, -2.0F, 2.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(62, 68).addBox(2.0F, -1.0002F, -2.0F, 2.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -13.9998F, 0.0F));

        PartDefinition cube_r1 = cap_top.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(72, 33).addBox(-0.05F, -0.6F, -0.5F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5F, -0.5002F, -3.5F, 0.5942F, 0.5484F, 0.9136F));

        PartDefinition cube_r2 = cap_top.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(72, 30).addBox(-5.95F, -0.6F, -0.5F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.5F, -0.5002F, -3.5F, 0.5942F, -0.5484F, -0.9136F));

        PartDefinition cube_r3 = cap_top.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(72, 27).addBox(-0.05F, -0.6F, -0.5F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5F, -0.5002F, 3.5F, -0.5942F, -0.5484F, 0.9136F));

        PartDefinition cube_r4 = cap_top.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(72, 24).addBox(-5.95F, -0.6F, -0.5F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.5F, -0.5002F, 3.5F, -0.5942F, 0.5484F, -0.9136F));

        PartDefinition pillars = frame.addOrReplaceChild("pillars", CubeListBuilder.create().texOffs(5, 74).addBox(6.0F, -4.0F, -7.0F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(10, 74).addBox(6.0F, -4.0F, 6.0F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(15, 74).addBox(-7.0F, -4.0F, -7.0F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(20, 74).addBox(-7.0F, -4.0F, 6.0F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -7.0F, 0.0F));

        PartDefinition lower_frame = frame.addOrReplaceChild("lower_frame", CubeListBuilder.create().texOffs(17, 47).addBox(4.0F, -1.0F, 4.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(34, 49).addBox(-4.0F, -1.0F, 5.0F, 8.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(23, 92).addBox(-5.0F, -0.9F, -5.0F, 10.0F, 0.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition corner_node_r1 = lower_frame.addOrReplaceChild("corner_node_r1", CubeListBuilder.create().texOffs(17, 47).addBox(4.0F, -1.0F, 4.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(34, 49).addBox(-4.0F, -1.0F, 5.0F, 8.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -3.1416F, 0.0F, 3.1416F));

        PartDefinition corner_node_r2 = lower_frame.addOrReplaceChild("corner_node_r2", CubeListBuilder.create().texOffs(17, 47).addBox(4.0F, -1.0F, 4.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(34, 49).addBox(-4.0F, -1.0F, 5.0F, 8.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition frame_arm_r1 = lower_frame.addOrReplaceChild("frame_arm_r1", CubeListBuilder.create().texOffs(34, 49).addBox(-4.0F, -1.0F, 5.0F, 8.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(17, 47).addBox(4.0F, -1.0F, 4.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition mid_frame = frame.addOrReplaceChild("mid_frame", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -3.0F, 6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(17, 47).addBox(4.0F, -4.0F, 4.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(61, 62).addBox(-7.0F, -3.1F, -7.0F, 14.0F, 0.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition frame_joiner_r1 = mid_frame.addOrReplaceChild("frame_joiner_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -3.0F, 6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(17, 47).addBox(4.0F, -4.0F, 4.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition frame_joiner_r2 = mid_frame.addOrReplaceChild("frame_joiner_r2", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -3.0F, 6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(17, 47).addBox(4.0F, -4.0F, 4.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -3.1416F, 0.0F, 3.1416F));

        PartDefinition frame_joiner_r3 = mid_frame.addOrReplaceChild("frame_joiner_r3", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -3.0F, 6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(17, 47).addBox(4.0F, -4.0F, 4.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));


        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }


    public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay) {
        frame.render(poseStack, vertexConsumer, packedLight, packedOverlay);
    }

}
