package com.resourceful_refinement.content.distillery;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.resourceful_refinement.ResourcefulRefinementMain;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class DistilleryModel {

    public static final ModelLayerLocation DISTILLERY_MODEL_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "distillery"), "main");

    public static final ResourceLocation SINGLE_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "textures/block/distillery/distillery_tank_entity_single.png");
    public static final ResourceLocation BASE_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "textures/block/distillery/distillery_tank_entity_base.png");
    public static final ResourceLocation MIDDLE_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "textures/block/distillery/distillery_tank_entity_middle.png");
    public static final ResourceLocation TOP_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "textures/block/distillery/distillery_tank_entity_top.png");

    private final ModelPart bone;

    public DistilleryModel(ModelPart root) {
        this.bone = root.getChild("bone");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 0).addBox(-16.0F, -16.0F, 0.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, 24.0F, -8.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    public void render(PoseStack poseStack, VertexConsumer buffer, int light, int overlay) {
        bone.render(poseStack, buffer, light, overlay);
    }

    public static ResourceLocation GetTextureForModelType(int modelType)
    {
        if (modelType == 1)
            return BASE_TEXTURE;
        else if (modelType == 2)
            return MIDDLE_TEXTURE;
        else if (modelType == 3)
            return TOP_TEXTURE;

        return SINGLE_TEXTURE;
    }

}
