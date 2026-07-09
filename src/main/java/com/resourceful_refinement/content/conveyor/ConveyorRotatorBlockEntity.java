package com.resourceful_refinement.content.conveyor;

import com.resourceful_refinement.registry.ModBlocks;
import com.resourceful_refinement.registry.ModStressValues;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.AssemblyException;
import com.simibubi.create.content.contraptions.ControlledContraptionEntity;
import com.simibubi.create.content.contraptions.IControlContraption;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class ConveyorRotatorBlockEntity extends KineticBlockEntity implements IControlContraption {
    private static final float DEGREES_PER_TICK_PER_RPM = 1.0f / 8.0f;

    private Direction outputDirection;
    private ConveyorRotatorRotationSession rotationSession;
    private BlockState completedFullSpinState;
    private BlockState pendingRotatedOutputState;
    private ControlledContraptionEntity movedContraption;
    private float contraptionAngle;

    public ConveyorRotatorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        outputDirection = state.hasProperty(ConveyorRotatorBlock.HORIZONTAL_FACING)
                ? state.getValue(ConveyorRotatorBlock.HORIZONTAL_FACING)
                : Direction.NORTH;
    }

    @Override
    public void tick() {
        super.tick();

        if (level == null) {
            return;
        }

        if (level.isClientSide) {
            tickClientContraption();
            return;
        }

        if (rotationSession != null) {
            tickRotationSession(getSpeed());
            return;
        }

        if (getSpeed() == 0) {
            return;
        }

        tickRotatorWork(getSpeed());
    }

    @Override
    public void remove() {
        if (level != null && !level.isClientSide) {
            disassembleRotatingContraption(false);
        }
        super.remove();
    }

    boolean tickRotatorWork(float speed) {
        if (level == null || level.isClientSide || speed == 0) {
            return false;
        }
        if (rotationSession != null) {
            tickRotationSession(speed);
            return true;
        }

        BlockPos sourcePos = worldPosition.above();
        if (!level.isLoaded(sourcePos)) {
            return false;
        }

        BlockState sourceState = level.getBlockState(sourcePos);
        if (sourceState.isAir()) {
            completedFullSpinState = null;
            pendingRotatedOutputState = null;
            return false;
        }
        if (completedFullSpinState != null && !sourceState.equals(completedFullSpinState)) {
            completedFullSpinState = null;
        }
        if (pendingRotatedOutputState != null && !sourceState.equals(pendingRotatedOutputState)) {
            pendingRotatedOutputState = null;
        }
        if (!canHandleSource(sourcePos)) {
            return false;
        }

        if (tryPushPendingRotatedOutput(sourcePos, sourceState)) {
            return true;
        }

        if (isRedstonePowered()) {
            return false;
        }

        ConveyorRotatorTarget target = ConveyorRotatorTarget.forState(sourceState, outputDirection, speed);
        if (!target.requiresRotation()) {
            ConveyorBlockMover.tryMoveBlock(level, sourcePos, outputDirection);
            return true;
        }
        if (target.fullSpin() && sourceState.equals(completedFullSpinState)) {
            ConveyorBlockMover.tryMoveBlock(level, sourcePos, outputDirection);
            return true;
        }

        completedFullSpinState = null;
        if (target.fullSpin()) {
            level.setBlock(sourcePos, target.targetState(), Block.UPDATE_ALL);
        }
        rotationSession = new ConveyorRotatorRotationSession(sourceState, target.targetState(), outputDirection,
                target.requiredDegrees(), target.fullSpin());
        if (!assembleRotatingContraption()) {
            rotationSession = null;
            if (target.fullSpin() && level.getBlockState(sourcePos).is(target.targetState().getBlock())) {
                level.setBlock(sourcePos, sourceState, Block.UPDATE_ALL);
            }
            return false;
        }
        sendData();
        tickRotationSession(speed);
        return true;
    }

    @Override
    public float calculateStressApplied() {
        return (float) ModStressValues.CONVEYOR_ROTATOR_STRESS;
    }

    @Override
    public AABB createRenderBoundingBox() {
        return super.createRenderBoundingBox().inflate(1);
    }

    @Override
    public void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        compound.putString("OutputDirection", outputDirection.getName());
        if (rotationSession != null) {
            compound.put("RotationSession", rotationSession.write());
        }
        compound.putFloat("ContraptionAngle", contraptionAngle);
        if (completedFullSpinState != null) {
            compound.put("CompletedFullSpinState", NbtUtils.writeBlockState(completedFullSpinState));
        }
        if (pendingRotatedOutputState != null) {
            compound.put("PendingRotatedOutputState", NbtUtils.writeBlockState(pendingRotatedOutputState));
        }
        super.write(compound, registries, clientPacket);
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        Direction readDirection = Direction.byName(compound.getString("OutputDirection"));
        outputDirection = readDirection == null ? Direction.NORTH : readDirection;
        rotationSession = compound.contains("RotationSession")
                ? ConveyorRotatorRotationSession.read(compound.getCompound("RotationSession"), registries)
                : null;
        contraptionAngle = compound.getFloat("ContraptionAngle");
        completedFullSpinState = compound.contains("CompletedFullSpinState")
                ? NbtUtils.readBlockState(registries.lookupOrThrow(Registries.BLOCK), compound.getCompound("CompletedFullSpinState"))
                : null;
        pendingRotatedOutputState = compound.contains("PendingRotatedOutputState")
                ? NbtUtils.readBlockState(registries.lookupOrThrow(Registries.BLOCK), compound.getCompound("PendingRotatedOutputState"))
                : null;
    }

    public Direction getOutputDirection() {
        return outputDirection;
    }

    public void setOutputDirection(Direction outputDirection) {
        if (outputDirection == null || outputDirection.getAxis().isVertical()) {
            return;
        }
        if (this.outputDirection == outputDirection) {
            return;
        }
        this.outputDirection = outputDirection;
        setChanged();
        sendData();
    }

    public boolean hasRotationSession() {
        return rotationSession != null;
    }

    public boolean finishRotationByPlayer() {
        if (level == null || level.isClientSide || rotationSession == null) {
            return false;
        }
        finishRotationSession(true);
        return true;
    }

    private void tickRotationSession(float speed) {
        if (rotationSession == null) {
            return;
        }

        BlockPos sourcePos = worldPosition.above();
        if (!level.isLoaded(sourcePos)) {
            completedFullSpinState = null;
            clearRotationSession();
            return;
        }

        BlockState currentState = level.getBlockState(sourcePos);
        if (!isSessionBlock(currentState)) {
            completedFullSpinState = null;
            disassembleRotatingContraption(false);
            clearRotationSession();
            return;
        }

        if (speed == 0) {
            if (rotationSession.setPaused(true)) {
                sendData();
            }
            return;
        }

        boolean wasPaused = rotationSession.setPaused(false);
        rotationSession.advance(rotationDegreesThisTick(speed));
        contraptionAngle = rotationSession.visualDegrees();
        applyContraptionRotation();
        if (rotationSession.isComplete()) {
            finishRotationSession(false);
            return;
        }

        setChanged();
        if (wasPaused) {
            sendData();
        }
    }

    private void finishRotationSession(boolean forcedByPlayer) {
        ConveyorRotatorRotationSession session = rotationSession;
        if (session == null) {
            return;
        }

        BlockPos sourcePos = worldPosition.above();
        if (!level.isLoaded(sourcePos)) {
            clearRotationSession();
            return;
        }

        disassembleRotatingContraption(true);
        level.setBlock(sourcePos, session.targetState(), Block.UPDATE_ALL);

        completedFullSpinState = session.fullSpin() ? session.targetState() : null;
        pendingRotatedOutputState = session.targetState();
        clearRotationSession();
        if (!forcedByPlayer || getSpeed() != 0) {
            ConveyorMovementResult result = ConveyorBlockMover.tryMoveBlock(level, sourcePos, outputDirection);
            if (result.moved()) {
                pendingRotatedOutputState = null;
            }
        }
    }

    private boolean tryPushPendingRotatedOutput(BlockPos sourcePos, BlockState sourceState) {
        if (pendingRotatedOutputState == null || !sourceState.equals(pendingRotatedOutputState)) {
            return false;
        }

        ConveyorMovementResult result = ConveyorBlockMover.tryMoveBlock(level, sourcePos, outputDirection);
        if (result.moved()) {
            pendingRotatedOutputState = null;
        }
        return true;
    }

    private boolean canHandleSource(BlockPos sourcePos) {
        return ConveyorBlockMover.getSourceMovementFailure(level, sourcePos, outputDirection)
                == ConveyorMovementResult.FailureReason.NONE;
    }

    private boolean isRedstonePowered() {
        return level != null && level.hasNeighborSignal(worldPosition);
    }

    private boolean isSessionBlock(BlockState state) {
        if (rotationSession == null) {
            return false;
        }
        if (state.isAir()) {
            return movedContraption != null;
        }
        return state.is(ModBlocks.CONVEYOR_ROTATOR_PROXY.get())
                || state.is(rotationSession.originalState().getBlock())
                || state.is(rotationSession.targetState().getBlock());
    }

    private float rotationDegreesThisTick(float speed) {
        return Math.max(1.0f, Math.abs(speed) * DEGREES_PER_TICK_PER_RPM);
    }

    private void clearRotationSession() {
        rotationSession = null;
        contraptionAngle = 0;
        setChanged();
        sendData();
    }

    private boolean assembleRotatingContraption() {
        if (level == null || level.isClientSide || rotationSession == null) {
            return false;
        }

        ConveyorRotatorContraption contraption = new ConveyorRotatorContraption(Direction.UP);
        try {
            if (!contraption.assemble(level, worldPosition)) {
                return false;
            }
        } catch (AssemblyException ignored) {
            return false;
        }

        BlockPos sourcePos = worldPosition.above();
        contraption.removeBlocksFromWorld(level, BlockPos.ZERO);
        movedContraption = ControlledContraptionEntity.create(level, this, contraption);
        movedContraption.setPos(sourcePos.getX(), sourcePos.getY(), sourcePos.getZ());
        movedContraption.setRotationAxis(Direction.Axis.Y);
        level.addFreshEntity(movedContraption);
        level.setBlock(sourcePos, ModBlocks.CONVEYOR_ROTATOR_PROXY.get().defaultBlockState(), Block.UPDATE_ALL);
        contraptionAngle = 0;
        applyContraptionRotation();
        AllSoundEvents.CONTRAPTION_ASSEMBLE.playOnServer(level, worldPosition);
        return true;
    }

    private void disassembleRotatingContraption(boolean playSound) {
        if (level == null || level.isClientSide) {
            return;
        }

        BlockPos sourcePos = worldPosition.above();
        if (level.isLoaded(sourcePos) && level.getBlockState(sourcePos).is(ModBlocks.CONVEYOR_ROTATOR_PROXY.get())) {
            level.setBlock(sourcePos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }

        if (movedContraption != null) {
            movedContraption.disassemble();
            movedContraption = null;
            if (playSound) {
                AllSoundEvents.CONTRAPTION_DISASSEMBLE.playOnServer(level, worldPosition);
            }
        }
    }

    private void applyContraptionRotation() {
        if (movedContraption != null) {
            movedContraption.setAngle(contraptionAngle);
            movedContraption.setRotationAxis(Direction.Axis.Y);
        }
    }

    private void tickClientContraption() {
        if (rotationSession == null || movedContraption == null) {
            return;
        }
        if (!rotationSession.paused() && getSpeed() != 0) {
            float predictedStep = rotationDegreesThisTick(getSpeed()) * ServerSpeedProvider.get();
            float currentProgress = Math.abs(contraptionAngle);
            float nextProgress = Math.min(Math.abs(rotationSession.requiredDegrees()), currentProgress + predictedStep);
            contraptionAngle = Math.signum(rotationSession.requiredDegrees()) * nextProgress;
        }
        applyContraptionRotation();
    }

    @Override
    public boolean isAttachedTo(AbstractContraptionEntity contraption) {
        return movedContraption == contraption;
    }

    @Override
    public void attach(ControlledContraptionEntity contraption) {
        movedContraption = contraption;
        movedContraption.setPos(worldPosition.getX(), worldPosition.getY() + 1, worldPosition.getZ());
        movedContraption.setRotationAxis(Direction.Axis.Y);
        applyContraptionRotation();
        setChanged();
        if (!level.isClientSide) {
            sendData();
        }
    }

    @Override
    public void onStall() {
        if (!level.isClientSide) {
            sendData();
        }
    }

    @Override
    public boolean isValid() {
        return !isRemoved();
    }

    @Override
    public BlockPos getBlockPosition() {
        return worldPosition;
    }
}
