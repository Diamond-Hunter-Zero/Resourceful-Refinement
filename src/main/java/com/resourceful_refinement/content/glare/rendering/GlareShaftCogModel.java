package com.resourceful_refinement.content.glare.rendering;

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

public class GlareShaftCogModel extends HierarchicalModel<Entity> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "glare_shaft_cog"),
            "main");

    private final ModelPart root;
    private final ModelPart Hollow_Cog;
    private final ModelPart cogwheel_teeth;
    private final ModelPart outer_rim;

    public GlareShaftCogModel(ModelPart root) {
        this.root = root;

        this.Hollow_Cog = root.getChild("Hollow_Cog");
        this.cogwheel_teeth = this.Hollow_Cog.getChild("cogwheel_teeth");
        this.outer_rim = this.Hollow_Cog.getChild("outer_rim");
    }

    @Override
    public ModelPart root() {
        return root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition Hollow_Cog = partdefinition.addOrReplaceChild("Hollow_Cog", CubeListBuilder.create().texOffs(0, 5).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(48, 8).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 16.0F, -7.0F, 1.5708F, 0.0F, 3.1416F));

        PartDefinition shaft_cap_r1 = Hollow_Cog.addOrReplaceChild("shaft_cap_r1", CubeListBuilder.create().texOffs(0, 5).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition cogwheel_teeth = Hollow_Cog.addOrReplaceChild("cogwheel_teeth", CubeListBuilder.create().texOffs(72, 39).addBox(-1.5F, 6.0F, -2.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition tooth_r1 = cogwheel_teeth.addOrReplaceChild("tooth_r1", CubeListBuilder.create().texOffs(72, 39).addBox(-1.5F, -8.0F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -1.0F, 0.0F, 0.0F, -0.7854F));

        PartDefinition tooth_r2 = cogwheel_teeth.addOrReplaceChild("tooth_r2", CubeListBuilder.create().texOffs(72, 39).addBox(-1.5F, 6.0F, -5.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.0F, 0.0F, 3.1416F));

        PartDefinition tooth_r3 = cogwheel_teeth.addOrReplaceChild("tooth_r3", CubeListBuilder.create().texOffs(72, 39).addBox(-1.5F, 6.0F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -1.0F, 0.0F, 0.0F, -1.5708F));

        PartDefinition tooth_r4 = cogwheel_teeth.addOrReplaceChild("tooth_r4", CubeListBuilder.create().texOffs(72, 39).addBox(-1.5F, 6.0F, -5.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.0F, 0.0F, -2.3562F));

        PartDefinition tooth_r5 = cogwheel_teeth.addOrReplaceChild("tooth_r5", CubeListBuilder.create().texOffs(72, 39).addBox(-1.5F, -8.0F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -1.0F, 0.0F, 0.0F, 2.3562F));

        PartDefinition tooth_r6 = cogwheel_teeth.addOrReplaceChild("tooth_r6", CubeListBuilder.create().texOffs(72, 39).addBox(-1.5F, 6.0F, -5.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition strut_r1 = cogwheel_teeth.addOrReplaceChild("strut_r1", CubeListBuilder.create().texOffs(87, 27).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(72, 39).addBox(-1.5F, 6.0F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -1.0F, 0.0F, 0.0F, 1.5708F));

        PartDefinition outer_rim = Hollow_Cog.addOrReplaceChild("outer_rim", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition rim_r1 = outer_rim.addOrReplaceChild("rim_r1", CubeListBuilder.create().texOffs(96, 37).addBox(-2.5F, 4.6F, -4.95F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, 2.899F, 0.0F, 0.0F, 2.7489F));

        PartDefinition rim_r2 = outer_rim.addOrReplaceChild("rim_r2", CubeListBuilder.create().texOffs(96, 37).addBox(-2.5F, 4.6F, -4.85F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, 2.8F, 0.0F, 0.0F, -2.7489F));

        PartDefinition rim_r3 = outer_rim.addOrReplaceChild("rim_r3", CubeListBuilder.create().texOffs(96, 37).addBox(-2.5F, 4.6F, -4.95F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, 2.899F, 0.0F, 0.0F, -1.9635F));

        PartDefinition rim_r4 = outer_rim.addOrReplaceChild("rim_r4", CubeListBuilder.create().texOffs(96, 37).addBox(-2.5F, 4.6F, -4.95F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, 2.899F, 0.0F, 0.0F, -1.1781F));

        PartDefinition rim_r5 = outer_rim.addOrReplaceChild("rim_r5", CubeListBuilder.create().texOffs(96, 37).addBox(-2.5F, 4.6F, -4.95F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, 2.899F, 0.0F, 0.0F, -0.3927F));

        PartDefinition rim_r6 = outer_rim.addOrReplaceChild("rim_r6", CubeListBuilder.create().texOffs(96, 37).addBox(-2.5F, 4.6F, -4.95F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, 2.899F, 0.0F, 0.0F, 1.9635F));

        PartDefinition rim_r7 = outer_rim.addOrReplaceChild("rim_r7", CubeListBuilder.create().texOffs(96, 37).addBox(-2.5F, 4.6F, -4.95F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, 2.899F, 0.0F, 0.0F, 1.1781F));

        PartDefinition rim_r8 = outer_rim.addOrReplaceChild("rim_r8", CubeListBuilder.create().texOffs(96, 37).addBox(-2.5F, 4.6F, -4.85F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, 2.8F, 0.0F, 0.0F, 0.3927F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

    public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay) {
        Hollow_Cog.render(poseStack, vertexConsumer, packedLight, packedOverlay);
    }

}
