package com.resourceful_refinement.content.radiator;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.resourceful_refinement.ResourcefulRefinementMain;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class RadiatorModel {

    public static final ModelLayerLocation RADIATOR_MODEL_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "radiator_pipe"), "main");

    public static final ResourceLocation INERT_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "textures/block/radiator/radiator_entity_model_inert.png");
    public static final ResourceLocation CHILLED_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "textures/block/radiator/radiator_entity_model_chilled.png");
    public static final ResourceLocation COOLED_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "textures/block/radiator/radiator_entity_model_cooled.png");
    public static final ResourceLocation PASSIVE_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "textures/block/radiator/radiator_entity_model_passive.png");
    public static final ResourceLocation HEATED_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "textures/block/radiator/radiator_entity_model_heated.png");
    public static final ResourceLocation SUPERHEATED_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ResourcefulRefinementMain.MOD_ID, "textures/block/radiator/radiator_entity_model_superheated.png");

    private final ModelPart bone;

    public RadiatorModel(ModelPart root) {
        this.bone = root.getChild("bone");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 18).addBox(-14.0F, -16.0F, 2.0F, 12.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(0, 18).addBox(-14.0F, -3.0F, 2.0F, 12.0F, 3.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-16.0F, -4.0F, 0.0F, 16.0F, 1.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-16.0F, -14.0F, 0.0F, 16.0F, 1.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-16.0F, -12.0F, 0.0F, 16.0F, 1.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-16.0F, -10.0F, 0.0F, 16.0F, 1.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-16.0F, -8.0F, 0.0F, 16.0F, 1.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-16.0F, -6.0F, 0.0F, 16.0F, 1.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 34).addBox(-14.0F, -14.0F, 2.0F, 12.0F, 11.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, 24.0F, -8.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    public void render(PoseStack poseStack, VertexConsumer buffer, int light, int overlay) {
        bone.render(poseStack, buffer, light, overlay);
    }

    public static ResourceLocation GetTextureForHeatEnergy(int blazeHeatEnergy)
    {
        if (blazeHeatEnergy <= -3)
            return CHILLED_TEXTURE;
        else if (blazeHeatEnergy == -2)
            return COOLED_TEXTURE;
        else if (blazeHeatEnergy == -1)
            return INERT_TEXTURE;
        else if (blazeHeatEnergy == 0)
            return PASSIVE_TEXTURE;
        else if (blazeHeatEnergy == 1)
            return HEATED_TEXTURE;
        else if (blazeHeatEnergy >= 2)
            return SUPERHEATED_TEXTURE;

        return INERT_TEXTURE;
    }

}
