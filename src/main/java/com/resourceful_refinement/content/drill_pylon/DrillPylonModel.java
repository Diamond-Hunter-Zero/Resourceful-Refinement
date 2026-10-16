package com.resourceful_refinement.content.drill_pylon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.resourceful_refinement.ResourcefulRefinementMain;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.joml.Vector3f;

public class DrillPylonModel extends HierarchicalModel<Entity> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ResourcefulRefinementMain.MOD_ID, "drill_pylon"),
            "main");

    private final ModelPart root;
    private final ModelPart drill_core;
    private final ModelPart side_group;
    private final ModelPart oct_corner;
    private final ModelPart oct_corner2;
    private final ModelPart side_group2;
    private final ModelPart oct_corner3;
    private final ModelPart oct_corner4;
    private final ModelPart under_carriage;
    private final ModelPart under_flap;
    private final ModelPart under_flap2;
    private final ModelPart under_flap3;
    private final ModelPart pylon_support;
    private final ModelPart pylon_leg;
    private final ModelPart pylon_support2;
    private final ModelPart pylon_leg2;
    private final ModelPart pylon_support3;
    private final ModelPart pylon_leg3;
    private final ModelPart pylon_support4;
    private final ModelPart pylon_leg4;
    private final ModelPart drill_bit;
    private final ModelPart upper_drill;
    private final ModelPart upper_segment;
    private final ModelPart upper_segment2;
    private final ModelPart upper_segment3;
    private final ModelPart upper_segment4;
    private final ModelPart upper_segment5;
    private final ModelPart upper_segment6;
    private final ModelPart middle_drill;
    private final ModelPart mid_segment;
    private final ModelPart mid_segment2;
    private final ModelPart mid_segment3;
    private final ModelPart mid_segment4;
    private final ModelPart mid_segment5;
    private final ModelPart mid_segment6;
    private final ModelPart lower_drill;
    private final ModelPart lower_segment;
    private final ModelPart lower_segment2;
    private final ModelPart lower_segment3;
    private final ModelPart shaft;

    public DrillPylonModel(ModelPart root) {
        this.root = root;

        this.drill_core = root.getChild("drill_core");
        this.side_group = this.drill_core.getChild("side_group");
        this.oct_corner = this.side_group.getChild("oct_corner");
        this.oct_corner2 = this.side_group.getChild("oct_corner2");
        this.side_group2 = this.drill_core.getChild("side_group2");
        this.oct_corner3 = this.side_group2.getChild("oct_corner3");
        this.oct_corner4 = this.side_group2.getChild("oct_corner4");
        this.under_carriage = this.drill_core.getChild("under_carriage");
        this.under_flap = this.under_carriage.getChild("under_flap");
        this.under_flap2 = this.under_carriage.getChild("under_flap2");
        this.under_flap3 = this.under_carriage.getChild("under_flap3");
        this.pylon_support = root.getChild("pylon_support");
        this.pylon_leg = this.pylon_support.getChild("pylon_leg");
        this.pylon_support2 = root.getChild("pylon_support2");
        this.pylon_leg2 = this.pylon_support2.getChild("pylon_leg2");
        this.pylon_support3 = root.getChild("pylon_support3");
        this.pylon_leg3 = this.pylon_support3.getChild("pylon_leg3");
        this.pylon_support4 = root.getChild("pylon_support4");
        this.pylon_leg4 = this.pylon_support4.getChild("pylon_leg4");
        this.drill_bit = root.getChild("drill_bit");
        this.upper_drill = this.drill_bit.getChild("upper_drill");
        this.upper_segment = this.upper_drill.getChild("upper_segment");
        this.upper_segment2 = this.upper_drill.getChild("upper_segment2");
        this.upper_segment3 = this.upper_drill.getChild("upper_segment3");
        this.upper_segment4 = this.upper_drill.getChild("upper_segment4");
        this.upper_segment5 = this.upper_drill.getChild("upper_segment5");
        this.upper_segment6 = this.upper_drill.getChild("upper_segment6");
        this.middle_drill = this.drill_bit.getChild("middle_drill");
        this.mid_segment = this.middle_drill.getChild("mid_segment");
        this.mid_segment2 = this.middle_drill.getChild("mid_segment2");
        this.mid_segment3 = this.middle_drill.getChild("mid_segment3");
        this.mid_segment4 = this.middle_drill.getChild("mid_segment4");
        this.mid_segment5 = this.middle_drill.getChild("mid_segment5");
        this.mid_segment6 = this.middle_drill.getChild("mid_segment6");
        this.lower_drill = this.drill_bit.getChild("lower_drill");
        this.lower_segment = this.lower_drill.getChild("lower_segment");
        this.lower_segment2 = this.lower_drill.getChild("lower_segment2");
        this.lower_segment3 = this.lower_drill.getChild("lower_segment3");
        this.shaft = this.drill_bit.getChild("shaft");
        }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition drill_core = partdefinition.addOrReplaceChild("drill_core", CubeListBuilder.create().texOffs(0, 0).addBox(-12.0F, -43.0F, -23.0F, 24.0F, 39.0F, 46.0F, new CubeDeformation(0.0F))
                .texOffs(0, 86).addBox(-12.0F, -47.0F, -19.0F, 24.0F, 4.0F, 38.0F, new CubeDeformation(0.0F))
                .texOffs(114, 129).addBox(10.0F, -43.0F, -24.0F, 2.0F, 39.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(114, 129).mirror().addBox(-12.0F, -43.0F, -24.0F, 2.0F, 39.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(141, 77).addBox(-10.0F, -6.0F, -24.0F, 20.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(212, 30).addBox(-6.0F, -15.0F, -24.0F, 12.0F, 9.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(201, 82).addBox(2.0F, -16.0F, -24.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(201, 82).addBox(-4.0F, -16.0F, -24.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(202, 96).addBox(-7.0F, -31.0F, 23.0F, 14.0F, 14.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(141, 81).addBox(-10.0F, -39.0F, -24.0F, 20.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -8.0F, 0.0F));

        PartDefinition side_rail_top_r1 = drill_core.addOrReplaceChild("side_rail_top_r1", CubeListBuilder.create().texOffs(141, 81).addBox(-10.0F, -40.0F, -24.0F, 20.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 34.0F, 0.0F, -3.1416F, 0.0F, 3.1416F));

        PartDefinition side_rail_top_r2 = drill_core.addOrReplaceChild("side_rail_top_r2", CubeListBuilder.create().texOffs(141, 81).addBox(-10.0F, -40.0F, -24.0F, 20.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, -3.1416F, 0.0F, 3.1416F));

        PartDefinition rail_r1 = drill_core.addOrReplaceChild("rail_r1", CubeListBuilder.create().texOffs(114, 129).mirror().addBox(-12.0F, -39.0F, -24.0F, 2.0F, 39.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(114, 129).addBox(10.0F, -39.0F, -24.0F, 2.0F, 39.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, -3.1416F, 0.0F, 3.1416F));

        PartDefinition bevel_rail_r1 = drill_core.addOrReplaceChild("bevel_rail_r1", CubeListBuilder.create().texOffs(139, 217).mirror().addBox(-12.01F, -17.6066F, -44.5772F, 2.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(139, 217).addBox(10.01F, -17.6066F, -44.5772F, 2.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 2.3562F, 0.0F, 3.1416F));

        PartDefinition bevel_rail_r2 = drill_core.addOrReplaceChild("bevel_rail_r2", CubeListBuilder.create().texOffs(139, 217).mirror().addBox(-12.1F, 9.9706F, -17.0F, 2.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(139, 217).addBox(9.92F, 9.9706F, -17.0F, 2.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.09F, -43.0F, 0.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition top_rail_r1 = drill_core.addOrReplaceChild("top_rail_r1", CubeListBuilder.create().texOffs(67, 210).mirror().addBox(-1.0F, -19.0F, -0.5F, 2.0F, 38.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(185, 180).addBox(3.0F, -8.0F, -0.5F, 16.0F, 16.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(67, 210).addBox(21.0F, -19.0F, -0.5F, 2.0F, 38.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-11.0F, -47.5F, 0.0F, -1.5708F, 0.0F, 0.0F));

        PartDefinition top_bevel_r1 = drill_core.addOrReplaceChild("top_bevel_r1", CubeListBuilder.create().texOffs(141, 66).addBox(-12.0F, -14.8492F, -17.8492F, 24.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -45.0F, 0.0F, 0.0F, 1.5708F, -0.7854F));

        PartDefinition top_bevel_r2 = drill_core.addOrReplaceChild("top_bevel_r2", CubeListBuilder.create().texOffs(141, 66).addBox(-12.0F, -14.8492F, -17.8492F, 24.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -45.0F, 0.0F, -2.3562F, 0.0F, 3.1416F));

        PartDefinition top_bevel_r3 = drill_core.addOrReplaceChild("top_bevel_r3", CubeListBuilder.create().texOffs(141, 66).addBox(-12.0F, -14.8492F, -17.8492F, 24.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -45.0F, 0.0F, 0.7854F, 0.0F, 0.0F));

        PartDefinition side_group = drill_core.addOrReplaceChild("side_group", CubeListBuilder.create().texOffs(0, 154).addBox(12.0F, -11.0F, -12.0F, 11.0F, 6.0F, 24.0F, new CubeDeformation(0.0F))
                .texOffs(141, 35).addBox(12.0F, 22.0F, -12.0F, 11.0F, 6.0F, 24.0F, new CubeDeformation(0.0F))
                .texOffs(134, 181).addBox(12.0F, -2.0F, -7.0F, 11.0F, 21.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(202, 80).addBox(17.0F, 19.0F, -6.0F, 4.0F, 3.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(202, 80).addBox(17.0F, -5.0F, -6.0F, 4.0F, 3.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(206, 212).addBox(12.0F, -16.0F, -12.0F, 7.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(206, 212).mirror().addBox(-19.0F, -16.0F, -12.0F, 7.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(71, 181).addBox(12.0F, -15.0F, -12.0F, 7.0F, 4.0F, 24.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -32.0F, 0.0F));

        PartDefinition bevel_rail_r3 = side_group.addOrReplaceChild("bevel_rail_r3", CubeListBuilder.create().texOffs(139, 217).mirror().addBox(-12.0F, 10.0342F, -17.0F, 2.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -10.9706F, 0.01F, 0.0F, -1.5708F, -0.7854F));

        PartDefinition bevel_rail_r4 = side_group.addOrReplaceChild("bevel_rail_r4", CubeListBuilder.create().texOffs(139, 217).addBox(10.0F, 9.9069F, -17.0F, 2.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -11.0F, -0.01F, 0.0F, -1.5708F, -0.7854F));

        PartDefinition top_bevel_r4 = side_group.addOrReplaceChild("top_bevel_r4", CubeListBuilder.create().texOffs(141, 66).addBox(-12.0F, -14.8492F, -17.8492F, 24.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -13.0F, 0.0F, 0.0F, -1.5708F, 0.7854F));

        PartDefinition shaft_frame_edge_r1 = side_group.addOrReplaceChild("shaft_frame_edge_r1", CubeListBuilder.create().texOffs(212, 41).addBox(-7.0F, 5.0F, 23.0F, 14.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 20.0F, 0.0F, 0.0F, -1.5708F, -3.1416F));

        PartDefinition shaft_frame_strut_r1 = side_group.addOrReplaceChild("shaft_frame_strut_r1", CubeListBuilder.create().texOffs(71, 154).addBox(5.0F, -7.0F, 23.0F, 1.0F, 10.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(212, 41).addBox(-7.0F, -9.0F, 23.0F, 14.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 10.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition shaft_frame_strut_r2 = side_group.addOrReplaceChild("shaft_frame_strut_r2", CubeListBuilder.create().texOffs(71, 154).addBox(5.0F, -7.0F, 23.0F, 1.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 10.0F, 11.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition side_rail_bottom_r1 = side_group.addOrReplaceChild("side_rail_bottom_r1", CubeListBuilder.create().texOffs(153, 217).addBox(10.0F, -39.0F, -24.0F, 2.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(153, 217).mirror().addBox(-12.0F, -39.0F, -24.0F, 2.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(146, 217).addBox(10.0F, -72.0F, -24.0F, 2.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(146, 217).mirror().addBox(-12.0F, -72.0F, -24.0F, 2.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 61.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition oct_corner = side_group.addOrReplaceChild("oct_corner", CubeListBuilder.create().texOffs(185, 212).addBox(12.0F, -46.0F, -17.0F, 5.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 31.0F, 0.0F));

        PartDefinition top_corner_bevel_r1 = oct_corner.addOrReplaceChild("top_corner_bevel_r1", CubeListBuilder.create().texOffs(71, 170).addBox(-6.0F, -17.0992F, -17.8492F, 12.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -42.5858F, 0.0F, 0.7854F, -0.7854F, 0.0F));

        PartDefinition corner_side_rail_top_r1 = oct_corner.addOrReplaceChild("corner_side_rail_top_r1", CubeListBuilder.create().texOffs(209, 22).addBox(-8.0F, -1.0F, -0.75F, 17.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(209, 26).addBox(-8.0F, 32.0F, -0.75F, 17.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(17.1716F, -37.0F, -17.8432F, 0.0F, -0.7854F, 0.0F));

        PartDefinition corner_panel_r1 = oct_corner.addOrReplaceChild("corner_panel_r1", CubeListBuilder.create().texOffs(125, 125).addBox(-6.75F, -42.0F, -8.0F, 12.0F, 39.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(13.8609F, 0.01F, -13.818F, 0.0F, 0.7854F, 0.0F));

        PartDefinition oct_corner2 = side_group.addOrReplaceChild("oct_corner2", CubeListBuilder.create().texOffs(185, 212).addBox(12.0F, -46.0F, -17.0F, 5.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 31.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition top_corner_bevel_r2 = oct_corner2.addOrReplaceChild("top_corner_bevel_r2", CubeListBuilder.create().texOffs(71, 170).addBox(-6.0F, -17.0992F, -17.8492F, 12.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -42.5858F, 0.0F, 0.7854F, -0.7854F, 0.0F));

        PartDefinition corner_side_rail_top_r2 = oct_corner2.addOrReplaceChild("corner_side_rail_top_r2", CubeListBuilder.create().texOffs(209, 22).addBox(-8.0F, -1.0F, -0.75F, 17.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(209, 26).addBox(-8.0F, 32.0F, -0.75F, 17.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(17.1716F, -37.0F, -17.8432F, 0.0F, -0.7854F, 0.0F));

        PartDefinition corner_panel_r2 = oct_corner2.addOrReplaceChild("corner_panel_r2", CubeListBuilder.create().texOffs(125, 125).addBox(-6.75F, -41.99F, -8.0F, 12.0F, 39.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(13.8609F, 0.0F, -13.818F, 0.0F, 0.7854F, 0.0F));

        PartDefinition side_group2 = drill_core.addOrReplaceChild("side_group2", CubeListBuilder.create().texOffs(0, 154).addBox(12.0F, -11.0F, -12.0F, 11.0F, 6.0F, 24.0F, new CubeDeformation(0.0F))
                .texOffs(141, 35).addBox(12.0F, 22.0F, -12.0F, 11.0F, 6.0F, 24.0F, new CubeDeformation(0.0F))
                .texOffs(134, 181).addBox(12.0F, -2.0F, -7.0F, 11.0F, 21.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(202, 80).addBox(17.0F, 19.0F, -6.0F, 4.0F, 3.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(202, 80).addBox(17.0F, -5.0F, -6.0F, 4.0F, 3.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(206, 212).addBox(12.0F, -16.0F, -12.0F, 7.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(206, 212).mirror().addBox(-19.0F, -16.0F, -12.0F, 7.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(71, 181).addBox(12.0F, -15.0F, -12.0F, 7.0F, 4.0F, 24.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -32.0F, 0.0F, -3.1416F, 0.0F, 3.1416F));

        PartDefinition bevel_rail_r5 = side_group2.addOrReplaceChild("bevel_rail_r5", CubeListBuilder.create().texOffs(139, 217).mirror().addBox(-12.0F, 10.0342F, -17.0F, 2.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -10.9706F, 0.01F, 0.0F, -1.5708F, -0.7854F));

        PartDefinition bevel_rail_r6 = side_group2.addOrReplaceChild("bevel_rail_r6", CubeListBuilder.create().texOffs(139, 217).addBox(10.0F, 9.9069F, -17.0F, 2.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -11.0F, -0.01F, 0.0F, -1.5708F, -0.7854F));

        PartDefinition top_bevel_r5 = side_group2.addOrReplaceChild("top_bevel_r5", CubeListBuilder.create().texOffs(141, 66).addBox(-12.0F, -14.8492F, -17.8492F, 24.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -13.0F, 0.0F, 0.0F, -1.5708F, 0.7854F));

        PartDefinition shaft_frame_edge_r2 = side_group2.addOrReplaceChild("shaft_frame_edge_r2", CubeListBuilder.create().texOffs(212, 41).addBox(-7.0F, -7.0F, 23.0F, 14.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 8.0F, 0.0F, 0.0F, -1.5708F, 3.1416F));

        PartDefinition shaft_frame_strut_r3 = side_group2.addOrReplaceChild("shaft_frame_strut_r3", CubeListBuilder.create().texOffs(71, 154).addBox(5.0F, -7.0F, 23.0F, 1.0F, 10.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(212, 41).addBox(-7.0F, -9.0F, 23.0F, 14.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 10.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition shaft_frame_strut_r4 = side_group2.addOrReplaceChild("shaft_frame_strut_r4", CubeListBuilder.create().texOffs(71, 154).addBox(5.0F, -7.0F, 23.0F, 1.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 10.0F, 11.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition side_rail_bottom_r2 = side_group2.addOrReplaceChild("side_rail_bottom_r2", CubeListBuilder.create().texOffs(153, 217).addBox(10.0F, -39.0F, -24.0F, 2.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(153, 217).mirror().addBox(-12.0F, -39.0F, -24.0F, 2.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(146, 217).addBox(10.0F, -72.0F, -24.0F, 2.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(146, 217).mirror().addBox(-12.0F, -72.0F, -24.0F, 2.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 61.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition oct_corner3 = side_group2.addOrReplaceChild("oct_corner3", CubeListBuilder.create().texOffs(185, 212).addBox(12.0F, -46.0F, -17.0F, 5.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 31.0F, 0.0F));

        PartDefinition top_corner_bevel_r3 = oct_corner3.addOrReplaceChild("top_corner_bevel_r3", CubeListBuilder.create().texOffs(71, 170).addBox(-6.0F, -17.0992F, -17.8492F, 12.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -42.5858F, 0.0F, 0.7854F, -0.7854F, 0.0F));

        PartDefinition corner_side_rail_top_r3 = oct_corner3.addOrReplaceChild("corner_side_rail_top_r3", CubeListBuilder.create().texOffs(209, 22).addBox(-8.0F, -1.0F, -0.75F, 17.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(209, 26).addBox(-8.0F, 32.0F, -0.75F, 17.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(17.1716F, -37.0F, -17.8432F, 0.0F, -0.7854F, 0.0F));

        PartDefinition corner_panel_r3 = oct_corner3.addOrReplaceChild("corner_panel_r3", CubeListBuilder.create().texOffs(125, 125).addBox(-6.75F, -41.99F, -8.0F, 12.0F, 39.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(13.8609F, 0.0F, -13.818F, 0.0F, 0.7854F, 0.0F));

        PartDefinition oct_corner4 = side_group2.addOrReplaceChild("oct_corner4", CubeListBuilder.create().texOffs(185, 212).addBox(12.0F, -46.0F, -17.0F, 5.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 31.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition top_corner_bevel_r4 = oct_corner4.addOrReplaceChild("top_corner_bevel_r4", CubeListBuilder.create().texOffs(71, 170).addBox(-6.0F, -17.0992F, -17.8492F, 12.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -42.5858F, 0.0F, 0.7854F, -0.7854F, 0.0F));

        PartDefinition corner_side_rail_top_r4 = oct_corner4.addOrReplaceChild("corner_side_rail_top_r4", CubeListBuilder.create().texOffs(209, 22).addBox(-8.0F, -1.0F, -0.75F, 17.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(209, 26).addBox(-8.0F, 32.0F, -0.75F, 17.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(17.1716F, -37.0F, -17.8432F, 0.0F, -0.7854F, 0.0F));

        PartDefinition corner_panel_r4 = oct_corner4.addOrReplaceChild("corner_panel_r4", CubeListBuilder.create().texOffs(125, 125).addBox(-6.75F, -41.99F, -8.0F, 12.0F, 39.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(13.8609F, 0.0F, -13.818F, 0.0F, 0.7854F, 0.0F));

        PartDefinition under_carriage = drill_core.addOrReplaceChild("under_carriage", CubeListBuilder.create().texOffs(182, 125).addBox(-6.0F, -4.0F, -4.0F, 12.0F, 4.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(0, 129).addBox(-10.0F, -4.0F, 10.0F, 20.0F, 4.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -20.0F));

        PartDefinition under_flap = under_carriage.addOrReplaceChild("under_flap", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -1.5F, 20.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition drill_mount_strut_r1 = under_flap.addOrReplaceChild("drill_mount_strut_r1", CubeListBuilder.create().texOffs(101, 210).mirror().addBox(-1.0F, -1.0F, -9.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(101, 210).addBox(9.0F, -1.0F, -9.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(210, 0).addBox(1.0F, 0.0F, -7.0F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, 0.0F, -10.5F, -0.3927F, 0.0F, 0.0F));

        PartDefinition under_flap2 = under_carriage.addOrReplaceChild("under_flap2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -1.5F, 20.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition drill_mount_strut_r2 = under_flap2.addOrReplaceChild("drill_mount_strut_r2", CubeListBuilder.create().texOffs(101, 210).mirror().addBox(-1.0F, -1.0F, -9.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(101, 210).addBox(9.0F, -1.0F, -9.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(210, 0).addBox(1.0F, 0.0F, -7.0F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, 0.0F, -10.5F, -0.3927F, 0.0F, 0.0F));

        PartDefinition under_flap3 = under_carriage.addOrReplaceChild("under_flap3", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -1.5F, 20.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition drill_mount_strut_r3 = under_flap3.addOrReplaceChild("drill_mount_strut_r3", CubeListBuilder.create().texOffs(101, 210).mirror().addBox(-1.0F, -1.0F, -9.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(101, 210).addBox(9.0F, -1.0F, -9.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(210, 0).addBox(1.0F, 0.0F, -7.0F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, 0.0F, -10.5F, -0.3927F, 0.0F, 0.0F));

        PartDefinition pylon_support = partdefinition.addOrReplaceChild("pylon_support", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition pylon_lower_foot_r1 = pylon_support.addOrReplaceChild("pylon_lower_foot_r1", CubeListBuilder.create().texOffs(202, 66).addBox(-4.5F, -4.0F, -4.5F, 9.0F, 4.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -45.2548F, 0.0F, 0.7854F, 0.0F));

        PartDefinition pylon_leg = pylon_support.addOrReplaceChild("pylon_leg", CubeListBuilder.create().texOffs(182, 144).addBox(-4.0F, -48.0F, -4.0F, 8.0F, 14.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(46, 185).addBox(-2.5F, -32.0F, -2.5F, 5.0F, 31.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(182, 167).addBox(-5.0F, -34.0F, -5.0F, 10.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(215, 144).addBox(-4.0F, -50.0F, -5.0F, 8.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(215, 148).addBox(-4.0F, -50.0F, 4.0F, 8.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 185).addBox(-5.0F, -80.0F, -5.0F, 1.0F, 32.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(0, 185).addBox(4.0F, -80.0F, -5.0F, 1.0F, 32.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(81, 129).addBox(-4.0F, -80.0F, -4.0F, 8.0F, 32.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -45.2548F, -0.0873F, 0.0F, 0.0F));

        PartDefinition pylon_lower_strut_r1 = pylon_leg.addOrReplaceChild("pylon_lower_strut_r1", CubeListBuilder.create().texOffs(141, 0).addBox(-2.0F, -2.0F, 3.0F, 4.0F, 4.0F, 30.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -35.0F, 0.0F, 0.3927F, 0.0F, 0.0F));

        PartDefinition pylon_top_strut_r1 = pylon_leg.addOrReplaceChild("pylon_top_strut_r1", CubeListBuilder.create().texOffs(125, 86).addBox(-4.0F, -4.0F, 3.0F, 8.0F, 8.0F, 30.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -72.0F, 0.0F, -0.0873F, 0.0F, 0.0F));

        PartDefinition pylon_support2 = partdefinition.addOrReplaceChild("pylon_support2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

        PartDefinition pylon_lower_foot_r2 = pylon_support2.addOrReplaceChild("pylon_lower_foot_r2", CubeListBuilder.create().texOffs(202, 66).addBox(-4.5F, -4.0F, -4.5F, 9.0F, 4.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -45.2548F, 0.0F, 0.7854F, 0.0F));

        PartDefinition pylon_leg2 = pylon_support2.addOrReplaceChild("pylon_leg2", CubeListBuilder.create().texOffs(182, 144).addBox(-4.0F, -48.0F, -4.0F, 8.0F, 14.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(46, 185).addBox(-2.5F, -32.0F, -2.5F, 5.0F, 31.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(182, 167).addBox(-5.0F, -34.0F, -5.0F, 10.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(215, 144).addBox(-4.0F, -50.0F, -5.0F, 8.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(215, 148).addBox(-4.0F, -50.0F, 4.0F, 8.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 185).addBox(-5.0F, -80.0F, -5.0F, 1.0F, 32.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(0, 185).addBox(4.0F, -80.0F, -5.0F, 1.0F, 32.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(81, 129).addBox(-4.0F, -80.0F, -4.0F, 8.0F, 32.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -45.2548F, -0.0873F, 0.0F, 0.0F));

        PartDefinition pylon_lower_strut_r2 = pylon_leg2.addOrReplaceChild("pylon_lower_strut_r2", CubeListBuilder.create().texOffs(141, 0).addBox(-2.0F, -2.0F, 3.0F, 4.0F, 4.0F, 30.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -35.0F, 0.0F, 0.3927F, 0.0F, 0.0F));

        PartDefinition pylon_top_strut_r2 = pylon_leg2.addOrReplaceChild("pylon_top_strut_r2", CubeListBuilder.create().texOffs(125, 86).addBox(-4.0F, -4.0F, 3.0F, 8.0F, 8.0F, 30.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -72.0F, 0.0F, -0.0873F, 0.0F, 0.0F));

        PartDefinition pylon_support3 = partdefinition.addOrReplaceChild("pylon_support3", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 2.3562F, 0.0F));

        PartDefinition pylon_lower_foot_r3 = pylon_support3.addOrReplaceChild("pylon_lower_foot_r3", CubeListBuilder.create().texOffs(202, 66).addBox(-4.5F, -4.0F, -4.5F, 9.0F, 4.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -45.2548F, 0.0F, 0.7854F, 0.0F));

        PartDefinition pylon_leg3 = pylon_support3.addOrReplaceChild("pylon_leg3", CubeListBuilder.create().texOffs(182, 144).addBox(-4.0F, -48.0F, -4.0F, 8.0F, 14.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(46, 185).addBox(-2.5F, -32.0F, -2.5F, 5.0F, 31.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(182, 167).addBox(-5.0F, -34.0F, -5.0F, 10.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(215, 144).addBox(-4.0F, -50.0F, -5.0F, 8.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(215, 148).addBox(-4.0F, -50.0F, 4.0F, 8.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 185).addBox(-5.0F, -80.0F, -5.0F, 1.0F, 32.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(0, 185).addBox(4.0F, -80.0F, -5.0F, 1.0F, 32.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(81, 129).addBox(-4.0F, -80.0F, -4.0F, 8.0F, 32.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -45.2548F, -0.0873F, 0.0F, 0.0F));

        PartDefinition pylon_lower_strut_r3 = pylon_leg3.addOrReplaceChild("pylon_lower_strut_r3", CubeListBuilder.create().texOffs(141, 0).addBox(-2.0F, -2.0F, 3.0F, 4.0F, 4.0F, 30.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -35.0F, 0.0F, 0.3927F, 0.0F, 0.0F));

        PartDefinition pylon_top_strut_r3 = pylon_leg3.addOrReplaceChild("pylon_top_strut_r3", CubeListBuilder.create().texOffs(125, 86).addBox(-4.0F, -4.0F, 3.0F, 8.0F, 8.0F, 30.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -72.0F, 0.0F, -0.0873F, 0.0F, 0.0F));

        PartDefinition pylon_support4 = partdefinition.addOrReplaceChild("pylon_support4", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, -2.3562F, 0.0F));

        PartDefinition pylon_lower_foot_r4 = pylon_support4.addOrReplaceChild("pylon_lower_foot_r4", CubeListBuilder.create().texOffs(202, 66).addBox(-4.5F, -4.0F, -4.5F, 9.0F, 4.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -45.2548F, 0.0F, 0.7854F, 0.0F));

        PartDefinition pylon_leg4 = pylon_support4.addOrReplaceChild("pylon_leg4", CubeListBuilder.create().texOffs(182, 144).addBox(-4.0F, -48.0F, -4.0F, 8.0F, 14.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(46, 185).addBox(-2.5F, -32.0F, -2.5F, 5.0F, 31.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(182, 167).addBox(-5.0F, -34.0F, -5.0F, 10.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(215, 144).addBox(-4.0F, -50.0F, -5.0F, 8.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(215, 148).addBox(-4.0F, -50.0F, 4.0F, 8.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 185).addBox(-5.0F, -80.0F, -5.0F, 1.0F, 32.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(0, 185).addBox(4.0F, -80.0F, -5.0F, 1.0F, 32.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(81, 129).addBox(-4.0F, -80.0F, -4.0F, 8.0F, 32.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -45.2548F, -0.0873F, 0.0F, 0.0F));

        PartDefinition pylon_lower_strut_r4 = pylon_leg4.addOrReplaceChild("pylon_lower_strut_r4", CubeListBuilder.create().texOffs(141, 0).addBox(-2.0F, -2.0F, 3.0F, 4.0F, 4.0F, 30.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -35.0F, 0.0F, 0.3927F, 0.0F, 0.0F));

        PartDefinition pylon_top_strut_r4 = pylon_leg4.addOrReplaceChild("pylon_top_strut_r4", CubeListBuilder.create().texOffs(125, 86).addBox(-4.0F, -4.0F, 3.0F, 8.0F, 8.0F, 30.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -72.0F, 0.0F, -0.0873F, 0.0F, 0.0F));

        PartDefinition drill_bit = partdefinition.addOrReplaceChild("drill_bit", CubeListBuilder.create(), PartPose.offset(0.0F, -2.0F, 0.0F));

        PartDefinition upper_drill = drill_bit.addOrReplaceChild("upper_drill", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition upper_segment = upper_drill.addOrReplaceChild("upper_segment", CubeListBuilder.create().texOffs(202, 112).addBox(3.0F, 0.0F, -3.0F, 11.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition upper_joining_segment_r1 = upper_segment.addOrReplaceChild("upper_joining_segment_r1", CubeListBuilder.create().texOffs(185, 198).addBox(3.0F, -2.0F, -4.0F, 10.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.99F, 0.0F, 0.0F, -0.5236F, 0.0F));

        PartDefinition upper_tooth_base_r1 = upper_segment.addOrReplaceChild("upper_tooth_base_r1", CubeListBuilder.create().texOffs(108, 170).addBox(-1.5F, -3.0F, -2.5F, 3.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(126, 217).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(12.0F, 7.5F, 0.0F, 0.0F, 0.0F, 0.0873F));

        PartDefinition upper_segment2 = upper_drill.addOrReplaceChild("upper_segment2", CubeListBuilder.create().texOffs(202, 112).addBox(3.0F, 0.0F, -3.0F, 11.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.0472F, 0.0F));

        PartDefinition upper_joining_segment_r2 = upper_segment2.addOrReplaceChild("upper_joining_segment_r2", CubeListBuilder.create().texOffs(185, 198).addBox(3.0F, -2.0F, -4.0F, 10.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.99F, 0.0F, 0.0F, -0.5236F, 0.0F));

        PartDefinition upper_tooth_base_r2 = upper_segment2.addOrReplaceChild("upper_tooth_base_r2", CubeListBuilder.create().texOffs(108, 170).addBox(-1.5F, -3.0F, -2.5F, 3.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(126, 217).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(12.0F, 7.5F, 0.0F, 0.0F, 0.0F, 0.0873F));

        PartDefinition upper_segment3 = upper_drill.addOrReplaceChild("upper_segment3", CubeListBuilder.create().texOffs(202, 112).addBox(3.0F, 0.0F, -3.0F, 11.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 2.0944F, 0.0F));

        PartDefinition upper_joining_segment_r3 = upper_segment3.addOrReplaceChild("upper_joining_segment_r3", CubeListBuilder.create().texOffs(185, 198).addBox(3.0F, -2.0F, -4.0F, 10.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.99F, 0.0F, 0.0F, -0.5236F, 0.0F));

        PartDefinition upper_tooth_base_r3 = upper_segment3.addOrReplaceChild("upper_tooth_base_r3", CubeListBuilder.create().texOffs(108, 170).addBox(-1.5F, -3.0F, -2.5F, 3.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(126, 217).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(12.0F, 7.5F, 0.0F, 0.0F, 0.0F, 0.0873F));

        PartDefinition upper_segment4 = upper_drill.addOrReplaceChild("upper_segment4", CubeListBuilder.create().texOffs(202, 112).addBox(3.0F, 0.0F, -3.0F, 11.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition upper_joining_segment_r4 = upper_segment4.addOrReplaceChild("upper_joining_segment_r4", CubeListBuilder.create().texOffs(185, 198).addBox(3.0F, -2.0F, -4.0F, 10.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.99F, 0.0F, 0.0F, -0.5236F, 0.0F));

        PartDefinition upper_tooth_base_r4 = upper_segment4.addOrReplaceChild("upper_tooth_base_r4", CubeListBuilder.create().texOffs(108, 170).addBox(-1.5F, -3.0F, -2.5F, 3.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(126, 217).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(12.0F, 7.5F, 0.0F, 0.0F, 0.0F, 0.0873F));

        PartDefinition upper_segment5 = upper_drill.addOrReplaceChild("upper_segment5", CubeListBuilder.create().texOffs(202, 112).addBox(3.0F, 0.0F, -3.0F, 11.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -2.0944F, 0.0F));

        PartDefinition upper_joining_segment_r5 = upper_segment5.addOrReplaceChild("upper_joining_segment_r5", CubeListBuilder.create().texOffs(185, 198).addBox(3.0F, -2.0F, -4.0F, 10.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.99F, 0.0F, 0.0F, -0.5236F, 0.0F));

        PartDefinition upper_tooth_base_r5 = upper_segment5.addOrReplaceChild("upper_tooth_base_r5", CubeListBuilder.create().texOffs(108, 170).addBox(-1.5F, -3.0F, -2.5F, 3.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(126, 217).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(12.0F, 7.5F, 0.0F, 0.0F, 0.0F, 0.0873F));

        PartDefinition upper_segment6 = upper_drill.addOrReplaceChild("upper_segment6", CubeListBuilder.create().texOffs(202, 112).addBox(3.0F, 0.0F, -3.0F, 11.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.0472F, 0.0F));

        PartDefinition upper_joining_segment_r6 = upper_segment6.addOrReplaceChild("upper_joining_segment_r6", CubeListBuilder.create().texOffs(185, 198).addBox(3.0F, -2.0F, -4.0F, 10.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.99F, 0.0F, 0.0F, -0.5236F, 0.0F));

        PartDefinition upper_tooth_base_r6 = upper_segment6.addOrReplaceChild("upper_tooth_base_r6", CubeListBuilder.create().texOffs(108, 170).addBox(-1.5F, -3.0F, -2.5F, 3.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(126, 217).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(12.0F, 7.5F, 0.0F, 0.0F, 0.0F, 0.0873F));

        PartDefinition middle_drill = drill_bit.addOrReplaceChild("middle_drill", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 5.0F, 0.0F, 0.0F, -0.5236F, 0.0F));

        PartDefinition mid_segment = middle_drill.addOrReplaceChild("mid_segment", CubeListBuilder.create().texOffs(210, 9).addBox(2.0F, 0.0F, -3.0F, 8.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition mid_joining_segment_r1 = mid_segment.addOrReplaceChild("mid_joining_segment_r1", CubeListBuilder.create().texOffs(74, 210).addBox(2.5F, -2.0F, -3.0F, 7.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.99F, 0.0F, 0.0F, -0.5236F, 0.0F));

        PartDefinition mid_tooth_r1 = mid_segment.addOrReplaceChild("mid_tooth_r1", CubeListBuilder.create().texOffs(215, 152).addBox(-1.5F, -3.0F, -1.5F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0F, 8.5F, 0.0F, 0.0F, 0.0F, 0.0873F));

        PartDefinition mid_segment2 = middle_drill.addOrReplaceChild("mid_segment2", CubeListBuilder.create().texOffs(210, 9).addBox(2.0F, 0.0F, -3.0F, 8.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.0472F, 0.0F));

        PartDefinition mid_joining_segment_r2 = mid_segment2.addOrReplaceChild("mid_joining_segment_r2", CubeListBuilder.create().texOffs(74, 210).addBox(2.5F, -2.0F, -3.0F, 7.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.99F, 0.0F, 0.0F, -0.5236F, 0.0F));

        PartDefinition mid_tooth_r2 = mid_segment2.addOrReplaceChild("mid_tooth_r2", CubeListBuilder.create().texOffs(215, 152).addBox(-1.5F, -3.0F, -1.5F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0F, 8.5F, 0.0F, 0.0F, 0.0F, 0.0873F));

        PartDefinition mid_segment3 = middle_drill.addOrReplaceChild("mid_segment3", CubeListBuilder.create().texOffs(210, 9).addBox(2.0F, 0.0F, -3.0F, 8.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -2.0944F, 0.0F));

        PartDefinition mid_joining_segment_r3 = mid_segment3.addOrReplaceChild("mid_joining_segment_r3", CubeListBuilder.create().texOffs(74, 210).addBox(2.5F, -2.0F, -3.0F, 7.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.99F, 0.0F, 0.0F, -0.5236F, 0.0F));

        PartDefinition mid_tooth_r3 = mid_segment3.addOrReplaceChild("mid_tooth_r3", CubeListBuilder.create().texOffs(215, 152).addBox(-1.5F, -3.0F, -1.5F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0F, 8.5F, 0.0F, 0.0F, 0.0F, 0.0873F));

        PartDefinition mid_segment4 = middle_drill.addOrReplaceChild("mid_segment4", CubeListBuilder.create().texOffs(210, 9).addBox(2.0F, 0.0F, -3.0F, 8.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition mid_joining_segment_r4 = mid_segment4.addOrReplaceChild("mid_joining_segment_r4", CubeListBuilder.create().texOffs(74, 210).addBox(2.5F, -2.0F, -3.0F, 7.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.99F, 0.0F, 0.0F, -0.5236F, 0.0F));

        PartDefinition mid_tooth_r4 = mid_segment4.addOrReplaceChild("mid_tooth_r4", CubeListBuilder.create().texOffs(215, 152).addBox(-1.5F, -3.0F, -1.5F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0F, 8.5F, 0.0F, 0.0F, 0.0F, 0.0873F));

        PartDefinition mid_segment5 = middle_drill.addOrReplaceChild("mid_segment5", CubeListBuilder.create().texOffs(210, 9).addBox(2.0F, 0.0F, -3.0F, 8.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 2.0944F, 0.0F));

        PartDefinition mid_joining_segment_r5 = mid_segment5.addOrReplaceChild("mid_joining_segment_r5", CubeListBuilder.create().texOffs(74, 210).addBox(2.5F, -2.0F, -3.0F, 7.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.99F, 0.0F, 0.0F, -0.5236F, 0.0F));

        PartDefinition mid_tooth_r5 = mid_segment5.addOrReplaceChild("mid_tooth_r5", CubeListBuilder.create().texOffs(215, 152).addBox(-1.5F, -3.0F, -1.5F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0F, 8.5F, 0.0F, 0.0F, 0.0F, 0.0873F));

        PartDefinition mid_segment6 = middle_drill.addOrReplaceChild("mid_segment6", CubeListBuilder.create().texOffs(210, 9).addBox(2.0F, 0.0F, -3.0F, 8.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.0472F, 0.0F));

        PartDefinition mid_joining_segment_r6 = mid_segment6.addOrReplaceChild("mid_joining_segment_r6", CubeListBuilder.create().texOffs(74, 210).addBox(2.5F, -2.0F, -3.0F, 7.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.99F, 0.0F, 0.0F, -0.5236F, 0.0F));

        PartDefinition mid_tooth_r6 = mid_segment6.addOrReplaceChild("mid_tooth_r6", CubeListBuilder.create().texOffs(215, 152).addBox(-1.5F, -3.0F, -1.5F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0F, 8.5F, 0.0F, 0.0F, 0.0F, 0.0873F));

        PartDefinition lower_drill = drill_bit.addOrReplaceChild("lower_drill", CubeListBuilder.create(), PartPose.offset(0.0F, 5.0F, 0.0F));

        PartDefinition lower_segment = lower_drill.addOrReplaceChild("lower_segment", CubeListBuilder.create().texOffs(212, 45).addBox(0.0F, -6.0F, -2.0F, 5.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(184, 77).addBox(0.0F, -1.0F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 12.0F, 0.0F));

        PartDefinition lower_segment_b_r1 = lower_segment.addOrReplaceChild("lower_segment_b_r1", CubeListBuilder.create().texOffs(212, 55).addBox(0.0F, -2.0F, -2.0F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(206, 216).addBox(0.0F, 2.0F, -1.5F, 4.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.0F, -1.0472F, 0.0F));

        PartDefinition lower_segment2 = lower_drill.addOrReplaceChild("lower_segment2", CubeListBuilder.create().texOffs(212, 45).addBox(0.0F, -6.0F, -2.0F, 5.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(184, 77).addBox(0.0F, -1.0F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, 0.0F, -2.0944F, 0.0F));

        PartDefinition lower_segment_b_r2 = lower_segment2.addOrReplaceChild("lower_segment_b_r2", CubeListBuilder.create().texOffs(212, 55).addBox(0.0F, -2.0F, -2.0F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(206, 216).addBox(0.0F, 2.0F, -1.5F, 4.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.0F, -1.0472F, 0.0F));

        PartDefinition lower_segment3 = lower_drill.addOrReplaceChild("lower_segment3", CubeListBuilder.create().texOffs(212, 45).addBox(0.0F, -6.0F, -2.0F, 5.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(184, 77).addBox(0.0F, -1.0F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, 0.0F, 2.0944F, 0.0F));

        PartDefinition lower_segment_b_r3 = lower_segment3.addOrReplaceChild("lower_segment_b_r3", CubeListBuilder.create().texOffs(212, 55).addBox(0.0F, -2.0F, -2.0F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(206, 216).addBox(0.0F, 2.0F, -1.5F, 4.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.0F, -1.0472F, 0.0F));

        PartDefinition shaft = drill_bit.addOrReplaceChild("shaft", CubeListBuilder.create().texOffs(173, 100).addBox(-6.0F, -7.0F, -3.5F, 0.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition shaft_face_r1 = shaft.addOrReplaceChild("shaft_face_r1", CubeListBuilder.create().texOffs(173, 100).addBox(-6.0F, -7.0F, -3.5F, 0.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.0472F, 0.0F));

        PartDefinition shaft_face_r2 = shaft.addOrReplaceChild("shaft_face_r2", CubeListBuilder.create().texOffs(173, 100).addBox(-6.0F, -7.0F, -3.5F, 0.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -3.1416F, 0.0F, 3.1416F));

        PartDefinition shaft_face_r3 = shaft.addOrReplaceChild("shaft_face_r3", CubeListBuilder.create().texOffs(173, 100).addBox(-6.0F, -7.0F, -3.5F, 0.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -3.1416F, -1.0472F, 3.1416F));

        PartDefinition shaft_face_r4 = shaft.addOrReplaceChild("shaft_face_r4", CubeListBuilder.create().texOffs(173, 100).addBox(-6.0F, -7.0F, -3.5F, 0.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 2.0944F, 0.0F));

        PartDefinition shaft_face_r5 = shaft.addOrReplaceChild("shaft_face_r5", CubeListBuilder.create().texOffs(173, 100).addBox(-6.0F, -7.0F, -3.5F, 0.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.0472F, 0.0F));

        return LayerDefinition.create(meshdefinition, 256, 256);

    }

    private static final Vector3f ANIMATION_VECTOR_CACHE = new Vector3f();

    /**
     * Poses the model for a point in time within an animation. Resets all parts first, then applies the
     * animation and adds a persistent yaw offset to the drill bit, so successive animations chain without
     * the bit visibly snapping back to its rest rotation.
     */
    public void applyAnimation(AnimationDefinition animation, float elapsedSeconds, float spinOffsetRadians) {
        root.getAllParts().forEach(ModelPart::resetPose);
        KeyframeAnimations.animate(this, animation, (long) (elapsedSeconds * 1000.0F), 1.0F, ANIMATION_VECTOR_CACHE);
        drill_bit.yRot += spinOffsetRadians;
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Block entity model; posed via applyAnimation instead
    }

    public void render(PoseStack poseStack, VertexConsumer buffer, int light, int overlay) {
        drill_core.render(poseStack, buffer, light, overlay);
        pylon_support.render(poseStack, buffer, light, overlay);
        pylon_support2.render(poseStack, buffer, light, overlay);
        pylon_support3.render(poseStack, buffer, light, overlay);
        pylon_support4.render(poseStack, buffer, light, overlay);
        drill_bit.render(poseStack, buffer, light, overlay);
    }

    public static final AnimationDefinition drill_spin_loop = AnimationDefinition.Builder.withLength(0.75F).looping()
            .addAnimation("drill_bit", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.75F, KeyframeAnimations.degreeVec(0.0F, -360.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .build();

    public static final AnimationDefinition drill_extend = AnimationDefinition.Builder.withLength(2.25F)
            .addAnimation("drill_bit", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.5F, KeyframeAnimations.degreeVec(0.0F, -180.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.25F, KeyframeAnimations.degreeVec(0.0F, -540.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("drill_bit", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 6.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.5F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .build();

    public static final AnimationDefinition drill_retract = AnimationDefinition.Builder.withLength(2.25F)
            .addAnimation("drill_bit", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.7917F, KeyframeAnimations.degreeVec(0.0F, -360.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.5F, KeyframeAnimations.degreeVec(0.0F, -660.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.25F, KeyframeAnimations.degreeVec(0.0F, -720.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("drill_bit", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.75F, KeyframeAnimations.posVec(0.0F, 6.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .build();


    @Override
    public ModelPart root() {
        return root;
    }
}
