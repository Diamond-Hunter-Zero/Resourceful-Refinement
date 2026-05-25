package com.resourceful_refinement.content.plushie;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.resourceful_refinement.ResourcefulRefinementMain;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class PlushieModel extends Model {
    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
    private final ModelPart root;
    private final ModelPart Head;
    private final ModelPart Ear;
    private final ModelPart Ear2;
    private final ModelPart Goggles;
    private final ModelPart bb_main;

    public PlushieModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
        this.Head = root.getChild("Head");
        this.Ear = this.Head.getChild("Ear");
        this.Ear2 = this.Head.getChild("Ear2");
        this.Goggles = this.Head.getChild("Goggles");
        this.bb_main = root.getChild("bb_main");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition Head = partdefinition.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(36, 36).addBox(-2.0F, -2.5F, -5.0F, 4.0F, 2.5F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-4.0F, -7.0F, -3.0F, 8.0F, 6.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(20, 13).addBox(-3.0F, -8.0F, -3.0F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(20, 20).addBox(-3.0F, -1.0F, -3.0F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(20, 27).addBox(-5.0F, -9.0F, -1.5F, 10.0F, 9.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 27).addBox(-5.0F, -9.0F, 0.0F, 10.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 14.0F, 0.0F));

        PartDefinition Ear = Head.addOrReplaceChild("Ear", CubeListBuilder.create().texOffs(30, 9).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(38, 9).addBox(-1.0F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(40, 27).addBox(2.0F, 1.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(36, 41).addBox(1.0F, 0.0F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(40, 31).addBox(0.0F, 0.0F, 0.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, -10.0F, 0.0F));

        PartDefinition Ear2 = Head.addOrReplaceChild("Ear2", CubeListBuilder.create().texOffs(30, 9).mirror().addBox(-1.0F, -1.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(38, 9).mirror().addBox(0.0F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(40, 27).mirror().addBox(-3.0F, 1.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(36, 41).mirror().addBox(-2.0F, 0.0F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(40, 31).mirror().addBox(-2.0F, 0.0F, 0.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-2.0F, -10.0F, 0.0F));

        PartDefinition Goggles = Head.addOrReplaceChild("Goggles", CubeListBuilder.create().texOffs(50, 1).addBox(-4.5F, -1.5F, -4.625F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(50, 1).addBox(0.5F, -1.5F, -4.625F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(44, 2).addBox(-0.5F, 0.0F, -4.625F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 46).addBox(-4.5F, 0.0F, -3.625F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 46).addBox(3.5F, 0.0F, -3.625F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(9, 53).addBox(-3.5F, 0.0F, 3.375F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -7.0F, 0.625F, -0.2182F, 0.0F, 0.0F));

        PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(0, 13).addBox(-3.0F, -10.0F, -2.0F, 6.0F, 10.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition Tag_r1 = bb_main.addOrReplaceChild("Tag_r1", CubeListBuilder.create().texOffs(1, 0).addBox(0.0F, -0.5F, -1.0F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3F, -4.5F, 2.75F, -0.1317F, -0.4184F, -0.4904F));

        PartDefinition Leg_r1 = bb_main.addOrReplaceChild("Leg_r1", CubeListBuilder.create().texOffs(30, 0).mirror().addBox(-1.5F, -1.5F, -3.0F, 3.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-3.0F, -1.4F, -2.0F, 0.0F, 0.0873F, 0.0F));

        PartDefinition Arm_r1 = bb_main.addOrReplaceChild("Arm_r1", CubeListBuilder.create().texOffs(0, 36).mirror().addBox(-1.0F, -1.0F, -6.5F, 2.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 36).addBox(5.0F, -1.0F, -6.5F, 2.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -8.0F, 0.5F, 0.3927F, 0.0F, 0.0F));

        PartDefinition Tail_r1 = bb_main.addOrReplaceChild("Tail_r1", CubeListBuilder.create().texOffs(18, 36).addBox(-1.0F, -3.0F, -1.0F, 3.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -0.5F, 2.5F, -0.0873F, 0.1745F, 0.0F));

        PartDefinition Leg_r2 = bb_main.addOrReplaceChild("Leg_r2", CubeListBuilder.create().texOffs(30, 0).addBox(-1.5F, -1.5F, -3.0F, 3.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -1.4F, -2.0F, 0.0F, -0.0873F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void renderToBuffer(PoseStack ms, VertexConsumer buffer, int light, int overlay, int color) {
        root.render(ms, buffer, light, overlay, color);
    }
}
