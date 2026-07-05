package com.resourceful_refinement.content.conveyor;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.state.BlockState;

public class ConveyorRotatorRotationSession {
    private final BlockState originalState;
    private final BlockState targetState;
    private final Direction targetDirection;
    private final float requiredDegrees;
    private final boolean fullSpin;
    private float progressDegrees;
    private boolean paused;

    public ConveyorRotatorRotationSession(BlockState originalState, BlockState targetState, Direction targetDirection,
                                          float requiredDegrees, boolean fullSpin) {
        this.originalState = originalState;
        this.targetState = targetState;
        this.targetDirection = targetDirection;
        this.requiredDegrees = requiredDegrees;
        this.fullSpin = fullSpin;
    }

    public static ConveyorRotatorRotationSession read(CompoundTag tag, HolderLookup.Provider registries) {
        HolderLookup.RegistryLookup<net.minecraft.world.level.block.Block> blockLookup = registries.lookupOrThrow(Registries.BLOCK);
        BlockState originalState = NbtUtils.readBlockState(blockLookup, tag.getCompound("OriginalState"));
        BlockState targetState = NbtUtils.readBlockState(blockLookup, tag.getCompound("TargetState"));
        Direction targetDirection = Direction.byName(tag.getString("TargetDirection"));
        if (targetDirection == null) {
            targetDirection = Direction.NORTH;
        }

        ConveyorRotatorRotationSession session = new ConveyorRotatorRotationSession(originalState, targetState,
                targetDirection, tag.getFloat("RequiredDegrees"), tag.getBoolean("FullSpin"));
        session.progressDegrees = tag.getFloat("ProgressDegrees");
        session.paused = tag.getBoolean("Paused");
        return session;
    }

    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.put("OriginalState", NbtUtils.writeBlockState(originalState));
        tag.put("TargetState", NbtUtils.writeBlockState(targetState));
        tag.putString("TargetDirection", targetDirection.getName());
        tag.putFloat("RequiredDegrees", requiredDegrees);
        tag.putFloat("ProgressDegrees", progressDegrees);
        tag.putBoolean("FullSpin", fullSpin);
        tag.putBoolean("Paused", paused);
        return tag;
    }

    public void advance(float degrees) {
        progressDegrees = Math.min(Math.abs(requiredDegrees), progressDegrees + Math.max(0, degrees));
    }

    public boolean isComplete() {
        return progressDegrees >= Math.abs(requiredDegrees);
    }

    public BlockState originalState() {
        return originalState;
    }

    public BlockState targetState() {
        return targetState;
    }

    public Direction targetDirection() {
        return targetDirection;
    }

    public float progressDegrees() {
        return progressDegrees;
    }

    public float requiredDegrees() {
        return requiredDegrees;
    }

    public float visualDegrees() {
        return Math.signum(requiredDegrees) * progressDegrees;
    }

    public boolean fullSpin() {
        return fullSpin;
    }

    public boolean paused() {
        return paused;
    }

    public boolean setPaused(boolean paused) {
        boolean changed = this.paused != paused;
        this.paused = paused;
        return changed;
    }
}
