package com.resourceful_refinement.content.refinery.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class RefineryTopModel {
    private final ModelPart root;

    public RefineryTopModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition Top_Layer = partdefinition.addOrReplaceChild("Top_Layer", CubeListBuilder.create().texOffs(204, 142).addBox(-22.0F, -7.2167F, 9.0F, 10.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(212, 131).addBox(-19.5F, -5.2067F, 10.5F, 6.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(212, 131).addBox(13.5F, -5.2067F, 10.5F, 6.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(204, 142).addBox(12.0F, -7.2167F, 9.0F, 10.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(233, 165).addBox(-4.0F, -7.2067F, -7.0F, 1.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(110, 214).addBox(-22.0F, -7.2067F, 19.0F, 44.0F, 16.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(110, 214).addBox(-22.0F, -7.2067F, -25.0F, 44.0F, 16.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 15.2067F, 3.0F));

        PartDefinition Outer_Glass_Wall_r1 = Top_Layer.addOrReplaceChild("Outer_Glass_Wall_r1", CubeListBuilder.create().texOffs(110, 214).addBox(-22.0F, -16.0F, 1.0F, 44.0F, 16.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 8.7933F, 20.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition Inner_Glass_Wall_r1 = Top_Layer.addOrReplaceChild("Inner_Glass_Wall_r1", CubeListBuilder.create().texOffs(110, 214).addBox(-22.0F, -16.0F, 1.0F, 44.0F, 16.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 8.7933F, -24.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition Outer_Glass_Wall_r2 = Top_Layer.addOrReplaceChild("Outer_Glass_Wall_r2", CubeListBuilder.create().texOffs(110, 227).addBox(-22.0F, -16.0F, 1.0F, 44.0F, 16.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(23.0F, 8.7933F, -3.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition Inner_Glass_Wall_r2 = Top_Layer.addOrReplaceChild("Inner_Glass_Wall_r2", CubeListBuilder.create().texOffs(110, 227).addBox(-22.0F, -16.0F, 1.0F, 44.0F, 16.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-21.0F, 8.7933F, -3.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition Outer_Glass_Wall_r3 = Top_Layer.addOrReplaceChild("Outer_Glass_Wall_r3", CubeListBuilder.create().texOffs(110, 240).addBox(-22.0F, -16.0F, 1.0F, 44.0F, 16.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-23.0F, 8.7933F, -3.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition Inner_Glass_Wall_r3 = Top_Layer.addOrReplaceChild("Inner_Glass_Wall_r3", CubeListBuilder.create().texOffs(110, 240).addBox(-22.0F, -16.0F, 1.0F, 44.0F, 16.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(21.0F, 8.7933F, -3.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition Shaft_Border_3_r1 = Top_Layer.addOrReplaceChild("Shaft_Border_3_r1", CubeListBuilder.create().texOffs(233, 165).addBox(0.0F, -2.0F, -4.0F, 1.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -5.2067F, 1.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition Shaft_Border_2_r1 = Top_Layer.addOrReplaceChild("Shaft_Border_2_r1", CubeListBuilder.create().texOffs(233, 165).addBox(0.0F, -2.0F, -4.0F, 1.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, -5.2067F, -3.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition Shaft_Border_1_r1 = Top_Layer.addOrReplaceChild("Shaft_Border_1_r1", CubeListBuilder.create().texOffs(233, 165).addBox(0.0F, -2.0F, -4.0F, 1.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -5.2067F, -7.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition Glass_Bottom_Underlay_r1 = Top_Layer.addOrReplaceChild("Glass_Buttom_Underlay_r1", CubeListBuilder.create().texOffs(6, 125).addBox(-20.0F, -2.0F, -20.0F, 40.0F, 0.0F, 40.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.2067F, -3.0F, 0.0F, 0.0F, 0.0F));

        PartDefinition Glass_Top_Underlay_r1 = Top_Layer.addOrReplaceChild("Glass_Top_Underlay_r1", CubeListBuilder.create().texOffs(6, 125).addBox(-20.0F, -2.0F, -20.0F, 40.0F, 0.0F, 40.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -8.2067F, -3.0F, 0.0F, 0.0F, -3.1416F));

        PartDefinition Frames = Top_Layer.addOrReplaceChild("Frames", CubeListBuilder.create().texOffs(58, 183).addBox(20.0F, -44.0F, -23.0F, 3.0F, 12.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(71, 183).addBox(20.0F, -48.0F, -24.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(89, 184).addBox(-20.0F, -48.0F, -23.0F, 40.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 40.7933F, -3.0F));

        PartDefinition Frame_Edge_4_r1 = Frames.addOrReplaceChild("Frame_Edge_4_r1", CubeListBuilder.create().texOffs(89, 184).addBox(-20.0F, -2.0F, -1.5F, 40.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(21.5F, -46.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition Frame_Edge_3_r1 = Frames.addOrReplaceChild("Frame_Edge_3_r1", CubeListBuilder.create().texOffs(89, 184).addBox(-20.0F, -2.0F, -1.5F, 40.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -46.0F, 21.5F, 0.0F, 3.1416F, 0.0F));

        PartDefinition Frame_Edge_2_r1 = Frames.addOrReplaceChild("Frame_Edge_2_r1", CubeListBuilder.create().texOffs(89, 184).addBox(-20.0F, -2.0F, -1.5F, 40.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-21.5F, -46.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition Frame_Node_4_r1 = Frames.addOrReplaceChild("Frame_Node_4_r1", CubeListBuilder.create().texOffs(71, 183).addBox(-3.0F, -2.0F, -1.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(58, 183).addBox(-2.0F, 2.0F, -1.0F, 3.0F, 12.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(21.0F, -46.0F, 21.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition Frame_Node_3_r1 = Frames.addOrReplaceChild("Frame_Node_3_r1", CubeListBuilder.create().texOffs(71, 183).addBox(-3.0F, -2.0F, -1.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-23.0F, -46.0F, 23.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition Frame_Node_2_r1 = Frames.addOrReplaceChild("Frame_Node_2_r1", CubeListBuilder.create().texOffs(71, 183).addBox(-3.0F, -2.0F, -1.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(58, 183).addBox(-2.0F, 2.0F, -1.0F, 3.0F, 12.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-21.0F, -46.0F, -21.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition Frame_Pillar_4_r1 = Frames.addOrReplaceChild("Frame_Pillar_4_r1", CubeListBuilder.create().texOffs(58, 183).addBox(-2.0F, -12.0F, -1.0F, 3.0F, 12.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-22.0F, -32.0F, 22.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition Item_Port = Top_Layer.addOrReplaceChild("Item_Port", CubeListBuilder.create().texOffs(177, 137).addBox(-1.01F, -8.01F, -6.0F, 1.0F, 16.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(210, 181).addBox(0.0F, -6.0F, -5.0F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(210, 181).addBox(1.0F, -5.25F, -5.0F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(210, 181).addBox(2.0F, -4.5F, -5.0F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(210, 181).addBox(3.0F, -3.75F, -5.0F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(210, 181).addBox(7.0F, -0.75F, -5.0F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(210, 181).addBox(6.0F, -1.5F, -5.0F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(210, 181).addBox(5.0F, -2.25F, -5.0F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(210, 181).addBox(4.0F, -3.0F, -5.0F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(210, 181).addBox(5.0F, -2.25F, 4.0F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(210, 181).addBox(6.0F, -1.5F, 4.0F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(210, 181).addBox(7.0F, -0.75F, 4.0F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(210, 181).addBox(3.0F, -3.75F, 4.0F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(210, 181).addBox(2.0F, -4.5F, 4.0F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(210, 181).addBox(1.0F, -5.25F, 4.0F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(210, 181).addBox(0.0F, -6.0F, 4.0F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(210, 181).addBox(4.0F, -3.0F, 4.0F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-23.0F, 0.7933F, -19.0F));

        PartDefinition Chute_Tray_r1 = Item_Port.addOrReplaceChild("Chute_Tray_r1", CubeListBuilder.create().texOffs(224, 184).addBox(-1.0F, -2.75F, -0.1F, 1.0F, 10.0F, 1.2F, new CubeDeformation(0.0F))
                .texOffs(224, 184).addBox(-1.0F, -2.75F, -9.1F, 1.0F, 10.0F, 1.2F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.5F, -1.75F, 4.0F, 0.0F, 0.0F, 2.2253F));

        PartDefinition Chute_Tray_r2 = Item_Port.addOrReplaceChild("Chute_Tray_r2", CubeListBuilder.create().texOffs(215, 175).addBox(-1.0F, -2.75F, -0.1F, 1.0F, 10.0F, 10.2F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.5F, 11.25F, -5.0F, 0.0F, 0.0F, 2.2253F));

        PartDefinition Item_Port_2 = Top_Layer.addOrReplaceChild("Item_Port_2", CubeListBuilder.create().texOffs(177, 137).mirror().addBox(0.01F, -8.01F, -6.0F, 1.0F, 16.0F, 12.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(210, 181).mirror().addBox(-1.0F, -6.0F, -5.0F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(210, 181).mirror().addBox(-2.0F, -5.25F, -5.0F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(210, 181).mirror().addBox(-3.0F, -4.5F, -5.0F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(210, 181).mirror().addBox(-4.0F, -3.75F, -5.0F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(210, 181).mirror().addBox(-8.0F, -0.75F, -5.0F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(210, 181).mirror().addBox(-7.0F, -1.5F, -5.0F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(210, 181).mirror().addBox(-6.0F, -2.25F, -5.0F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(210, 181).mirror().addBox(-5.0F, -3.0F, -5.0F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(210, 181).mirror().addBox(-6.0F, -2.25F, 4.0F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(210, 181).mirror().addBox(-7.0F, -1.5F, 4.0F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(210, 181).mirror().addBox(-8.0F, -0.75F, 4.0F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(210, 181).mirror().addBox(-4.0F, -3.75F, 4.0F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(210, 181).mirror().addBox(-3.0F, -4.5F, 4.0F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(210, 181).mirror().addBox(-2.0F, -5.25F, 4.0F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(210, 181).mirror().addBox(-1.0F, -6.0F, 4.0F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(210, 181).mirror().addBox(-5.0F, -3.0F, 4.0F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(23.0F, 0.7933F, -19.0F));

        PartDefinition Chute_Tray_r3 = Item_Port_2.addOrReplaceChild("Chute_Tray_r3", CubeListBuilder.create().texOffs(224, 184).mirror().addBox(0.0F, -2.75F, -0.1F, 1.0F, 10.0F, 1.2F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(224, 184).mirror().addBox(0.0F, -2.75F, -9.1F, 1.0F, 10.0F, 1.2F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-5.5F, -1.75F, 4.0F, 0.0F, 0.0F, -2.2253F));

        PartDefinition Chute_Tray_r4 = Item_Port_2.addOrReplaceChild("Chute_Tray_r4", CubeListBuilder.create().texOffs(215, 175).mirror().addBox(0.0F, -2.75F, -0.1F, 1.0F, 10.0F, 10.2F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-5.5F, 11.25F, -5.0F, 0.0F, 0.0F, -2.2253F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    public void render(PoseStack poseStack, VertexConsumer buffer, int light, int overlay) {
        root.render(poseStack, buffer, light, overlay);
    }
}