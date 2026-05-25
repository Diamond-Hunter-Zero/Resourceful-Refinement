package com.resourceful_refinement.content.fracking_pump;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.world.entity.Entity;
import org.joml.Vector3f;

public class FrackingPumpBaseModel extends HierarchicalModel<Entity> {
    private final ModelPart root;
    private final Vector3f ANIMATION_VECTOR_CACHE = new Vector3f();

    public FrackingPumpBaseModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition Base = partdefinition.addOrReplaceChild("Base", CubeListBuilder.create().texOffs(53, 84).addBox(-7.0F, -16.0F, -6.0F, 14.0F, 16.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(115, 142).addBox(-8.0F, -16.0F, -8.0F, 16.0F, 16.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition Front_Face_r1 = Base.addOrReplaceChild("Front Face_r1", CubeListBuilder.create().texOffs(152, 142).addBox(-8.0F, -16.0F, -1.0F, 16.0F, 16.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 7.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition Upper_Core = partdefinition.addOrReplaceChild("Upper Core", CubeListBuilder.create().texOffs(106, 84).addBox(-8.0F, -48.0F, -8.0F, 16.0F, 32.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 54).addBox(-7.0F, -48.0F, -6.0F, 14.0F, 32.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(0, 128).addBox(-19.0F, -36.0F, -5.0F, 10.0F, 15.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(144, 0).addBox(-9.0F, -35.0F, -4.0F, 2.0F, 13.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(145, 116).addBox(-16.0F, -42.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(72, 132).addBox(-12.0F, -42.0F, -2.0F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition Front_Face_r2 = Upper_Core.addOrReplaceChild("Front_Face_r2", CubeListBuilder.create().texOffs(106, 84).addBox(-8.0F, -48.0F, -1.0F, 16.0F, 32.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 7.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition Support_Leg = partdefinition.addOrReplaceChild("Support Leg", CubeListBuilder.create().texOffs(143, 87).addBox(16.0F, -4.0F, -24.0F, 8.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(66, 165).addBox(16.5F, 0.0F, -23.5F, 7.0F, 16.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(143, 82).addBox(-10.0F, -15.0F, -15.0F, 20.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition Suspension_Leg = Support_Leg.addOrReplaceChild("Suspension Leg", CubeListBuilder.create().texOffs(100, 119).addBox(-3.5F, -3.0F, -7.0F, 7.0F, 7.0F, 15.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-2.5F, -2.0F, -11.0F, 5.0F, 5.0F, 34.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(14.5F, -10.0F, -14.5F, 0.7854F, -0.7854F, 0.0F));

        PartDefinition Support_Leg2 = partdefinition.addOrReplaceChild("Support_Leg2", CubeListBuilder.create().texOffs(143, 87).addBox(16.0F, -4.0F, -24.0F, 8.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(66, 165).addBox(16.5F, 0.0F, -23.5F, 7.0F, 16.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition Suspension_Leg2 = Support_Leg2.addOrReplaceChild("Suspension_Leg2", CubeListBuilder.create().texOffs(100, 119).addBox(-3.5F, -3.0F, -7.0F, 7.0F, 7.0F, 15.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-2.5F, -2.0F, -11.0F, 5.0F, 5.0F, 34.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(14.5F, -10.0F, -14.5F, 0.7854F, -0.7854F, 0.0F));

        PartDefinition Support_Leg3 = partdefinition.addOrReplaceChild("Support_Leg3", CubeListBuilder.create().texOffs(143, 87).addBox(16.0F, -4.0F, -24.0F, 8.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(143, 82).addBox(-10.0F, -15.0F, -15.0F, 20.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(66, 165).addBox(16.5F, 0.0F, -23.5F, 7.0F, 16.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition Suspension_Leg3 = Support_Leg3.addOrReplaceChild("Suspension_Leg3", CubeListBuilder.create().texOffs(100, 119).addBox(-3.5F, -3.0F, -7.0F, 7.0F, 7.0F, 15.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-2.5F, -2.0F, -11.0F, 5.0F, 5.0F, 34.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(14.5F, -10.0F, -14.5F, 0.7854F, -0.7854F, 0.0F));

        PartDefinition Support_Leg4 = partdefinition.addOrReplaceChild("Support_Leg4", CubeListBuilder.create().texOffs(143, 87).addBox(16.0F, -4.0F, -24.0F, 8.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(143, 82).addBox(-10.0F, -15.0F, -15.0F, 20.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(66, 165).addBox(16.5F, 0.0F, -23.5F, 7.0F, 16.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition Suspension_Leg4 = Support_Leg4.addOrReplaceChild("Suspension_Leg4", CubeListBuilder.create().texOffs(100, 119).addBox(-3.5F, -3.0F, -7.0F, 7.0F, 7.0F, 15.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-2.5F, -2.0F, -11.0F, 5.0F, 5.0F, 34.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(14.5F, -10.0F, -14.5F, 0.7854F, -0.7854F, 0.0F));

        PartDefinition Pistons = partdefinition.addOrReplaceChild("Pistons", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.5F));

        PartDefinition Piston_Valve = Pistons.addOrReplaceChild("Piston Valve", CubeListBuilder.create().texOffs(143, 100).addBox(-2.5F, -5.0F, -2.5F, 5.0F, 10.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-11.768F, -11.1961F, -3.5F, 0.0F, 0.0F, -0.4363F));

        PartDefinition Pipe_r1 = Piston_Valve.addOrReplaceChild("Pipe_r1", CubeListBuilder.create().texOffs(144, 26).addBox(-1.25F, -2.5F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 7.0F, 0.0F, 0.0F, 0.0F, -0.3054F));

        PartDefinition Cap = Piston_Valve.addOrReplaceChild("Cap", CubeListBuilder.create(), PartPose.offset(2.5F, -5.0F, 0.0F));

        PartDefinition Cap_r1 = Cap.addOrReplaceChild("Cap_r1", CubeListBuilder.create().texOffs(89, 47).addBox(-5.0F, 0.0F, -2.5F, 5.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.3491F));

        PartDefinition Pistons2 = partdefinition.addOrReplaceChild("Pistons2", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 6.5F));

        PartDefinition Piston_Valve2 = Pistons2.addOrReplaceChild("Piston_Valve2", CubeListBuilder.create().texOffs(143, 100).addBox(-2.5F, -5.0F, -2.5F, 5.0F, 10.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-11.768F, -11.1961F, -3.5F, 0.0F, 0.0F, -0.4363F));

        PartDefinition Pipe_r2 = Piston_Valve2.addOrReplaceChild("Pipe_r2", CubeListBuilder.create().texOffs(144, 26).addBox(-1.25F, -2.5F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 7.0F, 0.0F, 0.0F, 0.0F, -0.3054F));

        PartDefinition Cap2 = Piston_Valve2.addOrReplaceChild("Cap2", CubeListBuilder.create(), PartPose.offset(2.5F, -5.0F, 0.0F));

        PartDefinition Cap_r2 = Cap2.addOrReplaceChild("Cap_r2", CubeListBuilder.create().texOffs(89, 47).addBox(-5.0F, 0.0F, -2.5F, 5.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.3491F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int light, int overlay, int color) {
        root.render(poseStack, buffer, light, overlay, color);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Not used
    }

    public void animatePistons(long accumulatedTime) {
        root.getAllParts().forEach(ModelPart::resetPose);
        KeyframeAnimations.animate(this, pistons_cycle, accumulatedTime, 1.0f, ANIMATION_VECTOR_CACHE);
    }

    // Piston animation
    public static final AnimationDefinition pistons_cycle = AnimationDefinition.Builder.withLength(0.625F).looping()
		.addAnimation("Piston Valve", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
		))
		.addAnimation("Piston_Valve2", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
		))
		.addAnimation("Cap", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 7.5F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(0.1667F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -10.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(0.3333F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 7.5F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -10.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(0.625F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 7.5F), AnimationChannel.Interpolations.LINEAR)
		))
		.addAnimation("Cap2", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -10.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(0.1667F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 7.5F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(0.3333F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -10.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 7.5F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(0.625F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -10.0F), AnimationChannel.Interpolations.LINEAR)
		))
		.build();
}
