package com.resourceful_refinement.content.milking_station;

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

public class MilkingStationModel extends HierarchicalModel<Entity> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "milking_station"),
            "main");

    private final ModelPart root;
    private final ModelPart arms_axes;
    private final ModelPart milker_arm;
    private final ModelPart bb_main;

    public MilkingStationModel(ModelPart root) {
        this.root = root;
        this.arms_axes = root.getChild("arms_axes");
        this.milker_arm = this.arms_axes.getChild("milker_arm");
        this.bb_main = root.getChild("bb_main");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition arms_axes = partdefinition.addOrReplaceChild("arms_axes", CubeListBuilder.create(), PartPose.offset(0.0F, 10.0F, 0.0F));

        PartDefinition milker_arm = arms_axes.addOrReplaceChild("milker_arm", CubeListBuilder.create().texOffs(39, 44).addBox(-19.0F, 0.0F, -1.0F, 20.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 44).addBox(-19.0F, -13.0F, 0.0F, 19.0F, 13.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(65, 7).addBox(-20.0F, -13.0F, -1.0F, 1.0F, 14.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -13.0F, -8.0F, 16.0F, 12.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(65, 0).addBox(-5.0F, -15.0F, -8.0F, 10.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 29).addBox(-6.0F, -16.0F, -6.0F, 12.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(49, 29).addBox(-4.0F, -14.0F, -4.0F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(30, 63).addBox(-8.0F, -1.0F, 6.0F, 16.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(40, 50).addBox(-8.0F, -1.0F, -6.0F, 2.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(40, 50).mirror().addBox(6.0F, -1.0F, -6.0F, 2.0F, 1.0F, 12.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition cube_r1 = bb_main.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(30, 63).mirror().addBox(-8.0F, -0.5F, -1.0F, 16.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -0.5F, -7.0F, 0.0F, 3.1416F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    public void render(PoseStack poseStack, VertexConsumer buffer, int light, int overlay) {
        bb_main.render(poseStack, buffer, light, overlay);
        arms_axes.render(poseStack, buffer, light, overlay);
    }

    public void animateArm(float angle) {
        root.getAllParts().forEach(ModelPart::resetPose);
        milker_arm.zRot = angle;
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Driven directly by the block entity renderer.
    }

    @Override
    public ModelPart root() {
        return root;
    }
}
