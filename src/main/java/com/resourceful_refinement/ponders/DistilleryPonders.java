package com.resourceful_refinement.ponders;

import com.mojang.math.Axis;
import com.resourceful_refinement.content.distillery.DistilleryBlockEntity;
import com.resourceful_refinement.content.paint_nozzle.PaintNozzleBlock;
import com.resourceful_refinement.content.radiator.RadiatorBlock;
import com.resourceful_refinement.registry.ModBlocks;
import com.resourceful_refinement.registry.ModFluids;
import com.resourceful_refinement.registry.ModItems;
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


public class DistilleryPonders {

    public static void distilleryScene(SceneBuilder builder, SceneBuildingUtil util) {

        //Build scene
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("distillery_basics", "Distillery Towers");

        scene.configureBasePlate(0,0,5);
        scene.removeShadow();

        scene.rotateCameraY(0);
        scene.scaleSceneView(1f);

        BlockPos scaffoldPos = util.grid().at(2, 1, 2);
        Selection scaffoldSegment = util.select().fromTo(scaffoldPos, scaffoldPos);

        BlockPos distilleryBasePos = util.grid().at(2, 2, 2);
        BlockPos distilleryTopPos = util.grid().at(2, 4, 2);
        Selection distillerySegment = util.select().fromTo(distilleryBasePos, distilleryTopPos);
        Object distilleryHeightSlot = new Object();
        Object distilleryHeatSlot = new Object();

        BlockPos pump1Pos = util.grid().at(1, 5, 2);
        Selection pump1Segment = util.select().fromTo(pump1Pos, pump1Pos);

        BlockPos pump2Pos = util.grid().at(3, 1, 4);
        Selection pump2Segment = util.select().fromTo(pump2Pos, pump2Pos);

        BlockPos tank1PosBottom = util.grid().at(4, 1, 4);
        BlockPos tank1PosTop = util.grid().at(4, 2, 4);
        Selection tank1Segment = util.select().fromTo(tank1PosBottom, tank1PosTop);

        BlockPos tank2PosBottom = util.grid().at(0, 1, 2);
        BlockPos tank2PosTop = util.grid().at(1, 2, 2);
        Selection tank2Segment = util.select().fromTo(tank2PosBottom, tank2PosTop);

        Selection pipes1Segment = util.select().fromTo(2, 1, 3, 4, 2, 4);
        Selection pipes2Segment = util.select().fromTo(0, 1, 2, 1, 5, 2).add(util.select().position(2,5,2));
        Selection conveyorSegment = util.select().fromTo(2, 1, 0, 2, 2, 1);


        // Show baseplate
        scene.showBasePlate();
        scene.idle(20);

        scene.world().showSection(scaffoldSegment, Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(distillerySegment, Direction.DOWN);
        scene.idle(20);


        // --- Page 1: Distillery Towers ---
        scene.addKeyframe();
        scene.overlay().showText(110)
                .text("resourceful_refinement.ponder.distillery_basics.text_1")
                .pointAt(distillerySegment.getCenter());
        scene.idle(130);

        scene.overlay().showText(110)
                .text("resourceful_refinement.ponder.distillery_basics.text_2")
                .independent();
        scene.idle(130);


        // --- Page 2: Tower Height ---
        scene.addKeyframe();

        scene.overlay().showOutline(PonderPalette.INPUT, distilleryHeightSlot, distillerySegment, 110);
        scene.overlay().showText(130)
                .text("resourceful_refinement.ponder.distillery_basics.text_3")
                .independent();

        scene.idle(150);


        // --- Page 3: Recipes ---
        scene.addKeyframe();

        scene.world().showSection(conveyorSegment, Direction.DOWN);
        scene.overlay().showText(320)
                .text("resourceful_refinement.ponder.distillery_basics.text_4")
                .independent(-8);
        scene.idle(90);

        scene.rotateCameraY(90);
        scene.idle(20);

        scene.world().showSection(pipes1Segment, Direction.DOWN);
        scene.overlay().showText(120)
                .text("resourceful_refinement.ponder.distillery_basics.text_5")
                .independent(24);
        scene.idle(30);
        FluidStack waterStack = new FluidStack(Fluids.WATER, 6000);
        scene.world().modifyBlockEntity(tank1PosBottom, FluidTankBlockEntity.class, tankBE -> {
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
        scene.world().modifyBlockEntity(distilleryBasePos, DistilleryBlockEntity.class, distilleryBE -> {
            distilleryBE.inputTank.fill(waterStack, IFluidHandler.FluidAction.EXECUTE);
            distilleryBE.sendData();
        });
        scene.world().setKineticSpeed(pump2Segment, 32);
        scene.idle(60);
        scene.rotateCameraY(-90);
        scene.idle(20);

        scene.world().showSection(pipes2Segment, Direction.DOWN);
        scene.overlay().showText(100)
                .text("resourceful_refinement.ponder.distillery_basics.text_6")
                .independent(64);

        scene.idle(30);
        FluidStack dieselStack = new FluidStack(ModFluids.CARBORAX_DIESEL.source, 6000);
        scene.world().modifyBlockEntity(tank2PosBottom, FluidTankBlockEntity.class, tankBE -> {
            if (tankBE.getLevel() != null) {
                var tankCapability = tankBE.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, tankBE.getBlockPos(), null);
                if (tankCapability != null)
                {
                    tankCapability.drain(32000, IFluidHandler.FluidAction.EXECUTE);
                    tankCapability.fill(dieselStack, IFluidHandler.FluidAction.EXECUTE);
                }
            }
            // Force the client-side Ponder world to synchronize and render the fluid
            tankBE.sendData();
        });
        scene.world().modifyBlockEntity(distilleryBasePos, DistilleryBlockEntity.class, distilleryBE -> {
            distilleryBE.outputTank.fill(dieselStack, IFluidHandler.FluidAction.EXECUTE);
            distilleryBE.sendData();
        });
        scene.world().setKineticSpeed(pump1Segment, 32);
        scene.idle(90);

        scene.world().hideSection(conveyorSegment, Direction.UP);
        scene.idle(20);

        // --- Page 4: Heating ---
        scene.addKeyframe();

        scene.overlay().showOutline(PonderPalette.RED, distilleryHeatSlot, scaffoldSegment, 100);
        scene.overlay().showText(100)
                .text("resourceful_refinement.ponder.distillery_basics.text_7")
                .pointAt(scaffoldPos.getCenter());
        scene.idle(120);


        scene.world().setBlock(scaffoldPos, AllBlocks.BLAZE_BURNER.getDefaultState(), true);
        scene.overlay().showText(70)
                .text("resourceful_refinement.ponder.distillery_basics.text_8")
                .independent();
        scene.idle(90);

        scene.world().setBlock(scaffoldPos, ModBlocks.RADIATOR_PIPE.get().defaultBlockState().setValue(RadiatorBlock.HEAT_STATE, 0), true);
        scene.overlay().showText(70)
                .text("resourceful_refinement.ponder.distillery_basics.text_9")
                .independent();
        scene.idle(90);

        scene.overlay().showText(120)
                .text("resourceful_refinement.ponder.distillery_basics.text_10")
                .pointAt(distillerySegment.getCenter());
        scene.idle(140);


        // --- Page 4: Explosions ---
        scene.addKeyframe();

        scene.overlay().showText(100)
                .text("resourceful_refinement.ponder.distillery_basics.text_10")
                .independent();
        scene.idle(105);

        ParticleEmitter fx = scene.effects().simpleParticleEmitter(ParticleTypes.EXPLOSION, util.vector().of(0, 0, 0));
        scene.effects().emitParticles(distillerySegment.getCenter(), fx, 5, 1);
        scene.world().setBlocks(distillerySegment, Blocks.AIR.defaultBlockState(), true);

        scene.idle(15);
        scene.overlay().showText(60)
                .text("resourceful_refinement.ponder.distillery_basics.text_10")
                .independent();
        scene.idle(60);


        scene.markAsFinished();
    }
}
