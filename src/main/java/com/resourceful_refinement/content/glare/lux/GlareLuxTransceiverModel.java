package com.resourceful_refinement.content.glare.lux;

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

public class GlareLuxTransceiverModel extends HierarchicalModel<Entity> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "glare_lux_transceiver"),
            "main");

    private final ModelPart root;

    private final ModelPart frame;
    private final ModelPart cap_top;
    private final ModelPart pillars;

    public GlareLuxTransceiverModel(ModelPart root) {
        this.root = root;

        this.frame = root.getChild("frame");
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

        PartDefinition frame = partdefinition.addOrReplaceChild("frame", CubeListBuilder.create().texOffs(31, 74).addBox(-7.0F, -2.0F, -6.0F, 14.0F, 2.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 17.0F, 8.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition cap_top = frame.addOrReplaceChild("cap_top", CubeListBuilder.create().texOffs(61, 20).addBox(-4.0F, -1.0002F, -4.0F, 8.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(62, 58).addBox(-4.0F, -1.0002F, 2.0F, 8.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(13, 63).addBox(-4.0F, -1.0002F, -2.0F, 2.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(62, 68).addBox(2.0F, -1.0002F, -2.0F, 2.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -12.9998F, 0.0F));

        PartDefinition cube_r1 = cap_top.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(58, 36).addBox(-0.35F, -0.4F, -0.5F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5F, -0.5002F, -3.5F, 0.4636F, 0.6591F, 0.6847F));

        PartDefinition cube_r2 = cap_top.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(60, 33).addBox(-3.65F, -0.4F, -0.5F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.5F, -0.5002F, -3.5F, 0.4636F, -0.6591F, -0.6847F));

        PartDefinition cube_r3 = cap_top.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(58, 30).addBox(-0.35F, -0.4F, -0.5F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5F, -0.5002F, 3.5F, -0.4636F, -0.6591F, 0.6847F));

        PartDefinition cube_r4 = cap_top.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(60, 27).addBox(-3.65F, -0.4F, -0.5F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.5F, -0.5002F, 3.5F, -0.4636F, 0.6591F, -0.6847F));

        PartDefinition pillars = frame.addOrReplaceChild("pillars", CubeListBuilder.create().texOffs(5, 74).addBox(5.0F, -5.0F, -6.0F, 1.0F, 10.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(10, 74).addBox(5.0F, -5.0F, 5.0F, 1.0F, 10.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(15, 74).addBox(-6.0F, -5.0F, -6.0F, 1.0F, 10.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(20, 74).addBox(-6.0F, -5.0F, 5.0F, 1.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -7.0F, 0.0F));


        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

    public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay) {
        frame.render(poseStack, vertexConsumer, packedLight, packedOverlay);
    }

}
