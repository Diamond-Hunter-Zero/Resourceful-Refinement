package com.resourceful_refinement.content.radiator;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.pipes.AxisPipeBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RadiatorBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {

    private static final float CONSUMPTION_RATE = 0.10f; // 10% of fluid is dissipated/consumed
    private static final int MAX_HEAT_ENERGY = 1000;

    private int heatEnergy = 0;
    private boolean isProcessingFlow = false; // Recursion guard flag

    public RadiatorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> list) { }


    // -------------------------------------------------------------------------
    // Tick Behaviour
    // -------------------------------------------------------------------------

    // Checks if the accessing face matches the radiator's current axis orientation.
    public boolean isValidFace(Direction face) {
        BlockState state = getBlockState();
        if (!state.hasProperty(AxisPipeBlock.AXIS)) return false;
        return face.getAxis() == state.getValue(AxisPipeBlock.AXIS);
    }

    private void onFluidConsumed(int amount)
    {
        int previousHeat = this.heatEnergy;
        this.heatEnergy = Math.min(this.heatEnergy + amount, MAX_HEAT_ENERGY);

        if (previousHeat != this.heatEnergy)
            syncData();
    }

    // Server tick handling heat decay and updating the block state's HEAT_STATE.
    public static void serverTick(Level level, BlockPos pos, BlockState state, RadiatorBlockEntity be) {
        // Slowly cool down over time
        if (be.heatEnergy > 0 && level.random.nextInt(4) == 3) {
            int previousHeat = be.heatEnergy;
            be.heatEnergy = Math.max(be.heatEnergy - 1, 0);

            if (previousHeat != be.heatEnergy)
                be.syncData();
        }

        // Map internal heat energy directly to the 0-3 block state property
        int currentHeatState = state.getValue(RadiatorBlock.HEAT_STATE);
        int targetHeatState = (be.heatEnergy * 3) / MAX_HEAT_ENERGY;

        if (currentHeatState != targetHeatState) {
            // Use flag 2 (Block.UPDATE_CLIENTS) instead of 3 (which would trigger neighbor updates
            // and wipe the pressure of adjacent pipe networks, disrupting fluid flow rendering)
            level.setBlock(pos, state.setValue(RadiatorBlock.HEAT_STATE, targetHeatState), 2);
        }
    }

    // Exposes a contextual capability handler tailored to the accessed face.
    @Nullable
    public IFluidHandler getFluidHandler(@NotNull Direction side) {
        if (!isValidFace(side)) return null;
        return new RadiatorProxyFluidHandler(side);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {

        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§6Heat: §8" + heatEnergy));
        tooltip.add(Component.literal("§7Processing: §8" + isProcessingFlow));

        return true;
    }


    // -------------------------------------------------------------------------
    // NBT persistence
    // -------------------------------------------------------------------------
    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);

        tag.putInt("HeatEnergy", heatEnergy);
        tag.putBoolean("IsProcessingFlow", isProcessingFlow);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);

        heatEnergy = tag.getInt("HeatEnergy");
        isProcessingFlow = tag.getBoolean("IsProcessingFlow");
    }

    // -------------------------------------------------------------------------
    // Network sync (for client-side rendering)
    // -------------------------------------------------------------------------
    private void syncData() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    /**
     * After a real (execute) fluid transfer, poke the FluidTransportBehaviour of
     * every pipe adjacent to the radiator's output face so that they know pressure
     * is arriving from the radiator. This drives the flow animation on glass pipes
     * that are technically bypassed by the radiator's BFS delegation.
     *
     * @param outputSide the direction in which fluid has just been sent (away from
     *                   the radiator, toward downstream pipes/tanks)
     * @param fluid      the fluid that was transferred, used to ensure the pipe
     *                   registers an appropriate flow type
     */
    private void notifyOutputPipes(Direction outputSide, FluidStack fluid) {
        if (level == null || fluid.isEmpty()) return;

        // Walk the BFS path starting from the first block on the output side and
        // notify every pipe we encounter (stopping as soon as we hit a non-pipe).
        java.util.Set<BlockPos> visited = new java.util.HashSet<>();
        java.util.Queue<BlockPos> queue = new java.util.LinkedList<>();
        visited.add(worldPosition);
        queue.add(worldPosition.relative(outputSide));

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            if (visited.contains(current)) continue;
            visited.add(current);

            FluidTransportBehaviour pipe = FluidPropagator.getPipe(level, current);
            if (pipe == null) continue; // Reached a tank/machine — skip, don't break the whole loop!

            // The direction on this pipe that faces back toward the radiator
            // (or the previous pipe in the chain). We tell the pipe that fluid
            // is arriving inbound from that direction.
            BlockState pipeState = level.getBlockState(current);

            // Find which of our visited neighbours connects to this pipe block
            Direction inboundFace = null;
            for (Direction d : Direction.values()) {
                if (visited.contains(current.relative(d)) && pipe.canHaveFlowToward(pipeState, d)) {
                    inboundFace = d;
                    break;
                }
            }
            if (inboundFace == null) continue;

            // Add a small inbound pressure pulse – enough for one tick of flow animation
            pipe.addPressure(inboundFace, true, 1f);

            // Continue down all open directions
            for (Direction d : Direction.values()) {
                if (d == inboundFace) continue;
                if (pipe.canHaveFlowToward(pipeState, d)) {
                    queue.add(current.relative(d));
                }
            }
        }
    }

    // Dedicated inline handler that mirrors and penalizes fluid operations dynamically.
    private class RadiatorProxyFluidHandler implements IFluidHandler {
        private final Direction accessedSide;

        public RadiatorProxyFluidHandler(Direction accessedSide) {
            this.accessedSide = accessedSide;
        }

        private List<IFluidHandler> getOppositeHandlers() {
            if (level == null || isProcessingFlow) return List.of();

            Direction oppositeSide = accessedSide.getOpposite();
            return findAllTerminalHandlers(oppositeSide);
        }

        // Uses a Breadth-First Search (BFS) to gather ALL unique fluid capability endpoints reachable through connected Create pipe lines.
        private List<IFluidHandler> findAllTerminalHandlers(Direction initialDir) {
            List<IFluidHandler> foundHandlers = new java.util.ArrayList<>();
            java.util.Set<BlockPos> visited = new java.util.HashSet<>();
            java.util.Queue<PathNode> queue = new java.util.LinkedList<>();

            // Start scanning from the block immediately adjacent to our output face
            BlockPos startPos = worldPosition.relative(initialDir);
            queue.add(new PathNode(startPos, initialDir.getOpposite()));

            // Mark the radiator itself as visited so searches never circle backward through it
            visited.add(worldPosition);

            int safetyCounter = 0;
            // Cap search at 128 checked blocks to guarantee zero server tick lag on huge setups
            while (!queue.isEmpty() && safetyCounter < 128) {
                PathNode node = queue.poll();
                BlockPos pos = node.pos;
                Direction cameFrom = node.cameFrom;

                if (visited.contains(pos)) continue;
                visited.add(pos);
                safetyCounter++;

                // 1. Check if this destination contains a real fluid handler capability
                IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, cameFrom);
                if (handler != null) {
                    foundHandlers.add(handler);
                    continue; // Destination reached! Stop scanning deeper down this specific branch.
                }

                // 2. If it's not a tank/machine, check if it's a passive Create pipe block we can traverse (such as standard pipes, encased pipes, pumps, etc.)
                BlockState state = level.getBlockState(pos);
                FluidTransportBehaviour pipe = FluidPropagator.getPipe(level, pos);
                if (pipe != null && pipe.canHaveFlowToward(state, cameFrom)) {
                    for (Direction dir : Direction.values()) {
                        if (dir == cameFrom) continue;
                        if (pipe.canHaveFlowToward(state, dir)) {
                            queue.add(new PathNode(pos.relative(dir), dir.getOpposite()));
                        }
                    }
                }
            }

            return foundHandlers;
        }

        @Override
        public int getTanks() {
            // Aggregate total tank count from all endpoints
            return getOppositeHandlers().stream().mapToInt(IFluidHandler::getTanks).sum();
        }

        @Override
        @NotNull
        public FluidStack getFluidInTank(int tank) {
            // Safe fallback logic for structural queries
            List<IFluidHandler> handlers = getOppositeHandlers();
            if (handlers.isEmpty()) return FluidStack.EMPTY;
            return handlers.get(0).getFluidInTank(tank);
        }

        @Override
        public int getTankCapacity(int tank) {
            return getOppositeHandlers().stream().mapToInt(h -> h.getTankCapacity(tank)).sum();
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return getOppositeHandlers().stream().anyMatch(h -> h.isFluidValid(tank, stack));
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty()) return 0;
            List<IFluidHandler> targets = getOppositeHandlers();
            if (targets.isEmpty()) return 0;

            int originalAmount = resource.getAmount();
            int forwardAmount = (int) (originalAmount * (1.0f - CONSUMPTION_RATE));
            if (forwardAmount <= 0 && originalAmount > 0) forwardAmount = 1;

            try {
                isProcessingFlow = true;
                int totalFilledByOpposites = 0;
                int remainingToForward = forwardAmount;

                // Evenly split and fill across all accessible branches/tanks
                for (IFluidHandler opp : targets) {
                    if (remainingToForward <= 0) break;

                    FluidStack forwardedStack = resource.copyWithAmount(remainingToForward);
                    int filled = opp.fill(forwardedStack, action);

                    remainingToForward -= filled;
                    totalFilledByOpposites += filled;
                }

                if (totalFilledByOpposites <= 0) return 0;

                // Scale the returned value back up so the pump accounts for the heat loss
                int totalAccepted = (int) Math.ceil(totalFilledByOpposites / (1.0f - CONSUMPTION_RATE));
                totalAccepted = Math.min(totalAccepted, originalAmount);

                if (action.execute()) {
                    int consumed = totalAccepted - totalFilledByOpposites;
                    if (consumed > 0) onFluidConsumed(consumed);
                    // Poke downstream pipes so they show fluid-flow animation
                    notifyOutputPipes(accessedSide.getOpposite(), resource);
                }

                return totalAccepted;
            } finally {
                isProcessingFlow = false;
            }
        }

        @Override
        @NotNull
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource.isEmpty()) return FluidStack.EMPTY;
            List<IFluidHandler> targets = getOppositeHandlers();
            if (targets.isEmpty()) return FluidStack.EMPTY;

            int targetDrain = resource.getAmount();
            int requiredFromSource = (int) Math.ceil(targetDrain / (1.0f - CONSUMPTION_RATE));

            try {
                isProcessingFlow = true;
                int totalDrainedFromSources = 0;
                FluidStack mergedResult = FluidStack.EMPTY;
                int remainingRequired = requiredFromSource;

                // Draw and pool matching fluids from any branch with stock available
                for (IFluidHandler opp : targets) {
                    if (remainingRequired <= 0) break;

                    FluidStack toDrainStack = resource.copyWithAmount(remainingRequired);
                    FluidStack drained = opp.drain(toDrainStack, action);

                    if (!drained.isEmpty()) {
                        if (mergedResult.isEmpty()) {
                            mergedResult = drained.copy();
                        } else if (FluidStack.isSameFluidSameComponents(mergedResult, drained)) {
                            mergedResult.grow(drained.getAmount());
                        }
                        remainingRequired -= drained.getAmount();
                        totalDrainedFromSources += drained.getAmount();
                    }
                }

                if (totalDrainedFromSources <= 0) return FluidStack.EMPTY;

                int deliveredAmount = (int) (totalDrainedFromSources * (1.0f - CONSUMPTION_RATE));

                if (action.execute()) {
                    int consumed = totalDrainedFromSources - deliveredAmount;
                    if (consumed > 0) onFluidConsumed(consumed);
                    // Poke downstream pipes so they show fluid-flow animation
                    notifyOutputPipes(accessedSide.getOpposite(), mergedResult);
                }

                return mergedResult.copyWithAmount(deliveredAmount);
            } finally {
                isProcessingFlow = false;
            }
        }

        @Override
        @NotNull
        public FluidStack drain(int maxDrain, FluidAction action) {
            if (maxDrain <= 0) return FluidStack.EMPTY;
            List<IFluidHandler> targets = getOppositeHandlers();
            if (targets.isEmpty()) return FluidStack.EMPTY;

            int requiredFromSource = (int) Math.ceil(maxDrain / (1.0f - CONSUMPTION_RATE));

            try {
                isProcessingFlow = true;
                int totalDrainedFromSources = 0;
                FluidStack mergedResult = FluidStack.EMPTY;
                int remainingRequired = requiredFromSource;

                for (IFluidHandler opp : targets) {
                    if (remainingRequired <= 0) break;

                    FluidStack drained = opp.drain(remainingRequired, action);
                    if (!drained.isEmpty()) {
                        if (mergedResult.isEmpty()) {
                            mergedResult = drained.copy();
                        } else if (FluidStack.isSameFluidSameComponents(mergedResult, drained)) {
                            mergedResult.grow(drained.getAmount());
                        }
                        remainingRequired -= drained.getAmount();
                        totalDrainedFromSources += drained.getAmount();
                    }
                }

                if (totalDrainedFromSources <= 0) return FluidStack.EMPTY;

                int deliveredAmount = (int) (totalDrainedFromSources * (1.0f - CONSUMPTION_RATE));

                if (action.execute()) {
                    int consumed = totalDrainedFromSources - deliveredAmount;
                    if (consumed > 0) onFluidConsumed(consumed);
                    // Poke downstream pipes so they show fluid-flow animation
                    notifyOutputPipes(accessedSide.getOpposite(), mergedResult);
                }

                return mergedResult.copyWithAmount(deliveredAmount);
            } finally {
                isProcessingFlow = false;
            }
        }

        /**
         * Small helper record to keep track of positions and the orientation they came from
         * during graph traversal.
         */
        private record PathNode(BlockPos pos, Direction cameFrom) {}
    }
}
