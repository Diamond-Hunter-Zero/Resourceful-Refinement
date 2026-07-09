package com.resourceful_refinement.content.conveyor;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

public class ConveyorBeltBlockEntity extends KineticBlockEntity {
    private BlockPos controller;
    public int beltLength;
    public int index;

    public ConveyorBeltBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        controller = pos;
    }

    @Override
    public void tick() {
        if (beltLength == 0 && level != null && ConveyorBeltBlock.isConveyor(level.getBlockState(worldPosition))) {
            ConveyorBeltBlock.initBelt(level, worldPosition);
        }

        super.tick();

        if (level == null || level.isClientSide || !isController() || getSpeed() == 0) {
            return;
        }

        tickBlockMovement(getSpeed());
    }

    boolean tickBlockMovement(float speed) {
        if (level == null || level.isClientSide || !isController() || speed == 0) {
            return false;
        }

        List<BlockPos> chain = new ArrayList<>(ConveyorBeltBlock.getBeltChain(level, worldPosition));
        if (isTrainPowered(chain)) {
            return false;
        }

        int interval = ConveyorBlockMover.movementIntervalForSpeed(speed);
        if (level.getGameTime() % interval != 0) {
            return false;
        }

        Direction movementDirection = movementFacingForSpeed(speed);
        ConveyorBlockMover.processLineFrontToBack(level, chain, movementDirection);
        return true;
    }

    private boolean isTrainPowered(List<BlockPos> chain) {
        if (level == null) {
            return false;
        }
        for (BlockPos beltPos : chain) {
            if (level.hasNeighborSignal(beltPos)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public float calculateStressApplied() {
        return 0;
    }

    @Override
    public AABB createRenderBoundingBox() {
        if (!isController()) {
            return super.createRenderBoundingBox();
        }
        return super.createRenderBoundingBox().inflate(beltLength + 1);
    }

    @Override
    public void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        if (controller != null) {
            compound.put("Controller", NbtUtils.writeBlockPos(controller));
        }
        compound.putBoolean("IsController", isController());
        compound.putInt("Length", beltLength);
        compound.putInt("Index", index);
        super.write(compound, registries, clientPacket);
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        if (compound.getBoolean("IsController")) {
            controller = worldPosition;
        } else if (compound.contains("Controller")) {
            controller = NbtUtils.readBlockPos(compound, "Controller").orElse(worldPosition);
        }
        beltLength = compound.getInt("Length");
        index = compound.getInt("Index");
    }

    @Override
    public void clearKineticInformation() {
        super.clearKineticInformation();
        controller = null;
        beltLength = 0;
        index = 0;
    }

    @Override
    public float propagateRotationTo(KineticBlockEntity target, BlockState stateFrom, BlockState stateTo, BlockPos diff,
                                     boolean connectedViaAxes, boolean connectedViaCogs) {
        if (target instanceof ConveyorBeltBlockEntity targetBelt && !connectedViaAxes) {
            return getController().equals(targetBelt.getController()) ? 1 : 0;
        }
        return 0;
    }

    public void setController(BlockPos controller) {
        this.controller = controller;
    }

    public BlockPos getController() {
        return controller == null ? worldPosition : controller;
    }

    public ConveyorBeltBlockEntity getControllerBE() {
        if (level == null || controller == null || !level.isLoaded(controller)) {
            return null;
        }
        BlockEntity blockEntity = level.getBlockEntity(controller);
        return blockEntity instanceof ConveyorBeltBlockEntity belt ? belt : null;
    }

    public boolean isController() {
        return getController().equals(worldPosition);
    }

    public boolean hasPulley() {
        BlockState state = getBlockState();
        return ConveyorBeltBlock.isConveyor(state) && state.getValue(ConveyorBeltBlock.PART) != BeltPart.MIDDLE;
    }

    public Direction getMovementFacing() {
        return movementFacingForSpeed(getSpeed());
    }

    private Direction getBeltFacing() {
        return getBlockState().getValue(ConveyorBeltBlock.HORIZONTAL_FACING);
    }

    private Direction movementFacingForSpeed(float speed) {
        Direction.Axis axis = getBeltFacing().getAxis();
        Direction.AxisDirection axisDirection = speed < 0 ^ axis == Direction.Axis.X
                ? Direction.AxisDirection.NEGATIVE
                : Direction.AxisDirection.POSITIVE;
        return Direction.fromAxisAndDirection(axis, axisDirection);
    }
}
