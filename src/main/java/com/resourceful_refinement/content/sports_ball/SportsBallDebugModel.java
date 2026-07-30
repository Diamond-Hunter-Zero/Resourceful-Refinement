package com.resourceful_refinement.content.sports_ball;

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
 * Placeholder ball geometry — a single 11px cube centred on the part origin so
 * {@link SportsBallRenderer} can spin it about its middle without extra offsets.
 */
public class SportsBallDebugModel {

    /** Cube edge length in model pixels. */
    public static final float SIZE = 11.0F;

    private final ModelPart body;

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        float half = SIZE / 2.0F;
        root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-half, -half, -half, SIZE, SIZE, SIZE, new CubeDeformation(0.0F)),
                PartPose.ZERO
        );

        return LayerDefinition.create(mesh, 64, 64);
    }

    public SportsBallDebugModel(ModelPart root) {
        this.body = root.getChild("body");
    }

    public void render(PoseStack poseStack, VertexConsumer buffer, int light, int overlay) {
        body.render(poseStack, buffer, light, overlay);
    }
}
