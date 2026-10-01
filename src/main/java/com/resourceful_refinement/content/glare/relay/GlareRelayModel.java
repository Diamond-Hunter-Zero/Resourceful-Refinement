package com.resourceful_refinement.content.glare.relay;

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

public class GlareRelayModel extends HierarchicalModel<Entity> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "glare_relay"),
            "main");

    private final ModelPart root;

    private final ModelPart frame;
    private final ModelPart cap_bottom;
    private final ModelPart cap_top;
    private final ModelPart pillars;

    public GlareRelayModel(ModelPart root) {
        this.root = root;

        this.frame = root.getChild("frame");
        this.cap_bottom = this.frame.getChild("cap_bottom");
        this.cap_top = this.frame.getChild("cap_top");
        this.pillars = this.frame.getChild("pillars");
    }

    @Override
    public ModelPart root() {
        return root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition frame = partdefinition.addOrReplaceChild("frame", CubeListBuilder.create().texOffs(0, 0).addBox(-7.5F, -13.0F, -7.5F, 15.0F, 10.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition cap_bottom = frame.addOrReplaceChild("cap_bottom", CubeListBuilder.create().texOffs(61, 20).addBox(-4.0F, -1.0F, -1.0F, 8.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(62, 58).addBox(-4.0F, -1.0F, 5.0F, 8.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(13, 63).addBox(-4.0F, -1.0F, 1.0F, 2.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(62, 68).addBox(2.0F, -1.0F, 1.0F, 2.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(61, 6).addBox(-8.0F, -3.0F, -5.0F, 16.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(61, 9).addBox(-8.0F, -3.0F, 10.0F, 16.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 47).addBox(-8.0F, -3.0F, -4.0F, 1.0F, 1.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(31, 58).addBox(7.0F, -3.0F, -4.0F, 1.0F, 1.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -3.0F));

        PartDefinition cube_r1 = cap_bottom.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(72, 33).addBox(-0.35F, -0.6F, -0.5F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5F, -0.5F, -0.5F, -0.3295F, 0.7268F, -0.4754F));

        PartDefinition cube_r2 = cap_bottom.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(72, 30).addBox(-5.65F, -0.6F, -0.5F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.5F, -0.5F, -0.5F, -0.3295F, -0.7268F, 0.4754F));

        PartDefinition cube_r3 = cap_bottom.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(72, 27).addBox(-0.35F, -0.6F, -0.5F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5F, -0.5F, 6.5F, 0.3295F, -0.7268F, -0.4754F));

        PartDefinition cube_r4 = cap_bottom.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(72, 24).addBox(-5.65F, -0.6F, -0.5F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.5F, -0.5F, 6.5F, 0.3295F, 0.7268F, 0.4754F));

        PartDefinition cap_top = frame.addOrReplaceChild("cap_top", CubeListBuilder.create().texOffs(61, 20).addBox(-4.0F, 0.0F, -1.0F, 8.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(62, 58).addBox(-4.0F, 0.0F, 5.0F, 8.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(13, 63).addBox(-4.0F, 0.0F, 1.0F, 2.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(62, 68).addBox(2.0F, 0.0F, 1.0F, 2.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(61, 6).addBox(-8.0F, 2.0F, -5.0F, 16.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(61, 9).addBox(-8.0F, 2.0F, 10.0F, 16.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 47).addBox(-8.0F, 2.0F, -4.0F, 1.0F, 1.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(31, 58).addBox(7.0F, 2.0F, -4.0F, 1.0F, 1.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -16.0F, -3.0F));

        PartDefinition cube_r5 = cap_top.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(72, 33).addBox(-0.35F, -0.4F, -0.5F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5F, 0.5F, -0.5F, 0.3295F, 0.7268F, 0.4754F));

        PartDefinition cube_r6 = cap_top.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(72, 30).addBox(-5.65F, -0.4F, -0.5F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.5F, 0.5F, -0.5F, 0.3295F, -0.7268F, -0.4754F));

        PartDefinition cube_r7 = cap_top.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(72, 27).addBox(-0.35F, -0.4F, -0.5F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5F, 0.5F, 6.5F, -0.3295F, -0.7268F, 0.4754F));

        PartDefinition cube_r8 = cap_top.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(72, 24).addBox(-5.65F, -0.4F, -0.5F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.5F, 0.5F, 6.5F, -0.3295F, 0.7268F, -0.4754F));

        PartDefinition pillars = frame.addOrReplaceChild("pillars", CubeListBuilder.create().texOffs(25, 74).addBox(7.0F, -13.0F, -8.0F, 1.0F, 10.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(30, 74).addBox(7.0F, -13.0F, 7.0F, 1.0F, 10.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(35, 74).addBox(-8.0F, -13.0F, -8.0F, 1.0F, 10.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(40, 74).addBox(-8.0F, -13.0F, 7.0F, 1.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

    public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay) {
        frame.render(poseStack, vertexConsumer, packedLight, packedOverlay);
    }

}
