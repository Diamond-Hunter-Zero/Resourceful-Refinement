package com.resourceful_refinement.content.bucket_excavator;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.resourceful_refinement.ResourcefulRefinementMain;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.joml.Vector3f;

public class BucketExcavatorModel extends HierarchicalModel<Entity> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "bucket_excavator"),
            "main");
    private final Vector3f ANIMATION_VECTOR_CACHE = new Vector3f();

    private final ModelPart root;

    private final ModelPart shield_mesh;
    private final ModelPart wheel;
    private final ModelPart wheel_segment;
    private final ModelPart bucket;
    private final ModelPart bucket2;
    private final ModelPart wheel_segment4;
    private final ModelPart bucket7;
    private final ModelPart bucket8;
    private final ModelPart wheel_segment3;
    private final ModelPart bucket5;
    private final ModelPart bucket6;
    private final ModelPart wheel_segment5;
    private final ModelPart bucket9;
    private final ModelPart bucket10;
    private final ModelPart wheel_segment2;
    private final ModelPart bucket3;
    private final ModelPart bucket4;
    private final ModelPart wheel_segment6;
    private final ModelPart bucket11;
    private final ModelPart bucket12;
    private final ModelPart bb_main;

    public BucketExcavatorModel(ModelPart root) {
        this.root = root;

        this.shield_mesh = root.getChild("shield_mesh");
        this.wheel = root.getChild("wheel");
        this.wheel_segment = this.wheel.getChild("wheel_segment");
        this.bucket = this.wheel_segment.getChild("bucket");
        this.bucket2 = this.wheel_segment.getChild("bucket2");
        this.wheel_segment4 = this.wheel.getChild("wheel_segment4");
        this.bucket7 = this.wheel_segment4.getChild("bucket7");
        this.bucket8 = this.wheel_segment4.getChild("bucket8");
        this.wheel_segment3 = this.wheel.getChild("wheel_segment3");
        this.bucket5 = this.wheel_segment3.getChild("bucket5");
        this.bucket6 = this.wheel_segment3.getChild("bucket6");
        this.wheel_segment5 = this.wheel.getChild("wheel_segment5");
        this.bucket9 = this.wheel_segment5.getChild("bucket9");
        this.bucket10 = this.wheel_segment5.getChild("bucket10");
        this.wheel_segment2 = this.wheel.getChild("wheel_segment2");
        this.bucket3 = this.wheel_segment2.getChild("bucket3");
        this.bucket4 = this.wheel_segment2.getChild("bucket4");
        this.wheel_segment6 = this.wheel.getChild("wheel_segment6");
        this.bucket11 = this.wheel_segment6.getChild("bucket11");
        this.bucket12 = this.wheel_segment6.getChild("bucket12");
        this.bb_main = root.getChild("bb_main");
    }

    @Override
    public ModelPart root() {
        return root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition shield_mesh = partdefinition.addOrReplaceChild("shield_mesh", CubeListBuilder.create().texOffs(126, 66).addBox(-7.0F, -23.0F, 0.0F, 14.0F, 15.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(112, 126).addBox(-8.0F, -24.0F, -0.5F, 1.0F, 48.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(112, 126).addBox(7.0F, -24.0F, -0.5F, 1.0F, 48.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(126, 81).addBox(-7.0F, -24.0F, -0.5F, 14.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(126, 81).addBox(-7.0F, -8.0F, -0.5F, 14.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(126, 81).addBox(-7.0F, 7.0F, -0.5F, 14.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(126, 81).addBox(-7.0F, 23.0F, -0.5F, 14.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 16.0F, -8.5F));

        PartDefinition front_mesh_lower_r1 = shield_mesh.addOrReplaceChild("front_mesh_lower_r1", CubeListBuilder.create().texOffs(126, 66).addBox(-7.0F, -7.5F, 0.0F, 14.0F, 15.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 15.5F, 0.0F, 3.1416F, 0.0F, 0.0F));

        PartDefinition wheel = partdefinition.addOrReplaceChild("wheel", CubeListBuilder.create().texOffs(1, 0).addBox(-6.5F, -24.0F, -24.0F, 13.0F, 48.0F, 48.0F, new CubeDeformation(0.0F))
                .texOffs(164, 73).addBox(-8.0F, -2.0F, -2.0F, 16.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 16.0F, -48.0F));

        PartDefinition wheel_segment = wheel.addOrReplaceChild("wheel_segment", CubeListBuilder.create().texOffs(208, 23).addBox(-7.75F, -6.5F, -24.0F, 15.0F, 13.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.25F, 0.0F, 0.0F));

        PartDefinition extent_r1 = wheel_segment.addOrReplaceChild("extent_r1", CubeListBuilder.create().texOffs(126, 41).addBox(-7.545F, -6.5F, -24.0F, 14.99F, 13.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2F, 0.0F, 0.0F, -0.5236F, 0.0F, 0.0F));

        PartDefinition bucket = wheel_segment.addOrReplaceChild("bucket", CubeListBuilder.create().texOffs(140, 3).addBox(-7.05F, -6.0F, -36.0F, 14.0F, 7.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(116, -9).addBox(-7.05F, -6.0F, -36.0F, 0.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(116, -9).addBox(6.95F, -6.0F, -36.0F, 0.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(163, 62).addBox(-7.0F, 4.9F, -30.75F, 13.9F, 1.0F, 7.1F, new CubeDeformation(0.0F)), PartPose.offset(-0.2F, 0.0F, 0.0F));

        PartDefinition bucket_slope_r1 = bucket.addOrReplaceChild("bucket_slope_r1", CubeListBuilder.create().texOffs(163, 62).addBox(-6.95F, -0.45F, -4.0F, 13.9F, 1.0F, 7.1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.05F, 3.45F, -32.75F, -0.7854F, 0.0F, 0.0F));

        PartDefinition bucket2 = wheel_segment.addOrReplaceChild("bucket2", CubeListBuilder.create().texOffs(140, 3).addBox(-7.05F, -6.0F, -36.0F, 14.0F, 7.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(116, -9).addBox(-7.05F, -6.0F, -36.0F, 0.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(116, -9).addBox(6.95F, -6.0F, -36.0F, 0.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(163, 62).addBox(-7.0F, 4.9F, -30.75F, 13.9F, 1.0F, 7.1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2F, 0.0F, 0.0F, -0.5236F, 0.0F, 0.0F));

        PartDefinition bucket_slope_r2 = bucket2.addOrReplaceChild("bucket_slope_r2", CubeListBuilder.create().texOffs(163, 62).addBox(-6.95F, -0.45F, -4.0F, 13.9F, 1.0F, 7.1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.05F, 3.45F, -32.75F, -0.7854F, 0.0F, 0.0F));

        PartDefinition wheel_segment4 = wheel.addOrReplaceChild("wheel_segment4", CubeListBuilder.create().texOffs(208, 23).addBox(-7.75F, -6.5F, -24.0F, 15.0F, 13.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.25F, 0.0F, 0.0F, 3.1416F, 0.0F, 0.0F));

        PartDefinition extent_r2 = wheel_segment4.addOrReplaceChild("extent_r2", CubeListBuilder.create().texOffs(126, 41).addBox(-7.545F, -6.5F, -24.0F, 14.99F, 13.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2F, 0.0F, 0.0F, -0.5236F, 0.0F, 0.0F));

        PartDefinition bucket7 = wheel_segment4.addOrReplaceChild("bucket7", CubeListBuilder.create().texOffs(140, 3).addBox(-7.05F, -6.0F, -36.0F, 14.0F, 7.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(116, -9).addBox(-7.05F, -6.0F, -36.0F, 0.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(116, -9).addBox(6.95F, -6.0F, -36.0F, 0.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(163, 62).addBox(-7.0F, 4.9F, -30.75F, 13.9F, 1.0F, 7.1F, new CubeDeformation(0.0F)), PartPose.offset(-0.2F, 0.0F, 0.0F));

        PartDefinition bucket_slope_r3 = bucket7.addOrReplaceChild("bucket_slope_r3", CubeListBuilder.create().texOffs(163, 62).addBox(-6.95F, -0.45F, -4.0F, 13.9F, 1.0F, 7.1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.05F, 3.45F, -32.75F, -0.7854F, 0.0F, 0.0F));

        PartDefinition bucket8 = wheel_segment4.addOrReplaceChild("bucket8", CubeListBuilder.create().texOffs(140, 3).addBox(-7.05F, -6.0F, -36.0F, 14.0F, 7.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(116, -9).addBox(-7.05F, -6.0F, -36.0F, 0.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(116, -9).addBox(6.95F, -6.0F, -36.0F, 0.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(163, 62).addBox(-7.0F, 4.9F, -30.75F, 13.9F, 1.0F, 7.1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2F, 0.0F, 0.0F, -0.5236F, 0.0F, 0.0F));

        PartDefinition bucket_slope_r4 = bucket8.addOrReplaceChild("bucket_slope_r4", CubeListBuilder.create().texOffs(163, 62).addBox(-6.95F, -0.45F, -4.0F, 13.9F, 1.0F, 7.1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.05F, 3.45F, -32.75F, -0.7854F, 0.0F, 0.0F));

        PartDefinition wheel_segment3 = wheel.addOrReplaceChild("wheel_segment3", CubeListBuilder.create().texOffs(165, 41).addBox(-7.75F, -6.5F, -24.0F, 15.0F, 13.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.25F, 0.0F, 0.0F, -2.0944F, 0.0F, 0.0F));

        PartDefinition extent_r3 = wheel_segment3.addOrReplaceChild("extent_r3", CubeListBuilder.create().texOffs(169, 23).addBox(-7.545F, -6.5F, -24.0F, 14.99F, 13.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2F, 0.0F, 0.0F, -0.5236F, 0.0F, 0.0F));

        PartDefinition bucket5 = wheel_segment3.addOrReplaceChild("bucket5", CubeListBuilder.create().texOffs(140, 3).addBox(-7.05F, -6.0F, -36.0F, 14.0F, 7.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(116, -9).addBox(-7.05F, -6.0F, -36.0F, 0.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(116, -9).addBox(6.95F, -6.0F, -36.0F, 0.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(163, 62).addBox(-7.0F, 4.9F, -30.75F, 13.9F, 1.0F, 7.1F, new CubeDeformation(0.0F)), PartPose.offset(-0.2F, 0.0F, 0.0F));

        PartDefinition bucket_slope_r5 = bucket5.addOrReplaceChild("bucket_slope_r5", CubeListBuilder.create().texOffs(163, 62).addBox(-6.95F, -0.45F, -4.0F, 13.9F, 1.0F, 7.1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.05F, 3.45F, -32.75F, -0.7854F, 0.0F, 0.0F));

        PartDefinition bucket6 = wheel_segment3.addOrReplaceChild("bucket6", CubeListBuilder.create().texOffs(140, 3).addBox(-7.05F, -6.0F, -36.0F, 14.0F, 7.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(116, -9).addBox(-7.05F, -6.0F, -36.0F, 0.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(116, -9).addBox(6.95F, -6.0F, -36.0F, 0.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(163, 62).addBox(-7.0F, 4.9F, -30.75F, 13.9F, 1.0F, 7.1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2F, 0.0F, 0.0F, -0.5236F, 0.0F, 0.0F));

        PartDefinition bucket_slope_r6 = bucket6.addOrReplaceChild("bucket_slope_r6", CubeListBuilder.create().texOffs(163, 62).addBox(-6.95F, -0.45F, -4.0F, 13.9F, 1.0F, 7.1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.05F, 3.45F, -32.75F, -0.7854F, 0.0F, 0.0F));

        PartDefinition wheel_segment5 = wheel.addOrReplaceChild("wheel_segment5", CubeListBuilder.create().texOffs(165, 41).addBox(-7.75F, -6.5F, -24.0F, 15.0F, 13.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.25F, 0.0F, 0.0F, 1.0472F, 0.0F, 0.0F));

        PartDefinition extent_r4 = wheel_segment5.addOrReplaceChild("extent_r4", CubeListBuilder.create().texOffs(169, 23).addBox(-7.545F, -6.5F, -24.0F, 14.99F, 13.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2F, 0.0F, 0.0F, -0.5236F, 0.0F, 0.0F));

        PartDefinition bucket9 = wheel_segment5.addOrReplaceChild("bucket9", CubeListBuilder.create().texOffs(140, 3).addBox(-7.05F, -6.0F, -36.0F, 14.0F, 7.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(116, -9).addBox(-7.05F, -6.0F, -36.0F, 0.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(116, -9).addBox(6.95F, -6.0F, -36.0F, 0.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(163, 62).addBox(-7.0F, 4.9F, -30.75F, 13.9F, 1.0F, 7.1F, new CubeDeformation(0.0F)), PartPose.offset(-0.2F, 0.0F, 0.0F));

        PartDefinition bucket_slope_r7 = bucket9.addOrReplaceChild("bucket_slope_r7", CubeListBuilder.create().texOffs(163, 62).addBox(-6.95F, -0.45F, -4.0F, 13.9F, 1.0F, 7.1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.05F, 3.45F, -32.75F, -0.7854F, 0.0F, 0.0F));

        PartDefinition bucket10 = wheel_segment5.addOrReplaceChild("bucket10", CubeListBuilder.create().texOffs(140, 3).addBox(-7.05F, -6.0F, -36.0F, 14.0F, 7.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(116, -9).addBox(-7.05F, -6.0F, -36.0F, 0.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(116, -9).addBox(6.95F, -6.0F, -36.0F, 0.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(163, 62).addBox(-7.0F, 4.9F, -30.75F, 13.9F, 1.0F, 7.1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2F, 0.0F, 0.0F, -0.5236F, 0.0F, 0.0F));

        PartDefinition bucket_slope_r8 = bucket10.addOrReplaceChild("bucket_slope_r8", CubeListBuilder.create().texOffs(163, 62).addBox(-6.95F, -0.45F, -4.0F, 13.9F, 1.0F, 7.1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.05F, 3.45F, -32.75F, -0.7854F, 0.0F, 0.0F));

        PartDefinition wheel_segment2 = wheel.addOrReplaceChild("wheel_segment2", CubeListBuilder.create().texOffs(165, 41).addBox(-7.75F, -6.5F, -24.0F, 15.0F, 13.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.25F, 0.0F, 0.0F, -1.0472F, 0.0F, 0.0F));

        PartDefinition extent_r5 = wheel_segment2.addOrReplaceChild("extent_r5", CubeListBuilder.create().texOffs(169, 23).addBox(-7.545F, -6.5F, -24.0F, 14.99F, 13.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2F, 0.0F, 0.0F, -0.5236F, 0.0F, 0.0F));

        PartDefinition bucket3 = wheel_segment2.addOrReplaceChild("bucket3", CubeListBuilder.create().texOffs(140, 3).addBox(-7.05F, -6.0F, -36.0F, 14.0F, 7.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(116, -9).addBox(-7.05F, -6.0F, -36.0F, 0.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(116, -9).addBox(6.95F, -6.0F, -36.0F, 0.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(163, 62).addBox(-7.0F, 4.9F, -30.75F, 13.9F, 1.0F, 7.1F, new CubeDeformation(0.0F)), PartPose.offset(-0.2F, 0.0F, 0.0F));

        PartDefinition bucket_slope_r9 = bucket3.addOrReplaceChild("bucket_slope_r9", CubeListBuilder.create().texOffs(163, 62).addBox(-6.95F, -0.45F, -4.0F, 13.9F, 1.0F, 7.1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.05F, 3.45F, -32.75F, -0.7854F, 0.0F, 0.0F));

        PartDefinition bucket4 = wheel_segment2.addOrReplaceChild("bucket4", CubeListBuilder.create().texOffs(140, 3).addBox(-7.05F, -6.0F, -36.0F, 14.0F, 7.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(116, -9).addBox(-7.05F, -6.0F, -36.0F, 0.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(116, -9).addBox(6.95F, -6.0F, -36.0F, 0.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(163, 62).addBox(-7.0F, 4.9F, -30.75F, 13.9F, 1.0F, 7.1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2F, 0.0F, 0.0F, -0.5236F, 0.0F, 0.0F));

        PartDefinition bucket_slope_r10 = bucket4.addOrReplaceChild("bucket_slope_r10", CubeListBuilder.create().texOffs(163, 62).addBox(-6.95F, -0.45F, -4.0F, 13.9F, 1.0F, 7.1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.05F, 3.45F, -32.75F, -0.7854F, 0.0F, 0.0F));

        PartDefinition wheel_segment6 = wheel.addOrReplaceChild("wheel_segment6", CubeListBuilder.create().texOffs(165, 41).addBox(-7.75F, -6.5F, -24.0F, 15.0F, 13.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.25F, 0.0F, 0.0F, 2.0944F, 0.0F, 0.0F));

        PartDefinition extent_r6 = wheel_segment6.addOrReplaceChild("extent_r6", CubeListBuilder.create().texOffs(169, 23).addBox(-7.545F, -6.5F, -24.0F, 14.99F, 13.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2F, 0.0F, 0.0F, -0.5236F, 0.0F, 0.0F));

        PartDefinition bucket11 = wheel_segment6.addOrReplaceChild("bucket11", CubeListBuilder.create().texOffs(140, 3).addBox(-7.05F, -6.0F, -36.0F, 14.0F, 7.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(116, -9).addBox(-7.05F, -6.0F, -36.0F, 0.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(116, -9).addBox(6.95F, -6.0F, -36.0F, 0.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(163, 62).addBox(-7.0F, 4.9F, -30.75F, 13.9F, 1.0F, 7.1F, new CubeDeformation(0.0F)), PartPose.offset(-0.2F, 0.0F, 0.0F));

        PartDefinition bucket_slope_r11 = bucket11.addOrReplaceChild("bucket_slope_r11", CubeListBuilder.create().texOffs(163, 62).addBox(-6.95F, -0.45F, -4.0F, 13.9F, 1.0F, 7.1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.05F, 3.45F, -32.75F, -0.7854F, 0.0F, 0.0F));

        PartDefinition bucket12 = wheel_segment6.addOrReplaceChild("bucket12", CubeListBuilder.create().texOffs(140, 3).addBox(-7.05F, -6.0F, -36.0F, 14.0F, 7.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(116, -9).addBox(-7.05F, -6.0F, -36.0F, 0.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(116, -9).addBox(6.95F, -6.0F, -36.0F, 0.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(163, 62).addBox(-7.0F, 4.9F, -30.75F, 13.9F, 1.0F, 7.1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2F, 0.0F, 0.0F, -0.5236F, 0.0F, 0.0F));

        PartDefinition bucket_slope_r12 = bucket12.addOrReplaceChild("bucket_slope_r12", CubeListBuilder.create().texOffs(163, 62).addBox(-6.95F, -0.45F, -4.0F, 13.9F, 1.0F, 7.1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.05F, 3.45F, -32.75F, -0.7854F, 0.0F, 0.0F));

        PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(112, 96).addBox(-7.0F, -15.0F, -8.0F, 14.0F, 14.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(190, 0).addBox(-8.0F, -16.0F, -8.0F, 16.0F, 1.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(116, 126).addBox(7.0F, -15.0F, -8.0F, 1.0F, 14.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(116, 126).addBox(7.0F, -15.0F, 7.0F, 1.0F, 14.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(116, 126).addBox(-8.0F, -15.0F, 7.0F, 1.0F, 14.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(116, 126).addBox(-8.0F, -15.0F, -8.0F, 1.0F, 14.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 121).addBox(-8.0F, -32.0F, -64.0F, 0.0F, 48.0F, 56.0F, new CubeDeformation(0.0F))
                .texOffs(0, 121).addBox(8.0F, -32.0F, -64.0F, 0.0F, 48.0F, 56.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition mount_bottom_r1 = bb_main.addOrReplaceChild("mount_bottom_r1", CubeListBuilder.create().texOffs(190, 0).addBox(-8.0F, -0.5F, -8.0F, 16.0F, 1.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5F, 0.0F, 0.0F, 3.1416F, 0.0F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

    public void animateWheel(long accumulatedTime) {
        root.getAllParts().forEach(ModelPart::resetPose);
        KeyframeAnimations.animate(this, wheel_rotate, accumulatedTime, 1.0f, ANIMATION_VECTOR_CACHE);
    }

    public static final AnimationDefinition wheel_rotate = AnimationDefinition.Builder.withLength(4.0F).looping()
            .addAnimation("wheel", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(4.0F, KeyframeAnimations.degreeVec(-360.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .build();


    public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay) {
        shield_mesh.render(poseStack, vertexConsumer, packedLight, packedOverlay);
        wheel.render(poseStack, vertexConsumer, packedLight, packedOverlay);
        bb_main.render(poseStack, vertexConsumer, packedLight, packedOverlay);
    }

}
