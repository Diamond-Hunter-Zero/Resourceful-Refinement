package com.resourceful_refinement.content.cyclotron_forge;

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

public class CyclotronFrontModel extends HierarchicalModel<Entity> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "cyclotron_front"),
            "main");

    private final ModelPart root;
    private final ModelPart front_cap;

    public CyclotronFrontModel(ModelPart root) {
            this.root = root;
            this.front_cap = root.getChild("front_cap");
        }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition front_cap = partdefinition.addOrReplaceChild("front_cap", CubeListBuilder.create().texOffs(37, 126).addBox(-24.0F, -8.0F, 9.0F, 1.0F, 16.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(37, 126).mirror().addBox(23.0F, -8.0F, 9.0F, 1.0F, 16.0F, 14.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 63).addBox(-23.0F, -24.0F, 9.0F, 46.0F, 48.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(68, 130).addBox(-22.0F, -8.0F, 7.0F, 12.0F, 16.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(68, 149).addBox(10.0F, -8.0F, 7.0F, 12.0F, 16.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(97, 130).addBox(-20.0F, -22.0F, 7.0F, 8.0F, 14.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(97, 130).addBox(12.0F, -22.0F, 7.0F, 8.0F, 14.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 126).addBox(-8.0F, -15.0F, 7.0F, 16.0F, 30.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(68, 126).addBox(-12.0F, -17.0F, 8.0F, 24.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(68, 126).addBox(-12.0F, 15.0F, 8.0F, 24.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -15.0F));

        PartDefinition bottom_strut_r1 = front_cap.addOrReplaceChild("bottom_strut_r1", CubeListBuilder.create().texOffs(97, 130).addBox(12.0F, -7.0F, -1.0F, 8.0F, 14.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(97, 130).addBox(-20.0F, -7.0F, -1.0F, 8.0F, 14.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 15.0F, 8.0F, 0.0F, 0.0F, -3.1416F));

        return LayerDefinition.create(meshdefinition, 256, 256);

    }

    public void render(PoseStack poseStack, VertexConsumer buffer, int light, int overlay) {
        front_cap.render(poseStack, buffer, light, overlay);
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // No animations
    }

    @Override
    public ModelPart root() {
        return root;
    }
}
