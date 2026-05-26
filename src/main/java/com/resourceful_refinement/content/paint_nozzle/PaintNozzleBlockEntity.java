package com.resourceful_refinement.content.paint_nozzle;

import com.resourceful_refinement.content.hosegun.GelBlobEntity;
import com.resourceful_refinement.content.hosegun.HosegunItem;
import com.resourceful_refinement.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import static com.resourceful_refinement.content.gel_splatter.GelPropertiesManager.getGelAmmoCost;

public class PaintNozzleBlockEntity extends BlockEntity {

    public static final int TANK_CAPACITY = 500;

    private int sprayTicks;

    public final FluidTank tank = new FluidTank(TANK_CAPACITY, stack -> stack.isEmpty() || stack.getAmount() <= TANK_CAPACITY) {
        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty() || isValveOpen()) {
                return 0;
            }
            if (!tank.isEmpty() && !FluidStack.isSameFluidSameComponents(tank.getFluid(), resource)) {
                return 0;
            }
            return super.fill(resource, action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidStack.EMPTY;
        }

        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    public PaintNozzleBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PaintNozzleBlockEntity be) {
        if (!PaintNozzleBlock.isValveOpen(state)) {
            return;
        }

        be.sprayTicks++;
        if (be.sprayTicks % 2 != 0) {
            return;
        }

        FluidStack contained = be.tank.getFluid();
        if (contained.isEmpty()) {
            return;
        }

        int cost = getGelAmmoCost(contained.getFluid());
        if (contained.getAmount() < cost) {
            return;
        }

        be.fireGelBlob(state);
        be.tank.drain(cost, IFluidHandler.FluidAction.EXECUTE);
    }

    private void fireGelBlob(BlockState state) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }

        Direction sprayDir = PaintNozzleBlock.getSprayDirection(state);
        Vec3 spawn = Vec3.atCenterOf(worldPosition)
                .add(Vec3.atLowerCornerOf(sprayDir.getNormal()).scale(0.55D));

        float velocity = 1.6F * HosegunItem.GEL_BLOB_VELOCITY_FACTOR;
        GelBlobEntity projectile = new GelBlobEntity(ModEntities.GEL_BLOB.get(), spawn.x, spawn.y, spawn.z, level);
        projectile.setFluid(tank.getFluid().getFluid());
        projectile.shoot(
                sprayDir.getStepX(),
                sprayDir.getStepY(),
                sprayDir.getStepZ(),
                velocity,
                HosegunItem.HOSEGUN_INNACURACY
        );
        server.addFreshEntity(projectile);

        server.playSound(null, worldPosition, SoundEvents.LAVA_POP, SoundSource.BLOCKS,
                0.45F, 1.15F + server.random.nextFloat() * 0.2F);
    }

    public boolean isValveOpen() {
        return level != null && PaintNozzleBlock.isValveOpen(getBlockState());
    }

    public void onValveStateChanged(boolean open) {
        sprayTicks = 0;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    public void dropContents() {
        if (level == null || level.isClientSide) {
            return;
        }
        FluidStack fluid = tank.getFluid();
        if (!fluid.isEmpty()) {
            Block.popResource(level, worldPosition, fluid.getFluid().getBucket().getDefaultInstance());
        }
        tank.setFluid(FluidStack.EMPTY);
    }

    public void invalidateCapabilities() {
        if (level != null) {
            level.invalidateCapabilities(worldPosition);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Tank", tank.writeToNBT(registries, new CompoundTag()));
        tag.putInt("SprayTicks", sprayTicks);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Tank")) {
            tank.readFromNBT(registries, tag.getCompound("Tank"));
        }
        sprayTicks = tag.getInt("SprayTicks");
    }

    @Override
    public void setBlockState(BlockState state) {
        super.setBlockState(state);
        setChanged();
    }
}
