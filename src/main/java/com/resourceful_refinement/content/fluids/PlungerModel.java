package com.resourceful_refinement.content.fluids;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/** Blockbench plunger geometry baked for {@link PlungerItemRenderer}. */
public class PlungerModel {

    private final ModelPart root;

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 0).addBox(-11.0F, -3.0F, 5.0F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(8, 12).addBox(-5.0F, -2.0F, 5.0F, 1.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(16, 9).addBox(-12.0F, -2.0F, 5.0F, 1.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 7).addBox(-11.0F, -2.0F, 4.0F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 11).mirror().addBox(-11.0F, -2.0F, 11.0F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(24, 0).mirror().addBox(-9.0F, -16.0F, 7.0F, 2.0F, 13.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(8.0F, 24.0F, -8.0F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    public PlungerModel(ModelPart root) {
        this.root = root.getChild("bone");
    }

    public void render(PoseStack poseStack, VertexConsumer buffer, int light, int overlay) {
        root.render(poseStack, buffer, light, overlay);
    }
}
