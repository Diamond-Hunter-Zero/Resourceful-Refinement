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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class CyclotronCoilModel extends HierarchicalModel<Entity> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "cyclotron_coil"),
            "main");

    private final ModelPart root;
    private final ModelPart coil_segment;
    private final ModelPart coil_interior;

    public CyclotronCoilModel(ModelPart root) {
            this.root = root;
            this.coil_segment = root.getChild("coil_segment");
            this.coil_interior = root.getChild("coil_interior");
        }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition coil_segment = partdefinition.addOrReplaceChild("coil_segment", CubeListBuilder.create().texOffs(0, 0).addBox(-22.0F, -46.0F, -8.0F, 44.0F, 44.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition coil_interior = partdefinition.addOrReplaceChild("coil_interior", CubeListBuilder.create().texOffs(0, 60).addBox(-21.0F, -37.0F, -8.0F, 42.0F, 42.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 16.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);

    }

    public void render(PoseStack poseStack, VertexConsumer buffer, int light, int overlay) {
        coil_segment.render(poseStack, buffer, light, overlay);
        coil_interior.render(poseStack, buffer, light, overlay);
    }

    public void renderWithInteriorTint(PoseStack poseStack, VertexConsumer buffer, int light, int overlay,
            float hue, float saturation, float value) {
        renderWithInteriorTint(poseStack, buffer, buffer, light, overlay, hue, saturation, value);
    }

    public void renderWithInteriorTint(PoseStack poseStack, VertexConsumer segmentBuffer, VertexConsumer interiorBuffer,
            int light, int overlay, float hue, float saturation, float value) {
        coil_segment.render(poseStack, segmentBuffer, light, overlay);
        coil_interior.render(poseStack, interiorBuffer, light, overlay, interiorTintColor(hue, saturation, value));
    }

    public static int interiorTintColor(float hue, float saturation, float value) {
        int rgb = Mth.hsvToRgb(wrapHue(hue), Mth.clamp(saturation, 0.0F, 1.0F), Mth.clamp(value, 0.0F, 1.0F));
        return 0xFF000000 | rgb;
    }

    private static float wrapHue(float hue) {
        float wrapped = hue % 1.0F;
        return wrapped < 0.0F ? wrapped + 1.0F : wrapped;
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
