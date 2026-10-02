package com.resourceful_refinement.content.resonator;

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

public class ResourceResonatorModel extends HierarchicalModel<Entity> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "resource_resonator"),
            "main");

    private final ModelPart root;

    private final ModelPart frame;
    private final ModelPart screen;
    private final ModelPart core;
    private final ModelPart support_leg;
    private final ModelPart support_leg_pillar;
    private final ModelPart support_leg2;
    private final ModelPart support_leg_pillar2;
    private final ModelPart support_leg3;
    private final ModelPart support_leg_pillar3;
    private final ModelPart support_leg4;
    private final ModelPart support_leg_pillar4;

    public ResourceResonatorModel(ModelPart root) {
        this.root = root;

        this.frame = root.getChild("frame");
        this.screen = this.frame.getChild("screen");
        this.core = this.frame.getChild("core");
        this.support_leg = this.frame.getChild("support_leg");
        this.support_leg_pillar = this.support_leg.getChild("support_leg_pillar");
        this.support_leg2 = this.frame.getChild("support_leg2");
        this.support_leg_pillar2 = this.support_leg2.getChild("support_leg_pillar2");
        this.support_leg3 = this.frame.getChild("support_leg3");
        this.support_leg_pillar3 = this.support_leg3.getChild("support_leg_pillar3");
        this.support_leg4 = this.frame.getChild("support_leg4");
        this.support_leg_pillar4 = this.support_leg4.getChild("support_leg_pillar4");
    }

    @Override
    public ModelPart root() {
        return root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition frame = partdefinition.addOrReplaceChild("frame", CubeListBuilder.create().texOffs(36, 28).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition screen = frame.addOrReplaceChild("screen", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -16.0F, -8.0F, 16.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 18).addBox(6.0F, -17.0F, -8.0F, 2.0F, 1.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 18).mirror().addBox(-8.0F, -17.0F, -8.0F, 2.0F, 1.0F, 16.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(36, 18).addBox(-6.0F, -17.0F, 6.0F, 12.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.0F, 0.0F));

        PartDefinition screen_frame_b_r1 = screen.addOrReplaceChild("screen_frame_b_r1", CubeListBuilder.create().texOffs(36, 18).addBox(-6.0F, -0.5F, -1.0F, 12.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -16.5F, -7.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition core = frame.addOrReplaceChild("core", CubeListBuilder.create().texOffs(36, 21).addBox(-3.0F, -16.0F, -3.0F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 35).addBox(-4.0F, -15.0F, -4.0F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(24, 45).addBox(-4.0F, -13.0F, -4.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 0.0F));

        PartDefinition core_pillar_r1 = core.addOrReplaceChild("core_pillar_r1", CubeListBuilder.create().texOffs(24, 45).addBox(-4.0F, -12.0F, -4.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition core_pillar_r2 = core.addOrReplaceChild("core_pillar_r2", CubeListBuilder.create().texOffs(24, 45).addBox(-4.0F, -12.0F, -4.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition core_pillar_r3 = core.addOrReplaceChild("core_pillar_r3", CubeListBuilder.create().texOffs(24, 45).addBox(-4.0F, -12.0F, -4.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition core_lower_r1 = core.addOrReplaceChild("core_lower_r1", CubeListBuilder.create().texOffs(0, 35).addBox(-4.0F, -1.0F, -4.0F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, 0.0F, 0.0F, -3.1416F));

        PartDefinition support_leg = frame.addOrReplaceChild("support_leg", CubeListBuilder.create().texOffs(12, 45).addBox(-8.0F, -2.0F, -8.0F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition support_leg_pillar = support_leg.addOrReplaceChild("support_leg_pillar", CubeListBuilder.create().texOffs(0, 45).addBox(-1.5F, -3.6667F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(32, 45).addBox(-1.0F, -7.6667F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(40, 45).addBox(-1.0F, 3.3333F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.5F, -6.3333F, -5.5F, -0.1745F, 0.0F, 0.1745F));

        PartDefinition support_leg2 = frame.addOrReplaceChild("support_leg2", CubeListBuilder.create().texOffs(12, 45).addBox(-8.0F, -2.0F, -8.0F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition support_leg_pillar2 = support_leg2.addOrReplaceChild("support_leg_pillar2", CubeListBuilder.create().texOffs(0, 45).addBox(-1.5F, -3.6667F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(32, 45).addBox(-1.0F, -7.6667F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(40, 45).addBox(-1.0F, 3.3333F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.5F, -6.3333F, -5.5F, -0.1745F, 0.0F, 0.1745F));

        PartDefinition support_leg3 = frame.addOrReplaceChild("support_leg3", CubeListBuilder.create().texOffs(12, 45).addBox(-8.0F, -2.0F, -8.0F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition support_leg_pillar3 = support_leg3.addOrReplaceChild("support_leg_pillar3", CubeListBuilder.create().texOffs(0, 45).addBox(-1.5F, -3.6667F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(32, 45).addBox(-1.0F, -7.6667F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(40, 45).addBox(-1.0F, 3.3333F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.5F, -6.3333F, -5.5F, -0.1745F, 0.0F, 0.1745F));

        PartDefinition support_leg4 = frame.addOrReplaceChild("support_leg4", CubeListBuilder.create().texOffs(12, 45).addBox(-8.0F, -2.0F, -8.0F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition support_leg_pillar4 = support_leg4.addOrReplaceChild("support_leg_pillar4", CubeListBuilder.create().texOffs(0, 45).addBox(-1.5F, -3.6667F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(32, 45).addBox(-1.0F, -7.6667F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(40, 45).addBox(-1.0F, 3.3333F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.5F, -6.3333F, -5.5F, -0.1745F, 0.0F, 0.1745F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

    public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay) {
        frame.render(poseStack, vertexConsumer, packedLight, packedOverlay);
    }

}
