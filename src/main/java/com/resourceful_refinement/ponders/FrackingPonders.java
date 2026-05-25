package com.resourceful_refinement.ponders;

import com.resourceful_refinement.content.fracking_pump.FrackingPumpOutletBlock;
import com.resourceful_refinement.content.fracking_pump.FrackingPumpOutletBlockEntity;
import com.resourceful_refinement.content.refinery.RefineryAccessPortBlock;
import com.resourceful_refinement.content.refinery.RefineryAccessPortBlockEntity;
import com.resourceful_refinement.registry.ModBlocks;
import com.resourceful_refinement.registry.ModFluids;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;


public class FrackingPonders {

    public static void frackingGeyserScene(SceneBuilder builder, SceneBuildingUtil util) {

        //Build scene
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("geyser_fracking", "Fracking Pylons");

        scene.configureBasePlate(1,0,10);

        scene.rotateCameraY(0);
        scene.scaleSceneView(1.25f);

        BlockPos geyserPos = util.grid().at(6, 1, 5);
        BlockPos outletBlock = util.grid().at(6, 2, 5);
        BlockPos andesiteCapBlock = util.grid().at(6, 7, 5);

        Selection geyserSegment = util.select().fromTo(geyserPos,geyserPos);
        Selection outletSegment = util.select().fromTo(outletBlock,outletBlock);
        Selection casingsSegment = util.select().fromTo(6, 3, 5,6, 4, 5);
        Selection girderSegment = util.select().fromTo(6, 5, 5,6, 6, 5);
        Selection andesiteCapSegment = util.select().fromTo(andesiteCapBlock,andesiteCapBlock);
        Selection counterweightSegment = util.select().fromTo(5,6,4,7,6,6);
        Selection geyserSpoutSegment = util.select().fromTo(5,2,4,7,2,6);

        Selection kineticSegment = util.select().fromTo(6,2,0,6,4,6);
        Selection inPipeSegment = util.select().fromTo(11,1,5,10,2,5).add(util.select().fromTo(10,2,5,7,2,5));
        Selection outPipeSegment = util.select().fromTo(5,2,5,1,2,5).add(util.select().fromTo(1,1,5,0,1,5));
        Selection geyserPoolSegment = util.select().fromTo(2,1,1,9,1,8).substract(geyserSegment);

        Object layerEffectHighlight = new Object();

        // Show baseplate
        scene.showBasePlate();
        scene.world().showSection(geyserPoolSegment, Direction.DOWN);
        scene.world().showSection(geyserSegment, Direction.DOWN);
        scene.idle(20);

        // --- Page 1: Geysers ---
        scene.addKeyframe();
        scene.overlay().showText(140)
                .text("resourceful_refinement.ponder.geyser_fracking.text_1")
                .independent();
        scene.idle(160);

        scene.overlay().showText(120)
                .text("resourceful_refinement.ponder.geyser_fracking.text_2")
                .pointAt(geyserPos.getCenter());
        scene.idle(140);


        // --- Page 2: Geysers ---
        scene.addKeyframe();

        scene.world().setBlocks(geyserSpoutSegment, Blocks.AIR.defaultBlockState(), false);
        scene.world().setBlock(outletBlock, ModFluids.MOLTEN_CRIMSITE.block.get().defaultBlockState(), false);
        scene.world().showSection(geyserSpoutSegment, Direction.DOWN);
        scene.idle(40);

        scene.overlay().showText(110)
                .text("resourceful_refinement.ponder.geyser_fracking.text_3")
                .pointAt(geyserPos.getCenter());
        scene.idle(110);

        scene.world().hideSection(geyserSpoutSegment, Direction.UP);
        scene.idle(20);
        scene.world().setBlock(outletBlock, ModBlocks.FRACKING_PUMP_OUTLET.get().defaultBlockState(), false);
        scene.world().modifyBlockEntity(outletBlock, FrackingPumpOutletBlockEntity.class, blockEntity -> {
            blockEntity.setFalseRenderingLevel(2);
        });
        scene.idle(30);
        scene.world().showSection(outletSegment, Direction.DOWN);
        scene.idle(20);
        scene.world().setKineticSpeed(outletSegment, 96);

        scene.overlay().showText(110)
                .text("resourceful_refinement.ponder.geyser_fracking.text_4")
                .pointAt(outletBlock.getCenter());
        scene.idle(130);


        scene.markAsFinished();
    }

    public static void frackingBuildScene(SceneBuilder builder, SceneBuildingUtil util) {

        //Build scene
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("fracking_assembly", "Constructing a Fracking Pylon");

        scene.configureBasePlate(1,0,10);

        scene.rotateCameraY(0);
        scene.scaleSceneView(0.725f);
        scene.setSceneOffsetY(-3);

        BlockPos geyserPos = util.grid().at(6, 1, 5);
        BlockPos outletBlock = util.grid().at(6, 2, 5);
        BlockPos andesiteCapBlock = util.grid().at(6, 9, 5);

        Selection geyserSegment = util.select().fromTo(geyserPos,geyserPos);
        Selection outletSegment = util.select().fromTo(outletBlock,outletBlock);
        Selection casingsSegment = util.select().fromTo(6, 3, 5,6, 4, 5);
        Selection girderSegment = util.select().fromTo(6, 5, 5,6, 8, 5);
        Selection fullGirderSegment = util.select().fromTo(6, 5, 5,6, 12, 5);
        Selection andesiteCapSegment = util.select().fromTo(andesiteCapBlock,andesiteCapBlock);
        Selection geyserSpoutSegment = util.select().fromTo(5,2,4,7,2,6);

        Selection counterweightSegment1 = util.select().fromTo(5,7,4,7,7,6);
        Selection counterweightSegment2 = util.select().fromTo(5,8,4,7,8,6);
        Selection counterweightSegment = util.select().fromTo(5,7,4,7,8,6);

        Selection kineticSegment = util.select().fromTo(6,2,0,6,4,6);
        Selection inPipeSegment = util.select().fromTo(11,1,5,10,2,5).add(util.select().fromTo(10,2,5,7,2,5));
        Selection outPipeSegment = util.select().fromTo(5,2,5,1,2,5).add(util.select().fromTo(1,1,5,0,1,5));
        Selection geyserPoolSegment = util.select().fromTo(2,1,1,9,1,8).substract(geyserSegment);

        Object layerEffectHighlight = new Object();


        // Show baseplate
        scene.showBasePlate();
        scene.world().showSection(geyserPoolSegment, Direction.DOWN);
        scene.world().showSection(geyserSegment, Direction.DOWN);
        scene.idle(20);

        scene.world().setKineticSpeed(kineticSegment, 0);
        scene.world().setKineticSpeed(outletSegment, 0);


        // --- Page 1: Base Assembly ---
        scene.addKeyframe();
        scene.world().showSection(outletSegment, Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(120)
                .text("resourceful_refinement.ponder.fracking_assembly.text_1")
                .pointAt(outletBlock.getCenter());
        scene.idle(140);

        scene.overlay().showOutline(PonderPalette.RED, layerEffectHighlight, casingsSegment.copy(), 140);
        scene.idle(20);

        scene.world().showSection(casingsSegment, Direction.DOWN);
        scene.overlay().showText(120)
                .text("resourceful_refinement.ponder.fracking_assembly.text_2")
                .pointAt(casingsSegment.getCenter());
        scene.idle(140);


        // --- Page 2: Pole  ---
        scene.addKeyframe();
        scene.overlay().showOutline(PonderPalette.RED, layerEffectHighlight, fullGirderSegment.copy(), 270);
        scene.idle(20);

        scene.overlay().showText(220)
                .text("resourceful_refinement.ponder.fracking_assembly.text_3")
                .independent();
        scene.idle(120);

        scene.overlay().showText(100)
                .text("resourceful_refinement.ponder.fracking_assembly.text_4")
                .independent(32);
        scene.idle(120);

        scene.world().showSection(girderSegment, Direction.DOWN);
        scene.idle(20);


        // --- Page 3: Counterweight  ---
        scene.addKeyframe();
        scene.overlay().showOutline(PonderPalette.RED, layerEffectHighlight, counterweightSegment.copy(), 320);
        scene.idle(20);

        scene.overlay().showText(140)
                .text("resourceful_refinement.ponder.fracking_assembly.text_5")
                .pointAt(counterweightSegment2.getCenter());
        scene.idle(80);
        scene.world().showSection(counterweightSegment2, Direction.DOWN);
        scene.idle(80);

        scene.world().showSection(counterweightSegment1, Direction.DOWN);
        scene.overlay().showText(140)
                .text("resourceful_refinement.ponder.fracking_assembly.text_6")
                .pointAt(counterweightSegment1.getCenter());
        scene.idle(160);

        scene.world().showSection(andesiteCapSegment, Direction.DOWN);
        scene.overlay().showText(100)
                .text("resourceful_refinement.ponder.fracking_assembly.text_7")
                .pointAt(andesiteCapSegment.getCenter());
        scene.idle(120);


        // --- Page 4: Assembly  ---
        scene.addKeyframe();

        scene.overlay().showControls(outletSegment.getCenter(), Pointing.RIGHT, 80);
        scene.overlay().showText(100)
                .text("resourceful_refinement.ponder.fracking_assembly.text_8")
                .pointAt(outletSegment.getCenter());
        scene.idle(120);

        scene.world().setBlocks(casingsSegment.add(girderSegment).add(counterweightSegment).add(andesiteCapSegment), Blocks.AIR.defaultBlockState(), false);
        scene.world().modifyBlockEntity(outletBlock, FrackingPumpOutletBlockEntity.class, blockEntity -> {
            blockEntity.setFalseRenderingLevel(4);
        });

        scene.overlay().showText(100)
                .text("resourceful_refinement.ponder.fracking_assembly.text_9")
                .independent();
        scene.idle(120);

        scene.markAsFinished();
    }

    public static void frackingCraftingScene(SceneBuilder builder, SceneBuildingUtil util) {

        //Build scene
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("fracking_crafting", "Constructing a Fracking Pylon");

        scene.configureBasePlate(1,0,10);

        scene.rotateCameraY(0);
        scene.scaleSceneView(0.725f);
        scene.setSceneOffsetY(-3);

        BlockPos geyserPos = util.grid().at(6, 1, 5);
        BlockPos outletBlock = util.grid().at(6, 2, 5);
        BlockPos andesiteCapBlock = util.grid().at(6, 9, 5);

        Selection geyserSegment = util.select().fromTo(geyserPos,geyserPos);
        Selection outletSegment = util.select().fromTo(outletBlock,outletBlock);
        Selection casingsSegment = util.select().fromTo(6, 3, 5,6, 4, 5);
        Selection girderSegment = util.select().fromTo(6, 5, 5,6, 8, 5);
        Selection fullGirderSegment = util.select().fromTo(6, 5, 5,6, 12, 5);
        Selection andesiteCapSegment = util.select().fromTo(andesiteCapBlock,andesiteCapBlock);
        Selection geyserSpoutSegment = util.select().fromTo(5,2,4,7,2,6);

        Selection counterweightSegment1 = util.select().fromTo(5,7,4,7,7,6);
        Selection counterweightSegment2 = util.select().fromTo(5,8,4,7,8,6);
        Selection counterweightSegment = util.select().fromTo(5,7,4,7,8,6);

        Selection kineticSegment = util.select().fromTo(6,2,0,6,4,4);
        Selection inPipeSegment = util.select().fromTo(11,1,5,10,2,5).add(util.select().fromTo(10,2,5,7,2,5));
        Selection outPipeSegment = util.select().fromTo(5,2,5,1,2,5).add(util.select().fromTo(1,1,5,0,1,5));
        Selection geyserPoolSegment = util.select().fromTo(2,1,1,9,1,8).substract(geyserSegment);

        Object layerEffectHighlight = new Object();


        // Show baseplate
        scene.showBasePlate();
        scene.world().showSection(geyserPoolSegment, Direction.DOWN);
        scene.world().showSection(geyserSegment, Direction.DOWN);
        scene.idle(20);

        scene.world().setKineticSpeed(kineticSegment, 0);
        scene.world().setKineticSpeed(outletSegment, 0);


        // --- Page 1: Piping ---
        scene.addKeyframe();
        scene.world().modifyBlockEntity(outletBlock, FrackingPumpOutletBlockEntity.class, blockEntity -> {
            blockEntity.setFalseRenderingLevel(4);
        });
        scene.world().showSection(outletSegment, Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(120)
                .text("resourceful_refinement.ponder.fracking_crafting.text_1")
                .pointAt(outletBlock.getCenter());
        scene.idle(140);

        scene.rotateCameraY(90);
        scene.idle(20);
        scene.addKeyframe();

        scene.overlay().showText(90)
                .text("resourceful_refinement.ponder.fracking_crafting.text_2")
                .independent();
        scene.idle(80);
        scene.world().showSection(inPipeSegment, Direction.DOWN);
        scene.idle(30);

        scene.overlay().showText(90)
                .text("resourceful_refinement.ponder.fracking_crafting.text_3")
                .pointAt(geyserPos.getCenter());
        scene.idle(80);

        scene.rotateCameraY(-90);
        scene.idle(20);
        scene.addKeyframe();

        scene.overlay().showText(90)
                .text("resourceful_refinement.ponder.fracking_crafting.text_4")
                .independent();
        scene.idle(40);
        scene.world().showSection(kineticSegment, Direction.DOWN);
        scene.idle(70);

        scene.rotateCameraY(-90);
        scene.idle(20);
        scene.addKeyframe();

        scene.overlay().showText(100)
                .text("resourceful_refinement.ponder.fracking_crafting.text_5")
                .independent();
        scene.idle(60);
        scene.world().showSection(outPipeSegment, Direction.DOWN);
        scene.idle(60);

        scene.rotateCameraY(90);
        scene.idle(20);
        scene.world().setKineticSpeed(kineticSegment.copy().add(outletSegment), 160);
        scene.idle(20);


        // --- Page 2: Speed ---
        scene.addKeyframe();

        scene.overlay().showText(100)
                .text("resourceful_refinement.ponder.fracking_crafting.text_6")
                .pointAt(girderSegment.getCenter());
        scene.idle(120);

        scene.overlay().showText(90)
                .text("resourceful_refinement.ponder.fracking_crafting.text_7")
                .pointAt(girderSegment.getCenter());
        scene.idle(110);

        scene.overlay().showText(110)
                .text("resourceful_refinement.ponder.fracking_crafting.text_8")
                .pointAt(kineticSegment.getCenter());
        scene.idle(130);


        scene.markAsFinished();
    }
}
