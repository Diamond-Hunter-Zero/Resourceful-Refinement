package com.resourceful_refinement.content.refill_station;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Placeholder casing mesh for the Fluid Refill Station BER.
 * Replace with the final Blockbench export when art is ready.
 */
public class FluidRefillStationCasingModel {

    private final ModelPart root;

    public FluidRefillStationCasingModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        //PartDefinition Fluid_Fill = partdefinition.addOrReplaceChild("Fluid_Fill", CubeListBuilder.create().texOffs(0, 40).addBox(-6.0F, -9.0F, -8.0F, 11.0F, 9.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 20.0F, 0.5F));

        PartDefinition Side = partdefinition.addOrReplaceChild("Side", CubeListBuilder.create().texOffs(0, 63).addBox(-8.0F, -10.0F, 3.0F, 2.0F, 10.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(15, 63).addBox(-8.0F, -10.0F, -8.0F, 2.0F, 10.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(30, 63).addBox(-7.0F, -10.0F, -3.0F, 0.0F, 10.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 20.0F, 0.0F));

        PartDefinition Side2 = partdefinition.addOrReplaceChild("Side2", CubeListBuilder.create().texOffs(0, 63).mirror().addBox(6.0F, -10.0F, 3.0F, 2.0F, 10.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(15, 63).mirror().addBox(6.0F, -10.0F, -8.0F, 2.0F, 10.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(30, 63).mirror().addBox(7.0F, -10.0F, -3.0F, 0.0F, 10.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 20.0F, 0.0F));

        PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -4.0F, -8.0F, 16.0F, 4.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 21).addBox(-8.0F, -16.0F, -8.0F, 16.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(49, 40).addBox(-6.0F, -14.0F, 6.0F, 12.0F, 10.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(49, 53).addBox(-6.0F, -14.0F, -8.0F, 12.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));


        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    public void render(PoseStack poseStack, VertexConsumer buffer, int light, int overlay) {
        root.render(poseStack, buffer, light, overlay);
    }
}
