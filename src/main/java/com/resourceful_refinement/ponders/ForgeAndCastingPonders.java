package com.resourceful_refinement.ponders;

import com.resourceful_refinement.content.forge_mould.MechanicalForgeMouldBlockEntity;
import com.resourceful_refinement.registry.ModBlocks;
import com.resourceful_refinement.registry.ModItems;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import com.simibubi.create.foundation.ponder.element.BeltItemElement;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;


public class ForgeAndCastingPonders {

    public static void mechanicalForgeScene(SceneBuilder builder, SceneBuildingUtil util) {

        //Build scene
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("fluid_forging", "Fluid Forging");

        scene.configureBasePlate(0,1,7);

        scene.rotateCameraY(0);
        scene.scaleSceneView(1.5f);

        BlockPos forgePos = util.grid().at(3, 3, 3);
        BlockPos motorPos1 = util.grid().at(5, 1, 4);
        Selection motorSegment1 = util.select().fromTo(5, 1, 4, 5, 1, 4);
        Selection motorSegment2 = util.select().fromTo(2,2,0, 4,2,0);
        Selection pumpSegment = util.select().fromTo(3, 4, 4, 3, 4, 4);

        BlockPos tunnel = util.grid().at(3, 3, 2);
        BlockPos castingDepotPos = util.grid().at(3, 1, 3);
        BlockPos pumpPos = util.grid().at(3, 4, 4);

        Selection castingDepotSegment = util.select().fromTo(3, 1, 3, 3, 1, 3);
        Selection beltSegment = util.select().fromTo(1,1,3, 5,1,3);
        Selection tunnelSegment = util.select().fromTo(3,2,1, 3,3,2);
        Selection axelSegment = util.select().fromTo(4,3,3, 5,3,3);
        Selection tankSegment = util.select().fromTo(3,4,3, 3,4,5).add(
                util.select().fromTo(3,1,5, 3,3,5));

        Selection forgeSegment = util.select().fromTo(0,1,1, 6,4,8)
                .substract(beltSegment).substract(tunnelSegment).substract(axelSegment).substract(tankSegment)
                .substract(motorSegment1).substract(motorSegment2);

        // Show baseplate
        scene.showBasePlate();
        scene.idle(20);

        scene.world().showSection(forgeSegment, Direction.DOWN);


        // --- Page 1: Mechanical Forge Placement ---
        scene.addKeyframe();
        scene.overlay().showText(90)
                .text("resourceful_refinement.ponder.fluid_forging.text_1")
                .pointAt(forgePos.getCenter());
        scene.idle(90);

        scene.idle(20);
        scene.world().showSection(beltSegment, Direction.DOWN);
        scene.idle(10);

        scene.overlay().showText(80)
                .text("resourceful_refinement.ponder.fluid_forging.text_2")
                .pointAt(castingDepotPos.getCenter());
        scene.idle(80);


        // --- Page 2: Mechanical Forge Inputs ---
        scene.idle(20);
        scene.addKeyframe();

        scene.world().showSection(tankSegment, Direction.DOWN);
        scene.overlay().showText(80)
                .text("resourceful_refinement.ponder.fluid_forging.text_3")
                .pointAt(forgePos.above().above().getCenter());
        scene.idle(80);

        scene.idle(10);
        scene.world().showSection(tunnelSegment, Direction.DOWN);
        scene.overlay().showText(90)
                .text("resourceful_refinement.ponder.fluid_forging.text_4")
                .pointAt(tunnel.getCenter());
        scene.idle(90);

        scene.idle(10);
        scene.world().showSection(axelSegment, Direction.DOWN);
        scene.overlay().showText(80)
                .text("resourceful_refinement.ponder.fluid_forging.text_5")
                .pointAt(axelSegment.getCenter());
        scene.idle(80);


        // --- Page 3: Mechanical Forge Crafting ---
        scene.idle(20);
        scene.addKeyframe();

        scene.world().setKineticSpeed(axelSegment.add(forgeSegment), 32);
        scene.world().setKineticSpeed(pumpSegment, 32);
        scene.overlay().showText(120)
                .text("resourceful_refinement.ponder.fluid_forging.text_6")
                .independent();

        scene.idle(30);
        ElementLink ironIngot = scene.world().createItemOnBelt(castingDepotPos, Direction.UP, Items.IRON_INGOT.getDefaultInstance());
        scene.idle(100);

        scene.world().hideSection(tankSegment, Direction.UP);
        scene.idle(10);

        scene.rotateCameraY(-180);
        scene.idle(10);

        scene.overlay().showText(80)
                .text("resourceful_refinement.ponder.fluid_forging.text_7")
                .pointAt(forgePos.getCenter());
        scene.idle(80);

        scene.rotateCameraY(180);
        scene.idle(10);


        // --- Page 4: Casting Depot ---
        scene.world().hideSection(tunnelSegment, Direction.UP);
        scene.world().hideSection(beltSegment, Direction.UP);

        scene.idle(20);
        scene.addKeyframe();

        scene.world().setBlock(castingDepotPos, ModBlocks.CASTING_DEPOT.get().defaultBlockState(), true);
        scene.world().showSection(castingDepotSegment, Direction.DOWN);

        scene.overlay().showText(90)
                .text("resourceful_refinement.ponder.fluid_forging.text_8")
                .pointAt(forgePos.getCenter());
        scene.idle(90);

        scene.idle(20);

        scene.overlay().showText(90)
                .text("resourceful_refinement.ponder.fluid_forging.text_9")
                .pointAt(forgePos.getCenter());
        scene.idle(90);

        scene.markAsFinished();

    }

    public static void coatingScene(SceneBuilder builder, SceneBuildingUtil util) {
        //Build scene
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("coating", "Coating");

        scene.configureBasePlate(0,2,7);

        scene.rotateCameraY(0);
        scene.scaleSceneView(1.5f);


        BlockPos forgePos = util.grid().at(3, 3, 4);
        BlockPos motorPos1 = util.grid().at(6, 3, 4);
        BlockPos motorPos2 = util.grid().at(4, 3, 0);
        Selection motorSegment1 = util.select().fromTo(motorPos1, motorPos1);
        Selection motorSegment2 = util.select().fromTo(motorPos2, motorPos2);

        BlockPos pumpPos = util.grid().at(3, 4, 5);
        Selection pumpSegment = util.select().fromTo(pumpPos, pumpPos);

        BlockPos castingDepotPos = util.grid().at(3, 1, 4);
        Selection castingDepotSegment = util.select().fromTo(castingDepotPos, castingDepotPos);

        BlockPos beltStartPos = util.grid().at(3, 3, 0);
        Selection beltSegment = util.select().fromTo(3,3,0, 3,3,3);

        Selection axelSegment = util.select().fromTo(4,3,4, 5,3,4);
        Selection tankSegment = util.select().fromTo(3,4,4, 3,4,6).add(
                util.select().fromTo(3,1,6, 3,3,6));

        BlockPos tankCorePos = util.grid().at(3,2,6);

        Selection forgeSegment = util.select().fromTo(0,1,1, 6,4,8)
                .substract(beltSegment).substract(motorSegment1).substract(motorSegment2);


        // Show baseplate
        scene.showBasePlate();
        scene.idle(20);

        scene.world().showSection(forgeSegment, Direction.DOWN);


        // --- Coating Placement ---
        scene.addKeyframe();
        scene.overlay().showText(120)
                .text("resourceful_refinement.ponder.coating.text_1")
                .independent();
        scene.idle(140);

        scene.overlay().showText(140)
                .text("resourceful_refinement.ponder.coating.text_2")
                .independent(0);
        scene.idle(60);

        scene.overlay().showText(80)
                .text("resourceful_refinement.ponder.coating.text_3")
                .independent(40);
        scene.idle(100);

        scene.overlay().showText(90)
                .text("resourceful_refinement.ponder.coating.text_4")
                .independent();
        scene.idle(90);

        //scene.idle(20);
        //scene.world().showSection(tankSegment, Direction.DOWN);
        scene.idle(20);

        // --- Coating Process ---
        scene.addKeyframe();
        scene.world().setKineticSpeed(pumpSegment, 24);
        scene.overlay().showText(110)
                .text("resourceful_refinement.ponder.coating.text_5")
                .pointAt(tankCorePos.getCenter());
        scene.idle(120);

        scene.idle(10);
        scene.world().showSection(beltSegment, Direction.DOWN);
        scene.idle(10);

        ElementLink<BeltItemElement> durasteelSheet = scene.world().createItemOnBelt(beltStartPos, Direction.UP, ModItems.DURASTEEL_SHEET.toStack());

        scene.overlay().showText(80)
                .text("resourceful_refinement.ponder.coating.text_6")
                .pointAt(forgePos.getCenter());
        scene.idle(70);
        scene.world().changeBeltItemTo(durasteelSheet, ItemStack.EMPTY);
        scene.idle(20);

        scene.world().modifyBlockEntity(forgePos, MechanicalForgeMouldBlockEntity.class, blockEntity -> {
            blockEntity.TriggerFalseAnimation();

            // If your method changes data that needs syncing to the client/renderer:
            blockEntity.setChanged();
            // blockEntity.getLevel().sendBlockUpdated(targetPos, blockEntity.getBlockState(), blockEntity.getBlockState(), 3);
        });

        scene.idle(60);

        scene.overlay().showText(90)
                .text("resourceful_refinement.ponder.coating.text_7")
                .pointAt(forgePos.getCenter());
        scene.idle(110);

        scene.markAsFinished();
    }

}
