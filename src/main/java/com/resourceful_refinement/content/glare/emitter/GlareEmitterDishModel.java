package com.resourceful_refinement.content.glare.emitter;

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

public class GlareEmitterDishModel extends HierarchicalModel<Entity> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "glare_emitter_dish"),
            "main");
    public static final int RING_ANIMATION_DURATION = 8;

    private final Vector3f ANIMATION_VECTOR_CACHE = new Vector3f();

    private final ModelPart root;

    private final ModelPart frame;
    private final ModelPart base_frame;
    private final ModelPart Hollow_Ring;
    private final ModelPart Hollow_Ring2;
    private final ModelPart Hollow_Ring3;


    public GlareEmitterDishModel(ModelPart root) {
        this.root = root;

        this.frame = root.getChild("frame");
        this.base_frame = this.frame.getChild("base_frame");
        this.Hollow_Ring = root.getChild("Hollow_Ring");
        this.Hollow_Ring2 = root.getChild("Hollow_Ring2");
        this.Hollow_Ring3 = root.getChild("Hollow_Ring3");
    }

    @Override
    public ModelPart root() {
        return root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition frame = partdefinition.addOrReplaceChild("frame", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition base_frame = frame.addOrReplaceChild("base_frame", CubeListBuilder.create().texOffs(83, 43).addBox(-8.0F, -2.0F, -8.0F, 2.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(83, 43).mirror().addBox(6.0F, -2.0F, -8.0F, 2.0F, 2.0F, 16.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(88, 78).addBox(-6.0F, -2.0F, -5.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(88, 78).addBox(-6.0F, -2.0F, 3.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(54, 91).addBox(-8.0F, -16.0F, -7.05F, 16.0F, 16.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(47, 0).addBox(-8.0F, -16.0F, -8.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(47, 0).mirror().addBox(6.0F, -16.0F, -8.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition upper_leg_strut_r1 = base_frame.addOrReplaceChild("upper_leg_strut_r1", CubeListBuilder.create().texOffs(108, 62).addBox(0.0F, -1.0F, -1.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.0F, 0.0F, -7.0F, -1.5708F, 0.0F, 1.5708F));

        PartDefinition lower_leg_strut_r1 = base_frame.addOrReplaceChild("lower_leg_strut_r1", CubeListBuilder.create().texOffs(108, 67).addBox(0.0F, -1.0F, -1.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.0F, 7.5F, -7.0F, -1.5474F, -0.2608F, 1.3059F));

        PartDefinition upper_leg_strut_r2 = base_frame.addOrReplaceChild("upper_leg_strut_r2", CubeListBuilder.create().texOffs(108, 62).addBox(0.0F, -1.0F, -1.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.0F, 0.0F, -7.0F, 3.1416F, 0.0F, 1.5708F));

        PartDefinition lower_leg_strut_r2 = base_frame.addOrReplaceChild("lower_leg_strut_r2", CubeListBuilder.create().texOffs(108, 67).addBox(0.0F, -1.0F, -1.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.0F, 7.5F, -7.0F, 3.0951F, -0.2557F, 1.8406F));

        PartDefinition lower_leg_strut_r3 = base_frame.addOrReplaceChild("lower_leg_strut_r3", CubeListBuilder.create().texOffs(108, 67).addBox(0.0F, -1.0F, -1.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.0F, 7.5F, 7.0F, 1.5941F, 0.2608F, 1.8357F));

        PartDefinition upper_leg_strut_r3 = base_frame.addOrReplaceChild("upper_leg_strut_r3", CubeListBuilder.create().texOffs(108, 62).addBox(0.0F, -1.0F, -1.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.0F, 0.0F, 7.0F, 1.5708F, 0.0F, 1.5708F));

        PartDefinition lower_leg_strut_r4 = base_frame.addOrReplaceChild("lower_leg_strut_r4", CubeListBuilder.create().texOffs(108, 67).addBox(0.0F, -1.0F, -1.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.0F, 7.5F, 7.0F, -0.0465F, 0.2557F, 1.301F));

        PartDefinition upper_leg_strut_r4 = base_frame.addOrReplaceChild("upper_leg_strut_r4", CubeListBuilder.create().texOffs(108, 62).addBox(0.0F, -1.0F, -1.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.0F, 0.0F, 7.0F, 0.0F, 0.0F, 1.5708F));

        PartDefinition strut_rail_r1 = base_frame.addOrReplaceChild("strut_rail_r1", CubeListBuilder.create().texOffs(88, 83).mirror().addBox(-6.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(7.0F, -8.0F, -4.0F, 3.1416F, 0.0F, -1.5708F));

        PartDefinition strut_rail_r2 = base_frame.addOrReplaceChild("strut_rail_r2", CubeListBuilder.create().texOffs(88, 83).addBox(-6.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -15.0F, -4.0F, -3.1416F, 0.0F, 3.1416F));

        PartDefinition strut_rail_r3 = base_frame.addOrReplaceChild("strut_rail_r3", CubeListBuilder.create().texOffs(88, 83).addBox(-6.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.0F, -8.0F, -4.0F, 3.1416F, 0.0F, 1.5708F));


        PartDefinition Hollow_Ring = partdefinition.addOrReplaceChild("Hollow_Ring", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 25.0F, 0.0F, -1.5708F, 0.0F, 3.1416F));

        PartDefinition rim_r9 = Hollow_Ring.addOrReplaceChild("rim_r9", CubeListBuilder.create().texOffs(82, 19).addBox(-3.0F, 5.1F, -4.95F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, 2.899F, 0.0F, 0.0F, -1.1781F));

        PartDefinition rim_r10 = Hollow_Ring.addOrReplaceChild("rim_r10", CubeListBuilder.create().texOffs(82, 19).addBox(-3.0F, 5.1F, -4.85F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, 2.8F, 0.0F, 0.0F, -2.7489F));

        PartDefinition rim_r11 = Hollow_Ring.addOrReplaceChild("rim_r11", CubeListBuilder.create().texOffs(82, 19).addBox(-3.0F, 5.1F, -4.95F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, 2.899F, 0.0F, 0.0F, -1.9635F));

        PartDefinition rim_r12 = Hollow_Ring.addOrReplaceChild("rim_r12", CubeListBuilder.create().texOffs(82, 19).addBox(-3.0F, 5.1F, -4.95F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, 2.899F, 0.0F, 0.0F, -0.3927F));

        PartDefinition rim_r13 = Hollow_Ring.addOrReplaceChild("rim_r13", CubeListBuilder.create().texOffs(82, 19).addBox(-3.0F, 5.1F, -4.95F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, 2.899F, 0.0F, 0.0F, 1.9635F));

        PartDefinition rim_r14 = Hollow_Ring.addOrReplaceChild("rim_r14", CubeListBuilder.create().texOffs(82, 19).addBox(-3.0F, 5.1F, -4.95F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, 2.899F, 0.0F, 0.0F, 2.7489F));

        PartDefinition rim_r15 = Hollow_Ring.addOrReplaceChild("rim_r15", CubeListBuilder.create().texOffs(82, 19).addBox(-3.0F, 5.1F, -4.95F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, 2.899F, 0.0F, 0.0F, 1.1781F));

        PartDefinition rim_r16 = Hollow_Ring.addOrReplaceChild("rim_r16", CubeListBuilder.create().texOffs(82, 19).addBox(-3.0F, 5.1F, -4.85F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, 2.8F, 0.0F, 0.0F, 0.3927F));


        PartDefinition Hollow_Ring2 = partdefinition.addOrReplaceChild("Hollow_Ring2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 28.0F, 0.0F, -1.5708F, 0.0F, 3.1416F));

        PartDefinition rim_r17 = Hollow_Ring2.addOrReplaceChild("rim_r17", CubeListBuilder.create().texOffs(82, 19).addBox(-3.0F, 5.1F, -4.95F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, 2.899F, 0.0F, 0.0F, -1.1781F));

        PartDefinition rim_r18 = Hollow_Ring2.addOrReplaceChild("rim_r18", CubeListBuilder.create().texOffs(82, 19).addBox(-3.0F, 5.1F, -4.85F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, 2.8F, 0.0F, 0.0F, -2.7489F));

        PartDefinition rim_r19 = Hollow_Ring2.addOrReplaceChild("rim_r19", CubeListBuilder.create().texOffs(82, 19).addBox(-3.0F, 5.1F, -4.95F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, 2.899F, 0.0F, 0.0F, -1.9635F));

        PartDefinition rim_r20 = Hollow_Ring2.addOrReplaceChild("rim_r20", CubeListBuilder.create().texOffs(82, 19).addBox(-3.0F, 5.1F, -4.95F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, 2.899F, 0.0F, 0.0F, -0.3927F));

        PartDefinition rim_r21 = Hollow_Ring2.addOrReplaceChild("rim_r21", CubeListBuilder.create().texOffs(82, 19).addBox(-3.0F, 5.1F, -4.95F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, 2.899F, 0.0F, 0.0F, 1.9635F));

        PartDefinition rim_r22 = Hollow_Ring2.addOrReplaceChild("rim_r22", CubeListBuilder.create().texOffs(82, 19).addBox(-3.0F, 5.1F, -4.95F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, 2.899F, 0.0F, 0.0F, 2.7489F));

        PartDefinition rim_r23 = Hollow_Ring2.addOrReplaceChild("rim_r23", CubeListBuilder.create().texOffs(82, 19).addBox(-3.0F, 5.1F, -4.95F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, 2.899F, 0.0F, 0.0F, 1.1781F));

        PartDefinition rim_r24 = Hollow_Ring2.addOrReplaceChild("rim_r24", CubeListBuilder.create().texOffs(82, 19).addBox(-3.0F, 5.1F, -4.85F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, 2.8F, 0.0F, 0.0F, 0.3927F));


        PartDefinition Hollow_Ring3 = partdefinition.addOrReplaceChild("Hollow_Ring3", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 31.0F, 0.0F, -1.5708F, 0.0F, 3.1416F));

        PartDefinition rim_r25 = Hollow_Ring3.addOrReplaceChild("rim_r25", CubeListBuilder.create().texOffs(82, 19).addBox(-3.0F, 5.1F, -4.95F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, 2.899F, 0.0F, 0.0F, -1.1781F));

        PartDefinition rim_r26 = Hollow_Ring3.addOrReplaceChild("rim_r26", CubeListBuilder.create().texOffs(82, 19).addBox(-3.0F, 5.1F, -4.85F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, 2.8F, 0.0F, 0.0F, -2.7489F));

        PartDefinition rim_r27 = Hollow_Ring3.addOrReplaceChild("rim_r27", CubeListBuilder.create().texOffs(82, 19).addBox(-3.0F, 5.1F, -4.95F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, 2.899F, 0.0F, 0.0F, -1.9635F));

        PartDefinition rim_r28 = Hollow_Ring3.addOrReplaceChild("rim_r28", CubeListBuilder.create().texOffs(82, 19).addBox(-3.0F, 5.1F, -4.95F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, 2.899F, 0.0F, 0.0F, -0.3927F));

        PartDefinition rim_r29 = Hollow_Ring3.addOrReplaceChild("rim_r29", CubeListBuilder.create().texOffs(82, 19).addBox(-3.0F, 5.1F, -4.95F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, 2.899F, 0.0F, 0.0F, 1.9635F));

        PartDefinition rim_r30 = Hollow_Ring3.addOrReplaceChild("rim_r30", CubeListBuilder.create().texOffs(82, 19).addBox(-3.0F, 5.1F, -4.95F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, 2.899F, 0.0F, 0.0F, 2.7489F));

        PartDefinition rim_r31 = Hollow_Ring3.addOrReplaceChild("rim_r31", CubeListBuilder.create().texOffs(82, 19).addBox(-3.0F, 5.1F, -4.95F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, 2.899F, 0.0F, 0.0F, 1.1781F));

        PartDefinition rim_r32 = Hollow_Ring3.addOrReplaceChild("rim_r32", CubeListBuilder.create().texOffs(82, 19).addBox(-3.0F, 5.1F, -4.85F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, 2.8F, 0.0F, 0.0F, 0.3927F));


        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

    public void animateRings(long accumulatedTime) {
        root.getAllParts().forEach(ModelPart::resetPose);
        KeyframeAnimations.animate(this, glare_emitter_rotate_rings, accumulatedTime, 1.0f, ANIMATION_VECTOR_CACHE);
    }

    public static final AnimationDefinition glare_emitter_rotate_rings = AnimationDefinition.Builder.withLength(RING_ANIMATION_DURATION).looping()
            .addAnimation("Hollow_Ring", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, -1440.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("Hollow_Ring2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, 1080.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("Hollow_Ring3", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(8.0F, KeyframeAnimations.degreeVec(0.0F, -720.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .build();



    public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay) {
        frame.render(poseStack, vertexConsumer, packedLight, packedOverlay);
        Hollow_Ring.render(poseStack, vertexConsumer, packedLight, packedOverlay);
        Hollow_Ring2.render(poseStack, vertexConsumer, packedLight, packedOverlay);
        Hollow_Ring3.render(poseStack, vertexConsumer, packedLight, packedOverlay);
    }

}
