package com.resourceful_refinement.ponders;

import com.resourceful_refinement.content.refinery.RefineryAccessPortBlock;
import com.resourceful_refinement.content.refinery.RefineryAccessPortBlockEntity;
import com.resourceful_refinement.content.sieve.MechanicalFluidSieveBlockEntity;
import com.resourceful_refinement.registry.ModBlocks;
import com.resourceful_refinement.registry.ModItems;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import static com.resourceful_refinement.content.refinery.BlenderBladeBlockEntity.computeTangentPush;


public class RefineryPonders {

    public static void refineryStructureScene(SceneBuilder builder, SceneBuildingUtil util) {

        //Build scene
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("refinery_basics", "Fluid Refinery");

        scene.configureBasePlate(0,0,7);

        scene.rotateCameraY(0);
        scene.scaleSceneView(1.25f);

        BlockPos accessPortPos = util.grid().at(3, 1, 2);
        Selection accessPortSegment = util.select().fromTo(accessPortPos, accessPortPos);

        BlockPos cogPos = util.grid().at(3, 4, 3);
        Selection cogSegment = util.select().fromTo(cogPos, cogPos);
        Selection kineticSegment = util.select().fromTo(3,2,3, 3, 3, 3);

        BlockPos pumpPos1 = util.grid().at(5, 4, 4);
        Selection pumpSegment1 = util.select().fromTo(pumpPos1, pumpPos1);

        Selection backPipeSegment = util.select().fromTo(6,4,4,4,4,4);
        Selection frontPipeSegment = util.select().fromTo(3,1,0,3,1,1);
        Selection beltSegment = util.select().fromTo(0,2,2,1,3,2);

        Selection baseLayerSegment = util.select().fromTo(2,1,2,4,1,4).substract(accessPortSegment);
        Selection middleLayerSegment = util.select().fromTo(2,2,2,4,2,4);
        Selection topLayerSegment = util.select().fromTo(2,3,2,4,3,4);

        Selection blazeSegment1 = util.select().fromTo(2,1,2,2,1,2);
        Selection blazeSegment2 = util.select().fromTo(4,1,2,4,1,2);
        Selection blazeSegment3 = util.select().fromTo(2,1,4,2,1,4);
        Selection blazeSegment4 = util.select().fromTo(4,1,4,4,1,4);
        Selection allBlazeSegments = blazeSegment1.copy().add(blazeSegment2).add(blazeSegment3).add(blazeSegment4);
        Selection casingSegment = baseLayerSegment.copy().substract(allBlazeSegments).substract(accessPortSegment);

        Selection blenderSegment1 = util.select().fromTo(3,2,3,3,2,3);
        Selection middleGlassSegment = middleLayerSegment.copy().substract(blenderSegment1);

        Selection itemVault1 = util.select().fromTo(2,3,2,2,3,2);
        Selection itemVault2 = util.select().fromTo(4,3,2,4,3,2);
        Selection fluidTank1 = util.select().fromTo(2,3,4,2,3,4);
        Selection fluidTank2 = util.select().fromTo(4,3,4,4,3,4);
        Selection blenderSegment2 = util.select().fromTo(3,3,3,3,3,3);
        Selection topGlassSegment = topLayerSegment.substract(itemVault1).substract(itemVault2).substract(fluidTank1).substract(fluidTank2).substract(blenderSegment2);

        Selection fullRefinerySegment = util.select().fromTo(2,1,2,4,3,4);
        Object layerEffectHighlight = new Object();

        // Show baseplate
        scene.showBasePlate();
        scene.idle(20);

        scene.world().modifyBlockEntity(accessPortPos, RefineryAccessPortBlockEntity.class, blockEntity -> {
            blockEntity.setFalseRenderingLevel(3);
        });
        scene.world().modifyBlock(accessPortPos, (blockState) -> blockState.setValue(RefineryAccessPortBlock.ASSEMBLED, true), false);
        scene.world().showSection(accessPortSegment, Direction.DOWN);
        scene.world().showSection(kineticSegment, Direction.DOWN);


        // --- Page 1: Fluid Refinery ---
        scene.addKeyframe();
        scene.overlay().showText(140)
                .text("resourceful_refinement.ponder.refinery_basics.text_1")
                .independent();
        scene.idle(160);

        scene.world().hideSection(accessPortSegment, Direction.UP);
        scene.world().hideSection(kineticSegment, Direction.UP);
        scene.idle(20);
        scene.world().modifyBlockEntity(accessPortPos, RefineryAccessPortBlockEntity.class, blockEntity -> {
            blockEntity.setFalseRenderingLevel(0);
        });
        scene.world().modifyBlock(accessPortPos, (blockState) -> blockState.setValue(RefineryAccessPortBlock.ASSEMBLED, false), false);
        scene.world().showSection(accessPortSegment, Direction.DOWN);
        scene.idle(20);


        // --- Page 2: Constructing the Base Layer ---
        scene.addKeyframe();
        scene.overlay().showText(90)
                .text("resourceful_refinement.ponder.refinery_basics.text_2")
                .pointAt(accessPortPos.getCenter());
        scene.idle(110);

        scene.idle(10);
        scene.overlay().showOutline(PonderPalette.RED, layerEffectHighlight, baseLayerSegment, 330);
        scene.idle(10);

        scene.addKeyframe();
        scene.overlay().showText(90)
                .text("resourceful_refinement.ponder.refinery_basics.text_3")
                .pointAt(accessPortPos.getCenter());
        scene.idle(110);

        scene.world().showSection(allBlazeSegments, Direction.DOWN);

        scene.overlay().showText(190)
                .text("resourceful_refinement.ponder.refinery_basics.text_4")
                .independent();
        scene.idle(100);

        scene.world().showSection(casingSegment, Direction.DOWN);

        scene.overlay().showText(90)
                .text("resourceful_refinement.ponder.refinery_basics.text_5")
                .independent(40);
        scene.idle(110);


        // --- Page 3: Constructing the Middle Layer ---
        scene.addKeyframe();

        scene.overlay().showOutline(PonderPalette.RED, layerEffectHighlight, middleLayerSegment, 140);
        scene.idle(15);
        scene.world().showSection(blenderSegment1, Direction.DOWN);
        scene.overlay().showText(130)
                .text("resourceful_refinement.ponder.refinery_basics.text_6")
                .independent();
        scene.idle(70);

        scene.world().showSection(middleGlassSegment, Direction.DOWN);
        scene.overlay().showText(60)
                .text("resourceful_refinement.ponder.refinery_basics.text_7")
                .independent(48);
        scene.idle(70);


        // --- Page 4: Constructing the Top Layer ---
        scene.addKeyframe();

        scene.overlay().showOutline(PonderPalette.RED, layerEffectHighlight, topLayerSegment, 280);
        scene.idle(15);
        scene.world().showSection(blenderSegment2, Direction.DOWN);
        scene.overlay().showText(200)
                .text("resourceful_refinement.ponder.refinery_basics.text_8")
                .independent();
        scene.idle(70);

        scene.world().showSection(fluidTank1, Direction.DOWN);
        scene.world().showSection(fluidTank2, Direction.DOWN);
        scene.overlay().showText(130)
                .text("resourceful_refinement.ponder.refinery_basics.text_9")
                .independent(36);
        scene.idle(70);

        scene.world().showSection(itemVault1, Direction.DOWN);
        scene.world().showSection(itemVault2, Direction.DOWN);
        scene.overlay().showText(60)
                .text("resourceful_refinement.ponder.refinery_basics.text_10")
                .independent(64);
        scene.idle(70);

        scene.world().showSection(topGlassSegment, Direction.DOWN);
        scene.overlay().showText(60)
                .text("resourceful_refinement.ponder.refinery_basics.text_11")
                .independent();
        scene.idle(90);


        // --- Page 5: Completing Assembly ---
        scene.addKeyframe();

        scene.overlay().showControls(accessPortPos.getCenter(), Pointing.RIGHT, 60);
        scene.overlay().showText(80)
                .text("resourceful_refinement.ponder.refinery_basics.text_12")
                .independent();
        scene.idle(90);

        scene.world().replaceBlocks(fullRefinerySegment.copy().substract(accessPortSegment).substract(kineticSegment), Blocks.AIR.defaultBlockState(), false);
        scene.world().modifyBlockEntity(accessPortPos, RefineryAccessPortBlockEntity.class, blockEntity -> {
            blockEntity.setFalseRenderingLevel(3);
        });
        scene.world().modifyBlock(accessPortPos, (blockState) -> blockState.setValue(RefineryAccessPortBlock.ASSEMBLED, true), false);
        scene.idle(20);

        scene.overlay().showText(80)
                .text("resourceful_refinement.ponder.refinery_basics.text_13")
                .independent();
        scene.idle(100);

        scene.markAsFinished();
    }

    public static void refineryCraftingScene(SceneBuilder builder, SceneBuildingUtil util) {

        //Build scene
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("refinery_crafting", "Fluid Refinery");

        scene.configureBasePlate(0,0,7);

        scene.rotateCameraY(0);
        scene.scaleSceneView(1.25f);

        BlockPos accessPortPos = util.grid().at(3, 1, 2);
        Selection accessPortSegment = util.select().fromTo(accessPortPos, accessPortPos);

        BlockPos cogPos = util.grid().at(3, 4, 3);
        Selection cogSegment = util.select().fromTo(cogPos, cogPos);
        Selection kineticSegment = util.select().fromTo(3,4,3, 3, 3, 3);
        Selection sideFunnelSegment = util.select().fromTo(2, 1, 1, 2, 1, 1);

        BlockPos pumpPos1 = util.grid().at(5, 4, 4);
        Selection pumpSegment1 = util.select().fromTo(pumpPos1, pumpPos1);

        Selection backPipeSegment = util.select().fromTo(6,4,4,4,4,4);
        Selection frontPipeSegment = util.select().fromTo(3,1,0,3,1,1);
        Selection beltSegment = util.select().fromTo(0,2,2,1,3,2);

        Selection baseLayerSegment = util.select().fromTo(2,1,2,4,1,4).substract(accessPortSegment);
        Selection middleLayerSegment = util.select().fromTo(2,2,2,4,2,4);
        Selection topLayerSegment = util.select().fromTo(2,3,2,4,3,4);

        Selection blazeSegment1 = util.select().fromTo(2,1,2,2,1,2);
        Selection blazeSegment2 = util.select().fromTo(4,1,2,4,1,2);
        Selection blazeSegment3 = util.select().fromTo(2,1,4,2,1,4);
        Selection blazeSegment4 = util.select().fromTo(4,1,4,4,1,4);
        Selection allBlazeSegments = blazeSegment1.copy().add(blazeSegment2).add(blazeSegment3).add(blazeSegment4);
        Selection casingSegment = baseLayerSegment.copy().substract(allBlazeSegments).substract(accessPortSegment);

        Selection blenderSegment1 = util.select().fromTo(3,2,3,3,2,3);
        Selection middleGlassSegment = middleLayerSegment.copy().substract(blenderSegment1);

        Selection itemVault1 = util.select().fromTo(2,3,2,2,3,2);
        Selection itemVault2 = util.select().fromTo(4,3,2,4,3,2);
        Selection fluidTank1 = util.select().fromTo(2,3,4,2,3,4);
        Selection fluidTank2 = util.select().fromTo(4,3,4,4,3,4);
        Selection blenderSegment2 = util.select().fromTo(3,3,3,3,3,3);
        Selection topGlassSegment = topLayerSegment.copy().substract(itemVault1).substract(itemVault2).substract(fluidTank1).substract(fluidTank2).substract(blenderSegment2);

        Selection fullRefinerySegment = util.select().fromTo(2,1,2,4,3,4);


        // Show baseplate
        scene.showBasePlate();
        scene.idle(20);

        scene.world().modifyBlockEntity(accessPortPos, RefineryAccessPortBlockEntity.class, blockEntity -> {
            blockEntity.setFalseRenderingLevel(3);
        });
        scene.world().modifyBlock(accessPortPos, (blockState) -> blockState.setValue(RefineryAccessPortBlock.ASSEMBLED, true), false);
        scene.world().showSection(accessPortSegment, Direction.DOWN);
        scene.world().showSection(kineticSegment, Direction.DOWN);
        scene.idle(20);

        // --- Page 1: Fluid Refinery Summary ---
        scene.addKeyframe();
        scene.overlay().showText(90)
                .text("resourceful_refinement.ponder.refinery_crafting.text_1")
                .pointAt(accessPortPos.above().getCenter());
        scene.idle(110);

        scene.overlay().showText(100)
                .text("resourceful_refinement.ponder.refinery_crafting.text_2")
                .independent();
        scene.idle(120);

        scene.overlay().showText(90)
                .text("resourceful_refinement.ponder.refinery_crafting.text_3")
                .independent();
        scene.idle(130);


        // --- Page 2: Fluid Refinery Inputs ---
        scene.addKeyframe();
        scene.overlay().showText(80)
                .text("resourceful_refinement.ponder.refinery_crafting.text_4")
                .independent();
        scene.idle(100);

        scene.idle(10);
        scene.world().showSection(backPipeSegment, Direction.DOWN);
        scene.idle(10);

        scene.overlay().showText(90)
                .text("resourceful_refinement.ponder.refinery_crafting.text_5")
                .pointAt(backPipeSegment.getCenter().add(1,1,0.5));
        scene.idle(110);

        scene.idle(10);
        scene.world().showSection(beltSegment, Direction.DOWN);
        scene.idle(10);

        scene.overlay().showText(100)
                .text("resourceful_refinement.ponder.refinery_crafting.text_6")
                .pointAt(beltSegment.getCenter());
        scene.idle(120);

        scene.idle(10);
        scene.world().showSection(cogSegment, Direction.DOWN);
        scene.idle(10);
        scene.world().setKineticSpeed(kineticSegment, 32);
        scene.world().setKineticSpeed(cogSegment, 32);

        scene.overlay().showText(100)
                .text("resourceful_refinement.ponder.refinery_crafting.text_7")
                .pointAt(kineticSegment.getCenter());
        scene.idle(120);


        // --- Page 3: Heating the Refinery ---
        scene.addKeyframe();

        scene.overlay().showText(340)
                .text("resourceful_refinement.ponder.refinery_crafting.text_8")
                .independent(-8);
        scene.idle(100);

        scene.overlay().showControls(accessPortPos.getCenter().add(0,0.5,-0.5), Pointing.DOWN, 120)
                .withItem(Items.COAL.getDefaultInstance());
        scene.overlay().showText(240)
                .text("resourceful_refinement.ponder.refinery_basics.text_9")
                .independent(24);
        scene.idle(140);

        scene.overlay().showControls(accessPortPos.getCenter().add(0,0.5,-0.5), Pointing.DOWN, 100)
                .withItem(AllItems.BLAZE_CAKE.asStack());
        scene.overlay().showText(100)
                .text("resourceful_refinement.ponder.refinery_basics.text_10")
                .independent(72);
        scene.idle(120);

        scene.world().setBlock(accessPortPos.offset(-1, 0, -1), AllBlocks.ANDESITE_BELT_FUNNEL.getDefaultState(), true);
        scene.world().showSection(sideFunnelSegment, Direction.DOWN);

        scene.idle(10);
        scene.overlay().showControls(accessPortPos.getCenter().add(-1.25,0,-0.25), Pointing.LEFT, 100)
                .withItem(Items.COAL.getDefaultInstance());
        scene.overlay().showText(110)
                .text("resourceful_refinement.ponder.refinery_basics.text_11")
                .independent();
        scene.idle(130);


        // --- Page 4: Extracting Outputs ---
        scene.world().showSection(frontPipeSegment, Direction.DOWN);
        scene.idle(10);

        scene.addKeyframe();

        scene.overlay().showText(120)
                .text("resourceful_refinement.ponder.refinery_basics.text_12")
                .pointAt(frontPipeSegment.getCenter());
        scene.idle(140);

        scene.markAsFinished();
    }

    public static void refineryStackingScene(SceneBuilder builder, SceneBuildingUtil util) {

        //Build scene
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("refinery_stacking", "Fluid Refinery");

        scene.configureBasePlate(0,0,7);

        scene.rotateCameraY(0);
        scene.scaleSceneView(1.25f);

        BlockPos accessPortPos = util.grid().at(3, 1, 2);
        Selection accessPortSegment = util.select().fromTo(accessPortPos, accessPortPos);

        BlockPos cogPos = util.grid().at(3, 9, 3);
        Selection cogSegment = util.select().fromTo(cogPos, cogPos);
        Selection lowKineticSegment = util.select().fromTo(3,2,3, 3, 3, 3);
        Selection kineticSegment = util.select().fromTo(3,2,3, 3, 8, 3);
        Selection sideFunnelSegment = util.select().fromTo(1, 1, 2, 1, 1, 2);

        BlockPos pumpPos1 = util.grid().at(5, 4, 4);
        Selection pumpSegment1 = util.select().fromTo(pumpPos1, pumpPos1);

        Selection frontPipeSegment = util.select().fromTo(3,1,0,3,1,1);

        Selection baseLayerSegment = util.select().fromTo(2,1,2,4,1,4).substract(accessPortSegment);
        Selection middleLayerSegment1 = util.select().fromTo(2,2,2,4,2,4);
        Selection middleLayerSegment2 = util.select().fromTo(2,3,2,4,3,4);
        Selection middleLayerSegment3 = util.select().fromTo(2,4,2,4,4,4);
        Selection middleLayerSegment4 = util.select().fromTo(2,5,2,4,5,4);
        Selection middleLayerSegment5 = util.select().fromTo(2,6,2,4,6,4);
        Selection middleLayerSegment6 = util.select().fromTo(2,7,2,4,7,4);
        Selection topLayerSegment = util.select().fromTo(2,8,2,4,8,4);

        Selection rulerSegment = util.select().fromTo(5,1,2,5,7,2);
        Selection blenderSegment = util.select().fromTo(3,2,3,3,5,3);
        Selection fullRefinerySegment = util.select().fromTo(2,1,2,4,8,4).substract(accessPortSegment).substract(kineticSegment);


        // Show baseplate
        scene.showBasePlate();
        scene.idle(20);

        scene.world().modifyBlockEntity(accessPortPos, RefineryAccessPortBlockEntity.class, blockEntity -> {
            blockEntity.setFalseRenderingLevel(3);
        });
        scene.world().modifyBlock(accessPortPos, (blockState) -> blockState.setValue(RefineryAccessPortBlock.ASSEMBLED, true), false);
        scene.world().setKineticSpeed(lowKineticSegment, 16);

        scene.world().showSection(accessPortSegment, Direction.DOWN);
        scene.world().showSection(lowKineticSegment, Direction.DOWN);
        scene.idle(20);

        // --- Page 1: Fluid Refinery Limits ---
        scene.addKeyframe();
        scene.overlay().showText(120)
                .text("resourceful_refinement.ponder.refinery_stacking.text_1")
                .pointAt(accessPortPos.above().getCenter());
        scene.idle(140);

        scene.overlay().showText(100)
                .text("resourceful_refinement.ponder.refinery_stacking.text_2")
                .independent();
        scene.idle(100);

        scene.world().setKineticSpeed(lowKineticSegment, 0);
        scene.idle(20);
        scene.world().hideSection(accessPortSegment, Direction.UP);
        scene.world().hideSection(lowKineticSegment, Direction.UP);
        scene.idle(20);
        scene.world().modifyBlockEntity(accessPortPos, RefineryAccessPortBlockEntity.class, blockEntity -> {
            blockEntity.setFalseRenderingLevel(0);
        });
        scene.world().modifyBlock(accessPortPos, (blockState) -> blockState.setValue(RefineryAccessPortBlock.ASSEMBLED, false), false);


        // --- Page 2: Expanding the Refinery ---
        scene.addKeyframe();

        scene.world().showSection(accessPortSegment, Direction.DOWN);
        scene.world().showSection(baseLayerSegment, Direction.DOWN);
        scene.world().showSection(middleLayerSegment1, Direction.DOWN);
        //scene.world().showSection(topLayerSegment, Direction.DOWN);
        ElementLink<WorldSectionElement> topSegmentLink = scene.world().showIndependentSection(topLayerSegment, Direction.DOWN);
        scene.world().moveSection(topSegmentLink,  new Vec3(0,-5,0), 0);
        scene.idle(20);

        scene.overlay().showText(90)
                .text("resourceful_refinement.ponder.refinery_stacking.text_3")
                .independent();
        scene.idle(110);


        // --- Page 3: Fluid Refinery Inputs ---
        scene.addKeyframe();
        scene.world().hideIndependentSection(topSegmentLink, Direction.UP);
        scene.idle(20);

        scene.world().showSection(rulerSegment, Direction.DOWN);
        ElementLink<WorldSectionElement> middleSegmentLink = scene.world().makeSectionIndependent(middleLayerSegment1);
        scene.world().moveSection(middleSegmentLink,  new Vec3(0,0.5,0), 15);
        scene.idle(20);

        scene.overlay().showText(120)
                .text("resourceful_refinement.ponder.refinery_stacking.text_4")
                .pointAt(middleLayerSegment2.getCenter());
        scene.idle(130);

        scene.world().moveSection(middleSegmentLink,  new Vec3(0,-0.5,0), 10);
        scene.idle(10);
        scene.world().showSection(middleLayerSegment2, Direction.DOWN);
        scene.world().showSection(middleLayerSegment3, Direction.DOWN);
        scene.world().showSection(middleLayerSegment4, Direction.DOWN);
        scene.world().showSection(middleLayerSegment5, Direction.DOWN);
        scene.world().showSection(middleLayerSegment6, Direction.DOWN);

        scene.world().moveSection(topSegmentLink,  new Vec3(0,5,0), 0);
        scene.world().showSection(topLayerSegment, Direction.DOWN);

        scene.overlay().showText(120)
                .text("resourceful_refinement.ponder.refinery_stacking.text_5")
                .pointAt(middleLayerSegment3.getCenter());
        scene.idle(140);

        scene.world().replaceBlocks(fullRefinerySegment, Blocks.AIR.defaultBlockState(), false);
        scene.world().modifyBlockEntity(accessPortPos, RefineryAccessPortBlockEntity.class, blockEntity -> {
            blockEntity.setFalseRenderingLevel(8);
        });
        scene.world().modifyBlock(accessPortPos, (blockState) -> blockState.setValue(RefineryAccessPortBlock.ASSEMBLED, true), false);

        scene.idle(10);
        scene.world().showSection(cogSegment, Direction.DOWN);
        scene.idle(10);
        scene.world().setKineticSpeed(kineticSegment, 32);
        scene.world().setKineticSpeed(cogSegment, 32);

        scene.overlay().showText(110)
                .text("resourceful_refinement.ponder.refinery_stacking.text_6")
                .independent();
        scene.idle(130);


        // --- Page 4: Extra fluids ---
        scene.addKeyframe();

        scene.overlay().showText(90)
                .text("resourceful_refinement.ponder.refinery_basics.text_9")
                .pointAt(middleLayerSegment3.getCenter());
        scene.idle(110);

        scene.markAsFinished();
    }

    public static void blenderBladesScene(SceneBuilder builder, SceneBuildingUtil util) {

        //Build scene
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("blender_blades", "Blender Blades");

        scene.configureBasePlate(0,0,7);

        scene.rotateCameraY(0);
        scene.scaleSceneView(1.25f);

        BlockPos bottomBlenderBlade = util.grid().at(3, 1, 3);
        Selection bottomBlenderBladeSegment = util.select().fromTo(bottomBlenderBlade, bottomBlenderBlade);
        BlockPos middleBlenderBlade = util.grid().at(3, 2, 3);
        Selection middleBlenderBladeSegment = util.select().fromTo(middleBlenderBlade, middleBlenderBlade);
        BlockPos topBlenderBlade = util.grid().at(3, 3, 3);
        Selection topBlenderBladeSegment = util.select().fromTo(topBlenderBlade, topBlenderBlade);
        BlockPos cogPos = util.grid().at(3, 4, 3);
        Selection cogSegment = util.select().fromTo(cogPos, cogPos);
        Selection kineticSegment = util.select().fromTo(bottomBlenderBlade, cogPos);

        BlockPos spawnPos = util.grid().at(2, 4, 3);

        // Show baseplate
        scene.showBasePlate();
        scene.idle(20);

        scene.idle(20);

        // --- Page 1: Blender Blades ---
        scene.addKeyframe();

        scene.world().setBlocks(bottomBlenderBladeSegment, AllBlocks.SHAFT.getDefaultState(), false);
        scene.world().setBlocks(topBlenderBladeSegment, AllBlocks.SHAFT.getDefaultState(), false);
        scene.world().showSection(bottomBlenderBladeSegment, Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(middleBlenderBladeSegment, Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(topBlenderBladeSegment, Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(cogSegment, Direction.DOWN);
        scene.idle(5);

        scene.overlay().showText(120)
                .text("resourceful_refinement.ponder.blender_blades.text_1")
                .pointAt(bottomBlenderBlade.above().getCenter());
        scene.idle(50);
        scene.world().setKineticSpeed(kineticSegment, 32);
        scene.idle(50);

        scene.world().setKineticSpeed(kineticSegment, 0);
        scene.idle(10);

        scene.overlay().showText(120)
                .text("resourceful_refinement.ponder.blender_blades.text_2")
                .independent();
        scene.idle(140);

        scene.overlay().showText(80)
                .text("resourceful_refinement.ponder.blender_blades.text_3")
                .independent();
        scene.idle(15);

        scene.world().hideSection(cogSegment, Direction.UP);
        scene.idle(5);
        scene.world().hideSection(topBlenderBladeSegment, Direction.UP);
        scene.idle(5);
        scene.world().hideSection(middleBlenderBladeSegment, Direction.UP);
        scene.idle(5);
        scene.world().hideSection(bottomBlenderBladeSegment, Direction.UP);
        scene.idle(5);

        scene.idle(35);

        scene.world().setBlocks(bottomBlenderBladeSegment, ModBlocks.BLENDER_BLADE.get().defaultBlockState(), false);
        scene.world().setBlocks(topBlenderBladeSegment, ModBlocks.BLENDER_BLADE.get().defaultBlockState(), false);
        scene.world().showSection(bottomBlenderBladeSegment, Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(middleBlenderBladeSegment, Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(topBlenderBladeSegment, Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(cogSegment, Direction.DOWN);
        scene.idle(5);


        // --- Page 2: Moving Entities ---
        scene.addKeyframe();
        scene.idle(20);

        scene.world().setKineticSpeed(kineticSegment, 24);
        scene.overlay().showText(110)
                .text("resourceful_refinement.ponder.blender_blades.text_4")
                .pointAt(middleBlenderBladeSegment.getCenter());
        scene.idle(20);

        var armorLink = scene.world().createEntity(level -> {
            ArmorStand stand = new ArmorStand(EntityType.ARMOR_STAND, level);

            // Convert (3, 1, 2) to absolute world coordinates, offsetting by 0.5 for perfect block centering
            stand.setPos(3, 1, 2.5);

            // Optional: customize the armor stand (e.g., hide base plate, show arms)
            stand.setNoBasePlate(false);
            stand.setShowArms(true);

            return stand;
        });

        for (int i = 0; i < 90; i++)
        {
            scene.world().modifyEntity(armorLink, entity -> {
                Vec3 push = computeTangentPush(entity.position(), new Vec3(3.5,1,3.5), Direction.Axis.Y, 42, 1);
                entity.setPos(entity.position().x + push.x, entity.position().y + push.y, entity.position().z + push.z);
            });
            scene.idle(1);
        }

        scene.effects().rotationSpeedIndicator(cogPos);
        scene.world().setKineticSpeed(kineticSegment, 72);
        scene.overlay().showText(90)
                .text("resourceful_refinement.ponder.blender_blades.text_5")
                .pointAt(middleBlenderBladeSegment.getCenter());

        for (int i = 0; i < 90; i++)
        {
            scene.world().modifyEntity(armorLink, entity -> {
                Vec3 push = computeTangentPush(entity.position(), new Vec3(3.5,1,3.5), Direction.Axis.Y, 76, 1);
                entity.setPos(entity.position().x + push.x, entity.position().y + push.y, entity.position().z + push.z);
            });
            scene.idle(1);
        }


        // Remove armor stand
        scene.world().modifyEntity(armorLink, Entity::discard);
        scene.idle(20);

        scene.overlay().showText(120)
                .text("resourceful_refinement.ponder.blender_blades.text_6")
                .pointAt(middleBlenderBladeSegment.getCenter());

        var creeperLink = scene.world().createEntity(level -> {
            Creeper mob = new Creeper(EntityType.CREEPER, level);

            // Convert (3, 1, 2) to absolute world coordinates, offsetting by 0.5 for perfect block centering
            mob.setPos(3, 1, 2.5);

            return mob;
        });

        for (int i = 0; i < 140; i++)
        {
            scene.world().modifyEntity(creeperLink, entity -> {
                Vec3 push = computeTangentPush(entity.position(), new Vec3(3.5,1,3.5), Direction.Axis.Y, 68, 1);
                entity.setPos(entity.position().x + push.x, entity.position().y + push.y, entity.position().z + push.z);
            });
            scene.idle(1);
        }

        // Remove creeper
        scene.world().modifyEntity(creeperLink, Entity::discard);
        scene.idle(10);
        scene.world().setKineticSpeed(kineticSegment, 0);
        scene.idle(10);

        scene.markAsFinished();
    }
}
