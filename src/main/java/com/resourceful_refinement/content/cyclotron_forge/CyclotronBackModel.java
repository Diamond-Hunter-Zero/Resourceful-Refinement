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

public class CyclotronBackModel extends HierarchicalModel<Entity> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "cyclotron_back"),
            "main");

    private final ModelPart root;
    private final ModelPart back_cap;

    public CyclotronBackModel(ModelPart root) {
            this.root = root;
            this.back_cap = root.getChild("back_cap");
        }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition back_cap = partdefinition.addOrReplaceChild("back_cap", CubeListBuilder.create().texOffs(0, 0).addBox(-23.0F, -48.0F, -24.0F, 46.0F, 48.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(150, 120).addBox(-6.0F, -32.0F, -10.0F, 12.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(150, 124).addBox(-6.0F, -18.0F, -10.0F, 12.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(150, 128).addBox(-10.0F, -32.0F, -10.0F, 4.0F, 16.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(150, 146).addBox(6.0F, -32.0F, -10.0F, 4.0F, 16.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(121, 120).addBox(10.0F, -46.0F, -10.0F, 12.0F, 30.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(121, 120).addBox(-22.0F, -46.0F, -10.0F, 12.0F, 30.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(161, 128).addBox(11.0F, -13.0F, -10.0F, 10.0F, 10.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(161, 128).addBox(-21.0F, -13.0F, -10.0F, 10.0F, 10.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(172, 127).mirror().addBox(23.0F, -32.0F, -24.0F, 1.0F, 16.0F, 14.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(172, 127).addBox(-24.0F, -32.0F, -24.0F, 1.0F, 16.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 16.0F));

        PartDefinition bridging_support_strut_r1 = back_cap.addOrReplaceChild("bridging_support_strut_r1", CubeListBuilder.create().texOffs(68, 126).addBox(-12.0F, -1.0F, -0.5F, 24.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(68, 126).addBox(-12.0F, -33.0F, -0.5F, 24.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -8.0F, -9.5F, 0.0F, 3.1416F, 0.0F));
        return LayerDefinition.create(meshdefinition, 256, 256);

    }

    public void render(PoseStack poseStack, VertexConsumer buffer, int light, int overlay) {
        back_cap.render(poseStack, buffer, light, overlay);
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
