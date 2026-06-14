package com.resourceful_refinement.content.advanced_pump;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.pump.PumpBlock;
import com.simibubi.create.content.fluids.pump.PumpBlockEntity;

import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.math.BlockFace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AdvancedPumpBlockEntity extends PumpBlockEntity implements IHaveGoggleInformation {

    private boolean redstonePowered;
    private int measuredThroughputMbPerTick;
    private int throughputThisTick;
    private long throughputTick = -1;
    private int syncCooldown;
    private boolean pendingThroughputSync;

    public AdvancedPumpBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void updateRedstonePower() {
        if (level == null || level.isClientSide)
            return;

        boolean powered = level.hasNeighborSignal(worldPosition);
        if (redstonePowered == powered)
            return;

        redstonePowered = powered;
        if (getBlockState().getBlock() instanceof AdvancedPumpBlock)
            AdvancedPumpBlock.updatePoweredState(level, worldPosition, getBlockState(), powered);
        updatePressureChange();
        sendData();
    }

    public boolean isRedstonePowered() {
        return redstonePowered;
    }

    @Override
    public void updatePressureChange() {
        AdvancedPumpThroughputTracker.remove(this);
        resetMeasuredThroughput();
        super.updatePressureChange();
    }

    @Override
    public void tick() {
        if (level != null && !level.isClientSide) {
            updateRedstonePower();
            updateMeasuredThroughput();
        }
        super.tick();
    }

    @Override
    public boolean isPullingOnSide(boolean front) {
        return redstonePowered ? front : !front;
    }

    @Override
    protected void distributePressureTo(Direction side) {
        if (getSpeed() == 0)
            return;

        BlockFace start = new BlockFace(worldPosition, side);
        boolean pull = isPullingOnSide(isFront(side));
        Set<BlockFace> targets = new HashSet<>();
        Map<BlockPos, Pair<Integer, Map<Direction, Boolean>>> pipeGraph = new HashMap<>();
        boolean immediateEndpoint = hasReachedValidEndpoint(level, start, pull);

        if (!pull)
            FluidPropagator.resetAffectedFluidNetworks(level, worldPosition, side.getOpposite());

        if (!immediateEndpoint) {
            pipeGraph.computeIfAbsent(worldPosition, $ -> Pair.of(0, new IdentityHashMap<>()))
                .getSecond()
                .put(side, pull);
            pipeGraph.computeIfAbsent(start.getConnectedPos(), $ -> Pair.of(1, new IdentityHashMap<>()))
                .getSecond()
                .put(side.getOpposite(), !pull);

            List<Pair<Integer, BlockPos>> frontier = new ArrayList<>();
            Set<BlockPos> visited = new HashSet<>();
            int maxDistance = FluidPropagator.getPumpRange() * 2;
            frontier.add(Pair.of(1, start.getConnectedPos()));

            while (!frontier.isEmpty()) {
                Pair<Integer, BlockPos> entry = frontier.remove(0);
                int distance = entry.getFirst();
                BlockPos currentPos = entry.getSecond();

                if (!level.isLoaded(currentPos))
                    continue;
                if (visited.contains(currentPos))
                    continue;
                visited.add(currentPos);
                BlockState currentState = level.getBlockState(currentPos);
                FluidTransportBehaviour pipe = FluidPropagator.getPipe(level, currentPos);
                if (pipe == null)
                    continue;

                for (Direction face : FluidPropagator.getPipeConnections(currentState, pipe)) {
                    BlockFace blockFace = new BlockFace(currentPos, face);
                    BlockPos connectedPos = blockFace.getConnectedPos();

                    if (!level.isLoaded(connectedPos))
                        continue;
                    if (blockFace.isEquivalent(start))
                        continue;
                    if (hasReachedValidEndpoint(level, blockFace, pull)) {
                        pipeGraph.computeIfAbsent(currentPos, $ -> Pair.of(distance, new IdentityHashMap<>()))
                            .getSecond()
                            .put(face, pull);
                        targets.add(blockFace);
                        continue;
                    }

                    FluidTransportBehaviour pipeBehaviour = FluidPropagator.getPipe(level, connectedPos);
                    if (pipeBehaviour == null)
                        continue;
                    if (pipeBehaviour.blockEntity instanceof PumpBlockEntity)
                        continue;
                    if (visited.contains(connectedPos))
                        continue;
                    if (distance + 1 >= maxDistance) {
                        pipeGraph.computeIfAbsent(currentPos, $ -> Pair.of(distance, new IdentityHashMap<>()))
                            .getSecond()
                            .put(face, pull);
                        targets.add(blockFace);
                        continue;
                    }

                    pipeGraph.computeIfAbsent(currentPos, $ -> Pair.of(distance, new IdentityHashMap<>()))
                        .getSecond()
                        .put(face, pull);
                    pipeGraph.computeIfAbsent(connectedPos, $ -> Pair.of(distance + 1, new IdentityHashMap<>()))
                        .getSecond()
                        .put(face.getOpposite(), !pull);
                    frontier.add(Pair.of(distance + 1, connectedPos));
                }
            }
        }

        Map<Integer, Set<BlockFace>> validFaces = new HashMap<>();
        searchForEndpointRecursively(pipeGraph, targets, validFaces,
            new BlockFace(start.getPos(), start.getOppositeFace()), pull);

        float pressure = Math.abs(getSpeed());
        Set<BlockFace> measuredFaces = new HashSet<>();
        if (immediateEndpoint)
            measuredFaces.add(start);
        for (Set<BlockFace> set : validFaces.values()) {
            int parallelBranches = Math.max(1, set.size() - 1);
            for (BlockFace face : set) {
                measuredFaces.add(face);
                BlockPos pipePos = face.getPos();
                Direction pipeSide = face.getFace();

                if (pipePos.equals(worldPosition))
                    continue;

                boolean inbound = pipeGraph.get(pipePos)
                    .getSecond()
                    .get(pipeSide);
                FluidTransportBehaviour pipeBehaviour = FluidPropagator.getPipe(level, pipePos);
                if (pipeBehaviour == null)
                    continue;

                pipeBehaviour.addPressure(pipeSide, inbound, pressure / parallelBranches);
            }
        }

        AdvancedPumpThroughputTracker.addPath(this, measuredFaces);
    }

    private boolean hasReachedValidEndpoint(LevelAccessor world, BlockFace blockFace, boolean pull) {
        BlockPos connectedPos = blockFace.getConnectedPos();
        BlockState connectedState = world.getBlockState(connectedPos);
        BlockEntity blockEntity = world.getBlockEntity(connectedPos);
        Direction face = blockFace.getFace();

        if (PumpBlock.isPump(connectedState) && connectedState.getValue(PumpBlock.FACING)
            .getAxis() == face.getAxis() && blockEntity instanceof PumpBlockEntity pumpBE) {
            boolean connectedFaceIsFront = connectedState.getValue(PumpBlock.FACING) == blockFace.getOppositeFace();
            return pumpBE.isPullingOnSide(connectedFaceIsFront) != pull;
        }

        FluidTransportBehaviour pipe = FluidPropagator.getPipe(world, connectedPos);
        if (pipe != null && pipe.canHaveFlowToward(connectedState, blockFace.getOppositeFace()))
            return false;

        if (blockEntity != null) {
            IFluidHandler capability = blockEntity.getLevel()
                .getCapability(Capabilities.FluidHandler.BLOCK, blockEntity.getBlockPos(), face.getOpposite());
            if (capability != null)
                return true;
        }

        return FluidPropagator.isOpenEnd(world, blockFace.getPos(), face);
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putBoolean("RedstonePowered", redstonePowered);
        tag.putInt("MeasuredThroughputMbPerTick", measuredThroughputMbPerTick);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        redstonePowered = tag.getBoolean("RedstonePowered");
        measuredThroughputMbPerTick = tag.getInt("MeasuredThroughputMbPerTick");
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        int millibucketsPerSecond = measuredThroughputMbPerTick * 20;

        tooltip.add(Component.literal("     Advanced Pump:"));
        tooltip.add(Component.literal("\u00A79Throughput: \u00A77" + measuredThroughputMbPerTick + " mB/t \u00A78(" + millibucketsPerSecond + " mB/s)"));
        tooltip.add(Component.literal("\u00A77Range: \u00A78" + (FluidPropagator.getPumpRange() * 2) + " blocks"));
        tooltip.add(Component.literal("\u00A77Direction: \u00A78" + (redstonePowered ? "Reversed" : "Normal")));
        return true;
    }

    public void recordMeasuredThroughput(int amount) {
        if (level == null || level.isClientSide)
            return;

        long gameTime = level.getGameTime();
        if (throughputTick != gameTime) {
            throughputTick = gameTime;
            throughputThisTick = 0;
        }

        throughputThisTick += amount;
        if (measuredThroughputMbPerTick != throughputThisTick) {
            measuredThroughputMbPerTick = throughputThisTick;
            syncMeasuredThroughput();
        }
    }

    private void updateMeasuredThroughput() {
        if (level == null)
            return;

        if (throughputTick < level.getGameTime() - 1 && measuredThroughputMbPerTick != 0) {
            throughputThisTick = 0;
            measuredThroughputMbPerTick = 0;
            syncMeasuredThroughput();
        }

        if (getSpeed() == 0)
            AdvancedPumpThroughputTracker.remove(this);

        if (syncCooldown > 0)
            syncCooldown--;

        if (syncCooldown == 0 && pendingThroughputSync) {
            pendingThroughputSync = false;
            syncMeasuredThroughput();
        }
    }

    private void syncMeasuredThroughput() {
        if (syncCooldown > 0) {
            pendingThroughputSync = true;
            return;
        }

        syncCooldown = 5;
        sendData();
    }

    private void resetMeasuredThroughput() {
        throughputThisTick = 0;
        throughputTick = level == null ? -1 : level.getGameTime();
        if (measuredThroughputMbPerTick == 0)
            return;

        measuredThroughputMbPerTick = 0;
        syncMeasuredThroughput();
    }

    @Override
    public void remove() {
        AdvancedPumpThroughputTracker.remove(this);
        super.remove();
    }
}
