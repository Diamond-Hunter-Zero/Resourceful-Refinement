package com.resourceful_refinement.content.tesla_coil;

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

/**
 * The Tesla Coil head: a neck sitting on the shaft and a bevelled dome. Modelled pointing up; the renderer orients it
 * to the block's FACING and spins it about that axis with the kinetic network.
 */
public class TeslaCoilHeadModel extends HierarchicalModel<Entity> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "tesla_coil_head"), "main");

    private final ModelPart root;
    private final ModelPart head;

    public TeslaCoilHeadModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
    }

    @Override
    public ModelPart root() {
        return root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(25, 32).addBox(-4.0F, -12.0F, -8.0F, 8.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 14).addBox(-4.0F, -14.0F, -4.0F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 32).addBox(-3.0F, -5.0F, -3.0F, 6.0F, 5.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 23).addBox(-4.0F, -5.0F, -4.0F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-8.0F, -12.0F, -4.0F, 16.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        head.addOrReplaceChild("lower_bevel_r1", CubeListBuilder.create().texOffs(33, 23).addBox(-4.0F, -5.8568F, -5.6569F, 8.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -7.0F, 0.0F, 0.0F, 1.5708F, -0.7854F));
        head.addOrReplaceChild("lower_bevel_r2", CubeListBuilder.create().texOffs(33, 23).addBox(-4.0F, -5.8568F, -5.6569F, 8.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -7.0F, 0.0F, 0.0F, -1.5708F, 0.7854F));
        head.addOrReplaceChild("lower_bevel_r3", CubeListBuilder.create().texOffs(33, 23).addBox(-4.0F, -5.8568F, -5.6569F, 8.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -7.0F, 0.0F, -2.3562F, 0.0F, 3.1416F));
        head.addOrReplaceChild("lower_bevel_r4", CubeListBuilder.create().texOffs(33, 23).addBox(-4.0F, -5.8568F, -5.6569F, 8.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -7.0F, 0.0F, 0.7854F, 0.0F, 0.0F));

        head.addOrReplaceChild("top_bevel_r1", CubeListBuilder.create().texOffs(33, 14).addBox(-4.0F, -2.6787F, -9.1924F, 8.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -7.0F, 0.0F, 2.3562F, 0.0F, -3.1416F));
        head.addOrReplaceChild("top_bevel_r2", CubeListBuilder.create().texOffs(33, 14).addBox(-4.0F, 0.8568F, -5.6569F, 8.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, 0.0F, 1.5708F, 0.7854F));
        head.addOrReplaceChild("top_bevel_r3", CubeListBuilder.create().texOffs(33, 14).addBox(-4.0F, 0.8568F, -5.6569F, 8.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, 0.0F, -1.5708F, -0.7854F));
        head.addOrReplaceChild("top_bevel_r4", CubeListBuilder.create().texOffs(33, 14).addBox(-4.0F, 0.8568F, -5.6569F, 8.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, -0.7854F, 0.0F, 0.0F));

        head.addOrReplaceChild("side_bevel_r1", CubeListBuilder.create().texOffs(25, 42).addBox(-3.0F, -5.0F, -8.5F, 6.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -7.0F, 0.0F, -3.1416F, 0.7854F, 3.1416F));
        head.addOrReplaceChild("side_bevel_r2", CubeListBuilder.create().texOffs(25, 42).addBox(-3.0F, -5.0F, -8.5F, 6.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -7.0F, 0.0F, -3.1416F, -0.7854F, 3.1416F));
        head.addOrReplaceChild("side_bevel_r3", CubeListBuilder.create().texOffs(25, 42).addBox(-3.0F, -11.0F, -8.5F, 6.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, 0.0F, 0.7854F, 0.0F));
        head.addOrReplaceChild("side_bevel_r4", CubeListBuilder.create().texOffs(25, 42).addBox(-3.0F, -11.0F, -8.5F, 6.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        head.addOrReplaceChild("core_front_r1", CubeListBuilder.create().texOffs(25, 32).addBox(-4.0F, -5.0F, -8.0F, 8.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -7.0F, 0.0F, -3.1416F, 0.0F, 3.1416F));

        head.addOrReplaceChild("corner_slant_r1", CubeListBuilder.create().texOffs(46, 42).addBox(-2.0F, -6.1068F, -5.6569F, 4.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -7.0F, 0.0F, -2.3562F, 0.7854F, -3.1416F));
        head.addOrReplaceChild("corner_slant_r2", CubeListBuilder.create().texOffs(0, 44).addBox(-2.0F, -1.4287F, -9.1924F, 4.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -7.0F, 0.0F, 2.3562F, 0.7854F, 3.1416F));
        head.addOrReplaceChild("corner_slant_r3", CubeListBuilder.create().texOffs(0, 44).addBox(-2.0F, -1.4287F, -9.1924F, 4.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -7.0F, 0.0F, 2.3562F, -0.7854F, -3.1416F));
        head.addOrReplaceChild("corner_slant_r4", CubeListBuilder.create().texOffs(46, 42).addBox(-2.0F, -6.1068F, -5.6569F, 4.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -7.0F, 0.0F, -2.3562F, -0.7854F, 3.1416F));
        head.addOrReplaceChild("corner_slant_r5", CubeListBuilder.create().texOffs(0, 44).addBox(-2.0F, 2.1068F, -5.6569F, 4.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, -0.7854F, 0.7854F, 0.0F));
        head.addOrReplaceChild("corner_slant_r6", CubeListBuilder.create().texOffs(46, 42).addBox(-2.0F, -6.1068F, -5.6569F, 4.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -7.0F, 0.0F, 0.7854F, 0.7854F, 0.0F));
        head.addOrReplaceChild("corner_slant_r7", CubeListBuilder.create().texOffs(46, 42).addBox(-2.0F, -6.1068F, -5.6569F, 4.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -7.0F, 0.0F, 0.7854F, -0.7854F, 0.0F));
        head.addOrReplaceChild("corner_slant_r8", CubeListBuilder.create().texOffs(0, 44).addBox(-2.0F, 2.1068F, -5.6569F, 4.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, -0.7854F, -0.7854F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay) {
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay);
    }
}
