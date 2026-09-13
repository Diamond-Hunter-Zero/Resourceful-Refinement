package com.resourceful_refinement.ponders;

import com.resourceful_refinement.content.advanced_pump.AdvancedPumpBlock;
import com.resourceful_refinement.content.advanced_pump.AdvancedPumpBlockEntity;
import com.resourceful_refinement.content.radiator.RadiatorBlock;
import com.resourceful_refinement.registry.ModFluids;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;


public class PumpPonders {

    public static void advancedPumpBasicsScene(SceneBuilder builder, SceneBuildingUtil util) {

        //Build scene
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("advanced_pump", "Advanced Fluid Control");

        scene.configureBasePlate(1,0,3);
        scene.removeShadow();

        scene.rotateCameraY(0);
        scene.scaleSceneView(1f);

        BlockPos pumpPos = util.grid().at(2, 1, 1);
        Selection pumpSegment = util.select().fromTo(pumpPos, pumpPos);

        BlockPos tank1Pos = util.grid().at(0, 1, 1);
        Selection tank1Segment = util.select().fromTo(tank1Pos, tank1Pos);

        BlockPos tank2Pos = util.grid().at(4, 1, 1);
        Selection tank2Segment = util.select().fromTo(tank2Pos, tank2Pos);

        BlockPos pipe1Pos = util.grid().at(1, 1, 1);
        Selection pipe1Segment = util.select().fromTo(pipe1Pos, pipe1Pos);

        BlockPos pipe2Pos = util.grid().at(3, 1, 1);
        Selection pipe2Segment = util.select().fromTo(pipe2Pos, pipe2Pos);

        BlockPos kineticsPos = util.grid().at(2, 1, 2);
        Selection kineticsSegment = util.select().fromTo(2,1,2,3,1,2);

        BlockPos leverPos = util.grid().at(2, 1, 0);
        Selection leverSegment = util.select().fromTo(leverPos, leverPos);



        // Show baseplate
        scene.showBasePlate();
        scene.idle(20);

        scene.world().showSection(pumpSegment, Direction.DOWN);
        scene.world().showSection(kineticsSegment, Direction.DOWN);
        scene.idle(20);

        // --- Page 1: Advanced Pump ---
        scene.addKeyframe();
        scene.overlay().showText(100)
                .text("resourceful_refinement.ponder.advanced_pump.text_1")
                .pointAt(pumpSegment.getCenter());
        scene.idle(120);

        scene.world().showSection(pipe1Segment, Direction.DOWN);
        scene.world().showSection(pipe2Segment, Direction.DOWN);

        scene.idle(20);
        scene.addKeyframe();
        scene.overlay().showText(100)
                .text("resourceful_refinement.ponder.advanced_pump.text_2")
                .independent();
        scene.idle(120);

        scene.overlay().showText(70)
                .text("resourceful_refinement.ponder.advanced_pump.text_3")
                .independent();
        scene.idle(90);


        // --- Page 2: Redstone Control ---
        scene.addKeyframe();
        scene.world().showSection(leverSegment, Direction.DOWN);
        scene.idle(20);

        scene.world().modifyBlock(leverPos, blockState ->{
            return blockState.setValue(LeverBlock.POWERED, true);
        }, false);
        scene.effects().indicateRedstone(leverPos);


        scene.world().modifyBlock(pumpPos, blockState ->{
            Direction currentFacing = blockState.getValue(AdvancedPumpBlock.FACING).getOpposite();
            return blockState.setValue(AdvancedPumpBlock.FACING, currentFacing);
        }, false);

        scene.idle(10);
        scene.world().modifyBlockEntity(pumpPos, AdvancedPumpBlockEntity.class, pumpBE -> {
            pumpBE.updatePressureChange();
            pumpBE.sendData();
        });
        scene.idle(10);

        scene.overlay().showText(210)
                .text("resourceful_refinement.ponder.advanced_pump.text_4")
                .independent();
        scene.idle(110);

        scene.overlay().showText(100)
                .text("resourceful_refinement.ponder.advanced_pump.text_4")
                .independent(40);
        scene.idle(120);



        scene.markAsFinished();
    }

}
