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
 * A pair of octagonal copper rings drawn around one exposed shaft block beneath a Tesla Coil. Modelled around a
 * vertical shaft; the renderer orients it to the coil's FACING.
 */
public class TeslaCoilRingModel extends HierarchicalModel<Entity> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "tesla_coil_ring"), "main");

    private final ModelPart root;
    private final ModelPart lowerRing;
    private final ModelPart upperRing;

    public TeslaCoilRingModel(ModelPart root) {
        this.root = root;
        this.lowerRing = root.getChild("lower_ring");
        this.upperRing = root.getChild("upper_ring");
    }

    @Override
    public ModelPart root() {
        return root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        addRing(partdefinition, "lower_ring", 24.0F);
        addRing(partdefinition, "upper_ring", 16.0F);

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    /** One octagonal ring: four straight segments (A) on the cardinal sides and four (B) on the diagonals. */
    private static void addRing(PartDefinition parent, String name, float y) {
        PartDefinition ring = parent.addOrReplaceChild(name, segmentA(), PartPose.offset(0.0F, y, 0.0F));

        ring.addOrReplaceChild("ring_segment_a_r1", segmentA(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));
        ring.addOrReplaceChild("ring_segment_a_r2", segmentA(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -3.1416F, 0.0F, 3.1416F));
        ring.addOrReplaceChild("ring_segment_a_r3", segmentA(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        ring.addOrReplaceChild("ring_segment_b_r1", segmentB(), PartPose.offsetAndRotation(0.0F, -0.005F, 0.0F, 0.0F, 0.7854F, 0.0F));
        ring.addOrReplaceChild("ring_segment_b_r2", segmentB(), PartPose.offsetAndRotation(0.0F, -0.005F, 0.0F, -3.1416F, 0.7854F, 3.1416F));
        ring.addOrReplaceChild("ring_segment_b_r3", segmentB(), PartPose.offsetAndRotation(0.0F, -0.005F, 0.0F, -3.1416F, -0.7854F, 3.1416F));
        ring.addOrReplaceChild("ring_segment_b_r4", segmentB(), PartPose.offsetAndRotation(0.0F, -0.005F, 0.0F, 0.0F, -0.7854F, 0.0F));
    }

    private static CubeListBuilder segmentA() {
        return CubeListBuilder.create().texOffs(0, 9).addBox(-3.0F, -6.0F, -7.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F));
    }

    private static CubeListBuilder segmentB() {
        return CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -5.99F, -7.1F, 6.0F, 3.99F, 4.0F, new CubeDeformation(0.0F));
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay) {
        lowerRing.render(poseStack, vertexConsumer, packedLight, packedOverlay);
        upperRing.render(poseStack, vertexConsumer, packedLight, packedOverlay);
    }
}
