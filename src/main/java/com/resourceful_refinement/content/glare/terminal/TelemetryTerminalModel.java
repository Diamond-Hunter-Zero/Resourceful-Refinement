package com.resourceful_refinement.content.glare.terminal;

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

public class TelemetryTerminalModel extends HierarchicalModel<Entity> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "glare_telemetry_terminal"),
            "main");

    private final ModelPart root;

    private final ModelPart frame;
    private final ModelPart keyboard;
    private final ModelPart light_left;
    private final ModelPart light_right;

    public TelemetryTerminalModel(ModelPart root) {
        this.root = root;

        this.frame = root.getChild("frame");
        this.keyboard = this.frame.getChild("keyboard");
        this.light_left = root.getChild("light_left");
        this.light_right = root.getChild("light_right");
    }

    @Override
    public ModelPart root() {
        return root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition frame = partdefinition.addOrReplaceChild("frame", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -2.0F, -8.0F, 16.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 32).addBox(-6.0F, -15.0F, 2.0F, 12.0F, 7.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 19).addBox(-8.0F, -8.0F, 2.0F, 16.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(37, 32).addBox(-8.0F, -6.0F, -1.0F, 16.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(46, 23).addBox(-8.0F, -9.0F, 3.0F, 2.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(46, 23).mirror().addBox(6.0F, -9.0F, 3.0F, 2.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(16, 37).addBox(-8.0F, -8.0F, -8.0F, 0.0F, 8.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(16, 37).addBox(8.0F, -8.0F, -8.0F, 0.0F, 8.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition keyboard = frame.addOrReplaceChild("keyboard", CubeListBuilder.create().texOffs(37, 44).addBox(-6.0F, -1.0F, -8.0F, 12.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(37, 40).addBox(-7.0F, -2.0F, -6.0F, 14.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(45, 19).addBox(-6.0F, -3.0F, -4.0F, 12.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(45, 23).addBox(4.0F, -2.0F, -4.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(45, 23).addBox(-5.0F, -2.0F, -4.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(45, 23).addBox(4.0F, -1.0F, -6.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(45, 23).addBox(-5.0F, -1.0F, -6.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, 1.0F));

        PartDefinition light_left = partdefinition.addOrReplaceChild("light_left", CubeListBuilder.create().texOffs(0, 46).addBox(-8.0F, -15.0F, 3.0F, 3.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition light_right = partdefinition.addOrReplaceChild("light_right", CubeListBuilder.create().texOffs(0, 46).mirror().addBox(5.0F, -15.0F, 3.0F, 3.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

    public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int colour) {
        frame.render(poseStack, vertexConsumer, packedLight, packedOverlay);
        light_left.render(poseStack, vertexConsumer, packedLight, packedOverlay, colour);
        light_right.render(poseStack, vertexConsumer, packedLight, packedOverlay, colour);
    }

}
