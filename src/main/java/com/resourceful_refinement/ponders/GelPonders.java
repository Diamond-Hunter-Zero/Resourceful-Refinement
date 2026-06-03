package com.resourceful_refinement.ponders;

import com.mojang.math.Axis;
import com.resourceful_refinement.content.paint_nozzle.PaintNozzleBlock;
import com.resourceful_refinement.content.paint_nozzle.PaintNozzleBlockEntity;
import com.resourceful_refinement.content.refinery.RefineryAccessPortBlockEntity;
import com.resourceful_refinement.content.sieve.MechanicalFluidSieveBlockEntity;
import com.resourceful_refinement.registry.ModBlocks;
import com.resourceful_refinement.registry.ModItems;
import com.resourceful_refinement.registry.ModFluids;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllParticleTypes;
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
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.level.redstone.Redstone;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.lang.reflect.Field;

import static com.resourceful_refinement.content.refinery.BlenderBladeBlockEntity.computeTangentPush;


public class GelPonders {

    public static void paintNozzleScene(SceneBuilder builder, SceneBuildingUtil util) {

        //Build scene
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("paint_nozzle", "Paint Nozzle");

        scene.configureBasePlate(0,-3,12);
        scene.removeShadow();

        scene.rotateCameraY(0);
        scene.scaleSceneView(1f);

        BlockPos paintNozzlePos = util.grid().at(9, 2, 1);
        Selection paintNozzleSegment = util.select().fromTo(paintNozzlePos, paintNozzlePos);

        BlockPos pumpPos = util.grid().at(10, 3, 2);
        Selection pumpSegment = util.select().fromTo(pumpPos, pumpPos);

        BlockPos tankPos1 = util.grid().at(10, 1, 3);
        BlockPos tankPos2 = util.grid().at(10, 2, 3);
        Selection tankSegment = util.select().fromTo(tankPos1, tankPos2);

        Selection gelSegment = util.select().fromTo(0,1,0, 2,1,2);
        Selection gelBaseSegment = util.select().fromTo(0,0,0, 2,0,2);
        Selection pipesSegment = util.select().fromTo(10,1,1, 10,3,3);

        Selection frontBaseplateSegement = util.select().fromTo(0,0,5, 5,0,5);
        Selection backBaseplateSegment = util.select().fromTo(6,0,5, 11,0,5);

        // Show baseplate
        scene.showBasePlate();
        scene.idle(20);

        scene.world().showSection(pipesSegment, Direction.DOWN);
        scene.idle(20);


        // --- Page 1: Paint Nozzle ---
        scene.addKeyframe();
        scene.overlay().showText(90)
                .text("resourceful_refinement.ponder.paint_nozzle.text_1")
                .pointAt(pipesSegment.getCenter().add(-0.5,0,0));
        scene.idle(70);
        scene.world().showSection(paintNozzleSegment, Direction.DOWN);
        scene.idle(40);

        scene.addKeyframe();
        scene.overlay().showControls(paintNozzleSegment.getCenter().add(-0.5,0,0), Pointing.LEFT, 80).withItem(AllItems.WRENCH.asStack());
        scene.overlay().showText(80)
                .text("resourceful_refinement.ponder.paint_nozzle.text_2")
                .independent();

        scene.idle(100);


        scene.world().setKineticSpeed(pumpSegment, 32);
        scene.effects().rotationSpeedIndicator(pumpPos);
        scene.idle(20);

        scene.addKeyframe();
        scene.overlay().showControls(paintNozzleSegment.getCenter().add(-0.5,0,0), Pointing.LEFT, 80).withItem(ModItems.PAINT_NOZZLE_ITEM.toStack());
        scene.overlay().showText(80)
                .text("resourceful_refinement.ponder.paint_nozzle.text_3")
                .independent();

        scene.idle(100);


        // --- Page 2: Fluids into Gels ---
        scene.addKeyframe();

        scene.world().modifyBlock(paintNozzlePos, blockState -> blockState.setValue(PaintNozzleBlock.VALVE_OPEN, true), false);
        scene.idle(20);

        scene.overlay().showText(80)
                .text("resourceful_refinement.ponder.paint_nozzle.text_4")
                .pointAt(paintNozzlePos.getCenter().add(-0.5,0,0));

        // Spray gel
        Vec3 sprayStart = util.vector().centerOf(paintNozzlePos);
        Vec3 sprayEnd = util.vector().centerOf(util.grid().at(1, 1, 1));

        PonderGelSprayHelper.playGelSpray(scene, sprayStart, sprayEnd, 22, ModFluids.GREEN_PAINT.color, 50,
                () -> {
                    scene.world().showSection(gelSegment, Direction.UP);
                    //scene.world().setBlocks(gelSegment, ModBlocks.GEL_SPLATTER.get().defaultBlockState()., false);
                });

        scene.idle(20);


        // --- Page 3: Paint Mechanics ---
        scene.world().hideSection(gelSegment, Direction.UP);
        scene.idle(20);
        scene.world().setBlocks(gelBaseSegment, Blocks.WHITE_CONCRETE.defaultBlockState(), true);
        scene.addKeyframe();


        scene.overlay().showText(90)
                .text("resourceful_refinement.ponder.paint_nozzle.text_5")
                .independent();
        scene.idle(30);
        PonderGelSprayHelper.playGelSpray(scene, sprayStart, sprayEnd, 22, ModFluids.RED_PAINT.color, 50,
                () -> {
                    scene.world().setBlocks(gelBaseSegment, Blocks.RED_CONCRETE.defaultBlockState(), false);
                });
        scene.idle(30);


        scene.overlay().showText(180)
                .text("resourceful_refinement.ponder.paint_nozzle.text_6")
                .independent();

        scene.world().setBlocks(gelBaseSegment, Blocks.WHITE_STAINED_GLASS.defaultBlockState(), true);
        scene.idle(15);
        PonderGelSprayHelper.playGelSpray(scene, sprayStart, sprayEnd, 22, ModFluids.YELLOW_PAINT.color, 30,
                () -> {
                    scene.world().setBlocks(gelBaseSegment, Blocks.YELLOW_STAINED_GLASS.defaultBlockState(), false);
                });
        scene.idle(10);

        scene.world().setBlocks(gelBaseSegment, Blocks.WHITE_TERRACOTTA.defaultBlockState(), true);
        scene.idle(15);
        PonderGelSprayHelper.playGelSpray(scene, sprayStart, sprayEnd, 22, ModFluids.BLUE_PAINT.color, 30,
                () -> {
                    scene.world().setBlocks(gelBaseSegment, Blocks.BLUE_TERRACOTTA.defaultBlockState(), false);
                });
        scene.idle(10);

        scene.world().setBlocks(gelBaseSegment, Blocks.WHITE_WOOL.defaultBlockState(), true);
        scene.idle(15);
        PonderGelSprayHelper.playGelSpray(scene, sprayStart, sprayEnd, 22, ModFluids.MAGENTA_PAINT.color, 30,
                () -> {
                    scene.world().setBlocks(gelBaseSegment, Blocks.MAGENTA_WOOL.defaultBlockState(), false);
                });
        scene.idle(20);

        scene.world().setBlocks(gelBaseSegment, Blocks.WHITE_CONCRETE.defaultBlockState(), true);
        scene.idle(10);

        scene.addKeyframe();
        scene.overlay().showText(80)
                .text("resourceful_refinement.ponder.paint_nozzle.text_7")
                .independent();

        var sheepLink = scene.world().createEntity(level -> {
            Sheep sheepMob = new Sheep(EntityType.SHEEP, level);
            sheepMob.setPos(1.5, 1, 1.5);
            sheepMob.setYRot(225f);
            sheepMob.setNoAi(true);
            return sheepMob;
        });

        scene.idle(20);

        PonderGelSprayHelper.playGelSpray(scene, sprayStart, sprayEnd, 22, ModFluids.ORANGE_PAINT.color, 8,
                () -> {
                    scene.world().modifyEntity(sheepLink, entity -> {
                        if (entity instanceof Sheep sheepEntity)
                        {
                            sheepEntity.setColor(DyeColor.ORANGE);
                        }
                    });
                });

        scene.idle(50);

        scene.markAsFinished();
    }

    public static void gelPropertiesScene(SceneBuilder builder, SceneBuildingUtil util) {

        //Build scene
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("gel_splatters", "Gel Splatters");

        scene.configureBasePlate(0,-2,14);
        scene.removeShadow();

        scene.scaleSceneView(1f);

        BlockPos paintNozzlePos = util.grid().at(10, 2, 6);
        Selection paintNozzleSegment = util.select().fromTo(paintNozzlePos, paintNozzlePos);

        BlockPos pumpPos = util.grid().at(9, 2, 7);
        Selection pumpSegment = util.select().fromTo(pumpPos, pumpPos);

        BlockPos depotPos = util.grid().at(4, 1, 7);
        Selection depotSegment = util.select().fromTo(depotPos, depotPos);

        Selection pipesSegment = util.select().fromTo(7,1,7, 11,2,7);

        Selection showerPumpSegment = util.select().fromTo(2, 1, 12, 2, 4, 12);

        BlockPos showerPaintNozzlePos = util.grid().at(2, 3, 11);
        Selection showerPaintNozzleSegment = util.select().fromTo(showerPaintNozzlePos, showerPaintNozzlePos);
        Selection showerSegment = util.select().fromTo(1, 1, 10, 2, 5, 14);

        Selection yellowGelSegment = util.select().fromTo(10,1,1, 11,1,4);
        Selection speedyGelSegment = util.select().fromTo(7,1,1, 8,1,4);
        Selection bouncyGelSegment = util.select().fromTo(4,1,1, 5,1,4);
        Selection moltenGelSegment = util.select().fromTo(1,1,1, 2,1,4);
        Selection waterGelSegment = util.select().fromTo(10,1,9, 11,1,12);
        Selection concreteGelSegment = util.select().fromTo(7,0,9, 8,0,12);
        Selection potionGelSegment = util.select().fromTo(4,1,9, 5,1,12);

        Vec3 speedyGelStart = new Vec3(8,1,5);
        Vec3 speedyGelEnd = new Vec3(8,1,1);
        Vec3 bouncyGelStart = new Vec3(5,1,5);
        Vec3 bouncyGelEnd = new Vec3(5,1,1);


        Selection frontBaseplateSegement = util.select().fromTo(0,0,0, 12,0,7);
        Selection backBaseplateSegment = util.select().fromTo(0,0,8, 12,0,13);

        Selection frontSceneSelection = util.select().fromTo(0,0,0, 12,6,7);
        Selection frontSceneWithoutDepotSelection = util.select().fromTo(0,0,0, 12,6,7).substract(depotSegment);
        Selection backSceneSelection = util.select().fromTo(0,0,8, 12,6,13);
        Selection backSceneWithoutShowerSelection = util.select().fromTo(0,0,8, 12,6,13).substract(showerSegment);

        // Show baseplate
        scene.world().showSection(frontBaseplateSegement, Direction.UP);
        scene.idle(20);

        scene.world().showSection(pipesSegment, Direction.DOWN);
        scene.world().showSection(paintNozzleSegment, Direction.DOWN);
        var hosegunLink = scene.world().createEntity(level -> {
            Display.ItemDisplay hosegunItem = new Display.ItemDisplay(EntityType.ITEM_DISPLAY, level);
            hosegunItem.setPos(depotPos.above().getCenter().add(-1,0,-1));
            try
            {
                Field itemStackAccessorField = Display.ItemDisplay.class.getDeclaredField("DATA_ITEM_STACK_ID");
                Field scaleAccessorField = Display.class.getDeclaredField("DATA_SCALE_ID");
                Field rotationAccessorField = Display.class.getDeclaredField("DATA_RIGHT_ROTATION_ID");
                itemStackAccessorField.setAccessible(true);
                scaleAccessorField.setAccessible(true);
                rotationAccessorField.setAccessible(true);
                @SuppressWarnings("unchecked")
                EntityDataAccessor<ItemStack> dataItemStackId = (EntityDataAccessor<ItemStack>) itemStackAccessorField.get(null);
                @SuppressWarnings("unchecked")
                EntityDataAccessor<Vector3f> dataScaleId = (EntityDataAccessor<Vector3f>) scaleAccessorField.get(null);
                @SuppressWarnings("unchecked")
                EntityDataAccessor<Quaternionf> dataRotationId = (EntityDataAccessor<Quaternionf>) rotationAccessorField.get(null);

                ItemStack hosegunStack = ModItems.HOSEGUN.toStack();

                // Update the entity's data tracker
                hosegunItem.getEntityData().set(dataItemStackId, hosegunStack);
                hosegunItem.getEntityData().set(dataScaleId, new Vector3f(2,2,2));
                Quaternionf myRotation = new Quaternionf()
                        .mul(Axis.ZP.rotationDegrees(0))
                        .mul(Axis.XP.rotationDegrees(0))
                        .mul(Axis.YP.rotationDegrees(-90));
                hosegunItem.getEntityData().set(dataRotationId, myRotation);
                //hosegunItem.lerpTo(depotPos.getX(), depotPos.getY(), depotPos.getZ(), -90, 0, 1);
            } catch (NoSuchFieldException | IllegalAccessException e)
            {
                e.printStackTrace();
            }

            return hosegunItem;
        });
        scene.idle(20);


        // --- Page 1: Gel sources ---
        scene.addKeyframe();
        scene.overlay().showText(100)
                .text("resourceful_refinement.ponder.gel_splatters.text_1")
                .independent();
        scene.idle(120);


        scene.addKeyframe();
        scene.world().setKineticSpeed(pumpSegment, 24);
        scene.overlay().showControls(paintNozzleSegment.getCenter().add(0,0.5,0), Pointing.DOWN, 90).withItem(ModBlocks.PAINT_NOZZLE.toStack());
        scene.overlay().showText(90)
                .text("resourceful_refinement.ponder.gel_splatters.text_2")
                .independent();

        scene.idle(10);
        PonderGelSprayHelper.playGelSpray(scene, paintNozzleSegment.getCenter(), yellowGelSegment.getCenter(), 22, ModFluids.CATALYSED_GOLD.color, 70,
                () -> {
                    scene.world().showSection(yellowGelSegment, Direction.UP);
                });

        scene.idle(20);

        scene.overlay().showText(100)
                .text("resourceful_refinement.ponder.gel_splatters.text_3")
                .independent();

        scene.idle(110);

        scene.world().showSection(speedyGelSegment, Direction.DOWN);
        scene.idle(20);

        // --- Page 2: Gel variations ---
        scene.addKeyframe();

        scene.overlay().showText(80)
                .text("resourceful_refinement.ponder.gel_splatters.text_4")
                .independent();

        scene.idle(20);
        PonderGelSprayHelper.playTravelingItem(scene, AllBlocks.ANDESITE_CASING.asStack(), speedyGelStart, speedyGelEnd, 12, 0);
        scene.idle(5);
        PonderGelSprayHelper.playTravelingItem(scene, AllBlocks.ANDESITE_CASING.asStack(), speedyGelStart, speedyGelEnd, 12, 0);
        scene.idle(5);
        PonderGelSprayHelper.playTravelingItem(scene, AllBlocks.ANDESITE_CASING.asStack(), speedyGelStart, speedyGelEnd, 12, 0);
        scene.idle(5);

        scene.world().showSection(bouncyGelSegment, Direction.DOWN);
        scene.idle(20);
        scene.addKeyframe();

        scene.overlay().showText(80)
                .text("resourceful_refinement.ponder.gel_splatters.text_5")
                .independent();

        scene.idle(20);
        PonderGelSprayHelper.playTravelingItem(scene, AllBlocks.ANDESITE_CASING.asStack(), bouncyGelStart, bouncyGelEnd, 45, 4);
        scene.idle(15);

        scene.world().showSection(moltenGelSegment, Direction.DOWN);
        scene.idle(20);
        scene.addKeyframe();

        scene.overlay().showText(80)
                .text("resourceful_refinement.ponder.gel_splatters.text_6")
                .independent();

        scene.idle(10);
        var sheepLink = scene.world().createEntity(level -> {
            Sheep sheepMob = new Sheep(EntityType.SHEEP, level);
            sheepMob.setPos(moltenGelSegment.getCenter().add(0,-0.5,0));
            sheepMob.setYRot(180);
            sheepMob.setNoAi(true);
            sheepMob.setColor(DyeColor.PINK);

            return sheepMob;
        });

        for (int age = 0; age <= 50; age++)
        {
            scene.world().modifyEntity(sheepLink, entity -> {
                entity.igniteForSeconds(0.1f);
            });
            scene.idle(1);
        }

        scene.world().modifyEntity(sheepLink, Entity::discard);

        ElementLink<EntityElement> muttonItem = scene.world().createItemEntity(moltenGelSegment.getCenter(), Vec3.ZERO, Items.MUTTON.getDefaultInstance());
        scene.idle(20);
        scene.world().modifyEntity(muttonItem, Entity::discard);

        scene.idle(10);

        scene.world().modifyEntity(hosegunLink, Entity::discard);
        scene.world().hideSection(frontSceneWithoutDepotSelection, Direction.UP);
        scene.idle(30);
        var backSceneLink = scene.world().showIndependentSectionImmediately(backSceneWithoutShowerSelection);
        scene.world().moveSection(backSceneLink, new Vec3(0,0,-6), 0);

        // --- Page 3: Non-splatter gels ---
        scene.addKeyframe();

        scene.overlay().showText(80)
                .text("resourceful_refinement.ponder.gel_splatters.text_7")
                .independent();
        scene.idle(100);

        scene.overlay().showText(80)
                .text("resourceful_refinement.ponder.gel_splatters.text_8")
                .independent();
        scene.idle(20);

        PonderGelSprayHelper.playGelSpray(scene, waterGelSegment.getCenter().add(0,3,-6), waterGelSegment.getCenter().add(0,0,-6), 22, ModFluids.BLUE_PAINT.color, 10,
                () -> {
                    scene.world().setBlocks(waterGelSegment, Blocks.AIR.defaultBlockState(), false);
                });
        scene.idle(70);

        scene.addKeyframe();
        scene.overlay().showText(80)
                .text("resourceful_refinement.ponder.gel_splatters.text_9")
                .independent();
        scene.idle(20);

        PonderGelSprayHelper.playGelSpray(scene, concreteGelSegment.getCenter().add(0,4,-6), concreteGelSegment.getCenter().add(0,0,-6), 22, ModFluids.PURPLE_PAINT.color, 30,
                () -> {
                    scene.world().setBlocks(concreteGelSegment, Blocks.PURPLE_CONCRETE.defaultBlockState(), false);
                });
        scene.idle(50);

        scene.addKeyframe();
        scene.overlay().showText(80)
                .text("resourceful_refinement.ponder.gel_splatters.text_10")
                .independent();
        scene.idle(20);

        PonderGelSprayHelper.playGelSpray(scene, potionGelSegment.getCenter().add(0,3,-6), potionGelSegment.getCenter().add(0,0,-6), 22, ModFluids.GREEN_PAINT.color, 10,
                () -> {
                    ParticleEmitter fx = scene.effects().simpleParticleEmitter(ParticleTypes.EFFECT, util.vector().of(0, .1, 0));
                    scene.effects().emitParticles(potionGelSegment.getCenter().add(0,0,-6), fx, 1, 16);
                    scene.effects().emitParticles(potionGelSegment.getCenter().add(-1,0,-6), fx, 2, 16);
                    scene.effects().emitParticles(potionGelSegment.getCenter().add(1,0,-6), fx, 2, 16);
                    scene.effects().emitParticles(potionGelSegment.getCenter().add(0,0,-5), fx, 1, 16);
                    scene.effects().emitParticles(potionGelSegment.getCenter().add(0,0,-7), fx, 1, 16);
                });
        scene.idle(70);


        // --- Page 4: Drains ---
        scene.addKeyframe();
        var showerLink = scene.world().showIndependentSection(showerSegment, Direction.DOWN);
        scene.world().moveSection(showerLink, new Vec3(0,0,-6), 0);
        scene.idle(20);

        scene.overlay().showText(100)
                .text("resourceful_refinement.ponder.gel_splatters.text_11")
                .independent();
        scene.idle(20);

        scene.world().setKineticSpeed(showerPumpSegment, 24);
        PonderGelSprayHelper.playGelSpray(scene, showerPaintNozzlePos.getCenter().add(0,0,-6), showerPaintNozzlePos.getCenter().add(0,-2,-6), 12,
                ModFluids.LIME_PAINT.color, 100, () -> {});

        scene.world().setKineticSpeed(showerPumpSegment, 0);


        scene.markAsFinished();
    }

    public static void hosegunBasicsScene(SceneBuilder builder, SceneBuildingUtil util) {

        //Build scene
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("hosegun", "Hosegun");

        scene.configureBasePlate(0,0,6);

        scene.scaleSceneView(1f);

        BlockPos spoutPos = util.grid().at(4, 3, 1);
        Selection spoutSegment = util.select().fromTo(spoutPos, spoutPos);

        BlockPos pumpPos = util.grid().at(4, 3, 2);
        Selection pumpSegment = util.select().fromTo(pumpPos, pumpPos);

        BlockPos depotPos = util.grid().at(4, 1, 1);
        Selection depotSegment = util.select().fromTo(depotPos, depotPos);

        BlockPos drainPos = util.grid().at(1, 1, 4);
        Selection drainSegment = util.select().fromTo(drainPos, drainPos);

        Selection pipesSegment = util.select().fromTo(4,3,1,4,3,3);
        Selection gelSegment = util.select().fromTo(0,1,0, 2,1,2);


        // Show baseplate
        scene.showBasePlate();
        scene.idle(20);

        var hosegunLink = scene.world().createEntity(level -> {
            Display.ItemDisplay hosegunItem = new Display.ItemDisplay(EntityType.ITEM_DISPLAY, level);
            hosegunItem.setPos(new Vec3(2,3,3));
            try
            {
                Field itemStackAccessorField = Display.ItemDisplay.class.getDeclaredField("DATA_ITEM_STACK_ID");
                Field scaleAccessorField = Display.class.getDeclaredField("DATA_SCALE_ID");
                Field rotationAccessorField = Display.class.getDeclaredField("DATA_RIGHT_ROTATION_ID");
                itemStackAccessorField.setAccessible(true);
                scaleAccessorField.setAccessible(true);
                rotationAccessorField.setAccessible(true);
                @SuppressWarnings("unchecked")
                EntityDataAccessor<ItemStack> dataItemStackId = (EntityDataAccessor<ItemStack>) itemStackAccessorField.get(null);
                @SuppressWarnings("unchecked")
                EntityDataAccessor<Vector3f> dataScaleId = (EntityDataAccessor<Vector3f>) scaleAccessorField.get(null);
                @SuppressWarnings("unchecked")
                EntityDataAccessor<Quaternionf> dataRotationId = (EntityDataAccessor<Quaternionf>) rotationAccessorField.get(null);

                ItemStack hosegunStack = ModItems.HOSEGUN.toStack();

                // Update the entity's data tracker
                hosegunItem.getEntityData().set(dataItemStackId, hosegunStack);
                hosegunItem.getEntityData().set(dataScaleId, new Vector3f(2,2,2));
                Quaternionf myRotation = new Quaternionf()
                        .mul(Axis.ZP.rotationDegrees(0))
                        .mul(Axis.XP.rotationDegrees(0))
                        .mul(Axis.YP.rotationDegrees(0));
                hosegunItem.getEntityData().set(dataRotationId, myRotation);
            } catch (NoSuchFieldException | IllegalAccessException e)
            {
                e.printStackTrace();
            }

            return hosegunItem;
        });


        // --- Page 1: Hosegun Filling ---
        scene.addKeyframe();
        scene.overlay().showText(100)
                .text("resourceful_refinement.ponder.hosegun.text_1")
                .independent();
        scene.idle(100);
        scene.world().modifyEntity(hosegunLink, Entity::discard);
        scene.idle(20);

        scene.world().showSection(depotSegment, Direction.DOWN);
        scene.idle(20);
        scene.world().showSection(pipesSegment, Direction.DOWN);
        scene.idle(20);

        scene.addKeyframe();
        scene.overlay().showText(90)
                .text("resourceful_refinement.ponder.hosegun.text_2")
                .pointAt(pipesSegment.getCenter());
        scene.idle(20);
        scene.world().setKineticSpeed(pumpSegment, 24);

        ParticleEmitter fx = scene.effects().simpleParticleEmitter(ParticleTypes.FALLING_LAVA, util.vector().of(0, -0.4, 0));
        scene.effects().emitParticles(spoutPos.getCenter().add(0,-0.5,0), fx, 1, 90);
        scene.idle(90);

        scene.overlay().showText(80)
                .text("resourceful_refinement.ponder.hosegun.text_3")
                .independent();
        scene.idle(90);

        scene.world().hideSection(pipesSegment, Direction.UP);
        scene.idle(20);
        scene.world().hideSection(depotSegment, Direction.UP);
        scene.idle(20);


        // --- Page 2: Hosegun Firing ---
        var hosegunLink2 = scene.world().createEntity(level -> {
            Display.ItemDisplay hosegunItem = new Display.ItemDisplay(EntityType.ITEM_DISPLAY, level);
            hosegunItem.setPos(new Vec3(3.66,2,4));
            try
            {
                Field itemStackAccessorField = Display.ItemDisplay.class.getDeclaredField("DATA_ITEM_STACK_ID");
                Field scaleAccessorField = Display.class.getDeclaredField("DATA_SCALE_ID");
                Field rotationAccessorField = Display.class.getDeclaredField("DATA_RIGHT_ROTATION_ID");
                itemStackAccessorField.setAccessible(true);
                scaleAccessorField.setAccessible(true);
                rotationAccessorField.setAccessible(true);
                @SuppressWarnings("unchecked")
                EntityDataAccessor<ItemStack> dataItemStackId = (EntityDataAccessor<ItemStack>) itemStackAccessorField.get(null);
                @SuppressWarnings("unchecked")
                EntityDataAccessor<Vector3f> dataScaleId = (EntityDataAccessor<Vector3f>) scaleAccessorField.get(null);
                @SuppressWarnings("unchecked")
                EntityDataAccessor<Quaternionf> dataRotationId = (EntityDataAccessor<Quaternionf>) rotationAccessorField.get(null);

                ItemStack hosegunStack = ModItems.HOSEGUN.toStack();

                // Update the entity's data tracker
                hosegunItem.getEntityData().set(dataItemStackId, hosegunStack);
                hosegunItem.getEntityData().set(dataScaleId, new Vector3f(2,2,2));
                Quaternionf myRotation = new Quaternionf()
                        .mul(Axis.ZP.rotationDegrees(0))
                        .mul(Axis.XP.rotationDegrees(0))
                        .mul(Axis.YP.rotationDegrees(-90));
                hosegunItem.getEntityData().set(dataRotationId, myRotation);
                //hosegunItem.lerpTo(depotPos.getX(), depotPos.getY(), depotPos.getZ(), -90, 0, 1);
            } catch (NoSuchFieldException | IllegalAccessException e)
            {
                e.printStackTrace();
            }

            return hosegunItem;
        });

        scene.addKeyframe();
        scene.overlay().showText(90)
                .text("resourceful_refinement.ponder.hosegun.text_4")
                .independent();

        scene.idle(15);

        scene.world().modifyEntity(hosegunLink2, entity -> {
            if (entity instanceof Display.ItemDisplay itemDisplay)
            {
                try
                {
                    Field rotationAccessorField = Display.class.getDeclaredField("DATA_RIGHT_ROTATION_ID");
                    rotationAccessorField.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    EntityDataAccessor<Quaternionf> dataRotationId = (EntityDataAccessor<Quaternionf>) rotationAccessorField.get(null);

                    // Update the entity's data tracker
                    Quaternionf myRotation = new Quaternionf()
                            .mul(Axis.ZP.rotationDegrees(0))
                            .mul(Axis.XP.rotationDegrees(0))
                            .mul(Axis.YP.rotationDegrees(-45));
                    itemDisplay.getEntityData().set(dataRotationId, myRotation);

                } catch (NoSuchFieldException | IllegalAccessException e)
                {
                    e.printStackTrace();
                }
            }
        });
        scene.idle(20);
        scene.overlay().showControls(new Vec3(4.5,2,4.0), Pointing.DOWN, 70).rightClick();
        scene.idle(70);

        scene.overlay().showText(80)
                .text("resourceful_refinement.ponder.hosegun.text_5")
                .independent();

        PonderGelSprayHelper.playGelSpray(scene, new Vec3(4.25,1.2,4), gelSegment.getCenter(), 22, ModFluids.MOLTEN_ASURINE.color, 60,
                () -> {
                    scene.world().showIndependentSectionImmediately(gelSegment);
                    //scene.world().showSection(gelSegment, Direction.UP);
                });
        scene.idle(40);


        // --- Page 3: Hosegun Emptying ---
        scene.world().modifyEntity(hosegunLink2, Entity::discard);
        scene.world().setBlocks(gelSegment, Blocks.AIR.defaultBlockState(), true);
        scene.idle(20);
        scene.world().showSection(drainSegment, Direction.DOWN);
        scene.idle(20);
        scene.addKeyframe();

        scene.overlay().showText(90)
                .text("resourceful_refinement.ponder.hosegun.text_6")
                .independent();
        scene.overlay().showControls(drainPos.above().getCenter().add(0.25,0,-0.5), Pointing.RIGHT, 80).withItem(ModItems.HOSEGUN.toStack());

        scene.idle(110);

        scene.markAsFinished();
    }

    public static void refillStationBasicsScene(SceneBuilder builder, SceneBuildingUtil util) {

        //Build scene
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("refill_station_basics", "Fluid Refill Station");

        scene.configureBasePlate(0,0,7);

        scene.scaleSceneView(1f);

        BlockPos refillStationPos = util.grid().at(3, 2, 3);
        Selection refillStationSegment = util.select().fromTo(refillStationPos, refillStationPos);

        BlockPos displayLinkPos = util.grid().at(2, 2, 3);
        Selection displayLinkSegment = util.select().fromTo(displayLinkPos, displayLinkPos);

        BlockPos pumpPos = util.grid().at(5, 1, 2);
        Selection pumpSegment = util.select().fromTo(pumpPos, pumpPos);

        Selection pipesSegment = util.select().fromTo(5,1,2,3,1,3);
        Selection tanksSegment = util.select().fromTo(5,1,1, 5,3,1);
        Selection displayBoardSegment = util.select().fromTo(0,1,6, 4,3,6);


        // Show baseplate
        scene.showBasePlate();
        scene.idle(20);

        scene.world().showSection(refillStationSegment, Direction.DOWN);
        scene.idle(20);

        // --- Page 1: Refill Station basics ---
        scene.addKeyframe();
        scene.overlay().showText(130)
                .text("resourceful_refinement.ponder.refill_station_basics.text_1")
                .pointAt(refillStationPos.getCenter());
        scene.idle(150);

        scene.world().showSection(tanksSegment, Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(pipesSegment, Direction.UP);
        scene.idle(10);

        scene.overlay().showText(90)
                .text("resourceful_refinement.ponder.refill_station_basics.text_2")
                .pointAt(refillStationPos.getCenter());
        scene.idle(20);
        scene.world().setKineticSpeed(pumpSegment, 24);
        scene.idle(70);
        scene.world().setKineticSpeed(pumpSegment, 0);
        scene.idle(10);

        // --- Page 2: Filling from stations ---
        scene.addKeyframe();
        scene.overlay().showText(80)
                .text("resourceful_refinement.ponder.refill_station_basics.text_3")
                .independent();
        scene.idle(100);

        scene.overlay().showText(80)
                .text("resourceful_refinement.ponder.refill_station_basics.text_4")
                .independent();
        scene.overlay().showControls(refillStationPos.getCenter().add(-0.5, 0, 0.5), Pointing.LEFT, 80).withItem(Items.BUCKET.getDefaultInstance());
        scene.idle(100);


        // --- Page 3: Display links ---
        scene.addKeyframe();
        scene.overlay().showText(120)
                .text("resourceful_refinement.ponder.refill_station_basics.text_5")
                .independent();
        scene.idle(20);
        scene.world().showSection(displayLinkSegment, Direction.DOWN);
        scene.idle(20);
        scene.world().showSection(displayBoardSegment, Direction.DOWN);
        scene.idle(20);
        scene.world().setKineticSpeed(displayBoardSegment, 24);
        scene.world().createEntity(level -> {
            Display.TextDisplay entity = new Display.TextDisplay(EntityType.TEXT_DISPLAY, level);
            entity.setPos(displayBoardSegment.getCenter().add(-1.25,2,-1.5));
            entity.setYRot(0);
            try
            {
                Field textAccessorField = Display.TextDisplay.class.getDeclaredField("DATA_TEXT_ID");
                Field widthAccessorField = Display.TextDisplay.class.getDeclaredField("DATA_LINE_WIDTH_ID");
                Field rotationAccessorField = Display.class.getDeclaredField("DATA_RIGHT_ROTATION_ID");
                textAccessorField.setAccessible(true);
                widthAccessorField.setAccessible(true);
                rotationAccessorField.setAccessible(true);
                @SuppressWarnings("unchecked")
                EntityDataAccessor<Component> dataTextId = (EntityDataAccessor<Component>) textAccessorField.get(null);
                @SuppressWarnings("unchecked")
                EntityDataAccessor<Integer> dataWidthId = (EntityDataAccessor<Integer>) widthAccessorField.get(null);
                @SuppressWarnings("unchecked")
                EntityDataAccessor<Quaternionf> dataRotationId = (EntityDataAccessor<Quaternionf>) rotationAccessorField.get(null);

                // Update the entity's data tracker
                entity.getEntityData().set(dataTextId, Component.literal("Purified Copper: 16000mb"));
                entity.getEntityData().set(dataWidthId, 160);
                Quaternionf myRotation = new Quaternionf()
                        .mul(Axis.ZP.rotationDegrees(0))
                        .mul(Axis.XP.rotationDegrees(0))
                        .mul(Axis.YP.rotationDegrees(180));
                entity.getEntityData().set(dataRotationId, myRotation);

            } catch (NoSuchFieldException | IllegalAccessException e)
            {
                e.printStackTrace();
            }

            return entity;
        });

        scene.idle(80);

        scene.markAsFinished();
    }

    public static void refillStationTrackingScene(SceneBuilder builder, SceneBuildingUtil util) {

        //Build scene
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("binding_refills", "Tracking Gels with Refill Stations");

        scene.configureBasePlate(0,0,9);

        scene.scaleSceneView(1f);

        BlockPos refillStationLeftPos = util.grid().at(3, 2, 6);
        Selection refillStationLeftSegment = util.select().fromTo(refillStationLeftPos, refillStationLeftPos.below());

        BlockPos refillStationRightPos = util.grid().at(1, 2, 6);
        Selection refillStationRightSegment = util.select().fromTo(refillStationRightPos, refillStationRightPos.below());

        BlockPos displayLinkLeftPos = util.grid().at(4, 2, 6);
        Selection displayLinkLeftSegment = util.select().fromTo(displayLinkLeftPos, displayLinkLeftPos);

        BlockPos displayLinkRightPos = util.grid().at(0, 2, 6);
        Selection displayLinkRightSegment = util.select().fromTo(displayLinkRightPos, displayLinkRightPos);

        BlockPos leverPos = util.grid().at(2, 1, 5);
        BlockPos lampPos = util.grid().at(2, 1, 6);
        BlockPos dustPos = util.grid().at(2, 2, 6);
        Selection redstoneSegment = util.select().fromTo(2,1,5,2,2,6);


        BlockPos pumpPos = util.grid().at(4, 3, 7);
        Selection pumpSegment = util.select().fromTo(pumpPos, pumpPos);

        Selection pipesSegment = util.select().fromTo(1,3,6,5,3,7);
        Selection tanksSegment = util.select().fromTo(6,1,7, 7,3,8);
        Selection displayBoardSegment = util.select().fromTo(8,1,1, 8,3,5);
        Selection displayBoardOutlineSegment = util.select().fromTo(8,2,1, 8,3,4);
        Selection gelSegment = util.select().fromTo(1,1,1,3,1,3);

        Object outlineHighlight = new Object();

        // Show baseplate
        scene.showBasePlate();
        scene.idle(20);

        scene.world().showSection(refillStationLeftSegment, Direction.DOWN);
        scene.idle(20);

        // --- Page 1: Refill Station IDs ---
        scene.addKeyframe();
        scene.overlay().showText(120)
                .text("resourceful_refinement.ponder.binding_refills.text_1")
                .pointAt(refillStationLeftSegment.getCenter());
        scene.idle(70);
        scene.overlay().showControls(refillStationLeftSegment.getCenter().add(1,0,-0), Pointing.RIGHT, 50).rightClick();
        scene.idle(65);

        scene.world().showSection(refillStationRightSegment, Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(80)
                .text("resourceful_refinement.ponder.binding_refills.text_2")
                .pointAt(refillStationRightSegment.getCenter());
        scene.idle(100);


        scene.world().showSection(tanksSegment, Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(pipesSegment, Direction.DOWN);
        scene.idle(20);
        scene.world().setKineticSpeed(pumpSegment, 24);
        scene.idle(10);


        // --- Page 2: Binding Hoseguns ---
        scene.addKeyframe();

        scene.overlay().showText(130)
                .text("resourceful_refinement.ponder.binding_refills.text_3")
                .independent();

        scene.idle(40);
        var hosegunLink = scene.world().createEntity(level -> {
            Display.ItemDisplay hosegunItem = new Display.ItemDisplay(EntityType.ITEM_DISPLAY, level);
            hosegunItem.setPos(new Vec3(4.66,2,5));
            try
            {
                Field itemStackAccessorField = Display.ItemDisplay.class.getDeclaredField("DATA_ITEM_STACK_ID");
                Field scaleAccessorField = Display.class.getDeclaredField("DATA_SCALE_ID");
                Field rotationAccessorField = Display.class.getDeclaredField("DATA_RIGHT_ROTATION_ID");
                itemStackAccessorField.setAccessible(true);
                scaleAccessorField.setAccessible(true);
                rotationAccessorField.setAccessible(true);
                @SuppressWarnings("unchecked")
                EntityDataAccessor<ItemStack> dataItemStackId = (EntityDataAccessor<ItemStack>) itemStackAccessorField.get(null);
                @SuppressWarnings("unchecked")
                EntityDataAccessor<Vector3f> dataScaleId = (EntityDataAccessor<Vector3f>) scaleAccessorField.get(null);
                @SuppressWarnings("unchecked")
                EntityDataAccessor<Quaternionf> dataRotationId = (EntityDataAccessor<Quaternionf>) rotationAccessorField.get(null);

                ItemStack hosegunStack = ModItems.HOSEGUN.toStack();

                // Update the entity's data tracker
                hosegunItem.getEntityData().set(dataItemStackId, hosegunStack);
                hosegunItem.getEntityData().set(dataScaleId, new Vector3f(2,2,2));
                Quaternionf myRotation = new Quaternionf()
                        .mul(Axis.ZP.rotationDegrees(0))
                        .mul(Axis.XP.rotationDegrees(0))
                        .mul(Axis.YP.rotationDegrees(-90));
                hosegunItem.getEntityData().set(dataRotationId, myRotation);
            } catch (NoSuchFieldException | IllegalAccessException e)
            {
                e.printStackTrace();
            }

            return hosegunItem;
        });
        scene.idle(60);
        scene.overlay().showControls(refillStationLeftSegment.getCenter().add(-0.25,0,0), Pointing.LEFT, 45).rightClick().withItem(ModItems.HOSEGUN.toStack());
        scene.idle(90);

        scene.overlay().showText(90)
                .text("resourceful_refinement.ponder.binding_refills.text_4")
                .independent();
        scene.world().modifyEntity(hosegunLink, entity -> {
            if (entity instanceof Display.ItemDisplay itemDisplay)
            {
                try
                {
                    Field rotationAccessorField = Display.class.getDeclaredField("DATA_RIGHT_ROTATION_ID");
                    rotationAccessorField.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    EntityDataAccessor<Quaternionf> dataRotationId = (EntityDataAccessor<Quaternionf>) rotationAccessorField.get(null);

                    // Update the entity's data tracker
                    Quaternionf myRotation = new Quaternionf()
                            .mul(Axis.ZP.rotationDegrees(0))
                            .mul(Axis.XP.rotationDegrees(0))
                            .mul(Axis.YP.rotationDegrees(-45));
                    itemDisplay.getEntityData().set(dataRotationId, myRotation);

                } catch (NoSuchFieldException | IllegalAccessException e)
                {
                    e.printStackTrace();
                }
            }
        });
        scene.idle(30);
        PonderGelSprayHelper.playGelSpray(scene, new Vec3(5.25,1.2,5), gelSegment.getCenter(), 22, ModFluids.CATALYSED_REDSTONE.color, 20,
                () -> {
                    scene.world().showIndependentSectionImmediately(gelSegment);
                });
        scene.idle(60);


        // --- Page 3: Displaying Tracked Gels ---
        scene.addKeyframe();
        scene.overlay().showText(100)
                .text("resourceful_refinement.ponder.binding_refills.text_5")
                .independent();
        scene.idle(10);
        scene.world().showSection(displayLinkRightSegment, Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(displayLinkLeftSegment, Direction.DOWN);
        scene.idle(20);
        scene.world().showSection(displayBoardSegment, Direction.DOWN);
        scene.idle(20);
        scene.overlay().showOutline(PonderPalette.INPUT, outlineHighlight, displayBoardOutlineSegment, 20);
        scene.overlay().showLine(PonderPalette.INPUT, displayLinkLeftSegment.getCenter(), displayBoardOutlineSegment.getCenter(), 20);
        scene.idle(60);


        scene.world().setKineticSpeed(displayBoardSegment, 24);
        var textLink = scene.world().createEntity(level -> {
            Display.TextDisplay entity = new Display.TextDisplay(EntityType.TEXT_DISPLAY, level);
            entity.setPos(displayBoardSegment.getCenter().add(-1,1.5,-1.5));
            entity.setYRot(0);
            try
            {
                Field textAccessorField = Display.TextDisplay.class.getDeclaredField("DATA_TEXT_ID");
                Field widthAccessorField = Display.TextDisplay.class.getDeclaredField("DATA_LINE_WIDTH_ID");
                Field rotationAccessorField = Display.class.getDeclaredField("DATA_RIGHT_ROTATION_ID");
                textAccessorField.setAccessible(true);
                widthAccessorField.setAccessible(true);
                rotationAccessorField.setAccessible(true);
                @SuppressWarnings("unchecked")
                EntityDataAccessor<Component> dataTextId = (EntityDataAccessor<Component>) textAccessorField.get(null);
                @SuppressWarnings("unchecked")
                EntityDataAccessor<Integer> dataWidthId = (EntityDataAccessor<Integer>) widthAccessorField.get(null);
                @SuppressWarnings("unchecked")
                EntityDataAccessor<Quaternionf> dataRotationId = (EntityDataAccessor<Quaternionf>) rotationAccessorField.get(null);

                // Update the entity's data tracker
                entity.getEntityData().set(dataTextId, Component.literal("Red Team: 9"));
                entity.getEntityData().set(dataWidthId, 160);
                Quaternionf myRotation = new Quaternionf()
                        .mul(Axis.ZP.rotationDegrees(0))
                        .mul(Axis.XP.rotationDegrees(0))
                        .mul(Axis.YP.rotationDegrees(-90));
                entity.getEntityData().set(dataRotationId, myRotation);

            } catch (NoSuchFieldException | IllegalAccessException e)
            {
                e.printStackTrace();
            }

            return entity;
        });

        scene.overlay().showText(90)
                .text("resourceful_refinement.ponder.binding_refills.text_6")
                .independent();

        scene.idle(110);


        // --- Page 4: Clearing Tracked Gels ---
        scene.addKeyframe();
        scene.overlay().showText(90)
                .text("resourceful_refinement.ponder.binding_refills.text_7")
                .independent();
        scene.idle(20);
        scene.world().showSection(redstoneSegment, Direction.DOWN);
        scene.idle(90);

        scene.world().setBlock(leverPos, Blocks.LEVER.defaultBlockState().setValue(LeverBlock.POWERED, true), false);
        scene.world().setBlock(lampPos, Blocks.REDSTONE_LAMP.defaultBlockState().setValue(RedstoneLampBlock.LIT, true), false);
        scene.world().setBlock(dustPos, Blocks.REDSTONE_WIRE.defaultBlockState().setValue(RedStoneWireBlock.POWER, 15)
                .setValue(RedStoneWireBlock.WEST, RedstoneSide.SIDE).setValue(RedStoneWireBlock.EAST, RedstoneSide.SIDE), false);

        scene.world().modifyEntity(textLink, entity -> {
            if (entity instanceof Display.TextDisplay textDisplay)
            {
                try
                {
                    Field textAccessorField = Display.TextDisplay.class.getDeclaredField("DATA_TEXT_ID");
                    textAccessorField.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    EntityDataAccessor<Component> dataTextId = (EntityDataAccessor<Component>) textAccessorField.get(null);

                    entity.getEntityData().set(dataTextId, Component.literal("Red Team: 0"));

                } catch (NoSuchFieldException | IllegalAccessException e)
                {
                    e.printStackTrace();
                }
            }
        });

        scene.world().setBlocks(gelSegment, Blocks.AIR.defaultBlockState(), true);
        scene.idle(20);

        scene.overlay().showText(100)
                .text("resourceful_refinement.ponder.binding_refills.text_8")
                .independent();
        scene.idle(120);


        scene.markAsFinished();
    }
}
