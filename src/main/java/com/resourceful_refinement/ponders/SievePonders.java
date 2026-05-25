package com.resourceful_refinement.ponders;

import com.resourceful_refinement.content.sieve.MechanicalFluidSieveBlockEntity;
import com.resourceful_refinement.registry.ModItems;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;


public class SievePonders {

    public static void mechanicalSieveScene(SceneBuilder builder, SceneBuildingUtil util) {

        //Build scene
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("fluid_sieving", "Fluid Sieving");

        scene.configureBasePlate(0,0,5);

        scene.rotateCameraY(0);
        scene.scaleSceneView(1.5f);

        BlockPos sievePos = util.grid().at(1, 2, 2);
        Selection sieveSegment = util.select().fromTo(sievePos, sievePos);

        BlockPos castingDepotPos = util.grid().at(1, 1, 1);
        Selection castingDepotSegment = util.select().fromTo(castingDepotPos, castingDepotPos.above());

        BlockPos pumpPos1 = util.grid().at(1, 1, 3);
        BlockPos pumpPos2 = util.grid().at(2, 3, 2);
        Selection pumpSegment1 = util.select().fromTo(pumpPos1, pumpPos1);
        Selection pumpSegment2 = util.select().fromTo(pumpPos2, pumpPos2);

        Selection tankSegment1 = util.select().fromTo(3,1,2, 3,3,2).add(
                util.select().fromTo(3,3,2, 1,3,2));
        Selection tankSegment2 = util.select().fromTo(1,1,2, 1,1,4).add(
                util.select().fromTo(1,1,4, 1,3,4));

        Selection axelSegment = util.select().fromTo(2,2,3, 2,4,3);


        // Show baseplate
        scene.showBasePlate();
        scene.idle(20);

        scene.world().showSection(sieveSegment, Direction.DOWN);


        // --- Page 1: Mechanical Sieve ---
        scene.addKeyframe();
        scene.overlay().showText(90)
                .text("resourceful_refinement.ponder.fluid_sieving.text_1")
                .pointAt(sieveSegment.getCenter());
        scene.idle(90);

        scene.idle(20);
        scene.world().showSection(tankSegment1, Direction.DOWN);
        scene.idle(10);

        // --- Page 2: Mechanical Sieve Inputs ---
        scene.addKeyframe();
        scene.overlay().showText(80)
                .text("resourceful_refinement.ponder.fluid_sieving.text_2")
                .pointAt(tankSegment1.getCenter());
        scene.idle(80);

        scene.idle(20);
        scene.world().showSection(tankSegment2, Direction.DOWN);
        scene.idle(10);

        scene.overlay().showText(80)
                .text("resourceful_refinement.ponder.fluid_sieving.text_3")
                .pointAt(tankSegment2.getCenter());
        scene.idle(80);


        // --- Page 3: Sieve Kinetics ---
        scene.idle(20);
        scene.world().showSection(axelSegment, Direction.DOWN);
        scene.idle(20);
        scene.addKeyframe();

        scene.world().setKineticSpeed(axelSegment, 24);
        scene.world().setKineticSpeed(pumpSegment1, 24);
        scene.world().setKineticSpeed(pumpSegment2, 24);
        scene.world().setKineticSpeed(sieveSegment, 24);

        scene.idle(10);
        scene.overlay().showText(120)
                .text("resourceful_refinement.ponder.fluid_sieving.text_4")
                .independent();
        scene.idle(130);


        // --- Page 4: Mechanical Sieve Outputs ---
        scene.addKeyframe();
        scene.overlay().showText(100)
                .text("resourceful_refinement.ponder.fluid_sieving.text_5")
                .pointAt(sievePos.above().getBottomCenter());

        scene.world().modifyBlockEntity(sievePos, MechanicalFluidSieveBlockEntity.class, blockEntity -> {
            blockEntity.getFiltering().setFilter(Direction.UP, ModItems.FERROUS_CRYSTAL.toStack());
        });
        scene.idle(120);

        scene.world().showSection(castingDepotSegment, Direction.DOWN);
        scene.idle(10);

        scene.overlay().showText(90)
                .text("resourceful_refinement.ponder.fluid_sieving.text_6")
                .pointAt(castingDepotPos.getCenter());

        scene.idle(40);
        scene.world().createItemOnBeltLike(castingDepotPos, Direction.UP, ModItems.FERROUS_CRYSTAL.toStack());
        scene.idle(60);

        scene.markAsFinished();

    }

    public static void mechanicalSieveStackScene(SceneBuilder builder, SceneBuildingUtil util){

        //Build scene
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("fluid_sieve_stacks", "Stacking Sieves");

        scene.configureBasePlate(0,0,5);

        scene.rotateCameraY(0);
        scene.scaleSceneView(1.5f);

        BlockPos sieveBasePos = util.grid().at(1, 2, 2);
        BlockPos sieveTopPos = util.grid().at(1, 5, 2);
        Selection sieveSegment = util.select().fromTo(sieveBasePos, sieveTopPos);

        BlockPos castingDepotPos = util.grid().at(1, 1, 1);
        Selection castingDepotSegment = util.select().fromTo(castingDepotPos, castingDepotPos.above());

        BlockPos pumpPos1 = util.grid().at(1, 1, 3);
        BlockPos pumpPos2 = util.grid().at(2, 6, 2);
        Selection pumpSegment1 = util.select().fromTo(pumpPos1, pumpPos1);
        Selection pumpSegment2 = util.select().fromTo(pumpPos2, pumpPos2);

        Selection tankSegment1 = util.select().fromTo(3,1,2, 3,6,2).add(
                util.select().fromTo(3,6,2, 1,6,2));
        Selection tankSegment2 = util.select().fromTo(1,1,2, 1,1,4).add(
                util.select().fromTo(1,1,4, 1,3,4));

        Selection axelSegment = util.select().fromTo(2,2,3, 2,6,3);


        // Show baseplate
        scene.showBasePlate();
        scene.idle(20);

        scene.world().showSection(sieveSegment, Direction.DOWN);


        // --- Page 1: Mechanical Sieve Stack ---
        scene.addKeyframe();
        scene.overlay().showText(100)
                .text("resourceful_refinement.ponder.fluid_sieve_stacks.text_1")
                .pointAt(sieveSegment.getCenter());
        scene.idle(120);

        scene.overlay().showText(90)
                .text("resourceful_refinement.ponder.fluid_sieve_stacks.text_2")
                .pointAt(sieveSegment.getCenter());
        scene.idle(110);



        // --- Page 2: Stack kinetics and outputs ---
        scene.addKeyframe();

        scene.world().showSection(axelSegment, Direction.DOWN);
        scene.idle(20);

        scene.world().setKineticSpeed(axelSegment, 24);
        scene.world().setKineticSpeed(pumpSegment1, 24);
        scene.world().setKineticSpeed(pumpSegment2, 24);
        scene.world().setKineticSpeed(sieveSegment, 24);
        scene.idle(20);

        scene.overlay().showText(100)
                .text("resourceful_refinement.ponder.fluid_sieve_stacks.text_3")
                .independent();
        scene.idle(120);

        scene.world().showSection(castingDepotSegment, Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(80)
                .text("resourceful_refinement.ponder.fluid_sieve_stacks.text_4")
                .pointAt(sieveBasePos.getCenter());
        scene.idle(100);

        scene.world().modifyBlockEntity(sieveTopPos, MechanicalFluidSieveBlockEntity.class, blockEntity -> {
            blockEntity.getFiltering().setFilter(Direction.UP, ModItems.FERROUS_CRYSTAL.toStack());
        });
        scene.idle(10);

        scene.overlay().showText(80)
                .text("resourceful_refinement.ponder.fluid_sieve_stacks.text_5")
                .pointAt(sieveTopPos.getCenter());
        scene.idle(100);

        scene.world().showSection(tankSegment1, Direction.DOWN);
        scene.world().showSection(tankSegment2, Direction.DOWN);
        scene.idle(20);


        // --- Page 3: Stack bonuses ---
        scene.addKeyframe();

        scene.overlay().showText(100)
                .text("resourceful_refinement.ponder.fluid_sieve_stacks.text_6")
                .independent();
        scene.idle(120);

        scene.overlay().showText(100)
                .text("resourceful_refinement.ponder.fluid_sieve_stacks.text_7")
                .independent();

        scene.idle(40);
        scene.world().createItemOnBeltLike(castingDepotPos, Direction.UP, ModItems.FERROUS_CRYSTAL.toStack());
        scene.idle(80);

        scene.markAsFinished();
    }

}
