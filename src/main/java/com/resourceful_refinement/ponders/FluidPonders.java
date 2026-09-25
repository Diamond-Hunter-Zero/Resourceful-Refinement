package com.resourceful_refinement.ponders;

import com.resourceful_refinement.registry.ModFluids;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;


public class FluidPonders {

    public static void liquidConcreteScene(SceneBuilder builder, SceneBuildingUtil util) {

        //Build scene
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("liquid_concrete", "Building with Liquid Concrete");

        scene.configureBasePlate(0,0,8);
        scene.removeShadow();

        scene.rotateCameraY(0);
        scene.scaleSceneView(1f);

        BlockPos hosePullyPos = util.grid().at(5, 4, 4);
        Selection hosePullySegment = util.select().fromTo(hosePullyPos, hosePullyPos);

        BlockPos pumpPos = util.grid().at(6, 4, 4);
        Selection pumpSegment = util.select().fromTo(pumpPos, pumpPos);

        BlockPos valvePos = util.grid().at(4, 4, 4);
        Selection valveSegment = util.select().fromTo(valvePos, valvePos);

        BlockPos tankPos = util.grid().at(7, 2, 4);
        Selection tankSegment = util.select().fromTo(tankPos, tankPos.above());

        Selection concretePumpSegment = util.select().fromTo(7,4,3, 4,4,5).add(util.select().fromTo(7,1,4,7,3,4));
        Selection constructionSegment = util.select().fromTo(6,3,7, 0,1,1);

        Selection constructionBorderSegment = util.select().fromTo(6,1,1, 6,3,7)
                .add(util.select().fromTo(0,1,7,6,2,7))
                .add(util.select().fromTo(0,1,3,0,2,6))
                .add(util.select().fromTo(1,1,1,2,1,3))
                .add(util.select().fromTo(3,1,1,5,2,1));

        Selection cement1Segment = util.select().fromTo(5,1,3,5,1,5).add(util.select().fromTo(4,1,4,4,1,4));
        Selection cement2Segment = util.select().fromTo(4,1,2,5,1,6).add(util.select().fromTo(3,1,3,3,1,5));
        Selection cement3Segment = util.select().fromTo(3,1,2,5,1,6).add(util.select().fromTo(2,1,4,2,1,5));
        Selection cement4Segment = util.select().fromTo(3,1,2,5,1,6).add(util.select().fromTo(1,1,4,2,1,6));



        // Show baseplate
        scene.showBasePlate();
        scene.idle(20);


        // --- Page 1: Liquid Concrete ---
        scene.addKeyframe();

        scene.world().showSection(constructionSegment, Direction.DOWN);
        scene.idle(20);

        scene.world().showSection(concretePumpSegment, Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(90)
                .text("resourceful_refinement.ponder.liquid_concrete.text_1")
                .independent();
        scene.idle(110);


        // --- Page 2: Liquid Concrete Conversion ---
        scene.addKeyframe();

        scene.world().setKineticSpeed(pumpSegment, 32);
        scene.world().movePulley(hosePullyPos, 1, 20);
        scene.idle(20);

        scene.overlay().showText(120)
                .text("resourceful_refinement.ponder.liquid_concrete.text_2")
                .pointAt(hosePullyPos.getCenter().add(-0.75,0,0.25));
        scene.idle(20);

        scene.world().setBlocks(cement1Segment, ModFluids.POURED_CEMENT.source.get().defaultFluidState().createLegacyBlock(), false);
        scene.idle(15);
        scene.world().setBlocks(cement2Segment, ModFluids.POURED_CEMENT.source.get().defaultFluidState().createLegacyBlock(), false);
        scene.idle(15);
        scene.world().setBlocks(cement3Segment, ModFluids.POURED_CEMENT.source.get().defaultFluidState().createLegacyBlock(), false);
        scene.idle(15);
        scene.world().setBlocks(cement4Segment, ModFluids.POURED_CEMENT.source.get().defaultFluidState().createLegacyBlock(), false);
        scene.idle(15);

        scene.idle(60);

        scene.addKeyframe();
        scene.overlay().showText(140)
                .text("resourceful_refinement.ponder.liquid_concrete.text_3")
                .independent();
        scene.idle(30);

        scene.world().setBlocks(cement4Segment, Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState(), false);
        scene.idle(30);

        scene.world().hideSection(concretePumpSegment, Direction.UP);
        scene.idle(10);
        scene.world().hideSection(constructionBorderSegment, Direction.UP);
        scene.idle(90);


        scene.markAsFinished();
    }
}
