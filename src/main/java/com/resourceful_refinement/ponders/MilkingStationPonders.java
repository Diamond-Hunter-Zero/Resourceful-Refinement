package com.resourceful_refinement.ponders;

import com.resourceful_refinement.content.fuel_tank.FuelTankBlockEntity;
import com.resourceful_refinement.content.radiator.RadiatorBlock;
import com.resourceful_refinement.registry.ModFluids;
import com.simibubi.create.AllFluids;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;


public class MilkingStationPonders {

    public static void milkingStationScene(SceneBuilder builder, SceneBuildingUtil util) {

        //Build scene
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("milking_station", "Milking Mobs");

        scene.configureBasePlate(0,0,5);
        scene.removeShadow();

        scene.rotateCameraY(0);
        scene.scaleSceneView(1f);

        BlockPos milkingStationPos = util.grid().at(2, 2, 2);
        Selection milkingStationSegment = util.select().fromTo(milkingStationPos, milkingStationPos);

        Vec3 cowPos = new Vec3(1,1,1.5);

        BlockPos gearboxPos = util.grid().at(2, 1, 2);
        Selection gearboxSegment = util.select().fromTo(gearboxPos, gearboxPos);

        Selection conveyorSegment = util.select().fromTo(2, 1, 0,2, 2, 1);
        Selection kineticSegment = util.select().fromTo(3, 1, 1,5, 1, 2).add(util.select().position(5, 0, 1));

        BlockPos tankPos = util.grid().at(0, 1, 4);
        Selection tankSegment = util.select().fromTo(2, 2, 3,0, 1, 4);


        // Show baseplate
        scene.showBasePlate();
        scene.idle(20);

        scene.world().showSection(milkingStationSegment, Direction.DOWN);
        scene.idle(20);


        // --- Page 1: Miking Station I/O ---
        scene.addKeyframe();
        scene.overlay().showText(120)
                .text("resourceful_refinement.ponder.milking_station.text_1")
                .pointAt(milkingStationPos.getCenter());
        scene.idle(140);

        scene.addKeyframe();
        scene.overlay().showText(80)
                .text("resourceful_refinement.ponder.milking_station.text_2")
                .pointAt(gearboxSegment.getCenter());
        scene.world().showSection(gearboxSegment, Direction.UP);
        scene.idle(100);

        scene.overlay().showText(80)
                .text("resourceful_refinement.ponder.milking_station.text_3")
                .independent();
        scene.world().showSection(tankSegment, Direction.DOWN);
        scene.idle(100);

        scene.overlay().showText(80)
                .text("resourceful_refinement.ponder.milking_station.text_4")
                .independent();
        scene.world().showSection(conveyorSegment, Direction.DOWN);
        scene.idle(100);


        // --- Page 2: Leashing Mobs ---
        scene.addKeyframe();

        scene.world().showSection(kineticSegment, Direction.DOWN);

        var cowLink = scene.world().createEntity(level -> {
            Cow cowMob = new Cow(EntityType.COW, level) {
                @Override
                public void tick() {
                    // Disable the default tick calls
                    //super.tick();
                    this.walkAnimation.update(0f, 0f);
                }
            };
            cowMob.setPos(cowPos);
            cowMob.setNoAi(true);

            cowMob.walkAnimation.setSpeed(0f);

            return cowMob;
        });

        scene.idle(20);
        scene.overlay().showText(120)
                .text("resourceful_refinement.ponder.milking_station.text_5")
                .independent();
        scene.idle(40);
        scene.overlay().showControls(cowPos.add(-0.25,0.5,0.5), Pointing.LEFT, 100).withItem(Items.LEAD.getDefaultInstance());
        scene.idle(100);

        scene.world().modifyEntity(cowLink, entity -> {
            entity.setPos(milkingStationPos.getCenter().add(0,0.5,0));
            entity.setDeltaMovement(Vec3.ZERO);
        });

        scene.idle(10);
        scene.world().setKineticSpeed(kineticSegment, -48);
        scene.world().setKineticSpeed(conveyorSegment, -48);

        scene.idle(10);
        FluidStack milkStack = new FluidStack(ModFluids.WHITE_PAINT.source, 1000);
        for (int i = 0; i < 8; i++)
        {
            scene.world().modifyBlockEntity(tankPos, FluidTankBlockEntity.class, tankBE -> {
                if (tankBE.getLevel() != null) {
                    var tankCapability = tankBE.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, tankBE.getBlockPos(), null);
                    if (tankCapability != null)
                    {
                        tankCapability.fill(milkStack, IFluidHandler.FluidAction.EXECUTE);
                    }
                }
                // Force the client-side Ponder world to synchronize and render the fluid
                tankBE.sendData();
            });
            scene.idle(5);
        }
        scene.idle(10);


        // --- Page 3: Removing Mobs ---
        scene.addKeyframe();
        scene.overlay().showText(80)
                .text("resourceful_refinement.ponder.milking_station.text_6")
                .independent();
        scene.idle(30);
        scene.overlay().showControls(milkingStationPos.getCenter().add(-0.25,0.5,0.5), Pointing.LEFT, 30).withItem(Items.LEAD.getDefaultInstance());
        scene.idle(30);
        scene.world().modifyEntity(cowLink, entity -> {
            entity.setPos(cowPos);
            entity.setDeltaMovement(Vec3.ZERO);
        });
        scene.idle(40);
        scene.world().modifyEntity(cowLink, Entity::discard);
        scene.idle(20);

        // --- Page 4: Boat Mobs ---
        scene.addKeyframe();

        scene.idle(10);
        var creeperLink = scene.world().createEntity(level -> {
            Creeper creeperMob = new Creeper(EntityType.CREEPER, level)
            {
                @Override
                public void tick() {
                // Disable the default tick calls
                //super.tick();
                this.walkAnimation.update(0f, 0f);
                }
            };
            creeperMob.setPos(cowPos);
            creeperMob.setYRot(180);
            creeperMob.setNoAi(true);

            return creeperMob;
        });
        scene.idle(10);

        scene.overlay().showText(100)
                .text("resourceful_refinement.ponder.milking_station.text_7")
                .independent();
        scene.idle(40);

        var boatLink = scene.world().createEntity(level -> {
            Boat boatMob = new Boat(EntityType.BOAT, level);
            boatMob.setPos(cowPos);
            boatMob.setYRot(180);

            return boatMob;
        });

        scene.idle(40);
        scene.overlay().showControls(cowPos.add(-0.25,0.5,0.5), Pointing.LEFT, 20).withItem(Items.LEAD.getDefaultInstance());
        scene.idle(40);


        scene.overlay().showText(100)
                .text("resourceful_refinement.ponder.milking_station.text_8")
                .independent();
        scene.world().modifyEntity(creeperLink, entity -> {
            entity.setPos(milkingStationPos.getCenter().add(0,0.5,0));
            entity.setDeltaMovement(Vec3.ZERO);
        });
        scene.idle(110);


        scene.markAsFinished();
    }
}
