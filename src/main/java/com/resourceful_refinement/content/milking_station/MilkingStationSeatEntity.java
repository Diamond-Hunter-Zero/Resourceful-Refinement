package com.resourceful_refinement.content.milking_station;

import com.resourceful_refinement.registry.ModBlocks;
import com.resourceful_refinement.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class MilkingStationSeatEntity extends Entity {

    public MilkingStationSeatEntity(EntityType<? extends MilkingStationSeatEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
        setInvisible(true);
    }

    public MilkingStationSeatEntity(Level level, BlockPos stationPos) {
        this(ModEntities.MILKING_STATION_SEAT.get(), level);
        setPos(stationPos.getX() + 0.5, stationPos.getY() + 0.65, stationPos.getZ() + 0.5);
    }

    @Override
    public void setPos(double x, double y, double z) {
        super.setPos(x, y, z);
        AABB bounds = getBoundingBox();
        Vec3 offset = new Vec3(x, y, z).subtract(bounds.getCenter());
        setBoundingBox(bounds.move(offset));
    }

    public BlockPos getStationPos() {
        return blockPosition();
    }

    @Override
    public void tick() {
        if (level().isClientSide) {
            return;
        }

        if (isVehicle() && level().getBlockState(blockPosition()).is(ModBlocks.MILKING_STATION.get())) {
            return;
        }

        discard();
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction callback) {
        if (!hasPassenger(passenger)) {
            return;
        }

        double heightOffset = getPassengerRidingPosition(passenger).y - passenger.getVehicleAttachmentPoint(this).y;
        callback.accept(passenger, getX(), 1.0 / 16.0 + heightOffset, getZ());
    }

    @Override
    public void setDeltaMovement(Vec3 movement) {
    }

    @Override
    public void onPassengerTurned(Entity entity) {
        entity.setYHeadRot(entity.getYRot());
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        return super.getDismountLocationForPassenger(passenger).add(0, 0.5, 0);
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        if (!level().isClientSide && getPassengers().isEmpty()) {
            discard();
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
    }
}
