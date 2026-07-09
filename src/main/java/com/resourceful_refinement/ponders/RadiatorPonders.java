package com.resourceful_refinement.ponders;

import com.mojang.math.Axis;
import com.resourceful_refinement.content.paint_nozzle.PaintNozzleBlock;
import com.resourceful_refinement.content.radiator.RadiatorBlock;
import com.resourceful_refinement.registry.ModBlocks;
import com.resourceful_refinement.registry.ModFluids;
import com.resourceful_refinement.registry.ModItems;
import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.ParticleEmitter;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.EntityElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.lang.reflect.Field;


public class RadiatorPonders {

    public static void radiatorBasicsScene(SceneBuilder builder, SceneBuildingUtil util) {

        //Build scene
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("radiator_basics", "Using Radiators");

        scene.configureBasePlate(0,0,6);
        scene.removeShadow();

        scene.rotateCameraY(0);
        scene.scaleSceneView(1f);

        BlockPos radiator1Pos = util.grid().at(3, 1, 1);
        Selection radiator1Segment = util.select().fromTo(radiator1Pos, radiator1Pos);
        Object radiatorHeatSlot1 = new Object();

        BlockPos radiator2Pos = util.grid().at(1, 1, 3);
        Selection radiator2Segment = util.select().fromTo(radiator2Pos, radiator2Pos);
        Object radiatorHeatSlot2 = new Object();

        BlockPos pumpPos = util.grid().at(4, 1, 2);
        Selection pumpSegment = util.select().fromTo(pumpPos, pumpPos);

        BlockPos tankPos1 = util.grid().at(4, 1, 3);
        BlockPos tankPos2 = util.grid().at(4, 2, 3);
        Selection tank1Segment = util.select().fromTo(tankPos1, tankPos2);

        BlockPos tankPos3 = util.grid().at(2, 1, 4);
        Selection tank2Segment = util.select().fromTo(tankPos3, tankPos3);

        Selection kineticSegment = util.select().fromTo(5,1,2, 4,1,2);
        Selection pipesSegment = util.select().fromTo(1,1,1, 4,1,4).substract(tank1Segment).substract(tank2Segment).substract(radiator1Segment).substract(radiator2Segment);


        // Show baseplate
        scene.showBasePlate();
        scene.idle(20);

        scene.world().showSection(radiator1Segment, Direction.DOWN);
        scene.world().showSection(radiator2Segment, Direction.DOWN);
        scene.idle(20);

        // --- Page 1: Radiator Flow ---
        scene.addKeyframe();
        scene.overlay().showText(90)
                .text("resourceful_refinement.ponder.radiator_basics.text_1")
                .pointAt(radiator2Pos.getCenter());


        FluidStack waterStack = new FluidStack(Fluids.WATER, 8000);
        scene.world().modifyBlockEntity(tankPos1, FluidTankBlockEntity.class, tankBE -> {
            if (tankBE.getLevel() != null) {
                var tankCapability = tankBE.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, tankBE.getBlockPos(), null);
                if (tankCapability != null)
                {
                    tankCapability.drain(32000, IFluidHandler.FluidAction.EXECUTE);
                    tankCapability.fill(waterStack, IFluidHandler.FluidAction.EXECUTE);
                }
            }
            // Force the client-side Ponder world to synchronize and render the fluid
            tankBE.sendData();
        });

        scene.idle(110);
        scene.world().showSection(tank1Segment, Direction.DOWN);
        scene.idle(8);
        scene.world().showSection(tank2Segment, Direction.DOWN);
        scene.idle(8);
        scene.world().showSection(pipesSegment, Direction.DOWN);
        scene.idle(20);

        scene.world().setKineticSpeed(kineticSegment, 32);
        scene.idle(10);

        scene.addKeyframe();
        scene.overlay().showText(160)
                .text("resourceful_refinement.ponder.radiator_basics.text_2")
                .independent();

        scene.idle(80);

        scene.overlay().showText(80)
                .text("resourceful_refinement.ponder.radiator_basics.text_3")
                .pointAt(radiator2Pos.getCenter());

        scene.idle(80);
        scene.world().setKineticSpeed(kineticSegment, 0);
        scene.idle(10);

        FluidStack coolantStack = new FluidStack(ModFluids.COOLANT.source, 8000);
        scene.world().modifyBlockEntity(tankPos1, FluidTankBlockEntity.class, tankBE -> {
            if (tankBE.getLevel() != null) {
                var tankCapability = tankBE.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, tankBE.getBlockPos(), null);
                if (tankCapability != null)
                {
                    tankCapability.drain(32000, IFluidHandler.FluidAction.EXECUTE);
                    tankCapability.fill(coolantStack, IFluidHandler.FluidAction.EXECUTE);
                }
            }
            // Force the client-side Ponder world to synchronize and render the fluid
            tankBE.sendData();
        });

        scene.idle(10);

        // --- Page 2: Fluids & Heat---
        scene.addKeyframe();
        scene.overlay().showText(100)
                .text("resourceful_refinement.ponder.radiator_basics.text_4")
                .pointAt(tank1Segment.getCenter());

        scene.idle(120);
        scene.world().setKineticSpeed(kineticSegment, 32);


        scene.addKeyframe();
        scene.overlay().showOutline(PonderPalette.BLUE, radiatorHeatSlot1, radiator1Segment, 80);
        scene.overlay().showOutline(PonderPalette.BLUE, radiatorHeatSlot2, radiator2Segment, 80);
        scene.world().modifyBlock(radiator1Pos, blockState ->{
            return blockState.setValue(RadiatorBlock.HEAT_STATE, 1);
        }, false);
        scene.world().modifyBlock(radiator2Pos, blockState ->{
            return blockState.setValue(RadiatorBlock.HEAT_STATE, 1);
        }, false);
        scene.overlay().showText(70)
                .text("resourceful_refinement.ponder.radiator_basics.text_5")
                .independent();

        scene.idle(80);
        scene.world().setKineticSpeed(kineticSegment, 0);
        scene.idle(10);

        FluidStack carboraxStack = new FluidStack(ModFluids.CATALYSED_CARBORAX.source, 8000);
        scene.world().modifyBlockEntity(tankPos1, FluidTankBlockEntity.class, tankBE -> {
            if (tankBE.getLevel() != null) {
                var tankCapability = tankBE.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, tankBE.getBlockPos(), null);
                if (tankCapability != null)
                {
                    tankCapability.drain(32000, IFluidHandler.FluidAction.EXECUTE);
                    tankCapability.fill(carboraxStack, IFluidHandler.FluidAction.EXECUTE);
                }
            }
            // Force the client-side Ponder world to synchronize and render the fluid
            tankBE.sendData();
        });

        scene.idle(10);
        scene.world().setKineticSpeed(kineticSegment, 32);
        scene.idle(10);

        scene.overlay().showOutline(PonderPalette.RED, radiatorHeatSlot1, radiator1Segment, 80);
        scene.overlay().showOutline(PonderPalette.RED, radiatorHeatSlot2, radiator2Segment, 80);
        scene.world().modifyBlock(radiator1Pos, blockState ->{
            return blockState.setValue(RadiatorBlock.HEAT_STATE, 4);
        }, false);
        scene.world().modifyBlock(radiator2Pos, blockState ->{
            return blockState.setValue(RadiatorBlock.HEAT_STATE, 4);
        }, false);
        scene.overlay().showText(70)
                .text("resourceful_refinement.ponder.radiator_basics.text_6")
                .independent();

        scene.idle(90);

        // --- Page 3: Consumption ---
        scene.addKeyframe();
        scene.overlay().showText(140)
                .text("resourceful_refinement.ponder.radiator_basics.text_7")
                .pointAt(radiator2Segment.getCenter());

        scene.idle(140);

        scene.markAsFinished();
    }

    public static void radiatorCraftingScene(SceneBuilder builder, SceneBuildingUtil util) {

        //Build scene
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("radiator_heating", "Heating with Radiators");

        scene.configureBasePlate(0,0,3);
        scene.removeShadow();

        scene.rotateCameraY(0);
        scene.scaleSceneView(1f);

        BlockPos radiator1Pos = util.grid().at(1, 1, 1);
        Selection radiator1Segment = util.select().fromTo(radiator1Pos, radiator1Pos);

        BlockPos basinPos = util.grid().at(1, 2, 1);
        Selection basinSegment = util.select().fromTo(basinPos, basinPos);

        BlockPos mixerPos = util.grid().at(1, 4, 1);
        Selection mixerSegment = util.select().fromTo(mixerPos, mixerPos);

        BlockPos pipePos1 = util.grid().at(0, 1, 1);
        BlockPos pipePos2 = util.grid().at(2, 1, 1);
        Selection pipeSegment = util.select().fromTo(pipePos1, pipePos2);



        // Show baseplate
        scene.showBasePlate();
        scene.idle(20);

        scene.world().showSection(pipeSegment, Direction.DOWN);
        scene.idle(20);

        // --- Page 1: Radiator Placement ---
        scene.addKeyframe();

        Object radiatorHeatSlot = new Object();
        scene.overlay().showOutline(PonderPalette.RED, radiatorHeatSlot, basinSegment, 130);
        scene.idle(20);

        scene.overlay().showText(110)
                .text("resourceful_refinement.ponder.radiator_heating.text_1")
                .independent();

        scene.idle(130);

        scene.world().showSection(basinSegment, Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(mixerSegment, Direction.DOWN);
        scene.idle(20);

        scene.world().modifyBlock(radiator1Pos, blockState ->{
            return blockState.setValue(RadiatorBlock.HEAT_STATE, 4);
        }, false);

        scene.world().setKineticSpeed(mixerSegment, 64);

        scene.addKeyframe();
        scene.overlay().showText(100)
                .text("resourceful_refinement.ponder.radiator_heating.text_2")
                .pointAt(radiator1Pos.getCenter());

        scene.idle(120);

        scene.world().setKineticSpeed(mixerSegment, 0);
        scene.idle(20);


        // --- Page 2: Fluids & Heat---
        scene.addKeyframe();
        scene.overlay().showText(60)
                .text("resourceful_refinement.ponder.radiator_heating.text_3")
                .independent();
        scene.world().modifyBlock(radiator1Pos, blockState ->{
            return blockState.setValue(RadiatorBlock.HEAT_STATE, 0);
        }, false);

        scene.idle(80);


        scene.overlay().showText(60)
                .text("resourceful_refinement.ponder.radiator_heating.text_4")
                .independent();
        scene.world().modifyBlock(radiator1Pos, blockState ->{
            return blockState.setValue(RadiatorBlock.HEAT_STATE, 1);
        }, false);

        scene.idle(80);

        scene.overlay().showText(60)
                .text("resourceful_refinement.ponder.radiator_heating.text_5")
                .independent();
        scene.world().modifyBlock(radiator1Pos, blockState ->{
            return blockState.setValue(RadiatorBlock.HEAT_STATE, 3);
        }, false);

        scene.idle(80);

        scene.overlay().showText(60)
                .text("resourceful_refinement.ponder.radiator_heating.text_6")
                .independent();
        scene.world().modifyBlock(radiator1Pos, blockState ->{
            return blockState.setValue(RadiatorBlock.HEAT_STATE, 4);
        }, false);

        scene.idle(80);

        scene.overlay().showText(60)
                .text("resourceful_refinement.ponder.radiator_heating.text_7")
                .independent();
        scene.world().modifyBlock(radiator1Pos, blockState ->{
            return blockState.setValue(RadiatorBlock.HEAT_STATE, 5);
        }, false);

        scene.idle(80);


        scene.markAsFinished();
    }
}
