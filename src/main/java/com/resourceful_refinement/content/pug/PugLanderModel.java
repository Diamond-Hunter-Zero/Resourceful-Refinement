package com.resourceful_refinement.content.pug;

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

public class PugLanderModel extends HierarchicalModel<Entity> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "pug_lander"),
            "main");

    private final ModelPart root;

    private final ModelPart thruster;
    private final ModelPart side_quarter;
    private final ModelPart side_panel;
    private final ModelPart leg;
    private final ModelPart strut_segment;
    private final ModelPart side_quarter2;
    private final ModelPart side_panel2;
    private final ModelPart leg2;
    private final ModelPart strut_segment2;
    private final ModelPart side_quarter3;
    private final ModelPart side_panel3;
    private final ModelPart leg3;
    private final ModelPart strut_segment3;
    private final ModelPart side_quarter4;
    private final ModelPart side_panel4;
    private final ModelPart leg4;
    private final ModelPart strut_segment4;
    private final ModelPart tank_assembly;
    private final ModelPart tank_assembly2;
    private final ModelPart corner_panel;
    private final ModelPart corner_panel2;
    private final ModelPart bb_main;

    public PugLanderModel(ModelPart root) {
        this.root = root;
        this.thruster = root.getChild("thruster");
        this.side_quarter = root.getChild("side_quarter");
        this.side_panel = this.side_quarter.getChild("side_panel");
        this.leg = this.side_quarter.getChild("leg");
        this.strut_segment = this.leg.getChild("strut_segment");
        this.side_quarter2 = root.getChild("side_quarter2");
        this.side_panel2 = this.side_quarter2.getChild("side_panel2");
        this.leg2 = this.side_quarter2.getChild("leg2");
        this.strut_segment2 = this.leg2.getChild("strut_segment2");
        this.side_quarter3 = root.getChild("side_quarter3");
        this.side_panel3 = this.side_quarter3.getChild("side_panel3");
        this.leg3 = this.side_quarter3.getChild("leg3");
        this.strut_segment3 = this.leg3.getChild("strut_segment3");
        this.side_quarter4 = root.getChild("side_quarter4");
        this.side_panel4 = this.side_quarter4.getChild("side_panel4");
        this.leg4 = this.side_quarter4.getChild("leg4");
        this.strut_segment4 = this.leg4.getChild("strut_segment4");
        this.tank_assembly = root.getChild("tank_assembly");
        this.tank_assembly2 = root.getChild("tank_assembly2");
        this.corner_panel = root.getChild("corner_panel");
        this.corner_panel2 = root.getChild("corner_panel2");
        this.bb_main = root.getChild("bb_main");

    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition thruster = partdefinition.addOrReplaceChild("thruster", CubeListBuilder.create().texOffs(92, 59).addBox(-6.0F, 0.1F, -4.0F, 2.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(90, 73).addBox(-6.0F, 0.1F, 4.0F, 12.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(90, 78).addBox(-6.0F, 0.1F, -6.0F, 12.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(24, 90).addBox(4.0F, 0.1F, -4.0F, 2.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(80, 0).addBox(-5.0F, -3.9F, -5.0F, 10.0F, 4.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 13.9F, 0.0F));

        PartDefinition side_quarter = partdefinition.addOrReplaceChild("side_quarter", CubeListBuilder.create(), PartPose.offset(0.0F, 8.0667F, 0.0F));

        PartDefinition side_panel = side_quarter.addOrReplaceChild("side_panel", CubeListBuilder.create().texOffs(120, 0).addBox(-8.0F, -12.0F, -16.0F, 16.0F, 12.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.9333F, 0.0F));

        PartDefinition side_panel_top_r1 = side_panel.addOrReplaceChild("side_panel_top_r1", CubeListBuilder.create().texOffs(120, 18).addBox(-7.875F, -3.0F, -3.0F, 15.95F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1F, -12.0F, -11.8F, -0.7854F, 0.0F, 0.0F));

        PartDefinition side_panel_bottom_r1 = side_panel.addOrReplaceChild("side_panel_bottom_r1", CubeListBuilder.create().texOffs(164, 13).addBox(-7.975F, -4.0F, -3.0F, 15.95F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -11.8F, -0.7854F, 0.0F, 0.0F));

        PartDefinition leg = side_quarter.addOrReplaceChild("leg", CubeListBuilder.create().texOffs(0, 103).addBox(-2.0F, 8.0F, -7.5F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(107, 49).addBox(-1.0F, -4.0F, 3.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.0F, 6.9333F, -10.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition inner_leg_mount_r1 = leg.addOrReplaceChild("inner_leg_mount_r1", CubeListBuilder.create().texOffs(94, 94).addBox(-1.5F, -1.5F, -3.0F, 3.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5F, 3.0F, 3.1416F, 0.0F, 0.0F));

        PartDefinition strut_segment = leg.addOrReplaceChild("strut_segment", CubeListBuilder.create().texOffs(84, 103).addBox(-1.0F, -1.7071F, -10.2929F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(68, 100).addBox(-2.0F, -2.7071F, -1.2929F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(24, 101).addBox(-2.0F, -2.7071F, -5.2929F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.0472F, 0.0F, 0.0F));

        PartDefinition leg_strut_r1 = strut_segment.addOrReplaceChild("leg_strut_r1", CubeListBuilder.create().texOffs(68, 87).addBox(-0.5F, -0.5F, -6.25F, 1.0F, 1.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.7929F, -3.2929F, -0.3491F, 0.0F, 0.0F));

        PartDefinition side_quarter2 = partdefinition.addOrReplaceChild("side_quarter2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 8.0667F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition side_panel2 = side_quarter2.addOrReplaceChild("side_panel2", CubeListBuilder.create().texOffs(120, 30).addBox(-8.0F, -12.0F, -16.0F, 16.0F, 12.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.9333F, 0.0F));

        PartDefinition side_panel_top_r2 = side_panel2.addOrReplaceChild("side_panel_top_r2", CubeListBuilder.create().texOffs(120, 48).addBox(-7.875F, -3.0F, -3.0F, 15.95F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1F, -12.0F, -11.8F, -0.7854F, 0.0F, 0.0F));

        PartDefinition side_panel_bottom_r2 = side_panel2.addOrReplaceChild("side_panel_bottom_r2", CubeListBuilder.create().texOffs(164, 43).addBox(-7.975F, -4.0F, -3.0F, 15.95F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -11.8F, -0.7854F, 0.0F, 0.0F));

        PartDefinition leg2 = side_quarter2.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(0, 103).addBox(-2.0F, 8.0F, -7.5F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(107, 49).addBox(-1.0F, -4.0F, 3.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.0F, 6.9333F, -10.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition inner_leg_mount_r2 = leg2.addOrReplaceChild("inner_leg_mount_r2", CubeListBuilder.create().texOffs(94, 94).addBox(-1.5F, -1.5F, -3.0F, 3.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5F, 3.0F, 3.1416F, 0.0F, 0.0F));

        PartDefinition strut_segment2 = leg2.addOrReplaceChild("strut_segment2", CubeListBuilder.create().texOffs(84, 103).addBox(-1.0F, -1.7071F, -10.2929F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(68, 100).addBox(-2.0F, -2.7071F, -1.2929F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(24, 101).addBox(-2.0F, -2.7071F, -5.2929F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.0472F, 0.0F, 0.0F));

        PartDefinition leg_strut_r2 = strut_segment2.addOrReplaceChild("leg_strut_r2", CubeListBuilder.create().texOffs(68, 87).addBox(-0.5F, -0.5F, -6.25F, 1.0F, 1.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.7929F, -3.2929F, -0.3491F, 0.0F, 0.0F));

        PartDefinition side_quarter3 = partdefinition.addOrReplaceChild("side_quarter3", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 8.0667F, 0.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition side_panel3 = side_quarter3.addOrReplaceChild("side_panel3", CubeListBuilder.create().texOffs(120, 60).addBox(-8.0F, -12.0F, -16.0F, 16.0F, 12.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.9333F, 0.0F));

        PartDefinition side_panel_top_r3 = side_panel3.addOrReplaceChild("side_panel_top_r3", CubeListBuilder.create().texOffs(120, 78).addBox(-7.875F, -3.0F, -3.0F, 15.95F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1F, -12.0F, -11.8F, -0.7854F, 0.0F, 0.0F));

        PartDefinition side_panel_bottom_r3 = side_panel3.addOrReplaceChild("side_panel_bottom_r3", CubeListBuilder.create().texOffs(164, 73).addBox(-7.975F, -4.0F, -3.0F, 15.95F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -11.8F, -0.7854F, 0.0F, 0.0F));

        PartDefinition leg3 = side_quarter3.addOrReplaceChild("leg3", CubeListBuilder.create().texOffs(0, 103).addBox(-2.0F, 8.0F, -7.5F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(107, 49).addBox(-1.0F, -4.0F, 3.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.0F, 6.9333F, -10.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition inner_leg_mount_r3 = leg3.addOrReplaceChild("inner_leg_mount_r3", CubeListBuilder.create().texOffs(94, 94).addBox(-1.5F, -1.5F, -3.0F, 3.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5F, 3.0F, 3.1416F, 0.0F, 0.0F));

        PartDefinition strut_segment3 = leg3.addOrReplaceChild("strut_segment3", CubeListBuilder.create().texOffs(84, 103).addBox(-1.0F, -1.7071F, -10.2929F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(68, 100).addBox(-2.0F, -2.7071F, -1.2929F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(24, 101).addBox(-2.0F, -2.7071F, -5.2929F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.0472F, 0.0F, 0.0F));

        PartDefinition leg_strut_r3 = strut_segment3.addOrReplaceChild("leg_strut_r3", CubeListBuilder.create().texOffs(68, 87).addBox(-0.5F, -0.5F, -6.25F, 1.0F, 1.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.7929F, -3.2929F, -0.3491F, 0.0F, 0.0F));

        PartDefinition side_quarter4 = partdefinition.addOrReplaceChild("side_quarter4", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 8.0667F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition side_panel4 = side_quarter4.addOrReplaceChild("side_panel4", CubeListBuilder.create().texOffs(120, 90).addBox(-8.0F, -12.0F, -16.0F, 16.0F, 12.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.9333F, 0.0F));

        PartDefinition side_panel_top_r4 = side_panel4.addOrReplaceChild("side_panel_top_r4", CubeListBuilder.create().texOffs(120, 108).addBox(-7.875F, -3.0F, -3.0F, 15.95F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1F, -12.0F, -11.8F, -0.7854F, 0.0F, 0.0F));

        PartDefinition side_panel_bottom_r4 = side_panel4.addOrReplaceChild("side_panel_bottom_r4", CubeListBuilder.create().texOffs(164, 103).addBox(-7.975F, -4.0F, -3.0F, 15.95F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -11.8F, -0.7854F, 0.0F, 0.0F));

        PartDefinition leg4 = side_quarter4.addOrReplaceChild("leg4", CubeListBuilder.create().texOffs(0, 103).addBox(-2.0F, 8.0F, -7.5F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(107, 49).addBox(-1.0F, -4.0F, 3.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.0F, 6.9333F, -10.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition inner_leg_mount_r4 = leg4.addOrReplaceChild("inner_leg_mount_r4", CubeListBuilder.create().texOffs(94, 94).addBox(-1.5F, -1.5F, -3.0F, 3.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5F, 3.0F, 3.1416F, 0.0F, 0.0F));

        PartDefinition strut_segment4 = leg4.addOrReplaceChild("strut_segment4", CubeListBuilder.create().texOffs(84, 103).addBox(-1.0F, -1.7071F, -10.2929F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(68, 100).addBox(-2.0F, -2.7071F, -1.2929F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(24, 101).addBox(-2.0F, -2.7071F, -5.2929F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.0472F, 0.0F, 0.0F));

        PartDefinition leg_strut_r4 = strut_segment4.addOrReplaceChild("leg_strut_r4", CubeListBuilder.create().texOffs(68, 87).addBox(-0.5F, -0.5F, -6.25F, 1.0F, 1.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.7929F, -3.2929F, -0.3491F, 0.0F, 0.0F));

        PartDefinition tank_assembly = partdefinition.addOrReplaceChild("tank_assembly", CubeListBuilder.create().texOffs(80, 41).addBox(8.0F, -12.0F, -14.0F, 6.0F, 12.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(98, 103).addBox(9.0F, -13.0F, -13.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 8.0667F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition tank_pipe_r1 = tank_assembly.addOrReplaceChild("tank_pipe_r1", CubeListBuilder.create().texOffs(104, 41).addBox(-1.0F, -2.0F, -2.0F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.0F, 1.0F, -11.0F, -0.5672F, -0.7854F, 0.0F));

        PartDefinition tank_assembly2 = partdefinition.addOrReplaceChild("tank_assembly2", CubeListBuilder.create().texOffs(80, 41).mirror().addBox(-14.0F, -12.0F, -14.0F, 6.0F, 12.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(98, 103).mirror().addBox(-13.0F, -13.0F, -13.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 8.0667F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition tank_pipe_r2 = tank_assembly2.addOrReplaceChild("tank_pipe_r2", CubeListBuilder.create().texOffs(104, 41).mirror().addBox(-1.0F, -2.0F, -2.0F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-11.0F, 1.0F, -11.0F, -0.5672F, 0.7854F, 0.0F));

        PartDefinition corner_panel = partdefinition.addOrReplaceChild("corner_panel", CubeListBuilder.create(), PartPose.offset(8.5858F, -1.8F, -8.5858F));

        PartDefinition corner_panel_thruster_r1 = corner_panel.addOrReplaceChild("corner_panel_thruster_r1", CubeListBuilder.create().texOffs(16, 103).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.1213F, 4.8F, -2.1213F, 0.4636F, 0.6591F, 0.6847F));

        PartDefinition corner_panel_body_r1 = corner_panel.addOrReplaceChild("corner_panel_body_r1", CubeListBuilder.create().texOffs(44, 87).addBox(-3.0F, -6.0F, -4.0F, 4.0F, 12.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.4142F, 6.8F, -1.4142F, 0.0F, 0.7854F, 0.0F));

        PartDefinition corner_panel_top_r1 = corner_panel.addOrReplaceChild("corner_panel_top_r1", CubeListBuilder.create().texOffs(0, 90).addBox(-2.45F, -2.35F, -3.9742F, 4.0F, 5.0F, 7.95F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.6155F, 0.5236F, -0.9553F));

        PartDefinition corner_panel2 = partdefinition.addOrReplaceChild("corner_panel2", CubeListBuilder.create(), PartPose.offset(-8.5858F, -1.8F, -8.5858F));

        PartDefinition corner_panel_thruster_r2 = corner_panel2.addOrReplaceChild("corner_panel_thruster_r2", CubeListBuilder.create().texOffs(16, 103).mirror().addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.1213F, 4.8F, -2.1213F, 0.4636F, -0.6591F, -0.6847F));

        PartDefinition corner_panel_body_r2 = corner_panel2.addOrReplaceChild("corner_panel_body_r2", CubeListBuilder.create().texOffs(44, 87).mirror().addBox(-1.0F, -6.0F, -4.0F, 4.0F, 12.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-1.4142F, 6.8F, -1.4142F, 0.0F, -0.7854F, 0.0F));

        PartDefinition corner_panel_top_r2 = corner_panel2.addOrReplaceChild("corner_panel_top_r2", CubeListBuilder.create().texOffs(0, 90).mirror().addBox(-1.55F, -2.35F, -3.9742F, 4.0F, 5.0F, 7.95F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.6155F, -0.5236F, 0.9553F));

        PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(0, 0).addBox(-10.0F, -25.0F, -10.0F, 20.0F, 12.0F, 20.0F, new CubeDeformation(0.0F))
                .texOffs(0, 32).addBox(-8.0F, -29.0F, -12.0F, 16.0F, 4.0F, 24.0F, new CubeDeformation(0.0F))
                .texOffs(44, 60).addBox(-6.0F, -30.0F, -6.0F, 12.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(80, 14).addBox(-5.0F, -31.0F, -5.0F, 10.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(80, 14).addBox(-5.0F, -30.0F, -5.0F, 10.0F, -1.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition antenna_dish_r1 = bb_main.addOrReplaceChild("antenna_dish_r1", CubeListBuilder.create().texOffs(92, 70).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.5F, -36.5F, 7.5F, -1.0472F, 0.7854F, 0.0F));

        PartDefinition antenna_b_r1 = bb_main.addOrReplaceChild("antenna_b_r1", CubeListBuilder.create().texOffs(40, 101).addBox(-0.5F, -5.5F, 0.0F, 1.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.5F, -32.5F, 7.5F, 0.0F, 0.7854F, 0.0F));

        PartDefinition antenna_a_r1 = bb_main.addOrReplaceChild("antenna_a_r1", CubeListBuilder.create().texOffs(42, 101).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.5F, -32.5F, -7.5F, 0.0F, 0.7854F, 0.0F));

        PartDefinition upper_cross_c_r1 = bb_main.addOrReplaceChild("upper_cross_c_r1", CubeListBuilder.create().texOffs(80, 33).addBox(-8.0F, -2.0F, -12.0F, 16.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(80, 25).addBox(-8.0F, -2.0F, 8.0F, 16.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -27.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        return LayerDefinition.create(meshdefinition, 256, 128);

    }

    public void render(PoseStack poseStack, VertexConsumer buffer, int light, int overlay) {
        root.render(poseStack, buffer, light, overlay);
    }

    public void animateArm(float angle) {
        root.getAllParts().forEach(ModelPart::resetPose);
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Driven directly by the block entity renderer.
    }

    @Override
    public ModelPart root() {
        return root;
    }
}
