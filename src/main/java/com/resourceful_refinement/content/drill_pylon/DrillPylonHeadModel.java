package com.resourceful_refinement.content.drill_pylon;

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

public class DrillPylonHeadModel extends HierarchicalModel<Entity> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "drill_pylon_head"),
            "main");

    private final ModelPart root;
    private final ModelPart casing;
    private final ModelPart drill_bit;
    private final ModelPart upper_drill;
    private final ModelPart upper_segment;
    private final ModelPart upper_segment2;
    private final ModelPart upper_segment3;
    private final ModelPart upper_segment4;
    private final ModelPart upper_segment5;
    private final ModelPart upper_segment6;
    private final ModelPart middle_drill;
    private final ModelPart mid_segment;
    private final ModelPart mid_segment2;
    private final ModelPart mid_segment3;
    private final ModelPart mid_segment4;
    private final ModelPart mid_segment5;
    private final ModelPart mid_segment6;
    private final ModelPart lower_drill;
    private final ModelPart lower_segment;
    private final ModelPart lower_segment2;
    private final ModelPart lower_segment3;

    public DrillPylonHeadModel(ModelPart root) {
        this.root = root;

        this.casing = root.getChild("casing");
        this.drill_bit = root.getChild("drill_bit");
        this.upper_drill = this.drill_bit.getChild("upper_drill");
        this.upper_segment = this.upper_drill.getChild("upper_segment");
        this.upper_segment2 = this.upper_drill.getChild("upper_segment2");
        this.upper_segment3 = this.upper_drill.getChild("upper_segment3");
        this.upper_segment4 = this.upper_drill.getChild("upper_segment4");
        this.upper_segment5 = this.upper_drill.getChild("upper_segment5");
        this.upper_segment6 = this.upper_drill.getChild("upper_segment6");
        this.middle_drill = this.drill_bit.getChild("middle_drill");
        this.mid_segment = this.middle_drill.getChild("mid_segment");
        this.mid_segment2 = this.middle_drill.getChild("mid_segment2");
        this.mid_segment3 = this.middle_drill.getChild("mid_segment3");
        this.mid_segment4 = this.middle_drill.getChild("mid_segment4");
        this.mid_segment5 = this.middle_drill.getChild("mid_segment5");
        this.mid_segment6 = this.middle_drill.getChild("mid_segment6");
        this.lower_drill = this.drill_bit.getChild("lower_drill");
        this.lower_segment = this.lower_drill.getChild("lower_segment");
        this.lower_segment2 = this.lower_drill.getChild("lower_segment2");
        this.lower_segment3 = this.lower_drill.getChild("lower_segment3");
        }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition casing = partdefinition.addOrReplaceChild("casing", CubeListBuilder.create().texOffs(76, 225).addBox(-8.0F, -16.0F, 4.0F, 16.0F, 16.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(115, 225).addBox(-11.0F, -19.0F, 4.05F, 22.0F, 22.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(80, 245).addBox(-8.0F, -2.0F, 7.0F, 16.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(60, 222).addBox(6.0F, -14.0F, 7.0F, 2.0F, 12.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition frame_strut_b_r1 = casing.addOrReplaceChild("frame_strut_b_r1", CubeListBuilder.create().texOffs(60, 222).addBox(6.0F, -6.0F, -0.5F, 2.0F, 12.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(80, 245).addBox(-8.0F, 6.0F, -0.5F, 16.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -8.0F, 7.5F, 0.0F, 0.0F, -3.1416F));

        PartDefinition drill_bit = partdefinition.addOrReplaceChild("drill_bit", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 16.0F, 4.0F, -1.5708F, 0.0F, 0.0F));

        PartDefinition upper_drill = drill_bit.addOrReplaceChild("upper_drill", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition upper_segment = upper_drill.addOrReplaceChild("upper_segment", CubeListBuilder.create().texOffs(202, 112).addBox(3.0F, 0.0F, -3.0F, 11.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition upper_joining_segment_r1 = upper_segment.addOrReplaceChild("upper_joining_segment_r1", CubeListBuilder.create().texOffs(185, 198).addBox(3.0F, -2.0F, -4.0F, 10.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.99F, 0.0F, 0.0F, -0.5236F, 0.0F));

        PartDefinition upper_tooth_base_r1 = upper_segment.addOrReplaceChild("upper_tooth_base_r1", CubeListBuilder.create().texOffs(108, 170).addBox(-1.5F, -3.0F, -2.5F, 3.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(126, 217).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(12.0F, 7.5F, 0.0F, 0.0F, 0.0F, 0.0873F));

        PartDefinition upper_segment2 = upper_drill.addOrReplaceChild("upper_segment2", CubeListBuilder.create().texOffs(202, 112).addBox(3.0F, 0.0F, -3.0F, 11.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.0472F, 0.0F));

        PartDefinition upper_joining_segment_r2 = upper_segment2.addOrReplaceChild("upper_joining_segment_r2", CubeListBuilder.create().texOffs(185, 198).addBox(3.0F, -2.0F, -4.0F, 10.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.99F, 0.0F, 0.0F, -0.5236F, 0.0F));

        PartDefinition upper_tooth_base_r2 = upper_segment2.addOrReplaceChild("upper_tooth_base_r2", CubeListBuilder.create().texOffs(108, 170).addBox(-1.5F, -3.0F, -2.5F, 3.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(126, 217).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(12.0F, 7.5F, 0.0F, 0.0F, 0.0F, 0.0873F));

        PartDefinition upper_segment3 = upper_drill.addOrReplaceChild("upper_segment3", CubeListBuilder.create().texOffs(202, 112).addBox(3.0F, 0.0F, -3.0F, 11.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 2.0944F, 0.0F));

        PartDefinition upper_joining_segment_r3 = upper_segment3.addOrReplaceChild("upper_joining_segment_r3", CubeListBuilder.create().texOffs(185, 198).addBox(3.0F, -2.0F, -4.0F, 10.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.99F, 0.0F, 0.0F, -0.5236F, 0.0F));

        PartDefinition upper_tooth_base_r3 = upper_segment3.addOrReplaceChild("upper_tooth_base_r3", CubeListBuilder.create().texOffs(108, 170).addBox(-1.5F, -3.0F, -2.5F, 3.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(126, 217).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(12.0F, 7.5F, 0.0F, 0.0F, 0.0F, 0.0873F));

        PartDefinition upper_segment4 = upper_drill.addOrReplaceChild("upper_segment4", CubeListBuilder.create().texOffs(202, 112).addBox(3.0F, 0.0F, -3.0F, 11.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition upper_joining_segment_r4 = upper_segment4.addOrReplaceChild("upper_joining_segment_r4", CubeListBuilder.create().texOffs(185, 198).addBox(3.0F, -2.0F, -4.0F, 10.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.99F, 0.0F, 0.0F, -0.5236F, 0.0F));

        PartDefinition upper_tooth_base_r4 = upper_segment4.addOrReplaceChild("upper_tooth_base_r4", CubeListBuilder.create().texOffs(108, 170).addBox(-1.5F, -3.0F, -2.5F, 3.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(126, 217).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(12.0F, 7.5F, 0.0F, 0.0F, 0.0F, 0.0873F));

        PartDefinition upper_segment5 = upper_drill.addOrReplaceChild("upper_segment5", CubeListBuilder.create().texOffs(202, 112).addBox(3.0F, 0.0F, -3.0F, 11.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -2.0944F, 0.0F));

        PartDefinition upper_joining_segment_r5 = upper_segment5.addOrReplaceChild("upper_joining_segment_r5", CubeListBuilder.create().texOffs(185, 198).addBox(3.0F, -2.0F, -4.0F, 10.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.99F, 0.0F, 0.0F, -0.5236F, 0.0F));

        PartDefinition upper_tooth_base_r5 = upper_segment5.addOrReplaceChild("upper_tooth_base_r5", CubeListBuilder.create().texOffs(108, 170).addBox(-1.5F, -3.0F, -2.5F, 3.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(126, 217).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(12.0F, 7.5F, 0.0F, 0.0F, 0.0F, 0.0873F));

        PartDefinition upper_segment6 = upper_drill.addOrReplaceChild("upper_segment6", CubeListBuilder.create().texOffs(202, 112).addBox(3.0F, 0.0F, -3.0F, 11.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.0472F, 0.0F));

        PartDefinition upper_joining_segment_r6 = upper_segment6.addOrReplaceChild("upper_joining_segment_r6", CubeListBuilder.create().texOffs(185, 198).addBox(3.0F, -2.0F, -4.0F, 10.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.99F, 0.0F, 0.0F, -0.5236F, 0.0F));

        PartDefinition upper_tooth_base_r6 = upper_segment6.addOrReplaceChild("upper_tooth_base_r6", CubeListBuilder.create().texOffs(108, 170).addBox(-1.5F, -3.0F, -2.5F, 3.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(126, 217).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(12.0F, 7.5F, 0.0F, 0.0F, 0.0F, 0.0873F));

        PartDefinition middle_drill = drill_bit.addOrReplaceChild("middle_drill", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 5.0F, 0.0F, 0.0F, -0.5236F, 0.0F));

        PartDefinition mid_segment = middle_drill.addOrReplaceChild("mid_segment", CubeListBuilder.create().texOffs(210, 9).addBox(2.0F, 0.0F, -3.0F, 8.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition mid_joining_segment_r1 = mid_segment.addOrReplaceChild("mid_joining_segment_r1", CubeListBuilder.create().texOffs(74, 210).addBox(2.5F, -2.0F, -3.0F, 7.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.99F, 0.0F, 0.0F, -0.5236F, 0.0F));

        PartDefinition mid_tooth_r1 = mid_segment.addOrReplaceChild("mid_tooth_r1", CubeListBuilder.create().texOffs(215, 152).addBox(-1.5F, -3.0F, -1.5F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0F, 8.5F, 0.0F, 0.0F, 0.0F, 0.0873F));

        PartDefinition mid_segment2 = middle_drill.addOrReplaceChild("mid_segment2", CubeListBuilder.create().texOffs(210, 9).addBox(2.0F, 0.0F, -3.0F, 8.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.0472F, 0.0F));

        PartDefinition mid_joining_segment_r2 = mid_segment2.addOrReplaceChild("mid_joining_segment_r2", CubeListBuilder.create().texOffs(74, 210).addBox(2.5F, -2.0F, -3.0F, 7.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.99F, 0.0F, 0.0F, -0.5236F, 0.0F));

        PartDefinition mid_tooth_r2 = mid_segment2.addOrReplaceChild("mid_tooth_r2", CubeListBuilder.create().texOffs(215, 152).addBox(-1.5F, -3.0F, -1.5F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0F, 8.5F, 0.0F, 0.0F, 0.0F, 0.0873F));

        PartDefinition mid_segment3 = middle_drill.addOrReplaceChild("mid_segment3", CubeListBuilder.create().texOffs(210, 9).addBox(2.0F, 0.0F, -3.0F, 8.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -2.0944F, 0.0F));

        PartDefinition mid_joining_segment_r3 = mid_segment3.addOrReplaceChild("mid_joining_segment_r3", CubeListBuilder.create().texOffs(74, 210).addBox(2.5F, -2.0F, -3.0F, 7.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.99F, 0.0F, 0.0F, -0.5236F, 0.0F));

        PartDefinition mid_tooth_r3 = mid_segment3.addOrReplaceChild("mid_tooth_r3", CubeListBuilder.create().texOffs(215, 152).addBox(-1.5F, -3.0F, -1.5F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0F, 8.5F, 0.0F, 0.0F, 0.0F, 0.0873F));

        PartDefinition mid_segment4 = middle_drill.addOrReplaceChild("mid_segment4", CubeListBuilder.create().texOffs(210, 9).addBox(2.0F, 0.0F, -3.0F, 8.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition mid_joining_segment_r4 = mid_segment4.addOrReplaceChild("mid_joining_segment_r4", CubeListBuilder.create().texOffs(74, 210).addBox(2.5F, -2.0F, -3.0F, 7.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.99F, 0.0F, 0.0F, -0.5236F, 0.0F));

        PartDefinition mid_tooth_r4 = mid_segment4.addOrReplaceChild("mid_tooth_r4", CubeListBuilder.create().texOffs(215, 152).addBox(-1.5F, -3.0F, -1.5F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0F, 8.5F, 0.0F, 0.0F, 0.0F, 0.0873F));

        PartDefinition mid_segment5 = middle_drill.addOrReplaceChild("mid_segment5", CubeListBuilder.create().texOffs(210, 9).addBox(2.0F, 0.0F, -3.0F, 8.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 2.0944F, 0.0F));

        PartDefinition mid_joining_segment_r5 = mid_segment5.addOrReplaceChild("mid_joining_segment_r5", CubeListBuilder.create().texOffs(74, 210).addBox(2.5F, -2.0F, -3.0F, 7.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.99F, 0.0F, 0.0F, -0.5236F, 0.0F));

        PartDefinition mid_tooth_r5 = mid_segment5.addOrReplaceChild("mid_tooth_r5", CubeListBuilder.create().texOffs(215, 152).addBox(-1.5F, -3.0F, -1.5F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0F, 8.5F, 0.0F, 0.0F, 0.0F, 0.0873F));

        PartDefinition mid_segment6 = middle_drill.addOrReplaceChild("mid_segment6", CubeListBuilder.create().texOffs(210, 9).addBox(2.0F, 0.0F, -3.0F, 8.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.0472F, 0.0F));

        PartDefinition mid_joining_segment_r6 = mid_segment6.addOrReplaceChild("mid_joining_segment_r6", CubeListBuilder.create().texOffs(74, 210).addBox(2.5F, -2.0F, -3.0F, 7.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.99F, 0.0F, 0.0F, -0.5236F, 0.0F));

        PartDefinition mid_tooth_r6 = mid_segment6.addOrReplaceChild("mid_tooth_r6", CubeListBuilder.create().texOffs(215, 152).addBox(-1.5F, -3.0F, -1.5F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0F, 8.5F, 0.0F, 0.0F, 0.0F, 0.0873F));

        PartDefinition lower_drill = drill_bit.addOrReplaceChild("lower_drill", CubeListBuilder.create(), PartPose.offset(0.0F, 5.0F, 0.0F));

        PartDefinition lower_segment = lower_drill.addOrReplaceChild("lower_segment", CubeListBuilder.create().texOffs(212, 45).addBox(0.0F, -6.0F, -2.0F, 5.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(184, 77).addBox(0.0F, -1.0F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 12.0F, 0.0F));

        PartDefinition lower_segment_b_r1 = lower_segment.addOrReplaceChild("lower_segment_b_r1", CubeListBuilder.create().texOffs(212, 55).addBox(0.0F, -2.0F, -2.0F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(206, 216).addBox(0.0F, 2.0F, -1.5F, 4.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.0F, -1.0472F, 0.0F));

        PartDefinition lower_segment2 = lower_drill.addOrReplaceChild("lower_segment2", CubeListBuilder.create().texOffs(212, 45).addBox(0.0F, -6.0F, -2.0F, 5.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(184, 77).addBox(0.0F, -1.0F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, 0.0F, -2.0944F, 0.0F));

        PartDefinition lower_segment_b_r2 = lower_segment2.addOrReplaceChild("lower_segment_b_r2", CubeListBuilder.create().texOffs(212, 55).addBox(0.0F, -2.0F, -2.0F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(206, 216).addBox(0.0F, 2.0F, -1.5F, 4.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.0F, -1.0472F, 0.0F));

        PartDefinition lower_segment3 = lower_drill.addOrReplaceChild("lower_segment3", CubeListBuilder.create().texOffs(212, 45).addBox(0.0F, -6.0F, -2.0F, 5.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(184, 77).addBox(0.0F, -1.0F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, 0.0F, 2.0944F, 0.0F));

        PartDefinition lower_segment_b_r3 = lower_segment3.addOrReplaceChild("lower_segment_b_r3", CubeListBuilder.create().texOffs(212, 55).addBox(0.0F, -2.0F, -2.0F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(206, 216).addBox(0.0F, 2.0F, -1.5F, 4.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.0F, -1.0472F, 0.0F));

        return LayerDefinition.create(meshdefinition, 256, 256);

    }


    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    /// Spins the drill bit about its own axis (the model's z axis, after the bone's -90 degree pitch)
    public void setDrillBitSpin(float radians) {
        drill_bit.zRot = radians;
    }

    public void render(PoseStack poseStack, VertexConsumer buffer, int light, int overlay) {
        casing.render(poseStack, buffer, light, overlay);
        drill_bit.render(poseStack, buffer, light, overlay);
    }

    @Override
    public ModelPart root() {
        return root;
    }
}
