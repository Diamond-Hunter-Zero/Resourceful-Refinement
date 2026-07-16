package com.resourceful_refinement.content.research_terminal;

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
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.joml.Vector3f;

public class ResearchTerminalModel extends HierarchicalModel<Entity> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "research_terminal"),
            "main");

    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "textures/block/research_terminal.png");

    private final Vector3f ANIMATION_VECTOR_CACHE = new Vector3f();

    private final ModelPart root;

    private final ModelPart bb_main;
    private final ModelPart light_rays;
    private final ModelPart scanner;

    public ResearchTerminalModel(ModelPart root) {
        this.root = root;

        this.light_rays = root.getChild("light_rays");
        this.bb_main = root.getChild("bb_main");
        this.scanner = root.getChild("scanner");
    }

    @Override
    public ModelPart root() {
        return root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition light_rays = partdefinition.addOrReplaceChild("light_rays", CubeListBuilder.create(), PartPose.offset(0.0F, 6.5328F, 0.0F));

        PartDefinition ray_panel_r1 = light_rays.addOrReplaceChild("ray panel_r1", CubeListBuilder.create().texOffs(0, 57).addBox(-12.0F, -14.0F, 0.0F, 24.0F, 14.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.75F, 5.4672F, 0.0F, 0.0F, -1.5708F, 0.1309F));

        PartDefinition ray_panel_r2 = light_rays.addOrReplaceChild("ray_panel_r2", CubeListBuilder.create().texOffs(0, 57).addBox(-12.0F, -14.0F, 0.0F, 24.0F, 14.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.75F, 5.4672F, 0.0F, 0.0F, -1.5708F, -0.1309F));

        PartDefinition ray_panel_r3 = light_rays.addOrReplaceChild("ray_panel_r3", CubeListBuilder.create().texOffs(47, 57).addBox(-12.0F, -14.0F, 0.0F, 24.0F, 14.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 5.4672F, -6.75F, 0.1309F, 0.0F, 0.0F));

        PartDefinition ray_panel_r4 = light_rays.addOrReplaceChild("ray_panel_r4", CubeListBuilder.create().texOffs(0, 57).addBox(-12.0F, -14.0F, 0.0F, 24.0F, 14.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 5.4672F, 6.75F, -0.1309F, 0.0F, 0.0F));

        PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(0, 0).addBox(-16.0F, -11.0F, 0.0F, 16.0F, 11.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 27).addBox(-15.0F, -13.0F, 1.0F, 14.0F, 2.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, 24.0F, -8.0F));

        PartDefinition keypad_r1 = bb_main.addOrReplaceChild("keypad_r1", CubeListBuilder.create().texOffs(64, 0).addBox(-5.0F, -2.8F, -0.5F, 10.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.0F, -11.3F, 0.6F, -0.3927F, 0.0F, 0.0F));

        PartDefinition scanner = partdefinition.addOrReplaceChild("scanner", CubeListBuilder.create().texOffs(0, 43).addBox(-7.0F, -15.0F, -7.0F, 14.0F, 0.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

    public static final AnimationDefinition scan_loop = AnimationDefinition.Builder.withLength(6.0F).looping()
            .addAnimation("scanner", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(3.0F, KeyframeAnimations.posVec(0.0F, 7.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(6.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .build();

    public void animateScan(long accumulatedTime) {
        root.getAllParts().forEach(ModelPart::resetPose);
        KeyframeAnimations.animate(this, scan_loop, accumulatedTime, 1.0f, ANIMATION_VECTOR_CACHE);
    }

    public void render(boolean isScanning, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {

        VertexConsumer cutoutBuffer = buffer.getBuffer(RenderType.entityCutout(TEXTURE));
        bb_main.render(poseStack, cutoutBuffer, packedLight, packedOverlay);

        if (isScanning)
        {
            scanner.render(poseStack, cutoutBuffer, packedLight, packedOverlay);

            VertexConsumer translucentBuffer = buffer.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE));
            light_rays.render(poseStack, translucentBuffer, packedLight, packedOverlay);
        }
    }
}
