package com.resourceful_refinement.ponders;

import com.resourceful_refinement.content.advanced_pump.AdvancedPumpBlock;
import com.resourceful_refinement.content.advanced_pump.AdvancedPumpBlockEntity;
import com.resourceful_refinement.content.brewers_tap.BrewersTapBlock;
import com.resourceful_refinement.content.brewers_tap.BrewersTapBlockEntity;
import com.resourceful_refinement.registry.ModItems;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.LeverBlock;


public class BrewersTapPonders {

    public static void brewersTapScene(SceneBuilder builder, SceneBuildingUtil util) {

        //Build scene
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("brewers_tap", "Pouring Drinks at a Brewer's Tap");

        scene.configureBasePlate(1,1,6);
        scene.removeShadow();

        scene.rotateCameraY(0);
        scene.scaleSceneView(1f);

        BlockPos arm1Pos = util.grid().at(3, 1, 1);
        Selection arm1Segment = util.select().fromTo(arm1Pos, arm1Pos);

        BlockPos arm2Pos = util.grid().at(1, 1, 5);
        Selection arm2Segment = util.select().fromTo(arm2Pos, arm2Pos);

        BlockPos arm1DepotPos = util.grid().at(3, 0, 0);
        Selection arm1DepotSegment = util.select().fromTo(arm1DepotPos, arm1DepotPos);

        BlockPos tapDepotPos = util.grid().at(3, 1, 3);
        Selection tapDepotSegment = util.select().fromTo(tapDepotPos, tapDepotPos);

        BlockPos tapPos = util.grid().at(3, 2, 3);
        Selection tapSegment = util.select().fromTo(tapPos, tapPos);

        BlockPos deployerPos = util.grid().at(3, 4, 3);
        Selection deployerSegment = util.select().fromTo(deployerPos, deployerPos);

        Selection tankSegment = util.select().fromTo(4,1,1,5,3,4);

        Selection kineticsSegment = util.select().fromTo(4,4,3,6,4,3).add(util.select().position(6,3,4));



        // Show baseplate
        scene.showBasePlate();
        scene.idle(20);


        // --- Page 1: Tap Placement ---
        scene.addKeyframe();
        scene.world().showSection(tapSegment, Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(100)
                .text("resourceful_refinement.ponder.brewers_tap.text_1")
                .pointAt(tapPos.getCenter().add(-0.375,0,0));
        scene.idle(120);

        scene.world().showSection(tankSegment, Direction.DOWN);

        scene.overlay().showText(80)
                .text("resourceful_refinement.ponder.brewers_tap.text_2")
                .independent();
        scene.idle(100);

        scene.world().showSection(tapDepotSegment, Direction.DOWN);

        scene.overlay().showText(70)
                .text("resourceful_refinement.ponder.brewers_tap.text_3")
                .pointAt(tapDepotPos.getCenter());

        scene.idle(90);


        // --- Page 2: Using Taps ---
        scene.addKeyframe();
        scene.world().createItemOnBeltLike(tapDepotPos, Direction.UP, ModItems.DRINKS_GLASS.toStack());

        scene.overlay().showText(100)
                .text("resourceful_refinement.ponder.brewers_tap.text_4")
                .independent();

        scene.idle(40);
        scene.overlay().showControls(tapPos.getCenter().add(-0.5,0,-0.25), Pointing.LEFT, 20).rightClick();
        scene.idle(10);
        scene.world().modifyBlock(tapPos, blockState -> {
            return blockState.setValue(BrewersTapBlock.VALVE_OPEN, true);
        }, false);
        scene.idle(30);
        scene.world().modifyBlock(tapPos, blockState -> {
            return blockState.setValue(BrewersTapBlock.VALVE_OPEN, false);
        }, false);

        scene.world().removeItemsFromBelt(tapDepotPos);
        scene.world().createItemOnBeltLike(tapDepotPos, Direction.UP, ModItems.MILKSHAKE_DRINK.toStack());
        scene.idle(40);
        scene.world().removeItemsFromBelt(tapDepotPos);
        scene.idle(20);


        // --- Page 3: Setting Flavours ---
        scene.addKeyframe();
        scene.overlay().showText(120)
                .text("resourceful_refinement.ponder.brewers_tap.text_5")
                .independent();

        scene.idle(60);
        scene.overlay().showControls(tapPos.getCenter().add(-0.5,0,-0.25), Pointing.LEFT, 40).withItem(Items.APPLE.getDefaultInstance());
        scene.idle(10);
        scene.world().modifyBlockEntity(tapPos, BrewersTapBlockEntity.class, tapBE -> {
            tapBE.flavourInv.insertItem(0, Items.APPLE.getDefaultInstance(), false);
            tapBE.setChanged();
            tapBE.sendData();
        });
        scene.idle(70);

        scene.overlay().showText(80)
                .text("resourceful_refinement.ponder.brewers_tap.text_6")
                .independent();

        scene.idle(20);
        scene.overlay().showControls(tapPos.getCenter().add(-0.5,0,-0.25), Pointing.LEFT, 40).rightClick();
        scene.idle(10);
        scene.world().modifyBlockEntity(tapPos, BrewersTapBlockEntity.class, tapBE -> {
            tapBE.flavourInv.extractItem(0,1, false);
            tapBE.setChanged();
            tapBE.sendData();
        });
        scene.idle(60);

        scene.world().createItemOnBeltLike(tapDepotPos, Direction.UP, ModItems.DRINKS_GLASS.toStack());
        scene.idle(20);

        scene.addKeyframe();
        scene.overlay().showText(80)
                .text("resourceful_refinement.ponder.brewers_tap.text_7")
                .independent();

        scene.world().modifyBlockEntity(tapPos, BrewersTapBlockEntity.class, tapBE -> {
            tapBE.flavourInv.insertItem(0, AllItems.BAR_OF_CHOCOLATE.asStack(), false);
            tapBE.setChanged();
            tapBE.sendData();
        });

        scene.idle(10);
        scene.world().modifyBlock(tapPos, blockState -> {
            return blockState.setValue(BrewersTapBlock.VALVE_OPEN, true);
        }, false);

        scene.idle(30);

        scene.world().modifyBlock(tapPos, blockState -> {
            return blockState.setValue(BrewersTapBlock.VALVE_OPEN, false);
        }, false);
        scene.world().removeItemsFromBelt(tapDepotPos);
        scene.world().createItemOnBeltLike(tapDepotPos, Direction.UP, ModItems.MILKSHAKE_DRINK.toStack());

        scene.idle(60);
        scene.world().removeItemsFromBelt(tapDepotPos);
        scene.world().modifyBlockEntity(tapPos, BrewersTapBlockEntity.class, tapBE -> {
            tapBE.flavourInv.extractItem(0,1, false);
            tapBE.setChanged();
            tapBE.sendData();
        });


        // --- Page 4: Automation ---
        scene.addKeyframe();

        scene.world().showSection(arm1Segment, Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(deployerSegment, Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(kineticsSegment, Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(arm2Segment, Direction.DOWN);
        scene.idle(20);

        scene.world().setKineticSpeed(kineticsSegment, 32);

        scene.overlay().showText(100)
                .text("resourceful_refinement.ponder.brewers_tap.text_8")
                .independent();

        scene.world().instructArm(arm1Pos, ArmBlockEntity.Phase.MOVE_TO_INPUT, ItemStack.EMPTY, 0);

        scene.idle(15);
        scene.world().instructArm(arm1Pos, ArmBlockEntity.Phase.SEARCH_OUTPUTS, ModItems.DRINKS_GLASS.toStack(), -1);

        scene.idle(15);
        scene.world().instructArm(arm1Pos, ArmBlockEntity.Phase.MOVE_TO_OUTPUT, ModItems.DRINKS_GLASS.toStack(), 0);

        scene.idle(20);
        scene.world().removeItemsFromBelt(tapDepotPos);
        scene.world().createItemOnBeltLike(tapDepotPos, Direction.UP, ModItems.DRINKS_GLASS.toStack());
        scene.world().instructArm(arm1Pos, ArmBlockEntity.Phase.SEARCH_INPUTS, ItemStack.EMPTY, -1);

        scene.idle(20);
        scene.world().moveDeployer(deployerPos, 1, 20);
        scene.idle(20);
        scene.world().moveDeployer(deployerPos, -1, 20);

        scene.world().modifyBlock(tapPos, blockState -> {
            return blockState.setValue(BrewersTapBlock.VALVE_OPEN, true);
        }, false);
        scene.idle(30);

        scene.world().removeItemsFromBelt(tapDepotPos);
        scene.world().createItemOnBeltLike(tapDepotPos, Direction.UP, ModItems.HOT_CHOCOLATE_DRINK.toStack());
        scene.idle(5);

        scene.world().instructArm(arm2Pos, ArmBlockEntity.Phase.MOVE_TO_INPUT, ItemStack.EMPTY, 0);
        scene.idle(40);

        scene.world().removeItemsFromBelt(tapDepotPos);
        scene.world().instructArm(arm2Pos, ArmBlockEntity.Phase.MOVE_TO_OUTPUT, ModItems.HOT_CHOCOLATE_DRINK.toStack(), 0);
        scene.idle(20);



        scene.markAsFinished();
    }

}
