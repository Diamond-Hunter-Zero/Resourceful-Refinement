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
    public void tick() {
        if (level != null && !level.isClientSide)
            updateRedstonePower();
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

        if (!pull)
            FluidPropagator.resetAffectedFluidNetworks(level, worldPosition, side.getOpposite());

        if (!hasReachedValidEndpoint(level, start, pull)) {
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
        for (Set<BlockFace> set : validFaces.values()) {
            int parallelBranches = Math.max(1, set.size() - 1);
            for (BlockFace face : set) {
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
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        redstonePowered = tag.getBoolean("RedstonePowered");
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        float speed = Math.abs(getSpeed());
        int millibucketsPerSecond = speed == 0 ? 0 : Math.max(1, Math.round(speed / 2f)) * 20;

        tooltip.add(Component.literal("     Advanced Pump:"));
        tooltip.add(Component.literal("§9Flow Rate: §7" + millibucketsPerSecond + " mB/s"));
        tooltip.add(Component.literal("§7Range: §8" + (FluidPropagator.getPumpRange() * 2) + " blocks"));
        tooltip.add(Component.literal("§7Direction: §8" + (redstonePowered ? "Reversed" : "Normal")));
        return true;
    }
}
