package com.resourceful_refinement.ponders;

import com.resourceful_refinement.content.distillery.DistilleryBlockEntity;
import com.resourceful_refinement.content.fuel_tank.FuelTankBlockEntity;
import com.resourceful_refinement.content.radiator.RadiatorBlock;
import com.resourceful_refinement.registry.ModFluids;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;


public class CombustionChamberPonders {

    public static void chamberBasicsScene(SceneBuilder builder, SceneBuildingUtil util) {

        //Build scene
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("combustion_chamber", "Powering Combustion Chambers");

        scene.configureBasePlate(1,0,5);
        scene.removeShadow();

        scene.rotateCameraY(0);
        scene.scaleSceneView(1f);

        BlockPos chamber1Pos = util.grid().at(4, 2, 2);
        Selection chamber1Segment = util.select().fromTo(chamber1Pos, chamber1Pos);

        Selection otherChamberTrainSegment = util.select().fromTo(2, 2, 2,3, 2, 2);
        Selection fullChamberTrainSegment = util.select().fromTo(2, 2, 2,4, 2, 2);
        Selection fullCooledChambersSegment = util.select().fromTo(2, 1, 2,4, 2, 2);
        BlockPos endChamberPos = util.grid().at(2, 2, 2);
        Object chamberTrainSlot = new Object();
        Object cooledChamberTrainSlot = new Object();

        BlockPos leverPos = util.grid().at(4, 3, 2);
        Selection leverSegment = util.select().fromTo(leverPos, leverPos);

        BlockPos fanPos = util.grid().at(1, 2, 2);
        Selection fanSegment = util.select().fromTo(fanPos, fanPos);

        BlockPos radiatorPos1 = util.grid().at(4, 1, 2);
        BlockPos radiatorPos2 = util.grid().at(3, 1, 2);
        BlockPos radiatorPos3 = util.grid().at(2, 1, 2);
        Selection radiatorSegment = util.select().fromTo(1, 1, 2, 5, 1, 2);

        BlockPos pipePos1 = util.grid().at(5, 2, 2);
        Selection pipe1Segment = util.select().fromTo(pipePos1, pipePos1);


        // Show baseplate
        scene.showBasePlate();
        scene.idle(20);

        scene.world().showSection(chamber1Segment, Direction.DOWN);
        scene.idle(20);

        // --- Page 1: Combustion Chamber ---
        scene.addKeyframe();
        scene.overlay().showText(120)
                .text("resourceful_refinement.ponder.combustion_chamber.text_1")
                .pointAt(chamber1Segment.getCenter());
        scene.idle(140);

        scene.overlay().showText(100)
                .text("resourceful_refinement.ponder.combustion_chamber.text_2")
                .independent();
        scene.world().showSection(pipe1Segment, Direction.DOWN);
        scene.idle(20);
        scene.world().setKineticSpeed(fullChamberTrainSegment, -40);
        scene.idle(100);

        scene.overlay().showText(70)
                .text("resourceful_refinement.ponder.combustion_chamber.text_3")
                .independent();
        scene.idle(90);


        // --- Page 2: Redstone Control ---
        scene.addKeyframe();

        scene.world().showSection(leverSegment, Direction.DOWN);
        scene.idle(10);
        scene.overlay().showText(80)
                .text("resourceful_refinement.ponder.combustion_chamber.text_4")
                .pointAt(leverPos.getCenter());
        scene.idle(40);

        scene.world().modifyBlock(leverPos, blockState ->{
            return blockState.setValue(LeverBlock.POWERED, true);
        }, false);
        scene.effects().indicateRedstone(leverPos);
        scene.world().setKineticSpeed(fullChamberTrainSegment, 0);

        scene.idle(60);
        scene.world().modifyBlock(leverPos, blockState ->{
            return blockState.setValue(LeverBlock.POWERED, false);
        }, false);
        scene.effects().indicateRedstone(leverPos);
        scene.world().setKineticSpeed(fullChamberTrainSegment, -40);

        scene.idle(10);
        scene.world().hideSection(leverSegment, Direction.UP);
        scene.idle(20);


        // --- Page 3: Chamber Trains ---
        scene.addKeyframe();

        scene.overlay().showText(90)
                .text("resourceful_refinement.ponder.combustion_chamber.text_5")
                .independent();
        scene.idle(20);
        scene.world().showSection(otherChamberTrainSegment, Direction.DOWN);
        scene.idle(90);

        scene.overlay().showText(100)
                .text("resourceful_refinement.ponder.combustion_chamber.text_6")
                .pointAt(endChamberPos.getCenter());
        scene.overlay().showOutline(PonderPalette.RED, chamberTrainSlot, fullChamberTrainSegment, 100);
        scene.idle(120);

        scene.addKeyframe();
        scene.world().showSection(fanSegment, Direction.DOWN);
        scene.idle(20);
        scene.world().setKineticSpeed(fanSegment, -40);

        scene.overlay().showText(100)
                .text("resourceful_refinement.ponder.combustion_chamber.text_7")
                .independent();
        scene.idle(140);


        // --- Page 4: Fueling Chambers ---
        scene.addKeyframe();
        scene.overlay().showText(260)
                .text("resourceful_refinement.ponder.combustion_chamber.text_8")
                .independent(-8);
        scene.idle(140);

        scene.world().setKineticSpeed(fullChamberTrainSegment, 0);
        scene.world().setKineticSpeed(fanSegment, 0);
        scene.overlay().showText(100)
                .text("resourceful_refinement.ponder.combustion_chamber.text_9")
                .independent(56);
        scene.idle(120);

        scene.addKeyframe();
        scene.world().showSection(radiatorSegment, Direction.UP);
        scene.overlay().showText(100)
                .text("resourceful_refinement.ponder.combustion_chamber.text_10")
                .independent(56);

        scene.idle(10);
        scene.world().modifyBlock(radiatorPos1, blockState ->{
            return blockState.setValue(RadiatorBlock.HEAT_STATE, 0);
        }, false);
        scene.idle(10);
        scene.world().modifyBlock(radiatorPos2, blockState ->{
            return blockState.setValue(RadiatorBlock.HEAT_STATE, 0);
        }, false);
        scene.idle(10);
        scene.world().modifyBlock(radiatorPos3, blockState ->{
            return blockState.setValue(RadiatorBlock.HEAT_STATE, 0);
        }, false);

        scene.idle(20);
        scene.world().setKineticSpeed(fullChamberTrainSegment, -72);
        scene.world().setKineticSpeed(fanSegment, -72);
        scene.idle(70);

        scene.addKeyframe();
        scene.overlay().showOutline(PonderPalette.INPUT, cooledChamberTrainSlot, fullCooledChambersSegment, 100);
        scene.overlay().showText(120)
                .text("resourceful_refinement.ponder.combustion_chamber.text_11")
                .independent();
        scene.idle(130);



        scene.markAsFinished();
    }

    public static void fuelTankScene(SceneBuilder builder, SceneBuildingUtil util) {

        //Build scene
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("fuel_tank", "Utilising Fuel Tanks");

        scene.configureBasePlate(0,0,3);
        scene.removeShadow();

        scene.rotateCameraY(0);
        scene.scaleSceneView(1f);

        BlockPos tankPos = util.grid().at(1, 1, 1);
        Selection tankSegment = util.select().fromTo(tankPos, tankPos);

        BlockPos chamberPos = util.grid().at(0, 1, 1);
        Selection chamberSegment = util.select().fromTo(chamberPos, chamberPos);

        BlockPos pumpPos = util.grid().at(1, 1, 2);
        Selection pumpSegment = util.select().fromTo(pumpPos, pumpPos);

        BlockPos pipePos = util.grid().at(2, 1, 1);
        Selection pipeSegment = util.select().fromTo(pipePos, pipePos);



        // Show baseplate
        scene.showBasePlate();
        scene.idle(20);

        scene.world().showSection(tankSegment, Direction.DOWN);
        scene.idle(20);

        // --- Page 1: Fuel Tank ---
        scene.addKeyframe();
        scene.overlay().showText(90)
                .text("resourceful_refinement.ponder.fuel_tank.text_1")
                .pointAt(tankSegment.getCenter());
        scene.idle(110);

        scene.world().showSection(pipeSegment, Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(pumpSegment, Direction.DOWN);
        scene.idle(20);

        scene.world().setKineticSpeed(pumpSegment, 32);
        FluidStack waterStack = new FluidStack(Fluids.WATER, 4000);
        scene.world().modifyBlockEntity(tankPos, FuelTankBlockEntity.class, tankBE -> {
            tankBE.tank.fill(waterStack, IFluidHandler.FluidAction.EXECUTE);
            tankBE.setChanged();
        });

        scene.overlay().showText(120)
                .text("resourceful_refinement.ponder.fuel_tank.text_2")
                .independent();
        scene.idle(140);

        scene.world().modifyBlockEntity(tankPos, FuelTankBlockEntity.class, tankBE -> {
            tankBE.tank.drain(4000, IFluidHandler.FluidAction.EXECUTE);
            tankBE.setChanged();
        });
        scene.idle(10);


        // --- Page 2: Manual Fill ---
        scene.addKeyframe();

        scene.overlay().showText(120)
                .text("resourceful_refinement.ponder.fuel_tank.text_3")
                .independent();
        scene.idle(20);

        scene.overlay().showControls(tankSegment.getCenter(), Pointing.LEFT, 40).withItem(ModFluids.CATALYSED_CARBORAX.bucket.toStack());
        scene.idle(10);
        FluidStack carboraxStack = new FluidStack(ModFluids.CATALYSED_CARBORAX.source, 4000);
        scene.world().modifyBlockEntity(tankPos, FuelTankBlockEntity.class, tankBE -> {
            tankBE.tank.fill(carboraxStack, IFluidHandler.FluidAction.EXECUTE);
            tankBE.setChanged();
        });
        scene.idle(110);


        // --- Page 3: Auto Filling ---
        scene.world().showSection(chamberSegment, Direction.DOWN);
        scene.idle(20);
        scene.world().modifyBlockEntity(tankPos, FuelTankBlockEntity.class, tankBE -> {
            tankBE.tank.drain(1250, IFluidHandler.FluidAction.EXECUTE);
            tankBE.setChanged();
        });

        scene.addKeyframe();
        scene.overlay().showText(120)
                .text("resourceful_refinement.ponder.fuel_tank.text_4")
                .independent();
        scene.idle(20);
        scene.world().setKineticSpeed(chamberSegment, 128);
        scene.idle(100);


        scene.markAsFinished();
    }
}
