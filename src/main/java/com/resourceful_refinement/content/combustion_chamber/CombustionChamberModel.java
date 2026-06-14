package com.resourceful_refinement.content.combustion_chamber;

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

public class CombustionChamberModel extends HierarchicalModel<Entity> {

        public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
                ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "combustion_chamber"),
                "main"
        );
    private final Vector3f ANIMATION_VECTOR_CACHE = new Vector3f();

        private final ModelPart root;
        private final ModelPart pistons_rack;
        private final ModelPart piston_cylinder;
        private final ModelPart piston_cap;
        private final ModelPart piston_cylinder3;
        private final ModelPart piston_cap3;
        private final ModelPart piston_cylinder2;
        private final ModelPart piston_cap2;
        private final ModelPart pistons_rack_2;
        private final ModelPart piston_cylinder4;
        private final ModelPart piston_cap4;
        private final ModelPart piston_cylinder5;
        private final ModelPart piston_cap5;
        private final ModelPart piston_cylinder6;
        private final ModelPart piston_cap6;
        private final ModelPart bb_main;

	public CombustionChamberModel(ModelPart root) {
            this.root = root;
            this.pistons_rack = root.getChild("pistons_rack");
            this.piston_cylinder = this.pistons_rack.getChild("piston_cylinder");
            this.piston_cap = this.piston_cylinder.getChild("piston_cap");
            this.piston_cylinder3 = this.pistons_rack.getChild("piston_cylinder3");
            this.piston_cap3 = this.piston_cylinder3.getChild("piston_cap3");
            this.piston_cylinder2 = this.pistons_rack.getChild("piston_cylinder2");
            this.piston_cap2 = this.piston_cylinder2.getChild("piston_cap2");
            this.pistons_rack_2 = root.getChild("pistons_rack_2");
            this.piston_cylinder4 = this.pistons_rack_2.getChild("piston_cylinder4");
            this.piston_cap4 = this.piston_cylinder4.getChild("piston_cap4");
            this.piston_cylinder5 = this.pistons_rack_2.getChild("piston_cylinder5");
            this.piston_cap5 = this.piston_cylinder5.getChild("piston_cap5");
            this.piston_cylinder6 = this.pistons_rack_2.getChild("piston_cylinder6");
            this.piston_cap6 = this.piston_cylinder6.getChild("piston_cap6");
            this.bb_main = root.getChild("bb_main");
        }

        public static LayerDefinition createBodyLayer() {
            MeshDefinition meshdefinition = new MeshDefinition();
            PartDefinition partdefinition = meshdefinition.getRoot();

            PartDefinition pistons_rack = partdefinition.addOrReplaceChild("pistons_rack", CubeListBuilder.create(), PartPose.offset(-0.2F, 24.0F, 0.0F));

            PartDefinition piston_cylinder = pistons_rack.addOrReplaceChild("piston_cylinder", CubeListBuilder.create().texOffs(57, 0).addBox(-2.0F, -3.5F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.25F, -9.5F, 0.0F, 0.0F, 0.0F, -0.3054F));

            PartDefinition piston_cap = piston_cylinder.addOrReplaceChild("piston_cap", CubeListBuilder.create().texOffs(53, 33).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                    .texOffs(41, 66).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -3.5F, 0.0F));

            PartDefinition piston_cylinder3 = pistons_rack.addOrReplaceChild("piston_cylinder3", CubeListBuilder.create().texOffs(57, 0).addBox(-2.0F, -3.5F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.25F, -9.5F, 4.5F, 0.0F, 0.0F, -0.3054F));

            PartDefinition piston_cap3 = piston_cylinder3.addOrReplaceChild("piston_cap3", CubeListBuilder.create().texOffs(53, 33).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                    .texOffs(41, 66).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -3.5F, 0.0F));

            PartDefinition piston_cylinder2 = pistons_rack.addOrReplaceChild("piston_cylinder2", CubeListBuilder.create().texOffs(57, 0).addBox(-2.0F, -3.5F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.25F, -9.5F, -4.5F, 0.0F, 0.0F, -0.3054F));

            PartDefinition piston_cap2 = piston_cylinder2.addOrReplaceChild("piston_cap2", CubeListBuilder.create().texOffs(53, 33).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                    .texOffs(41, 66).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -3.5F, 0.0F));

            PartDefinition pistons_rack_2 = partdefinition.addOrReplaceChild("pistons_rack_2", CubeListBuilder.create(), PartPose.offset(0.2F, 24.0F, 0.0F));

            PartDefinition piston_cylinder4 = pistons_rack_2.addOrReplaceChild("piston_cylinder4", CubeListBuilder.create().texOffs(57, 0).mirror().addBox(-2.0F, -3.5F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(5.25F, -9.5F, 0.0F, 0.0F, 0.0F, 0.3054F));

            PartDefinition piston_cap4 = piston_cylinder4.addOrReplaceChild("piston_cap4", CubeListBuilder.create().texOffs(53, 33).mirror().addBox(-2.0F, -1.0F, -2.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
                    .texOffs(41, 66).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, -3.5F, 0.0F));

            PartDefinition piston_cylinder5 = pistons_rack_2.addOrReplaceChild("piston_cylinder5", CubeListBuilder.create().texOffs(57, 0).mirror().addBox(-2.0F, -3.5F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(5.25F, -9.5F, 4.5F, 0.0F, 0.0F, 0.3054F));

            PartDefinition piston_cap5 = piston_cylinder5.addOrReplaceChild("piston_cap5", CubeListBuilder.create().texOffs(53, 33).mirror().addBox(-2.0F, -1.0F, -2.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
                    .texOffs(41, 66).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, -3.5F, 0.0F));

            PartDefinition piston_cylinder6 = pistons_rack_2.addOrReplaceChild("piston_cylinder6", CubeListBuilder.create().texOffs(57, 0).mirror().addBox(-2.0F, -3.5F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(5.25F, -9.5F, -4.5F, 0.0F, 0.0F, 0.3054F));

            PartDefinition piston_cap6 = piston_cylinder6.addOrReplaceChild("piston_cap6", CubeListBuilder.create().texOffs(53, 33).mirror().addBox(-2.0F, -1.0F, -2.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
                    .texOffs(41, 66).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, -3.5F, 0.0F));

            PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(0, 21).addBox(-5.0F, -16.0F, -8.0F, 10.0F, 1.0F, 16.0F, new CubeDeformation(0.0F))
                    .texOffs(0, 0).addBox(-7.0F, -6.0F, -7.0F, 14.0F, 6.0F, 14.0F, new CubeDeformation(0.0F))
                    .texOffs(0, 39).addBox(-4.0F, -15.0F, -7.0F, 8.0F, 9.0F, 14.0F, new CubeDeformation(0.0F))
                    .texOffs(45, 39).addBox(-8.0F, -10.0F, -8.0F, 1.0F, 10.0F, 16.0F, new CubeDeformation(0.0F))
                    .texOffs(0, 63).addBox(-7.0F, -10.0F, -8.0F, 4.0F, 10.0F, 1.0F, new CubeDeformation(0.0F))
                    .texOffs(11, 63).addBox(-3.0F, -5.0F, -8.0F, 6.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
                    .texOffs(26, 63).addBox(-3.0F, -15.0F, -8.0F, 6.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                    .texOffs(50, 66).addBox(-5.0F, -15.0F, -8.0F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
                    .texOffs(53, 21).addBox(-7.0F, -10.0F, 7.0F, 14.0F, 10.0F, 1.0F, new CubeDeformation(0.0F))
                    .texOffs(57, 13).addBox(-5.0F, -15.0F, 7.0F, 10.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
                    .texOffs(50, 66).mirror().addBox(3.0F, -15.0F, -8.0F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                    .texOffs(0, 41).mirror().addBox(3.0F, -10.0F, -8.0F, 4.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 24.0F, 0.0F));

            PartDefinition cube_r1 = bb_main.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(45, 39).addBox(-8.0F, -10.0F, -8.0F, 1.0F, 10.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

            PartDefinition arch_strut_r1 = bb_main.addOrReplaceChild("arch_strut_r1", CubeListBuilder.create().texOffs(57, 66).mirror().addBox(-0.5F, -6.5F, -0.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                    .texOffs(57, 66).mirror().addBox(-0.5F, -6.5F, -15.3F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(7.25F, -9.9F, 7.4F, 0.0F, 0.0F, -0.4363F));

            PartDefinition arch_strut_r2 = bb_main.addOrReplaceChild("arch_strut_r2", CubeListBuilder.create().texOffs(57, 66).addBox(-0.5F, -6.5F, -0.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                    .texOffs(57, 66).addBox(-0.5F, -6.5F, -15.3F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.25F, -9.9F, 7.4F, 0.0F, 0.0F, 0.4363F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    public void render(PoseStack poseStack, VertexConsumer buffer, int light, int overlay) {
        bb_main.render(poseStack, buffer, light, overlay);
        pistons_rack.render(poseStack, buffer, light, overlay);
        pistons_rack_2.render(poseStack, buffer, light, overlay);
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Not used
    }

    public void animatePistons(long accumulatedTime) {
        root.getAllParts().forEach(ModelPart::resetPose);
        KeyframeAnimations.animate(this, engine_active_loop, accumulatedTime, 1.0f, ANIMATION_VECTOR_CACHE);
    }

    public static final AnimationDefinition engine_active_loop = AnimationDefinition.Builder.withLength(0.5F).looping()
            .addAnimation("piston_cap", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 2.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, 2.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("piston_cap2", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, 2.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("piston_cap3", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, 2.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("piston_cap4", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, 2.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("piston_cap5", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 2.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, 2.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("piston_cap6", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 2.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, 2.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .build();

    @Override
    public ModelPart root() {
        return root;
    }
}
